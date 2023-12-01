#!/bin/bash
set -e -o pipefail

cd ..

git checkout main
git pull
mvn clean;

# check that we are not ahead or behind
if  ! [[ $(git status --porcelain -u no  --branch) == "## main...origin/main" ]]; then
    echo "";
    echo "There is something wrong with your git. It seems you are not up to date with main. Run git status";
    exit 1;
fi

# check that there are no uncomitted or untracked files
if  ! [[ $(git status --porcelain) == "" ]]; then
    echo "";
    echo "There are uncomitted or untracked files! Commit, delete or unstage files. Run git status for more info.";
    exit 1;
fi

mvn verify

MVN_CURRENT_SNAPSHOT_VERSION=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" pom_parent.xml)

MVN_VERSION_RELEASE="${MVN_CURRENT_SNAPSHOT_VERSION/-SNAPSHOT/}"

echo "Your current maven snapshot version is: '${MVN_CURRENT_SNAPSHOT_VERSION}'"
echo "Your maven release version will be: '${MVN_VERSION_RELEASE}'"

read -n 1 -srp "Press any key to continue (ctrl+c to cancel)"; printf "\n\n";

# set maven version
mvn versions:set -DnewVersion="${MVN_VERSION_RELEASE}"

#Remove backup files. Finally, commit the version number changes:
mvn versions:commit

echo "Run the following command to continue the release process:"
echo "./release.sh"


