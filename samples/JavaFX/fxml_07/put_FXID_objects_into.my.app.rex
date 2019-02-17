#!/usr/bin/env rexx
/*    author:     Rony G. Flatscher
      date:       2016-11-26, 2017-11-04
      purpose:    generic ooRexx program which stores the GLOBAL_SCOPE entries in .environment as a directory
                  with the name of the FXML-file it got invoked from;

      explanation: this program is invoked by the JavaFX FXMLLoader when loading a FXML document and
                   instantiating the JavaFX controls using the RexxScript support, which means that
                   a proper Rexx interpreter instance gets created in which this program executes;
                   to share data with other Rexx interpreter instances we use .environment (shared
                   among all Rexx interpreter instances) rather than .local (unique per Rexx interpreter
                   instance);

                   FXMLLoader will create a new RexxScriptEngine for each FXML document it processes!

                   FXMLLoader will put all JavaFX objects with a fx:id attribute into the ScriptContext's
                   Bindings for the global scope, such that we can fetch these objects from there and
                   save it in a directory named after the FXML location (file name) that defines them for
                   later retrieval by other Rexx programs

      changed:    2017-02-09, rgf: remove usage of .jsr223 as it is not needed anymore
                  2017-11-04, rgf: on debug show all available scopes, usually (SimpleScriptContext)
                                   the 100 (ENGINE_SCOPE) and 200 (GLOBAL_SCOPE) Bindings

      license:    Apache License 2.0
*/

parse source . . thisProg
thisProg=filespec("Name", thisProg)

   -- make sure global Rexx .environment has an entry MY.APP (a Rexx directory)
if \.environment~hasEntry("my.app") then           -- not there?
   .environment~setEntry("my.app", .directory~new) -- create it!

bDebug=(.my.app~bDebug=.true)    -- set debug mode
if bDebug then say .dateTime~new " ==> ---> arrived in Rexx program" pp(thisProg) "..."

slotDir=arg(arg())  -- get slotDir argument (BSF4ooRexx adds this as the last argument)
scriptContext=slotDir~scriptContext   -- get entry "SCRIPTCONTEXT"

GLOBAL_SCOPE=200
   -- "location" will have the URL for the FXML-file
url=scriptContext~getAttribute("location",GLOBAL_SCOPE)
fxmlFileName=filespec("name",url~getFile) -- make sure we only use the filename portion
dir2obj =.directory~new          -- will contain all GLOBAL_SCOPE entries
.my.app~setEntry(fxmlFileName,dir2obj) -- add to .My.APP

bindings=scriptContext~getBindings(GLOBAL_SCOPE)
keys=bindings~keySet~makearray   -- get the kay values as a Rexx array
do key over keys
   val=bindings~get(key)         -- fetch the key's value
   dir2obj ~setEntry(key,val)    -- save it in our directory
end

if bDebug then
do
   say "all GLOBAL_SCOPE attributes now available via:" pp(".MY.App~"fxmlFileName)
   say
      -- show all the currently defined attributes in all ScriptContext's scopes
   say "getting all attributes from all ScriptContext's scopes..."
   dir=.directory~new   -- known constant names
   dir[100]="ENGINE_SCOPE"
   dir[200]="GLOBAL_SCOPE"
   arr=scriptContext~getScopes~makearray  -- get all scopes, turn them into a Rexx array
   do sc over arr                --
       str="ScriptContext scope" pp(sc)
       if dir~hasEntry(sc) then str=str "("dir~entry(sc)")"
       say str", available attributes:"
       say
       bin=scriptContext~getBindings(sc)
       if bin=.nil then iterate  -- inexistent scope
       keys=bin~keySet           -- get kay values
       it=keys~makearray         -- get the keys as a Rexx array
       do key over it~sortWith(.CaselessComparator~new)  -- sort caselessly
          val=bin~get(key)       -- fetch the key's value
          str=""
          if val~isA(.bsf) then str="~toString:" pp(val~toString)
          say "  " pp(key)~left(35,".") pp(val) str
       end
       if sc<>arr~lastItem then say "-"~copies(89)
                           else say "="~copies(89)
   end
end

if bDebug then
do
   say .dateTime~new " <== <--- returning from program" pp(thisProg) "."
   say
end


/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2016-2017 Rony G. Flatscher

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