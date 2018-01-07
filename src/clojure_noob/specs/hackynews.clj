(ns clojure-noob.specs.hackynews
  "Based on https://adambard.com/blog/domain-modeling-with-clojure-spec/"
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [org.httpkit.client :as http]
            [clojure.xml :as xml]
            [cheshire.core :as json]))

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument))

(s/def ::title string?)
(s/def ::description string?)
(s/def ::link-uri uri?)
(s/def ::comments-uri uri?)
(s/def ::pub-date string?)
(s/def ::content string?)

(s/def ::error string?)

(s/def ::rss-feed
  (s/keys
   :req-un [::title ::description ::link-uri ::items]))

(s/def ::feed-item
  (s/keys
   :req-un [::title ::description ::link-uri ::comments-uri ::pub-date]))

(s/def ::items (s/coll-of ::feed-item))

(s/def ::blacklist (s/coll-of #(instance? java.util.regex.Pattern %)))

(s/def ::feed-item-with-content
  (s/merge ::feed-item
           (s/keys :req-un [::content])))

(s/def ::fetched-item-result
  (s/or
   :ok ::feed-item-with-content
   :error ::feed-item))

(s/fdef try-fetch-item-content
        :args (s/cat :blacklist ::blacklist
                     :item ::feed-item)
        :ret ::fetched-item-result)

(s/fdef try-fetch-items
        :args (s/cat :blacklist ::blacklist
                     :feed ::rss-feed)
        :ret (s/coll-of ::fetched-item-result))

(s/fdef get-rss-feed
        :args (s/cat :uri uri?)
        :ret (s/or :ok ::rss-feed
                   :error ::error))

(s/def ::fetch-rss-feed-items
  (s/fspec
   :args (s/cat
          :blacklist ::blacklist
          :uri ::link-uri)
   :ret (s/or
         :ok (s/coll-of ::fetched-item-result)
         :error ::error)))

(defn- parse-item
  [item-node]
  (reduce
   (fn [item node]
     (case (:tag node)
              :title (assoc item :title (-> node :content first))
              :description (assoc item :description (-> node :content first))
              :link (assoc item :link-uri (-> node :content first (java.net.URI.)))
              :comments (assoc item :comments-uri (-> node :content first (java.net.URI.)))
              :pubDate (assoc item :pub-date (-> node :content first))))
   {} (:content item-node)))

(defn- parse-channel
  [channel-node]
  (reduce
   (fn [feed node]
     (case (:tag node)
       :title (assoc feed :title (-> node :content first))
       :description (assoc feed :description (-> node :content first))
       :link (assoc feed :link-uri (-> node :content first (java.net.URI.)))
       :item (update feed :items conj (parse-item node))
       feed))
   {} (:content channel-node)))

(defn get-rss-feed
  [uri]
  (let [feed (xml/parse (str uri))
        channel (-> feed :content first)]
    (parse-channel channel)))

(defn- fetch-item-content
  [item]
  (let [req {:query-prams {:url (str (:link-uri item))}}]))

(def result (get-rss-feed (java.net.URI. "https://news.ycombinator.com/rss")))
(-> result
    )
