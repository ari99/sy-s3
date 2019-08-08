#!/bin/bash

# This script creates a number of directorys and files.
# You can use it to create files to put on a scality local s3 server running in docker.
#
# Run using: 
# . ./fileGenerator.sh
# cd dataToSync
#
# Send data to local s3 running on focker:
# aws s3 sync . s3://mybucket --endpoint-url=http://localhost:8000
#
# Confirm data is on local s3:
# aws s3 ls s3://mybucket --endpoint-url=http://localhost:8000 --recursive
#
# SSH in:
# docker exec -it s3server_file bash
#

mkdir dataToSync

# Based on:
# http://stackoverflow.com/questions/4140822/creating-multiple-files-with-content-from-shell

for e in {a..z}
do
	mkdir "./dataToSync/$e"
	for d in {0..10}
	do
		mkdir "./dataToSync/$e/$d"
		echo "hello$e$d" > "./dataTosync/$e/$d.txt"
	for f in {a..f} {0..5}
		do
		    echo "hello$e$d$f" > "./dataTosync/$e/$d/$f.txt"
		done
	done
done

