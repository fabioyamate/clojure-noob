(ns clojure-noob.fish
  (:require [clojure.spec.alpha :as s]))

;;;; Play with http://gigasquidsoftware.com/blog/2016/05/29/one-fish-spec-fish/

(def fish-numbers
  {0 "Zero"
   1 "One"
   2 "Two"})

(s/def ::fish-number (set (keys fish-numbers)))

(s/valid? ::fish-number 1)
(s/valid? ::fish-number 5)

(s/explain ::fish-number 1)
(s/explain ::fish-number 5)

(s/def ::color #{"Red" "Blue" "Dun"})

(defn one-bigger? [{:keys [n1 n2]}]
  (= n2 (inc n1)))

(defn fish-number-rhymes-with-color? [{n :n2 c :c2}]
  (or
   (= [n c] [2 "Blue"])
   (= [n c] [1 "Dun"])))

(s/def ::first-line (s/and (s/cat :n1 ::fish-number
                                  :n2 ::fish-number
                                  :c1 ::color
                                  :c2 ::color)
                           one-bigger?
                           #(not= (:c1 %) (:c2 %))
                           fish-number-rhymes-with-color?))

(comment
  (s/explain ::first-line [1 2 "Red" "Black"])
  (s/explain ::first-line [1 2 "Red" "Dun"])

  (s/valid? ::first-line [1 2 "Red" "Blue"])

  (s/conform ::first-line [1 2 "Red" "Blue"])
  (s/conform ::first-line [1 2 "Red" "Black"])

  (s/exercise ::first-line 5)

  (s/exercise ::first-line))

(defn fish-line [n1 n2 c1 c2]
  (clojure.string/join
   (map #(str % " fish.")
        [(get fish-numbers n1)
         (get fish-numbers n2)
         c1
         c2])))

(s/fdef fish-line
  :args ::first-line
  :ret string?)

(comment
  (s/exercise-fn #'fish-line)

  (fish-line  2  1 "Red" "Blue"))
