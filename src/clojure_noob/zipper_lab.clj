(ns clojure-noob.zipper-lab
  (:require [clojure.zip :as z]))

(def data [[1 2 3] [4 [5 6] 7] [8 9]])


(def zp (z/zipper vector? seq (fn [_ c] c) data))

(-> zp z/down)

(def zm (z/zipper map? seq (fn [_ c] c) {:foo {:bar 1}}))

(-> zm z/down)
