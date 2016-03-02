(ns clojure-noob.markov)

;; type Word = String
;; type FollowedWord = Word
;; markov-data :: String -> Map Word [FollowedWord]
(defn markov-data-original [text]
  (let [maps
        (for [line (clojure.string/split text #"\.")
              m (let [l (str line ".")
                      words
                      (cons :start (clojure.string/split l #"\s+"))]
                  (for [p (partition 2 1 (remove #(= "" %) words))]
                    {(first p) [(second p)]}))]
          m)]
    (apply merge-with concat maps)))

;; sentendce :: Map Word [FollowedWord] -> String
(defn sentence-original [data]
  (loop [ws (data :start)
         acc []]
    (let [w (rand-nth ws)
          nws (data w)
          nacc (concat acc [w])]
      (if (= \. (last w))
        (clojure.string/join " " nacc)
        (recur nws nacc)))))

;; refactored

;; sentence->wordlist :: String -> [Word]
(defn sentence->wordlist [sentence]
  (-> sentence
      (str ".")
      (clojure.string/split #"\s+")
      (->> (cons :start)
           (remove #(= "" %)))))

(comment
  (sentence->wordlist "This is a sentence")
  )

;; make-markov-mapping :: String -> Map Word [Word]
(defn make-markov-mapping [sentence]
  (let [wordlist (sentence->wordlist sentence)]
    (for [[word & words] (partition 2 1 wordlist)]
      {word words})))

(comment
  (make-markov-mapping "This is a sentence")
  )

(defn markov-data [text]
  (->> (clojure.string/split text #"\.")
       (mapcat make-markov-mapping)
       (apply merge-with concat)))

(defn pick-next-word [mapping this-word]
  (let [choices (get mapping this-word)]
    (rand-nth choices)))

(defn sentence [mapping]
  (loop [words []
         this-word :start]
    (let [next-word (pick-next-word mapping this-word)
          words (conj words next-word)]
      (if (= (last next-word) \.)
        (clojure.string/join " " words)
        (recur words next-word)))))

(comment
  (def text
    "Returns a lazy sequence of lists of n items each, at offsets step
  apart. If step is not supplied, defaults to n, i.e. the partitions
  do not overlap. If a pad collection is supplied, use its elements as
  necessary to complete last partition upto n items. In case there are
  not enough padding elements, return a partition with less than n items")

  (-> (markov-data-original text)
      sentence-original)

  (-> (markov-data text)
      sentence)
  )
