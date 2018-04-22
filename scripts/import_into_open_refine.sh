#!/usr/bin/env bash
# for test, copy project into OPENREFINE
OPEN_REFINE_DIR="../../OpenRefine"
EXTENSION_ROOT="$OPEN_REFINE_DIR/extensions/fuzzy-match-extension"
mkdir $EXTENSION_ROOT
cp -r ../module $EXTENSION_ROOT
cp -r ../src $EXTENSION_ROOT
cp ../build.xml $EXTENSION_ROOT