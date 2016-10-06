(ns clojure-noob.specter-event
  (:require [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]
            [criterium.core :as c]))

(def world
  {:events (mapv (fn [x] (assoc x :event/amount (* (:event/points x) (:event/fair-value x))))
                 [{:event/points 10M
                   :event/fair-value 0.009M
                   :event/source :a}
                  {:event/points 50M
                   :event/fair-value 0.009M
                   :event/source :a}
                  {:event/points -20M
                   :event/fair-value 0.009M
                   :event/source :a}
                  {:event/points 200M
                   :event/fair-value 0.009M
                   :event/source :a}])
   :accounting {:accounting/points 0M}})

(defn minimal [amount data]
  (first (filter #(>= % amount) (reductions + data))))

(defn add-event [accounting event]
  (-> accounting
      (update :accounting/amount + (:event/amount event))
      (update :accounting/points + (:event/points event))))

(->> (reductions add-event
                 {:accounting/points -100M
                  :accounting/amount 0M}
                 (:events world))
     (remove #(neg? (:accounting/points %)))
     first)

(def points [:events s/ALL :event/points])

(defn accounting [world desired-points]
  (let [points (sm/select points world)

        discounted-amount (minimal desired-points points)]

    (if discounted-amount
      (->> world
           (sm/setval [:accounting :accounting/points] discounted-amount))
      (throw (IllegalArgumentException. "Not enough balance")))))



(accounting world 100M)
