#!/bin/bash

pass=$(cat "/home/nekomimi/.keystore/pass.txt")
echo "Using key: $pass to assembleRelease"

./gradlew assembleRelease \
    -Pandroid.injected.signing.store.file="/home/nekomimi/.keystore/my-release-key.keystore" \
    -Pandroid.injected.signing.store.password="$pass" \
    -Pandroid.injected.signing.key.alias="my-app-key" \
    -Pandroid.injected.signing.key.password="$pass"
