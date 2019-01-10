(ns clojure-noob.ns-sample2)

(defn f1 [x]
  x)

(defn f2 [x y]
  (+ (f1 x) y))

(defn f3 [x y z]
  (+ (f2 x y) z))
