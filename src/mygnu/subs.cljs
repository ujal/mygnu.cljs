(ns mygnu.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as r]))

(r/register-sub
 :mouse
 (fn [db]
   (reaction (:mouse @db))))

(r/register-sub
 :particle-list
 (fn [db]
   (reaction (:particle-list @db))))

(r/register-sub
  :particle
  (fn [db [_ id]]
    (let [particle-list (r/subscribe [:particle-list])]
      (reaction (first (filter #(= id (:id %)) @particle-list))))))

