(ns clojure-noob.piped-streams
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen])
  (:import java.io.PipedInputStream
           java.io.PipedOutputStream))

(s/def ::name string?)
(s/def ::age int?)
(s/def ::address-line1 string?)
(s/def ::address-line2 string?)
(s/def ::address-state string?)
(s/def ::address-city string?)
(s/def ::address-country string?)
(s/def ::address-number string?)

(s/def ::customer
  (s/keys :req [::name
                ::age
                ::address-line1
                ::address-line2
                ::address-state
                ::address-city
                ::address-country
                ::address-number]))

(defn as-xml
  [d]
  (xml/element :customer {}
               (xml/element :name {} (::name d))
               (xml/element :age {} (::age d))
               (xml/element :address {}
                            (xml/element :line1 {} (::address-line1 d))
                            (xml/element :line2 {} (::address-line2 d))
                            (xml/element :state {} (::address-state d))
                            (xml/element :city {} (::addres-city d))
                            (xml/element :country {} (::address-country d))
                            (xml/element :number {} (::address-number d)))))

(comment
  (with-open [in (piped-input-stream (fn [out]
                                       (with-open [writer (io/writer out)]
                                         (xml/emit
                                          (xml/element :root {}
                                                       (map as-xml (take 1000000 (gen/sample-seq (s/gen ::customer)))))
                                          writer))))]
    (io/copy in (io/file "/tmp/sample.xml")))


  (time
   (spit "/tmp/sample2.xml"
         (xml/emit-str
          (xml/element :root {}
                       (map as-xml (take 1000000 (gen/sample-seq (s/gen ::customer))))))
         :encoding "UTF-8")))

(defn piped-input-stream
  [func]
  (let [input  (PipedInputStream.)
        output (PipedOutputStream.)]
    (.connect input output)
    (future
      (try
        (func output)
        (finally (.close output))))
    input))

(comment
  (with-open [in (piped-input-stream
                  (fn [out]
                    (with-open [writer (io/writer out)]
                      (xml/emit (xml/element :foo {} nil) writer))))]
    (slurp in))

  (let [in (piped-input-stream
            (fn [out]
              (spit out "Hello World")))]
    (slurp in)))
