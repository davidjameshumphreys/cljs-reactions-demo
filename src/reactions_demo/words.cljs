(ns reactions-demo.words
  (:require [reactions-demo.helpers :refer [slider nav-links]]
            [reagent.core :as rc]
            [word-numbers.core :as word-numbers])
  (:require-macros
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defcard
  (rc/as-element [nav-links]))

(defcard-doc "## Words to numbers

First we have some static values"
             (dc/mkdn-pprint-source word-numbers/numerals)
             (dc/mkdn-pprint-source word-numbers/tens)

             "then some helper functions:"

             (dc/mkdn-pprint-source word-numbers/split-num)
             (dc/mkdn-pprint-source word-numbers/round-number?)

             "The main code"

             (dc/mkdn-pprint-source word-numbers/n->str))

(defn enumerate-numbers
  []
  [:table {:style {:border-collapse "collapse"}}
   [:thead [:tr [:th "#"] [:th "In English"]]]
   (for [i (range 0 1001)
         :let [s (word-numbers/n->str i)]]
     ^{:key s}
     [:tr
      [:td {:style {:text-align   "right"
                    :border-right "solid 1px black"}}
       i]
      [:td {:style {:padding-left "1em"}}
       s]])])


(defcard
  (rc/as-element [enumerate-numbers]))