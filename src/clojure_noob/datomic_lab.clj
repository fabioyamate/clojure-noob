(ns clojure-noob.datomic-lab
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.core.async :as async :refer [>! <! go go-loop]]))

(comment
  ;; set uri for local mem datomic
  (def datomic-uri "datomic:mem://local")
  #_(def datomic-uri "datomic:dev://localhost:4334/local")

  ;; create the db
  (d/create-database datomic-uri)

  ;; create a connection to datomic
  (def conn (d/connect datomic-uri))

  ;; gets a db from conn
  (defn db [conn] (d/db conn))

  ;; reads the schema stored (based on mbrainz-sample)
  (def schema
    (with-open [r (java.io.PushbackReader. (clojure.java.io/reader (io/resource "schema.edn")))]
      (read r)))

  ;; transact schema
  (d/transact conn schema)

  ;; loads all attrs (used for debug)
  (def attrs
    (-> '{:find [[?v ...]]
          :where [[?e :db/ident ?v]]}
        (d/q (db conn))))

  ;; create a entity (based on temp id)
  (def r1 (d/transact conn [{:db/id #db/id[:db.part/user]
                             :country/name "Brazil"}]))

  ;; updates the entity from r1
  (def r2 (d/transact conn [{:db/id [:country/name "Brazil"]
                             :country/name "BRASIL"}]))

  ;; understanding what is *-t fns
  (d/basis-t (:db-before @r1)) ; the current t for db
  (d/basis-t (:db-after @r1))
  (d/next-t (:db-before @r1))  ; the current or latest t from this db
  (d/next-t (:db-after @r1))
  (d/since-t (:db-after @r1))  ; the t for since db, if any

  ;; checks the changes done on db given tx-id from db
  (defn changes [db]
    (-> '{:find [?old ?new]
          :in [$ ?tx]
          :where [[?e :country/name ?new ?tx true]
                  [?e :country/name ?old ?tx false]]}
        (d/q (d/history db) (-> db d/basis-t d/t->tx))))

  (changes (:db-after @r2))

  ;; creates a core.async channel from tx-report-queue
  (defn tx-report [conn]
    (let [c (async/chan)
          queue (d/tx-report-queue conn)]
      (go
        (while true
          (>! c (.take queue))))
      c))

  (def queue (tx-report conn))

  ;; start consuming tx-reports
  (go-loop []
    (let [i (<! queue)]
      (prn i)
      #_(prn (changes (:db-after i)))
      (recur)))

  (async/close! queue)

  ;; think tx-data as a "mini-db" with all datoms transacted
  ;; you can query over it or over db
  (:tx-data @r2)

  ;; this builds a changes on given attr name based on tx-data
  (defn changes2 [{:keys [tx-data db-after] :as tx-report}, attr]
    (-> '{:find [?value]
          :in [$ ?attr]
          :where [[?e ?attr ?value]]}
        (d/q tx-data (d/entid db-after attr))))

  (changes2 @r2 :country/name)

  ;; gets the attribute data from eid
  (d/attribute (db conn) 50)
  )
