(ns mygnu.style
  (:require [clojure.set :refer [rename-keys]]
            [camel-snake-kebab.core :as csk]))

(defn rm [n]
  (str n "rem"))

(defn px [n]
  (str n "px"))

(def transform
  (js/Modernizr.prefixed "transform"))

(def user-select
  (js/Modernizr.prefixed "userSelect"))

(defn prefix [styles]
  (rename-keys styles
               {:transform transform}
               {:user-select user-select}))

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
     :font-weight "700"
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
         :margin-bottom (rm (* lhs 5))
         :margin-left (rm lhs)
         :margin-right (rm lhs)}))

(defn logo []
  (pipe
    {:fontSize "7rem"
     :fontFamily "Montserrat"
     :fontWeight "400"
     :transform "rotate(90deg)"
     :display "inline-block"
     ;:margin (rm (* lhs 1))
     :margin 0
     }))

(defn nav []
  (pipe
    {:list-style "decimal outside none"
     :color "#1EAEDB"
     :margin (rm (* lhs 2))
     :fontFamily "Montserrat"
     :fontSize "2rem"
     :fontWeight "700"}))

(defn nav-item []
  (pipe
    {:display "inline-block"
     :margin-right (str (* 1.5 1.6) "rem")
     :margin-bottom 0
     :vertical-align "middle"
     :cursor "pointer"
     :line-height lh
     :user-select "none"}))

(defn nav-item-a []
  (pipe
    {:display "inline-block"
     :line-height "3.2rem"
     :user-select "none"}))

(defn page []
  (pipe
    {:fontSize (rm (* fs 1.5))
     :margin (rm (* lhs 2))
     :line-height lh}))

(defn footer []
  (pipe
    {:fontSize "2rem"
     :line-height lh
     :position "absolute"
     :bottom 0
     :marginBottom (rm lhs)
     :left 0 :right 0}))

