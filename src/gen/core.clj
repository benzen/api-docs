(ns gen.core
  (:require
    [clojure.set :refer [difference]]
    [clojure.string :refer [split-lines join lower-case trim]]
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :refer [list-dir size]]
    [clojure.contrib.humanize :refer [filesize]]))

(def docs-outfile "docs.edn")
(def min-docs-outfile "docs.min.edn")

;;----------------------------------------------------------------------
;; .cljsdoc parsing
;;----------------------------------------------------------------------

(defn section-line? [line]
  (.startsWith line "====="))

(defn format-title [title]
  (-> title (subs 5) lower-case trim))

(defn format-section
  "Given title lines and body lines, create a formatted title and body pair.
  (If there was no empty line between this title and the last, they will be grouped.)"
  [[title-lines body-lines]]
  (let [title (-> title-lines last format-title) ;; ignore all but last title
        body (trim (join "\n" body-lines))]
    [title body]))

(defn parse-doc
  "Convert cljsdoc content to a map of section title => section body text."
  [content]
  (let [lines (split-lines content)

        ;; parse content as a list of section title/body pairs
        pairs (->> lines
                   (partition-by section-line?)
                   (drop-while (comp not section-line? first)) ;; ignore lines preceding first section
                   (partition 2) ;; create title/body lines pairs
                   (map format-section)
                   (remove #(= (second %) ""))) ;; remove empty sections

        ;; get the set of empty sections
        all-titles (->> lines
                        (filter section-line?)
                        (map format-title)
                        (apply hash-set))
        titles (map first pairs)
        empty-titles (->> (apply hash-set titles)
                          (difference all-titles))

        ;; get example order
        examples (filter #(.startsWith % "example") titles)

        ;; final structure
        result (-> (into {} pairs)
                   (assoc :example-ids examples
                          :empty-sections empty-titles))]
    result))

;;----------------------------------------------------------------------
;; .cljsdoc transforming
;;----------------------------------------------------------------------

(defn valid-doc? [doc]
  true)

(defn transform-doc [doc]
  doc)

;;----------------------------------------------------------------------
;; main
;;----------------------------------------------------------------------

(defn build-doc
  [filename]
  (let [doc (parse-doc (slurp filename))]
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
