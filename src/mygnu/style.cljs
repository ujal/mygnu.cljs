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

(defn pipe [m]
  (-> m
      prefix
      camelize))

(def fs 1.5)
(def lh 1.6)
(def lhs (* fs 1.6))

(defn content []
  (pipe
    {:transform "translateY(-50%)"
     :position "relative"
     ;:top "45%"
     :top (rm (* lhs 8))
     :color "hsla(0, 0%, 30%, 1)"}))

(defn headings []
  (pipe
    {:font-size (rm (* fs 1.4))
     :font-family "Montserrat"
     :font-weight "bold"
     :margin-bottom (rm (* lhs 3))}))

(defn ul []
  (pipe
    {:list-style "none"
     :margin "1.2rem"}))

(defn li []
  (pipe
    {:list-style "none"
     :margin "1.2rem"
     :display "inline"}))

(defn mcoords []
  (pipe
    {:list-style "none"
     :display "inline"}))

(defn board []
  (pipe
    {:font-size (rm (* fs 1.4))
     ;:font-family "VT323"
     :font-weight "normal"
     :margin "2rem"}))

