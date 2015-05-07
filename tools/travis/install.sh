#!/bin/bash

# we execute this separately in travis-ci so the dependencies
# downloads don't pollute our task log

set -e

pushd docs-compiler
lein deps
popd

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then
  pushd docs-report
  lein deps
  popd
fi
