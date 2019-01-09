(ns clojure-noob.ns-playground
  (:require [clojure-noob.ns-sample :as a]
            [topology.dependencies :as td]))

(td/ns->fn-dep-map `clojure-noob.ns-sample)

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
