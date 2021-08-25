(ns clojure-noob.datomic-lab.identity-and-uniqueness
  (:require [datomic.api :as d]))

;; https://docs.datomic.com/on-prem/schema/identity.html#unique-identities

(def datomic-uri "datomic:mem://local")

(d/create-database datomic-uri)

(def conn (d/connect datomic-uri))

;; person/id       - identity uuid
;; person/name     - string
;; person/email    - identity string
;; token/value     - unique string
;; account/id      - identity uuid
;; account/balance - bigdec

(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :person/id
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/index true
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :person/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :person/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :token/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/value
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :account/id
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :account/balance
    :db/valueType :db.type/bigdec
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(d/transact conn schema)

;; sample

(def alice-id (d/squuid))
(def alice-account-id (d/squuid))
(def bob-id (d/squuid))
(def bob-account-id (d/squuid))

(def alice-tx
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id alice-id
                     :person/name "Alice"
                     :person/email "alice@mail.local"
                     :account/id alice-account-id}]))

(defn entity [db ref]
  (into {} (d/entity db ref)))

(entity (d/db conn) [:person/id alice-id])
;; => {:person/id #uuid "60929de6-8346-49f0-8ebd-cb9458e64126",
;;     :person/name "Alice",
;;     :person/email "alice@mail.local",
;;     :account/id #uuid "60929e7c-1fa5-4a56-94ae-b0eeaa0b993d"}

(entity (d/db conn) [:account/id alice-account-id])
;; => {:person/id #uuid "60929de6-8346-49f0-8ebd-cb9458e64126",
;;     :person/name "Alice",
;;     :person/email "alice@mail.local",
;;     :account/id #uuid "60929e7c-1fa5-4a56-94ae-b0eeaa0b993d"}

(def bob-tx
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id bob-id
                     :person/name "bob"
                     :person/email "bob@mail.local"}]))

(def bob-account-tx
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :account/id bob-account-id
                     :account/balance 10M}]))

(entity (d/db conn) [:person/id bob-id])
;; => #:person{:id #uuid "60929fa9-811c-43ae-b6a7-1a982ca8c41f",
;;             :name "bob",
;;             :email "bob@mail.local"}

(entity (d/db conn) [:account/id bob-account-id])
;; => #:account{:id #uuid "60929fa8-be71-4e28-894f-32b91ed39e76",
;;              :balance 10M}

(def associate-bob-account-tx
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id bob-id
                     :account/id bob-account-id}]))
;; Caused by datomic.impl.Exceptions$IllegalStateExceptionInfo
;; :db.error/unique-conflict Unique conflict: :person/id, value:
;; 60929fa9-811c-43ae-b6a7-1a982ca8c41f already held by:
;; 17592186045424 asserted for: 17592186045427
;; #:db{:error :db.error/unique-conflict}

(comment
  (def charlie-id
    (d/squuid))

  (def david-id
    (d/squuid))

  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id charlie-id
                     :person/name "Charlie"
                     :token/value "token-c"}])

  ;; fails with unique conflict
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id david-id
                     :person/name "David"
                     :token/value "token-c"}])

  ;; changes charlie token
  (d/transact conn [{:person/id charlie-id
                     :token/value "token-a"}])

  ;; david is able to get token-c
  (d/transact conn [{:db/id #db/id[:db.part/user]
                     :person/id david-id
                     :person/name "David"
                     :token/value "token-c"}])

  ;; charlie can't get token-c
  (d/transact conn [{:person/id charlie-id
                     :token/value "token-c"}])

  )
