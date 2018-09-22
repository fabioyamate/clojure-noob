(ns clojure-noob.specs.blockchain
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument))

(s/def ::account int?)
(s/def ::from ::account)
(s/def ::to ::account)
(s/def ::amount decimal?)

(s/def ::transaction (s/keys :req [::from ::to ::amount]))

(s/def ::digest string?) ;; (?)
(s/def ::parent-hash ::digest)
(s/def ::miner ::account)

(s/def ::block-header (s/keys :req [::miner ::parent-hash]))

(s/def ::block (s/coll-of ::transaction :kind vector?))

(s/def ::genesis (s/and empty? vector?))

(s/def ::node (s/keys :req [::block-header ::block]))

(s/conform ::block
           [(gen/generate (s/gen ::transaction))])

(s/def ::blockchain (s/or :genesis ::genesis
                          :node ::node))

(s/def ::transaction-pool (s/coll-of ::transaction :kind vector?))

(s/fdef mine-on
        :args (s/cat :pending-transactions ::transaction-pool
                     :account ::account
                     :blockchain ::blockchain))

(defn mine-on
  [pending-transactions account parent])



(defn make-genesis
  []
  [])
