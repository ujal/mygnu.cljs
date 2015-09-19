(ns mygnu.views
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [mygnu.style :as st]))

(defn now []
  (.getTime (js/Date.)))

(defn particle-view [c t]
  (let [id (gensym)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (let [el (reagent/dom-node this)
               p {:id id
                  :type t
                  :char c
                  :width (.-offsetWidth el) :height (.-offsetHeight el)
                  :origin-x (-> el .getBoundingClientRect .-left)
                  :origin-y (-> el .getBoundingClientRect .-top)}]
           (r/dispatch-sync [:add-particle p])
           #_(r/dispatch-sync [:particle-did-mount id char type])))
       :reagent-render
       (let [p (if (= t :heading)
                 (r/subscribe [:particle id])
                 (r/subscribe [:page-particle id]))
             cs (map char (range 128 254))]
         (fn []
           (when (= t :page) 
             (pr (:char @p)))
           [:span {:style {:color (:color @p)
                           :opacity (or (:opacity @p) 1)
                           :display "inline-block"
                           :position "relative"
                           :min-width "1.24688rem"}}
            (if (< (or (:opacity @p) 1) 1)
              (rand-nth cs)
              (:char @p))]))})))

(defn nav-item [item h] ^{:key h}
  [:li {:style {:display "inline-block"
                :margin-right (str (* 1.5 1.6) "rem")}}
   [:a {:style { :color (str "hsla(0, 0%, " h "%, 1)")}}
    item]])

(defn nav []
  (let [list ["ABOUT" "TOOLS" "WORK"]
        hues [30 40 50]]
    [:ul {:on-mouse-move #(r/dispatch-sync [:nav-mouse-move %])
          :on-mouse-out #(r/dispatch-sync [:nav-mouse-out %])
          :style {:list-style "decimal outside none"
                  :color "#1EAEDB"}}
     (map nav-item list hues)]))

(defn header []
  [:div {:style (st/headings)}
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "INTERACTIVE ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "DESIGN & DEVELOPMENT")]])

(defn main-view []
  (fn []
    [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
     [:div.content {:style (st/content)}
      [header]
      [:div {:style (st/logo)}
       "*"]
      [nav]
      [:div {:style (st/page)}
       [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page]) "HELLO, MY NAME IS UDSCHAL.")]
       [:div "I'm a front-end developer from Cologne, Germany."]
       [:div "Currently crafting keyput.com"]]]]))

