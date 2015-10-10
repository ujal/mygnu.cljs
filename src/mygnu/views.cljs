(ns mygnu.views
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]
            [reagent.core :as reagent]
            [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]]
            [bardo.transition :refer [transition]]
            [mygnu.style :as st]))

(def cs (map char (range 128 254)))

(defn now []
  (.getTime (js/Date.)))

(defn heading-render [p-type id]
  (let [motion (reagent/adapt-react-class (.-Motion js/ReactMotion))
        spring (.-spring js/ReactMotion)
        p (r/subscribe [p-type id])]
    (fn []
      [motion {:defaultStyle {:val 0}
               :style {:val (spring (if (:active @p) 1 0) #js [210 20])}}
       (fn [t]
         (let [o (.-val t)]
           (reagent/as-element
             [:span {:style {:color (if (and (not= o 1)
                                             (not= o 0))
                                      (str "hsla(" (rand-int 180) ",50%,50%,.7)"))
                             :opacity (if (:active @p)
                                        o
                                        (- 1 o))
                             :display "inline-block"
                             :min-width "1.24688rem"}}
              (if (and (not= o 1)
                       (not= o 0))
                (rand-nth cs)
                (:char @p))])))])))

(defn page-render [p-type id]
  (let [motion (reagent/adapt-react-class (.-Motion js/ReactMotion))
        spring (.-spring js/ReactMotion)
        p (r/subscribe [p-type id])]
    (fn []
      [motion {:defaultStyle {:val 0}
               :style {:val (spring (if (:active @p) 1 0) #js [210 20])}}
       (fn [t]
         (let [o (.-val t)]
           (reagent/as-element
             [:span {:style {:color nil
                             ;(if (and (not= o 1)
                                             ;(not= o 0))
                                      ;(str "hsla(" (rand-int 360) ",50%,50%,.7)"))
                             :opacity (if (:active @p)
                                        o
                                        o)
                             :transform "translateZ(0)"
                             :display "inline-block"
                             :min-width "0.998438rem"}}
              (if (not= o 1)
                (rand-nth cs)
                (:char @p))])))])))

(defn nav-render [p-type id]
  (let [motion (reagent/adapt-react-class (.-Motion js/ReactMotion))
        spring (.-spring js/ReactMotion)
        p (r/subscribe [p-type id])]
    (fn []
      [motion {:defaultStyle {:val 0}
               :style {:val (spring (if (:active @p) 1 0) #js [210 20])}}
       (fn [t]
         (let [o (.-val t)]
           (reagent/as-element
             [:span {:on-click #(r/dispatch-sync [:nav-p-click id])
                     :style {:color (if (:active @p) "hsla(0,0%,30%,1)")
                             :opacity nil
                             :transform "translateZ(0)"
                             :display "inline-block"
                             :min-width "0.998438rem"}}
              (:char @p)])))])))

(let [uid (atom 0)]
  (defn particle-view [c p-type]
    (let [id (keyword (str @uid))
          _ (swap! uid inc)]
      (reagent/create-class
        {:component-did-mount
         (fn [this] (r/dispatch-sync [:particle-did-mount id c p-type this]))
         :reagent-render (case p-type
                           :heading (heading-render p-type id)
                           :logo-s  (heading-render p-type id)
                           :logo  (heading-render p-type id)
                           :nav (nav-render p-type id)
                           (page-render p-type id))}))))

(defn header [t]
  [:div {:style st/headings}
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "INTERACTIVE ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "DESIGN ")]
   [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :heading]) "& DEVELOPMENT")]])

  (defn nav-item [page-active item]
    (let [re (re-pattern (str "(?i)(.*)" item))
          match (re-find re (str page-active))]
      [:li {:style st/nav-item
            :className (if match "active")
            :key item}
       [:a {:on-click #(r/dispatch-sync [:nav-item-click item])
            :style st/nav-item-a}
        (map-indexed (fn [i c] ^{:key i} [particle-view c :nav]) item)]]))

(defn nav []
  (let [items ["ABOUT" "TOOLS" "WORK"]
        page-active (r/subscribe [:page-active])]
    [:ul {:style st/nav
              :on-mouse-enter #(r/dispatch-sync [:nav-mouse-enter])
              :on-mouse-leave #(r/dispatch-sync [:nav-mouse-leave])}
     (map (partial nav-item @page-active) items)]))

(defn footer []
  [:div {:style st/footer}
   [:div {:style (conj st/logo
                       {:fontSize "2rem"
                        :margin "0rem"})}
    ;(map-indexed (fn [i c] ^{:key i} [particle-view c :logo-s]) "⦠")
    "⦠"]
   [:div {:style {:fontFamily "Montserrat"
                  :fontSize "1.5rem"}}
    "CONTACT"]
   [:div {:style {:fontSize "1.8rem"}}
    "udschal.imanov@gmail.com"]])

(defn pages []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (r/dispatch [:show-page-p :page-about])
       (r/dispatch [:nav-p-click :34]))
     :reagent-render
     (let [page (r/subscribe [:page-active])
           motion (reagent/adapt-react-class (.-Motion js/ReactMotion))
           spring (.-spring js/ReactMotion)]
       (fn []
         [:div {:style st/page}
          [:div.page {:style {:z-index (if (= @page :page-about) 100 0)}}
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-about]) "HELLO, MY NAME IS UDSCHAL.")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-about]) "I'm a front-end developer from Cologne, Germany.")]
           [:div
            [:span (map-indexed (fn [i c] ^{:key i} [particle-view c :page-about]) "Currently crafting ")]
            [:a {:href "http://keyput.com" :target "_blank"}
             (map-indexed (fn [i c] ^{:key i} [particle-view c :page-about]) "keyput.com")]]]
          [:div.page {:style {:z-index (if (= @page :page-tools) 100 0)}}
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-tools]) "HTML5/CSS3/LESS/SASS")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-tools]) "JavaScript/CoffeScript/ClojureScript")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-tools]) "Backbone/React/Node/Reagent")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-tools]) "Ruby/PHP")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-tools]) "Sinatra/Rails Slim/CakePHP")]]
          [:div.page {:style {:z-index (if (= @page :page-work) 100 0)}}
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-work]) "WORK")]
           [:div (map-indexed (fn [i c] ^{:key i} [particle-view c :page-work]) "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod.")]]]))}))

(defn main-view []
  (fn []
    [:div.board {:on-mouse-move #(r/dispatch-sync [:mouse-move %])}
     [:div.content {:style st/content}
      [header]
      [:div {:style st/logo}
       (map-indexed (fn [i c] ^{:key i} [particle-view c :logo]) "⦠")]
      [nav]
      [pages]
      [footer]]]))

