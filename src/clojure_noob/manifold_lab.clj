(ns clojure-noob.manifold-lab
  (:require [manifold.deferred :as d]
            [manifold.stream :as s]))

;;;; deferreds / promise

(comment
  (def x (promise))
  (deliver x 1)
  (realized? x)
  @x
  )


(comment
  (def d (d/deferred))
  (d/on-realized d prn prn)
  (d/success! d 1)
  (d/error! d (Exception. "error"))
  @d
  )

;;; composing

(let [d (d/deferred)]
  (-> (d/let-flow [a (future 1)
                   b (future 2)
                   c (+ a 3)
                   e ]
        (+ a b c))
      (deref 100 "timeout")))

;;;; streams

(comment
  (def s (s/stream))
  (s/put! s 1)
  (prn @(s/take! s))
  )
