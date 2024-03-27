(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}

  :plugins [[lein-ancient "0.6.15"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.specs.alpha "0.2.44"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/core.cache "0.7.2"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/data.zip "0.1.3"]

                 [io.pedestal/pedestal.interceptor "0.5.10"]

                 [org.clojure/data.xml "0.0.8"]
                 [uk.me.rkd.xml-validation "0.1.0-SNAPSHOT"]

                 [org.clojure/math.numeric-tower "0.0.4"]

                 [org.clojure/tools.logging "0.4.0"]
                 [prismatic/plumbing "0.5.5"]
                 [manifold/manifold "0.1.8"]
                 #_[com.datomic/datomic-free "0.9.5697"]
                 [com.datomic/datomic-pro "0.9.5927"]
                 [com.rpl/specter "1.1.2"]
                 [meander/epsilon "0.0.207"]
                 [irresponsible/anarchy "0.2.0"]

                 [pandec/pandect "0.6.1"]
                 [crypto-random/crypto-random "1.2.0"]
                 [prismatic/schema "1.1.10"]

                 [cheshire/cheshire "5.8.0"]

                 [http-kit/http-kit "2.6.0"]
                 [clj-http/clj-http "3.12.3"]
                 [less-awful-ssl/less-awful-ssl "1.0.6"]

                 [criterium/criterium "0.4.4"]

                 [funcool/cats "2.4.2"]
                 [funcool/promesa "11.0.678"]

                 [honeysql/honeysql "1.0.461"]

                 ;; markdown
                 [io.github.nextjournal/markdown "0.5.148"]]

  :eftest {:test-warn-time 500
           :report clojure.test/report
           }

  :profiles {:dev {:plugins [[lein-eftest "0.5.8"]
                             [lein-topology "0.2.0"]]
                   :dependencies [[eftest "0.5.8"]
                                  [lein-topology "0.2.0"]
                                  [org.clojure/test.check "0.9.0"]]}})
