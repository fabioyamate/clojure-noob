(ns clojure-noob.cache
  (:require [clojure.core.cache :as cache]))

;;; Basics

;; Caches are immutable structure, the cache updates and returns a new cache always

(def fifo-cache (cache/fifo-cache-factory {:initial :data}))

(cache/lookup fifo-cache :initial)
(:initial fifo-cache)

(cache/has? fifo-cache :initial)
(cache/has? fifo-cache :invalid)

(cache/hit fifo-cache :initial)
(cache/miss fifo-cache :initial 42)

(cache/evict fifo-cache :initial)

;; stateful cache

(defonce cache-store (atom (cache/fifo-cache-factory {} :threshold 3)))

(defn get-data [cache-store key]
  (cache/lookup (swap! cache-store
                       #(if (cache/has? % key)
                          (cache/hit % key)
                          (cache/miss % key (rand-int 1000))))
                key))

(get-data cache-store 1)

;;; TTL

(def ttl-cache (atom (cache/ttl-cache-factory {} :ttl 10000)))

(def my-ttl-cache
  (-> ttl-cache
      (assoc :a 1)
      (assoc :b 2)))

(get-data ttl-cache 1)




(:a my-ttl-cache)

;;; LIRS

(def lirs-cache (cache/lirs-cache-factory {}))


(defn wrap-cache-fn
  [cache-store key f]
  (fn [& args]
    (cache/lookup (swap! cache-store
                         #(if (cache/has? % key)
                            (cache/hit % key)
                            (cache/miss % key (apply f args))))
                  key)))

(defn db-fn [db]
  (println (str "called: " db))
  (Thread/sleep (* db 1000))
  #{{:result (rand-int (* db 1000))}})

(time (db-fn (rand-int 10)))

(def cache-db-fn (wrap-cache-fn cache-store :db-fn-cache db-fn))

(time (cache-db-fn (rand-int 10)))

(defn iopmap [f coll]
  "Like pmap, but appropriate for blocking IO tasks (e.g. network calls). As opposed to pmap, it's not semi-lazy."
  (->> coll
       (mapv #(future (f %)))
       (mapv deref)))

(time (iopmap db-fn (reverse (range 50))))

(time (doall (pmap db-fn (reverse (range 10)))))
