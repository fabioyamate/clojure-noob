(ns clojure-noob.datomic-lab.settings-with-components
  (:require [datomic.api :as d]
            [schema.core :as s]
            [schema.coerce :as coerce]))

(def db-uri "datomic:mem://local/app-settings")
(d/create-database db-uri)
(def conn (d/connect db-uri))

(def application-schema
  [;; primary key
   {:db/ident :application/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   ;; application settings
   {:db/ident :application/settings
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   ;; setting key-val
   {:db/ident :setting/key
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident :setting/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(d/transact conn application-schema)

(def add-application
  {:application/id "blog"
   :application/settings [{:setting/key :blog/title
                           :setting/value "My Blog"}
                          {:setting/key :social/instagram
                           :setting/value "@myblog"}
                          {:setting/key :host
                           :setting/value "myblog.com"}]})

(d/transact conn [add-application])

(d/q '[:find ?a
       :in $ ?id
       :where [?a :application/id ?id]]
     (d/db conn) "blog")

(def blog (d/entity (d/db conn) [:application/id "blog"]))

blog
;; => #:db{:id 17592186045418}

(d/touch blog)
;; => {:db/id 17592186045418, :application/id "blog", :application/settings #{{:db/id 17592186045424, :setting/key :host, :setting/value "myblog.com"} {:db/id 17592186045419, :setting/key :blog/title, :setting/value "My Blog"} {:db/id 17592186045420, :setting/key :social/instagram, :setting/value "@myblog"} {:db/id 17592186045422, :setting/key :blog/title, :setting/value "My Blog"} {:db/id 17592186045423, :setting/key :social/instagram, :setting/value "@myblog"}}}

(->> (:application/settings blog)
     (map (juxt :setting/key :setting/value))
     (into {}))
;; => {:host "myblog.com",
;;     :blog/title "My Blog",
;;     :social/instagram "@myblog"}


(def settings-schema
  {:counter s/Int
   :blog/title s/Str})
