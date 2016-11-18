started=.dateTime~new   -- take the date and time
/*@showsource*/   /* the previous Rexx script annotation will cause this script's source code to be shown */
use arg slotDir         -- fetch the slotDir argument (BSF4ooRexx adds this as the last argument at the Java side)
scriptContext=slotDir~scriptContext -- get the ScriptContext from the slotDir (last) argument

parse source s
say "just arrived at" pp(started)": parse source ->" pp(s)

   -- add an attribute to the ScriptContext's GLOBAL_SCOPE Bindings, used for "labelStartTime" in the fxml-document
.JSR223~setAttribute(scriptContext, "rexxStarted", "Rexx started at:" started~string, .jsr223~global_scope)

parse version v         -- get Rexx version, display it in the "rexxInfo" label
.JSR223~setAttribute(scriptContext, "rexxInfo", "Rexx version:" v, .jsr223~global_scope)

   -- set attribute at ENGINE_SCOPE (visible for this script engine only):
.JSR223~setAttribute(scriptContext, "title", "Jahr:")

   -- set attribute at global scope (visible for all script engines):
.JSR223~setAttribute(scriptContext, "count", "1", .jsr223~global_scope)



/* *************************************************************************************************** */
/* --------------------------------------------------------------------------------------------------- */
/* This routine will be called from the Rexx code defined with the "onAction" event attribute; cf.
   the JavaFX control with the id "label" in the fxml document    */
::routine klickButtonAction public
    use arg slotDir  -- fetch the slotDir argument (BSF4ooRexx adds this as the last argument at the Java side)
    scriptContext=slotDir~scriptContext   -- get the slotDir (the last) argument, get the entry "SCRIPTCONTEXT"

    say center(" arrived in public Rexx routine klickButtonAction() ", 79, "-")

       /* the following Rexx script annotation will incorporate the attributes as local
          Rexx variables which can be used immediately thereafter from Rexx:  */
    /*@get("rexxInfo label count event title")*/

      /* changing the attribute that gets constantly updated (once we return from
         this event handler) thanks to the FXMLLoader */
    rexxInfo="Updated from public Rexx routine klickButtonAction()."
    if count//2=0 then rexxInfo=rexxInfo~reverse   -- if even, reverse the current text
       /* the following Rexx script annotation will update the value of an existing
          attribute in one of the ScriptContext's scopes to the current value of the
          Rexx variable:   */
    /*@set("rexxInfo")*/   -- update the attribute with the Rexx variable's current (new) value

       /* show the currently defined attributes in all ScriptContext's scopes   */
    say
    say "getting all attributes from all ScriptContext's scopes..."
    dir=.directory~new
    dir[.jsr223~engine_scope]="ENGINE_SCOPE"
    dir[.jsr223~global_scope]="GLOBAL_SCOPE"
    arr=.array~of(.jsr223~engine_scope, .jsr223~global_scope)
    do sc over arr
        -- bin=getBindings(scriptContext,sc)
        say "ScriptContext scope:" pp(sc) "("dir~entry(sc)"), available attributes:"
        say
        bin=.jsr223~getBindings(scriptContext,sc)
        if bin=.nil then iterate -- inexistent scope
        keys=bin~keySet          -- get kay values
        it=keys~makearray        -- get the keys as a Rexx array
        do key over it~sortWith(.CaselessComparator~new) -- sort keys (attributes) caselessly
           val=bin~get(key)      -- fetch the key's value
           str=""
           if val~isA(.bsf) then str="~toString:" pp(val~toString)
           say "  " pp(key)~left(35,".") pp(val) str
        end
        say "-"~copies(30)
    end
    say

      -- access the "label" JavaFX label, change its text
    label~setText(title .dateTime~new~string "(count #" count")")

      /* now explicitly update the count attribute in global scope bindings; if the
         attribute does not exist, it would be created        */
    .JSR223~setAttribute(scriptContext, "count", count+1,  .jsr223~global_scope)


