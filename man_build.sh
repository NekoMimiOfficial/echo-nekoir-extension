#!/bin/zsh

./gradlew assembleRelease -Dandroid.injected.signing.store.file="$HOME/.keystore/key.b64" -Dandroid.injected.signing.store.password=$(cat $HOME/.keystore/pass.txt) -Dandroid.injected.signing.key.alias=NekoKey -Dandroid.injected.signing.key.password=$(cat $HOME/.keystore/pass.txt)
