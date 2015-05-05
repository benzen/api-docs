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

## Report

The `docs-report` directory contains the visual report code.

[visual report]:http://cljsinfo.github.io/api-docs/
[generated docs]:https://github.com/cljsinfo/api-docs-generator
