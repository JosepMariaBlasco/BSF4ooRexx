/*    author:     Rony G. Flatscher
      date:       2016-11-26
      purpose:    generic ooRexx program which stores the GLOBAL_SCOPE entries in .local as a directory
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
*/
    say .dateTime~new " ==> ---> arrived in Rexx program 'Put_FXID_Objects_Into.my.app.rex' ..."

    use arg slotDir  -- fetch the slotDir argument (BSF4ooRexx adds this as the last argument at the Java side)
    scriptContext=slotDir~scriptContext   -- get the slotDir (the last) argument, get the entry "SCRIPTCONTEXT"

         -- make sure we have a directory with the environment symbol ".my.app" available to everyone which
         -- is to receive information about those JavaFX objects that have a unique "fx:id" value;
    if \.environment~hasEntry("my.app") then
       .environment~setEntry("my.app", .directory~new)

call "rgf_util2.rex"
call dump2 slotDir, "slotDir"
if slotDir~hasEntry("REXXCOMPILEDSCRIPT") then
   rse=slotDir~RexxCompiledScript~getRexxScriptEngine

else
   rse=slotDir~RexxScriptEngine

say "---> RexxEngine:" pp(rse) "<---" "BSFEngine:" pp(rse~getBSFRexxEngine) "rii_id:" pp(rse~getBSFRexxEngine~get_rii_id) "<---"

if slotDir~hasEntry("RexxCompiledScript") then
    rse=rse~toString "(via RexxCompiledScript entry)"

    -- globalBindings=.jsr223~getBindings(scriptContext,sc)
    -- say globalBindings~getAttribute("location")
    url= scriptContext~getAttribute("location", .jsr223~global_scope)
    say "location:" pp(url~toString) "getFile:" pp(filespec("name",url~getFile)) -- make sure we get the file-name only
    fxmlFileName=filespec("name",url~getFile)      -- get filename, make sure we get unqualified file-name
    dir2obj =.directory~new         -- all GLOBAL_SCOPE entries, mapping uppercase attribute names to Java-values
    .my.app~setEntry(fxmlFileName, dir2obj)

     bindings=.jsr223~getBindings(scriptContext,.jsr223~global_scope)  -- get the Bindings for the global scope
     keys=bindings~keySet~makearray -- get the kay values as a Rexx array
     do key over keys
        val=bindings~get(key)       -- fetch the key's value
        dir2obj ~setEntry(key,val)  -- save it in our directory
if val=.nil then
do
   say "---> key:" pp(key) "val: " pp(val)
   val2=dir2obj~entry(key)
   say "<--- key:" pp(key) "val2:" pp(val2)
end
else
do
   say "---> key:" pp(key) "val: " pp(val) "rii_id="pp(val~rii_id) "val~toString: " pp(val~toString)  "val~getClass~toString: " pp(val~getClass~toString)
   val2=dir2obj~entry(key)
   say "<--- key:" pp(key) "val2:" pp(val2) "rii_id="pp(val2~rii_id)  "val2~toString:" pp(val2~toString) "val2~getClass~toString:" pp(val2~getClass~toString)
end
say
     end


       /* the following Rexx script annotation will incorporate the attributes as local
          Rexx variables which can be used immediately thereafter from Rexx:  */
    /*@ get("rexxInfo label count event title")*/

      /* changing the attribute that gets constantly updated (once we return from
         this event handler) thanks to the FXMLLoader */
--    rexxInfo="Updated from public Rexx routine 'klickButtonAction'."
--    if count//2=0 then rexxInfo=rexxInfo~reverse   -- if even, reverse the current text
       /* the following Rexx script annotation will update the value of an existing
          attribute in one of the ScriptContext's scopes to the current value of the
          Rexx variable:   */
    /*@ set("rexxInfo")*/   -- update the attribute with the Rexx variable's current (new) value

       /* show the currently defined attributes in all ScriptContext's scopes   */
    say "getting all attributes from all ScriptContext's scopes..."
    dir=.directory~new
    dir[.jsr223~engine_scope]="ENGINE_SCOPE"
    dir[.jsr223~global_scope]="GLOBAL_SCOPE"
    arr=.array~of(.jsr223~engine_scope, .jsr223~global_scope)
    do sc over arr
        say "ScriptContext scope:" pp(sc) "("dir~entry(sc)"), available attributes:"
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
        say "-"~copies(79)
    end

      -- access the "label" JavaFX Label, change its text
--    label~setText(title .dateTime~new~string "(count #" count")")

      /* now explicitly update the count attribute in global scope bindings; if the
         attribute does not exist, it would be created        */
--    .JSR223~setAttribute(scriptContext, "count", count+1,  .jsr223~global_scope)
    say .dateTime~new " <== <--- returning from program 'Put_FXID_Objects_Into.my.app.rex'."
    say


