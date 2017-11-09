/*    author:     Rony G. Flatscher
      date:       2016-11-26
      purpose:    generic ooRexx program which stores the GLOBAL_SCOPE entries in a Rexx
                  directory under the name of the FXML-file that invoked this program in
                  .environment~my.app (a Rexx directory, which will be created, if it does
                  not exist yet)

      explanation: this program is invoked by the JavaFX FXMLLoader when loading a FXML
                  document and instantiating the JavaFX controls using the RexxScript
                  support, which means that a proper Rexx interpreter instance gets
                  created in which this program executes; to share data with other Rexx
                  interpreter instances we use .environment (shared among all Rexx
                  interpreter instances) rather than .local (unique per Rexx interpreter
                  instance);

                  FXMLLoader will create a new RexxScriptEngine for each FXML document
                  it processes!

                  FXMLLoader will put all JavaFX objects with a fx:id attribute into the
                  ScriptContext's GLOBAL_ENGINE Bindings, such that we can fetch these
                  JavaFX objects from there and save it in a directory named after the
                  FXML location (file name) that defines them for later retrieval by
                  other Rexx programs

      changed:    2017-02-09, rgf: remove usage of .jsr223 as it is not needed anymore
                  2017-11-08, rgf: pretty-print a little bit
*/

parse source . . thisProg
thisProg=filespec("Name", thisProg)

     -- make sure a directory entry ".environment~my.app" exits, if not, create it
if \.environment~hasEntry("my.app") then
   .environment~setEntry("my.app", .directory~new)

bDebug=(.my.app~bDebug=.true) -- get debug mode, if set show what we do

if bDebug then
do
   say
   say pp(.dateTime~new) "==> ---> arrived in Rexx program '"thisProg"' ..."
end

slotDir=arg(arg())   -- fetch the slotDir argument (always last argument)
scriptContext=slotDir~scriptContext   -- get the entry "SCRIPTCONTEXT"

if bDebug then
do
   tab="09"x
     -- make sure we have access to 'rgf_util2.rex' (supplied via BSF4ooRexx)
   .context~package~addPackage(.package~new('rgf_util2.rex'))
   call dump2 slotDir, "slotDir"
   if slotDir~hasEntry("REXXCOMPILEDSCRIPT") then
      rse=slotDir~RexxCompiledScript~getRexxScriptEngine
   else
      rse=slotDir~RexxScriptEngine

   if rse<>.nil then
      say tab "---> RexxEngine:" pp(rse) "--->" "BSFEngine:" pp(rse~getBSFRexxEngine) -
              "---> rii_id:" pp(rse~getBSFRexxEngine~get_rii_id) "<---"
end

  -- get and save all entries in GLOBAL_SCOPE in .my.app, use 'location' as name
engine_scope=scriptContext~ENGINE_SCOPE   -- constant, value: 100
global_scope=scriptContext~GLOBAL_SCOPE   -- constant, value: 200
url= scriptContext~getAttribute("location", global_scope)   -- get FXML URL (a file)
fxmlFileName=filespec("name",url~getFile) -- get filename, remove path, if any
if bDebug then
   say "location:" pp(url~toString) "fxmlFileName:" pp(fxmlFileName)

dir2obj =.directory~new         -- the directory to contain all GLOBAL_SCOPE entriess
.my.app~setEntry(fxmlFileName, dir2obj)

bindings=scriptContext~getBindings(global_scope)   -- get the GLOBAL SCOPE Bindings
keys=bindings~keySet~makearray   -- get all the kay values as a Rexx array
do key over keys              -- iterate over keys
   val=bindings~get(key)      -- fetch the key's value
   dir2obj ~setEntry(key,val) -- save it in our directory
end

if bDebug then    -- dump ScriptContext's engine_scope and global_scope Bindings
do
      /* show the currently defined attributes in all ScriptContext's scopes   */
   say "iterating over all attributes from ScriptContext's scopes..."
   nameDir=.directory~new
   nameDir[engine_scope]="ENGINE_SCOPE"
   nameDir[global_scope]="GLOBAL_SCOPE"
   arr=.array~of(100,200)
   arr=ScriptContext~getScopes~makearray
   lastItem=arr~lastItem
   do sc over arr
      say
      str="ScriptContext scope:" pp(sc)
      if nameDir~hasEntry(sc) then str=str "("nameDir~entry(sc)")"
      say str", available attributes:"
      say
      bin=scriptContext~getBindings(sc)
      if bin=.nil then iterate -- inexistent scope
      keys=bin~keySet          -- get kay values
      it=keys~makearray        -- get the keys as a Rexx array
      do key over it~sortWith(.CaselessComparator~new) -- sort keys caselessly
         val=bin~get(key)      -- fetch the key's value
         str=""
         if val~isA(.bsf) then str="~toString:" pp(val~toString)
         say "  " pp(key)~left(35,".") pp(val) str
      end
      if sc<>lastItem then say "-"~copies(89)
                      else say "="~copies(89)
   end
end

if bDebug then
do
   say pp(.dateTime~new) "<== <--- returning from program '"thisProg"'."
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
