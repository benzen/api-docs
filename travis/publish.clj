(use '[leiningen.exec :only (deps)])

;; download deps
(deps '[[me.raynes/conch "0.8.0"]
        [me.raynes/fs "1.4.6"]
        [org.clojure/data.json "0.2.6"]])

;; use deps
(require '[me.raynes.conch :refer [programs]]
         '[me.raynes.fs :refer [glob base-name]]
         '[clojure.data.json :as json]
         '[clojure.pprint :refer [pprint]]
         '[clojure.string :refer [trim]])

;; use curl command
(programs curl)

;; get github token
(def gh-token (System/getenv "GH_TOKEN"))

(def user "cljsinfo")
(def repo "api-docs")
(def release "1260660")

(defn releases-url
  [sub]
  (str "https://" sub ".github.com/repos/" user "/" repo "/releases/"))

(defn get-assets []
  (json/read-str (curl (str (releases-url "api") release "/assets")) :key-fn keyword))

(defn delete-asset
  [id]
  (let [url (str (releases-url "api") "assets/" id "?access_token=" gh-token)]
    (println url)
    (pprint (curl "-X" "DELETE" url {:verbose true}))))

(defn upload-asset
  [file]
  (let [url (str \" (releases-url "uploads")
                 release "/assets"
                 "?name=" (base-name file)
                 "&access_token=" gh-token
                 \")]
    (println url)
    (pprint (curl "-X" "POST"
                  "-H" "\"Content-Type:application/edn\""
                  "--data-binary" (str "@" (str file))
                  url
                  {:verbose true}))))

;; get existing assets
(def assets (get-assets))

;; get files to upload
(def files (glob "../docs-compiler/cljsdocs-*.edn"))

(doseq [f files]
  (let [filename (base-name f)
        existing-id (->> assets (filter #(= (:name %) filename)) first :id)]

    ;; delete existing asset
    (when existing-id
      (println "Deleting" filename)
      (delete-asset existing-id))

    ;; upload new asset
    (println "Uploading" filename)
    (upload-asset f)))
