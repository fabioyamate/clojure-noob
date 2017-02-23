(ns clojure-noob.specter-book
  (:require [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

(defn select-kw
  [path data]
  (vector (get-in data path)))

(= (select-kw [:a] {:a 1})
   (s/select [:a] {:a 1}))

(= (select-kw [:a :b] {:a {:b 1}})
   (s/select [:a :b] {:a {:b 1}}))

(= (select-kw [:a] {:not-a 1})
   (s/select [:a] {:not-a 1}))

(= (select-kw [:a :b] {:a 1})
   (s/select [:a :b] {:a 1}))

(= (select-kw [:a] :random)
   (s/select [:a] :random))

(s/select [s/ALL :a even?]
          [{:a 1} {:a 2} {:a 3} {:a 4}])

(defn select-pred
  [selector candidate]
  (if (empty? selector)
    (vector candidate)
    (let [pred (first selector)]
      (if (pred candidate)
        (select-pred (rest selector) candidate)
        nil))))

(defprotocol Navigator
  (select* [this reminder structure]))
