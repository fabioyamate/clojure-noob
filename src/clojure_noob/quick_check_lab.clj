(ns clojure-noob.quick-check-lab
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(comment
  (def sort-idempotent-prop
    (prop/for-all [v (gen/vector gen/int)]
      (prn v)
      (= (sort v) (sort (sort v)))))

  (def prop-sorted-first-less-than-last
    (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
      (let [s (sort v)]
        (< (first s) (last s)))))

  (tc/quick-check 100 sort-idempotent-prop)
  (tc/quick-check 100 prop-sorted-first-less-than-last)
  ;; => {:result true, :num-tests 100, :seed 1382488326530}

  (defspec prop-all-numbers-are-even
    100
    (prop/for-all [v (gen/int)]
      (even? v)))

  (defn ascending?
    "clojure.core/sorted? doesn't do what we might expect, so we write our
  own function"
    [coll]
    (every? (fn [[a b]] (<= a b))
            (partition 2 1 coll)))

  (def prop-sorted-collection-is-ascending
    (prop/for-all [v (gen/vector gen/int)]
      (let [s (sort v)]
        (and (= (count v) (count s))
             (ascending? s)))))

  (tc/quick-check 100 prop-sorted-collection-is-ascending)

  (gen/sample gen/int)
  (gen/sample gen/int 10)

  (take 10 (gen/sample-seq gen/int))

  (gen/sample (gen/vector gen/nat))

  (gen/sample (gen/list gen/boolean))

  (gen/sample (gen/map gen/keyword gen/boolean) 5)

  (gen/sample gen/string)

  (gen/sample (gen/tuple gen/nat gen/boolean gen/ratio))


  (gen/sample (gen/map gen/keyword
                       (gen/fmap set
                                 (gen/vector (gen/map gen/string
                                                      (gen/such-that not-empty
                                                                     (gen/vector gen/nat))))))
              4)

  (-> (->> (gen/vector gen/nat)
           (gen/such-that not-empty)
           (gen/map gen/string)
           (gen/vector)
           (gen/fmap set)
           (gen/map gen/keyword))
      (gen/sample 4))

  (gen/sample (gen/such-that
               (every-pred not-empty
                           #(= (count %) (count (distinct %))))
               (gen/vector (gen/elements ["a" "b" "c"]))))

  (gen/sample (gen/fmap #(distinct %) (gen/vector gen/int 10)))

  (def domain (gen/elements ["gmail.com" "yahoo.com" "hotmail.com"]))

  (def gen-email (gen/fmap #(clojure.string/join "@" %)
                           (gen/tuple gen/string-alphanumeric domain)))

  (gen/sample gen-email)

  (gen/sample
   (gen/fmap (partial zipmap [:int :nat :string :string-alphanumeric :boolean :uuid :vec-int :set-int])
             (gen/tuple gen/int
                        gen/nat
                        (gen/such-that not-empty gen/string)
                        (gen/such-that not-empty gen/string-alphanumeric)
                        gen/boolean
                        gen/uuid
                        (gen/such-that not-empty (gen/vector gen/int))
                        (gen/such-that not-empty (gen/set gen/int)))) 1)

  ;; can be rewritten

  (gen/sample
   (gen/let [a gen/int]
     {:a a}))

  (gen/sample gen/s-pos-int) ;; excludes zero
  (gen/sample gen/s-neg-int) ;; excludes zero

  gen/pos-int

  (gen/sample (gen/not-empty (gen/vector gen/s-pos-int)))

  (gen/sample  (gen/return 3)) ;; => (3 3 3 3 3 3 3 3 3)

  (def definition-data
    {:min 100 :max 500})

  (gen/sample (gen/choose 100 500) 10)

  (gen/sample gen/char 10)
  (gen/sample gen/boolean 10)
  (gen/sample gen/char-ascii 10)
  (gen/sample gen/keyword)
  (gen/sample gen/large-integer)
  (gen/sample gen/keyword-ns)
  (gen/sample gen/neg-int)
  (gen/sample gen/pos-int)
  #_(gen/sample (gen/sample-seq gen/int 10))
  (gen/sample (gen/frequency [[5 gen/int] [3 (gen/vector gen/int)] [2 gen/boolean]]))
  (gen/sample (gen/hash-map :a gen/boolean :b gen/nat))

  (gen/sample
   (gen/let [users (gen/list (gen/hash-map :name gen/string-ascii
                                           :age gen/nat))]
     (->> users
          (map #(assoc %2 :id %1) (range))
          (gen/shuffle))) 10)

  (def prop-sorted-first-less-than-last
    (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
      (let [s (sort v)]
        (< (first s) (last s)))))

  (tc/quick-check 100 prop-sorted-first-less-than-last)

  (def gen-key gen/string)
  (def gen-value gen/string)
  (def gen-clear (gen/return [:clear!]))
  (def gen-size (gen/return [:size]))
  (def gen-store (gen/tuple (gen/return :store!) gen-key gen-value))
  (def gen-delete (gen/tuple (gen/return :delete!) gen-key))
  (def gen-fetch (gen/tuple (gen/return :fetch) gen-key))
  (def gen-ops (gen/vector (gen/one-of [gen-clear gen-store gen-delete gen-fetch gen-size])))

  (gen/sample gen-ops)

  (defn hm-run [db ops]
    (reduce
     (fn [hm [op k v]]
       (case op
         :clear! {}
         :size hm
         :store! (assoc hm k v)
         :delete! (dissoc hm k)
         :fetch hm))
     db ops))

  (defspec hash-map-equiv 100
    (prop/for-all [ops gen-ops]
      (let [hm (hm-run {} ops)])))
  )
