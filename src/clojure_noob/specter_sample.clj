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

(sm/transform [(s/srange 4 11) (s/filterer even?)]
              reverse
              [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15])
;; srange -> [4 5 6 7 8 9 10]
;; filterer(event) -> [4 6 8 10]
;; rever -> [10 8 6 4]
;; => [0 1 2 3 10 5 8 7 6 9 4 11 12 13 14 15]

(sm/select [s/ALL s/ALL #(= 0 (mod % 3))]
           [[1 2 3 4] [] [5 3 2 18] [2 4 6] [12]])
;; s/ALL -> each element
;; s/all -> each subelement
;; only divisible by 3
;; [3 3 18 6 12]

(for [xs [[1 2 3 4] [] [5 3 2 18] [2 4 6] [12]]
      x xs
      :when (zero? (mod x 3))]
  x)

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
