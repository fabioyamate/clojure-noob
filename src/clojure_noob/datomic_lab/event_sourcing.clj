(ns clojure-noob.datomic-lab.event-sourcing
  (:require [datomic.api :as d]))

(def datomic-uri "datomic:mem://local")

(d/create-database datomic-uri)

(def conn (d/connect datomic-uri))

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :account/id
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :account/owner
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :account/amount
    :db/valueType :db.type/bigdec
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   ])

(d/transact conn schema)

(def account-id (d/squuid))

(def event1
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :account/id account-id
                     :account/owner "John Doe"}]))

(def event2
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :account/id account-id
                     :account/balance 20M}]))

(def event3
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :account/id account-id
                     :account/owner "Jane Doe"}]))

(def event4
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :account/id account-id
                     :account/amount 10M}]))

(def db (d/db conn))

(defn entities [q & inputs]
  (->> (apply d/q q inputs)
       (map first)
       (map (partial d/entity db))
       (map (partial into {}))))

(-> '{:find [?a]
      :where [[?a :account/id]]}
    (entities (d/db conn)))

(def output
  (-> '{:find [?tx]
        :where [[?a :account/id _]
                [?a _ _ ?tx]]}
      (d/q (d/history db))
      (->> (map first)
           (map (partial d/entity db))
           (map (partial into {})))))

(#:db{:txInstant #inst "2017-06-24T22:13:52.672-00:00"} #:db{:txInstant #inst "2017-06-24T22:13:56.368-00:00"} #:db{:txInstant #inst "2017-06-24T22:13:57.908-00:00"})

(-> '{:find [?e]
      :in [$ ?t1 ?t2]
      :where [[(tx-ids $ ?t1 ?t2) [?tx ...]]
              [(tx-data $ ?tx) [[?e]]]]}
    (entities (d/log conn) #inst "2017-06-23" #inst "2017-06-25")
    (->> (drop 1)))

(-> '{:find [?e ?a ?v]
      :in [$ ?t1 ?t2]
      :where [[(tx-ids $ ?t1 ?t2) [?tx ...]]
              [(tx-data $ ?tx) [[?e ?a ?v]]]]}
    (d/q (d/log conn) #inst "2017-06-23" #inst "2017-06-25"))

(-> '{:find [?e]
      :in [$ $log]
      :where [[?a :account/id]
              [?a _ _ ?tx]
              [(tx-data $log ?tx) [[?e]]]]}
    (entities (d/history (d/db conn)) (d/log conn))
    )
