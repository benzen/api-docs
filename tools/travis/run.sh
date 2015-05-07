#!/bin/bash

set -e

echo
echo "COMPILING DOCS..."
cd docs-compiler
lein test
lein run
cd ..

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then

  pushd docs-report
  echo
  echo "BUILDING REPORT..."
  ./push-report.sh
  popd # docs-report

  pushd travis
  echo
  echo "PUBLISHING DOCS..."
  lein exec publish.clj
  popd # travis
fi
