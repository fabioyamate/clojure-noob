(ns clojure-noob.xml-noob
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]
            [uk.me.rkd.xml-validation :as xmlv]))

(def sample
  (-> "sample.xml" io/resource io/reader xml/parse))

(take 5 (map (partial * 10) (range)))

(into []
 (comp (map (partial * 10))
       (take 5))
 (range 5))

(def xform (comp (map (partial * 10))
                 (take 5)))

(sequence xform (range 5))

(+ (* 2 3) (/ 1 2))

(defn foo [a]
  (into [] (comp (map (partial * 10))
                 (take 5)
                 (map inc)) (range)))

(comment
  (def validator? (xmlv/create-validation-fn "resources/BV_001.xsd"))
  (def xml-content (-> "BVXA001.xml" io/resource slurp))

  (validator? xml-content))
