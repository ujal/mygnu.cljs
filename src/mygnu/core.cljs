(ns ^:figwheel-always mygnu.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as sab :include-macros true]
            [cljs.core.async :refer [put! chan <! timeout]]
            [clojure.data :as data]
            [clojure.string :as string]
            [cljs.repl :as repl]
            [devtools.core :as devtools]
            [mygnu.style :as st]))

(enable-console-print!)
(js/console.clear)
(defonce install-devtools (devtools/install!))



(defonce app-state
  (atom {:mouse {:x 0 :y 0}
         :cur-time 0
         :start-time 0
         :particle-list []}))

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

(defn particle-view [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [el (om/get-node owner)
            p {:el el
               :width (.-offsetWidth el) :height (-> el .-offsetHeight)
               :origin-x (-> el .getBoundingClientRect .-left)
               :origin-y (-> el .getBoundingClientRect .-top)}]
        (om/transact! data #(conj % p))))
    om/IRender
    (render [_]
      (sab/html
        [:span (:char data)]))))


(defn particles-view [{:keys [particle-list]} type]
  (map-indexed #(om/build particle-view %2 {:react-key %1})
               (filter #(= (:type %) type) particle-list)))

(defn handle-mouse-move [e data]
  (om/update! data :mouse {:x e.pageX :y e.pageY}))

(defn app-view [data owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div.board {:on-mouse-move #(handle-mouse-move % data)}
         [:div.content {:style (st/content)}
          [:div {:style (st/headings)}
           [:div (particles-view data :hfirst)]
           [:div (particles-view data :hsecond)]]
          [:ul {:style (st/ul)}
             (for [i (filter #(= (last %) type) content)]
               [:li {:style (st/li) :key (gensym)} (first i)])]
          [:div.mcoords
           [:span "x:" (-> data :mouse :x)] " "
           [:span "y:" (-> data :mouse :y)]]]]))))

(defn time-update [timestamp state]
  (-> state
      (assoc
        :cur-time timestamp
        :time-delta (- timestamp (:start-time state)))
      #_update-particles))

(defn time-loop [time]
  (let [new-state (swap! app-state (partial time-update time))]
    ;(when (:hover new-state)
    (when true
      (go
        (js/requestAnimationFrame time-loop)))))

(def xf-particles
  (mapcat (fn [[string type]]
            (mapv #(new-particle % type) string))))

(defn reset-state [state time]
  (-> state
      (assoc
        :particle-list (into [] xf-particles content)
        :start-time time)))

(defn update-state []
  (js/requestAnimationFrame
    (fn [time]
      (reset! app-state (reset-state @app-state time))
      (time-loop time))))

(defn mount-root []
  (om/root app-view
           app-state
           {:target (. js/document (getElementById "app"))}))

(defn ^:export init []
  (mount-root)
  (update-state))



(defn on-js-reload []
  (mount-root)

  ;(swap! app-state update-in [:mouse :x] inc)

  ;(js/console.log @app-state)
  ;(println @app-state)
  ;(repl/source conj)
  ;(repl/doc defonce)
  )



