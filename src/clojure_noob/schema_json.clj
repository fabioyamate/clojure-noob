(ns clojure-noob.schema-json
  (:require [schema.core :as s]
            [schema-forms.core :as sf]))

(def units (s/enum "celsius" "farenheit"))

(s/defschema Location
  "Descripition Location"
  {:location s/Str
   :unit units})

(sf/prismatic->json-schema
 Location)
;; => {:type "object",
;;     :title "Location",
;;     :properties
;;     {:location {:type "string", :title "Location"},
;;      :unit
;;      {:type "string", :enum #{"celsius" "farenheit"}, :title "Unit"}},
;;     :additionalProperties false,
;;     :required [:location :unit]}
;; => {:type "object",
;;     :properties
;;     {:temperature {:type "string", :title "Temperature"},
;;      :unit
;;      {:type "string", :enum #{"celsius" "farenheit"}, :title "Unit"}},
;;     :additionalProperties false,
;;     :required [:temperature :unit]}

(s/defschema Metadata
  "Fooo"
  {:source s/Str})

(s/defschema Page
  {:number s/Int
   :metdata Metadata})

(s/defschema Document
  {:pages [Page]
   (s/optional-key :a) s/Bool
   :num (s/maybe s/Str)
   :str-or-list (s/either s/Str [s/Int])
   :metdata Metadata})

(sf/prismatic->json-schema
 Document)
;; => {:type "object",
;;     :title "Document",
;;     :properties
;;     {:pages
;;      {:type "array",
;;       :items
;;       {:type "object",
;;        :title "Page",
;;        :properties
;;        {:number {:type "integer", :title "Number"},
;;         :metdata
;;         {:type "object",
;;          :title "Metdata",
;;          :properties {:source {:type "string", :title "Source"}},
;;          :additionalProperties false,
;;          :required [:source]}},
;;        :additionalProperties false,
;;        :required [:number :metdata]},
;;       :minItems 0,
;;       :title "Pages"},
;;      :a {:type "boolean", :title "a"},
;;      :num {:type "string", :title "Num"},
;;      :str-or-list
;;      {:anyOf
;;       [{:type "string"}
;;        {:type "array",
;;         :items {:type "integer", :title "integer?"},
;;         :minItems 0}],
;;       :title "Str Or List"},
;;      :metdata
;;      {:type "object",
;;       :title "Metdata",
;;       :properties {:source {:type "string", :title "Source"}},
;;       :additionalProperties false,
;;       :required [:source]}},
;;     :additionalProperties false,
;;     :required [:pages :num :str-or-list :metdata]}
;; => {:type "object",
;;     :title "Document",
;;     :properties
;;     {:pages
;;      {:type "array",
;;       :items
;;       {:type "object",
;;        :title "Page",
;;        :properties
;;        {:number {:type "integer", :title "Number"},
;;         :metdata
;;         {:type "object",
;;          :title "Metdata",
;;          :properties {:source {:type "string", :title "Source"}},
;;          :additionalProperties false,
;;          :required [:source]}},
;;        :additionalProperties false,
;;        :required [:number :metdata]},
;;       :minItems 0,
;;       :title "Pages"},
;;      :a {:type "boolean", :title "a"},
;;      :metdata
;;      {:type "object",
;;       :title "Metdata",
;;       :properties {:source {:type "string", :title "Source"}},
;;       :additionalProperties false,
;;       :required [:source]}},
;;     :additionalProperties false,
;;     :required [:pages :metdata]}
;; => {:type "object",
;;     :title "Document",
;;     :properties
;;     {:pages
;;      {:type "array",
;;       :items
;;       {:type "object",
;;        :title "Page",
;;        :properties
;;        {:number {:type "integer", :title "Number"},
;;         :metdata
;;         {:type "object",
;;          :title "Metdata",
;;          :properties {:source {:type "string", :title "Source"}},
;;          :additionalProperties false,
;;          :required [:source]}},
;;        :additionalProperties false,
;;        :required [:number :metdata]},
;;       :minItems 0,
;;       :title "Pages"},
;;      :metdata
;;      {:type "object",
;;       :title "Metdata",
;;       :properties {:source {:type "string", :title "Source"}},
;;       :additionalProperties false,
;;       :required [:source]}},
;;     :additionalProperties false,
;;     :required [:pages :metdata]}
;; => {:type "object",
;;     :title "Document",
;;     :properties
;;     {:pages
;;      {:type "array",
;;       :items
;;       {:type "object",
;;        :title "Page",
;;        :properties {:number {:type "integer", :title "Number"}},
;;        :additionalProperties false,
;;        :required [:number]},
;;       :minItems 0,
;;       :title "Pages"}},
;;     :additionalProperties false,
;;     :required [:pages]}

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
b
(type (:schema (k get-weather)))
;; => schema.core.FnSchema

(let [[_f output input] (s/explain (:schema (k get-weather)))])

(sf/prismatic->json-schema
 get-weather)
;; => Execution error (IllegalArgumentException) at schema-forms.schemafy/eval12151$fn$G (schemafy.clj:30).
;;    No implementation of method: :schemafy* of protocol: #'schema-forms.schemafy/JsonSchemafy found for class: clojure_noob.schema_json$eval15419$get_weather__15424
