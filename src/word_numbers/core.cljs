(ns word-numbers.core
  (:require [clojure.string :as str]))

(def ^:const numerals
  (zipmap (drop 1 (range))
          ["one" "two" "three" "four" "five" "six" "seven" "eight" "nine" "ten" "eleven" "twelve" "thirteen" "fourteen" "fifteen" "sixteen" "seventeen" "eighteen" "nineteen" "twenty"]))

(def ^:const tens
  (zipmap (drop 1 (range))
          ["ten" "twenty" "thirty" "fourty" "fifty" "sixty" "seventy" "eighty" "ninety"]))

(defn- split-num
  "Split n on quotient & remainder in relation to magnitude"
  [magnitude n]
  [(quot n magnitude) (rem n magnitude)])

(defn- round-number?
  "Does n have a zero remainder for magnitude"
  [magnitude n]
  (-> n
      (rem magnitude)
      (zero?)))

(defn n->str
  "Convert an integer [0..1000] to English words, nil otherwise"
  [n]
  (when (and (integer? n)
             (<= 0 n 1000))
    (cond
      (zero? n) "zero"
      (= n 1000) "one thousand"
      (<= 1 n 20) (numerals n)
      (and
        (< n 100)
        (round-number? 10 n)) (let [[ten _] (split-num 10 n)]
                                (tens ten))
      (<= 21 n 99) (let [[ten unit] (split-num 10 n)]
                     (str (tens ten) \- (numerals unit)))
      (round-number? 100 n) (let [[hundreds _] (split-num 100 n)]
                              (str/join \space [(numerals hundreds) "hundred"]))
      (< n 1000) (let [[hundreds lower] (split-num 100 n)]
                   (str/join \space [(numerals hundreds) "hundred" "and" (n->str lower)])))))
