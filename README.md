# CLJS API Docs [![Build Status](https://travis-ci.org/cljsinfo/api-docs.svg)](https://travis-ci.org/cljsinfo/api-docs)

These are manually created docs that will be merged over the [generated docs]
to create all the information for a symbol's doc page.

Please see the __[api-docs-report]__ for file format details and progress.

To help __validate__ the large amount of docs we have, we are implementing a
cljsdoc file parser and validator that will run via travis-ci.

To test the parser/validator is working as expected:

```
lein test
```

To parse/validate all the cljsdoc files and produce a `docs.edn` structure:

```
lein run
```

[api-docs-report]:http://cljsinfo.github.io/api-docs-report/
[generated docs]:https://github.com/shaunlebron/cljs-api-docs
