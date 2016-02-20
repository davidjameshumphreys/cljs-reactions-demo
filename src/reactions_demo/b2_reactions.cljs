(ns reactions-demo.b2-reactions
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

(defcard
  (rc/as-element [nav-links]))

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
    (rc/as-element [reset-counter]))
  nil)
