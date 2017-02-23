(ns clojure-noob.pedestal.interceptors-lab
  (:require [io.pedestal.interceptor.chain :as ic]
            [io.pedestal.interceptor.error :as ie]
            [clojure.core.async :as async :refer [go]]))

(def attach-guid
  {:name ::attach-guid
   :enter (fn [context]
            (assoc context ::guid (java.util.UUID/randomUUID)))
   :leave (fn [{:keys [::guid] :as context}]
            (assoc context ::guid-str (str guid)))})

(def break
  {:name ::break
   :enter (fn [context]
            (ic/terminate-when context (fn [c] (odd? (or (::number c) 0)))))
   :leave (fn [context]
            (ic/terminate-when context (fn [c] (odd? (or (::number c) 0)))))})

(def cause-err
  {:name ::cause-err
   :enter (fn [context]
            (assoc context ::result (/ 1 0)))})

(def attach-number
  {:name ::attach-number
   :enter (fn [context]
            (assoc context ::number 1))
   :leave (fn [context]
            (update context ::number inc))})

(def async-run
  {:name ::async-run
   :enter (fn [context]
            (go
              (assoc context ::collection
                     (doall (map (fn [x]
                                   (Thread/sleep (* x 100))
                                   x)
                                 (range 10))))))})

(def service-error-handler
  ;; vector binding is a tuple with [context exception]
  (ie/error-dispatch [ctx ex]

    ;; the following expression are clojure.core.match pattern-match
    ;; against ex-data wrapped by pedestal.
    ;; :execution-id, :stage, :intercetor (name), :exception-type
    [{:exception-type :java.lang.ArithmeticException :interceptor ::another-bad-one}]
    (assoc ctx :response {:status 400 :body "Another bad one"})


    [{:exception-type :java.lang.ArithmeticException}]
    (assoc ctx :cause "Stupid you divided 1/0")

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))

(ic/execute {} [service-error-handler cause-err])

(keys (ic/execute (ic/execute {}) [attach-number async-run attach-guid]))
