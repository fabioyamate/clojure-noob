(ns clojure-noob.cats-lab
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.monad.either :as either]
            [cats.labs.promise]
            [promesa.core :as p]))

(m/alet [x (maybe/just 1)
         y (maybe/just 2)]
  (+ x y))



(defn sleep-promise
  "A simple function that emulates an
  asynchronous operation."
  [wait]
  (p/promise (fn [resolve reject]
               (future
                 (Thread/sleep wait)
                 (resolve wait)))))


(time
 @(m/mlet [x (sleep-promise 42)
           y (sleep-promise 41)]
    (+ x y)))

(m/alet [x (maybe/just 1)
         y (maybe/just (inc x))]
  (+ x y))
