(ns mygnu.views
  (:require [re-frame.core :as re-frame]
            [mygnu.style :as st]))

;(defn particle-view [data owner]
  ;(reify
    ;om/IDidMount
    ;(did-mount [_]
      ;(let [el (om/get-node owner)
            ;p {:el el
               ;:width (.-offsetWidth el) :height (-> el .-offsetHeight)
               ;:origin-x (-> el .getBoundingClientRect .-left)
               ;:origin-y (-> el .getBoundingClientRect .-top)}]
        ;(om/transact! data #(conj % p))))
    ;om/IRender
    ;(render [_]
      ;(sab/html
        ;[:span (:char data)]))))

;(defn particle [i data]
  ;(om/build particle-view data {:react-key i}))

;(defn particles-view [{:keys [particle-list]} type]
  ;(map-indexed particle (filter #(= (:type %) type) particle-list)))

(defn main-view []
  (let [mouse (re-frame/subscribe [:mouse])
        content-st (st/content)
        headings-st (st/headings)]
    (fn []
      [:div.board {:on-mouse-move #(re-frame/dispatch-sync [:handle-mouse-move %])}
       [:div.content {:style content-st}
        [:div.mcoords
         [:span "x:" (:x @mouse)] " "
         [:span "y:" (:y @mouse)]]]])))
