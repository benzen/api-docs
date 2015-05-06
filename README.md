# CLJS API Docs [![Build Status](https://travis-ci.org/cljsinfo/api-docs.svg)](https://travis-ci.org/cljsinfo/api-docs)

These are manually created docs that will be merged over the [generated docs]
to create all the information for a symbol's doc page.

Please see the __[visual report]__ for file format details and progress.

## Compiler

To help __validate__ the large amount of docs we have, we are implementing a
cljsdoc file parser and validator that will run via travis-ci.

In the `docs-compiler/` directory, you can:

1. Test the parser/validator is working as expected:

    ```
    lein test
    ```

1. Parse/validate all the cljsdoc files and produce edn structure files for different uses:

    ```
    lein run
    ```

## Docs Release (edn)

The full docs are available for [download here](https://github.com/cljsinfo/api-docs/releases/download/docs-release/cljsdocs-full.edn):

It is a list of maps containing the following keys:

```clj
{;; from auto docs
 :full-name         "cljs.core/assoc-in"
 :ns                "cljs.core"
 :name              "assoc-in"
 :type              "function"
 :signature         ["[m [k & ks] v]" *]
 :docstring         "...full docstring..."
 :history           ["+r927"]
 :return-type       nil
 :source-filename   "clojurescript/src/cljs/cljs/core.cljs"
 :source-link       "https://github.com/clojure/clojurescript/blob/r2505/src/cljs/cljs/core.cljs#L4018-L4025"
 :source            "...full source code..."

 ;; from manual docs
 :queries           () ;; mainly for syntax forms, (e.g. syntax/vector has "[]" as a query)
 :description       "... markdown description ..."
 :examples          ({:id "e76f20" content "... markdown example ..."} *})
 :related           ("cljs.core/assoc" "cljs.core/update-in" "cljs.core/get-in")
 }
```

## Report

The `docs-report` directory contains the visual report code.

[visual report]:http://cljsinfo.github.io/api-docs/
[generated docs]:https://github.com/cljsinfo/api-docs-generator
