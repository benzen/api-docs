(ns cljsdoc.validate
  (:import
    [java.util.regex Pattern])
  (:require
    [cljsdoc.utils :refer [read-forms encode-symbol example-hash]]
    [me.raynes.fs :refer [exists?]]
    [clojure.string :refer [split split-lines join]]
    [clansi.core :refer [style]]
    [fuzzy-matcher.core :as fuzzy]))

;;--------------------------------------------------------------------------------
;; Get known ClojureScript symbols
;;--------------------------------------------------------------------------------

(def known-symbols-url
  "The latest symbols auto-detected from clojurescript's code base."
  "https://raw.githubusercontent.com/cljsinfo/api-docs-generated/docs/symbol-history")

(def known-symbols
  "Set of namespace-qualified symbols from cljs."
  (atom nil))

(defn known-symbol? [s]
  (contains? @known-symbols s))

(defn get-known-symbols! []
  (reset! known-symbols
    (->> (slurp known-symbols-url)
         (split-lines)
         (map #(split % #"\s+"))              ;; split into (name,history...)
         (remove #(.startsWith (last %) "-")) ;; remove symbols not present in latest version
         (map first)
         (apply hash-set))))

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
;; Recognized Sections
;;--------------------------------------------------------------------------------

(def recognized-sections
  ["name"
   "type"
   "return type"
   "description"
   "signature"
   "todo"
   "notes"
   "example"
   #"^example#[a-z0-9]+$"
   "related"
   "docstring"
   "history"])

(defn section-match?
  [name- known]
  (if (instance? Pattern known)
    (re-find known name-)
    (= known name-)))

(defn similar-section
  [name-]
  (let [knowns (filter string? recognized-sections)
        candidates (fuzzy/search name- knowns)]
    (first candidates)))

(defn recognized-section?
  [name-]
  (some #(section-match? name- %) recognized-sections))

(defn unrecognized-section-error-msg
  [name-]
  (when-not (recognized-section? name-)
    (let [similar (similar-section name-)]
      (cond-> (str "'" name- "' is not a recognized section")
        similar (str ", did you mean '" similar "'?")))))

(defn unrecognized-sections-error-msg
  [doc]
  (let [msgs (keep unrecognized-section-error-msg (:sections doc))]
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
;; Validate Examples
;;--------------------------------------------------------------------------------

(defn example-error-msg
  [i {:keys [id] :as example}]
  (when (= "" id)
    (str "Example " (inc i) " has no hash.  Try " (example-hash))))

(defn examples-error-msg
  [{:keys [examples] :as doc}]
  (let [msgs (keep identity (map-indexed example-error-msg examples))]
    (when (seq msgs)
      (join "\n" msgs))))

;;--------------------------------------------------------------------------------
;; Validate Related
;;--------------------------------------------------------------------------------

(defn related-missing-error-msg*
  [name-]
  (let [filename (str "docs/" (gen-filename name-))]
    (when (and (not (exists? filename))
               (not (known-symbol? name-)))
      (str "Related symbol '" name- "' is an unknown symbol."))))

(defn related-missing-error-msg
  [{:keys [related] :as doc}]
  (let [msgs (keep related-missing-error-msg* related)]
    (when (seq msgs)
      (join "\n" msgs))))

;;--------------------------------------------------------------------------------
;; Validate All
;;--------------------------------------------------------------------------------

(defn valid-doc? [doc]
  (let [error-messages (keep #(% doc)
                         [required-sections-error-msg
                          unrecognized-sections-error-msg
                          filename-error-msg
                          signatures-error-msg
                          type-error-msg
                          examples-error-msg
                          related-missing-error-msg])
        valid? (empty? error-messages)]
    (when-not valid?
      (binding [*out* *err*]
        (println "----------------------------------------------------------------")
        (println (style "ERRORS" :red) (style (:filename doc) :cyan))
        (println)
        (doseq [msg error-messages]
          (println msg))))
    valid?))
