(ns clojure-noob.schema-json
  (:require [schema.core :as s]
            [schema-forms.core :as sf]))

(def units (s/enum "celsius" "farenheit"))

(def Location
  {:location s/Str
   :unit units})

(sf/prismatic->json-schema
 Location)
;; => {:type "object",
;;     :properties
;;     {:temperature {:type "string", :title "Temperature"},
;;      :unit
;;      {:type "string", :enum #{"celsius" "farenheit"}, :title "Unit"}},
;;     :additionalProperties false,
;;     :required [:temperature :unit]}

(s/defn get-weather :- s/Int
  [location :- Location])

(defmacro k [f]
  `(meta (var ~f)))

(k get-weather)
;; => {:schema
;;     (=> Int {:location Str, :unit (enum "celsius" "farenheit")}),
;;     :ns #namespace[clojure-noob.schema-json],
;;     :name get-weather,
;;     :file
;;     "/Users/fabioyamate/Projects/clojure-noob/src/clojure_noob/schema_json.clj",
;;     :column 1,
;;     :raw-arglists ([location :- Location]),
;;     :line 21,
;;     :arglists ([location]),
;;     :doc "Inputs: [location :- Location]\n  Returns: s/Int"}

(sf/prismatic->json-schema (:schema (k get-weather)))
;; => {:type "object",
;;     :properties
;;     {:output-schema {:type "integer", :title "Output Schema"},
;;      :input-schemas
;;      {:type "array",
;;       :items
;;       {:type "array",
;;        :items
;;        {:type "object",
;;         :properties
;;         {:location {:type "string", :title "Location"},
;;          :unit
;;          {:type "string",
;;           :enum #{"celsius" "farenheit"},
;;           :title "Unit"}},
;;         :additionalProperties false,
;;         :required [:location :unit]},
;;        :maxItems 1,
;;        :minItems 1},
;;       :minItems 0,
;;       :title "Input Schemas"}},
;;     :additionalProperties false,
;;     :required [:output-schema :input-schemas]}

(type (:schema (k get-weather)))
;; => schema.core.FnSchema

(let [[_f output input] (s/explain (:schema (k get-weather)))])

(sf/prismatic->json-schema
 get-weather)
;; => Execution error (IllegalArgumentException) at schema-forms.schemafy/eval12151$fn$G (schemafy.clj:30).
;;    No implementation of method: :schemafy* of protocol: #'schema-forms.schemafy/JsonSchemafy found for class: clojure_noob.schema_json$eval15419$get_weather__15424
