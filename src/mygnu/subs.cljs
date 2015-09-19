(ns mygnu.subs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]))

(r/register-sub
 :mouse
 (fn [db]
   (reaction (:mouse @db))))

(r/register-sub
 :particle-list
 (fn [db]
   (:particle-list @db)))

(r/register-sub
  :particle
  (fn [db [_ id]]
    (reaction (-> @db :particle-list id))))

(r/register-sub
  :page-particle
  (fn [db [_ id]]
    (reaction (first (filter #(= (:id %) id) (:page-particles @db))))))
