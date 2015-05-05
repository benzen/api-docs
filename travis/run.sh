#!/bin/bash

set -e

cd docs-compiler
lein test
lein run
cd ..

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then

  pushd docs-report

  # create report data
  lein run

  # create report code
  lein cljsbuild once report-prod

  # clone gh-pages branch
  git clone --branch gh-pages https://github.com/cljsinfo/api-docs.git hosted

  pushd hosted

  # remove all files
  git rm -rf .

  # add new report files
  cp -r ../resources/report/* .

  # clean out unneeded
  rm -rf js/out-prod \
         js/out \
         js/report.js

  # choose production page
  mv index_prod.html index.html

  # publish to website
  echo "https://${GH_TOKEN}:@github.com" > .git/credentials
  git add .
  git commit -m "auto-update"
  git push origin gh-pages

  popd # hosted

  popd # docs-report
fi
