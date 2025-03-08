(ns clojure-noob.walking
  (:require [clojure.walk :as w]))

(w/walk
 (fn [[k v]]
   [k (* 10 v)])
 identity {:a 1 :b 2 :c 3})
;; => {:a 10, :b 20, :c 30}

(w/walk
 (fn [x]
   (println "inner" x)
   x)
 (fn [x]
   (println "outer" x)
   x)
 (range 5))
;; => (0 1 2 3 4)

(w/walk
 (fn [x]
   (println "traversing" x)
   x)
 (fn [x]
   (println "full result" x)
   x)
 [[1 2]
  [3 4]])

(w/walk
 (fn [x]
   (println "traversing" x)
   x)
 (fn [x]
   (println "full result" x)
   x)
 {:a 1
  :b 2})

(defn hello [name] (str "Hello" name))



(let [counter (atom -1)
      line-counter (atom 0)
      print-touch (fn [x]
                    (print (swap! line-counter inc) ":" (pr-str x) "→ "))
      change (fn [x]
               (let [new-x (swap! counter inc)]
                 (prn new-x)
                 [new-x x]))]
  (w/postwalk (fn [x]
                (print-touch x)
                (change x))
              {:a 1 :b 2}))
