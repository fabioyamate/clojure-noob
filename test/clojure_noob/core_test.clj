(ns clojure-noob.core-test
  (:require [midje.sweet :refer :all]
            [clojure-noob.core :refer :all]))

;.;. The highest reward for a man's toil is not what he gets for it but
;.;. what he becomes by it. -- Ruskin
(fact "a-test"
  (= 1 1) => true)
