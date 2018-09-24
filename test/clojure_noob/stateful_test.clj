(ns clojure-noob.stateful-test
  (:require [clojure.test :refer :all]
            [clojure-noob.stateful :as db]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(deftest store-contains
  (let [db (db/create)
        k "a"
        v "b"]
    (db/store! db k v)
    (is (= v (db/fetch db k)))))

(deftest store-overwrite
  (let [db (db/create)
        k "a"
        v1 "b"
        v2 "c"]
    (db/store! db k v1)
    (db/store! db k v2)
    (is (= v2 (db/fetch db k)))))

(deftest clear-empty
  (let [db (db/create)
        k "a"
        v "b"]
    (db/store! db k v)
    (db/clear! db)
    (is (zero? (db/size db)))))

;;; generative

(def gen-key gen/string)
(def gen-value gen/string)

(defspec prop-store-contains 100
  (prop/for-all [k gen-key
                 v gen-value]
    (let [db (db/create)]
      (db/store! db k v)
      (= v (db/fetch db k)))))

(defspec prop-store-overwrite 100
  (prop/for-all [k gen-key
                 v1 gen-value
                 v2 gen-value]
    (let [db (db/create)]
      (db/store! db k v1)
      (db/store! db k v2)
      (= v2 (db/fetch db k)))))

(defspec prop-clear-empty 100
  (prop/for-all [k gen-key
                 v gen-value]
    (let [db (db/create)]
      (db/store! db k v)
      (db/clear! db)
      (zero? (db/size db)))))

;;; property operations

(def gen-clear (gen/return [:clear!]))
(def gen-size (gen/return [:size]))
(defn gen-store
  [keys]
  (gen/tuple (gen/return :store!)
             (gen/elements keys)
             gen-value))

(defn gen-delete
  [keys]
  (gen/tuple (gen/return :delete!)
             (gen/elements keys)))

(defn gen-fetch
  [keys]
  (gen/tuple (gen/return :fetch)
             (gen/elements keys)))
(defn gen-ops*
  [keys]
  (gen/vector
   (gen/one-of [gen-clear
                (gen-store keys)
                (gen-delete keys)
                (gen-fetch keys)
                gen-size])))

(def gen-ops
  (gen/let [keys (gen/not-empty (gen/vector gen-key))]
    (gen-ops* keys)))

(defn gen-ops-sequences
  [n]
  (gen/let [keys (gen/not-empty
                  (gen/vector gen-key))]
    (apply gen/tuple
           (repeat n (gen-ops* keys)))))

(gen/sample gen-ops 10)

(defn db-run
  [db ops]
  (doseq [[op k v] ops]
    (case op
      :clear! (db/clear! db)
      :store! (db/store! db k v)
      :delete! (db/delete! db k)
      :fetch (db/fetch db k)
      :size (db/size db))))

(defn hm-run
  [db ops]
  (reduce
   (fn [hm [op k v]]
     (case op
       :clear! {}
       :store! (assoc hm k v)
       :delete! (dissoc hm k)
       :fetch hm
       :size hm))
   db ops))

(defn equiv?
  [db hm]
  (and (= (count hm) (db/size db))
       (every? (fn [[k v]]
                 (= v (db/fetch db k)))
               hm)))

(defspec prop-hash-map-equiv 100
  (prop/for-all [ops gen-ops]
    (let [hm (hm-run {} ops)
          db (db/create)]
      (db-run db ops)
      (equiv? db hm))))

(defn run-in-thread
  [latch db ops]
  (let [done (promise)]
    (.start (Thread. (fn []
                       @latch
                       (db-run db ops)
                       (deliver done :done!))))
    done))

(defn thread-run
  [db ops-sequences]
  (let [latch (promise)
        threads (map #(run-in-thread latch db %)
                     ops-sequences)]
    (dorun threads)
    (deliver latch :go!)
    (run! deref threads)))

(defspec prop-race-condition-hash-map-equiv 100
  (prop/for-all [[ops-a ops-b] (gen-ops-sequences 2)]
    (let [ops (concat ops-a ops-b)
          hm (hm-run {} ops)
          db (db/create)]
      (thread-run db [ops-a ops-b])
      (equiv? db hm))))
