(ns clojure-noob.datomic-lab.composite-tuples
  (:require [datomic.api :as d]))

;; this requires a pro version as free version is not available.

(def datomic-uri "datomic:mem://local")

(d/create-database datomic-uri)

(def conn (d/connect datomic-uri))

(def student-schema
  [{:db/ident :student/first
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :student/last
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :student/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}])

(def semester-schema
  [{:db/ident :semester/year
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident :semester/season
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident :semester/year+season
    :db/valueType :db.type/tuple
    :db/tupleAttrs [:semester/year :semester/season]
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}])

(def course-schema
  [{:db/ident :course/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :course/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(def schema
  (concat student-schema
          semester-schema
          course-schema))

(d/transact conn schema)

;; adding some seed

(def seed
  [{:semester/year 2018
    :semester/season :fall}
   {:course/id "BIO-101"}
   {:student/first "John"
    :student/last "Doe"
    :student/email "johndoe@university.edu"}])

(d/transact conn seed)

(into {} ;; just eager loading entity
      (d/entity (d/db conn) [:semester/year+season [2018 :fall]]))
;; => #:semester{:year 2018, :season :fall, :year+season [2018 :fall]}

;; can write missing attribute from composite tuple?

(d/transact conn [{:semester/year 2019}])

(d/transact conn [{:semester/season :summer}])

(d/q '[:find (pull ?e [*])
       :in $
       :where [?e :semester/year 2019]]
     (d/db conn))
;; => [[{:db/id 17592186045422,
;;       :semester/year 2019,
;;       :semester/year+season [2019 nil]}]]


(d/q '[:find (pull ?e [*])       :in $
       :where [?e :semester/year+season [2019 nil]]]
     (d/db conn))
