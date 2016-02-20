(ns reactions-demo.b1-reactions
  (:require
    [reagent.core :as rc]
    [reagent.ratom :as rr]
    [devcards.core :as dc]
    [cljs.core.async :refer [<! chan >!]]
    [reactions-demo.helpers :refer [track-update update-chan slider counter]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defn t-reaction
  ""
  [value]
  (reagent.ratom/reaction
    (do
      (println "Reaction #1")
      (< 50 (-> @value :val)))))

(defn s-reaction
  ""
  [threshold]
  (reagent.ratom/reaction
    (do
      (println "Reaction #2")
      (if @threshold
        "red"
        "green"))))

(defcard-doc
  "## What is a reaction?

   Think of a reaction like a read-only `ratom`. When the inputs to
   the `reaction` change, it re-calculates.

   But, if the _output_ of the `reaction` does not change, then any
   component (or other `reactions`) that depend on it will not
   re-calculate.

   ## Making a reaction

   A reaction is quite simple, refer to the ratoms you need to listen to.

   In this example, our output signal won't change that often.

   (It is a function so that we close over the variable `value`)
   "
  (dc/mkdn-pprint-source t-reaction)

  "## Reactions on reactions

   You can refer to other reactions in a reaction; just treat them like read-only ratoms."
  (dc/mkdn-pprint-source s-reaction))

(defn reactive-component
  "This component will listen to the value using a reaction, it won't
  need to update so often."
  [value]
  (let [threshold (t-reaction value)
        style-value (s-reaction threshold)
        ;; This one is never called in the code.
        another (rr/reaction (do
                               (println "Reaction #3")
                               (if (< 75 (-> @value val))
                                 "blue"
                                 "green")))]
    (rc/create-class
      {:reagent-render (fn [value]
                         (go (>! update-chan :render))
                         [:div {:style {:background-color @style-value}} (str "Static text.")])
       :component-did-update
                       (track-update "Reaction component updated")})))

(defcard slider-with-reaction
         "### Using a reaction to figure out when to render."
         (fn [value _]
           (rc/as-element [:div
                           [slider value 0 100]
                           [reactive-component value]]))
         (rc/atom {:val 23})
         {:inspect-data true :history true})


(defcard updates
         counter
         {:inspect-data true})

(defcard
  (fn [_ _]
    (rc/as-element [:button {:on-click (fn [_]
                                         (reset! counter {:render 0
                                                          :update 0}))}
                    "Reset counters"]))
  nil)

(defcard-doc
  "## Code for the reactive slider.

  There's lots of extra code just because we want to trace it."
  (dc/mkdn-pprint-source reactive-component))

(defcard-doc
  "## A sad reaction :'(

   In the example above, `another` is never read in the code.  We
   never see it evaluated.")

(defcard-doc
  "[Prev](#!/reactions_demo.a2_non_reaction)")
