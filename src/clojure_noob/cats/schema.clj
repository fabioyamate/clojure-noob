(ns clojure-noob.cats.schema
  (:require [cats.monad.maybe :as maybe]
            [cats.monad.either :as either]
            [cats.context :as ctx]
            [cats.protocols :as p]
            [cats.builtin]
            [cats.core :as m]
            [schema.macros :as macros]
            [schema.core :as s]
            [schema.spec.leaf :as leaf]
            [schema.spec.core :as spec]
            [schema.utils :as utils]))

(defn precondition2
  [s p err-f]
  (fn [x]
    (when-let [reason (macros/try-catchall (when-not (p @x) 'not) (catch e# 'throws?))]
      (macros/validation-error s @x (err-f (utils/value-name @x)) reason))))


(defrecord Reference [name schema]
  s/Schema
  (spec [this]
    (leaf/leaf-spec
     (precondition2 this
                    (fn [v] (nil? (s/check schema v)))
                    #(list name schema %))))
  (explain [this] (list name (s/explain schema))))

(s/explain (Reference. 'left (s/enum :a)))

(s/check s/Int 1)
(s/check s/Int "a")

(s/check (Reference. 'Maybe (s/enum :a)) (maybe/nothing))
(s/check (Reference. 'Nothing (s/pred nil?)) (maybe/just 1))

(defn Either [l r]
  (s/conditional either/left? (Reference. 'Left l)
                 either/right? (Reference. 'Right r)
                 :else (s/eq (list 'Either l r))))

(defn Maybe [a]
  (s/conditional maybe/just? (Reference. 'Just a)
                 maybe/nothing? (Reference. 'Nothing s/Any)
                 :else (s/eq (list 'Maybe a))))

(def SomeError (s/enum :error1))

(s/validate (Either SomeError s/Int) (either/right "a"))
(s/validate (Either SomeError s/Int) (either/left "a"))
(s/validate (Either SomeError s/Int) (either/right 1))
(s/validate (Either SomeError s/Int) (either/left :error1))
(s/validate (Either SomeError s/Int) (maybe/nothing))

(s/validate (Maybe s/Int) (maybe/just "a"))
(s/validate (Maybe s/Int) (either/right 1))
