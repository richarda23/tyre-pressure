#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Gradle start up script for POSIX compatible shells
# (sh, dash, ksh, zsn, bash, etc.)
#
# Important for running:
#   - You need to generate the gradle-wrapper.jar before using this script.
#   - Open the project in Android Studio, or run: gradle wrapper --gradle-version 7.3.3

set -e
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
APP_NAME="Gradle"
APP_BASE_NAME="$(basename "$0")"

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" \
    "-classpath" "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
