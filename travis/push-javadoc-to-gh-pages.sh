#!/bin/bash
#
# Licensed to Apereo under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Apereo licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a
# copy of the License at the following location:
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

invokeJavadoc=false
invokeDoc=false

# Only invoke the javadoc deployment process
# for the first job in the build matrix, so as
# to avoid multiple deployments.

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  case "${TRAVIS_JOB_NUMBER}" in
       *\.1) 
  		echo -e "Invoking auto-doc deployment for Travis job ${TRAVIS_JOB_NUMBER}"
  		invokeJavadoc=true;
  		invokeDoc=true;;
  esac
fi 

invokeJavadoc=false

if [ "$invokeDoc" == true ]; then

  echo -e "Copying project documentation over ...\n"
  cp -R cas-server-documentation $HOME/docs-latest

fi

if [ "$invokeJavadoc" == true ]; then

  echo -e "Started to publish latest Javadoc to gh-pages...\n"

  echo -e "Invoking Maven to generate the project site...\n"
  mvn site site:stage -q -ff -B -P nocheck -Dversions.skip=false
  
  echo -e "Copying the generated docs over...\n"
  cp -R target/staging $HOME/javadoc-latest

fi

if [[ "$invokeJavadoc" == true || "$invokeDoc" == true ]]; then

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  echo -e "Cloning the gh-pages branch...\n"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/Jasig/cas gh-pages > /dev/null

  cd gh-pages

  if [ "$invokeDoc" == true ]; then
    echo -e "Removing previous documentation...\n"
    git rm -rf ./development > /dev/null

    echo -e "Creating development directory...\n"
    test -d "./development" || mkdir -m777 -v ./development

    echo -e "Copying new docs...\n"
    cp -Rf $HOME/docs-latest ./development
  fi

  if [ "$invokeJavadoc" == true ]; then
    echo -e "Removing previous Javadocs...\n"
    git rm -rf ./development/javadocs > /dev/null

    echo -e "Creating development directory...\n"
    test -d "./development" || mkdir -m777 -v ./development

    echo -e "Creating javadocs directory...\n"
    test -d "./development/javadocs" || mkdir -m777 -v ./development/javadocs

    echo -e "Copying new Javadocs...\n"
    cp -Rf $HOME/javadoc-latest ./development/javadocs
  fi

  echo -e "Adding changes to the index...\n"
  git add -f . > /dev/null

  echo -e "Committing changes...\n"
  git commit -m "Published documentation to [gh-pages]. Build $TRAVIS_BUILD_NUMBER" > /dev/null

  echo -e "Pushing upstream to origin...\n"
  git push -fq origin gh-pages > /dev/null

  echo -e "Successfully published documenetation to [gh-pages] branch.\n"

fi
