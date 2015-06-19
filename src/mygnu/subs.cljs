(ns mygnu.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
 :mouse
 (fn [db]
   (reaction (:mouse @db))))

