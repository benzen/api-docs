(ns cljsdoc.utils
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.string :refer [replace]]
    [clojure.tools.reader :as reader]
    [clojure.tools.reader.reader-types :as readers]))

(defn encode-symbol
  "cljsdoc's encoding scheme for special characters."
  [s]
  (-> s
      (replace "." "DOT")
      (replace ">" "GT")
      (replace "<" "LT")
      (replace "!" "BANG")
      (replace "?" "QMARK")
      (replace "*" "STAR")
      (replace "+" "PLUS")
      (replace "=" "EQ")
      (replace "/" "SLASH")))

(defn read-forms
  "Replacement for read-string. Reads all forms from string."
  [s]
  (let [r (readers/string-push-back-reader s)]
    (loop [forms (transient [])]
      (if-let [f (try (reader/read r)
                      (catch Exception e
                        (when-not (= (.getMessage e) "EOF") (throw e))))]
        (recur (conj! forms f))
        (persistent! forms)))))

