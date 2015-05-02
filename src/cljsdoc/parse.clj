(ns cljsdoc.parse
  (:require
    [clojure.set :refer [difference]]
    [clojure.string :refer [split-lines join lower-case trim]]))

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
  [content filename]
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
                   (assoc :filename filename
                          :example-ids examples
                          :empty-sections empty-titles))]
    result))

