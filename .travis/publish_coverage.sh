#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

    echo -e "Generating code coverage reports.\n"
    cd ./gode-sunder
    sbt coveralls
    sbt coverageReport
    cd ..

    echo -e "Publishing coverage reports.\n"

    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"
    git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/ScalABM/example-model gh-pages > /dev/null

    cd gh-pages

    # get rid of old coverage stats
    git rm -rf ./coverage

    # copy over the new coverage stats
    mkdir -p ./coverage/gode-sunder
    cp -Rf ../target/scala-2.11/scoverage-report ./coverage/gode-sunder

    # push to github!
    git add -f .
    git commit -m "Lastest coverage report on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
    git push -fq origin gh-pages > /dev/null

    echo -e "Published coverage reports to gh-pages.\n"
fi