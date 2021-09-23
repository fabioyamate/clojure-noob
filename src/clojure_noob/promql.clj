(ns clojure-noob.promql
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

;; services_http_request_latency_seconds_bucket - metric name time-series
;; {} - time-series filter
;; = (equal), != (not equal), =~ (matches), !~ (not matches)
;; [5m] - range selector on time-series, so it will decide how far it should go in relation to the instant... [t-5m, t], return all collected data-points in the given time and past period
;; ms, s, m, h, d, w, y
;; offset - shifts the t back to t-offset

;; operators
;; +, -, *, /, %, ^
;; operations between two scalars
;; operators vector and scalar (applying the operator to each value, [1,2]*10 = [10,20])
;; instant vector [1,2] * [3,4] = [3,8] (unmatched elements are discarted)

;; comparison
;; ==, !=, <, >, >=, <=
;; operations between two scalars
;; operators vector and scalar select only points that matches the criteria (filter)
;; instant vector [1,2] < [3,4] = [1 < 3,2 < 4] select values that matches in the corresponding collection



(defn multiple-request-paths
  [paths]
  (str "path=~\""
       (clojure.string/join "|" paths)
       "\""))

(defn single-path
  [path]
  (str "path=\"" path "\""))

(defn- normalize-value-or-multi-values
  [value-or-multi-values]
  (if (and (coll? value-or-multi-values))
    (if (> (count value-or-multi-values) 1)
      [:multiple value-or-multi-values]
      [:single (first value-or-multi-values)])
    [:single value-or-multi-values]))

(defn equal [value]
  {:match/op "="
   :match/value value})

(defn not-equal [value]
  {:match/op "!="
   :match/value value})

(defn regex-match [value-or-values]
  {:match/op "=~"
   :match/value (if (coll? value-or-values)
                  (->> value-or-values
                       (remove nil?)
                       (map name)
                       (str/join "|"))
                  value-or-values)})

(defn not-regex-match [value-or-values]
  {:match/op "!~"
   :match/value (if (coll? value-or-values)
                  (->> value-or-values
                       (remove nil?)
                       (map name)
                       (str/join "|"))
                  value-or-values)})

(defn match
  [matching-labels]
  (str "{"
       (clojure.string/join ","
                            (for [[label value-or-values] matching-labels
                                  :when (some? value-or-values)
                                  :let [[matching-type value] (normalize-value-or-multi-values value-or-values)]]
                              (if (= :single matching-type)
                                (str (name label) "=\"" value "\"")
                                (str (name label) "=~\"" (clojure.string/join "|" value) "\""))))
       "}"))

(comment
  (match {:service "foo" :path ["http"]})
  )

(defn- render-match-op
  [label {:match/keys [op value]}]
  (str (name label) op "\"" value "\""))

(defn- labels-filter
  [filters]
  (str "{"
       (str/join ","
                 (for [[label scalar-or-op] filters
                       :when (some? scalar-or-op)]
                   (cond (and (map? scalar-or-op)
                              (contains? scalar-or-op :match/op))
                         (render-match-op label scalar-or-op)

                         (coll? scalar-or-op)
                         (render-match-op label (regex-match scalar-or-op))

                         :else
                         (render-match-op label (equal scalar-or-op)))))
       "}"))

(labels-filter {:foo "bar" :bar ["car" :caz nil]
                :fooz (equal "fooz")
                :status (equal 200)
                :active (equal true)
                :error 300
                :baaz (not-equal "baaz")
                :car (regex-match "2.*")
                :cdr (not-regex-match "5.*")
                :door (regex-match ["c" "b"])
                :ignore nil})

(defn metric-selector
  ([metric-name]
   metric-name)
  ([metric-name labels-or-range-value]
   (if (map? labels-or-range-value)
     (metric-selector metric-name labels-or-range-value nil)
     (metric-selector metric-name nil labels-or-range-value)))
  ([metric-name labels range-value]
   (str metric-name (labels-filter labels)
        (when range-value (str "[" range-value "]")))))

(defn escape-parameters
  [parameters]
  (mapv (fn [param]
          (if (string? param)
            (str "\"" param "\"")
            param))
        parameters))

(defn labels-list
  [labels]
  (str "(" (str/join "," (map name labels)) ")"))

(defn render-aggr-op
  [{:aggr-op/keys [op without by parameters vector-expr]}]

  (apply str
         [(name op)
          "("
          (str/join "," (concat (escape-parameters parameters) [vector-expr]))
          ")"
          (cond without (str " without " (labels-list without))
                by (str " by " (labels-list by)))

          ]))

(defn sum
  [vector-selector & {:keys [by without]}]
  {:pre [(not (and without by))]}
  (render-aggr-op
   (cond-> {:aggr-op/op :sum
            :aggr-op/vector-expr vector-selector}
     (some? by) (assoc :aggr-op/by by)
     (some? without) (assoc :aggr-op/without without))))

(defn rate [vector-selector]
  (render-aggr-op
   {:aggr-op/op :rate
    :aggr-op/vector-expr vector-selector}))


(sum (rate (metric-selector "http_total" "5m")) :by [:path])



(comment
  (render-aggr-op {:aggr-op/op :sum
                   ;;:aggr-op/without [:path "jon"]
                   :aggr-op/by [:path]
                   :aggr-op/parameters [5, "foo", true]
                   :aggr-op/vector-expr "http_total"
                   })

  (render-aggr-op {:aggr-op/op :sum :aggr-op/without ["foo"] :aggr-op/parameters [5]}))
