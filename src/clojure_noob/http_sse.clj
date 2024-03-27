(ns clojure-noob.http-sse
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.core.async :as async]))

(def sample ": this is a test stream

data: some text

data: another message
data: with two lines
")

(def line-pattern #"([^\:]+)?: (.+)")

(defn append-line
  [s line]
  (if (nil? s)
    line
    (str s "\n" line)))

(defn parse-line
  [line]
  (when-some [[_ field value] (re-matches line-pattern line)]
    (println "parse-line")
    (if (str/blank? field)
      [:comment value]
      [(keyword field) value])))

(defn parse-event
  [lines]
  (reduce (fn [event [field value]]
            (update event field append-line value))
          nil
          (keep parse-line lines)))

(->> (str/split-lines sample)
     (partition-by str/blank?)
     (keep parse-event))

(comment

  (def c (async/chan 2 (comp (partition-by str/blank?)
                             (keep parse-event))))


  (def a (async/go-loop []
           (let [x (async/<! c)]
             (println "Got a value in this loop:" x))
           (recur)))

  (def b (async/go
           (doseq [l (str/split-lines sample)]
             (println "      > pushing " l)
             (async/>! c l))))

  )

(defn get-stream
  [url & [params]]
  (let [events (async/chan 10 (comp (partition-by str/blank?)
                                    (keep parse-event)))]
    (async/thread
      (println "    connecting to: " url)
      (let [in ^InputStream (:body (http/get url (merge params {:as :stream})))]
        (println "      connected!")
        (with-open [rdr (io/reader in)]
          (doseq [l (line-seq rdr)]
            (println "        new line: " l)
            (async/>!! events l))
          (println "... no more lines...")))
      (println "ended..."))
    events))

(comment

  (def consumer
    (let [c (get-stream "http://127.0.0.1:8080/ticker")]
      (async/go-loop []
        (println "    waiting...")
        (let [x (async/<! c)]
          (cond (nil? x)
                (println "closed!")

                :else
                (do
                  (println "Got a value in this loop:" x)
                  (recur)))))))

  (async/go
    (when (nil? (async/<! consumer))
      (println "closed")))


  (http/get url (merge params {:as :stream}))

  (:body @(http/get "https://httpbin.org/get"))
  y
  (+ 1 1)



  (let [in (:body @(http/get "http://127.0.0.1:8080/ticker" {:as :stream}))]
    (with-open [rdr (io/reader in)]
      (println
       (first (line-seq in)))))

  )
