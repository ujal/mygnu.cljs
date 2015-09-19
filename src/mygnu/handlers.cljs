(ns mygnu.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [mygnu.db :as db]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [reagent.core :as reagent]))

(def content
  [["INTERACTIVE"]
   ["DESIGN & DEVELOPMENT"]
   ["ABOUT PROJECTS TOOLS"]
   ["Hi, my name is Ujjal"]])

(defn new-particle [id char type el]
  {:id id
   :char char
   :type type
   :x 0
   :y 0
   :vx 0
   :vy 0
   :width (.-offsetWidth el) :height (.-offsetHeight el)
   :origin-x (-> el .getBoundingClientRect .-left)
   :origin-y (-> el .getBoundingClientRect .-top)})

(defn now []
  (.getTime (js/Date.)))

(r/register-handler
  :initialize-db
  (fn  [_ _]
    db/default-db))

(defn add-header-particle [state {:keys [id] :as p}]
  (assoc-in state [:header-particles id] p))

(defn add-page-particle [state p]
  (update state :page-particles #(conj % p)))

(r/register-handler
 :particle-did-mount
 (fn  [state [_ id char type comp]]
   (let [el (reagent/dom-node comp)
         p (new-particle id char type el)]
     (-> state
         (add-header-particle p)
         (add-page-particle p)))))

(let [cs (map char (range 128 254))]
  (defn update-char [state rid]
    (assoc-in state [:header-particles rid :char-r] (rand-nth cs))))

(defn update-color [state e]
  (let [rid (rand-nth (keys (:header-particles state)))]
    (assoc-in state
              [:header-particles rid :color]
              (str "hsla(" "0, 0%,"
                   (min 80 (max 30 (mod e.clientX 100)))
                   "%, 1)"))))

(r/register-handler
  :update-particle
  (fn  [state [_ id m]]
    (-> state
        (update-in [:header-particles id] #(conj % m)))))

(defn transition-fn [state id from to duration]
  (let [ch (transition from to {:duration duration})]
    (go-loop []
             (when-let [m (<! ch)]
               (r/dispatch-sync [:update-particle id m])
               (recur)))
    state))

(r/register-handler
  :mouse-move
  (fn [state [_ e]]
    (when (= (mod (.-clientX e) 7) 0)
      (transition-fn state
                     (rand-nth (keys (:header-particles state)))
                     {:opacity 0}
                     {:opacity 1}
                     1600))
    (-> state
        #_(update-color e)
        #_update-char)))

(defn update-pos [state]
  (let [rid (rand-nth (keys (:header-particles state)))
        cs (map char (range 128 254))]
    (assoc-in state [:page-particles] (rand-nth cs))))

(r/register-handler
  :nav-mouse-move
  (fn [state [_ e]]
    (-> state
        (assoc :is-hover true)
        (update :page-particles (fn [ps]
                                  (mapv #(assoc % :is-settled false) ps))))))


(r/register-handler
  :nav-mouse-out
  (fn [state [_ e]]
    (-> state
        (assoc :is-hover false))))

(r/register-handler
  :time-update
  (fn [state [_ t]]
    (-> state
        #_update-pos)))

