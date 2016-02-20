(ns reactions-demo.a2-non-reaction
  (:require
    [reagent.core :as rc]
    [devcards.core :as dc]
    [cljs.core.async :refer [<! chan >!]]
    [reactions-demo.helpers :refer [slider update-chan track-update counter]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defn non-reaction-component
  [value children]
  (rc/create-class
    {:reagent-render
     (fn [value children]
       ;; look out for this message ☟ in the console
       (println "re-rendering the component.")
       (go (>! update-chan :render))
       [:div {:style {:background-color (if (< 50 (-> @value :val))
                                          "red"
                                          "green")}}
        (str "Static text")
        (when children
          children)])
     :component-did-update
     ;; and this one too ☟
     (track-update "Static-text component updated")}))

(defn child
  [value]
  (rc/create-class
    {:reagent-render       (fn [value]
                             (go (>! update-chan :render))  ;; track renders
                             [:span {:style {:background-color (if (< 50 (-> @value :val))
                                                                 "red"
                                                                 "green")}} ""])
     :component-did-update (track-update "child") ;; and track updates
     }))

(defn lots-of-children
  [value]
  [:div
   [non-reaction-component value (for [c (range 1 100)]
                                   ;; each child does the same calculation :-/
                                   ^{:key (str "child-" c)}
                                   [child value])]])

(defcard loads-of-children
         "## Render storm.

          So when there are lots of children, we end up with a massive
         render-storm!"
         (fn [value _]
           (rc/as-element [:div
                           [slider value 0 100]
                           [lots-of-children value]]))
         (rc/atom {:val 23}))

(defcard
  "We have many events happening!"
  counter
  {:history false
   :frame false})

(defcard-doc
  (dc/mkdn-pprint-source child)
  (dc/mkdn-pprint-source lots-of-children))

(defcard
  (fn [_ _]
    (rc/as-element [:button {:on-click (fn [_]
                                         (reset! counter {:render 0
                                                          :update 0}))}
                    "Reset counters"]))
  nil)

(defcard-doc
  "[Prev](#!/reactions_demo.a1_non_reaction) [Next](#!/reactions_demo.b1_reactions)")