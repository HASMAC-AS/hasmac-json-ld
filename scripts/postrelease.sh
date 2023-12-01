#!/bin/bash
set -e -o pipefail

cd ..

# Prompt the user to enter the new version number
echo "Please enter the new snapshot version (include -SNAPSHOT in the string):"

# Read the input from the user and store it in the variable
read MVN_NEXT_SNAPSHOT_VERSION

# (Optional) Display the entered version number
echo "The new version number is: $MVN_NEXT_SNAPSHOT_VERSION"

read -n 1 -srp "Press any key to continue (ctrl+c to cancel)"; printf "\n\n";

# set maven version
mvn versions:set  -DnewVersion="${MVN_NEXT_SNAPSHOT_VERSION}"

#Remove backup files. Finally, commit the version number changes:
mvn versions:commit

echo "Verifying that the build is successful with the new version number."
mvn clean
mvn verify

echo ""
echo "Now you need commit and push your changes to github."
