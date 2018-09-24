(ns clojure-noob.stateful)

(defn create
  []
  (atom {}))

(defn clear!
  [db]
  (reset! db {}))

(defn store!
  [db k v]
  (swap! db assoc k v))

(defn delete!
  [db k]
  (swap! db dissoc k))

(defn fetch
  [db k]
  (get @db k))

(defn size [db]
  (count @db))
