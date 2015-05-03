(ns cljsdoc.validate
  (:refer-clojure :exclude [replace])
  (:require
    [cljsdoc.utils :refer [read-forms encode-symbol]]
    [clojure.string :refer [replace join]]
    [clansi.core :refer [style]]))

(defn gen-filename
  "Generates expected filename from a namespace qualified symbol string"
  [fullname]
  (let [split-symbol (juxt namespace name)
        [ns- name-] (-> fullname symbol split-symbol)]
    (str ns- "_" (encode-symbol name-) ".cljsdoc")))

(defn filename-error-msg
  "If filename is not valid, return error message."
  [{:keys [full-name filename] :as doc}]
  (let [expected (gen-filename full-name)]
    (when (not= filename expected)
      (str full-name " should be in a file called " expected))))

(defn signature-error-msg
  "If signature is not valid, return error message."
  [sig]
  (let [forms (try (read-forms sig) (catch Exception e nil))]
    (when (or (> 1 (count forms))
              (not (vector? (first forms))))
      (str "signature " (pr-str sig) " must be a single valid vector"))))

(defn signatures-error-msg
  "If signatures are not valid, return all error messages."
  [{:keys [signature] :as doc}]
  (let [msgs (keep signature-error-msg signature)]
    (when (seq msgs)
      (join "\n" msgs))))

(defn valid-doc? [doc]
  (let [error-messages (keep #(% doc)
                         [filename-error-msg
                          signatures-error-msg])
        valid? (empty? error-messages)]
    (when-not valid?
      (binding [*out* *err*]
        (println "----------------------------------------------------------------")
        (println (style "ERRORS" :red) (style (:filename doc) :cyan))
        (println)
        (doseq [msg error-messages]
          (println msg))
        (println "----------------------------------------------------------------")
        (println)))
    valid?))
