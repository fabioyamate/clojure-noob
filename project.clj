(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :auth}}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/core.cache "0.6.5"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/data.zip "0.1.2"]
                 [org.clojure/data.xml "0.0.8"]
                 [manifold "0.1.6"]
                 [com.datomic/datomic-free "0.9.5561.62"]
                 ;; [com.datomic/datomic-pro "0.9.5407"]
                 [com.rpl/specter "1.0.4"]

                 [criterium "0.4.4"]
                 ]
  :profiles {:dev {:plugins [[lein-midje "3.2.1"]]
                   :dependencies [[midje "1.9.0-alpha11"]
                                  [org.clojure/test.check "0.9.0"]]}})
