#! /bin/bash

if [ "$#" -ne 2 ]; then
	echo "Too few arguments"
	echo "./run_lock_server interface port"
	exit 1
fi

java LockServer $@
