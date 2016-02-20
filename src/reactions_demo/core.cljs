(ns reactions-demo.core
  (:require
   [sablono.core :as sab :include-macros true]
   [devcards.core :as dc]
   [reactions-demo.a1-non-reaction]
   [reactions-demo.a2-non-reaction]
   [reactions-demo.b1-reactions]
   [reactions-demo.b3-reactions]))

(enable-console-print!)

(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (js/React.render (sab/html [:div "This is working"]) node)))

(main)
