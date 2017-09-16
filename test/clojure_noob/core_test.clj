(ns clojure-noob.core-test
  (:require [midje.sweet :refer :all]
            [midje.config :as config]))

(prn "====")

(config/at-print-level
 :print-facts
 (facts "here"
   (fact "foo"
     1 => 1)

   (fact "bar"
     1 => 3)))

(defn slow-fn
  [ms]
  (Thread/sleep ms))

(defn log-form
  [form]
  (prn "here")
  (prn form)
  form)

(background
 (around :facts (time ?form)))

(facts "foo"
  (fact "slow test"
    (slow-fn 1000) => irrelevant)

  (fact "slowest test"
    (slow-fn 2000) => irrelevant))
