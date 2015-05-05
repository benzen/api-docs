#!/bin/bash

set -e

pushd docs-compiler
lein deps
popd

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then
  pushd docs-report
  lein deps
  popd
fi
