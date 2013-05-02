#Fibonacci - example
(defn fib [x]
  (cond (<= x 0) 0
        (== x 1) 1
        :else (+ (fib (- x 1)) (fib (- x 2)))
        )
)

(for [i (range 15)] (fib i))

