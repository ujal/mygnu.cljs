(ns mygnu.views
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [re-frame.core :as r]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [mygnu.style :as st]))

(defn particle-view [c]
  (let [id (gensym)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (let [el (reagent/dom-node this)
               p {:id id
                  :char c
                  :width (.-offsetWidth el) :height (.-offsetHeight el)
                  :origin-x (-> el .getBoundingClientRect .-left)
                  :origin-y (-> el .getBoundingClientRect .-top)
                  }]
           (r/dispatch [:add-particle p])
           (go
             (<! (timeout (rand-int 1500)))
             (r/dispatch-sync [:transition id :opacity 0 1 1500]))))
       :reagent-render
       (let [p (r/subscribe [:particle id])]
         (fn []
           [:span {:style {:color (:color @p)
                           :opacity (or (:opacity @p) 0)
                           :transform "translateZ(0)"
                           :display "inline-block"
                           :min-width ".475rem" ;; space chars
                           }}
            (or  (:char @p) c)]))})))

(defn particles [s]
  [:div
   (map-indexed (fn [i c] ^{:key i} [particle-view c]) s)])

(defn main-view []
  (let [s1 (seq "INTERACTIVE")
        s2 (seq "DESIGN & DEVELOPMENT")]
    (fn []
      [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
       [:div.content {:style (st/content)}
        [:div {:style (st/headings)}
         (particles s1)
         (particles s2)]
        #_[:div {:style (st/board)}
         (for [y (range 9)]
           ^{:key y} [particles "XXXXXXXXXXXXXXXXXXXXXXXXXXX"])]]])))

