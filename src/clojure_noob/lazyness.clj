(ns clojure-noob.lazyness)

;; chunked sequences

;; > With the release of Clojure 1.1, the granularity of Clojure's
;; > laziness was changed from a one-at-a-time model to a chunk-at-a-time model.
;; > (windowed view approach)
(take 1 (map #(do (print \.) %) (range 20)))
(take 1 (map #(do (print %) %) (range 40)))

;; > this change will break the purpose of lazyness, but strikes when dealing with large
;; > sequences that would need to fit in memory

;; No function should be doing side-effects

(defn m-range [n]
  (println n)
  (range 10))

(take 5 (concat (query-datomic) (query-datomi2)))

(def a (lazy-cat (m-range 1) (m-range 2) (m-range 3)))

(lazy-cat (m-range 1) (m-range 2) (m-range 3))

(def b (concat (m-range 1) (m-range 2) (m-range 3)))

(take 11 a)
(take 11 b)

(defn produce-seq
  ([n]
   (produce-seq 0 n))
  ([i n]
   (lazy-seq
    (cons (do (println ">" i) i)
          (when (< i n)
            (produce-seq (inc i) n))))))

(take 5 (produce-seq 10))

(take 15
      (lazy-cat (produce-seq 10 15) (produce-seq 30 100)))


;; map cat will always call the n elements of collection
(take 2
      (mapcat m-range (range 50)))
;; it uses map beneath, map always run over the first 32 elements, no matter
;; how many elements you consume

(println
 (take 15
       (mapcat m-range (produce-seq 0 25))))
;; this shows that mapcat always consume the first 4 elements
