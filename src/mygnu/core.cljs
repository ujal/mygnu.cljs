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

(def string-list [{:string "Interactive" :type :header-first}
                  {:string "Design & Development" :type :header-second}

                  {:string "ABOUT" :type :nav}
                  {:string "PROJECTS":type :nav}
                  {:string "TOOLS":type :nav}])

(defonce app-state
  (atom {:mouse {:x 0 :y 0}
         :particle-list []}))

(defn particle-list []
  (om/ref-cursor (:particle-list (om/root-cursor app-state))))

(defn new-particle []
  {:char nil
   :x 0
   :y 0
   :vx 0
   :vy 0
   :el nil
   :width nil
   :height nil
   :origin-x nil
   :origin-y nil})

(defn handle-mouse-move [e data]
  (om/update! data :mouse {:x e.pageX :y e.pageY}))

;(defn update-particle [data owner]
  ;(let [el (om/get-node owner)
        ;m {:el el
           ;:width (.-offsetWidth el) :height (.-offsetHeight el)
           ;:origin-x (-> el .getBoundingClientRect .-left)
           ;:origin-y (-> el .getBoundingClientRect .-top)}]
    ;(om/transact! data #(into % m))))

;(defn particle-view [data owner]
  ;(reify
    ;om/IDidMount
    ;(did-mount [_]
      ;(update-particle data owner))
    ;om/IRender
    ;(render [_]
      ;(sab/html
        ;[:span (:char data)]))))

(defn particle-view [data owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [el (om/get-node owner)
            m {:char data
               :el el
               :width (.-offsetWidth el) :height (.-offsetHeight el)
               :origin-x (-> el .getBoundingClientRect .-left)
               :origin-y (-> el .getBoundingClientRect .-top)}
            m (conj m (new-particle))]
        (om/transact! (particle-list) #(conj % m))))
    om/IRender
    (render [_]
      (sab/html
        [:span data]))))

;(defn particles-template [{:keys [particle-list]} type]
  ;(map-indexed #(om/build particle-view %2 {:react-key %1})
               ;(filter #(= (:type %) type) particle-list)))

(defn particles [s]
  (map-indexed #(om/build particle-view %2 {:react-key %1}) s))

(defn app-view [data owner]
  (reify
    om/IRender
    (render [_]
      (sab/html
        [:div.board {:on-mouse-move #(handle-mouse-move % data)}
         [:div.headers
          [:div.header (particles "Interactive")]
          [:div.header (particles "Design & Development")]
          ;[:div.header (particles-template data :header-first)]
          ;[:div.header (particles-template data :header-second)]
          ]
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


