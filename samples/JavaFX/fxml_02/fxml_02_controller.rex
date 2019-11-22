#!/usr/bin/env rexx
slotDir=arg(arg())      -- last argument is the slotDir argument, added by BSF4ooRexx
started=.dateTime~new   -- get current date and time
parse source s          -- get the source information and show it
say "just arrived at" pp(started)": parse source ->" pp(s)

sc=slotDir~scriptContext -- get the ScriptContext entry from slotDir

call dumpAllBindings '--> from prolog code (1) before adding entries', sc
   -- add the attribute "rexxStarted" to the ScriptContext's GLOBAL_SCOPE Bindings
sc~setAttribute("rexxStarted", "Rexx started at:" started~string, sc~global_scope)
parse version v         -- get Rexx version, display it in the "rexxInfo" label
sc~setAttribute("rexxInfo", "Rexx version:" v, sc~global_scope)
   -- set attribute at ENGINE_SCOPE (visible for this script engine only):
sc~setAttribute("title", "--> -> >", sc~engine_scope)
   -- set attribute at global scope (visible for all script engines):
sc~setAttribute("count", "1", sc~global_scope)
call dumpAllBindings '--> from prolog code (2) AFTER adding entries', sc

/* ---------------------------------------------------------------------------------- */
/* This routine will be called from the Rexx code defined with the "onAction" event
   attribute; cf.    the JavaFX control with the id "idButton" in the fxml_02.fxml    */
::routine klickButton public
    use arg slotDir                     -- fetch the slotDir argument
    scriptContext=slotDir~scriptContext -- get the slotDir entry
    /* @get( idLabel count title ) */

    rexxInfo="Updated from public Rexx routine 'klickButton'."
    if count//2=0 then rexxInfo=rexxInfo~reverse   -- if even, reverse the current text
    /* @set( rexxInfo ) */   -- update the "rexxInfo" attribute, will auto update label

    call dumpAllBindings '--> from klickButton()', scriptContext
      -- change the text of idLabel
    idLabel~setText(title .dateTime~new~string "(count #" count")")
    count+=1            -- increase counter
    /* @set(count) */   -- save it in the ScriptContext bindings
    say


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

