(ns cljsdoc.validate
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.tools.reader :as reader]
    [clojure.tools.reader.reader-types :as readers]
    [clojure.string :refer [replace join]]
    [clansi.core :refer [style]]))

(defn encode-symbol
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

(defn signature-error-msg [sig]
  (let [forms (try (read-forms sig) (catch Exception e nil))]
    (when (or (> 1 (count forms))
              (not (vector? (first forms))))
      (str "signature " (pr-str sig) " must be a single valid vector"))))

(defn signatures-error-msg
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
