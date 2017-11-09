    slotDir=arg(arg())      -- last argument is the slotDir argument, added by BSF4ooRexx
    started=.dateTime~new   -- get current date and time
    parse source s          -- get the source information and show it
    say "just arrived at" pp(started)": parse source ->" pp(s)

    sc=slotDir~scriptContext -- get the ScriptContext entry from slotDir
       -- add the attribute "rexxStarted" to the ScriptContext's GLOBAL_SCOPE Bindings
    sc~setAttribute("rexxStarted", "Rexx started at:" started~string, sc~global_scope)
    parse version v         -- get Rexx version, display it in the "rexxInfo" label
    sc~setAttribute("rexxInfo", "Rexx version:" v, sc~global_scope)
       -- set attribute at ENGINE_SCOPE (visible for this script engine only):
    sc~setAttribute("title", "--> -> >", sc~engine_scope)
       -- set attribute at global scope (visible for all script engines):
    sc~setAttribute("count", "1", sc~global_scope)

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

        /* show the currently defined attributes in the default ScriptContext's scopes   */
        say "getting all attributes from all ScriptContext's scopes..."
        do sc over .array~of(100, 200)
            say "ScriptContext scope:" pp(sc) pp(iif(sc=100,'ENGINE','GLOBAL')"_SCOPE")":"
            bin=scriptContext~getBindings(sc)
            if bin=.nil then iterate -- inexistent scope
            keys=bin~keySet          -- get kay values
            it=keys~makearray        -- get the keys as a Rexx array
            do key over it~sortWith(.CaselessComparator~new) -- sort keys caselessly
               val=bin~get(key)      -- fetch the key's value
               str=" " pp(key)~left(31,".") pp(val)
               if key="location" then str=str "~toString="pp(val~toString)
               say str
            end
            if sc=100 then say "-"~copies(86); else say "="~copies(86)
        end
          -- change the text of idLabel
        idLabel~setText(title .dateTime~new~string "(count #" count")")
        count+=1            -- increase counter
        /* @set(count) */   -- save it in the ScriptContext bindings
        say