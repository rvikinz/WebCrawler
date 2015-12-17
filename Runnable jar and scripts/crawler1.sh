#!/bin/sh
# setting current directory
cd "$(dirname "$0")"

printf "Enter number of hopes:"
read NumHop

printf "Enter number of pages to download:"
read NumPagesDwnld

#printf "Enter Jar file URL path (up to Jar file name ):"
#read jarFilePath

printf "Enter Seed URL path:"
read seedURL

test -c seedURL

printf "Enter File path for document store:"
read docStore

printf "Enter Number of Threads:" 
read numThread

java -jar  multiThreadedCrawler.jar $NumHop $NumPagesDwnld $docStore $seedURL $numThread
#java -jar $jarFilePath $NumHop $NumPagesDwnld $docStore $seedURL $numThread

#java -jar /Users/vikashkumar/Documents/IRCrawler/multiThreadedCrawler.jar 0 0 /Users/vikashkumar/Documents/IR/Download /Users/vikashkumar/Documents/SeedURL/seedURL.txt