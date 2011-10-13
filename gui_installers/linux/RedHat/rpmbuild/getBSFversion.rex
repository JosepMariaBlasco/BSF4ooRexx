#!/bin/sh
getVersion()
::routine getVersion
	"grep .bsf4rexx~version /tmp/bsftemp/bsf4oorexx/BSF.CLS 2>&1 | rxqueue"
	do while (queued()>0)
		parse pull rest '~version="' version '.' release '"'
		if version <> "" then 
			CHAROUT("BSFVersion.txt", version,1)
			CHAROUT("BSFRelease.txt", release,1)
	end

