(ns report.core
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]]
    [hiccups.core :refer [html defhtml]])
  (:require
    [hiccups.runtime]
    [ajax.core :refer [GET]]
    [cljs.core.async :refer [chan <! put! close!]]
    [clojure.string :refer [join split split-lines]]
    [hiccups.runtime]
    [ajax.core :refer [GET]]
    [markdown.core :refer [md->html]]))

(enable-console-print!)

;; Columns:
;; 
;; symbol name
;;   - autodocs link
;;   - "from clojure.core" if in corelib.org (link to github repo)
;; 
;; manual docs status
;;   - cljsdoc link
;;   - number of examples
;; 
;; references:
;;   - "on clojuredocs"
;;     - number of examples
;;   - grimoire
;;   - http://crossclj.info/fun/cljs.core/unchecked-multiply-int.html for usages
;; 


;; markdown content
(def welcome-md nil)
(def progress-md nil)

(defn welcome-section
  []
  [:div.header
   (md->html welcome-md)])

(def symbol-data nil)
(def wont-doc? nil)

(defn transform-wont-doc
  [content]
  (let [lines (split-lines content)]
    (apply hash-set lines)))

(defn get-color
  [{:keys [manual-link
           full-name
           manual-examples-count]}]
  (if (wont-doc? full-name)
    "gray"
    (if manual-link
      (if (pos? manual-examples-count)
        "green"
        "yellow")
      "red")))

(defn add-color
  [data]
  (let [color (get-color data)]
    (assoc data :color color)))

(def ns-description
  {"syntax" "syntax forms (not in a namespace)"
   "special" "special forms (not in a namespace)"
   "specialrepl" "repl special forms (not in a namespace)"})

(def ns-order
  {"syntax" 0
   "special" 1
   "specialrepl" 2
   "cljs.core" 3})

(defn sort-namespaces
  [a b]
  (let [ai (get ns-order a)
        bi (get ns-order b)]
    (cond
      (and (nil? ai) (nil? bi)) (compare a b)
      (nil? ai) 1
      (nil? bi) -1
      :else (compare ai bi))))

(defn transform-symbol-data
  [data]
  (let [new-data (group-by :ns (vals data))
        values (->> (vals new-data)
                    (map #(sort-by :name %))
                    (map #(map add-color %))
                    doall)
        result (->> (map vector (keys new-data) values)
                    (sort-by first sort-namespaces)
                    (filter first) ;; <--- TODO: remove symbols that failed to parse (indicated by nil namespace)
                                   ;; i.e. - repl things (*1 *2 *3 in-ns)
                                   ;;      - special forms (catch, finally) because api-parser can't detect them (part of `try`)
                                   ;;      - functions deleted from api (destructure, format)
                    )]
    result))

(defn ns-overview-table
  [ns- symbols]
  [:div.overview-table
   [:h2 [:a {:name ns-
             :href (str "#" ns-)}
         ns-]]
   (when-let [desc (ns-description ns-)]
     [:div.ns-description
      desc])

   (for [s symbols]
     [:a {:href (str "#" (:full-name-encode s))}
      [:div
       {:class (str "symbol-box " (:color s) "-bg")}
       (:name s)]])])

(defn ns-full-table
  [ns- symbols]
  [:div
   [:h2 ns-]
   (when-let [desc (ns-description ns-)]
     [:div.ns-description
      desc])
   [:table
    [:tr.header-row
     [:td
      "symbol name"]
     [:td {:colspan 2}
      "will be merged for final doc page"]
     [:td {:colspan 2}
      "external references"]]
    (for [{:keys [auto-link
                  manual-link
                  manual-examples-count
                  examples-count
                  examples-link
                  full-name-encode
                  color
                  clojuredocs
                  grimoire
                  crossclj
                  corelib-link] :as s} symbols]
      [:tr
       [:td.symbol-column
        [:a {:name full-name-encode
             :href (str "#" full-name-encode)}
         [:span.ns ns- "/"]
         [:span.symbol (:name s)]]
        (when corelib-link
          [:div.corelib [:a {:href corelib-link} "(a corelib symbol)"]])]
       [:td.autodoc-column
        (if auto-link
          [:a {:href auto-link} "auto-docs"]
          "no auto-docs")]
       [:td
        {:class (str "manualdoc-column " color "-bg")}
        (case color
          "gray" "won't document"
          "red" "missing manual docs"
          nil)
        (when manual-link
          (list
            [:a {:href manual-link} "manual docs"]
            [:div.manual-examples
             (if (zero? manual-examples-count)
               (list
                 [:i.fa.fa-times]
                 " examples missing")
               (list
                 manual-examples-count
                 " example" (when (> manual-examples-count 1) "s")))
             ]))]
       [:td.examples-column
        (when (pos? examples-count)
          (let [s (str examples-count " example" (when (> examples-count 1) "s") " scraped.")]
            [:a {:href examples-link} s]))]
       [:td.refs-column
        (when clojuredocs
          (list
            [:a {:href clojuredocs} "clojuredocs"] " | "
            [:a {:href grimoire} "grimoire"] " | "
            [:a {:href crossclj} "crossclj"]))]
       ]
      )]
   ])

(defn symbol-table []
  [:div#symbol-table
   (md->html progress-md)
   (for [[ns- symbols] symbol-data]
     (ns-overview-table ns- symbols))
   (for [[ns- symbols] symbol-data]
     (ns-full-table ns- symbols))])

(defn page []
  (html
    [:div
     (welcome-section)
     (symbol-table)]))

(defn force-hash-nav!
  []
  (let [h (aget js/location "hash")]
    (aset js/location "hash" "")
    (aset js/location "hash" h)))

(defn make-links-popout!
  []
  (let [elements (.querySelectorAll js/document "a")
        href (first (split (aget js/location "href") "#"))]
    (dotimes [i (aget elements "length")]
      (let [el (aget elements i)
            target (.getAttribute el "href")]
        (when-not (= (first target) "#")
          (.setAttribute el "target" "_blank"))))))

(defn re-render
  []
  (let [e (. js/document (getElementById "app"))]
    (aset e "innerHTML" (page)))
  (make-links-popout!)
  (force-hash-nav!))

(defn get-async
  [url res-format]
  (let [c (chan)
        handler (fn [data] (put! c data) (close! c))]
    (GET url {:response-format res-format :handler handler})
    c))

(defn fetch-all
  [urls]
  (->> urls
       (map #(get-async (first %) (second %)))
       (zipmap (keys urls))))

(defn load-fonts!
  [families]
  (let [c (chan)]
    (.load js/WebFont (clj->js {:google {:families families} :active #(close! c)}))
    c))

(defn main
  []
  (let [downloads (fetch-all {"symbol-data.edn" :edn
                              "welcome.md" :raw
                              "progress.md" :raw
                              "wontdoc" :raw})
        loading-fonts (load-fonts! ["Roboto+Slab:400,700,300"
                                    "Roboto:100,300"
                                    "Open+Sans:400,700"
                                    "Inconsolata"])]
    (go
      (set! symbol-data (transform-symbol-data (<! (get downloads "symbol-data.edn"))))
      (set! welcome-md  (<! (get downloads "welcome.md")))
      (set! progress-md (<! (get downloads "progress.md")))
      (set! wont-doc? (transform-wont-doc (<! (get downloads "wontdoc"))))
      (<! loading-fonts)
      (re-render))))

(.addEventListener js/window "load" main)

