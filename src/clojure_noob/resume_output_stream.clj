(ns clojure-noob.resume-output-stream
  (:require [clojure.java.io :as io])
  (:import [java.io RandomAccessFile]))

(comment
  (def f (RandomAccessFile. "/tmp/test-random" "rw"))
  (.getFilePointer f)

  (.seek f 7389232)
  (.getFilePointer f)
  (.read f)
  (.write f (.getBytes "ZX"))

  (.close f)

  (with-open []
    (prn (.getFilePointer f))
    (.seek f 50000)
    (prn (.getFilePointer f))
    (.write f (.getBytes "DDDDE")))


  (count (.getBytes "BBB" "UTF-8"))
  )
