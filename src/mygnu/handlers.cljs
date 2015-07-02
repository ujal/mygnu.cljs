(ns mygnu.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [re-frame.core :as r]
            [mygnu.db :as db]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]))

(def content
  [["INTERACTIVE"]
   ["DESIGN & DEVELOPMENT"]
   ["ABOUT PROJECTS TOOLS"]
   ["Hi, my name is Ujjal"]])

(defn new-particle [id type char]
  {:id id
   :char char
   :type type
   :x 0
   :y 0
   :vx 0
   :vy 0
   :width nil
   :height nil
   :origin-x nil
   :origin-y nil})

(defn now []
  (.getTime (js/Date.)))

(r/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(r/register-handler
 :add-particle
 (fn  [state [_ {:keys [id] :as p}]]
   ;(println (count (:particle-list state)))
   (assoc-in state [:particle-list id] p)))

(defn update-char [state]
  (let [rid (rand-nth (keys (:particle-list state)))]
    (assoc-in state [:particle-list rid :char] (rand-nth (map char (range 65 90))))))

(defn update-color [state e]
  (let [rid (rand-nth (keys (:particle-list state)))]
    (assoc-in state
              [:particle-list rid :color]
              (str "hsla(" "0, 0%,"
                   (min 80 (max 30 (mod e.clientX 100)))
                   "%, 1)"))))

(r/register-handler
 :update-particle
 (fn  [state [_ id m]]
   (update-in state [:particle-list id] #(conj % m))))

(r/register-handler
 :transition
 (fn [state [_ id from to duration]]
   (let [ch (transition (into {} from) (into {} to) {:duration duration})]
     (go-loop []
              (when-let [m (<! ch)]
                (r/dispatch-sync [:update-particle id m])
                (recur)))
     state)))

(r/register-handler
 :mouse-move
 (fn [state [_ e]]
   (r/dispatch [:transition (rand-nth (keys (:particle-list state)))
                {:opacity 0} {:opacity 1} 1500])
   (-> state
       #_(update-color e))))

(r/register-handler
  :time-update
  (fn [state [_ t]]
    (-> state
        (update-color (rand-int 360)))))

