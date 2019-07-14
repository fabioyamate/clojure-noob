(ns clojure-noob.graph-reverse-engineer
  (:require [plumbing.graph :as graph]
            [plumbing.core :refer [fnk]]
            [plumbing.map :as p-map]
            [plumbing.fnk.pfnk :as pfnk]
            [schema.core :as s]))

(comment
  (def g (graph/->graph {:a (fnk [x] x)}))


  (def ng-spec
    {:a {:b (fnk [x] x)}
     :c (fnk [b]
          b)})

  (defn sum [f xs]
    (reduce
     (fn [acc x]
       (+ acc (f x)))
     0
     xs))



  (pfnk/input-schema-keys (fnk [x/a b c d] a))

  (def g-spec
    {:n  (fnk [xs]   (count xs))
     :m  (fnk [xs n] (/ (sum identity xs) n))
     :m2 (fnk [xs n] (/ (sum #(* % %) xs) n))
     :v  (fnk [m m2] (- m2 (* m m)))})

  (def ng (graph/->graph ng-spec))
  (def ngc (graph/eager-compile ng-spec))

  (get-in (ngc {:x 2}) [:a])

  (p-map/topological-sort {::a [::b ::c]
                           ::b [::x]
                           ::c [::b]})


  (def h (fn [x]))
  (:arglists (meta #'h))



  ;; (fn? (fnk [x] x)) => true


  (meta g)

  )
