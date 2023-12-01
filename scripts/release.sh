#!/bin/bash
set -e -o pipefail

cd ..

echo "Read to release to maven central?"
read -n 1 -srp "Press any key to continue (ctrl+c to cancel)"; printf "\n\n";

mvn clean deploy -P maven-central -DskipTests


echo "Run the following command to clean up after the release:"
echo "./postrelease.sh"
