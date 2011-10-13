#!/usr/bin/rexx
CHAROUT("BSFVersion.txt", getVersion(),1)
::routine getVersion
	"grep .bsf4rexx~version BSF4ooRexx_src/bsf4oorexx/BSF.CLS 2>&1 | rxqueue"
	do while (queued()>0)
		parse pull rest '~version="' version '"'
		if version <> "" then return version
	end 
	return version
