(ns clojure-noob.core-async-flow
  (:require [clojure.core.async :as a]
            [clojure.core.async.flow :as flow]))

(defn source
  "Source proc for random stats"
  ;; describe
  ([] {:params {}
       :ins {:in "Input channel"}
       :outs {:out "Output channel for stats"}})

  ;; init
  ([args]
   args)

  ;; transition
  ([state transition]
   (case transition
     ::flow/resume
     state

     (::flow/pause ::flow/stop)
     state))

  ;; transform
  ([state in msg]
   [state (when (= in :stat) {:out [msg]})]))

(comment

  (defn create-flow
    []
    (flow/create-flow
     {:procs {:single {:args {} :proc (flow/process #'source)}}
      :conns []}))


  )
