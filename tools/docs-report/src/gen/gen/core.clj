(ns gen.core
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.java.shell :refer [sh]]
    [clojure.string :refer [replace join split split-lines trim]]
    [me.raynes.fs :refer [list-dir base-name exists? mkdir]]
    )
  (:import
    (java.net URLEncoder)))

(defn decode-name
  [filename]
  (-> filename
      (replace "DOT"   ".")
      (replace "GT"    ">")
      (replace "LT"    "<")
      (replace "BANG"  "!")
      (replace "QMARK" "?")
      (replace "STAR"  "*")
      (replace "PLUS"  "+")
      (replace "EQ"    "=")
      (replace "SLASH" "/")))

(def example-symbol
  {:ns "cljs.core"
   :name "re-find"
   :auto-link ""
   :manual-link ""
   :corelib-link ""
   :examples-link ""
   :clojuredocs ""
   :grimoire ""
   :crossclj ""
   })

(def symbols (atom nil))

(defn get-ns-and-name
  [filename]
  (when-let [[_ ns- name-] (re-find #"([^_]+)_(.+)\.cljsdoc" filename)]
    [ns- (decode-name name-)]))

(defn clone-docs-repos
  []
  (when-not (exists? "repos")
    (mkdir "repos"))
  (when-not (exists? "repos/api-docs-generator")
    (sh "git" "clone" "https://github.com/cljsinfo/api-docs-generator.git" :dir "repos")))

(defn get-report-docs []
  (read-string (slurp "../docs-compiler/cljsdocs-report.edn")))

(defn get-corelib-notes
  "to be merged with existing api symbol data"
  []
  (let [f (slurp "https://raw.githubusercontent.com/clojure/clojurescript/master/devnotes/corelib.org")
        values (keep identity
                  (for [line (split-lines f)]
                    (when-let [[_ name-] (re-find #"^\* DONE ([^\s]+)" line)]
                      {:full-name (str "cljs.core/" name-)
                       :corelib-link (str "https://github.com/clojure/clojurescript/blob/master/devnotes/corelib.org#" name-)})))
        result (zipmap (map :full-name values) values)]
    result))

;; from clojuredocs:
;; https://github.com/zk/clojuredocs/blob/813caf018bc2f0681553581761c222f3f9b78d7f/src/cljx/clojuredocs/util.cljx#L58-L66
(defn cd-encode [s]
  (when s
    (cond
      (= "." s) "_."
      (= ".." s) "_.."
      :else (-> s
                (replace #"/" "_fs")
                (replace #"\\" "_bs")
                (replace #"\?" "_q")))))

(defn cljs->clojure-ns
  [ns-]
  (-> ns-
      (replace "cljs.core" "clojure.core")
      (replace "cljs.test" "clojure.test")))

(defn symbol->clojuredocs-url
  [ns- s]
  (let [ns- (cljs->clojure-ns ns-)
        s (cd-encode s)]
    (str "http://clojuredocs.org/" ns- "/" s)))

(defn symbol->grimoire-url
  [ns- s]
  (let [ns- (cljs->clojure-ns ns-)
        s (URLEncoder/encode s "UTF-8")]
    (str "http://conj.io/store/v1/org.clojure/clojure/1.7.0-alpha4/clj/" ns- "/" s "/")))

(defn symbol->crossclj-url
  [ns- s]
  (let [ns- (cljs->clojure-ns ns-)
        s (URLEncoder/encode s "UTF-8")]
    (str "http://crossclj.info/fun/" ns- "/" s ".html")))

(defn get-examples-and-refs
  "Examples are scraped from clojuredocs, so we use the existence of an example file
   to suggest the existence of clojuredocs, grimoire, and crossclj links."
  []
  (sh "git" "checkout" "examples" :dir "repos/api-docs-generator")
  (sh "git" "pull" :dir "repos/api-docs-generator")
  (let [filenames (filter #(re-find #".cljsdoc$" %) (map base-name (list-dir "repos/api-docs-generator/")))
        symbols (for [f filenames]
                  (join "/" (get-ns-and-name f)))
        values (for [f filenames]
                 (let [example-count (count (re-seq #"===== Example" (slurp (str "repos/api-docs-generator/" f))))
                       [ns- name-] (get-ns-and-name f)]
                   {:full-name (str ns- "/" name-)
                    :examples-count example-count
                    :examples-link (str "https://github.com/cljsinfo/api-docs-generator/blob/examples/" f)
                    :clojuredocs (symbol->clojuredocs-url ns- name-)
                    :grimoire (symbol->grimoire-url ns- name-)
                    :crossclj (symbol->crossclj-url ns- name-)}))
        result (zipmap symbols values)]
    result))

(defn -main
  [& args]
  (clone-docs-repos)

  (let [data (merge-with merge
               (get-report-docs)
               (get-corelib-notes)
               (get-examples-and-refs))
        filename "resources/report/symbol-data.edn" ]
    (spit filename (pr-str data))
    (println "Wrote to" filename))

  (System/exit 0))

