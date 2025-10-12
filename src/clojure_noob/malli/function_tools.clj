(ns clojure-noob.malli.function-tools
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [malli.instrument :as mi]
            [malli.util :as mu]
            [malli.json-schema :as json-schema]
            [clojure.string :as str]
            [cheshire.core :as json]
            [malli.transform :as mt]))

(defn plus [x y]
  (+ x y))

(plus 1 2)
;; => 3

(m/validate fn? plus)
;; => true

(def =>plus [:=> [:cat :int :int] :int])

(m/validate =>plus plus)
;; => true


(def =>plus
  (m/schema
   [:=> [:cat :int :int] :int]
   {::m/function-checker mg/function-checker}))

(def Location
  [:map
   {:name "locationextrator"
    :description "Location"}
   [:location
    {:title "Location"
     :description "The location e.g Sao Paulo"}
    :string]
   [:unit [:enum "celsius" "fahrenheit"]]])

(defn json-schema-response [?schema]
  (let [schema (mu/closed-schema ?schema)]
    {:type "json_schema"
     :name ""
     :json_schema {:name (some-> schema (m/properties) :name (or "response"))
                   :schema (json-schema/transform schema)
                   :strict true}}))

(json-schema-response Location)
;; => {:type "json_schema",
;;     :json_schema
;;     {:name "locationextrator",
;;      :schema
;;      {:description "Location",
;;       :type "object",
;;       :properties
;;       {:location
;;        {:title "Location",
;;         :description "The location e.g Sao Paulo",
;;         :type "string"},
;;        :unit {:type "string", :enum ["celsius" "fahrenheit"]}},
;;       :required [:location :unit],
;;       :additionalProperties false},
;;      :strict true}}

(json-schema/transform
 Location)
;; => {:description "Location",
;;     :type "object",
;;     :properties
;;     {:location
;;      {:title "Location",
;;       :description "The location e.g Sao Paulo",
;;       :type "string"},
;;      :unit {:type "string", :enum ["celsius" "fahrenheit"]}},
;;     :required [:location :unit]}
;; => {:description "Location",
;;     :type "object",
;;     :properties
;;     {:location
;;      {:title "Location",
;;       :description "The location e.g Sao Paulo",
;;       :type "string"},
;;      :unit {:type "string", :enum ["celsius" "fahrenheit"]}},
;;     :required [:location :unit]}


(defn get-current-weather
  "Returns the weather"
  {:malli/schema [:=>
                  [:cat
                   Location]
                  :string]}
  [req]
  1)

(m/validate (mu/closed-schema Location)
            {:location "foo"
             :unit "celsius"
             :add 1})

(def small-int [:int {:max 6}])

(defn ^{:tool true} plus1 [x] (inc x))
(m/=> plus1 [:=> [:cat :int] small-int])

(defn plus2 [x] (inc x))
(m/=> ^{::tool true} plus2 [:=> [:cat :int] small-int])


(def =>plus3
  (m/schema
   [:=> [:cat :int :int] :int]
   {::tool true}))

(defn minus
  "a normal clojure function, no dependencies to malli"
  {:malli/schema [:=> [:cat :int] small-int]
   :mally/jdklsd 1}
  [x]
  (dec x))

