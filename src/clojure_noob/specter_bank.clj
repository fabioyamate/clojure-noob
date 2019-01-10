(ns clojure-noob.specter-bank
  (:require [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]
            [criterium.core :as c]))

(def world
  {:people [{:money 129827 :name "Alice Brown"}
            {:money 100 :name "John Smith"}
            {:money 6821212339 :name "Donald Trump"}
            n            {:money 2870 :name "Charlie Johnson"}
            {:money 8273821 :name "Charlie Rose"}
            ]
   :bank {:funds 4782328748273}})

(defn user->bank [world name amt]
  (let [;; First, find out how much money that user has
        ;; to determine whether or not this is a valid transfer
        curr-funds (->> world
                        :people
                        (filter (fn [user] (= (:name user) name)))
                        first
                        :money
                        )]
    (if (< curr-funds amt)
      (throw (IllegalArgumentException. "Not enough funds!"))
      ;; If valid, then need to subtract the transfer amount from the
      ;; user and add the amount to the bank
      (-> world
          (update
           :people
           (fn [user-list]
             ;; Important to use mapv to maintain the type of the
             ;; sequence containing the list of users. This code
             ;; modifies the user matching the name and keeps
             ;; every other user in the sequence the same.
             (mapv (fn [user]
                     ;; Notice how nested this code is that manipulates the users
                     (if (= (:name user) name)
                       (update user :money #(+ % amt))
                       ;; If a user doesn't match the name during the scan,
                       ;; don't modify them
                       user
                       ))
                   user-list)))
          (update-in
           [:bank :funds]
           #(- % amt))
          ))))




(defn transfer
  "Note that this function works on *any* world structure. This handles
   arbitrary many to many transfers of a fixed amount without overdrawing anyone"
  [world from-path to-path amt]
  (let [;; Get the sequence of funds for all entities making a transfer
        givers (sm/select from-path world)

        ;; Get the sequence of funds for all entities receiving a transfer
        receivers (sm/select to-path world)

        ;; Compute total amount each receiver will be credited
        total-receive (* amt (count givers))

        ;; Compute total amount each transferrer will be deducted
        total-give (* amt (count receivers))]

    ;; Make sure every transferrer has sufficient funds
    (if (every? #(>= % total-give) givers)
      (->> world
           ;; Deduct from transferrers
           (sm/transform from-path #(- % total-give))
           ;; Credit the receivers
           (sm/transform to-path #(+ % total-receive))
           )
      (throw (IllegalArgumentException. "Not enough funds!"))
      )))

(defn pay-fee [world]
  (transfer world
            [:people s/ALL :money]
            [:bank :funds]
            1))

(defn bank-give-dollar [world]
  (transfer world
            [:bank :funds]
            [:people s/ALL :money]
            1))

(defn user [name]
  [:people
   s/ALL
   #(= (:name %) name)])

(defn transfer-users [world from to amt]
  (transfer world
            [(user from) :money]
            [(user to) :money]
            amt))



(defn user->bank* [world from amt]
  (transfer world
            [(user from) :money]
            [:bank :funds]
            amt))

(defn bank-loyal-bonus [world]
  (transfer world
            [:bank :funds]
            [:people (s/srange 0 3) s/ALL :money]
            5000))

(comment
  (-> world
      (user->bank "John Smith" 10)
      (user->bank "Alice Brown" -5000))
  (-> world
      pay-fee
      bank-give-dollar)
  (transfer-users world "Alice Brown" "John Smith" 100000)
  (user->bank world "John Smith" 50)
  (c/bench (bank-loyal-bonus world)))
