(ns gen.core
  (:require
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :refer [list-dir size]]
    [clojure.contrib.humanize :refer [filesize]]

    [gen.transform :refer [transform-doc]]
    [gen.validate :refer [valid-doc?]]
    [gen.parse :refer [parse-doc]]))

(def docs-outfile "docs.edn")
(def min-docs-outfile "docs.min.edn")

(defn build-doc
  [filename]
  (let [doc (parse-doc (slurp filename) filename)]
    (if (valid-doc? doc)
      (transform-doc doc)
      (do
        (binding [*out* *err*]
          (println (str "Skipped file '" filename "'. Invalid format.")))
        nil))))

(defn format-status
  [parsed skipped]
  (cond-> (str "Parsed " parsed " files")
    (pos? skipped) (str ", skipped " skipped)
    true (str ".")))

(defn show-created-file-status
  [filename]
  (let [size-str (filesize (size filename) :binary true)]
    (println (str "Created " filename " (" size-str ")"))))

(defn build-docs []
  (let [filenames (list-dir "docs")
        docs (map build-doc filenames)
        skipped (- (count filenames) (count docs))
        parsed (- (count filenames) skipped)
        output (with-out-str (pprint docs))]
    (spit docs-outfile output)
    (spit min-docs-outfile (pr-str docs))

    (println (format-status parsed skipped))
    (show-created-file-status docs-outfile)
    (show-created-file-status min-docs-outfile)))

(defn -main
  [& args]
  (build-docs))
