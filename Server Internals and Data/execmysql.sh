#!/bin/sh

date > lastrun.out

for i in `seq 1 6`;
do
  mysql -u"query" -p"antequ" findatable < script.sql
  sleep 9.1s #give it some breathing room at the end
done




