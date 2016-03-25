(ns clojure-noob.cats-lab
  (:require [cats.applicative.validation :as v]
            [cats.core :as m]
            [cats.context :as ctx]
            [cats.builtin]
            [cats.monad.maybe :as maybe]
            [cats.monad.either :as either]
            [cats.labs.state :as state]
            [cats.monad.identity :as m-identity]
            [cats.monad.exception :as exc]))

;;;; Monoids

(m/mappend [1 2 3] [4 5 6])


(m/mappend (maybe/just [1,2,3])
           (maybe/just [4 5 6]))

(m/mappend (maybe/just [1,2,3])
           (maybe/nothing))

(m/mappend (maybe/just [1,2,3])
           (maybe/nothing)
           (maybe/just [5,6,7]))

;;;; Functors

(m/fmap (partial + 1) (maybe/just 1))

(m/fmap (comp (partial + 1)
              (partial * 2))
        (maybe/just 1))

(m/fmap (partial + 1) (m/fmap (partial * 2) (maybe/just 1)))

((comp (partial + 1)
       (partial * 2)) 1)


;;;; Applicative

(defn make-something [^String lang]
  (condp = lang
    "es" (maybe/just (fn [name] (str "Hola " name)))
    "en" (maybe/just (fn [name] (str "Hello " name)))
    (maybe/nothing)))

(m/fapply (make-something "es") (maybe/just "john"))

(m/foldl - 0 [1 2 3 4 5]) ; (((((0 -1) - 2) - 3) - 4) - 5)  -- needs to be eager (may OOM on infinity inputs)
(m/foldr - 0 [1 2 3 4 5]) ; 0 - (1 - (2 - (3 - (4 - 5))))   -- can be lazily (ok for infinity inputs)
(reduce - 0 [1 2 3 4 5])

(m/foldl #(m/return (+ %1 %2)) 1 (maybe/just 1))

(defn m-div
  [x y]
  (if (zero? y)
    (maybe/nothing)
    (maybe/just (/ x y))))

(m-div 1 2)
(m-div 1 0)

(m/foldm m-div 1 [1 2 0])

;;;; Traversable

;;; Fail on first error
;;; - single transaction action with independent logic validation
(defn just-if-even [n]
  (if (even? n)
    (maybe/just n)
    (maybe/nothing)))

(just-if-even 1)
(just-if-even 2)

(ctx/with-context maybe/context
  (m/traverse just-if-even []))

;;; short-circuit fail if one doesn't match
(ctx/with-context maybe/context
  (m/traverse just-if-even [2 1]))

;;; Validation accumulation (useful for model-like validation)
(defn valid-if-even [n]
  (if (even? n)
    (v/ok n)
    (v/fail {n :not-valid})))

(ctx/with-context v/context
  (m/traverse valid-if-even []))

(ctx/with-context v/context
  (m/traverse valid-if-even [1 2 3 4]))

;;;; Monads (the big topic)

;;; Functor
;; fmap :: Functor f => (a -> b) -> f a -> f b

;;; Applicative
;; fapply :: Applicative f => f (a -> b) -> f a -> f b
;; pure :: Applicative f => a -> f a

;;; Monad
;; (>>=) :: Monad m => m a -> (a -> m b) -> m b
;; return == pure
(m/bind (maybe/just 1)
        (fn [n] (maybe/just 2)))

;;; composing the explicit way

;;; (Just 2) >>= (\a -> (Just 3) >>= (\b -> return (a * b)))
(m/bind (maybe/just 2)
        (fn [a] (m/bind (maybe/just 3)
                        (fn [b]
                          (m/return (* a b))))))

;;; the do-notation
;;;
;;; do a <- Just 2
;;;    b <- Just 3
;;;    return a * b
(m/mlet [a (maybe/just 2)
         b (maybe/just (+ a 1))]
        (m/return (* a b)))

(m/mlet [a (maybe/just 2)
         b (maybe/just 3)]
        (m/pure (* a b))) ; since pure = return we can use it

;;; since the sequence chain is a "nesting" closure, the binded
;;; values gets available on each level

;;; this is important because it is different than just chaining calls
;;; it is common to see examples of code like:
;;; (RxJava) observable.flatMap(a -> makeNewObservable())
;;;                    .flatMap(a -> makeNewObservable())


;;;; Monad Zero

;;; (m/pure 1) => reads the context (probably uses clojure dynamic var)
(m/pure maybe/context 1) ; explicit
(m/mzero maybe/context)  ; is the "short-circuit" value to stops the monad sequence

;;; Monads are sequence computation with a context ("side-effect")
;;; so the mzero represents the one that "breaks" its sequence
;;; - Maybe = Nothing
;;; - Either a b = Left a

(m/mzero either/context)

;;; so we can replace the maybe/nothing with m/mzero

(defn just-if-odd [n]
  (if (odd? n)
    (m/pure n)
    (m/mzero)))

;;; explicit context
(ctx/with-context maybe/context
  (just-if-odd 1))

;;; something sets the context of execution

(m/bind (maybe/just 1)
        just-if-odd)

(m/bind (maybe/just 2)
        just-if-odd)

;;; these are guards
(defn just-if-odd-guard [n]
  (m/guard (odd? n)))

(m/mlet [a (maybe/just 1)
         :when (= a 2)]
  (m/return (* a 2)))

;;;; MonadPlus

;;; mplus similar to OR logic for monads

(m/mplus (maybe/nothing) (maybe/just 2))


;;;; Monad Transformers

;;; MaybeState s a = StateT s Maybe a
(def maybe-state
  (state/state-t maybe/context))

;;; newtype StateT = StateT { runStateT :: a }
;;; the state/run-state is the function to extract the inner monad fron stateT
(ctx/with-context maybe-state
  (let [state {}]
    (state/run-state (m/return 42) state)))

(def maybe-identity
  (maybe/maybe-t m-identity/context))

;;;; Either Monad

(either/right 42)
(either/left "some error")

(m/mlet [a (either/right 42)
         b (either/left "some error")
         c (either/right 4)]
  (m/return (* a b c)))

;;;; Exception Monad

(exc/try-on 1)
(exc/try-on (+ 1 nil))
(exc/try-or-else (+ 1 nil) 0)
(exc/try-or-recover (/ 1 0) (fn [e]
                              (cond
                                (instance? NullPointerException e) 0
                                :else e)))

(exc/try-on (+ 1 nil))

;;; (<*>) :: Applicative f => f (a -> b) -> f a -> f b
;;; fapply = <*>
(m/<*> (v/ok 42)
       (v/ok 3)
       (v/fail {:foo "bar"})
       (v/ok 99))

;;; (<$>) :: Functor f => f (a -> b) -> f a -> f b
;;; fmap = <$>
(m/<$> inc (maybe/just 1))
