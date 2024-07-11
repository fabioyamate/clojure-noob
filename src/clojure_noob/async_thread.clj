(ns clojure-noob.async-thread
  (:require [clojure.core.async :as a :refer [<! go go-loop]]))

(defn thread-name
  []
  (.getName (Thread/currentThread)))

(print-thread)
;; => "nREPL-session-9f1cc377-f18a-4d8e-911c-e3d1c9ac5d36"

;; futures uses the Agent (CachedThreadPool)
@(future
   (thread-name))
;; => "clojure-agent-send-off-pool-31"


;; core.async runs in a 8 fixed thread pool
;; lets run a lot of go code, and see which threads they run
(sort (frequencies
       (map (fn [_]
              (a/<!! (go
                       (thread-name))))
            (range 100))))
;; => (["async-dispatch-1" 10]
;;     ["async-dispatch-2" 21]
;;     ["async-dispatch-3" 9]
;;     ["async-dispatch-4" 12]
;;     ["async-dispatch-5" 11]
;;     ["async-dispatch-6" 11]
;;     ["async-dispatch-7" 9]
;;     ["async-dispatch-8" 17])


;; whenever we run something in a core.async/thread
;; it runs the code in a async-thread cached thread pool
(a/<!! (a/thread (thread-name)))
;; => "async-thread-macro-1"

;; even if we run in the go block.
(a/<!! (go [(thread-name)
            (<! (a/thread (thread-name)))]))
;; => ["async-dispatch-19" "async-thread-macro-23"]

;; thread run in the async-thread-macro-*
;; the <! go run in the async-dispatch-*

;; when we run in a go block we are running inside the
;; core async thread pool (fixed-size)
(a/<!! (go (thread-name)))
;; => "async-dispatch-1"

;; future in clojure have their own thread-pool also (unbounded, not cached)
(let [x (future (Thread/sleep 1000)
                (thread-name))
      y(future (Thread/sleep 1000)
               (thread-name))]
  [@x @y])
;; => ["clojure-agent-send-off-pool-240" "clojure-agent-send-off-pool-241"]



;; Playing with sequences

(defn pinc [n]
  (println "inc" n (thread-name))
  (Thread/sleep 50)
  (inc n))

(defn add [x y]
  (println "add" x y (thread-name))
  (+ x y))

;; this is a lazy sequence. The range fn for some reason does not mimic a
;; lazy seq where the value is produced when consumed.
;; (map pinc (range 10)) will already consume 32 items
;; map always consume data in chunks of 32, even if you do (take 2 (map)) or (first (map))
;; this is why in the documentation they state to not do "effectufl" code in map.

(defn positive-numbers
  ([] (positive-numbers 1))
  ([n] (lazy-seq (cons n (positive-numbers (pinc n))))))

(take 2 (positive-numbers))
(take 10 (map inc (positive-numbers)))

;; running code with buffer
(def ic (a/chan 100 (map inc)))

;; this will sum all items in the queue
;; this will wait...
(go (println "sum" (a/<! (a/reduce add 0 ic))))

;; pushing values
(a/onto-chan!! ic (take 50 (positive-numbers)))

;; if you check the onto-chan! and to-chan! both have
;; the variants with !!. The difference between than is
;; that the code runs in a a/thread.

;; channel with no buffer

(def c (a/chan))

;; pushing to the chan will block, until a consumer takes it

(go
  (let [t (thread-name)]
    (println "put " t)
    (a/>! c t) ;; wait
    (println "put-consumed " [t (thread-name)])))

;; consumer
(go
  (let [t (thread-name)]
    (println "consuming in " t)
    (println "<!" (a/<! c)) ;; take
    (println "consumed " [t (thread-name)])))


;; Loops

;; same thing, it is running in a go-loop
(let [c (a/to-chan!! (range 10))]
  (go-loop []
    (when-some [x (<! c)]
      (println x (thread-name))
      (recur))))


;; experiment

;; block all 8 threads, and try to take the value from a async thread

;; blocking fn
(defn f [n]
  (go (println "running " n " " (thread-name))
      (Thread/sleep 10000)
      (println "released " n " " (thread-name))))

(do (f 1)
    (f 2)
    (f 3)
    (f 4)
    (f 5)
    (f 6)
    (f 7)
    (f 8) ;; all thread consumed

    ;; these are all blocked
    (f 9)
    (f 10)

    ;; things that run in thread are dispatched
    (let [k (a/thread
              (let [x (thread-name)]
                (println "kkkkkkkkkkk" x)
                (Thread/sleep 5)
                [x (thread-name)]))]
      (println "waiting..." (a/<!! k)))) ;; waits


;; both go and take can run in different async-dispatch thread
;; this will always the depend on which thread the pool dispatched to
(a/take!
 (go (thread-name))
 (fn [n]
   (println "got" n "in" (thread-name))))

;; collections

(def ch1 (a/chan 10))
(def ch2 (a/chan 10 (comp (map pinc)
                          (partition-all 10))))

(go
  (a/onto-chan! ch1 (take 50 (positive-numbers))) ;; will consume 50 items from the collection
  (a/pipe ch1 ch2))

(go
  (println "waiting..." (thread-name))
  (loop []
    (when-some [x (a/<! ch2)]
      (println x (thread-name))
      (recur)))
  (println "done!!!!!!!!!" (thread-name)))

(a/<!! (a/<!! (go (a/to-chan!! (range 10))))) ;; => 0


;; exceptions
;;
;; any exception that happens in the go, will return a "closed" channel
;; the user will not now it.
;; also, it kills the thread in the pool, and a new thread is launched.
;; you can observe this by checking the async-dispatch-%d number.

(go
  (throw (ex-info (thread-name) {})))

;; the blocking take and put operator
;; which thread they run?

(do
  (f 1)
  (f 2)
  (f 3)
  (f 4)
  (f 5)
  (f 6)
  (f 7)
  (f 8)
  (a/<!! (a/go "Hello"))) ;; waits

(defn tf [n]
  (a/thread
    (println "running " n " " (thread-name))
    (Thread/sleep 10000)
    (println "released " n " " (thread-name))))

(do
  (tf 1)
  (tf 2)
  (tf 3)
  (tf 4)
  (tf 5)
  (tf 6)
  (tf 7)
  (tf 8)
  (a/<!! (a/go "Hello"))) ;; runs immediately
