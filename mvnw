#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script
# ----------------------------------------------------------------------------

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-`pwd`}"
MAVEN_CONFIG=".mvn/wrapper"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/$MAVEN_CONFIG/maven-wrapper.jar"

wrapper_properties="$MAVEN_PROJECTBASEDIR/$MAVEN_CONFIG/maven-wrapper.properties"
distributionUrl=$(grep "^distributionUrl=" "$wrapper_properties" | cut -d= -f2-)

exec mvn "$@"
