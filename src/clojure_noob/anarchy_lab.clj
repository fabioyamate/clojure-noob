(ns clojure-noob.anarchy-lab
  (:require [irresponsible.anarchy :refer [match-conds match-actions]]))

;; data and conds are identical to above
(def data {:season :peak :price 500 :start :monday :length 7})
(def conds [#(= :peak   (:season %)) ;; season is peak
            #(= :monday (:start %))  ;; booking starts on monday
            #(>= (:length %) 7)])    ;; booking is for 7 daysor more

(def conds-map
  {:season-is-peak #(= :peak (:season %))
   :starts-on-monday #(= :monday (:start %))
   :booking-more-than-7d #(>= (:length %) 7)})

;; cond-matrix and action-matrix differ
(def cond-matrix
  [[:a true nil false]
   [:b false true false]
   [:c false false true]
   [:d true false false]])
(def action-matrix
  [[#(update % :price * 1.5) #{:a :d}]
   [#(update % :price * 0.9) #{:a :b}]
   [#(update % :price * 0.8) #{:c}]])

(defn trace
  [i]
  (prn i)
  i)

(->> (match-conds cond-matrix conds data) ;; match conditions
     trace
     (take 1)                               ;; first match wins
     (match-actions action-matrix)        ;; match actions
     (reduce #(%2 %) data))                 ;; apply actions
