(ns gen.core
  (:require
    [clojure.string :refer [split-lines join lowercase trim]]
    [clojure.pprint :refer [pprint]]))

(def docs-output-file "docs.edn")

(defn section-line? [line]
  (.startsWith line "====="))

(defn format-title [title]
  (-> title (subs 5) lowercase trim))

(defn format-section
  [[title-lines body-lines]]
  (let [;; title lines can potentially have more than one title if there were no lines
        ;; between them, so we ignore them by choosing the last one.
        title (-> title-lines last format-title)
        body (join "\n" body-lines)]
    [title body]))

(defn parse-doc
  [content]
  (->> (split-lines content)
       (partition-by section-line?)
       (drop-while (comp not section-line? first)) ;; ignore lines preceding first section
       (partition 2) ;; create section-body pairs
       (map format-section)
       (into {})))

(defn valid-doc? [doc]
  true)

(defn transform-doc [doc]
  doc)

(defn build-doc
  [filename]
  (let [doc (parse-doc (slurp filename))]
    (if (valid-doc? doc)
      (transform-doc doc)
      (do
        (binding [*out* *err*]
          (println (str "Skipped file '" filePath "'. Invalid format.")))
        nil))))

(defn format-status
  [parsed skipped]
  (cond-> (str "Parsed " parsed " files")
    (pos? skipped) (str ", skipped " skipped)
    true (str ".")))

(defn build-docs []
  (let [filenames (list-dir "docs")
        docs (map build-doc filenames)
        skipped (- (count filenames) (count docs))
        parsed (- (count filenames) skipped)]
    (spit docs-output-file (with-out-str (pprint docs)))
    (println (format-status parsed skipped))
    (println "Wrote" docs-output-file)))

(defn -main
  [& args]
  (println "not ready yet."))
