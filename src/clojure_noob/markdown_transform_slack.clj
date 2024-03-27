(ns clojure-noob.markdown-transform-slack
  (:require [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]
            [clojure.string :as str]
            [cheshire.core :as json]))

(def text
  "Hello _john!_\n\n* a **foo**\n* the [*b*ar](https://slack.com)\n\n*Bye* friend\n\n```\n(println \"hello\")\n```\n\nOk, `done` :smiley:")

(println "--- text")
(println text)

(def data
  (md/parse text))

(println "--- AST")
(clojure.pprint/pprint data)
data
;; => {:toc
;;     {:type :toc,
;;      :children
;;      [{:type :toc,
;;        :content [{:type :text, :text "Letter"}],
;;        :heading-level 1,
;;        :attrs {:id "letter"},
;;        :path [:content 0]}]},
;;     :footnotes [],
;;     :content
;;     [{:type :heading,
;;       :content [{:type :text, :text "Letter"}],
;;       :heading-level 1,
;;       :attrs {:id "letter"}}
;;      {:type :paragraph,
;;       :content
;;       [{:type :text, :text "Hello "}
;;        {:type :em, :content [{:type :text, :text "john!"}]}]}
;;      {:type :bullet-list,
;;       :content
;;       [{:type :list-item,
;;         :content
;;         [{:type :plain,
;;           :content
;;           [{:type :text, :text "a "}
;;            {:type :strong, :content [{:type :text, :text "foo"}]}]}]}
;;        {:type :list-item,
;;         :content
;;         [{:type :plain,
;;           :content
;;           [{:type :text, :text "the "}
;;            {:type :link,
;;             :content
;;             [{:type :em, :content [{:type :text, :text "b"}]}
;;              {:type :text, :text "ar"}],
;;             :attrs {:href "https://slack.com"}}]}]}]}
;;      {:type :paragraph,
;;       :content
;;       [{:type :em, :content [{:type :text, :text "Bye"}]}
;;        {:type :text, :text " friend"}]}
;;      {:type :image,
;;       :content [{:type :text, :text "image"}],
;;       :attrs {:src "files://the.png", :alt ""}}
;;      {:type :code,
;;       :content [{:type :text, :text "(println \"hello\")\n"}],
;;       :language "",
;;       :info ""}],
;;     :type :doc,
;;     :title "Letter"}

(declare ->slack)

(def slack-renderers
  {:doc (fn [ctx {:keys [content] :as node}]
          {:blocks (keep (partial ->slack (assoc ctx ::parent node)) content)})
   :paragraph (fn [ctx {:keys [content text] :as node}]
                (cond
                  text {:type "section"
                        :text {:type "plain_text"
                               :text text
                               :emoji true}}
                  content {:type "rich_text"
                           :elements [{:type "rich_text_section"
                                       :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]}))
   :text (fn [_ {:keys [text]}]
           {:type "text"
            :text text})
   :link (fn [ctx {:keys [text content attrs] :as node}]
           (cond
             text {:type "link"
                   :text text
                   :url (:href attrs)}
             content (if (contains? ctx ::parent)
                       (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                            flatten
                            (map #(assoc % :type "link" :url (:href attrs))))
                       {:type "rich_text"
                        :elements [{:type "rich_text_section"
                                    :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))

   :em (fn [ctx {:keys [text content] :as node}]
         (cond
           ;; if it is just text
           text {:type "text"
                 :text text
                 :style {:italic true}}
           ;; if it contains subnodes
           content (if (contains? ctx ::parent)
                     ;; if it has a parent, it should not spawn a new node, it should return just elements
                     (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                          flatten
                          (map #(assoc-in % [:style :italic] true)))
                     ;; if has no parent, opens a new block
                     {:type "rich_text"
                      :elements [{:type "rich_text_section"
                                  :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))
   :bold (fn [ctx {:keys [text content] :as node}]
           (cond
             text {:type "text"
                   :text text
                   :style {:bold true}}
             content (if (contains? ctx ::parent)
                       (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                            flatten
                            (map #(assoc-in % [:style :bold] true)))
                       {:type "rich_text"
                        :elements [{:type "rich_text_section"
                                    :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))

   :strong (fn [ctx {:keys [text content] :as node}]
             (cond
               text {:type "text"
                     :text text
                     :style {:bold true}}
               content (if (contains? ctx ::parent)
                         (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                              flatten
                              (map #(assoc-in % [:style :bold] true)))
                         {:type "rich_text"
                          :elements [{:type "rich_text_section"
                                      :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))

   :plain (fn [ctx {:keys [text content] :as node}]
            (cond
              text {:type "text"
                    :text text}
              content (if (contains? ctx ::parent)
                        (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))
                        {:type "rich_text"
                         :elements [{:type "rich_text_section"
                                     :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))

   ;; lists
   :bullet-list (fn [ctx {:keys [content text] :as node}]
                  {:type "rich_text"
                   :elements [{:type "rich_text_list"
                               :style "bullet"
                               :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})
   :list-item (fn [ctx {:keys [content] :as node}]
                {:type "rich_text_section"
                 :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))})

   ;; code
   :monospace (fn [ctx {:keys [text content] :as node}]
                (cond
                  text {:type "mrkdwn"
                        :text text}
                  content (if (contains? ctx ::parent)
                            (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                                 flatten
                                 (map #(assoc-in % [:style :code] true)))
                            {:type "section"
                             :elements [{:type "mrkdwn"
                                         :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))

   :code (fn [ctx {:keys [text content] :as node}]
           (cond
             text {:type "section"
                   :text {:type "mrkdwn"
                          :text text}}
             content (if (contains? ctx ::parent)
                       (do #nu/tapd (::parent ctx)
                           (->> (keep (partial ->slack (assoc ctx ::parent node)) content)
                                flatten
                                (map #(assoc-in % [:style :code] true))))
                       {:type "section"
                        :elements [{:type "mrkdwn"
                                    :elements (flatten (keep (partial ->slack (assoc ctx ::parent node)) content))}]})))})


(defn ->slack
  [ctx {:as node t :type}]
  (let [{:as node :keys [type]} (cond-> node (= :doc t) md.transform/hydrate-toc)]
    (if-some [f (md.transform/guard fn? (get ctx type))]
      (f ctx node)
      {::error {:msg (str "Unknown type: '" type "'.")
                :code (pr-str node)}})))

(println "--- slack tree")
(clojure.pprint/pprint
 (->slack slack-renderers data))

(println "--- json")
(println
 (json/generate-string
  (->slack slack-renderers data)))
