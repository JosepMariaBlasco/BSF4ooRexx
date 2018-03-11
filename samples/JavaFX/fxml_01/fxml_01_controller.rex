#!/usr/bin/env rexx
/* This routine will be called from the Rexx code defined in the Button element in
   with the fx:id="button" the "onAction" attribute in the FXML Button definition */
::routine buttonClicked public
  slotDir=arg(arg())  -- note: last argument is the slotDir argument from BSF4ooRexx
  now=.dateTime~new -- time of invocation
  say now": arrived in routine 'buttonClicked' ..."
  /* RexxScript annotation fetches "label" from ScriptContext
     and makes it available as the Rexx variable "LABEL":  */
  /* @get(idLabel1) */
  say '... current value of label='pp(idLabel1~getText)
  idLabel1~text="Clicked at:" now         -- set text property
  say '...     new value of label='pp(idLabel1~getText)
  say
  ----- to be deleted ...
  bDebug=.false -- change to .true to learn about the ScriptContext bindings
  if bDebug=.true then
  do
      scriptContext=slotDir~scriptContext
        /* show the currently defined attributes in all ScriptContext's scopes   */
     say "getting all attributes from all ScriptContext's scopes..."
     dir=.directory~new
     dir[100]="ENGINE_SCOPE"
     dir[200]="GLOBAL_SCOPE"
     arr=.array~of(100,200)
     do sc over arr
         say "ScriptContext scope:" pp(sc) "("dir~entry(sc)"), available attributes:"
         bin=scriptContext~getBindings(sc)
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
  end