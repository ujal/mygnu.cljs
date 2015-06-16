(ns mygnu.style
  (:require [clojure.set :refer [rename-keys]]
            [camel-snake-kebab.core :as csk]))

(defn rm [n]
  (str n "rem"))

(defn px [n]
  (str n "px"))

(def transform-prop
  (js/Modernizr.prefixed "transform"))

(defn prefix [styles]
  (rename-keys styles {:transform transform-prop}))

(defn camelize [styles]
  (into {} (for [[k v] styles] (vector (csk/->camelCase k) v))))

(def font-size-base 1.5)
(def line-height-base 1.6)

(defn content []
  (-> {:transform "translateY(-50%)"
       :position "relative"
       :top "45%"}
      prefix))

(defn headings []
  (-> {:font-size (rm (* font-size-base 1.2))
       :font-family "Montserrat"
       :font-weight "bold"
       :margin "1.2rem"}
      prefix
      camelize))

(defn ul []
  (-> {:list-style "none"
       :margin "1.2rem"}
      camelize))

(defn li []
  (-> {:list-style "none"
       :margin "1.2rem"
       :display "inline"}
      camelize))

