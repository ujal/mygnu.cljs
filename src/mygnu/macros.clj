(ns mygnu.macros)

(defmacro timep
  "Evaluates expr and prints the time it took. Returns the value of expr."
  [expr]
  `(let [start# (.now js/performance)
         ret# ~expr]
     (prn (cljs.core/str "Elapsed time: " (- (.now js/performance) start#) " msecs"))
     ret#))
