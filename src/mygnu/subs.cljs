(ns mygnu.subs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]))

(r/register-sub
 :mouse
 (fn [db]
   (reaction (:mouse @db))))

(r/register-sub
 :header-particles
 (fn [db]
   (:header-particles @db)))

(r/register-sub
  :header-particle
  (fn [db [_ id]]
    (reaction (-> @db :header-particles id))))

(r/register-sub
  :page-particle
  (fn [db [_ id]]
    (reaction (first (filter #(= (:id %) id) (:page-particles @db))))))

(r/register-sub
  :contact-particle
  (fn [db [_ id]]
    (reaction (-> @db :contact-particles id))))

(r/register-sub
  :logo-s-particle
  (fn [db [_ id]]
    (reaction (-> @db :logo-s-particles id))))
