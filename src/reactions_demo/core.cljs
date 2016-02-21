(ns reactions-demo.core
  (:require
   [sablono.core :as sab :include-macros true]
   [devcards.core :as dc :refer-macros [start-devcard-ui!]]
   [reactions-demo.a1-non-reaction]
   [reactions-demo.a2-non-reaction]
   [reactions-demo.b1-reactions]
   [reactions-demo.b3-reactions]
   [reactions-demo.b4-reactions]))

(enable-console-print!)

(start-devcard-ui!)
