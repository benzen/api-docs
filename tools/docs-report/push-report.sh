#!/bin/bash

# build and publish the report to:
# http://cljsinfo.github.io/api-docs

# there are some switches here that will allow this to be run from travis-ci
# assuming the CI env var is set to true when travis-ci runs.

set -e

cd `dirname $0`

# create report data
lein run

# clone repo is needed
if [ "$CI" = "true" ]; then
  # travis-ci never persists state between builds
  git clone https://shaunlebron:${GH_TOKEN}@github.com/cljsinfo/api-docs.git hosted
elif [ ! -d hosted ]; then
  git clone git@github.com:cljsinfo/api-docs.git hosted
fi

# go to gh-pages branch
cd hosted
git checkout gh-pages

# make sure gh-pages is up-to-date
git pull

# remove all files
git rm -rf .

# add new report files
lein cljsbuild once report-prod
cp -r ../resources/report/* .

# clean out unneeded
rm -rf js/out-prod \
       js/out \
       js/report.js

# choose production page
mv index_prod.html index.html

if [ "$CI" = "true" ]; then
  # add creds
  #none of these worked on travis-ci (always failed authentication)
  #git config credential.helper store
  #echo "https://shaunlebron:${GH_TOKEN}@github.com" > .git/credentials
  git config user.name "${GIT_NAME}"
  git config user.email "${GIT_EMAIL}"
fi

# add all files
git add .

if [ -z "$(git status --porcelain)" ]; then

  echo
  echo "NO CHANGES TO PUBLISH!"

else

  # publish
  git commit -m "auto-update"

  echo
  echo "PUBLISHING REPORT..."
  git push origin gh-pages &> /dev/null # prevent github token from being printed

fi

