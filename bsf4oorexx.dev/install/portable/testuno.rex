#!/usr/bin/env rexx
/*
   Name:    testuno.rex (copy of bsf4oorexx/samples/OOo/listPathSettings.rxo with uno.getDefinition demonstration)
   Author:  Rony G. Flatscher
   Date:    2022-02-07
   Note:    the reference implementation of the UNO support in BSF4ooRexx is
            OpenOffice.org (OOo), a.k.a. Apache OpenOffice (AOO); LibreOffice (LO)
            is a fork of OOo and can therefore be addressed via UNO as well
   Remarks: test environment for accessing UNO (demonstrate the service "com.sun.star.util.PathSettings")
   License:
    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010-2022 Rony G. Flatscher

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
xContext = UNO.connect()            -- connect to UNO server and retrieve the XContext object
XMcf = xContext~getServiceManager   -- retrieve the XMultiComponentFactory
oPathSettings=XMcf~createInstanceWithContext("com.sun.star.util.PathSettings", xContext)
xpaths=oPathSettings~XPropertySet   -- get its XPropertySet interface
paths=xpaths~getPropertySetInfo~getProperties   -- get the sequence of properties

do i=1 to paths~items               -- iterate over all properties
   name=paths[i]~name               -- get the Property's name
   call printPathSetting i, name, xpaths~getPropertyValue(name)
end

say
say "Get information about the composition of the 'oPathSettings' UNO object"
say """say ppd(oPathSettings~uno.getDefinition)"" yields (cf. ""refcardOOo.pdf""):"
say
say ppd(oPathSettings~uno.getDefinition)     -- cf. "refcardOOo.pdf"

/* UNO.CLS adds public routines and public classes to ease programming AOO/LO via UNO.
   It uses the Java APIs of UNO and therefore requires BSF.CLS, such that the ooRexx-Java
   bridge gets automatically included and available to us as well.
*/
::requires UNO.CLS   -- get the BSF4ooRexx UNO (AOO/LO) support (includes BSF.CLS)

   /* print the PathSetting name with its value(s) */
::routine printPathSetting
  use arg i, name, propValue
  tmpStr=i~right(3)":" pp(name)
  resArr=processPathUrls(propValue)
  say tmpStr || detTabs(tmpStr) pp(resArr[1])
  do k=2 to resArr~items
     say "090909"x pp(resArr[k])
  end
  return

   /* procedure local to routine printPathSettings()  */
   /* make sure that the path setting's value is indented at least three tabs deep  */
detTabs: procedure
  parse arg string
  len=length(string)
  if      len<8  then return "090909"x
  else if len<16 then return "0909"x
  else if len<24 then return "09"x
  else    return ""

   /* procedure local to routine printPathSettings()  */
   /* take care of special case that multiple paths are encoded in a single
      value (instead of defining them as an array of java.lang.String;
      the URL-style will delimit different paths with the semi-colon char (;)
      on all platforms; will return a Rexx array of strings representing the
      defined URLs rendered to the operating system's file system format   */
processPathUrls: procedure
   use arg propValue
   resArr=.array~new
   if propValue~string~left(18)=="[Ljava.lang.String" then
   do
      if propValue~items=0 then     -- empty array
         resArr~append("")
      else
      do k=1 to propValue~items
         resArr~appendAll(convertPathUrl(propValue[k]))
      end
   end
   else                             -- value is a string already (representing single path)
      resArr~appendAll(convertPathUrl(propValue))
   return resArr

   /* procedure local to routine printPathSettings()  */
   /* parse string, could be that more than one path is encoded and delimited by semi-colon,
      return an array of converted strings */
convertPathUrl: procedure
   parse arg str
   arr=.array~new
   if str="" then       -- path is explicitly an empty string
      arr~append(str)
   else
   do while str<>""
      parse var str before ";" str     -- parse using ";" as delimiter
      arr~append(uno.convertFromUrl(before)) -- cf. "refcardOOo.pdf"
   end
   return arr