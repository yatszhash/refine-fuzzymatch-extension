#!/usr/bin/env bash
# for test, copy project into OPENREFINE
OPEN_REFINE_DIR="../../OpenRefine"
EXTENSION_ROOT="$OPEN_REFINE_DIR/extensions/fuzzy-match-extension"
mkdir $EXTENSION_ROOT
cp -r -v ../module $EXTENSION_ROOT
cp -r -v ../src $EXTENSION_ROOT
cp -v ../build.xml $EXTENSION_ROOT