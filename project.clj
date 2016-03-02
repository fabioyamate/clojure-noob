(defproject clojure-noob "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.logic "0.8.10"]
                 [org.clojure/core.typed "0.3.22"]
                 [prismatic/schema "1.0.4"]
		 [org.clojure/algo.monads "0.1.5"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.xml "0.0.8"]
		 [org.mock-server/mockserver-client-java "3.9.17"]
                 [reduce-fsm "0.1.4"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]}})
