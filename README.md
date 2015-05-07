## ClojureScript API Documentation Project [<img align="right" src="https://travis-ci.org/cljsinfo/api-docs.svg">](https://travis-ci.org/cljsinfo/api-docs)

Creating ClojureScript API [`docs/`](docs) in a simple plaintext format (examples, descriptions, see-also),
then compiling them with [`tools/`](tools) where they are validated and merged with automatically parsed API data, broadcasted to a progress report, and released as an edn file ready to use.

__[Project Website](http://cljsinfo.github.io/api-docs/)__ for more details and a progress chart continuously updated on every commit:

[![progress](http://i.imgur.com/rhhPfkA.png)](http://cljsinfo.github.io/api-docs/)

__[Download Latest Docs](https://github.com/cljsinfo/api-docs/releases/download/docs-release/cljsdocs-full.edn)__ in an EDN format, a list of structures of the following form:

```clj
{;; from auto docs
 :full-name         "cljs.core/assoc-in"
 :ns                "cljs.core"
 :name              "assoc-in"
 :type              "function"
 :signature         ["[m [k & ks] v]" *]
 :docstring         "...full docstring..."
 :history           ["+r927"]
 :return-type       nil ;; if detected
 :source-filename   "clojurescript/src/cljs/cljs/core.cljs"
 :source-link       "https://github.com/clojure/clojurescript/blob/r2505/src/cljs/cljs/core.cljs#L4018-L4025"
 :source            "...full source code..."

 ;; from manual docs
 :queries           () ;; mainly for syntax forms, (e.g. syntax/vector has "[]" as a query)
 :description       "... markdown description ..."
 :examples          ({:id "e76f20" :content "... markdown example ..."} *)
 :related           ("cljs.core/assoc" "cljs.core/update-in" "cljs.core/get-in")}
```

