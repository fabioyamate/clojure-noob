(ns clojure-noob.ns-playground
  (:require [clojure-noob.ns-sample :as a]
            [topology.dependencies :as td]
            [clojure.repl :as repl]
            [topology.edgelist :as te]
            [topology.printer :as tp]
            [vijual]))

(defn parent-children-map
  "Converts a vector of [child parent] edges into a map where every entry has
  the form [parent set-of-children].

  Examples:

  (parent-children-map [[0, 2], [3, 0], [1, 4], [2, 4]])
  ;=> {4 #{1 2}, 0 #{3}, 2 #{0}}

  (parent-children-map [[10 1] [1 4] [6 1] [8 6] [9 5] [2 4]
                        [3 0] [7 0] [5 2] [0 2] [11 1]])
  ;=> {2 #{0 5}, 0 #{3 7}, 5 #{9}, 6 #{8}, 4 #{1 2}, 1 #{6 10 11}}"
  [edges]
  (reduce (fn [m [child parent]]
            (update-in m [parent]
                       (fn [s] (set (conj s child)))))
          {}
          edges))

(defn get-root
  "Gets the root entry of the parent-children-map pcm."
  [pcm]
  (loop [xs pcm]
    (if (seq xs)
      (let [[p _ :as root] (first xs)]
        (if (some (fn [[_ cs]] (contains? cs p)) pcm)
          (recur (rest xs))
          root))
      (throw (RuntimeException. "There is no root!")))))

(defn make-tree
  "Returns a map representing the given sequence of edges (each edge
  represented as [child parent]) as a tree.

  Examples:

  (make-tree [[0, 2], [3, 0], [1, 4], [2, 4]])
  ;=> {4 {2 {0 {3 nil}},
          1 nil}}

  (make-tree [[10 1] [1 4] [6 1] [8 6] [9 5] [2 4]
              [3 0] [7 0] [5 2] [0 2] [11 1]])
  ;=> {4 {2 {5 {9 nil},
             0 {7 nil,
                3 nil}},
          1 {11 nil,
             10 nil,
             6 {8 nil}}}}"
  [edges]
  (let [pcm (parent-children-map edges)]
    (letfn [(build-child-trees [cs]
              (apply merge (map #(build-tree [% (pcm %)]) cs)))
            (build-tree [[root cs]]
              (if root
                {root (build-child-trees cs)}
                {}))]
      (build-tree (get-root pcm)))))

(comment
  (require 'clojure-noob.ns-sample :reload)

  (td/ns->fn-dep-map `clojure-noob.ns-sample)
  ;; #:clojure-noob.ns-sample{f4 (clojure.core/defn clojure-noob.ns-sample2/f2 clojure-noob.ns-sample/f3)
  ;;                          f1 (clojure.core/defn clojure-noob.ns-sample2/f1)
  ;;                          f3 (clojure.core/defn
  ;;                               clojure-noob.ns-sample/f1
  ;;                               clojure-noob.ns-sample/f2)
  ;;                          f2 (clojure.core/defn clojure-noob.ns-sample2/f1)}
  ;; #:clojure-noob.ns-sample{f1 (clojure.core/defn)
  ;;                          f3 (clojure.core/defnX
  ;;                               clojure-noob.ns-sample/f1
  ;;                               clojure-noob.ns-sample/f2)
  ;;                          f2 (clojure.core/defn)}

  (def edges (te/dirs->fn-edges "./src"))

  (defn project-fn? [sym]
    (clojure.string/starts-with?
     (namespace sym)
     "clojure-noob.ns-sample"))

  (defn edge-project? [[e w]]
    (and (project-fn? (first e))
         (project-fn? (second e))))

  (tp/print-weighted-edges
   (filter edge-project? edges))

  (->>
   (filter edge-project? edges)
   (map first)
   (vijual/draw-directed-graph))


  (->> (filter edge-project? edges)
       (map (fn [[e w]]
              (reverse e)))
       make-tree
       clojure.pprint/pprint)

  (tp/print-weighted-edges
   (te/dirs->fn-edges "./src"))

  (ns-interns `clojure-noob.ns-sample)

  (def nspc `clojure-noob.ns-sample)
  (def vars (map meta (vals (ns-interns `clojure-noob.ns-sample))))

  (map :name vars)

  (->> vars
       (map (partial str nspc \/))
       (map symbol)
       #_(map repl/source-fn))

  (repl/source-fn 'a/f1)

  (clojure.pprint/pprint (ns-map `clojure-noob.ns-sample))

  (ns-aliases `clojure-noob.ns-sample)
  )
