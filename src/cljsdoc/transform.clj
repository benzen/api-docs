(ns cljsdoc.transform
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :refer [split-lines trim lower-case]]))

(defn section-as-list
  "Turn section body text into non-empty trimmed lines vector"
  [body-text]
  (->> (split-lines body-text)
       (map trim)
       (remove #{""})))

(defn transform-name [doc]
  (if-let [name- (get doc "name")]
    (let [names (section-as-list name-)]
      (-> doc
          (assoc :full-name (first names)
                 :queries (rest names))
        (dissoc "name")))
    doc))

(defn transform-signature [doc]
  (if-let [sig (get doc "signature")]
    (-> doc
        (assoc :signature (section-as-list sig))
        (dissoc "signature"))
    doc))

(defn transform-type [doc]
  (if-let [type- (get doc "type")]
    (-> doc
        (assoc :type (lower-case type-))
        (dissoc "type"))
    doc))

(defn transform-doc [doc]
  (-> doc
      transform-name
      transform-signature
      transform-type))
