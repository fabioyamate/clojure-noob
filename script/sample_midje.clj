(ns circle.foo-test
  (:require [midje.sweet :refer :all]
            [circle.foo :as foo]))
(fact "foo works"
  (foo x) => 42)
