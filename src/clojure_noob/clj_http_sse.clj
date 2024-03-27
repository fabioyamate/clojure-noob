(ns clojure-noob.clj-http-sse
  (:require [clj-http.client :as http]
            [clojure.core.async :as a]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.io InputStream]))

(def event-mask (re-pattern (str "(?s).+?\r\n\r\n")))

(defn- parse-event [raw-event]
  (->> (re-seq #"(.*): (.*)\n?" raw-event)
       (map #(drop 1 %))
       (group-by first)
       (reduce (fn [acc [k v]]
                 (assoc acc (keyword k) (string/join (map second v)))) {})))

(defn connect [url & [params]]
  (println "connecting to " url "...")
  (let [events (a/chan (a/sliding-buffer 10))
        event-stream ^InputStream (:body (http/get url (merge params {:as :stream})))]
    (println "got event-stream")
    (a/thread
      (println "in a thread")
      (loop []
        (println "new loop...")
        (let [n-bytes (.available event-stream)
              _ (println "available: " n-bytes)
              byte-array (byte-array (max 1 n-bytes))
              bytes-read (.read event-stream byte-array)]

          (if (neg? bytes-read)

            (do (println "Input stream closed, exiting read-loop")
                (.close event-stream))

            (let [data (slurp byte-array)]
              (println "some data " data)
              (if-let [es (not-empty (re-seq event-mask data))]
                (if (every? true? (map #(a/>!! events %) es))
                  (recur)
                  (do (println "Output stream closed, exiting read-loop")
                      (.close event-stream)))

                (recur)))))))
    events))

(comment

  (def c (connect "http://127.0.0.1:8080/ticker"))

  (a/go-loop []
    (let [x (a/<! c)]
      (println "Got a value in this loop:" x))
    (recur))

  (a/close! c)
  )
