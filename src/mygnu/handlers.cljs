(ns mygnu.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [mygnu.db :as db]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [reagent.core :as reagent]
            [clojure.set :refer [difference]]))

(defn new-particle [id char p-type el]
  {:id id
   :char char
   :type p-type
   :x nil
   :y nil
   :vx nil
   :vy nil
   :width (.-offsetWidth el)
   :height (.-offsetHeight el)
   :origin-x (-> el .getBoundingClientRect .-left)
   :origin-y (-> el .getBoundingClientRect .-top)
   :active false})

(defn now []
  (.getTime (js/Date.)))

(r/register-handler
  :initialize-db
  (fn  [_ _]
    db/default-db))

(defn add-particle [state p-type {:keys [id char] :as p}]
  (if (re-matches #":page(.*)" (str p-type))
    (update state p-type #(conj % p))
    (assoc-in state [p-type id] p)))

(r/register-handler
  :particle-did-mount
  (fn  [state [_ id char p-type comp]]
    (let [el (reagent/dom-node comp)
          p (new-particle id char p-type el)]
      (add-particle state p-type p))))

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

(defn trigger-heading-rand [state]
  (let [id (rand-nth (keys (:heading state)))]
    (update-in state [:heading id]
               (fn [p]
                 (if (:active p)
                   (conj p {:active (not (:active p))})
                   (conj p {:active true}))))))

(defn trigger-logo [state]
  (let [id (rand-nth (keys (:logo state)))]
    (update-in state [:logo id]
               (fn [p]
                 (if (:active p)
                   (conj p {:active (not (:active p))})
                   (conj p {:active true}))))))

(defn trigger-logo-s [state]
  (let [id (rand-nth (keys (:logo-s state)))]
    (update-in state [:logo-s id]
               (fn [p]
                 (if (:active p)
                   (conj p {:active (not (:active p))})
                   (conj p {:active true}))))))

(r/register-handler
  :mouse-move
  (fn [state [_ e]]
    (let [modulo (mod (.-clientX e) 20)]
      (cond
        (= modulo 0) (-> state
                         trigger-heading-rand
                         trigger-logo
                         trigger-logo-s)
        (> modulo 0) (-> state)))))

(defn show-page-rand [{:keys [page-active rids] :as state}]
  (update state page-active (fn [ps]
                              (map (fn [p]
                                     (if ((into #{} rids) (:id p))
                                       (conj p {:active true})
                                       p))
                                   ps))))

(defn hide-page-rand [{:keys [page-active] :as state}]
  (let [ids (filter identity (map (fn [p]
                                 (if (not= (:opacity p) 1)
                                   (:id p)))
                               (page-active state)))
        sids (shuffle ids)
        rids (subvec sids (js/parseInt (* (count sids) 0.8)))]
    (-> state
        (assoc :rids rids)
        (update page-active (fn [ps]
                              (map (fn [p]
                                     (if ((into #{} rids) (:id p))
                                       (conj p {:active false})
                                       p))
                                   ps))))))

(r/register-handler
  :nav-mouse-enter
  (fn [state [_]]
    (-> state
        (assoc :is-hover true)
        #_hide-page-rand)))


(r/register-handler
  :nav-mouse-leave
  (fn [state [_]]
    (-> state
        (assoc :is-hover false)
        #_show-page-rand)))

(defn transition-page-in [state pagek]
  (let [ids (filter identity (map (fn [p]
                                 (if (not= (:opacity p) 1)
                                   (:id p)))
                               (pagek state)))
        sids (shuffle ids)
        rids (subvec sids (js/parseInt (* (count sids) 0.7)))
        ch (transition {:opacity 0} {:opacity 1} {:duration 100 :easing :linear})]
    (go-loop []
             (when-let [m (<! ch)]
               (r/dispatch-sync [:transition-page-in pagek m rids])
               (if (and (= (:opacity m) 1)
                        (> (count ids) 0))
                 (r/dispatch [:transition-page-in-end pagek]))
               (recur))))
  state)


(defn hide-page [state pagek]
  (update state pagek (fn [ps]
                  (map (fn [p]
                         (conj p {:active false}))
                       ps))))

(defn show-page-p [state pagek]
  (let [ids (remove nil? (map (fn [p]
                                (if (not (:active p))
                                  (:id p)))
                              (pagek state)))
        sids (shuffle ids)
        rids (subvec sids (js/parseInt (* (count sids) 0.66)))]
    (go
      (r/dispatch-sync [:transition-page-p pagek {:active true} rids])
      (<! (timeout 100))
      (if (> (count ids) 0)
        (r/dispatch [:show-page-p pagek]))))
  state)

(r/register-handler
  :transition-page-p
  (fn [state [_ pagek m rids]]
    (update state pagek (fn [ps]
                    (map (fn [p]
                           (if ((into #{} rids) (:id p))
                             (conj p m)
                             p))
                         ps)))))

(r/register-handler
  :show-page-p
  (fn [state [_ pagek]]
    (show-page-p state pagek)))

(r/register-handler
  :nav-p-click
  (fn [state [_ id]]
    (-> state
        (assoc :nav (into {} (map (fn [[k v]]
                                    (hash-map k (conj v {:active false})))
                                  (:nav state))))
        (update-in [:nav id] (fn [p]
                               (conj p {:active true}))))))

(r/register-handler
  :nav-item-click
  (fn [state [_ page]]
    (let [pages {"ABOUT" :page-about
                 "TOOLS" :page-tools
                 "WORK"  :page-work}
          last-pagek (:page-active state)
          new-pagek (pages page)]
      (-> state
          (hide-page last-pagek)
          (hide-page new-pagek)
          (show-page-p new-pagek)
          (assoc :page-active new-pagek)))))

(r/register-handler
  :time-update
  (fn [state [_ t]]
    (-> state
        )))

