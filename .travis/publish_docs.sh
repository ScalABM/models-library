#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Generating scaladoc...\n"
  cd ./gode-sunder
  sbt doc

  echo -e "..publishing scaladoc...\n"

  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/ScalABM/example-model gh-pages > /dev/null

  cd gh-pages

  # copy over the new docs
  mkdir -p ./docs/gode-sunder/latest
  cp -Rf ./target/scala-2.11/api/* ./docs/gode-sunder/latest

  # push to github!
  git add ./docs
  git commit -m "Latest docs for travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push origin gh-pages > /dev/null

  echo -e "Published scaladoc to gh-pages!\n"

fi
