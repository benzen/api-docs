# CLJS API Docs Report

Creates the report at <http://cljsinfo.github.io/api-docs>

## Setup

1. Use this generate the data required by the report (run docs-compiler first):

    ```
    lein run
    ```

1. Use this when developing the report UI:

    ```
    lein figwheel report
    ```

1. Open <http://localhost:3449/> to see the report.

## Updating the official report

This will generate and push the report to this repo's `gh-pages` to publish it.

```
./push-report.sh
```

