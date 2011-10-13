#!/usr/bin/rexx
/* Name: rexx_version.rex
   Purpose: Returns the ooRexx bitness
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

exit(getRexxBitness())


::routine getRexxBitness

   "rexx -v 2>&1 | rxqueue"
   bitness=0
   do while queued()>0
      parse pull hint ":" val .
      if hint="Addressing Mode" then bitness=val
   end
   return bitness
