(ns clojure-noob.datomic-lab.relation
  (:require [datomic.api :as d]
            [clojure-noob.stateful :as db]))

(def datomic-uri "datomic:mem://local")

(d/create-database datomic-uri)

(def conn (d/connect datomic-uri))

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :movie/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :actor/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :movie/cast
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db.install/_attribute :db.part/db}])

(d/transact conn schema)

(def movie1
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :movie/name "The Matrix"
                     :movie/cast #{{:actor/name "Keanu Reeves"}
                                   {:actor/name "Carrie-Anne Moss"}}}]))

movie1

(def db (d/db conn))

(defn entities [q & inputs]
  (->> (apply d/q q inputs)
       (map first)
       (map (partial d/entity db))
       (map (partial into {}))))

(d/q '{:find [?m]
       :where [[?m :movie/name]]} db)

(-> '{:find [?m]
      :where [[?m :movie/name]]}
    (entities db))

(d/transact conn [[:db/retractEntity [:actor/name "Keanu Reeves"]]])

(-> '{:find []
      :where [[?m :movie/name]]}
    (entities (d/db conn)))

(d/pull (d/db conn) '[* {:movie/cast [*]}] [:movie/name "The Matrix"])
(d/pull (d/db conn) '[*] [:actor/name "Carrie-Anne Moss"])
(d/pull (d/db conn) '[*] [:actor/name "Keanu Reeves"])

(d/entid (d/db conn) [:actor/name "Carrie-Anne Moss"])

(d/q '{:find [?m]
       :where [[?m :movie/name]]} db)

(d/transact conn [[:db/retract [:movie/name "The Matrix"] :movie/cast [:actor/name "Carrie-Anne Moss"]]])
