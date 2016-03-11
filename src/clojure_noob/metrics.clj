(ns clojure-noob.metrics
  (:require [riemann.client :as rc])
  (:import [com.codahale.metrics.riemann RiemannReporter Riemann]
           [com.codahale.metrics MetricRegistry]
           [com.codahale.metrics.jvm MemoryUsageGaugeSet GarbageCollectorMetricSet]
           [java.util.concurrent TimeUnit]))

(defn conn [uri]
  (rc/tcp-client {:host uri}))

(def metric-registry (MetricRegistry.))

(.registerAll metric-registry (MemoryUsageGaugeSet.))
(.registerAll metric-registry (GarbageCollectorMetricSet.))

(def riemann-client
  (rc/tcp-client {:host "127.0.0.1"}))

@(rc/send-event riemann-client {:service "foo" :state "ok" :tags ["foo"]})

(def reporter
  (-> (RiemannReporter/forRegistry metric-registry)
      (.localHost "localhost")
      (.tags ["foo"])
      (.build (Riemann. riemann-client))))

(def started-reporter (future (.start reporter 10 TimeUnit/SECONDS)))

(def my-counter (.counter metric-registry "mycounter"))

(dotimes [_ 1000]
  (.inc my-counter))
