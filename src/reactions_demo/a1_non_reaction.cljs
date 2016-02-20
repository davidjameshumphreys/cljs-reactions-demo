(ns reactions-demo.a1-non-reaction
  (:require
    [reagent.core :as rc]
    [devcards.core :as dc]
    [reactions-demo.helpers :refer [slider update-chan track-update counter reset-counter nav-links]])
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defn non-reaction-component
  [value]
  (rc/create-class
    {:reagent-render
     (fn [value]
       ;; look out for this message ☟ in the console
       (println "re-rendering the component.")
       (go (>! update-chan :render))
       [:div {:style {:background-color (if (< 50 (-> @value :val))
                                          "red"
                                          "green")}}
        (str "Static text")])
     :component-did-update
     ;; and this one too ☟
     (track-update "Static-text component updated")}))

(defcard
  (rc/as-element [nav-links]))

(defcard-doc
  "## The difference between `ratom`s and `reaction`s.\n\n

  Accessing `ratom`s directly works well, but it just doesn't scale.

  We access the `ratom` directly. Each time it changes, we will re-render."
  (dc/mkdn-pprint-source non-reaction-component))

(defcard
  "Notice how many times `non-reaction-component` is re-rendered even
   though the component does not need to update all that frequently.

   What happens when that component has many sub-components?"
  (fn [value _]
    (rc/as-element [:div
                    [slider value 0 100]
                    [non-reaction-component value]]))
  (rc/atom {:val 23})
  {:inspect-data true
   :history      true})

(defcard updates
         counter
         {:inspect-data true})

(defcard
  (fn [_ _]
    (rc/as-element [reset-counter]))
  nil)
