(defproject cljs-docs-report "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2665"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [me.raynes/fs "1.4.6"]
                 [hiccups "0.3.0"]
                 [figwheel "0.2.3-SNAPSHOT"]
                 [cljs-ajax "0.3.10"]
                 [markdown-clj "0.9.62"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-figwheel "0.2.3-SNAPSHOT"]
            [lein-auto "0.1.2"]]

  :source-paths ["src/gen"]

  :main gen.core

  :auto
  {:default
   {:paths ["cljs-doc-files"]
    :file-pattern #"\.cljsdoc$"}}

  :cljsbuild
  {
   :builds
   [{:id "report"
     :source-paths ["src/report" "src/report_dev"]
     :compiler
     {:output-to  "resources/report/js/report.js"
      :output-dir "resources/report/js/out"
      :optimizations :none
      :source-map true}}

    {:id "report-prod"
     :source-paths ["src/report"]
     :compiler
     {:output-to  "resources/report/js/report-prod.js"
      :output-dir "resources/report/js/out-prod"
      :optimizations :advanced}}

    ]}

  :figwheel
  {:http-server-root "report"
   :css-dirs "resources/report"}

  )
