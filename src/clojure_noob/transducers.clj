(ns clojure-noob.transducers)

(defn inc-all [xs]
  (map inc xs))

(def xform1
  (comp (partition-all 5)
        (map inc-all)))

(transduce xform1 conj nil (range 27))
