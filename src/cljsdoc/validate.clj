(ns cljsdoc.validate
  (:require
    [cljsdoc.utils :refer [read-forms encode-symbol]]
    [clojure.string :refer [join]]
    [clansi.core :refer [style]]))

;;--------------------------------------------------------------------------------
;; Required Sections
;;--------------------------------------------------------------------------------

(def required-sections
  ["name"
   "description"])

(defn required-section-error-msg
  "Returns error message if section name is not present in doc."
  [name- doc]
  (when-not (some #{name-} (:sections doc))
    (str "'" (name name-) "' is a required section.")))

(defn required-sections-error-msg
  "Returns error messages if required sections are not present in doc."
  [doc]
  (let [msgs (keep #(required-section-error-msg % doc) required-sections)]
    (when (seq msgs)
      (join "\n" msgs))))

;;--------------------------------------------------------------------------------
;; Validate Filename
;;--------------------------------------------------------------------------------

(defn gen-filename
  "Generates expected filename from a namespace qualified symbol string"
  [fullname]
  (let [split-symbol (juxt namespace name)
        [ns- name-] (-> fullname symbol split-symbol)]
    (str ns- "_" (encode-symbol name-) ".cljsdoc")))

(defn filename-error-msg
  "If filename is not valid, return error message."
  [{:keys [full-name filename] :as doc}]
  (when full-name
    (let [expected (gen-filename full-name)]
      (when (not= filename expected)
        (str full-name " should be in a file called " expected)))))

;;--------------------------------------------------------------------------------
;; Validate Signature
;;--------------------------------------------------------------------------------

(defn signature-error-msg
  "If signature is not valid, return error message."
  [sig]
  (let [forms (try (read-forms sig) (catch Exception e nil))
        valid? (and (= 1 (count forms))
                    (vector? (first forms)))]
    (when-not valid?
      (str "signature " (pr-str sig) " must be a single valid vector"))))

(defn signatures-error-msg
  "If signatures are not valid, return all error messages."
  [{:keys [signature] :as doc}]
  (let [msgs (keep signature-error-msg signature)]
    (when (seq msgs)
      (join "\n" msgs))))

;;--------------------------------------------------------------------------------
;; Validate Type
;;--------------------------------------------------------------------------------

(def valid-type?
  #{"function"
    "macro"
    "special form"
    "special form (repl)"
    "macro character"
    "syntax"
    "tagged literal"
    "special var"})

(defn type-error-msg
  "If type is not valid, return error message."
  [doc]
  (when-let [type- (:type doc)]
    (when-not (valid-type? type-)
      (str "'" type- "' is not a valid type."))))

;;--------------------------------------------------------------------------------
;; Validate All
;;--------------------------------------------------------------------------------

(defn valid-doc? [doc]
  (let [error-messages (keep #(% doc)
                         [required-sections-error-msg
                          filename-error-msg
                          signatures-error-msg
                          type-error-msg])
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
