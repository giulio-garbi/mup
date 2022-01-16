#!/bin/bash

mongorestore --drop aair_dump
java -jar jps.jar makedb
java -jar ts.jar makedb
java -jar tms.jar makedb