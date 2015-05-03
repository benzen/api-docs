(ns cljsdoc.transform
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :refer [split-lines trim lower-case replace]]))

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

(defn transform-description [doc]
  (if-let [desc (get doc "description")]
    (-> doc
        ;; TODO: markdown process desc
        (assoc :description desc)
        (dissoc "description"))
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

(defn make-example
  [name- doc]
  {:id (replace name- #"example#?" "")
   ;; TODO: markdown process content
   :content (get doc name-)})

(defn transform-examples [doc]
  (let [example? #(re-find #"^example(#[a-z0-9]+)?$" %)
        example-names (filter example? (:sections doc))
        examples (map #(make-example % doc) example-names)]
    (if (seq examples)
      (as-> doc d
        (assoc d :examples examples)
        (apply dissoc d example-names))
      doc)))

(defn transform-doc [doc]
  (-> doc
      transform-name
      transform-description
      transform-signature
      transform-type
      transform-examples))
