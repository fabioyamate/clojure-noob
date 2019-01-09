(ns clojure-noob.ns-sample)

(defn f1 [x]
  x)

(defn f2 [x]
  x)

(defn f3 [x]
  (f1 (f2 x)))
