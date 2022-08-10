#!/usr/bin/env rexx
/*
   Name:    testbsf4oorexx.rex
   Purpose: test BSF4ooRexx functionality, give version information
   Usage:   rexxj.sh testbsf4oorexx.rex
            rexx testbsf4oorexx.rex (if environment is already set for BSF4ooRexx)
   Date:    2022-02-01
   Needs:   BSF4ooRexx environment set up properly
   license:
    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2022 Rony G. Flatscher

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
say .bsf4rexx~display.version -- show version information

::requires "BSF.CLS" -- get ooRexx-Java bridge