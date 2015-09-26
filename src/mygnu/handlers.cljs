(ns mygnu.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [mygnu.db :as db]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [reagent.core :as reagent]))

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
  (if (= type :page-about)
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
               (recur)))
    state))

(r/register-handler
  :mouse-move
  (fn [state [_ e]]
    (let [modulo (mod (.-clientX e) 7)]
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

(defn page-fade-out [page]
  (let [ks expr]
    (transition-fn
      (rand-nth (keys (:page-about state)))
      {:opacity 0}
      {:opacity 1}
      1600
      :update-page)))

(r/register-handler
  :nav-item-click
  (fn [state [_ e]]
    (let [page (-> e .-target .-id)]
      (-> state
          ;(page-fade-out (:page-active state))
          (assoc :page-active page)
          ))))

(r/register-handler
  :time-update
  (fn [state [_ t]]
    (-> state
        )))

