#!/bin/bash

PATTERN="ssh -fNL [0-9]*:127\.0\.0\.1:[0-9]* wydev"

pids=$(ps -ef | grep "$PATTERN" | grep -v grep | awk '{print $2}')

if [ -z "$pids" ]; then
    echo "No ssh -fNL wydev processes found."
    exit 0
fi

echo "Will kill the following processes:"
echo "$pids"

kill $pids && echo "Killed successfully." || echo "Failed to kill some processes."
