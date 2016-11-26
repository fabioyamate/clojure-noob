(ns clojure-noob.parsec.core)

;; type parser = String -> [(Tree, String)]
;; type parser a = String -> [(a, String)]

(defn result [v]
  (fn [inp]
    [[v, inp]]))

(defn zero [inp]
  [])

(defn item [inp]
  (if (empty? inp)
    []
    [[(first inp), (rest inp)]]))

(defn seq [p q]
  (fn [inp]
    (for [[v, inp'] (p inp)
          [w, inp''] (q inp')]
      [[v, w] inp''])))

(defn bind [p f]
  (fn [inp]
    (apply concat (for [[v inp'] (p inp)]
                    ((f v) inp')))))

(defn sat [p]
  (bind item (fn [x]
               (if (p x)
                 (result x)
                 zero))))

(defn char [x]
  (sat (fn [y]
         (= x y))))

(def digit (sat (fn [x]
                  (Character/isDigit x))))

(def lower (sat (fn [x]
                  (Character/isLowerCase x))))

(def upper (sat (fn [x]
                  (Character/isUpperCase x))))

(defn plus [p q]
  (fn [inp]
    (concat (p inp) (q inp))))

((bind lower (fn [x]
              (bind lower (fn [y]
                            (result [x,y]))))) "abc")

(def letter (plus lower upper))

(def alphanum (plus letter digit))

(def word
  (let [neWord (bind letter (fn [x]
                              (bind word (fn [xs]
                                           (result (cons x xs))))))]
    (plus neWord (result ""))))

(word "ab")
