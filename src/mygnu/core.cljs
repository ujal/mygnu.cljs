(ns ^:figwheel-always mygnu.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! timeout]]
            [reagent.core :as reagent]
            [re-frame.core :as r]
            [mygnu.handlers]
            [mygnu.subs]
            [mygnu.views :as views]
            [devtools.core :as devtools]))

(enable-console-print!)
;(js/console.clear)
(defonce install-devtools (devtools/install!))


(defn time-loop [time]
  (r/dispatch [:time-update time])
  ;(when (:hover new-state)
  (when true
    (go
      ;(<! (timeout 250))
      (js/requestAnimationFrame time-loop))))

(defn update! []
  (js/requestAnimationFrame
    (fn [time]
      (time-loop time))))

(defn mount-root []
  (reagent/render [views/main-view]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (r/dispatch-sync [:initialize-db])
  (mount-root)
  #_(update!))



(defn on-js-reload []
  (mount-root))
