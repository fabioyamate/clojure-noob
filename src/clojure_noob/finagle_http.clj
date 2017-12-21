(ns clojure-noob.finagle-http
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.service :as service]
            [finagle-clojure.filter :as filter]
            [finagle-clojure.builder.client :as builder-client]
            [finagle-clojure.http.builder.codec :as http-codec]
            [finagle-clojure.http.client :as http]
            [finagle-clojure.http.message :as message]
            [finagle-clojure.duration :as duration]
            [cheshire.core :as cheshire])
  (:import [com.twitter.finagle.service TimeoutFilter]
           [com.twitter.util JavaTimer]
           [zipkin.finagle.http HttpZipkinTracer HttpZipkinTracer$Config]
           [com.twitter.finagle.stats JavaLoggerStatsReceiver]
           [java.util.logging Logger]))

(defn timeout-filter
  [duration-ms]
  (TimeoutFilter. (duration/->Duration duration-ms :ms) (JavaTimer.)))

#_(def zipkin-config
    (.. (HttpZipkinTracer$Config/builder)
        (host "192.168.1.18:9411")
        build))

#_(def tracer (HttpZipkinTracer/create zipkin-config
                                       (JavaLoggerStatsReceiver. (Logger/getAnonymousLogger))))

(def tracer (HttpZipkinTracer.))



(def c (-> (builder-client/builder)
           (builder-client/codec (.enableTracing http-codec/http true))
           (builder-client/hosts "httpbin.org:80")
           (.tracer (HttpZipkinTracer.))
           (builder-client/build)))

(def c2 (-> (builder-client/builder)
            (builder-client/codec http-codec/http)
            (builder-client/hosts "httpbin.org:80")
            (builder-client/build)
            (->> (filter/and-then (timeout-filter 5000)))))

(defn status-req
  [status-code]
  (-> (message/request (str "/status/" status-code) :get)
      (message/set-header "Host" "httpbin.org")))

(defn delay-req
  [seconds]
  (-> (message/request (str "/delay/" seconds) :get)
      (message/set-header "Host" "httpbin.org")))

(defn- parse-json-response
  [response]
  (-> response message/content-string (cheshire/parse-string true)))

(comment

  (let [m (-> (message/request "/get" :get)
              (message/set-header "Host" "httpbin.org"))]
    (-> (service/apply c m)
        (f/map [x]
               (-> x
                   message/content-string
                   (cheshire/parse-string true)))
        f/await))

  (f/await (service/apply c (delay-req 10)))

  (f/await (service/apply c2 (delay-req 10)))

  (->> (vector
        (service/apply c (delay-req 10))
        (service/apply c2 (delay-req 10))
        (service/apply c (delay-req 10))
        (service/apply c2 (delay-req 10))
        (service/apply c (delay-req 10)))
       (mapv f/await)
       time)

  (time
   (f/await
    (f/for [r1 (service/apply c (delay-req 10))
            r2 (service/apply c2 (delay-req 10))
            r3 (service/apply c (delay-req 10))
            r4 (service/apply c2 (delay-req 10))
            r5 (service/apply c (delay-req 10))]
      (f/value (mapv message/content-string (vector r1 r2 r3 r4 r5))))))

  (f/await (f/for [r1 (service/apply c m)
                   r2 (service/apply c2 (status-req 400))]
             (f/value
              (->> (vector r1 r2)
                   (mapv parse-json-response)))))


  )
