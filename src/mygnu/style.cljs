(ns mygnu.style
  (:require [clojure.set :refer [rename-keys]]))

(def transform-prop
  (js/Modernizr.prefixed "transform"))

(def app-styles
  {:transform "translateY(-50%)"
   :position "relative"
   :top "50%"})

(defn app []
  (rename-keys app-styles {:transform transform-prop}))


