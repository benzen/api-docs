(ns cljsdoc.core
  (:require
    [clojure.pprint :refer [pprint]]
    [me.raynes.fs :refer [list-dir base-name]]
    [clansi.core :refer [style]]
    [cljsdoc.builds :as builds]
    [cljsdoc.autodocs :refer [get-autodocs!]]
    [cljsdoc.config :refer [docs-dir]]
    [cljsdoc.transform :refer [transform-doc]]
    [cljsdoc.validate :refer [valid-doc?]]
    [cljsdoc.parse :refer [parse-doc]]))

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

(defn cljsdoc-files [dir]
  (let [files (list-dir dir)]
    (filter #(.endsWith (.getName %) ".cljsdoc") files)))

(defn build-docs []
  (let [files (cljsdoc-files docs-dir)
        mandocs (keep build-doc files)
        mandoc-map (zipmap (map :full-name mandocs)
                           (map #(dissoc % :empty-sections) mandocs))
        skipped (- (count files) (count mandocs))
        parsed (- (count files) skipped)]

    (println "----------------------------------------------------------------")
    (println)
    (if (zero? skipped)
      (println (style "Done with no errors." :green))
      (println (style "Done with some errors." :red)))
    (println (format-status parsed skipped))

    (builds/full! mandoc-map)
    (builds/report! mandoc-map)

    skipped))

(defn -main
  [& args]
  (get-autodocs!)
  (let [skipped (build-docs)]
    (System/exit (if (pos? skipped) 1 0))))

