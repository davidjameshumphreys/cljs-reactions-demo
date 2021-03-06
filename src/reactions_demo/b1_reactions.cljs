(ns reactions-demo.b1-reactions
  (:require
    [reagent.core :as rc]
    [reagent.ratom :as rr]
    [devcards.core :as dc]
    [cljs.core.async :refer [<! chan >!]]
    [reactions-demo.helpers :refer [track-update update-chan slider counter reset-counter nav-links]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defn t-reaction [value]
  (reagent.ratom/reaction
    (do
      (println "Reaction #1")
      (< 50 (-> @value :val)))))

(defn s-reaction [threshold]
  (reagent.ratom/reaction
    (do
      (println "Reaction #2")
      (if @threshold
        "red"
        "green"))))

(defcard
  (rc/as-element [nav-links]))

(defcard-doc
  "## What is a reaction?

   Think of a reaction like a read-only `ratom`. When the inputs to
   the `reaction` change, it re-calculates.

   But, if the _output_ of the `reaction` does not change, then any
   component (or other `reactions`) that depend on it will not
   re-calculate.

   ## Making a reaction

   A reaction is quite simple, refer to the ratoms you need to listen
   to.

   In this example, our output signal won't change that often. (It is
   a function so that we close over the variable `value`)"
  (dc/mkdn-pprint-source t-reaction)

  "## Reactions on reactions

   You can refer to other reactions in a reaction; just treat them
   like read-only ratoms."
  (dc/mkdn-pprint-source s-reaction)

  "## &c.
  Combine them, mix with other `ratoms` to produce signals.")

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
