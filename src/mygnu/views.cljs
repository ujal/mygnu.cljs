(ns mygnu.views
  (:require [re-frame.core :as r]
            [reagent.core :as reagent]
            [mygnu.style :as st]))

(defn particle-view [{:keys [id]}]
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [el (reagent/dom-node this)
             data {:el el
                   :width (.-offsetWidth el) :height (-> el .-offsetHeight)
                   :origin-x (-> el .getBoundingClientRect .-left)
                   :origin-y (-> el .getBoundingClientRect .-top)}]
         #_(r/dispatch [:update-particle data id])))
     :reagent-render
     (let [p (r/subscribe [:particle id])]
       (fn []
         [:span (:char @p)]))}))

(defn particles [xs]
  (let [st (st/headings)]
    [:div {:style st}
     (for [p xs]
       ^{:key (:id p)} [particle-view p])]))

;(defn particles-view [{:keys [particle-list]} type]
  ;(map-indexed particle (filter #(= (:type %) type) particle-list)))

(defn main-view []
  (let [content-st (st/content)
        mouse (r/subscribe [:mouse])
        particle-list (r/subscribe [:particle-list])]
    (fn []
      [:div.board {:on-mouse-move #(r/dispatch-sync [:handle-mouse-move %])}
       [:div.content {:style content-st}
        [particles @particle-list]
        [:div.mcoords
         [:span "x:" (:x @mouse)] " "
         [:span "y:" (:y @mouse)]]]])))
