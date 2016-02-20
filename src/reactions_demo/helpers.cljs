(ns reactions-demo.helpers
  (:require
   [reagent.core :as rc]
   [reagent.ratom :as rr]
   [sablono.core :as sab :include-macros true]
   [devcards.core :as dc]
   [devcards.system :refer [app-state navigate-to-path prevent-> set-current-path!]]
   [cljs.core.async :refer [<! chan >!]]
   [clojure.set :refer [map-invert]])
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

(defonce counter-default {:render 0
                          :update 0})
(defonce counter (atom counter-default))

(defn reset-counter
  []
  [:button {:on-click (fn [_]
                        (reset! counter counter-default))}
   "Reset counters"])

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

(defn nav-links
  []
  (let [nses        (-> app-state deref :cards)
        num->ns     (->> nses
                         (map (fn [[k _]] k))
                         (sort-by name)
                         (map-indexed (fn [i k] [i k]))
                         (into {}))

        ns->num     (map-invert num->ns)
        current     (-> app-state deref :current-path first)
        current-idx (get ns->num current)
        nxt         (get num->ns (inc current-idx))
        prv         (get num->ns (dec current-idx))]
    [:div
     [:div {:style {:width      "50%"
                    :display    "inline-block"
                    :text-align "left"}}
      (when prv
        [:a {:href     "#"
             :on-click (prevent-> (fn [e]
                                    (set-current-path! app-state [prv])))
             :title (name prv)}
         "Prev"])]
     [:div {:style {:width      "50%"
                    :display    "inline-block"
                    :text-align "right"}}
      (when nxt
        [:a {:href     "#"
             :on-click (prevent-> (fn [e]
                                    (set-current-path! app-state [nxt])))
             :title (name nxt)}
         "Next"])]]))
