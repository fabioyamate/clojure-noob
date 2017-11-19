(ns clojure-noob.reducers
  (:require [clojure.core.reducers :as r]))

(r/fold + (r/filter even? (r/map inc [1 1 1 2])))

(->> (range 1000)
     (r/map inc)
     (r/filter even?)
     r/foldcat)

(->> (range 10000000)
     (r/map inc)
     (r/filter even?)
     (r/fold +))

(+ 1 2)
