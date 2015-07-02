(ns mygnu.views
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [re-frame.core :as r]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [mygnu.style :as st]))

(defn now []
  (.getTime (js/Date.)))

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
                  :origin-y (-> el .getBoundingClientRect .-top)}]
           (r/dispatch-sync [:add-particle p])

           (go
             (<! (timeout (rand-int 1500)))
             (r/dispatch [:transition id {:opacity 0} {:opacity 1} 1500]))))
       :reagent-render
       (let [p (r/subscribe [:particle id])
             cs (map char (range 65 254))]
         (fn []
           [:span {:style {:color (:color @p)
                           :opacity (or (:opacity @p) 0)
                           :transform "translateZ(0)"
                           :display "inline-block"
                           :position "relative"
                           :min-width "1.24688rem" ;; space chars
                           }}
            (if (= (:opacity @p) 1)
              (:char @p)
              (rand-nth cs))]))})))

(defn particles [s]
  [:div
   (map-indexed (fn [i c] ^{:key i} [particle-view c]) s)])

(defn main-view []
  (let [s1 (seq "INTERACTIVE")
        s2 (seq "DESIGN & DEVELOPMENT")
        navs ["ABOUT" "TOOLS" "WORK"]
        h [20 194 300]]
    (fn []
      [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
       [:div.content {:style (st/content)}
        [:div {:style (st/headings)}
         (particles s1)
         (particles s2)]
        #_[:ul {:style {:list-style "decimal outside none" :color "#1EAEDB"}}
         (map (fn [n h]
                (println h)
                ^{:key h} [:li [:a {:style {:color (str "hsla(" h " , 76%, 49%, 1)")}} n]])
              navs h)]]])))

