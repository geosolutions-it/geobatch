#!/bin/bash

for dir in `ls $1`; do
	echo "running in $1/$dir"
	for file in `ls $1/$dir`; do
		if [ -d "$1/$dir" ]; then
			case $file  in
				J4M*)
				echo "J4M :" $dir/$file
				fileM="$fileM $file"
				;;
				*)
				echo "J4D :" $dir/$file
				fileD="$fileD $file"
				;;
			esac		
		fi
	done
	if [ -n "$fileM" ]; then
		cd "$1/$dir";
		tar -cvjf "../J4M_"$dir".tar.bz2" $fileM;
		tar -cvjf "../J4D_"$dir".tar.bz2" $fileD;
		fileM="";
		fileD="";
		cd ../..;
	fi
done


