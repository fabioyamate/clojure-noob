(ns clojure-noob.logic
  (:require [clojure.core.logic :as l]))

(comment
  ;; run* [logic-variable] & logic expressions
  (l/run* [q]
    (l/== q true))

  (l/run* [q]
    (l/membero q [1 2 3])
    (l/membero q [2 3 4]))

  ;; convention names between logic and default
  ;; ends with 'o', 'u', 'e' or 'a'

  ;; fresh is similar to let but for logic variables
  ;; (fresh [a b c] & logic-expressions)

  ;; == unify

  (l/run* [q]
    (l/fresh [a]
      (l/membero a [1 2 3])
      (l/membero q [2 3 4])
      (l/== q a)))

  (l/run* [q]
    (l/conde
     [(l/== q 2) (l/== q 3)]
     [(l/== q 1)]))

  (l/run* [q]
    (l/conso 1 [2 3] q))

  (l/run* [q]
    (l/resto [1 2 3 4] q))

  ;; examples

  (l/defne moveo [before action after]
    ([[:middle :onbox :middle :hasnot]
      :grasp
      [:middle :onbox :middle :has]])
    ([[pos :onfloor pos has]
      :climb
      [pos :onbox pos has]])
    ([[pos1 :onfloor pos1 has]
      :push
      [pos2 :onfloor pos2 has]])
    ([[pos1 :onfloor box has]
      :walk
      [pos2 :onfloor box has]]))

  (l/defne cangeto [state out]
    ([[_ _ _ :has] true])
    ([_ _] (l/fresh [action next]
             (moveo state action next)
             (cangeto next out))))

  (l/run 1 [q]
    (cangeto [:atdoor :onfloor :atwindow :hasnot] q))
  )
