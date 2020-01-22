#!/usr/bin/env bash
CACHEDIR=/tmp

FREE=$(df -k --output=avail "$CACHEDIR" | tail -n1)
LIMITSIZE=20000000 # 20GB

if [ $FREE -lt $LIMITSIZE ]; then
  find "$CACHEDIR" -maxdepth 1 -mindepth 1 -type d -mmin +240 -print0 |
    while IFS= read -d '' -r dir; do
        rm -rf "$dir"

        # recording each delete in the log
        current_date_time="`date +%Y%m%d%H%M%S`";
        echo "Deleting $dir at $current_date_time"
    done
fi