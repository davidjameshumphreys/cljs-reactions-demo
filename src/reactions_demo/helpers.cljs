(ns reactions-demo.helpers
  (:require
   [reagent.core :as rc]
   [reagent.ratom :as rr]
   [sablono.core :as sab :include-macros true]
   [devcards.core :as dc]
   [cljs.core.async :refer [<! chan >!]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)

(defn slider [value min max]
  (fn [value min max]
    [:div
     [:input {:type "range" :value (-> @value :val) :min min :max max
              :style {:width "100%"}
              :on-change (fn [e]
                           (swap! value assoc :val (.-target.value e)))}]
     [:span (str "Value:" (-> @value :val))]]))

(defonce counter (atom {:render 0
                    :update 0}))

(defonce update-chan (chan))

(go-loop []
  (let [k (<! update-chan)]
    (swap! counter update-in [k] inc))
  (recur))

(defn- track-update
  "Helper fn just to track when the component updates."
  [string]
  (fn [_ old-args]
    (go (>! update-chan :update))
    (println string)))
