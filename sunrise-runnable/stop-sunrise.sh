#!/bin/bash
kill `ps -ef | grep sunrise.jar | grep -v grep | awk '{ print $2 }'`