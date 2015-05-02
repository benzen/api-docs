# CLJS API Docs [![Build Status](https://travis-ci.org/cljsinfo/api-docs.svg)](https://travis-ci.org/cljsinfo/api-docs)

These are manually created docs that will be merged over the [generated docs]
to create all the information for a symbol's doc page.

To help __validate__ the large amount of docs we have, we are implementing
a cljsdoc file parser and validator that will run via travis-ci.

Please see the __[api-docs-report]__ for file format details and progress.

To run the tests:

```
lein test
```

[api-docs-report]:http://cljsinfo.github.io/api-docs-report/
[generated docs]:https://github.com/shaunlebron/cljs-api-docs
