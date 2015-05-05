#!/bin/bash

set -e

echo
echo "COMPILING DOCS..."
cd docs-compiler
lein test
lein run
cd ..

if [ "${TRAVIS_PULL_REQUEST}" = "false" ]; then

  echo
  echo "BUILDING REPORT..."

  pushd docs-report

  # create report data
  lein run

  # create report code
  lein cljsbuild once report-prod

  # clone gh-pages branch
  git clone --branch gh-pages https://shaunlebron:${GH_TOKEN}@github.com/cljsinfo/api-docs.git hosted

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

  if [ -z "$(git status --porcelain)" ]; then

    echo "NO CHANGES TO PUBLISH!"

  else

    # add creds
    #none of these worked on travis-ci (always failed authentication)
    #git config credential.helper store
    #echo "https://shaunlebron:${GH_TOKEN}@github.com" > .git/credentials
    git config user.name "${GIT_NAME}"
    git config user.email "${GIT_EMAIL}"

    # publish
    git add .
    git commit -m "auto-update"

    echo
    echo "PUBLISHING REPORT..."
    git push origin gh-pages &> /dev/null

  fi

  popd # hosted

  popd # docs-report
fi