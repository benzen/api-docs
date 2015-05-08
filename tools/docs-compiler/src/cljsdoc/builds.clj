(ns cljsdoc.builds
  (:refer-clojure :exclude [replace])
  (:require
    [cljsdoc.autodocs :refer [autodoc-map]]
    [me.raynes.fs :refer [size]]
    [clojure.string :refer [replace]]
    [clojure.contrib.humanize :refer [filesize]]
    ))

(defn created-file-status
  [filename]
  (let [size-str (filesize (size filename) :binary true)]
    (str "Created " filename " (" size-str ")")))

(defn spit-docs!
  [filename output]
  (spit filename (pr-str output))
  (println (created-file-status filename)))

(defn mapmap
  "Apply a map function over the values of a map, returning a map."
  [mapf datamap]
  (let [[ks vs] ((juxt keys vals) datamap)
        result (zipmap ks (map mapf vs))]
    result))

(defn full!
  "Create a build with unabridged docs."
  [mandoc-map]
  (let [filename "cljsdocs-full.edn"
        mandocs (mapmap #(dissoc % :sections) mandoc-map)
        stringify #(if % (str %) %)
        process-val #(update-in % [:return-type] stringify)
        output (->> (merge-with merge @autodoc-map mandocs)
                    (mapmap process-val))]
    (spit-docs! filename output)))

(defn report!
  "Create a build for the report."
  [mandoc-map]
  (let [filename "cljsdocs-report.edn"

        autodocs (mapmap #(assoc % :auto-link
                            (str "https://github.com/cljsinfo/api-docs-generator/blob/docs/docs/"
                              (:filename %)))
                         @autodoc-map)

        mandocs (mapmap #(assoc %
                           :manual-link
                           (str "https://github.com/cljsinfo/api-docs/blob/master/docs/"
                             (:filename %))
                           :manual-examples-count (count (:examples %))
                           :manual-todos-left (some #{"todo"} (:sections %)))
                        mandoc-map)

        output (mapmap #(-> %
                            (select-keys
                              [:full-name :ns :name
                               :auto-link
                               :manual-link
                               :manual-examples-count
                               :manual-todos-left])
                            (assoc :full-name-encode (replace (:filename %) #"\.cljsdoc$" "")))
                       (merge-with merge autodocs mandocs))]
    (spit-docs! filename output)))