(comment
  (mi/collect!)

  (m/function-schemas)
  ;; => {clojure-noob.malli.function-tools
  ;;     {plus1
  ;;      {:schema [:=> [:cat :int] [:int {:max 6}]],
  ;;       :ns clojure-noob.malli.function-tools,
  ;;       :name plus1},
  ;;      get-current-weather
  ;;      {:schema
  ;;       [:=> [:cat [:map {:name "locationextrator", :description "Location"} [:location {:title "Location", :description "The location e.g Sao Paulo"} :string] [:unit [:enum "celsius" "fahrenheit"]]]] :string],
  ;;       :ns clojure-noob.malli.function-tools,
  ;;       :name get-current-weather},
  ;;      plus2
  ;;      {:clojure-noob.malli.function-tools/tool true,
  ;;       :schema [:=> [:cat :int] [:int {:max 6}]],
  ;;       :ns clojure-noob.malli.function-tools,
  ;;       :name plus2},
  ;;      minus
  ;;      {:schema [:=> [:cat :int] [:int {:max 6}]],
  ;;       :ns clojure-noob.malli.function-tools,
  ;;       :name minus}}}


  (type (first (keys (m/function-schemas))))
  ;; => clojure.lang.Symbol

  (some-> (vals (m/function-schemas))
          ffirst
          first)
  ;; => plus1

  (some-> (vals (m/function-schemas))
          ffirst
          first
          type)
  ;; => clojure.lang.Symbol

  (let [fns (for [[ns fns] (m/function-schemas)
                  [fname data] fns]
              (ns-resolve ns fname))]
    (apply (first fns) [1]))

  (into {}
        (for [[ns fns] (m/function-schemas)
              [fname data] fns
              :when (::tool data)]
          [[(get data ::context "global") (name fname)]
           {:var (ns-resolve ns fname)
            :schema (:schema data)}]))



  (m/coerce [:or :int :string] true)

  (json-schema/transform [:or :int :string])

  (mu/closed-schema [:map
                     {:lo}]))

