#!/bin/sh
if [ "$#" -ne 3 ]; then
 echo "
Generates git log report between two tags:
USAGE:
$0 {START_TAG} {STOP_TAG|HEAD} {MESSAGE DESCRIPTION}"
exit 1
fi

echo "What's new?
===========

$3

Where can I get it?
===================

* Home:
  http://geobatch.geo-solutions.it/
* Download:
  http://geobatch.geo-solutions.it/download/latest/
* Documentation:
  http://geobatch.geo-solutions.it/download/latest/doc/

Who contributed to this release?
================================

* List authors of this release"
git log $1..$2 --pretty="format:* %aN" | sort | uniq

echo "What changed in detail?

=======================
FIXES IN MAIN MODULES:
=======================
"
for i in core dao fsm gui web users file-catalog ftp-server; do
git log $1..$2 --no-merges --pretty=oneline --reverse --grep fix --grep improve --grep add --format='* %s' src/$i
done

echo "
=======================
CHANGES IN ACTIONS
=======================
"
git log $1..$2 --no-merges --pretty=oneline --reverse --grep fix --grep improve --grep add --format='* %s' src/actions

echo "
=======================
CHANGES IN SERVICES
=======================
"
git log $1..$2 --no-merges --pretty=oneline --reverse --grep fix --grep improve --grep add --format='* %s' src/services

echo "
=======================
CANGES IN TOOLS
=======================
"
git log $1..$2 --no-merges --pretty=oneline --reverse --grep fix --grep improve --grep add --format='* %s' src/tools

echo "
=======================
CHANGES IN DOCUMENTATION
=======================
"
git log $1..$2 --no-merges --pretty=oneline --reverse --grep fix --grep improve --grep add --format='* %s' src/doc/source

exit 0
