(ns clojure-noob.honeysql-lab
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(def sqlmap {:select [:a :b :c]
             :from   [:foo]
             :where  [:= :foo.a "baz"]})

(sql/format sqlmap)
;; => ["SELECT a, b, c FROM foo WHERE foo.a = ?" "baz"]


(h/select :*)

(h/from (h/select :*) :users)
