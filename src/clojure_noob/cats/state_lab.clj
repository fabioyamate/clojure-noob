(ns clojure-noob.cats.state-lab
  (:require [cats.core :as m]
            [cats.monad.state :as state]))

(def f
  (m/mlet [x (state/get)
           :let [y (+ 1 x)]]
    (m/return y)))

(def g
  (m/mlet [x (state/get)
           _ (state/put (+ 1 x))]
    (m/return 9)))

(state/run f 1)
;; => #<Pair [2 1]>


(state/exec f 1)
;; => 1

(state/run g 1) ;; returns the new state and the value of the function
;; => #<Pair [9 2]>

(state/exec g 1) ;; returns the new state
;; => 2
