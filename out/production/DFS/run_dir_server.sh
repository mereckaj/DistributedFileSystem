#! /bin/bash

if [ "$#" -ne 2 ]; then
	echo "Too few arguments"
	echo "./run_dir_server interface port"
	exit 1
fi

java DirServer $@
