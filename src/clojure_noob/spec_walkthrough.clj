(ns clojure-noob.spec-walkthrough
  (:require [clojure.spec.alpha :as s]))

(s/def ::even? (s/and integer? even?))
(s/def ::odd? (s/and integer? odd?))
(s/def ::a integer?)
(s/def ::b integer?)
(s/def ::c integer?)

(def spec (s/cat :forty-two #{42}
                 :odds (s/+ ::odd?)
                 :m (s/keys :req-un [::a ::b ::c])
                 :oes (s/* (s/cat :o ::odd? :e ::even?))
                 :ex (s/alt :odd ::odd? :even ::even?)))

(s/conform spec [42 11 13 15 {:a 1 :b 2 :c 3} 1 2 3 42 43 44 11])
