(require '[cljs.build.api :as api])

(def source-dir "src")

(def compiler-config  {:main          'my-app.core
                       :output-to     "target/my-app/main.js"
                       :output-dir    "target/my-app/main"
                       :target        :nodejs
                       :optimizations :simple
                       :source-map    "target/my-app/main.js.map"})

(defn try-require  [ns-sym]
  (try  (require ns-sym) true  (catch Exception e false)))

(defmacro with-namespaces
  [namespaces & body]
  (if  (every? try-require namespaces)
    `(do ~@body)
    `(println "task not available - required dependencies not found")))

(defmulti task first)

(defmethod task "compile"
  [args]
  (api/build source-dir compiler-config))

(defmethod task "figwheel"
  [[_ port]]
  (with-namespaces [figwheel-sidecar.repl-api]
    (figwheel-sidecar.repl-api/start-figwheel!
      {:figwheel-options (when port
                           {:nrepl-port (some-> port Long/parseLong)
                            :nrepl-middleware ["cider.nrepl/cider-middleware"
                                               "refactor-nrepl.middleware/wrap-refactor"
                                               "cmeric.piggieback/wrap-cljs-repl"]})
       :all-builds [{:id "dev"
                     :figwheel true
                     :source-paths [source-dir]
                     :compiler dev-config}]})
    (when-not port
      (figwheel-sidecar.repl-api/cljs-repl))))

(defmethod task :default
  [args]
  (let [all-tasks (-> task methods (dissoc :default) keys sort)
        interposed (->> all-tasks (interpose ", ") (apply str))]
    (println "Unknown or missing task. Choose one of: " interposed)
    (System/exit 1)))

(task *command-line-args*)
