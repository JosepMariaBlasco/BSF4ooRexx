#!/usr/bin/env rexx
/* This is a simple demo of a Rexx (modelled after the JavaScript) script that
   uses the Java URL class to download some content from some URL. rgf, 2003-02-17, 2003-04-18

   2008-09-09, rgf, changed "registerBean" and "registerBeanStrict" to their synonyms
                    "new" and "newStrict" which better describe the semantics of
                    these subfunctions

   2010-02-10, rgf, fixed library name

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2003-2010 Rony G. Flatscher

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


if rxFuncQuery("BSF") then  /* no bsf4rexx support available yet, hence load Java */
do
   call rxFuncAdd "BsfLoadFuncs", "BSF4ooRexx", "BsfLoadFuncs"
   call BsfLoadFuncs    /* load all BSF-functions                    */
   call BsfLoadJava     /* load the JVM and set up BSF4Rexx-support  */
end

if      BsfInvokedBy()=1 then say "This Rexx program was invoked by Java!"
else if BsfInvokedBy()=2 then say "This Rexx program was invoked by Rexx, JVM loaded by Rexx!"
else                          say "No JVM present, we got troubles ..."


URL_ADDR = "http://www.ooRexx.org/";
say "connecting to:" URL_ADDR
url=bsf("newStrict", "", "java.net.URL", "String", URL_ADDR) /* create a java.net.URL instance */
content = bsf('invoke', url, "getContent")   /* get the content object, a <java.io.FilterInputStream> */
say "Downloading .. "

size=8*1024    /* use chunks of maximum 8 KB */
      /* "Array.class" and "byte.class" are pre-loaded on the Java side, hence usable already */
byteArray=bsf("invokeStrict", "Array.class", "newInstance", "object", "byte.class", "int", size)

len=""
do until len=-1
   len=bsf("invokeStrict", content, "read", "obj", byteArray, "int", 0, "int", size)   /* read the content into the byte array */
   if len>0 then
   do
      stringObject=bsf("newStrict", , "java.lang.String", "obj", byteArray)   /* create a Java String from the byte array */
      call charout , left( bsf("invoke", stringObject, "toString"), len)  /* get the string itself, use the read bytes as its length */
      call bsf "unregisterBean", stringObject      /* remove the String Object from the Java repository */
   end
end