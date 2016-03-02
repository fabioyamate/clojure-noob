(ns clojure-noob.core
  (:require [clojure-noob.new :refer [bar t]]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn bar [a] a)

(defmacro pick-one [x y]
  `(if (= 0 (rand-int 2))
     ~x
     ~y))

(pick-one
 (println "hello")
 (println "world"))



(defn dummy-distribution [n]
  (map (partial + n)
       (range (int (* (/ (float (* 2 n))
                         n)
                      10)))))

#dbg (+ 2 2)

(dummy-distribution 2)

(t "hh")

(defn floop [xs]
  (loop [x xs]
    (prn x)
    (recur (rest xs))))

