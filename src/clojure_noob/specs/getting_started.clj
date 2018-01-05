(ns clojure-noob.specs.getting-started
  (:require [clojure.spec.alpha :as s]))

(s/conform even? 1000)
(s/conform even? 1)

(s/valid? even? 10)
(s/valid? even? 1)

(s/def ::int int?)
(s/valid? ::int 1)
(s/conform ::int 1.5M)

(s/def ::positive-even (s/and int? pos? even?))

(s/valid? ::positive-even 2)
(s/valid? ::positive-even -2)

(s/def ::odd-or-negative (s/or :odd odd?
                               :negative neg?))

(s/valid? ::odd-or-negative 1)
(s/valid? ::odd-or-negative 2)
(s/valid? ::odd-or-negative -2)

(s/conform ::odd-or-negative 1)
(s/conform ::odd-or-negative -2)

(s/explain ::odd-or-negative 2)
(s/explain-str ::odd-or-negative 2)
(s/explain-data ::odd-or-negative 2)

;;; entity maps

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/and string? #(re-matches email-regex %)))

(s/def ::acctid int?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::email ::email-type)

(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))

(s/valid? ::person
          {::first-name "Elon"
           ::last-name "Musk"
           ::email "elon@example.com"})

(s/explain-str ::person
               {::first-name "Elon"})

(s/def :unq/person
  (s/keys :req-un [::first-name ::last-name ::email]
          :opt-un [::phone]))

(defrecord Person [first-name last-name email phone])

(s/conform :unq/person
           {:first-name "Elon"
            :last-name "Musk"
            :email "elon@example.com"})

(s/conform :unq/person
           (->Person "Elon" "Musk" "elon@example.com" nil))

(s/def ::port number?)
(s/def ::host string?)
(s/def ::id keyword?)
(s/def ::server (s/keys* :req [::id ::host]
                         :opt [::port]))

(s/conform ::server [::id :s1 ::host "example.com" ::port 5555])

(s/def :animal/kind string?)
(s/def :animal/says string?)
(s/def :animal/common (s/keys :req [:animal/kind :animal/says]))

(s/def :dog/tail? boolean?)
(s/def :dog/breed string?)
(s/def :animal/dog (s/merge :animal/common
                            (s/keys :req [:dog/tail? :dog/breed])))

(s/valid? :animal/dog
          {:animal/kind "dog"
           :animal/says "woof"
           :dog/tail? true
           :dog/breed "retriever"})

;;; multi-spec

(s/def :event/type keyword?)
(s/def :event/timestamp int?)
(s/def :search/url string?)
(s/def :error/message string?)
(s/def :error/code int?)

(defmulti event-type :event/type)

(defmethod event-type :event/search [_]
  (s/keys :req [:event/type :event/timestamp :search/url]))

(defmethod event-type :event/error [_]
  (s/keys :req [:event/type :event/timestamp :error/message :error/code]))

(s/def :event/event (s/multi-spec event-type :event/type))

(s/valid? :event/event
          {:event/type :event/search
           :event/timestamp 1463970123000
           :search/url "https://clojure.org"})

(s/valid? :event/event
          {:event/type :event/error
           :event/timestamp 1463970123000
           :error/message "Invalid host"
           :error/code 500})

(s/valid? :event/event
          {:event/type :event/restart})

(s/conform :event/event
           {:event/type :event/restart})

(s/explain-str :event/event
               {:event/type :event/restart})

(s/explain-str :event/event
               {:event/type :event/search
                :search/url 200})

(s/conform (s/coll-of keyword?) [:a :b :c])

(s/conform (s/coll-of number?) #{1 2 3})

(s/conform (s/coll-of int? :count 3) [1 2 3])
(s/conform (s/coll-of int? :count 2) [1 2 3])

(s/conform (s/coll-of int? :min-count 2) [1 2])
(s/conform (s/coll-of int? :min-count 3) [1 2 3])

(s/conform (s/coll-of int? :distinct true) [1 2 3])
(s/explain-str (s/coll-of int? :distinct true) [2 2 2])

(s/def ::vnum3 (s/coll-of number? :kind vector? :count 3))
(s/conform ::vnum3 [1 2 3])
(s/conform ::vnum3 #{1 2 3})

(s/def ::point (s/tuple double? double? double?))

(s/conform ::point [1.5 2.5 -0.5])

(s/def ::point3 (s/cat :x double? :y double? :z double?))

(s/conform ::point3 [1.5 2.5 -0.5])

(s/def ::scores (s/map-of string? int?))

(s/conform ::scores {"Sally" 1000 "Joe" 500})

(s/def ::ingredient (s/cat :quantity number?
                           :unit keyword?))

(s/conform ::ingredient [2 :teaspoon])

(s/def ::seq-of-keywords (s/* keyword?))

(s/conform ::seq-of-keywords [])
(s/conform ::seq-of-keywords [:a])

(s/def ::seq-of-keywords+ (s/+ keyword?))
(s/conform ::seq-of-keywords+ [])
(s/conform ::seq-of-keywords+ [:a])

(s/def ::odds-then-maybe-even (s/cat :odds (s/+ odd?)
                                     :even (s/? even?)))

(s/conform ::odds-then-maybe-even [1 3 5 2])

(s/def ::odds-then-maybe-evens (s/cat :odds (s/+ odd?)
                                      :evens (s/* even?)))

(s/conform ::odds-then-maybe-evens [1 3 5 2 4])

(s/def ::opts (s/* (s/cat :opt keyword? :val boolean?)))
(s/conform ::opts [:silent? false :verbose true])

(s/def ::config (s/*
                 (s/cat :prop string?
                        :val  (s/alt :s string? :b boolean?))))
(s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])

(s/def ::even-strings (s/& (s/* string?) #(even? (count %))))

(s/valid? ::even-strings ["a"])
(s/valid? ::even-strings ["a" "b"])
(s/valid? ::even-strings ["a" "b" "c"])
(s/valid? ::even-strings ["a" "b" "c" "d"])

(s/def ::range (s/and (s/cat :start int? :end int?)
                      #(< (:start %) (:end %))))


(s/conform ::range [1 2])
(s/conform ::range [2 1])
