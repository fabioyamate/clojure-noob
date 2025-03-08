(ns clojure-noob.malli.function-tools
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [malli.instrument :as mi]
            [malli.util :as mu]
            [malli.json-schema :as json-schema]
            [clojure.string :as str]))

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
   {:description "Location"}
   [:location :string]
   [:unit [:enum "celsius" "fahrenheit"]]])

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

(defn plus1 [x] (inc x))
(m/=> plus1 [:=> [:cat :int] small-int])

(mi/collect!)

(m/function-schemas)

(m/coerce [:or :int :string] true)

(json-schema/transform [:or :int :string])

(mu/closed-schema [:map
                   {:lo}])

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
  (println [:=> schema children _options])
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
