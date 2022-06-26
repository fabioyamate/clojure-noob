(ns clojure-noob.pedestal.interceptors-lab
  (:require [io.pedestal.interceptor.chain :as ic]
            [io.pedestal.interceptor.error :as ie]
            [io.pedestal.interceptor :as interceptor]
            [clojure.core.async :as async :refer [go]]))

(defn trace [moment context value]
  (println (str (apply str (repeat (::debug-depth context) " ")) moment ": " value)))

(def enter-trace (partial trace "enter"))
(def leave-trace (partial trace "leave"))
(def error-trace (partial trace "error"))

(def attach-guid
  (interceptor/interceptor
   {:name ::attach-guid
    :enter (fn [context]
             (assoc context ::guid (java.util.UUID/randomUUID)))
    :leave (fn [{:keys [::guid] :as context}]
             (assoc context ::guid-str (str guid)))}))

(def break
  (interceptor/interceptor
   {:name ::break
    :enter (fn [context]
             (ic/terminate-when context (fn [c] (odd? (or (::number c) 0)))))
    :leave (fn [context]
             (ic/terminate-when context (fn [c] (odd? (or (::number c) 0)))))}))

(def cause-err
  (interceptor/interceptor
   {:name ::cause-err
    :enter (fn [context]
             (assoc context ::result (/ 1 0)))}))

(defn attach-number
  [initial-value]
  (interceptor/interceptor
   {:name ::attach-number
    :enter (fn [context]
             (enter-trace context "attach-number")
             (-> context
                 (assoc ::number initial-value)
                 (update ::debug-depth inc)))
    :leave (fn [context]
             (leave-trace context "attach-number")
             (-> context
                 (assoc ::original-number (::number context))
                 (update ::number inc)))}))

(def compute-division
  (interceptor/interceptor
   {:name ::compute-division
    :leave (fn [{::keys [original-number number] :as context}]
             (enter-trace context "compute-division")
             (assoc context ::division (/ number original-number)))
    :error (fn [context exception]
             (error-trace context "compute-division")
             (clojure.pprint/pprint context)
             (assoc context :exception-type (type exception)))}))

(def division-error
  (interceptor/interceptor
   {:name ::division-error
    :leave (fn [context]
             (leave-trace context "division-error")
             context)
    :error (fn [context exception]
             (let [cause (ex-cause exception)]
               (error-trace context "division-error")
               (when (= java.lang.ArithmeticException (type cause))
                 ;;(println (keys cause))
                 (clojure.pprint/pprint context)
                 (assoc context ::division :infinity))))}))

(def print-result
  (interceptor/interceptor
   {:name ::print-result
    :leave (fn [context]
             (leave-trace context "print-result")
             (println (str "The division is: " (::division context))))}))

(def never-printed
  (interceptor/interceptor
   {:name ::never-printed
    :enter (fn [context]
             (enter-trace context "never-printed")
             #_(println (str (::ic/execution-id context)  "enter: never-printed"))
             context)
    :leave (fn [context]
             (leave-trace context "never-printed")
             (println "NEVER PRINTED"))}))

(def async-run
  (interceptor/interceptor
   {:name ::async-run
    :enter (fn [context]
             (go
               (assoc context ::collection
                      (doall (map (fn [x]
                                    (Thread/sleep (* x 100))
                                    x)
                                  (range 10))))))}))

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

(comment
  (ic/execute {} [service-error-handler cause-err])

  (keys (ic/execute (ic/execute {}) [attach-number async-run attach-guid]))


  (ic/execute {} [attach-guid])

  (ic/execute {::debug-depth 0}
              [never-printed
               print-result
               division-error
               compute-division
               (attach-number 0)])

  )
