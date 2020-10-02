(ns clojure-noob.lazyness)

(defn m-range [n]
  (println n)
  (range 10))

(def a (lazy-cat (m-range 1) (m-range 2) (m-range 3)))

(take 11 a)

(defn produce-seq
  ([n]
   (produce-seq 0 n))
  ([i n]
   (lazy-seq
    (cons (do (println ">" i) i)
          (when (< i n)
            (produce-seq (inc i) n))))))

(take 20 (produce-seq 10))

(take 15
      (lazy-cat (produce-seq 10 15) (produce-seq 30 100)))


;; map cat will always call the n elements of collection
(take 2
      (mapcat m-range (range 50)))
;; it uses map beneath, map always run over the first 32 elements, no matter
;; how many elements you consume

(println
 (take 15
       (mapcat m-range (produce-seq 20 25))))
;; this shows that mapcat always consume the first 4 elements