(defmacro ->tool
  [f]
  `(meta (var ~f)))
(map (fn [f]
       (->tool f))
     [get-current-weather])
(->tool get-current-weather)
;; => {:arglists ([req]),
;;     :doc "Returns the weather",
;;     :malli/schema [:=> [:cat :int] :int],
;;     :line 26,
;;     :column 1,
;;     :file
;;     "/Users/fabioyamate/Projects/clojure-noob/src/clojure_noob/malli/function_tools.clj",
;;     :name get_weather,
;;     :ns #namespace[clojure-noob.malli.function-tools]}

(defmacro tool-metadata
  [xxxx]
  `(let [t (meta (var ~xxxx))
         [_ input output] (:malli/schema t)]
     {:tool/name (str/replace (name (:name t))
                              #"-" "_")
      :tool/description (:doc t)
      :tool/parameters (second input)
      :tool/output output}))

(defmacro tool-metadata
  [xxxx]
  `(let [t (meta (var ~xxxx))]
     t))

(tool-metadata get-current-weather)




;; => {:name "get_current_weather",
;;     :description "Returns the weather",
;;     :parameters
;;     {:type "object",
;;      :properties
;;      {:location {:type "string"},
;;       :unit {:type "string", :enum ["celsius" "fahrenheit"]}},
;;      :required [:location :unit]}}

(m/validate [:map
             [:location :string]
             [:unit [:enum "celsius" "fahrenheit"]]]
            {:location "string"
             :unit "celsius"})
;; => true

(m/validate [:map
             [:location :string]
             [:unit [:enum "celsius" "fahrenheit"]]]
            {:location "string"
             })

(m/validate =>plus plus)
;; => true


(m/validate =>plus str)
;; => false

(m/explain =>plus str)
;; => {:schema [:=> [:cat :int :int] :int],
;;     :value #function[clojure.core/str],
;;     :errors
;;     ({:path [],
;;       :in [],
;;       :schema [:=> [:cat :int :int] :int],
;;       :value #function[clojure.core/str],
;;       :check
;;       {:total-nodes-visited 1,
;;        :depth 0,
;;        :result false,
;;        :smallest [(0 0)],
;;        :malli.core/result "00"}}
;;      {:path [1], :in [], :schema :int, :value "00"})}

(m/from-ast
 {:type :=>
  :input {:type :cat
          :children [{:type :int}]}
  :output {:type :int}
  :guard {:type :fn
          :value (fn [[[arg] ret]] (< arg ret))
          :properties {:error/message "argument should be less than return"}}}
 {::m/function-checker mg/function-checker})
;; => [:=> [:cat :int]
;;     :int [:fn #:error{:message "argument should be less than return"}#function[clojure-noob.malli.function-tools/eval14881/fn--14883]]]


(defmethod json-schema/accept
  :=>
  [_name schema children _options]
  (println schema)
  (println children)
  (println _options)
  {})

(defmethod json-schema/accept
  :function
  [_ schema _ options]
  [:function schema])


(json-schema/transform =>plus)

(json-schema/transform [:map
                        [:location
                         {:description "The location"}
                         :string]
                        [:unit
                         {:description "The unit"
                          :optional true}
                         [:enum
                          {:json-schema/default "celsius"}
                          "celsius" "farenheit"]]])
;; => {:type "object",
;;     :properties
;;     {:location {:description "The location", :type "string"},
;;      :unit
;;      {:description "The unit",
;;       :type "string",
;;       :enum ["celsius" "farenheit"],
;;       :default "celsius"}},
;;     :required [:location]}


(def Location
  [:map
   {:foo "dsjk"}
   [:components map?]
   [:location
    {:description "The location"}
    :string]
   [:unit
    {:description "The unit"
     :optional true}
    [:enum
     {:json-schema/default "celsius"}
     "celsius" "farenheit"]]])

(m/=> my-tool [:=> [:cat Location] :string])
(defn my-tool
  [x] x)

(def x (:schema (first (vals (first (vals (m/function-schemas)))))))

(let [[in out] (m/children x)]
  (assert (= 1 (count (m/children in))))
  (json-schema/transform (mu/dissoc (first (m/children in)) :components)))


(m/decode [:map [:int int?]]
          {:int "1"}
          mt/string-transformer)
;; => {:int 1}

(m/encode [:map [:int int?]]
          {:int 1}
          mt/string-transformer)
;; => {:int "1"}

mt/string-transform ;; decode -> from string to type, encode -> from type to string

;; identity
(let [schema [:map [:int int?]]
      data {:int 1}]
  (as-> data $
    (m/encode schema $ mt/string-transformer)
    (m/decode schema $ mt/string-transformer)
    (= data)))

(let [schema [:map [:int int?]]
      data {:int 1
            :date #inst "2010-01-01T00:00:00Z"}
      t mt/string-transformer]
  (println (m/encode schema data t))
  (as-> data $
    (m/encode schema $ t)
    (m/decode schema $ t)
    (= data)))

(def Address
  [:map
   [:id string?]
   [:tags [:set keyword?]]
   [:address
    [:map
     [:street string?]
     [:city string?]
     [:zip int?]
     [:lonlat [:tuple double? double?]]]]])

(m/decode
 Address
 {:id "Lillan",
  :tags ["coffee" "artesan" "garden"],
  :address {:street "Ahlmanintie 29"
            :city "Tampere"
            :zip 33100
            :lonlat [61.4858322 23.7854658]}}
 mt/json-transformer)
;; => {:id "Lillan",
;;     :tags #{:coffee :artesan :garden},
;;     :address
;;     {:street "Ahlmanintie 29",
;;      :city "Tampere",
;;      :zip 33100,
;;      :lonlat [61.4858322 23.7854658]}}

(m/encode
 Address
 {:id "Lillan",
  :tags ["coffee" "artesan" "garden"],
  :address {:street "Ahlmanintie 29"
            :city "Tampere"
            :zip 33100
            :lonlat [61.4858322 23.7854658]}}
 mt/json-transformer)
;; => {:id "Lillan",
;;     :tags #{:coffee :artesan :garden},
;;     :address
;;     {:street "Ahlmanintie 29",
;;      :city "Tampere",
;;      :zip 33100,
;;      :lonlat [61.4858322 23.7854658]}}


mt/collection-transformer


(m/validate int? 42)

((m/validator int?) 42)
