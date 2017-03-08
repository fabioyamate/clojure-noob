(ns clojure-noob.reducers-requests
  (:require [clojure.core.reducers :as r]
            [clj-http.client :as client]))

(def size 10000000)

(r/reduce + (range 10))

(time (r/reduce + (range size))) ;; contains overhead of threads
(time (reduce + (range size)))
(time (r/fold + (range size)))

(time (r/fold))

(time (r/foldcat
       (r/map (fn [n]
                (client/get (str "https://httpbin.org/delay/" n)))
              (r/map inc (range 3)))))

(defn iopmap
  [f coll]
  "Like pmap, but appropriate for blocking IO tasks (e.g. network calls). As opposed to pmap, it's not semi-lazy."
  (->> coll
       (mapv #(future (f %)))
       (mapv deref)))

(defn do-delay-req
  [delay]
  (prn (str "request -> " delay))
  (time
   (client/get (str "https://httpbin.org/delay/" delay))))

(time (let [req (pmap do-delay-req
                      (map #(+ % 3) (range 10)))]
        (doseq [v req]
          (prn v))))
