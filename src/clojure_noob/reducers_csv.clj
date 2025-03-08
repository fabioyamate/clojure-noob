(ns clojure-noob.reducers-csv
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.core.reducers :as r]
            [clojure.core.async :as a]))

(time
 (with-open [reader (io/reader (io/resource "airports.csv"))]
   (->> (csv/read-csv reader)
        (map (fn [row]
               {:id (nth row 0)
                :type (nth row 2)
                :name (nth row 3)
                :country (nth row 8)}))
        (remove #(= "closed" (:type %)))
        count)))
;; => 68536
;; => 766ms


(time
 (with-open [reader (io/reader (io/resource "airports.csv"))]
   (->> (csv/read-csv reader)
        (r/map (fn [row]
                 (Thread/sleep 100)
                 {:id (nth row 0)
                  :type (nth row 2)
                  :name (nth row 3)
                  :country (nth row 8)}))
        (r/remove #(= "closed" (:type %)))
        (r/foldcat)
        count)))
;; => 68536
;; => 900ms

(time
 (with-open [reader (io/reader (io/resource "airports.csv"))]
   (let [producer (a/to-chan! (csv/read-csv reader))
         output (a/chan)]
     (a/pipeline 8 output
                 (comp (map (fn [row]
                              {:id (nth row 0)
                               :type (nth row 2)
                               :name (nth row 3)
                               :country (nth row 8)}))
                       (remove #(= "closed" (:type %))))
                 producer)
     (second (a/<!! (a/into [] output))))))
;; => 68536
;; => 1134ms
