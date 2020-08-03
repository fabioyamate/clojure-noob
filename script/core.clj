(ns core
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [clojure.zip :as zip]
            [rewrite-clj.zip.base :as zb]))

(def form (p/parse-string "(defn my-function [a]\n  (* a 3))"))

(n/meta-node
 (n/token-node :private)
 (n/token-node 'sym))

(def data-string
  "(defn my-function [a]
  ;; a comment
  (* a 3))")
(def data (z/of-string data-string))

(z/sexpr data)
;; => (defn my-function [a] (* a 3))
(-> data z/down z/right z/node)
;; => <token: my-function>
(-> data z/down z/right z/sexpr)
;; => my-function

(-> data z/down z/right (z/edit (comp symbol str) "2") z/up z/sexpr)
;; => (defn my-function2 [a] (* a 3))

(-> data z/down z/right (z/edit (comp symbol str) "2") z/print-root)


(def data (z/of-file "../project.clj"))

(def prj-map (z/find-value data z/next 'defproject))

(n/string-node prj-map)

(def descr (-> prj-map (z/find-value :description) z/right))

(-> descr (z/replace "My first Project.") z/root-string)


;;; midje clojure test

(def midje-sample (z/of-file "sample_midje.clj"))

(-> midje-sample (z/find-next z/next #{'fact}))

(defn clojure-test-require
  [zloc]
  (-> (z/find-value zloc z/next 'midje.sweet)
      (z/replace 'clojure.test)
      (z/root)))

(-> midje-sample
    #_zb/edn
    (z/find-value z/next 'facts)
    (z/replace 'testing))

(-> midje-sample
    (z/find-next z/right)
    #_(z/replace 'testing))

(let [sign (-> midje-sample
               z/right
               (z/find-value z/next '=>)
               z/right)
      new-node (n/token-node 'is
                             (n/list-node "1"))]
  new-node)

(n/whitespace-node)

(-> midje-sample z/right)


(-> midje-sample (z/find-next-value :require))
