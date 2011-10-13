#!/usr/bin/rexx
/* Name: getBSFVersion.rex
   Purpose: Returns the current version of BSF4ooRexx
   Author: Juergen Hesse, Manuel Paar
   Version: 1.0

   ------------------- Apache Version 2.0 license --------------------
     Copyright (C) 2011 Juergen Hesse, Manuel Paar
 
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
 
           http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
     implied.
     See the License for the specific language governing permissions and
     limitations under the License.
   -------------------------------------------------------------------
*/

CHAROUT("BSFVersion.txt", getVersion(),1)

::routine getVersion
  'find /n ".bsf4rexx~version" BSF.CLS 2>&1 | rxqueue'
  do while queued()>0
    parse pull rest '~version="' version '"'
    if version <> "" then return version
  end
  return version
