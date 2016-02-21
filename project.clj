(defproject reactions-demo "0.1.0-SNAPSHOT"
  :description "Demo code showing the benefits of reactions in reagent"
  :url "https://github.com/davidjameshumphreys/cljs-reactions-demo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [devcards "0.2.1-5"
                  :exclusions [cljsjs/react]]
                 [sablono "0.5.3"]

                 [reagent "0.6.0-alpha" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "0.14.3-0"]
                 [cljsjs/chartist "0.9.4-2"]]

  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {:builds [{:id           "devcards"
                        :source-paths ["src"]
                        :figwheel     {:devcards true}
                        :compiler     {:main                 "reactions-demo.core"
                                       :asset-path           "js/compiled/devcards_out"
                                       :output-to            "resources/public/js/compiled/reactions_demo_devcards.js"
                                       :output-dir           "resources/public/js/compiled/devcards_out"
                                       :source-map-timestamp true }}
                       {:id           "prod"
                        :source-paths ["src"]
                        :compiler     {:main           "reactions-demo.core"
                                       :asset-path     "js/compiled/"
                                       :devcards       true
                                       :output-dir     "resources/public/js/compiled/"
                                       :source-map     "resources/public/js/compiled/reactions_demo_devcards.js.map"
                                       :output-to      "resources/public/js/compiled/reactions_demo_devcards.js"
                                       :optimizations  :advanced
                                       :parallel-build true}}]}

  :figwheel { :css-dirs ["resources/public/css"] })
