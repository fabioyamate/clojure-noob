(ns clojure-noob.meander-lab
  (:require [meander.epsilon :as m]
            [criterium.core :as criterium]))

;; Single Favorite Food

(defn favorite-food-info [foods-by-name user]
  (m/match {:user user
            :foods-by-name foods-by-name}
    {:user
     {:name ?name
      :favorite-food {:name ?food}}
     :foods-by-name {?food {:popularity ?popularity
                            :calories ?calories}}}
    {:name ?name
     :favorite {:food ?food
                :popularity ?popularity
                :calories ?calories}}))

(def foods-by-name
  {:nachos {:popularity :high
            :calories :lots}
   :smoothie {:popularity :high
              :calories :less}})


(favorite-food-info
 foods-by-name
 {:name :alice
  :favorite-food {:name :nachos}})

;; =>

;; {:name :alice,
;;  :favorite {:food :nachos, :popularity :high, :calories :lots}}




;; Multiple favorite foods

(defn favorite-foods-info [foods-by-name user]
  (m/search {:user user
             :foods-by-name foods-by-name}
    {:user
     {:name ?name
      :favorite-foods (m/scan {:name ?food})}
     :foods-by-name {?food {:popularity ?popularity
                            :calories ?calories}}}
    {:name ?name
     :favorite {:food ?food
                :popularity ?popularity
                :calories ?calories}}))

(favorite-foods-info
 foods-by-name
 {:name :alice
  :favorite-foods [{:name :nachos} {:name :smoothie}]})

;; =>
;; ({:name :alice,
;;   :favorite {:food :nachos, :popularity :high, :calories :lots}}
;;  {:name :alice,
;;   :favorite {:food :smoothie, :popularity :high, :calories :less}})



;; Grabbing the food

(defn grab-all-foods [user]
  (m/find user
    {:favorite-foods [{:name !foods} ...]
     :special-food !food
     :recipes [{:title !foods} ...]
     :meal-plan {:breakfast [{:food !foods} ...]
                 :lunch [{:food !foods} ...]
                 :dinner [{:food !foods} ...]}}
    !foods))

(grab-all-foods
 {:favorite-foods [{:name :food1} {:name :food2}]
  :special-food :food3
  :recipes [{:title :food4} {:title :food5} {:title :food6}]
  :meal-plan {:breakfast [{:food :food7}]
              :lunch []
              :dinner [{:food :food8}]}})

;; =>
;; [:food1 :food2 :food4 :food5 :food6 :food7 :food8]



(def skynet-widgets
  [{:basic-info {:producer-code "Cyberdyne"}
    :widgets [{:widget-code "Model-101"
               :widget-type-code "t800"}
              {:widget-code "Model-102"
               :widget-type-code "t800"}
              {:widget-code "Model-201"
               :widget-type-code "t1000"}]
    :widget-types [{:widget-type-code "t800"
                    :description "Resistance Infiltrator"}
                   {:widget-type-code "t1000"
                    :description "Mimetic polyalloy"}]}
   {:basic-info {:producer-code "ACME"}
    :widgets [{:widget-code "Dynamite"
               :widget-type-code "c40"}]
    :widget-types [{:widget-type-code "c40"
                    :description "Boom!"}]}])

(for [{:keys [widgets widget-types basic-info]} skynet-widgets
      :let [{:keys [producer-code]} basic-info
            descriptions (into {} (for [{:keys [widget-type-code description]} widget-types]
                                    [widget-type-code description]))]
      {:keys [widget-code widget-type-code]} widgets
      :let [description (get descriptions widget-type-code)]
      :when description]
  [producer-code widget-code description])

(m/search skynet-widgets
  (m/scan {:basic-info {:producer-code ?producer-code}
           :widgets (m/scan {:widget-code ?widget-code
                             :widget-type-code ?widget-type-code})
           :widget-types (m/scan {:widget-type-code ?widget-type-code
                                  :description ?description})})
  [?producer-code ?widget-code ?description])


(def person
  {:name "jimmy"
   :preferred-address
   {:address1 "123 street ave"
    :address2 "apt 2"
    :city "Townville"
    :state "IN"
    :zip "46203"}})

(defn reformat-preferred-address [person]
  (let [address (:preferred-address person)]
    {:address {:line1 (:address1 address)
               :line2 (:address2 address)}
     :city-info {:city (:city address)
                 :state (:state address)
                 :zipcode (:zip address)}}))

