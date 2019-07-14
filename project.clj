(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :auth}}
  :plugins [[lein-ancient "0.6.15"]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.specs.alpha "0.2.44"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/core.cache "0.7.2"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/data.zip "0.1.3"]

                 [org.clojure/data.xml "0.0.8"]
                 [uk.me.rkd.xml-validation "0.1.0-SNAPSHOT"]

                 [org.clojure/math.numeric-tower "0.0.4"]

                 [org.clojure/tools.logging "0.4.0"]
                 [prismatic/plumbing "0.5.5"]
                 [manifold "0.1.8"]
                 [com.datomic/datomic-free "0.9.5697"]
                 ;; [Comcid.datomic/datomic-pro "0.9.5407"]
                 [com.rpl/specter "1.1.2"]
                 [irresponsible/anarchy "0.2.0"]

                 [pandect "0.6.1"]
                 [crypto-random "1.2.0"]

                 [cheshire "5.8.0"]

                 [http-kit "2.2.0"]

                 [criterium "0.4.4"]]

  :eftest {:test-warn-time 500
           :report clojure.test/report
           }

  :profiles {:dev {:plugins [[lein-eftest "0.5.8"]
                             [lein-topology "0.2.0"]]
                   :dependencies [[eftest "0.5.8"]
                                  [lein-topology "0.2.0"]
                                  [org.clojure/test.check "0.9.0"]]}})
