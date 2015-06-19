(ns ^:figwheel-always mygnu.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! timeout]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [mygnu.handlers]
            [mygnu.subs]
            [mygnu.views :as views]
            [devtools.core :as devtools]))

(enable-console-print!)
(js/console.clear)
(defonce install-devtools (devtools/install!))


(defn time-loop [time]
  (re-frame/dispatch [:time-update time])
  ;(when (:hover new-state)
  (when true
    (go
      (<! (timeout 1000))
      (js/requestAnimationFrame time-loop))))

(defn update-state []
  (js/requestAnimationFrame (fn [time]
                              (time-loop time))))

(defn mount-root []
  (reagent/render [views/main-view]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root)
  #_(update-state))



(defn on-js-reload []
  (init))



