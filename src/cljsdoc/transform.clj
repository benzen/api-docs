(ns cljsdoc.transform
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :refer [split-lines trim]]))

(defn section-as-list
  "Turn section body text into non-empty trimmed lines vector"
  [body-text]
  (->> (split-lines body-text)
       (map trim)
       (remove #{""})))

(defn transform-name
  [doc]
  (let [names (section-as-list (get doc "name"))]
    (-> doc
        (assoc :full-name (first names)
               :queries (rest names))
        (dissoc "name"))))

(defn transform-doc [doc]
  (-> doc
      transform-name))
