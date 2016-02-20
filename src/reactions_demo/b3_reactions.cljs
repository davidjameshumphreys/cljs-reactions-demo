(ns reactions-demo.b3-reactions
  (:require
    [reagent.core :as rc]
    [reagent.ratom :as rr]
    [devcards.core :as dc]
    [reactions-demo.helpers :refer [nav-links]]
    [reactions-demo.b2-reactions :refer [reactive-component]])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc]]))

(defcard
  (rc/as-element [nav-links]))

(defcard-doc
  "## Code for the reactive slider.

  There's lots of extra code just because we want to trace it."
  (dc/mkdn-pprint-source reactive-component)
  "## A sad reaction :'(

   In the example above, `another` is never read in the code.  We
   never see it evaluated.")
