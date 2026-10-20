#!/usr/bin/env bash
# macOS / Linux launcher. Needs mysql-connector-j-*.jar inside ./lib for live MySQL mode.
set -e
cd "$(dirname "$0")"
mkdir -p bin lib
find src -name '*.java' > sources.txt
javac -d bin @sources.txt
rm -f sources.txt
java -cp "bin:lib/*" com.careconnect.Main
