(ns clojure-noob.pedestal.interceptors-concept
  (:require [io.pedestal.interceptor.chain :as ic]
            [io.pedestal.interceptor.error :as ie]
            [io.pedestal.interceptor :as interceptor]
            [clojure.core.async :as async :refer [go]]))

;; handler is a simple function
(defn hello-wolrd [request]
  {:status 200
   :body "Hello World"})

;; this is internally converted into:
(interceptor/interceptor {:enter (fn [context]
                                   (assoc context :response (hello-world (:request context))))})

;; context - input processing, input processing result, machinery, information shared across intercptors
