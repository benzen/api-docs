#!/bin/bash

set -e

pushd docs-compiler
lein deps
popd

pushd docs-report
lein deps
popd
