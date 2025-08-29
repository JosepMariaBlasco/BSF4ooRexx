#!/usr/bin/env rexx
/* This routine will be called from the Rexx code defined in the Button element in
   with the fx:id="button" the "onAction" attribute in the FXML Button definition */
::routine buttonClicked public
  slotDir=arg(arg())  -- note: last argument is the slotDir argument from BSF4ooRexx
  now=.dateTime~new -- time of invocation

  infoPrefix=pp("R".context~interpreter) pp("T".context~thread) pp("I".context~invocation)
  say infoPrefix now": arrived in routine 'buttonClicked' ..."

  /* RexxScript annotation fetches the Label object with the id "idLabel1" from
     the ScriptContext and makes it available as the Rexx variable "IDLABEL1":  */
  /* @get(idLabel1) */
  say infoPrefix '... current value of label='pp(idLabel1~getText)
  idLabel1~text="Clicked at:" now         -- set text property
  say infoPrefix '...     new value of label='pp(idLabel1~getText)
  say
  bDebug=.false -- change to .true to learn about the ScriptContext bindings
  if bDebug=.true then
      call dumpAllBindings '--> from buttonClicked()', slotDir~scriptContext

::routine dumpAllBindings
    use strict arg title, scriptContext
    /* show the currently defined attributes in the default ScriptContext's scopes   */
    say  title 'at' pp(.dateTime~new)':'
    say
    say "getting all attributes from all ScriptContext's scopes..."
    allScopes=scriptContext~getScopes  -- there may be any number of scopes

    do scope over allScopes      -- show Bindings for each scope
        say "ScriptContext scope:" pp(scope) getScopeTitle(scope)
        bin=scriptContext~getBindings(scope)
        if bin=.nil then iterate -- inexistent scope
        minWidth=31              -- minimum width for key column
        keys=bin~keySet          -- get kay values
        it=keys~makearray        -- get the keys as a Rexx array
        do key over it~sortWith(.CaselessComparator~new) -- sort keys caselessly
           val=bin~get(key)      -- fetch the key's value
           str=" " pp(key)~left(max(minWidth,key~length+2),".")":" pp(val)
           if key="location" then str=str "~toString="pp(val~toString)
           say str
        end
        say copies("-",86)
    end
    say copies("=",86)
    return

getScopeTitle: procedure
   parse arg scopeNumber
   if      scopeNumber=100 then return "[ENGINE_SCOPE]"
   else if scopeNumber=200 then return "[GLOBAL_SCOPE]"
   return ""

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
