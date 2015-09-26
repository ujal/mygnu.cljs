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

(let [uid (atom 0)]
  (defn particle-view [c type]
    (let [id (keyword (str @uid))
          _ (swap! uid inc)]
      (reagent/create-class
        {:component-did-mount
         (fn [this]
           (r/dispatch-sync [:particle-did-mount id c type this]))
         :reagent-render
         (let [p (r/subscribe [type id])
               cs (map char (range 128 254))]
           (fn []
             (let [opacity (or (:opacity @p) 1)]
               [:span {:style {:color (:color @p)
                               :opacity opacity
                               :display "inline-block"
                               :position "relative"
                               :min-width (if (= type :header-particle)
                                            "1.24688rem"
                                            "0.998438rem")}}
                (if (< opacity 1)
                  (rand-nth cs)
                  (:char @p))])))}))))
(defn header []
  [:div {:style (st/headings)}
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :header-particle]) "INTERACTIVE ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :header-particle]) "DESIGN ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :header-particle]) "& DEVELOPMENT")]])

(defn nav-item [item h] ^{:key item}
  [:li {:style (st/nav-item)}
   [:a {:on-click #(r/dispatch-sync [:nav-item-click %])
        :id item
        :style (conj (st/nav-item-a)
                     {:color (str "hsla(0, 0%," h "%, 1)")})}
    item]])

(defn nav []
  (let [list ["ABOUT" "TOOLS" "WORK"]
        hues [30 50 50]]
    [:ul {:on-mouse-move #(r/dispatch-sync [:nav-mouse-move %])
          :on-mouse-out #(r/dispatch-sync [:nav-mouse-out %])
          :style (st/nav)}
     (map nav-item list hues)]))

(defn footer []
  [:div {:style (st/footer)}
   [:div {:style (conj (st/logo)
                       {:fontSize "2rem"
                        :margin "0rem"})}
    (map-indexed (fn [i c] ^{:key i} [particle-view c :logo-s-particle]) "⦠")]
   [:div {:style {:fontFamily "Montserrat"
                  :fontSize "1.5rem"}}
    "CONTACT"]
   [:div {:style {:fontSize "1.8rem"}}
    "udschal.imanov@gmail.com"]])

(defn page []
  (let [page (r/subscribe [:page-active])]
    (fn []
      [:div {:style (st/page)}
       [:div {:style {:display (if (= @page "ABOUT") "block" "none")}}
        [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-particle]) "HELLO, MY NAME IS UDSCHAL.")]
        [:div "I'm a front-end developer from Cologne, Germany."]
        [:div "Currently crafting keyput.com"]]
       [:div {:style {:display (if (= @page "TOOLS") "block" "none")}}
        [:div "TOOLS"]
        [:div "I'm a front-end developer from Cologne, Germany."]
        [:div "Currently crafting keyput.com"]]
       [:div {:style {:display (if (= @page "WORK") "block" "none")}}
        [:div "WORK"]
        [:div "I'm a front-end developer from Cologne, Germany."]
        [:div "Currently crafting keyput.com"]]])))

(defn main-view []
  (fn []
    [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
     [:div.content {:style (st/content)}
      [header]
      [:div {:style (st/logo)}
       "⦠"]
      [nav]
      [page]
      [footer]]]))

