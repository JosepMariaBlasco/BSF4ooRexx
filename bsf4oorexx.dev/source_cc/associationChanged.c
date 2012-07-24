/*
   purpose: Program to notify the shell that file associations have changed, hence
            the icons may need to be refreshed.

   author:  Rony G. Flatscher, WU

   date:    2010-06-30

   license:

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2010 Rony G. Flatscher

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-----------------------------------------------------------------------------

 */

#include <windows.h>
#include <stdio.h>

#include <ShlObj.h>     // get the constant definitions


int main( int argc, char *argv[] )
{
    if (argc>1)
    {
        fprintf(stdout, "%s: notify shell that file associations have changed. [rgf, WU, 2010-06-30; open-source license: Apache License (AL) 2.0]\n",
                        argv[0]);
        return 0;
    }

    // notify Shell that icons have changed; available from Windows XP on
    SHChangeNotify( SHCNE_ASSOCCHANGED,
                    SHCNF_IDLIST,
                    NULL, NULL);

   return 0;
}

// to compile:            cl shell32.lib associationChanged.c
