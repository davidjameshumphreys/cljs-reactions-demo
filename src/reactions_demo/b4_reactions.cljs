(ns reactions-demo.b4-reactions
  (:require
    [reagent.core :as rc]
    [reagent.ratom :as rr]
    [devcards.core :as dc]
    [reactions-demo.helpers :refer [slider nav-links]]
    [cljsjs.chartist])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(defcard
  (rc/as-element [nav-links]))

(defonce value (rc/atom {:val 0}))
(defonce chart-data (rc/atom '()))

(defonce lower-bound
  (rr/reaction (< 50 (:val @value))))

(defonce upper-bound
  (rr/reaction (< (:val @value) 75)))

(defonce between?
  (rr/reaction (and @lower-bound
                    @upper-bound)))

(defonce the-div (rc/atom nil))

(defn signal-graph
  []
  (let [data           (rr/reaction (->> @chart-data
                                         reverse
                                         vec))
        slider         (rr/reaction (if-let [d (not-empty (mapv :val @data))]
                                      d
                                      [0]))
        lower          (rr/reaction (if-let [d (not-empty (mapv :lower @data))]
                                      d
                                      [0]))
        upper          (rr/reaction (if-let [d (not-empty (mapv :upper @data))]
                                      d
                                      [0]))
        inner          (rr/reaction (if-let [d (not-empty (mapv :inner @data))]
                                      d
                                      [0]))

        options        (clj->js {:fullWidth     true,
                                 :axisX         {:showGrid  false
                                                 :showLabel false
                                                 :ticks     [0 25 50 75 100]}
                                 :axisY         {:showGrid  false
                                                 :showLabel false
                                                 :ticks     [0 25 50 75 100]}
                                 :showPoint     false
                                 :showArea      false
                                 :lineSmooth    false
                                 :chartPadding  {:right 40}
                                 :ticks         [0 25 50 75 100]
                                 :scaleMinSpace 20})
        processed-data (rr/reaction
                        {:labels (apply array (range 1 101))
                         :series [(apply array @slider)
                                  ;;(apply array @lower)
                                  ;;(apply array @upper)
                                  (apply array @inner)]})]
    (rc/create-class
     {:reagent-render
      (fn []
        (when-let [c @the-div]
          (.update c (clj->js @processed-data) options false))
        [:div#chart
         ""])
      :component-will-update
      (fn [_ _])
      :component-will-unmount
      (fn [_]
        (remove-watch value :watch-data))
      :component-did-mount
      (fn []
        (add-watch value :watch-data
           (fn [_ _ o n]
             (when-not (= o n)
               (let [lb  @lower-bound
                     ub  @upper-bound
                     bt  @between?
                     val (:val n)
                     b   (fn [bl] (if bl 100 0))]
                 (swap! chart-data (fn [d]
                                     (take 100
                                           (conj d {:lower (b lb)
                                                    :upper (b ub)
                                                    :inner (b bt)
                                                    :val   val}))))))))
        (reset! the-div (js/Chartist.Line. "#chart" (clj->js @processed-data) options)))})))

(defcard-doc
  "## Signals everywhere

  Reactions are a great place to read values from `ratom`s and
  `cursor`s and apply logic, there will be fewer updates to your
  components.  Everyone wins :)")

(defcard
  (fn [value _]
    (rc/as-element [:div
                    [slider value 0 100]
                    [:span {:style {:background-color (if @between?
                                         "blue"
                                         "white")}}
                     (if @between?
                       "in-between"
                       "not good")]]))
  value)

(defcard
  (rc/as-element [signal-graph]))
