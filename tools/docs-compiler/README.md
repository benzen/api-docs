# CLJS API Docs Compiler

- Parses and validates the `docs/*.cljsdoc` files at root
- Merges with parsed ClojureScript API metadata
- Creates `cljsdoc-*.edn` build release files

## Usage

1. Test the parser/validator:

    ```
    lein test
    ```

1. Perform all the build tasks mentioned above:

    ```
    lein run
    ```

