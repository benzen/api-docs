(ns gen.core
  (:require
    [clojure.string :refer [split-lines join lower-case trim]]
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :refer [list-dir size]]
    [clojure.contrib.humanize :refer [filesize]]))

(def docs-outfile "docs.edn")
(def min-docs-outfile "docs.min.edn")

(defn section-line? [line]
  (.startsWith line "====="))

(defn format-title [title]
  (-> title (subs 5) lower-case trim))

(defn format-section
  "Given title lines and body lines, create a formatted title and body pair.

  ===== Foo   <---- Title
  ===== Bar   <----   lines (potentially multiple due to partitioning algorithm)

  hello       <---- Body
  world       <----   lines
  "
  [[title-lines body-lines]]
  (let [title (-> title-lines last format-title) ;; ignore all but last title
        body (join "\n" body-lines)]
    [title body]))

(defn example-ids
  "Get ordered list of example title ids from a section pair (title,body)"
  [pairs]
  (->> pairs
       (map first)
       (filter #(.startsWith % "example"))))

(defn pairs->map
  "Convert the final parsed section title/body pairs to a map."
  [pairs]
  (-> (into {} pairs)
      (assoc :example-ids (example-ids pairs))))

(defn parse-doc
  "Convert cljsdoc content to a map of section title => section body text.
  Plus "
  [content]
  (->> (split-lines content)
       (partition-by section-line?)
       (drop-while (comp not section-line? first)) ;; ignore lines preceding first section
       (partition 2) ;; create section-body pairs
       (map format-section)
       pairs->map))

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
