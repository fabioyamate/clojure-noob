(ns clojure-noob.aoe.circuit
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(def varname-gen
  (gen/fmap (fn [chars]
              (symbol
               (str/lower-case
                (apply str chars))))
            (gen/vector gen/char-alpha
                        1
                        2)))

(s/def ::varname
  (s/with-gen symbol?
    (fn [] varname-gen)))

(s/def ::val (s/alt :name ::varname
                    :value nat-int?))


(s/def ::rhs ::varname)

(s/def ::lhs (s/alt :simple-value ::val
                    :binary-expression
                    ::binary-expression
                    :not ::not))

(s/def ::not (s/cat :not #{'NOT} :operand ::val))


(s/def ::expr (s/cat :lhs ::lhs :arrow #{'->} :rhs ::rhs))

(s/def ::binary-expression (s/cat
                            :left-operand ::val
                            :operator ::binary-operator
                            :right-operand ::val))

(s/def ::binary-operator #{'LSHIFT 'RSHIFT 'AND 'OR})

(gen/sample (s/gen ::expr))

(edn/read-string (format "[%s]" "a -> b"))

(s/conform ::expr (quote [a -> b]))

(def context (atom {}))

(defn value-by-symbol [sym]
  @(get @context sym))

(defn evaluate* [[kind tree-or-val]]
  (case kind
    :value tree-or-val
    :name (value-by-symbol tree-or-val)
    :simple-value (evaluate* tree-or-val)
    :not (bit-not
          (evaluate*
           (:operand tree-or-val)))
    :binary-expression
    (let [l (evaluate* (:left-operand tree-or-val))
          r (evaluate* (:right-operand tree-or-val))
          operator (case (:operator tree-or-val)
                     AND bit-and
                     OR bit-or
                     LSHIFT bit-shift-left
                     RSHIFT bit-shift-right)]
      (operator l r))))

(defn evaluate-expr! [expr]
  (let [rhs (:rhs expr)
        lhs (:lhs expr)]
    (swap! context assoc rhs
           (delay
            (evaluate* lhs)))))

(comment
  (reset! context {})
  (def exprs
    (mapv (partial s/conform ::expr)
          [(quote [123 -> x])
           (quote [456 -> y])
           (quote [x AND y -> d])
           (quote [x OR y -> e])
           (quote [x LSHIFT 2 -> f])
           (quote [y RSHIFT 2 -> g])
           (quote [NOT x -> h])
           (quote [NOT y -> i])]))
  (for [expr exprs] (evaluate-expr! expr))
  (value-by-symbol 'f)

  (into {} @context))
