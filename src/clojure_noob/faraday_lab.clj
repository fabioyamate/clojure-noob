(ns clojure-noob.faraday-lab
  (:require [taoensso.faraday :as far]))

(def client-opts
  {:access-key "<AWS_DYNAMODB_ACCESS_KEY>"
   :secret-key "<AWS_DYNAMODB_SECRET_KEY>"
   :endpoint "http://localhost:8000"})

(def table-name :ss5)

(far/list-tables client-opts)

(far/create-table client-opts
                  table-name
                  [:customer-id :s]  ; Primary key named "id", (:n => number type)
                  {:range-keydef [:request-id :s]
                   :lsindexes [{:name "another-table-timestamp"
                                :range-keydef [:timestamp :n]}]
                   :gsindexes [{:name        "request-id-index"
                                :hash-keydef [:request-id :s]
                                :throughput  {:read 2 :write 3}
                                :projection  :all}]
                   :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                   :block? true ; Block thread during table creation
                   })

(far/put-item client-opts
              table-name
              {:customer-id "client-d1"
               :request-id "request-id1"
               :timestamp 12345
               :random-value "djksldjsd"})

(far/put-item client-opts
              table-name
              {:customer-id "client-d2"
               :request-id "request-id3"
               :timestamp 12346
               :random-value "djksldjsd"})

(far/get-item client-opts
              table-name
              {:customer-id "client-d1" :request-id "request-id1"})

(far/query client-opts table-name {:customer-id [:eq "client-d1"]})

(far/query client-opts table-name {:request-id [:eq "request-id3"]} {:index "request-id-index"})

(far/query client-opts table-name {:request-id [:eq "request-id3"]} {:index "request-id-index"})

(def gamescore-table
  :game-scores-temp)

(far/create-table client-opts
                  gamescore-table
                  [:user-id :s]  ; Primary key named "id", (:n => number type)
                  {:range-keydef [:game-title :s]
                   :throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
                   :block? true ; Block thread during table creation
                   })

(far/put-item client-opts gamescore-table {:user-id "101" :game-title "Galaxy Invaders" :top-score 5842 :top-score-datetime "2015-09-15:17:24:31" :wins 21 :loses 72})
(far/put-item client-opts gamescore-table {:user-id "101" :game-title "Meteor Blasters" :top-score 1000 :top-score-datetime "2015-10-22:23:18:01" :wins 12 :loses 3})
(far/put-item client-opts gamescore-table {:user-id "101" :game-title "Starship X" :top-score 24 :top-score-datetime "2015-08-31:13:14:21" :wins 4 :loses 9})

(far/put-item client-opts gamescore-table {:user-id "102" :game-title "Alien Adventure" :top-score 192 :top-score-datetime "2015-07-12:11:07:56" :wins 32 :loses 192})
(far/put-item client-opts gamescore-table {:user-id "102" :game-title "Galaxy Invaders" :top-score 0 :top-score-datetime "2015-09-18:07:33:42" :wins 0 :loses 5})

(far/put-item client-opts gamescore-table {:user-id "104" :game-title "Attack Ships" :top-score 3 :top-score-datetime "2015-09-15:17:24:31" :wins 1 :loses 8})
(far/put-item client-opts gamescore-table {:user-id "104" :game-title "Galaxy Invaders" :top-score 2317 :top-score-datetime "2015-09-15:17:24:31" :wins 40 :loses 3})
(far/put-item client-opts gamescore-table {:user-id "104" :game-title "Meteor Blasters" :top-score 723 :top-score-datetime "2015-10-22:23:18:01" :wins 22 :loses 12})
(far/put-item client-opts gamescore-table {:user-id "104" :game-title "Starship X" :top-score 42 :top-score-datetime "2015-08-31:13:14:21" :wins 4 :loses 19})
