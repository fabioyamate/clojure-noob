(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :auth}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.specs.alpha "0.1.24"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/core.cache "0.6.5"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/data.zip "0.1.2"]

                 [org.clojure/data.xml "0.0.8"]
                 [uk.me.rkd.xml-validation "0.1.0-SNAPSHOT"]

                 [org.clojure/tools.logging "0.4.0"]
                 [manifold "0.1.6"]
                 [com.datomic/datomic-free "0.9.5385"]
                 ;; [Comcid.datomic/datomic-pro "0.9.5407"]
                 [com.rpl/specter "1.0.5"]
                 [irresponsible/anarchy "0.2.0"]

                 [pandect "0.6.1"]
                 [crypto-random "1.2.0"]

                 [cheshire "5.8.0"]

                 [http-kit "2.2.0"]

                 [finagle-clojure/core "0.7.0"]
                 [finagle-clojure/http "0.7.0"]
                 [io.zipkin.finagle/zipkin-finagle-http_2.11 "0.3.6"]

                 [criterium "0.4.4"]
                 ]
  :profiles {:dev {:plugins [[lein-midje "3.2.1"]]
                   :dependencies [[midje "1.9.1"]
                                  [org.clojure/test.check "0.9.0"]]}})
