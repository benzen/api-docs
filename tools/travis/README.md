# CLJS API Docs - continuous integration

This repo is integrated with [Travis-CI](http://travis-ci.org).  Configured by
the [`.travis.yml`](../../.travis.yml) file at root.

- __On every PR__, test the docs for validity.
- __On every commit__, test/build the docs, update the report, and release the build.

Files:

- `install.sh` - install leiningen dependencies
- `run.sh` - coordinates all tasks
- `publish.clj` - releases a build as a github release asset
  - `profiles.clj` - required for running publish.clj as a script

