(ns clojure-noob.specter-sample
  (:require [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

(sm/transform [s/MAP-VALS s/MAP-VALS]
              inc
              {:a {:aa 1} :b {:ba -1 :bb 2}})
;; => {:a {:aa 2} :b {:ba 0 :bb 3}}

(sm/transform [s/ALL :a even?]
              inc
              [{:a 1} {:a 2} {:a 4} {:a 3}])
;; => [{:a 1} {:a 3} {:a 5} {:a 3}]

(sm/transform [(s/filterer odd?) s/LAST]
              inc
              [2 1 3 6 9 4 8])
;; => [2 1 3 6 10 4 8]

(sm/setval (s/srange 2 4) [:a :b :c :d :e] [0 1 2 3 4 5 6 7 8 9])
;; => [0 1 :a :b :c :d :e 4 5 6 7 8 9]


(defrecord Account [funds])
(defrecord User [account])
(defrecord Family [accounts-list])

(sm/defprotocolpath AccountPath [])

(sm/extend-protocolpath AccountPath
                        User :account
                        Family [:accounts-list s/ALL])

(sm/select [s/ALL AccountPath :funds]
           [(->User (->Account 50))
            (->User (->Account 51))
            (->Family [(->Account 1)
                       (->Account 2)])])
