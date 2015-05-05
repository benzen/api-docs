# CLJS API Docs Report

our progress in documenting the CLJS API

Hosted at <http://cljsinfo.github.io/api-docs>

## Setup

1. Use this generate the data required by the report:

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
$ lein run
$ ./push-report.sh
```
