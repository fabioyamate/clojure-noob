(ns clojure-noob.portal-demo
  (:require [portal.api :as p]
            [vlaaad.reveal :as r]
            [dev.nu.morse :as morse]))

(morse/launch-in-proc)
(remove-tap #'morse/inspect)

(tap> (+ 1 1))

(def x 2)
(def k 4)
(tap> (+ x k))

(def portal (p/open))
(add-tap #'p/submit)

(r/tap-log)

(morse/inspect 1)

(morse/inspect (+ 1 1))
(tap> 1)
(tap> {:hello 2
       :bar 2
       :car [{:foo 1}]})

(morse/inspect [{:title "foo" :body "djskl"}
                {:title "foo" :body "djskl"}
                {:title "foo" :body "djskl"}
                {:title "foo" :body "djskl"}
                {:title "foo" :car "djskl"}])

(tap> (System/getProperties))


(morse/inspect (System/getProperties))

(r/inspect {:fx/type r/vega-view
            :spec {:mark :bar
                   :encoding {:x {:field :a
                                  :type :nominal
                                  :axis {:labelAngle 0}}
                              :y {:field :b
                                  :type :quantitative}}}
            :data [{:a "A" :b 28}
                   {:a "B" :b 55}
                   {:a "C" :b 43}
                   {:a "D" :b 91}
                   {:a "E" :b 81}
                   {:a "F" :b 53}
                   {:a "G" :b 19}
                   {:a "H" :b 87}
                   {:a "I" :b 52}]})
