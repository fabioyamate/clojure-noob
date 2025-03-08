(ns clojure-noob.struct-transform-zipper
  (:require [clojure.zip :as z]
            [clojure.zip :as zip]))

(def data [1 2 3 4])



(def zp
  (z/zipper vector? ;; branch?
            seq     ;; children
            (fn [_node children] ;; make node
              children)
            data)) ;; root

(-> zp
    z/down ;; goes to first element in the current sequence
    (z/replace 10) ;; replace the value with new value
    z/root) ;; go back to the root (the value)

(-> zp
    z/down ;; first element
    z/right ;; go to its right sibling
    (z/edit inc)
    z/root)

(-> zp
    z/next ;; first element in the structure, 1
    z/next ;; next element in the structure, 2
    )


(def data2 [[1] [2] 3 4])

(def zp2
  (z/zipper vector?
            seq
            (fn [_ c]
              c)
            data2))

(-> zp2
    z/next ;; traverses the struct depth first. [1]
    z/next  ;; 1
    z/next) ;; [2]

(-> zp2
    z/next
    z/children) ;; returns (1) since it is the only children

;; Adds zip support for maps.
;; (Source: http://stackoverflow.com/a/15020649/42188)
(defn map-zipper [m]
  (z/zipper
   (fn [x] (or (vector? x)
               (map? x)
               (map? (nth x 1))))
   (fn [x] (seq (if (map? x)
                  x
                  (nth x 1))))
   (fn [x children]
     (if (map? x)
       (into {} children)
       (assoc x 1 (into {} children))))
   m))

(def messages [{:role "user"
                :content [{:type "text"
                           :text "hello"}]}])

(def zpm (z/zipper
          vector?
          seq
          (fn [_ c] c)
          messages))

(-> zpm
    z/down
    )

(-> (z/seq-zip (seq messages))
    z/down
    z/down
    )

(defn print-tree [original]
  (loop [loc (zip/seq-zip (seq original))]
    (if (zip/end? loc)
      (zip/root loc)
      (recur (zip/next
              (do (println (zip/node loc))
                  loc))))))

(print-tree messages)

(-> (z/xml-zip messages)
    z/down)
