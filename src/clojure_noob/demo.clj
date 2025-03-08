(ns clojure-noob.demo
  (:require [clojure.test :refer :all]))

(defn f [x]
  (inc (inc 2)))

(f
 2)

(defn g [y]
  (* y 2))

(f (g 2))

f o g

((comp g f) 2)
;; => 8

(defn f [x]
  (+ (* 2 x ) -1))

(f 3)
;; => 7

(f 5)
;; => 11


(deftest f-test
  (is (= 7 (f 3))))

[[1 2]
 [2 3]]

[[1]
 [2]]



a x b b x c ->  a x c
