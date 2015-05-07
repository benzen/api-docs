## ClojureScript API Documentation Project

[![Build Status](https://travis-ci.org/cljsinfo/api-docs.svg)](https://travis-ci.org/cljsinfo/api-docs)

Creating ClojureScript API [`docs/`](docs) in a simple plaintext format (examples, descriptions, see-also),
then compiling them with [`tools/`](tools) where they are validated and merged with automatically parsed API data, broadcasted to a progress report, and released as an edn file ready to use.

- __[Project Website](http://cljsinfo.github.io/api-docs/)__ for more details and a progress chart continuously updated on every commit:
- __[Download Latest Docs](https://github.com/cljsinfo/api-docs/releases/download/docs-release/cljsdocs-full.edn)__ in an EDN format, a list of structures of the following form:

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

### Contributors

If you want to help by modifying/adding doc files to `docs/`, peruse the
[project page](http://cljsinfo.github.io/api-docs) to learn about the format
and then [read the examples guide](https://github.com/cljsinfo/api-docs/wiki/Examples-Guide)
to help write proper examples.  Pull Requests welcome!

#### Create/Edit docs from the browser

You don't have to leave your browser to contribute new/edited docs:

1. [Click a symbol in progress](http://cljsinfo.github.io/api-docs/#progress) that you wish to modify/create.

[![progress](http://i.imgur.com/rhhPfkA.png)](http://cljsinfo.github.io/api-docs/#progress)

1. Click "add new" to create a new doc, or click "manual docs" to see an existing doc.

![create-or-edit](http://i.imgur.com/WRp8UCO.png)

1. You can __edit existing__ docs by clicking the edit button in github.

![edit](http://i.imgur.com/KbVEpiu.png)

1. Once you save your new file or edits, a Pull Request will be created.

1. If the PR requires edits, you can edit through the "Files changed" tab.

### License

The `docs/` files are released under the CC0.

The `tools/` code is released under the MIT license.

The docstrings and source code included in the generated docs release are
Copyright © Rich Hickey. All rights reserved. Eclipse Public License 1.0
