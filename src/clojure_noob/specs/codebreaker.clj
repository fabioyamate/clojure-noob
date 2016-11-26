(ns clojure-noob.specs.codebreaker
  (:require [clojure.spec :as s]
            [clojure.spec.test :as stest]))

(def peg? #{:y :g :r :c :w :b})
(s/def ::code (s/coll-of peg? :min-count 4 :max-count 6))

(s/def ::exact-matches nat-int?)
(s/def ::loose-matches nat-int?)

(s/fdef score
        :args (s/and (s/cat :secret ::code :guess ::code)
                     (fn [{:keys [secret guess]}]
                       (= (count secret) (count guess))))
        :ret (s/keys :req [::exact-matches ::loose-matches])
        :fn (fn [{{secret :secret} :args ret :ret}]
              (<= 0 (apply + (vals ret)) (count secret))))

(s/exercise (:ret (s/get-spec `score)))

(defn score [secret guess]
  {::exact-matches 4
   ::loose-matches 3})

(s/exercise-fn `score)

(stest/check `score)
