(ns mygnu.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [mygnu.db :as db]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [reagent.core :as reagent]
            [clojure.set :refer [difference]]))

(defn new-particle [id char type el]
  {:id id
   :char char
   :type type
   :x nil
   :y nil
   :vx nil
   :vy nil
   :width (.-offsetWidth el)
   :height (.-offsetHeight el)
   :origin-x (-> el .getBoundingClientRect .-left)
   :origin-y (-> el .getBoundingClientRect .-top)})

(defn now []
  (.getTime (js/Date.)))

(r/register-handler
  :initialize-db
  (fn  [_ _]
    db/default-db))

(defn add-particle [state type {:keys [id] :as p}]
  (if (re-matches #":page(.*)" (str type))
    (update state type #(conj % p))
    (assoc-in state [type id] p)))

(r/register-handler
  :particle-did-mount
  (fn  [state [_ id char type comp]]
    (let [el (reagent/dom-node comp)
          p (new-particle id char type el)]
      (add-particle state type p))))

(let [cs (map char (range 128 254))]
  (defn update-char [state rid]
    (assoc-in state [:heading rid :char-r] (rand-nth cs))))

(defn update-color [state e]
  (let [rid (rand-nth (keys (:heading state)))]
    (assoc-in state
              [:heading rid :color]
              (str "hsla(" "0, 0%,"
                   (min 80 (max 30 (mod e.clientX 100)))
                   "%, 1)"))))

(r/register-handler
  :update-heading
  (fn  [state [_ id m]]
    (-> state
        (update-in [:heading id] #(conj % m)))))


(r/register-handler
  :update-logo-s
  (fn  [state [_ id m]]
    (-> state
        (update-in [:logo-s id] #(conj % m)))))

(defn transition-fn [state id from to duration handlk]
  (let [ch (transition from to {:duration duration})]
    (go-loop []
             (when-let [m (<! ch)]
               (r/dispatch-sync [handlk id m])
               (recur))))
  state)

(r/register-handler
  :mouse-move
  (fn [state [_ e]]
    (let [modulo (mod (.-clientX e) 50)]
      (cond
        (= modulo 0) (-> state
                         (transition-fn
                           (rand-nth (keys (:heading state)))
                           {:opacity 0}
                           {:opacity 1}
                           1600
                           :update-heading)
                         (transition-fn
                           (rand-nth (keys (:logo-s state)))
                           {:opacity 0}
                           {:opacity 1}
                           3600
                           :update-logo-s)
                         #_(update-color e)
                         #_update-char)
         (> modulo 0) (-> state)))))

(r/register-handler
  :nav-mouse-move
  (fn [state [_ e]]
    (-> state
        (assoc :is-hover true))))


(r/register-handler
  :nav-mouse-out
  (fn [state [_ e]]
    (-> state
        (assoc :is-hover false))))

(r/register-handler
  :transition-page-out
  (fn [state [_ pagek m _]]
    (-> state
        (update pagek (fn [ps]
                        (mapv (fn [p]
                                (if (not= (:opacity p) 0)
                                  (conj p m)
                                  p))
                              ps))))))

(defn transition-page-out [state pagek]
  (let [ids (mapv #(:id %) (pagek state))
        sids (shuffle ids)
        rids (subvec sids (js/parseInt (* (count sids) 0.9)))
        ch (transition {:opacity 1} {:opacity 0} {:duration 0})]
    (go-loop []
             (when-let [m (<! ch)]
               (r/dispatch-sync [:transition-page-out pagek m rids])
               (recur))))
  state)

(r/register-handler
  :transition-page-in
  (fn [state [_ pagek m rids]]
    (-> state
        (update pagek (fn [ps]
                        (mapv (fn [p]
                                (if ((into #{} rids) (:id p))
                                  (conj p m)
                                  p))
                              ps))))))

(defn transition-page-in [state pagek]
  (let [ids (remove nil? (mapv (fn [p]
                                 (if (not= (:opacity p) 1)
                                   (:id p)))
                               (pagek state)))
        sids (shuffle ids)
        ;rids (if (< (count ids) (js/parseInt (* (count (pagek state)) 0.2)))
               ;ids
               ;(subvec sids (js/parseInt (* (count sids) 0.9))))
        rids (subvec sids (js/parseInt (* (count sids) 0.9)))
        ch (transition {:opacity 0} {:opacity 1} {:duration 0})]
    (go-loop []
             (when-let [m (<! ch)]
               (r/dispatch-sync [:transition-page-in pagek m rids])
               (if (and (= (:opacity m) 1)
                        (> (count ids) 0))
                 (r/dispatch [:transition-page-in-end pagek]))
               (recur))))
  state)

(defn hide-page [state pagek]
    (let [ids (map #(:id %) (pagek state))
          sids (shuffle ids)
          rids (subvec sids (js/parseInt (* (count sids) 0.1)))]
      (-> state
          (update pagek (fn [ps]
                          (mapv (fn [p]
                                  (conj p {:opacity 0})
                                  #_(if ((into #{} rids) (:id p))
                                      (conj p {:opacity 0})
                                      p))
                                ps))))))

(r/register-handler
  :transition-page-in-end
  (fn [state [_ pagek]]
    (transition-page-in state pagek)
    state))

(r/register-handler
  :show-some
  (fn [state [_ pagek m]]
    (-> state
        (update pagek (fn [ps]
                        (mapv (fn [p]
                                (if (not= (:opacity p) 1)
                                  (conj p m)
                                  p))
                              ps))))))

(r/register-handler
  :nav-item-click
  (fn [state [_ e]]
    (let [last-pagek (:page-active state)
          page (-> e .-target .-id)
          pages {"ABOUT" :page-about
                 "TOOLS" :page-tools
                 "WORK"  :page-work}
          new-pagek (pages page)]
      (-> state
          (hide-page last-pagek)
          (hide-page new-pagek)
          (assoc :page-active new-pagek)
          (transition-page-in new-pagek)))))

(r/register-handler
  :time-update
  (fn [state [_ t]]
    (-> state
        )))

