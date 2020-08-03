(ns clojure-noob.transducers
  (:require [clojure.core.async :as async :refer [>! <! >!! <!! go go-loop]]))

;; https://clojure.org/reference/transducers
;; https://github.com/matthiasn/talk-transcripts/blob/master/Hickey_Rich/Transducers.md

(defn inc-all [xs]
  (map inc xs))

;; xform = transformation
(def xform1
  (comp (partition-all 5)
        (map inc-all)))

(comment
  ;; "it is a reduce" with transformation
  (transduce xform1 conj nil (range 27))

  )

(defn my-inc [x]
  (println "my-inc: " x)
  (inc x))

(defn my-even? [x]
  (println "my-even: " x)
  (even? x))

(defn my-sum
  [& x]
  (println "my-sum: " x)
  (apply + x))

(def xform-sum
  (comp (map my-inc)
        (filter my-even?)
        (map (partial my-sum 5))))

(transduce xform-sum
           vector
           (range 5))

;; traditional way

(->> (range 5)
     (map my-inc)
     (filter my-even?)
     (map (partial my-sum 5))
     (reduce vector []))

;; eduction

(def iter (eduction xform-sum (range 5)))
(reduce vector [] iter)

;; into

(into [] xform-sum (range 5))

;; sequence

(sequence xform-sum (range 5))

;; async go channel

(let [c1 (async/chan 10 xform-sum)]
  (async/go
    (>! c1 1)
    (>! c1 2))

  (<!! c1)
  #_(<!! c1))
