(ns clojure-noob.changeset
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(defrecord Changeset
    [valid?
     repo
     repo-opts
     params
     changes
     required
     prepare
     errors
     contraints
     validations
     filters
     action
     types])

#_(def changeset)
;; Maps

(s/keys :req [::x ::y (or ::secret (and ::user ::pwd))] :opt [::z])


;; Sequences
(s/def ::even? (s/and integer? even?))
(s/def ::odd? (s/and integer? odd?))
(s/def ::a integer?)
(s/def ::b integer?)
(s/def ::c integer?)
(def s (s/cat :forty-two #{42}
              :odds (s/+ ::odd?)
              :m (s/keys :req-un [::a ::b ::c])
              :oes (s/* (s/cat :o ::odd? :e ::even?))
              :ex (s/alt :odd ::odd? :even ::even?)))

(s/conform s [42 11 13 15 {:a 1 :b 2 :c 3} 1 2 3 42 43 44 11])

(defn my-inc [x]
  (str (inc x)))

(s/fdef my-inc
        :args (s/cat :x number?)
        :ret number?)

(defn extract
  "Given a map and some keys, return a map with only those keys"
  [m ks]
  (reduce (fn [acc k]
            (if-let [v (get m k)]
              (assoc acc k v)
              acc))
          {}
          ks))

(s/fdef extract
        :args (s/cat :m map?
                     :ks (s/coll-of any?))
        :fn (fn [ctx] (prn ctx)
              (= (into #{} (-> ctx :args :ks))
                 (into #{} (-> ctx :ret keys))))
        :ret map?)

(stest/instrument `extract)

(comment
  (my-inc "a")

  (extract {:a 1 :b 2} [:a]))

(extract {:bar false} [:bar])


(s/fdef symbol2
        :args (s/alt :separate (s/cat :ns string? :n string?)
                     :str string?
                     :sym symbol?)
        :ret symbol?)
