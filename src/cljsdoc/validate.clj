(ns cljsdoc.validate
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.string :refer [replace]]))

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

(defn valid-doc? [doc]
  (let [error-funcs [filename-error-msg]
        error-messages (keep #(% doc) error-funcs)
        valid? (empty? error-messages)]
    (when-not valid?
      (binding [*out* *err*]
        (println "----------------------------------------")
        (println "ERRORS in" (:filename doc) "...\n")
        (doseq [msg error-messages]
          (println msg))
        (println "----------------------------------------\n")))
    valid?))
