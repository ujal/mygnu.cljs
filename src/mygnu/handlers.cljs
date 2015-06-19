(ns mygnu.handlers
    (:require [re-frame.core :as re-frame]
              [mygnu.db :as db]))

(def content
  [["INTERACTIVE" :hfirst]
   ["DESIGN & DEVELOPMENT" :hsecond]
   ;["ABOUT" :nav]
   ;["PROJECTS" :nav]
   ;["TOOLS" :nav]
   ])

(defn new-particle [char type]
  {:char char
   :type type
   :x 0
   :y 0
   :vx 0
   :vy 0
   :el nil
   :width nil
   :height nil
   :origin-x nil
   :origin-y nil})

(def xf-particles
  (mapcat (fn [[string type]]
            (mapv #(new-particle % type) string))))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   (assoc db/default-db :particle-list (into [] xf-particles content))))

(re-frame/register-handler
 :handle-mouse-move
 (fn [state [_ e]]
   (assoc state :mouse {:x e.clientX :y e.clientY})))

(re-frame/register-handler
  :time-update
  (fn [state [_ timestamp]]
    (println "time-to-update!")
    (-> state
        (assoc
          :cur-time timestamp
          :time-delta (- timestamp (:start-time state)))
        #_update-particles)))

