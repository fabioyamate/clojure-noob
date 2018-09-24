(ns clojure-noob.somelib
  (:require [clojure.test :refer [is with-test]]
            [clojure.set :as set]))

(defn sum
  [x y]
  (+ x y))

(with-test
  (defn my-function [x y]
    (+ x 0))
  (is (= 2 (my-function 2 2)))
  (is (= 3 (my-function 3 4))))

(defn z?
  [x]
  (zero? x))

(defn fibo-iter
  ([n] (fibo-iter 0 1 n))
  ([curr nxt n]
   (cond
     (z? n) curr
     :else (recur nxt (+ curr nxt) (dec n)))))
