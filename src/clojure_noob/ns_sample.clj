(ns clojure-noob.ns-sample
  (:require [clojure-noob.ns-sample2 :as ns2]
            [schema.core :as s]))

(defn f1 [x]
  (ns2/f1 x))

(defn f2 [x]
  (ns2/f1 x))

(defn f3 [x]
  (f1 (f2 x)))

(defn f4 [x y]
  (ns2/f2 x (f3 y))
  (f3 y))

(s/defn f5 [x y]
  (+ x y))

(s/defn f6 [x y]
  (f5 x y))
