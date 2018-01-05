(ns clojure-noob.specs.game-of-cards
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

(def suit? #{:club
             :diamond
             :heart
             :spade})

(def rank? (into #{:jack :queen :king :ace} (range 2 11)))

(def deck (for [suit suit? rank rank?] [rank suit]))

(s/def ::card (s/tuple rank? suit?))

(s/def ::hand (s/* ::card))

(s/def ::name string?)
(s/def ::score int?)
(s/def ::player (s/keys :req [::name ::score ::hand]))

(s/def ::players (s/* ::player))
(s/def ::deck (s/* ::card))

(s/def ::game (s/keys :req [::players ::deck]))

(def kenny
  {::name "Kenny Rogers"
   ::score 100
   ::hand []})

(s/valid? ::player kenny)

(s/explain ::game
           {::deck deck
            ::players [(assoc kenny ::hand [[2 :banana]])]})

(defn total-cards [{:keys [::deck ::players] :as game}]
  (apply + (count deck)
         (map #(-> % ::hand count) players)))

(defn deal [game] ,,,)

(s/fdef deal
        :args (s/cat :game ::game)
        :ret ::game
        :fn #(= (total-cards (-> % :args :game))
                (total-cards (-> % :ret))))

(gen/sample (s/gen ::card) 5)


(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))

(gen/generate (s/gen ::player))

(s/exercise (s/cat :k keyword? :ns (s/+ number?)))

(s/exercise-fn `deal)

(gen/sample (s/gen (s/and int? pos? even?)) 10)

(stest/instrument `deal)

(deal 1)
(stest/check `deal)
