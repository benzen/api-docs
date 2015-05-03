(defproject api-docs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [clojure-humanize "0.1.0"]
                 [narkisr/clansi "1.2.0"]
                 [org.clojure/tools.reader "0.9.2"]
                 [ccfontes/fuzzy-matcher "0.0.1"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :main cljsdoc.core
  )