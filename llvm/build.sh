#!/usr/bin/env bash
cd "$(dirname "$0")"

COMMAND="clang -fembed-bitcode -fPIC -shared -O1 -g -I $JAVA_HOME/languages/llvm/include main.c -o ../src/main/resources/main"

jq -n --arg command "$COMMAND" --arg pwd "$PWD" '[ { directory: $pwd, command: $command, file: "main.c" } ]' > compile_commands.json

eval "$COMMAND"
