(ns clojure-noob.fsm
  (:require [reduce-fsm :as fsm]))

(defn inc-val [val & _] (inc val))

(inc-val 1 2 3)

(fsm/defsm count-ab
  [[:start
    \a -> :found-a]
   [:found-a
    \a -> :found-a
    \b -> {:action inc-val} :start
    _ -> :start]])

(map (partial count-ab 0) ["aaabbaa" "abababab"])

(fsm/show-fsm count-ab)
