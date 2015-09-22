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

(defn particle-view [c type]
  (let [id (gensym)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (r/dispatch-sync [:particle-did-mount id c type this]))
       :reagent-render
       (let [p (case type
                 :heading (r/subscribe [:header-particle id])
                 :page (r/subscribe [:page-particle id])
                 :contact (r/subscribe [:contact-particle id])
                 :logo-s (r/subscribe [:logo-s-particle id]))
             cs (map char (range 128 254))]
         (fn []
           (let [opacity (or (:opacity @p) 1)]
             [:span {:style {:color (:color @p)
                             :opacity opacity
                             :display "inline-block"
                             :position "relative"
                             :min-width (if (= type :heading)
                                          "1.24688rem"
                                          "0.998438rem")}}
              (if (< opacity 1)
                (rand-nth cs)
                (:char @p))])))})))
(defn header []
  [:div {:style (st/headings)}
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "INTERACTIVE ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "DESIGN ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "& DEVELOPMENT")]])

(defn nav-item [item h] ^{:key item}
  [:li {:style (st/nav-item)}
   [:a {:style {:color (str "hsla(0, 0%," h "%, 1)")}}
    item]])

(defn nav []
  (let [list ["ABOUT" "TOOLS" "WORK"]
        hues [30 55 55]]
    [:ul {:on-mouse-move #(r/dispatch-sync [:nav-mouse-move %])
          :on-mouse-out #(r/dispatch-sync [:nav-mouse-out %])
          :style (st/nav)}
     (map nav-item list hues)]))

(defn footer []
  [:div {:style (st/footer)}
   [:div {:style (conj (st/logo) {:fontSize "2rem"
                                  :margin "0rem"})}
           (map-indexed (fn [i c] ^{:key i} [particle-view c :logo-s]) "⦠")]
   [:div {:style {:fontFamily "Montserrat"
                  :fontSize "1.5rem"}}
    (map-indexed (fn [i c] ^{:key i} [particle-view c :contact]) "CONTACT")]
   [:div {:style {:fontSize "1.8rem"}}
    "udschal.imanov@gmail.com"]])

(defn main-view []
  (fn []
    [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
     [:div.content {:style (st/content)}
      [header]
      [:div {:style (st/logo)}
       "⦠"]
      [nav]
      [:div {:style (st/page)}
       [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page]) "HELLO, MY NAME IS UDSCHAL.")]
       [:div "I'm a front-end developer from Cologne, Germany."]
       [:div "Currently crafting keyput.com"]]
      [footer]]]))

