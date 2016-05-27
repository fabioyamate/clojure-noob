(ns clojure-noob.components
  (:require [com.stuartsierra.component :as component]))

(defrecord Database [host port connection]
  ;; Implement the Lifecycle protocol
  component/Lifecycle

  (start [component]
    (println ";; Starting database")
    ;; In the 'start' method, initialize this component
    ;; and start it running. For example, connect to a
    ;; database, create thread pools, or initialize shared
    ;; state.
    (let [conn (connect-to-database host port)]
      ;; Return an updated version of the component with
      ;; the run-time state assoc'd in.
      (assoc component :connection conn)))

  (stop [component]
    (println ";; Stopping database")
    ;; In the 'stop' method, shut down the running
    ;; component and release any external resources it has
    ;; acquired.
    (.close connection)
    ;; Return the component, optionally modified. Remember that if you
    ;; dissoc one of a record's base fields, you get a plain map.
    (assoc component :connection nil)))

(defn new-database [host port]
  (map->Database {:host host :port port}))

(defrecord ExampleComponent [options cache database scheduler]
  component/Lifecycle

  (start [this]
    (println ";; Starting ExampleComponent")
    ;; In the 'start' method, a component may assume that its
    ;; dependencies are available and have already been started.
    (assoc this :admin (get-user database "admin")))

  (stop [this]
    (println ";; Stopping ExampleComponent")
    ;; Likewise, in the 'stop' method, a component may assume that its
    ;; dependencies will not be stopped until AFTER it is stopped.
    this))

(defn example-component [config-options]
  (map->ExampleComponent {:options config-options
                          :cache (atom {})}))

(defn example-system [config-options]
  (let [{:keys [host port]} config-options]
    (component/system-map
     :db (new-database host port)
     :scheduler (new-scheduler)
     :app (component/using
           (example-component config-options)
           {:database  :db
            :scheduler :scheduler}))))
