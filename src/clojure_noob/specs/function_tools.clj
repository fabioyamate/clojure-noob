(ns clojure-noob.specs.function-tools
  (:require [spec-tools.json-schema :as json-schema]
            [spec-tools.core :as st]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::street string?)
(s/def ::city #{:tre :hki})
(s/def ::address (s/keys :req-un [::street ::city]))
(s/def ::user (s/keys :req-un [::id ::name ::address]))

(json-schema/transform ::user)
;; => {:type "object",
;;     :properties
;;     {"id" {:type "string"},
;;      "name" {:type "string"},
;;      "address"
n;;      {:type "object",
;;       :properties
;;       {"street" {:type "string"}, "city" {:enum [:tre :hki]}},
;;       :required ["street" "city"],
;;       :title "clojure-noob.specs.function-tools/address"}},
;;     :required ["id" "name" "address"],
;;     :title "clojure-noob.specs.function-tools/user"}

(s/def :weather/location
  (st/spec {:spec string?
            :name "the location"
            :description "The city and state, e.g. San Francisco, CA."}))
(s/def :weather/unit
  (st/spec {:spec #{:celsius :fahrenheit}}))

(defn get-current-weather
  [{:keys [location unit]}]
  "30")

(s/fdef get-current-weather
  :args (s/cat :input (s/keys :req-un [:weather/location]
                              :opt-un [:weather/unit]))
  :ret string?)

(require '[clojure.spec.test.alpha :as stest])
(stest/instrument)

(get-current-weather {:location "Sao Paulo, SP"
                      :unit :celsius})

#_(get-current-weather {:location 1})

(meta (s/spec (s/get-spec (resolve 'get-current-weather))))
;; => {:line 1744,
;;     :column 5,
;;     :clojure.spec.alpha/name
;;     clojure-noob.specs.function-tools/get-current-weather}
(meta (s/get-spec (resolve 'get-current-weather)))
;; => {:line 1744,
;;     :column 5,
;;     :clojure.spec.alpha/name
;;     clojure-noob.specs.function-tools/get-current-weather}

(s/describe* (:args (s/get-spec (resolve 'get-current-weather))))

(json-schema/transform (last (s/form (:args (s/get-spec (resolve 'get-current-weather))))))
;; => {:type "object",
;;     :properties
;;     {"location"
;;      {:type "string",
;;       :description "The city and state, e.g. San Francisco, CA",
;;       :title "weather/location"},
;;      "unit" {:enum [:fahrenheit :celsius], :title "weather/unit"}},
;;     :required ["location"]}

(defn foo [x])

(json-schema/transform (:args (s/get-spec (resolve 'get-current-weather))))
;; => {:type "array",
;;     :items
;;     {:anyOf
;;      [{:type "object",
;;        :properties
;;        {"location" {:type "string"},
;;         "unit" {:enum [:fahrenheit :celsius]}},
;;        :required ["location"]}]}}


(s/specize (:args (s/get-spec (resolve 'get-current-weather))))

(defn xxx [f]
  #_(resolve (quote f))
  (s/get-spec f))

(defn sd
  "Doc string"
  [x]
  x)

(meta (sd `update))

(defmacro kkk [x]
  `(meta (resolve ~x)))

(kkk `get-current-weather)

(meta (resolve `sd))


(defn tool-spec
  [tool]
  (meta (resolve tool))
  #_(let [{tool-name :name tool-description :doc} (meta tool)
          parameters (json-schema/transform (last (s/form (:args (s/get-spec tool)))))]
      {:name (str/replace (name tool-name) #"-" "_")
       :description tool-description
       :input_schema parameters}))

(defmacro tool
  [f]
  `(tool-spec ~f))

(meta `get-current-weather)

(name `get-current-weather)

`get-current-weather
;; => clojure-noob.specs.function-tools/get-current-weather

(resolve `get-current-weather)
;; => #'clojure-noob.specs.function-tools/get-current-weather

(tool-spec `get-current-weather)
;; => {:arglists ([{:keys [location unit]}]),
;;     :line 35,
;;     :column 1,
;;     :file
;;     "/Users/fabioyamate/Projects/clojure-noob/src/clojure_noob/specs/function_tools.clj",
;;     :name get-current-weather,
;;     :ns #namespace[clojure-noob.specs.function-tools]}

(tool-spec `get-current-weather)

(macroexpand-1
 (tool `get-current-weather))

get-current-weather
;; => #function[clojure-noob.specs.function-tools/get-current-weather]

`get-current-weather
;; => clojure-noob.specs.function-tools/get-current-weather

(quote get-current-weather)
;; => get-current-weather

(defmacro x
  [l]
  `(meta '~l))

(x get-current-weather)
;; => #function[clojure-noob.specs.function-tools/get-current-weather]
