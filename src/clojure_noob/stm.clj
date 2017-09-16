(ns clojure-noob.stm
  (:import java.util.concurrent.Executors))

(defn run [nvecs nitems nthreads niters]
  (let [vec-refs (vec (map (comp ref vec)
                           (partition nitems (range (* nvecs nitems)))))
        swap #(let [v1 (rand-int nvecs)
                    v2 (rand-int nvecs)
                    i1 (rand-int nitems)
                    i2 (rand-int nitems)]
                (dosync
                 (let [temp (nth @(vec-refs v1) i1)]
                   (alter (vec-refs v1) assoc i1 (nth @(vec-refs v2) i2))
                   (alter (vec-refs v2) assoc i2 temp))))
        report #(do
                  (prn (map deref vec-refs))
                  (println "Distinct:"
                           (count (distinct (apply concat (map deref vec-refs))))))]
    (report)
    (dorun (apply pcalls (repeat nthreads #(dotimes [_ niters] (swap)))))
    (report)))

(time
 (run 100 10 10 100000))

(def state (ref {:a 0}))

(defn plus2 [x]
  (+ x 2))

(time
 (let [t1 (future
            (dosync
             #_(ensure state)
             (Thread/sleep 1000)
             (alter state update :a (fnil plus2 0))))
       t2 (future
            (dosync
             (Thread/sleep 500)
             (alter state update :a (fnil plus2 0))))]
   [@t1 @t2]))

(def thread-pool
  (Executors/newFixedThreadPool
   (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(defn dothreads!
  [f & {thread-count :threads
        exec-count :times
        :or {thread-count 1 exec-count 1}}]
  (dotimes [t thread-count]
    (.submit thread-pool
             #(dotimes [_ exec-count] (f)))))

(dothreads! #(.print System/out "Hi ") :threads 2 :times 2)

(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])

(defn board-map [f board]
  (vec
   (map #(vec (for [s %]
                (f s)))
        board)))

(defn reset-board!
  "Resets the board state. Generally these types of functions are a
  bad idea, but matters of page count for our hand."
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

(defn neighbors
  ([size yx]
   (neighbors [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
   (filter
    (fn [new-yx]
      (every? #(< -1 % size)
              new-yx))
    (map #(vec (map + yx %)) deltas))))

(def king-moves
  ;; ignores [0 0] because king needs to move
  (partial neighbors [[-1 -1]
                      [-1 0]
                      [-1 1]
                      [0 -1]
                      [0 1]
                      [1 -1]
                      [1 0]
                      [1 1]] 3))

(defn good-move? ;; if no collision, good to go
  [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  "Randomly choose a legal move"
  [[[mover mpos] [_ enemy-pos]]]
  ;; try a move and if it is good, use it
  ;; but always try a random move
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos)))])

(reset-board!)

(take 5 (repeatedly #(choose-move @to-move)))

(defn place [from to] to)

(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece)
  (alter (get-in board src) place :-)
  (alter num-moves inc))

(defn update-to-move
  ;; swaps the move to the next king
  [move]
  (alter to-move #(vector (second %) move)))

(defn make-move!
  []
  (dosync
   (let [move (choose-move @to-move)]
     (move-piece move @to-move)
     (update-to-move move))))

(reset-board!)
(make-move!)
(board-map deref board)

(time
 (dothreads! make-move! :threads 100000 :times 1000000000000000))

@to-move
@num-moves
