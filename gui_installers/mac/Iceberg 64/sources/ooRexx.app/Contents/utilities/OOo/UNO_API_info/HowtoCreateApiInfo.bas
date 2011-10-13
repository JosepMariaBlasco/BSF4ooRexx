' 2009-02-21, 2010-06-01, Rony G. Flatscher, demonstrates how to use "UNO_API_info.rxo" from OOo Basic
Sub testCreateApiInfo
   DIM sDispatchHelper AS object, xDispatchProvider AS object  ' objects
   DIM macroUrl, library, scriptName, langName, location       ' variants
   DIM args1(0) AS NEW com.sun.star.beans.PropertyValue 	      ' array of type PropertyValue
   DIM args2(1), options(6)                                    ' arrays of variants

   sDispatchHelper  =createUnoService("com.sun.star.frame.DispatchHelper") ' create DispatchHelper service
   xDispatchProvider=ThisComponent.CurrentController.Frame  ' get dispatch provider interface of current Desktop

   ' define Rexx dispatch target, library "wu_tools", script name "create_UNO_API_info.rex", location "share"
   location   ="share"              ' case sensitive, other possible values: "user" (current user), "application"
   libraryName="wu_tools"           ' case sensitive, name of the Rexx macro library
   scriptName ="UNO_API_info.rxo"   ' case sensitive, name of the Rexx script
   langName   ="ooRexx"             ' case sensitive, OOo name of the scripting language
    	' build 'macroUrl' string for the dispatcher
   macroUrl="vnd.sun.star.script:" & libraryName & "." & scriptName & "?language=" & langName & "&location=" & location


   '  ------ use one argument denoting an UNO object from the running program
   ' define one argument (an UNO object from the running program)
   ' remark: the array 'args1' is explicitly defined to be of type com.sun.star.beans.PropertyValue,
   '         hence its element is a PropertyValue object already
   args1(0).name ="arg1"            ' name of the PropertyValue
   args1(0).value=sDispatchHelper   ' value: UNO object to analyze

   ' dispatching to 'create_UNO_API_info.rxo' using an UNO object from the running program
   sDispatchHelper.executeDispatch(xDispatchProvider, macroUrl, "", 0, args1())


   '  ------ use one argument denoting an UNO object from the running program
   ' define options; create PropertyValue objects and assign them to the 'options' variant array
   options(0)=createProperty("NrOfLayers",           2)  ' 2="show two levels deep"
   options(1)=createProperty("View",                 1)  ' 1="view in writer"
   options(2)=createProperty("DocumentationSource",  1)  ' 1="use Internet" (base url)
   options(3)=createProperty("NumberingTypeLevel_1", 0)  ' 0="Alpha Uppercase"
   options(4)=createProperty("NumberingTypeLevel_2", 4)  ' 4="arabic"
   options(5)=createProperty("NumberingTypeLevel_3", 3)  ' 3="roman lower"
   options(6)=createProperty("FontName",             "DejaVu Sans Condensed")

   ' define two arguments (an UNO IDL string and formatting options
   ' create PropertyValue objects and assign them to the 'args2' variant array
   args2(0)=createProperty("arg1", "com.sun.star.frame.Desktop") ' an UNO IDL string
   args2(1)=createProperty("arg2", options)                      ' rendering options

   ' dispatching to 'create_UNO_API_info.rxo' using an UNO IDL string and rendering options
   sDispatchHelper.executeDispatch(xDispatchProvider, macroUrl, "", 0, args2())
End Sub


' utility function to ease creation of PropertyValue objects
Function createProperty (n AS string, v)
   prop=CreateObject("com.sun.star.beans.PropertyValue") ' create a PropertyValue object
   prop.name=n          ' assign name (a string)
   prop.value=v         ' assign value (a variant, i.e. can be any value)
   createProperty=prop  ' set this function's return value
end Function


' /*
'     ------------------------ Apache Version 2.0 license -------------------------
'        Copyright (C) 2009-2010 Rony G. Flatscher
'
'        Licensed under the Apache License, Version 2.0 (the "License");
'        you may not use this file except in compliance with the License.
'        You may obtain a copy of the License at
'
'            http://www.apache.org/licenses/LICENSE-2.0
'
'        Unless required by applicable law or agreed to in writing, software
'        distributed under the License is distributed on an "AS IS" BASIS,
'        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
'        See the License for the specific language governing permissions and
'        limitations under the License.
'     -----------------------------------------------------------------------------
' */
