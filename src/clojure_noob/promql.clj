(ns clojure-noob.promql
  (:require [clojure.spec.alpha :as s]))

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
  ;;{:pre [(not (and without by))]}
  (apply str
         [(name op)
          "("
          (str/join "," (concat (escape-parameters parameters) [vector-expr]))
          ")"
          (cond without (str "without" (labels-list without))
                by (str "by" (labels-list by)))
          ]))


(comment
(render-aggr-op {:aggr-op/op :sum
                 :aggr-op/without [:path "jon"]
                 :aggr-op/by [:path]
                                        ;:aggr-op/parameters [5, "foo", true]
                 :aggr-op/vector-expr "http_total"
                 })

(render-aggr-op {:aggr-op/op :sum :aggr-op/without ["foo"] :aggr-op/by ["foo"]}))

