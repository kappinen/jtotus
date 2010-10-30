#!/bin/bash


for i in `find . -name *.java`
do
wc -l $i
 num=`cat $i|wc -l | awk -F\  '{print $NF}'`
 let sum=$sum+$num
 let total_files=$total_files+1
done

echo "Files:" $total_files " Loc:" $sum
