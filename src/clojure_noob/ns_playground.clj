(ns clojure-noob.ns-playground
  (:require [clojure-noob.ns-sample :as a]
            [topology.dependencies :as td]
            [clojure.repl :as repl]
            [topology.edgelist :as te]))

(require 'clojure-noob.ns-sample :reload)

(td/ns->fn-dep-map `clojure-noob.ns-sample)
;; #:clojure-noob.ns-sample{f4 (clojure.core/defn clojure-noob.ns-sample2/f2 clojure-noob.ns-sample/f3)
;;                          f1 (clojure.core/defn clojure-noob.ns-sample2/f1)
;;                          f3 (clojure.core/defn
;;                               clojure-noob.ns-sample/f1
;;                               clojure-noob.ns-sample/f2)
;;                          f2 (clojure.core/defn clojure-noob.ns-sample2/f1)}
;; #:clojure-noob.ns-sample{f1 (clojure.core/defn)
;;                          f3 (clojure.core/defn
;;                               clojure-noob.ns-sample/f1
;;                               clojure-noob.ns-sample/f2)
;;                          f2 (clojure.core/defn)}

(te/dirs->fn-edges "./src")

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
