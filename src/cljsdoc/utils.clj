(ns cljsdoc.utils
  (:require
    [clojure.tools.reader :as reader]
    [clojure.tools.reader.reader-types :as readers]))

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

