#!/bin/bash

set -e

pushd ../docs-compiler
  echo
  echo "COMPILING DOCS..."
  lein test
  lein run
popd

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then

  pushd ../docs-report
    echo
    echo "BUILDING REPORT..."
    ./push-report.sh
  popd

  echo
  echo "PUBLISHING DOCS..."
  lein exec publish.clj

fi
