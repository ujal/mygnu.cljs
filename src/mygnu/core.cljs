(ns ^:figwheel-always mygnu.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as sab :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]
            [cljs.repl :as repl]
            [devtools.core :as devtools]))

(enable-console-print!)
(js/console.clear)
(defonce install-devtools (devtools/install!))


;; define your app data so that it doesn't get over-written on reload

(defonce app-state
  (atom {:mouse {:x 0 :y 0}
         :particle-list []}))

(def text
  [{:s "Interactive" :stype :hfirst}
   {:s "Design & Development" :stype :hsecond}])

(defn new-particle [s t]
  {:string s
   :type t
   :x 0
   :y 0
   :vx 0
   :vy 0
   :el nil
   :width nil
   :height nil
   :origin-x nil
   :origin-y nil})

(defn create-particles [{:keys [s stype]}]
  (mapv #(new-particle % stype) s))

(defn add-particles [xs]
  (swap! app-state update-in [:particle-list] into xs))

(let [ps-hfirst (create-particles (text 0))
      ps-hsecond (create-particles (text 1))]
  (defonce populate-particles
    ((add-particles ps-hfirst)
     (add-particles ps-hsecond))))


(defn handle-mouse-move [e data]
  (om/update! data :mouse {:x e.pageX :y e.pageY}))

(defn particle-view [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [el (om/get-node owner)
            p {:el el
               :width (.-offsetWidth el) :height (.-offsetHeight el)
               :origin-x (-> el .getBoundingClientRect .-left)
               :origin-y (-> el .getBoundingClientRect .-top)}]
        (om/transact! data #(conj % p))))
    om/IRender
    (render [_]
      (sab/html
        [:span (:string data)]))))


(defn build-particles [{:keys [particle-list]} type]
  (map-indexed #(om/build particle-view %2 {:react-key %1})
               (filter #(= (:type %) type) particle-list)))

(defn app-view [data owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div.board {:on-mouse-move #(handle-mouse-move % data)}
         [:div.headers
          [:div.header (build-particles data :hfirst)]
          [:div.header (build-particles data :hsecond)]]
         [:br]
         [:div.mcoords
          [:span "x:" (-> data :mouse :x)] " "
          [:span "y:" (-> data :mouse :y)]]]))))


(om/root app-view
         app-state
         {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:mouse :x] inc)
)

(js/console.log @app-state)
;(println @app-state)
;(repl/source conj)
;(repl/doc defonce)


