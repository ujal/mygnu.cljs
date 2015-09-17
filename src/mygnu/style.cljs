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
    {:position "relative"
     :font-size (rm (* fs 2))
     :font-family "VT323"
     :line-height 1
     ;:top "45%"
     ;:top (rm (* lhs 5))
     :height "100%"
     :color "hsla(0, 0%, 30%, 1)"}))

(defn headings []
  (pipe {:font-size (rm (* fs 2))
         :font-family "VT323"
         :line-height 1
         :top (rm (* lhs 1))
         :position "relative"
         :margin-bottom (rm (* lhs 3))}))

(defn logo []
  (pipe
    {:fontSize "8rem"
     :transform "rotate(-45deg)"
     :display "inline-block"}))

(defn page []
  (pipe
    {:fontSize "2.4rem"
     :fontWidth "700"
     :margin-top "4.8rem"
     :line-height (/ lh 1)}))

