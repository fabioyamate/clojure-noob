(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :auth}}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/core.async "0.2.391"]
                 [org.clojure/core.logic "0.8.10"]
                 [org.clojure/core.typed "0.3.28"]
                 [org.clojure/test.check "0.9.0"]
                 [prismatic/schema "1.1.3"]
                 [org.clojure/algo.monads "0.1.6"]
                 [org.clojure/data.zip "0.1.2"]
                 [org.clojure/data.xml "0.0.8"]
                 [funcool/cats "1.2.1"]
                 [manifold "0.1.5"]
                 [org.mock-server/mockserver-client-java "3.10.4"]
                 [com.datomic/datomic-pro "0.9.5350"]
                 [reduce-fsm "0.1.4"]

                 [io.dropwizard.metrics/metrics-core "3.1.2"]
                 [io.dropwizard.metrics/metrics-jvm "3.1.2"]
                 [com.aphyr/riemann-java-client "0.4.1"]
                 [com.aphyr/metrics3-riemann-reporter "0.4.1"]
                 [riemann-clojure-client "0.4.2"]

                 [com.rpl/specter "0.13.0"]

                 [criterium "0.4.4"]

                 ;; architecture
                 [com.stuartsierra/component "0.3.1"]
                 ]
  :profiles {:dev {:dependencies [[midje "1.8.3"]]}})
