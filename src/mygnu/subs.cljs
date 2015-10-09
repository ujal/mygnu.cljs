(ns mygnu.subs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [mygnu.macros :refer [timep]])
  (:require [re-frame.core :as r]))

(r/register-sub
  :heading
  (fn [db [_ id]]
    (reaction (-> @db :heading id))))

(r/register-sub
  :page-about
  (fn [db [_ id]]
    (reaction (first (filter #(= (:id %) id) (:page-about @db))))))

(r/register-sub
  :page-tools
  (fn [db [_ id]]
    (reaction (first (filter #(= (:id %) id) (:page-tools @db))))))

(r/register-sub
  :page-work
  (fn [db [_ id]]
    (reaction (first (filter #(= (:id %) id) (:page-work @db))))))

(r/register-sub
  :logo
  (fn [db [_ id]]
    (reaction (-> @db :logo id))))

(r/register-sub
  :logo-s
  (fn [db [_ id]]
    (reaction (-> @db :logo-s id))))

(r/register-sub
  :page-active
  (fn [db [_]]
    (reaction (-> @db :page-active))))

(r/register-sub
  :nav
  (fn [db [_ id]]
    (reaction (-> @db :nav id))))
