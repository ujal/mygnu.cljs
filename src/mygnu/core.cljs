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

(def texts
  [["Interactive" :hfirst]
   ["Design & Development" :hsecond]
   ["ABOUT" :nav]
   ["PROJECTS" :nav]
   ["TOOLS" :nav]])

(defonce app-state
  (atom {:mouse {:x 0 :y 0}
         :particle-list []}))

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

(defn particle-type [type]
  #(= (:type %) type))

(defn text-type [type]
  #(= (last %) type))

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
        [:span (:char data)]))))


(defn build-particles [{:keys [particle-list]} type]
  (map-indexed #(om/build particle-view %2 {:react-key %1})
               (filter (particle-type type) particle-list)))

(defn handle-mouse-move [e data]
  (om/update! data :mouse {:x e.pageX :y e.pageY}))

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
         [:ul {:class "nav"} (for [i (filter (text-type :nav) texts)]
                [:li {:key (gensym)} (first i)])]
         [:div.mcoords
          [:span "x:" (-> data :mouse :x)] " "
          [:span "y:" (-> data :mouse :y)]]]))))


(defn create-particles [[string type]]
  (mapv #(new-particle % type) string))

(def xf-particles
  (mapcat create-particles))

(defn particles [state]
  (-> state
      (assoc :particle-list (into [] xf-particles texts))))

(defonce fill-state
  (swap! app-state particles))

(om/root app-view
         app-state
         {:target (. js/document (getElementById "app"))})



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
   ;(swap! app-state update-in [:mouse :x] inc)
)

(js/console.log @app-state)
;(println @app-state)
;(repl/source conj)
;(repl/doc defonce)


