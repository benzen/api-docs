(ns cljsdoc.core
  (:require
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :refer [list-dir size base-name]]
    [clojure.contrib.humanize :refer [filesize]]

    [cljsdoc.transform :refer [transform-doc]]
    [cljsdoc.validate :refer [valid-doc?]]
    [cljsdoc.parse :refer [parse-doc]]))

(def docs-outfile "docs.edn")
(def min-docs-outfile "docs.min.edn")

(defn build-doc
  [file]
  (let [filename (base-name file)
        doc (-> (parse-doc (slurp file) filename)
                transform-doc)]
    (when (valid-doc? doc)
      doc)))

(defn format-status
  [parsed skipped]
  (cond-> (str "Parsed " parsed " files")
    (pos? skipped) (str ", skipped " skipped)
    true (str ".")))

(defn show-created-file-status
  [filename]
  (let [size-str (filesize (size filename) :binary true)]
    (println (str "Created " filename " (" size-str ")"))))

(defn cljsdoc-files [dir]
  (let [files (list-dir dir)]
    (filter #(.endsWith (.getName %) ".cljsdoc") files)))

(defn build-docs []
  (let [files (cljsdoc-files "docs")
        docs (keep build-doc files)
        skipped (- (count files) (count docs))
        parsed (- (count files) skipped)
        output (with-out-str (pprint docs))]
    (spit docs-outfile output)
    (spit min-docs-outfile (pr-str docs))

    (println (format-status parsed skipped))
    (show-created-file-status docs-outfile)
    (show-created-file-status min-docs-outfile)

    skipped))

(defn -main
  [& args]
  (let [skipped (build-docs)]
    (System/exit (if (pos? skipped) 1 0))))

