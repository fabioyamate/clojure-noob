(ns clojure-noob.datomic-lab.tags
  (:require [datomic.api :as d]
            [clojure.data :as data]))

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :post/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc "Post id"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :post/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Post title"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :post/tags
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/many
    :db/doc "Post tags"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :post/category
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Post category"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :category/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Category's name"
    :db.install/_attribute :db.part/db}])

(defn uuid [] (java.util.UUID/randomUUID))

(def datomic-uri "datomic:mem://blog")

(d/create-database datomic-uri)
(def conn (d/connect datomic-uri))
(d/transact conn schema)

;; create post
(def post-id (uuid))

(d/transact conn [{:db/id #db/id[:db.part/user -1]
                   :category/name "datomic"}
                  {:db/id #db/id[:db.part/user -2]
                   :post/id post-id
                   :post/title "Creating tags with datomic"
                   :post/category #db/id[:db.part/user -1]
                   :post/tags ["this" "are" "tags"]}])

(def post-id (-> '{:find [?p-id .]
                   :where [[?p :post/id ?p-id]]}
                 (d/q (d/db conn))))

(def post (d/entity (d/db conn) [:post/id post-id]))

(:post/tags post)

(d/transact conn [[:db/add [:post/id post-id] :post/tags "updated"]])


(defn update-tags [post new-tags]
  (let [[retract add _] (data/diff (set (:post/tags post)) (set new-tags))]
    (concat (map #(vector :db/retract (:db/id post) :post/tags %) retract)
            (map #(vector :db/add (:db/id post) :post/tags %) add))))

(d/transact conn (update-tags post #{"updated" "tags"}))

(into {} (d/entity (d/db conn) [:post/id post-id]))
