(ns clojure-noob.datomic-lab.components
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://local/inventory-tutorial")
(d/create-database db-uri)
(def conn (d/connect db-uri))
(set! *print-length* 10)

(def sizes [:small :medium :large :xlarge])
(def colors [:red :green :blue :yellow])
(def types [:shirt :pants :dress :hat])

@(def schema-1
   [{:db/ident :inv/sku
     :db/valueType :db.type/string
     :db/unique :db.unique/identity
     :db/cardinality :db.cardinality/one}
    {:db/ident :inv/color
     :db/valueType :db.type/keyword
     :db/cardinality :db.cardinality/one}
    {:db/ident :inv/size
     :db/valueType :db.type/keyword
     :db/cardinality :db.cardinality/one}
    {:db/ident :inv/type
     :db/valueType :db.type/keyword
     :db/cardinality :db.cardinality/one}])

(d/transact conn schema-1)

(def sample-data
  (->> (for [color colors size sizes type types]
         {:inv/color color
          :inv/size size
          :inv/type type})
       (map-indexed
        (fn [idx map]
          (assoc map :inv/sku (str "SKU-" idx))))))

(d/transact conn sample-data)

(def db (d/db conn))

[:inv/sku "SKU-42"]

;; pull
(d/pull
 db
 [:inv/color :inv/size :inv/type]
 [:inv/sku "SKU-42"])

;; repeat refs
(d/q
 '[:find ?e ?sku
   :where
   [?e :inv/sku "SKU-42"]
   [?e :inv/color ?color]
   [?e2 :inv/color ?color]
   [?e2 :inv/sku ?sku]]
 db)

(def order-schema
  [{:db/ident :order/items
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   {:db/ident :item/id
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :item/count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(d/transact conn order-schema)

(def add-order
  {:order/items
   [{:item/id [:inv/sku "SKU-25"]
     :item/count 10}
    {:item/id [:inv/sku "SKU-26"]
     :item/count 20}]})

(d/transact conn [add-order])

(def db (d/db conn))

;; query to retrieve other SKUs order with a given SKU
(d/q
 '[:find ?sku
   :in $ ?inv
   :where
   [?item :item/id ?inv]
   [?order :order/items ?item]
   [?order :order/items ?other-item]
   [?other-item :item/id ?other-inv]
   [?other-inv :inv/sku ?sku]]
 db [:inv/sku "SKU-25"])

;; replacing the query with a rule
(def rules
  '[[(ordered-together ?inv ?other-inv)
     [?item :item/id ?inv]
     [?order :order/items ?item]
     [?order :order/items ?other-item]
     [?other-item :item/id ?other-inv]]])

(d/q
 '[:find ?sku
   :in $ % ?inv
   :where
   (ordered-together ?inv ?other-inv)
   [?other-inv :inv/sku ?sku]]
 db rules [:inv/sku "SKU-25"])

;; time
(def inventory-counts
  [{:db/ident :inv/count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(d/transact conn inventory-counts)

(def inventory-update
  [[:db/add [:inv/sku "SKU-21"] :inv/count 7]
   [:db/add [:inv/sku "SKU-22"] :inv/count 7]
   [:db/add [:inv/sku "SKU-42"] :inv/count 100]])

(d/transact conn inventory-update)

;; never had any 22s
(d/transact
 conn
 [[:db/retract [:inv/sku "SKU-22"] :inv/count 7]
  [:db/add "datomic.tx" :db/doc "remove incorrect assertion"]])

;; correct count for 42s
(d/transact
 conn
 [[:db/add [:inv/sku "SKU-42"] :inv/count 1000]
  [:db/add "datomic.tx" :db/doc "correct data entry error"]])
;; => #<promise$settable_future$reify__7608@36b9fe16:
;;      {:db-before datomic.db.Db@a1f325db,
;;       :db-after datomic.db.Db@577a8aab,
;;       :tx-data
;;       [#datom[13194139534386 50 #inst "2023-06-23T21:47:15.503-00:00" 13194139534386 true] #datom[17592186045460 79 1000 13194139534386 true] #datom[17592186045460 79 100 13194139534386 false] #datom[13194139534386 62 "correct data entry error" 13194139534386 true]],
;;       :tempids {"datomic.tx" 13194139534386}}>


(def db (d/db conn))
(d/q
 '[:find ?sku ?count
   :where
   [?inv :inv/sku ?sku]
   [?inv :inv/count ?count]]
 db)
                                        ;=> [["SKU-42" 1000] ["SKU-21" 7]]


(d/q
 '[:find (max 3 ?tx)
   :where
   [?tx :db/txInstant]]
 db)
                                        ;=> [[[13194139533330 13194139533329 13194139533328]]]

;; history query
(require '[clojure.pprint :as pp])
(def db-hist (d/history db))
(->> (d/q
      '[:find ?tx ?sku ?val ?op
        :keys tx sku val op
        :where
        [?inv :inv/count ?val ?tx ?op]
        [?inv :inv/sku ?sku]]
      db-hist)
     (sort-by first)
     (pp/print-table))

=>
|            :tx |   :sku | :val |   :op |
|----------------+--------+------+-------|
| 13194139534384 | SKU-22 |    7 |  true |
| 13194139534384 | SKU-42 |  100 |  true |
| 13194139534384 | SKU-21 |    7 |  true |
| 13194139534385 | SKU-22 |    7 | false |
| 13194139534386 | SKU-42 |  100 | false |
| 13194139534386 | SKU-42 | 1000 |  true |


(d/attribute db :inv/sku)
=> #AttrInfo{:id 72
             :ident :inv/sku
             :value-type :db.type/string
             :cardinality :db.cardinality/one
             :indexed false
             :has-avet true
             :unique :db.unique/identity
             :is-component false
             :no-history false
             :fulltext false}

@(def ent (d/entity db [:inv/sku "SKU-42"]))
=> #:db{:id 17592186045460}

(d/touch ent)
=> {:db/id 17592186045460,
    :inv/sku "SKU-42",
    :inv/color :blue,
    :inv/size :large,
    :inv/type :dress,
    :inv/count 1000}


(comment
  (d/delete-database client db-uri))
