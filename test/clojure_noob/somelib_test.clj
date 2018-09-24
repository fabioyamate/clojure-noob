(ns clojure-noob.somelib-test
  (:require [clojure-noob.somelib :as lib]
            ;; clojure unit test
            [clojure.test :refer :all]
            ;; test-check
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(deftest sum
  (is (= 5 (lib/sum 2 3)))
  (is true)
  (is (= 5 (+ 2 3)) "Simple sum math")
  (is (thrown? ArithmeticException (/ 1 0))))

(testing "arithmetic"
  (testing "with positive integers"
    (is (= 4 (+ 2 2)))
    (is (= 7 (+ 3 4))))
  (testing "with negative integers"
    (is (= -4 (+ -2 -2)))
    (is (= -1 (+ 3 -4)))))

(deftest slow
  (is (do (Thread/sleep 600)
          true) "Really slow"))

(deftest addition
  (is (= 4 (+ 2 2)))
  (is (= 7 (+ 3 4))))

(deftest subtraction
  (is (= 2 (- 4 2)))
  (is (= 3 (- 7 4))))

(deftest arithmetic
  (addition)
  (subtraction))

;;;; Property-based testing

(defspec sort-idempotent-prop 50
  (prop/for-all [v (gen/vector gen/int)]
    (= (sort v) (sort (sort v)))))
