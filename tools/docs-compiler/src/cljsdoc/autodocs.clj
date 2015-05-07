(ns cljsdoc.autodocs)

(def edn-url
  "Latest autodocs."
  "https://raw.githubusercontent.com/cljsinfo/api-docs-generator/docs/autodocs.edn")

(def autodoc-map
  (atom {}))

(defn get-autodocs! []
  (let [autodocs (read-string (slurp edn-url))
        doc-map (zipmap (map :full-name autodocs) autodocs)]
    (reset! autodoc-map doc-map)))