(defn reformat-preferred-address-m [person]
  (m/match person
    {:preferred-address
     {:address1 ?address1
      :address2 ?address2
      :city ?city
      :state ?state
      :zip ?zip}}

    {:address {:line1 ?address1
               :line2 ?address2}
     :city-info {:city ?city
                 :state ?state
                 :zipcode ?zip}}))

(criterium/bench
 (reformat-preferred-address person))

(criterium/quick-bench
 (reformat-preferred-address person))

(criterium/quick-bench
 (reformat-preferred-address person))


(let [person {:name "jimmy"
              :preferred-address
              {:address1 "123 street ave"
               :address2 "apt 2"
               :city "Townville"
               :state "IN"
               :zip "46203"}
              :other-addresses
              [{:address1 "432 street ave"
                :address2 "apt 7"
                :city "Cityvillage"
                :state "New York"
                :zip "12345"}
               {:address1 "534 street ave"
                :address2 "apt 5"
                :city "Township"
                :state "IN"
                :zip "46203"}]}
      case-1 (fn distinct-zips-and-cities [person]
               (let [preferred-address-zip (get-in person [:preferred-address :zip])
                     preferred-address-city (get-in person [:preferred-address :city])
                     other-zips (map :zip (:other-addresses person))
                     other-cities (map :city (:other-addresses person))]
                 {:zips (filter some? (distinct (cons preferred-address-zip other-zips)))
                  :cities (filter some? (distinct (cons preferred-address-city other-cities)))}))

      case-2 (fn distinct-zips-and-cities [person]
               (m/match person
                 {:preferred-address {:zip (m/or nil !zips)
                                      :city (m/or nil !cities)}
                  :other-addresses [{:zip (m/or nil !zips)
                                     :city (m/or nil !cities)} ...]}
                 {:zips (distinct !zips)
                  :cities (distinct !cities)}))]
  (criterium/quick-bench (case-1 person))
  (criterium/quick-bench (case-2 person))
  )

(m/match {:name "jimmy"
          :preferred-address
          {:address1 "123 street ave"
           :address2 "apt 2"
           :city "Townville"
           :state "IN"
           :zip "46203"}
          :other-addresses
          [{:address1 "432 street ave"
            :address2 "apt 7"
            :city "Cityvillage"
            :state "New York"
            :zip "12345"}
           {:address1 "534 street ave"
            :address2 "apt 5"
            :city "Township"
            :state "IN"
            :zip "46203"}]}
  {:preferred-address {:zip (m/or nil !zips)
                       :city (m/or nil !cities)}
   :other-addresses
   [{:zip (m/or nil !zips)
     :city (m/or nil !cities)} ...]}
  {:zips (distinct !zips)
   :cities (distinct !cities)})



(m/search {:people
           [{:name "jimmy" :id 1}
            {:name "joel" :id 2}
            {:name "tim" :id 3}]
           :addresses
           {1 [{:address1 "123 street ave"
                :address2 "apt 2"
                :city "Townville"
                :state "IN"
                :zip "46203"
                :preferred true}
               {:address1 "534 street ave",
                :address2 "apt 5",
                :city "Township",
                :state "IN",
                :zip "46203"
                :preferred false}]
            2 [{:address1 "2026 park ave"
                :address2 "apt 200"
                :city "Town"
                :state "CA"
                :zip "86753"
                :preferred true}]
            3 [{:address1 "1448 street st"
                :address2 "apt 1"
                :city "City"
                :state "WA"
                :zip "92456"
                :preferred true}]}
           :visits {1 [{:date "12-31-1900"
                        :geo-location {:zip "46203"}}]
                    2 [{:date "1-1-1970"
                        :geo-location {:zip "12345"}}
                       {:date "1-1-1970"
                        :geo-location {:zip "86753"}}]
                    3 [{:date "4-4-4444"
                        :geo-location {:zip "54221"}}
                       {:date "4-4-4444"
                        :geo-location {:zip "92456"}}]}}
  {:people (m/scan {:id ?id :name ?name})
   :addresses {?id (m/scan {:preferred true :zip ?zip})}
   :visits {?id (m/scan {:geo-location {:zip (m/and (m/not ?zip) ?bad-zip)}
                         :date ?date})}}
  {:name ?name
   :id ?id
   :zip ?bad-zip
   :date ?date})
