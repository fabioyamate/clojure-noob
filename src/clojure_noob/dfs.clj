(ns clojure-noob.dfs
  (:require [clojure.set :as set]))

(def dummy-nodes
  [{:id 1, :title "node1", :parent_id nil}
   {:id 2, :title "node2", :parent_id nil}
   {:id 3, :title "node3", :parent_id 1 :weight 2}
   {:id 4, :title "node4", :parent_id 1 :weight 1}
   {:id 5, :title "node5", :parent_id 2 :weight 1}
   {:id 6, :title "node6", :parent_id 3 :weight 1}
   {:id 7, :title "node7", :parent_id 3 :weight 2}])

(defn add-children [nodes, node]
  (let [children (filter #(= (node :id) (% :parent_id)) nodes)]
    (if (empty? children)
      node
      (assoc node
             :children (sort-by :weight (map #(add-children nodes %) children))))))

(def dummy-edges
  ;; [parent child]
  [[["node1" "node3"] 2]
   [["node1" "node4"] 1]
   [["node2" "node5"] 1]
   [["node3" "node6"] 1]
   [["node3" "node7"] 2]])

(defn ->node
  [[[p c] w]]
  {:id c :title (str p) :parent_id p :weight w})

(defn ->root
  [x]
  {:id x :title (str x) :parent_id nil})

(defn edges->nodes
  [edges]
  (let [{:keys [parents children nodes]} (reduce
                                          (fn [stats [[p c] w :as wedge]]
                                            (-> stats
                                                (update :parents conj p)
                                                (update :children conj c)
                                                (update :nodes conj (->node wedge))))
                                          {:parents #{}
                                           :children #{}
                                           :nodes []}
                                          edges)]
    (reduce (fn [xs p]
              (conj xs (->root p)))
            nodes
            (set/difference parents children))))

(edges->nodes dummy-edges)

(defn node-tree []
  (let [nodes dummy-nodes]
    (add-children nodes {:id nil})))

(comment
  (= dummy-nodes
     (edges->nodes dummy-edges))

  (clojure.pprint/pprint
   (node-tree))

  (clojure.pprint/pprint
   (add-children (edges->nodes dummy-edges)
                 {:id nil}))

  )
