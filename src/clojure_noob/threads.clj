(ns clojure-noob.threads)

(def keep-running false)

(loop []
  (future #(Thread/sleep (rand-int 10000)))
  (Thread/sleep 1000)
  (when keep-running
    (recur)))

(defn slow-fn
  [n]
  (Thread/sleep 5000)
  {:finished n})

(defn slow-inc
  [n]
  (Thread/sleep 5000)
  (inc n))

(defn slow-sum
  [n x]
  ;; (reduce + x (range 100000000))
  (Thread/sleep 4000)
  x
  )

(comment
  (time (slow-sum 10 2))

  (def results (mapv #(future (slow-fn %)) (range 2000)))

  (def results (vec (pmap slow-fn (range 50))))

;;;; agents thread model, they are basically a pool of threads
;;;; agents have by default two options `send` and `send-off`
;;;; `send` = fixed thread pool (2 + number of processors)
;;;; `send-off` = cached thread pool / unbounded threads (grows until OOM)

;;;; `send-via` is the API used by both, the difference is that it can refer
;;;; to a customer executor (thread pool) created specifically

;;;; on visualvm this pools can be so by viewing the threads as:
;;;; clojure-agent-send-pool-%d (%d is up-to 2+num-cores)
;;;; clojure-agent-send-off-pool-%d (%d is a counter, so every new thread spawn this is inc)

;;;; most of this can be found on clojure Agent.java implementation
;;;; also read on Java Concurrency in Practice book
;;;; Joy of Clojure on concurrency / parallel chapters

  (def log-agent (agent 0))
  (def log-agent2 (agent 0))
  (def log-agent3 (agent 0))
  (def log-agent4 (agent 0))
  (def log-agent5 (agent 0))
  (def log-agent6 (agent 0))
  (def log-agent7 (agent 0))

  (send log-agent slow-inc)

;;; some conclusions on this is that agents are sequential
;;; all send "tasks" to the same agent are serial, so if you
;;; having a blocking work it will hold all tasks

;;; having different agents will spread to different threads
;;; as available

;;; the idea for agents it to control the concurrent writes in
;;; sequence, on of uses are for append log files

;;; so be careful for usages because it may block all tasks if they
;;; are slow

;;; `await` is a way to block until all tasks in a given agent
;;; is complete. If the queue size is large it will wait until
;;; everything is consumed

  (time (await (send log-agent6 slow-sum 12)))
  (time
   (->> (vector
         (future (await (send log-agent slow-sum 7)))
         (future (await (send log-agent slow-sum 8)))
         (future (await (send log-agent slow-sum 9)))
         (future (await (send log-agent slow-sum 10)))
         (future (await (send log-agent slow-sum 11)))
         (future (await (send log-agent slow-sum 12)))
         (future (await (send log-agent slow-sum 12)))
         )
        (mapv deref)))

  @log-agent

;;;; futures in clojure uses agent internally and dispatches
;;;; to a cached thread pool, thus is a unbounded thread
;;;; good for IO since you don't block. However, creating too
;;;; many threads might degradate the instance performance
;;;; if it doesn't have enough processors available
  (future (slow-fn))


;;;; Some more conclusions (there are differences) but
;;;; different languages has its own ways to handle tasks submission
;;;; to be processed in "threads" of work

;;;; java = executor patter producer / consumer pattern
;;;; go = go routines
;;;; erlang = actor model mail box
;;;; clojure = uses java underneath / core.async as go routines

  )
