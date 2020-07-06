#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2020-07-05  (started: 2020-06-23)
   changes: --

   purpose: demonstrate - TableView with cells containing text and graphics
                        - Setting TableView cell to null as opposed to removing the cell
                        - Enabling/disabling menu options
                        - ContextMenu and demonstrating an ooRexx routine to serve as the callback
                          target from Java right before the context menu is displayed to allow for
                          dis/enabling the menu items to match the File's menu items
                        - Playing AudioClip (works on all operating systems) if clicking on a
                          displayed cell that is not backed by data
                        - Animation

   additional infos on the web:

         <https://www.educba.com/javafx-listview/>, 2020-06-15
         <https://docs.oracle.com/javafx/2/ui_controls/list-view.htm>, 2020-06-21

         table-View and edit control infos (2020-06, 2020-07):
            DatePicker: <https://gist.github.com/james-d/9776485>
                        <https://www.programcreek.com/java-api-examples/?code=vibridi%2Ffxutils%2Ffxutils-master%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvibridi%2Ffxu%2Finput%2FDatePickerTableCell.java#>
                        <https://stackoverflow.com/questions/22637292/javafx-tableview-with-custom-cell-datepicker-oneditcommit-is-not-invoked>
            Color Picker:
            <https://info.michael-simons.eu/2014/10/27/custom-editor-components-in-javafx-tablecells/>


   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx tpgm_demoTableView.rex
         rexxj.{cmd|sh} tpgm_demoTableView.rex

   needs: ooRexx 5.0 or higher, and BSF4ooRexx 6.00 or highter
*/

   -- make sure we run this program only on ooRexx 5.0 or higher
parse version "_" majorVersion "."
if majorVersion<5 then
do
   say "this program needs at least ooRexx 5.0 to run, aborting"
   exit -1
end

   -- make sure we change the current directory to the one this Rexx program resides in
parse source  . . pgm   -- get fully qualified path to this program
   -- change current directory such that relatively addressed resources (fxml-file) can be found
call directory filespec('L', pgm)

.environment~my.app=.directory~new  -- directory to contain objects relevant to this application
.my.app~bDebug=.false   /* if set to .true, "put_FXID_objects_into.my.app.rex" will show
                           all entries in ScriptContext Bindings on the console           */

   -- import JavaFX classes that we may use more often in different parts of the program
call bsf.import    "javafx.fxml.FXMLLoader",                      "fx.FXMLLoader"
call bsf.import    "javafx.scene.Scene",                          "fx.Scene"

call bsf.import    "javafx.scene.image.Image",                    "fx.Image"
call bsf.import    "javafx.scene.image.ImageView",                "fx.ImageView"

call bsf.import    "javafx.collections.FXCollections",            "fx.FXCollections"
call bsf.loadClass "javafx.util.Callback",                        "fx.Callback"
call bsf.loadClass "javafx.util.Duration",                        "fx.Duration"

call bsf.import    "javafx.beans.property.SimpleStringProperty",  "fx.SimpleStringProperty"
call bsf.import    "javafx.beans.property.SimpleIntegerProperty", "fx.SimpleIntegerProperty"

call bsf.loadClass "javafx.scene.control.ContentDisplay",         "fx.ContentDisplay"
call bsf.loadClass "javafx.scene.input.KeyEvent",                 "fx.KeyEvent"

   -- create Rexx object that will control the application
rexxApp=.RxMainApplication~new
.my.app~mainApp=rexxApp        -- store the Rexx MainApp object in .my.app

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxApp
jRexxApp=BsfCreateRexxProxy(rexxApp, ,"javafx.application.Application")

signal on syntax
   -- launch the application, invoke "start" and then stay up until the application closes
jRexxApp~launch(jRexxApp~getClass, .nil)  -- need to use this version of launch in order to work

call syssleep 0.01
exit

syntax:
   co=condition("object")
   say ppCondition2(co)
   say "---"
   jexc=co~additional~at(2)
   say "jexc="pp(jexc) "jexc~isA(.bsf)="pp(jexc~isA(.bsf))
   if jexc~isA(.bsf) then jexc~printStackTrace
   say " done. "~center(100, "-")


::requires "rgf_util2.rex"    -- get access to public routine ppCondition() which pretty prints
                              -- the information in a condition object to ease debugging

::requires "BSF.CLS"          -- get Java support


/* =================================================================================== */
/* Implements the abstract method "start" for the Java class "javafx.application.Application"
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java).
*/
::class RxMainApplication

   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */

::method start    -- will be invoked by the Java "launch" method
  expose locale primaryStage
  use arg primaryStage  -- we fetch the stage (Window) to use for our user interface

   -- set window's system icon and title text
  primaryStage~getIcons~add(.bsf~new("javafx.scene.image.Image", "file:oorexx_032.png"))
  primaryStage~setTitle("TableView with Graphics (ooRexx)")

  .my.app~tableview_stylesheet_kind=0     -- we start out without customized formatting
   -- define the style for kind=3:
  .my.app~tableview_style_kind3="-fx-background-color: #dbffdb; -fx-text-fill: black;"

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  demoTableViewUrl=.bsf~new("java.net.URL", "file:demoTableView.fxml")
  demoTableView   =.fx.FXMLLoader~load(demoTableViewUrl) -- load the fxml document
   -- get current selection state from corresponding menu object, now that .my.app is populated
  .my.app~showDebugOutput=.my.app~demoTableView.fxml~id_menu_show_debug~isSelected
   -- as .my.app got populated, all the FXML fx:id objects are available to Rexx
  call setupApplicationData   -- now that the graphics subsystem is initialized

   -- demo action! :) will fade on and off for a minute while user can interact with GUI!
  ft=.bsf~new("javafx.animation.FadeTransition", .fx.Duration~millis(2000),demoTableView)
  ft ~~setFromValue(.25) ~~setToValue(1) ~~setCycleCount(15) ~~setAutoReverse(.true) ~~play

  scene=.fx.scene~new(demoTableView)   -- create a scene from the parsed FXML tree
  primaryStage~setScene(scene)         -- set the stage to our scene

   -- this will setup and register the event handlers
  .demoTableViewController~new
  primaryStage~show              -- show the stage (and thereby its scene)


/* =================================================================================== */
/*
   This Rexx class controls the interaction with the fxml form named "demoTableView.fxml".

*/
::class demoTableViewController

   /* save the Java objects in ooRexx attributes for faster access in this class */
::method init
  expose id_vbox id_tableview id_menu_setitems id_menu_clear id_menu_quit  -
         id_menu_remove id_label id_menu_show_debug id_menu_enable_editing -
         id_button_add id_button_delete

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them to attributes
  appDir=.my.app~demoTableView.fxml

  if .my.app~showDebugOutput=.true then
    call dumpDir appDir, .line":" .dateTime~new self"::".context~name": .my.app~demoTableView.fxml:"

  id_tableview      =appDir~id_tableview
  id_vbox           =appDir~id_vbox      -- is parent, can have stylesheets
  id_label          =appDir~id_label
  id_menu_show_debug=appDir~id_menu_show_debug
  id_menu_enable_editing=appDir~id_menu_enable_editing

  id_menu_setItems  =appDir~id_menu_setitems
  id_menu_remove    =appDir~id_menu_remove
  id_menu_clear     =appDir~id_menu_clear
  id_menu_quit      =appDir~id_menu_quit
  id_menue          =appDir~id_menu

   -- add self as the EventHandler (we have the method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.event.EventHandler")

  /* set callback for creating TableView rows to which we add us as an event listener to get mouse click events on the rows */
  id_tableview~setOnMouseClicked(rp) -- allows us to get the mouse clicked event
  label=.bsf~new("javafx.scene.control.Label","<empty>")~~setStyle("-fx-text-fill: silver; -fx-font-size: 17pt;")
  id_tableview~setPlaceHolder(label)

   /* set callback for menu click */
  id_menu_show_debug~setOnAction(rp)
  id_menu_enable_editing~setOnAction(rp)
  id_menu_setitems  ~setOnAction(rp)
  id_menu_clear     ~setOnAction(rp)
  id_menu_quit      ~setOnAction(rp)

  /* create and assign context menu */
  ctxtMenu=createContextMenu(rp)
  id_tableview~setContextMenu(ctxtMenu)

  /* - a CellValueFactory gets used to create an Observable, fill-in cell's value and return it.
     - a CellFactory gets used for two purposes:
       - special set-ups like setting a graphic for a cell or
       - allowing the cell to be editable (TableView must be made editable as well)
  */
   /* set callback for filling the individual TableView cells */
  id_column_nr=appDir~id_column_nr     -- get TableColumn
  id_column_nr~setEditable(.false)     -- user must not edit the "nr" (id) column
  id_column_nr~setCellValueFactory(BsfCreateRexxProxy(.RexxCellValueFactory~new("nr"), , .fx.Callback))
   -- we need to set the CellFactory for this column too, if we want to become able to
   -- format the cell programmatically eg. with setStyle() (cf. menu "id_menu_style_cell")
  id_column_nr~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("nr"), , .fx.Callback))

  id_column_name=appDir~id_column_name -- get TableColumn
   -- must return an observable value
  id_column_name~setCellValueFactory(BsfCreateRexxProxy(.RexxCellValueFactory~new("name"), , .fx.Callback))
      -- make cell editable)
  id_column_name~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("name"), , .fx.Callback))

  id_column_expert=appDir~id_column_expert   -- get TableColumn
  id_column_expert~setCellValueFactory(BsfCreateRexxProxy(.RexxCellValueFactory~new("expert"), , .fx.Callback))
      -- format cell individually (also makes it editable)
  id_column_expert~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("expert"), , .fx.Callback))

  id_column_since=appDir~id_column_since     -- get TableColumn
  id_column_since~setCellValueFactory(BsfCreateRexxProxy(.RexxCellValueFactory~new("since"), , .fx.Callback))
      -- make cell editable)
  id_column_since~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("since"), , .fx.Callback))

  /* set callback for button press actions */
  id_button_add   =appDir~id_button_add    ~~setOnAction(rp)
  id_button_delete=appDir~id_button_delete ~~setOnAction(rp)


/* Could also be invoked via .FX.GuiThread~runLater(...), when the "add" button gets pressed as in
  cf. <https://stackoverflow.com/questions/22639386/select-row-in-javafx-tableview> as of 2020-07-04;
  but in this use case it is not necessary, can be directly invoked
*/
::method selectTableColumnToEdit
  expose id_tableview
  use strict arg pos, tableColumn

  if .my.app~showDebugOutput=.true then   -- show extensive information about event
     say .line":" .dateTime~new "\\\" self"::".context~name";" "arg()="pp(arg()) "| pos="pp(pos)", nameTableColumn="pp(nameTableColumn)

  id_tableView~requestFocus
  id_tableView~getSelectionModel~select(pos,tableColumn)
  id_tableView~getFocusModel~focus(pos,tablecolumn)   -- if not put in edit mode, <enter> will do that
--  id_tableView~edit(pos,tableColumn)                  -- put cell directly in edit mode



/** Event handler for most events that are of interest for our little application.
   @param event the JavaFX event object
*/
::method handle   /* implements the interface "javafx.event.EventHandler"   */
  expose id_tableview id_menu_setitems id_menu_clear id_menu_quit id_menu_remove id_label -
         id_menu_show_debug id_menu_enable_editing id_button_add id_button_delete

  use arg event

  source=event~source
  target=event~target
  eventType=event~eventType~toString
  isMouseEvent=(event~objectname~startsWith("javafx.scene.input.MouseEvent@"))

  indent="09"x

  if .my.app~showDebugOutput=.true then   -- show extensive information about event
  do
     say
     say .line":" .dateTime~new "\\\" self"::".context~name"; eventType="pp(eventType) "event:" pp(event)
     say indent .line":" "    event~source="pp(source) "~toString:"pp(source~toString)
     if target=.nil then say indent .line":" "    event~target="pp(target) "<--- !"
                    else say indent .line":" "    event~target="pp(target) "~toString:"pp(target~toString)
     say indent .line":" "    event~toString:" pp(event~tostring)

    if isMouseEvent, eventType="MOUSE_CLICKED" then   -- if a mouse click, show extensive information
    do
      -- get and format mouse related information
      mb=.mutableBuffer~new
      if event~isAltDown then mb~append("Alt+")
      if event~isControlDown then mb~append("Control+")
      if event~isMetaDown then mb~append("Meta+")
      if event~isShiftDown then mb~append("Shift+")
      if event~isShortcutDown then mb~append("SHORTCUT+")
      mb~append(pp(event~button~toString), " ")

      if event~isPrimaryButtonDown then mb~append("PrimaryDown ")
      if event~isMiddleButtonDown then mb~append("MiddleDown ")
      if event~isSecondaryButtonDown then mb~append("SecondaryDown ")

      mb~append("-> clickCount="pp(event~getClickCount) ")")

      if event~isStillSincePress  then mb~append("| isStillSincePress ")
      if event~isPopupTrigger     then mb~append("| isPopupTrigger ")
      if event~isDragDetect       then mb~append("| isDragDetect ")
      if event~isSynthesized      then mb~append("| isSynthesized ")

      mb~append("@ screen: x=",  event~getScreenX,"/y=",event~getScreenY," ")
      mb~append("@ Scene: x=",   event~getSceneX, "/y=",event~getSceneY," ")
      mb~append("@ eventSource=",event~getX,      "/y=",event~getY,"/z=",event~getZ)

      say indent .line":" "MOUSE_CLICKED -->" pp(mb~string)

      selectedIndex=id_tableView~getSelectionModel~getSelectedIndex
      selectedItem =id_tableView~getSelectionModel~getSelectedItem
      say indent .line":" "selectedIndex:" pp(selectedIndex) "selectedItem:" pp(selectedItem)
      say indent .line":" "event~eventType~toString="pp(eventType)", event~button~toString="pp(event~button~toString)", event~getClickCount="pp(event~getClickCount) "\\\"
      say indent .line":" "/// ---> pickResult~intersectedNode-Infos:"
      say indent .line":" ppNodeInfo(event)
      say indent .line":" "\\\ <---"
    end
  end



  /* Handle menu and button events. Note: the id values of the context MenuItems are set
     to the same id values defined in the FXML file such that the same actions get carried
     out. */
  tgtId=target~getId                   -- get fx:id value
  tableViewData=.my.app~tableViewData  -- fetch observable array list that backs the TableView

  select
     when tgtId="id_menu_show_debug" then    -- get selected-state, use it for setting .showDebugOutput
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              .my.app~showDebugOutput=id_menu_show_debug~isSelected
              return
           end

     when tgtId="id_ctxt_menu_show_debug" then  -- reflect change from context menu in File menu item as well
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              showDebugOutput=\id_menu_show_debug~isSelected
              id_menu_show_debug~setSelected(showDebugOutput)
              .my.app~showDebugOutput=showDebugOutput
              return
           end

     when tgtId="id_menu_enable_editing" then   -- get selected-state, use it for setting TableView edit mode
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              isSelected=id_menu_enable_editing~isSelected
              id_tableview~setEditable(isSelected)
              id_button_add   ~setDisable(\isSelected)
              id_button_delete~setDisable(\isSelected)
              return
           end

     when tgtId="id_ctxt_menu_enable_editing" then -- reflect change from context menu in File menu item as well
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              bSelected=event~source~isSelected    -- get ContextMenu's CheckMenuItem object
              id_tableview~setEditable(bSelected)  -- set TableView editing mode
              id_menu_enable_editing~setSelected(bSelected)

              if id_menu_clear~isDisable=.false then
              do
                 id_button_add   ~setDisable(\bSelected)
                 id_button_delete~setDisable(\bSelected)
              end
              return
           end

     when tgtId="id_menu_setitems" then      -- fill the TableView, set the menu and button states
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              call resetTableViewData   -- reset the tableviewdata to inital state, such setitems will show them all again
              id_tableview~setItems(tableViewData)
              id_menu_setitems~setDisable(.true)
              id_menu_remove  ~setDisable(.true)
              id_menu_clear   ~setDisable(.false)
              id_label~setText("TableView has" tableViewData~size "row(s)")
              if id_menu_enable_editing~isSelected then
              do
                 id_button_add   ~setDisable(.false)
                 id_button_delete~setDisable(.false)
              end
              return
           end

     when tgtId="id_menu_remove" then        -- remove .nil entries from ArrayList
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              do idx=tableViewData~size-1 to 0 by -1     -- remove empty cells
                 if tableViewData~get(idx)~isNil then
                    tableViewData~remove(box('int',idx))  -- make sure integer version of remove() gets used
              end

              if tableViewData~size>0 then               -- set state of menu items
              do
                 id_menu_setitems~setDisable(.true)
                 id_menu_clear   ~setDisable(.false)
              end
              else
              do
                 id_menu_setitems~setDisable(.false)
                 id_menu_clear   ~setDisable(.true)
              end
              id_menu_remove  ~setDisable(.true)
              id_label~setText("TableView has" tableViewData~size "row(s)")

              return
           end

     when tgtId="id_menu_clear" then
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              tableViewData~clear
              id_menu_setitems~setDisable(.false)
              id_menu_remove  ~setDisable(.true)
              id_menu_clear   ~setDisable(.true)
              id_label~setText("TableView has" tableViewData~size "cell(s)")
              id_button_add   ~setDisable(.true)
              id_button_delete~setDisable(.true)
              return
           end

     when tgtId="id_menu_quit" then
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              self~handleExit
              return
           end

      when tgtId="id_button_add" then
            do
               if .my.app~showDebugOutput=.true then
                  say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
               iin=.my.app~imageIndexNames
               tableViewData~add(.person~createTestPerson)
               id_tableView~refresh
               size=tableViewData~size
               id_label~setText("TableView has" size "row(s)")
               id_tableView~scrollTo(size-1)
                  -- will be run the next time the FX GuiThread runs
               pos=size-1
               nameTableColumn=id_tableView~getColumns~get(1)   -- get name column
               self~selectTableColumnToEdit(pos,nameTableColumn)
               return
            end

      when tgtId="id_button_delete" then
            do
               if .my.app~showDebugOutput=.true then
                  say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
               selectedIndex=id_tableView~getSelectionModel~getSelectedIndex

               if .my.app~showDebugOutput=.true then
                  say indent .line":" "id_button_delete, selectedIndex="pp(selectedIndex)

               if \selectedIndex~isNil then
               do
                  tableViewData~remove(selectedIndex)
                  id_tableView~refresh
                  id_label~setText("TableView has" tableViewData~size "row(s)")
               end
               else if .my.app~showDebugOutput=.true then
                   say indent .line":" "id_button_delete, selectedIndex="pp(selectedIndex)
               return
            end

     when eventType="EDIT_CANCEL" then    -- force updateItem
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              id_tableview~refresh        -- force a refresh of the TableView
              return
           end

     otherwise nop
  end

   -- if Ctrl-click on a cell with a value, then set it to .nil, otherwise reinstate value
  idx=getIndex(event)
  if idx>-1, isMouseEvent, event~getClickCount=1 then
  do
      if idx<tableViewData~size, event~isControlDown then      -- clicked cell index is within bounds
      do
         val=tableViewData~get(Idx)

         if .my.app~showDebugOutput=.true then
            say indent .line":" "*** handling" pp("Ctl-"eventType) "for index="pp(idx)", value="pp(val)

         if val~isNil then                   -- no backing observable (was nullified before with ctl-click)
         do
            val=.person~testData~at(idx+1)   -- fetch Person from predefined test data
            if val~isNil then
               val=.person~createTestPerson  -- create a new test person
            tableViewData~set(idx,val)       -- save as observable

            bDisable=.true           -- if some .nil entry, then enable id_menu_remove
            do o over tableViewData  -- iterate over all items to determine state of "remove" menu
               if o~isNil then       -- o.k. one is .nil, allow removal in menu
               do
                  bDisable=.false
                  leave
               end
            end
            id_menu_remove~setDisable(bDisable)
         end
         else  -- nullify
         do
            tableViewData~set(idx, .nil)                 -- nullify
            id_menu_remove~setDisable(.false)            -- enable "remove" menu
         end
      end
      else  -- left mouse button click at a cell outside of the range of our currently defined data
      do
         if event~button~toString="PRIMARY", idx>(tableViewData~size-1) then   -- this way only left mouse buttong clicks
         do
            call beep 250,100       -- beep via Rexx (may not beep on some Unixes)
            .my.app~alert.audioclip~play   -- use JavaFX to have an "oops" clip played on all platforms alike
         end
      end
  end

  if .my.app~showDebugOutput=.true then   -- show current entries in ObservableArrayList
  do
     tab="09"x
     say indent .line":" "// iterate over cells (observableArrayList):"
     if \tableViewData~isNil then
     do
        say indent tab"current tableViewData~size:" pp(tableViewData~size)
        do i=0 to tableViewData~size - 1 --  o over id_tableview~getItems
            o=tableViewData~get(i)
            info=""
            if \o~isNil, o~isA(.bsf) then info=pp("o~toString="o~toString)
            if info~pos(o~objectname)>0 then
               say indent tab"    " i~right(3)":" pp(o)
            else
               say indent tab"    " i~right(3)":" pp(o) info
        end
     end
  end

  if eventType~startsWith("EDIT") then    -- handle edit_cancel event, show information about edit events
  do
     idx=event~getIndex
     if .my.app~showDebugOutput=.true then
         .error~say(indent .line ":" "//" pp(eventType) "event: ~getIndex=" pp(idx)", event~getSource~getEditingIndex="pp(event~getSource~getEditingIndex))

     if eventType="EDIT_CANCEL" then   -- force updateItem
              id_tableview~refresh
        -- event~getSource~refresh  -- force a refresh of the TableView
  end


 /* Closes the application. */
::method handleExit
  bsf.loadClass("javafx.application.Platform")~exit   -- unload JavaFX, but let Rexx continue in main thread


/* =================================================================================== */
/* Implements "R javafx.util.Callback<P,R>(P o) used by PropertyValueFactory */

/* This class defines and returns the cell's standard data types for the different
   TableColumns as simple JavaFX properties that the TableView can handle on its own
   (eg. for sorting or simple editing).

*/
::class RexxCellValueFactory -- Rexx class implementing "javafx.util.Callback"'s "call" method
::method init
  expose columnName
  use strict arg columnName

  if .my.app~showDebugOutput=.true then
  do
     say
     say .line":" .datetime~new "///" self"::init" "columnName="pp(columnName)
  end

  /* ------------------------------------------------------------------------------ */
  /* create and setup a cell, assign stringConverter for in-place edits */
::method call           -- implements "javafx.util.Callback.call(Object o)": in this case an instance of a TableCell (or a subclass of it) must be returned!
  expose columnName
  use arg obj, slotDir

  if obj~getValue~isNil then return    -- "obj~value" may be .nil (if user nullified row with ctl-mouseclick

  aRexxObj=bsfRexxProxy(obj~getValue)  -- extract Rexx object from Java RexxProxy
  if aRexxObj~isNil then
     val=.nil
  else
     val=aRexxObj~send(columnName)     -- get value from Rexx object

  select case columnName
      when "nr"     then cell=.fx.simpleIntegerProperty~new(val)
      when "name"   then cell=.fx.simpleStringProperty~new(val)
      when "expert" then cell=.fx.simpleStringProperty~new(val)
      when "since"  then cell=.fx.simpleStringProperty~new(val)
      otherwise nop
  end

  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "///<--" self"::call -" "obj="pp(obj) pp(columnName)"="pp(val) "| RETURNING:" pp(cell)
     if columnName="since" then
     do
         say .line":" "--- last column of this row ---"
         say     -- on the last column add an empty line
     end
  end

  return cell


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) used by PropertyValueFactory */

/* This class can be used to define specific TableCell implementations for a column,
   which may be used to make a cell into a DatePicker cell when edited.

   This one serves as the default implemention for a TableCell using a RexxTableCell instance.
   However, in the case of the "since" column (a DatePicker TableCell) a different
   implementation will be used.
*/
::class RexxCellFactory -- Rexx class implementing "javafx.util.Callback"'s "call" method

  /* ------------------------------------------------------------------------------ */
::method init
  expose columnName
  use strict arg columnName

  if .my.app~showDebugOutput=.true then
      say .line":" .datetime~new "///" self"::init" "columnName="pp(columnName)

  /* ------------------------------------------------------------------------------ */
  /* create and setup a cell, assign stringConverter for in-place edits */
::method call           -- implements "javafx.util.Callback.call(Object o)": in this case an instance of a TableCell (or a subclass of it) must be returned!
  expose columnName
  use arg obj, slotDir

               -- create the TableCell handler, supply a RexxTableCell object to carry out "updateItem"
  if columnName="since" then   -- use our custom RexxDatePickerTableCell implementation
     myClz=.RexxDatePickerTableCell
  else if columnName="expert" then
     myClz=.RexxComboBoxTableCell
  else
     myClz=.RexxTableCell        -- get Rexx class object to ease access to its class attributes

  rexxObj=myClz~new(columnName)  -- create Rexx object to handle cell
  rexxCellHandler=myClz~proxyClass~new(rexxObj)       -- create corresponding Java cell
  rexxCellHandler~setConverter(myClz~stringConverter)
  rexxCellHandler~setTooltip(.my.app~cell.tooltip)

  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "///<--" self"::call" "columnName="pp(columnName)  "| RETURNING:" pp(rexxCellHandler)
     if columnName="since" then say
  end
  return rexxCellHandler         -- return the Java cell to be used for the TableView



/* =================================================================================== */
/* This is an ooRexx class, subclassing the JavaFX "javafx.scene.control.TableCell" class
   in order to become able to edit/format TableCells by intercepting the "updateItem" method.

   This implements the standard Cell behaviour controlled by our implementation.
*/
::class "RexxTableCell"
/* -------------------------------------------------------------------------------- */
::attribute proxyClass        class get   -- a class attribute
::attribute stringConverter   class get   -- a class attribute

::method init class     -- the Rexx constructor at the class level: create and memorize a proxy class for "javfx.scene.control.TableCell"
  expose proxyClass stringConverter
   -- use a TextFieldTableCell to allow in place edits
  proxyClass=bsf.createProxyClass("javafx.scene.control.cell.TextFieldTableCell", -
                                  "jRexxTableCell", -
                                  "javafx.scene.control.Cell startEdit",  - -- needed to inhibt editing .nil observable values
                                  "javafx.scene.control.Cell updateItem")

   -- we need a stringConverter for in-place edits
  stringConverter=.bsf~new("javafx.util.converter.DefaultStringConverter")


  /* ------------------------------------------------------------------------------ */
::method init
  expose columnName
  use strict arg columnName   -- save column name in case we use this class for multiple string columns

  if .my.app~showDebugOutput=.true then say .line":" .datetime~new "///   " self"::init" "columnName="pp(columnName)

   -- in the case that the obsverable of the record got nullified (ctl-mouseclick), make sure to inhibit editing
::method startEdit
  use arg slotDir

  jself=slotDir~javaObject       -- get Java peer object
  index=jself~getIndex
  if .my.App~tableViewData~get(index)~isNil then
  do
     .my.app~alert.audioclip~play   -- use JavaFX to have an "oops" clip played on all platforms alike
     return
  end
  jself~startEdit_forwardToSuper -- continue editing


::method updateItem           -- void TableCell.updateItem (SimpleStringProperty name, boolean empty)
  expose columnName
  use arg item, empty, slotDir

  jSelf=slotDir~javaObject
  index=jSelf~getIndex     -- get 0-based index (row) of this cell

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "/// -->" self"::".context~name": jself:" pp(jself) "columnName="pp(columnName) "| item="pp(item)",empty="pp(empty)",index="pp(index)

     -- we need to invoke the method in the superclass first
  jSelf~updateItem_forwardToSuper(item,empty)

  if item=.nil | empty=.true then
  do
     jSelf~setText(.nil)
     jSelf~setGraphic(.nil)
  end
  else   -- we have a value and are not empty
  do
     jSelf~setText(item)
         -- update Person object
     if columnName="name" then   -- after changes, we only use this class for the "name" attribute/column
     do
        index=jSelf~getIndex    -- get 0-based index of this cell (= row)
        rexxProxy=jSelf~tableview~getItems~get(index) -- fetch the corresponding RexxProxy entry

        if \rexxProxy~isNil then
        do
           rexxobj=bsfRexxProxy(rexxProxy)               -- unbox Rexx object from the Java RexxProxy
           if rexxObj~name<>item then  -- only update, if value has changed
              rexxObj~name=item        -- assign new value to Rexx attribute
        end
     end
  end

   -- style the cell's background
  if index>-1, .my.app~tableview_stylesheet_kind=3 then      -- colorize the background of the odd entries
  do
     if index//2 then
     do
        -- we need to make sure that any predefined style (like -fx-alignment) remains intact
        jSelf~setStyle(jself~getStyle .my.app~tableview_style_kind3)
     end
  end




/* =================================================================================== */
/* This routine can only work, once the internal graphics of JavaFX got initilaized
   (e.g. in the Application's start method). It sets up the items to be viewed in the
   list and saves all collections in this package local directory.
*/
::routine setupApplicationData
   -- create and save an ObservableList
  entryNames=.array~of("First Image (Bug)", "Second Image (Java)", -
                       "Third Image (Linux)", "Fourth Image (OpenSource)")
  imageIndexNames="Bug", "Java", "Linux", "OpenSource"
  imageNames=.array~of("icons8-bug-64.png","icons8-java-64.png", -
                       "icons8-linux-64.png", "icons8-open-source-64.png")

   -- TableView will use the observableArrayList and maintains it as well
  tableViewData=.fx.FXCollections~observableArrayList  -- StringProperties to be shown in TableView
  .person~createTestData
  do p over .person~testData
     tableViewData~add(p)           -- will wrap the Rexx object into a Java RexxProxy
  end

  imageViewDir=.directory~new       -- maps the entryName to its icon/grahpics
  do counter i name over entryNames
     img=.fx.Image~new(imageNames[i])
     imageViewDir~setEntry(name,img)
     imageViewDir~setEntry(imageIndexNames[i],img)
  end

   -- save in directory accessible via the environment symbol .my.app
  .my.app~entryNames     =entryNames
  .my.app~imageIndexNames=imageIndexNames
  .my.app~imageViewDir   =imageViewDir

  .my.app~tableViewData=tableViewData
  .my.app~alert.audioclip=.bsf~new("javafx.scene.media.AudioClip", "file:oops.wav")

  tipText="If editing enabled: <enter> or click to edit," "0a"x"Ctl-click to nullify cell/undo cell nullify"
  tooltip =.bsf~new("javafx.scene.control.Tooltip", tipText)
  imgView=.fx.ImageView~new("file:icons8-mailbox-opened-flag-up-64.png") ~~setPreserveRatio(.true) ~~setFitHeight(50) ~~setPadding(5)
  tooltip~setGraphic(imgView)
   -- Cf. <https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html> (as of 2020-07-05)
  tooltip~setStyle("-fx-font-size: 14pt; -fx-text-fill: black; -fx-padding: 5; -fx-background-color: moccasin ;")
  .my.app~cell.tooltip=tooltip


/* =================================================================================== */
/* Recreates the tableViewData content from .person~testData */
::routine resetTableViewData   -- clears .app.dir~tableViewData and sets its entries according to .my.app~entryNames
  tableViewData=.my.app~tableViewData
  tableViewData~clear      -- clear
  do counter i o over .person~testData
     tableViewData~add(o)  -- ObservableArrayList to be displayed in the list
  end


/* =================================================================================== */
/* For a context menu we cannot refer to existing MenuItems (they will not be honored/shown),
   rather we must create proper context MenuItems. */
::routine createContextMenu
  use arg eventHandler
  ctxtMenu=.bsf~new("javafx.scene.control.ContextMenu")
  items=ctxtMenu~getItems

  sdo=.bsf~new("javafx.scene.control.CheckMenuItem","Show Debug Output") -
      ~~setSelected(.my.app~showDebugOutput) -
      ~~setId("id_ctxt_menu_show_debug")~~setOnAction(eventHandler)
  items~add(sdo)

  ee=.bsf~new("javafx.scene.control.CheckMenuItem","Enable Editing") -
      ~~setId("id_ctxt_menu_enable_editing")~~setOnAction(eventHandler)
  items~add(ee)

  fxMI=bsf.importClass("javafx.scene.control.MenuItem")
  items~add(.bsf~new("javafx.scene.control.SeparatorMenuItem"))
      -- use same id as the File menu items, such that the context menu items cause the same behaviour
  items~add(fxMI~new("Setitems")        ~~setId("id_menu_setitems")~~setOnAction(eventHandler) )
  items~add(fxMI~new("RemoveEmptyCells")~~setId("id_menu_remove")  ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Clear")           ~~setId("id_menu_clear")   ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Quit")            ~~setId("id_menu_quit")    ~~setOnAction(eventHandler) )

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "\\\"  "createContextMenu(eventHandler): ctxtMenu~toString="pp(ctxtMenu~toString)

   -- use an ooRexx routine object as the Java EventHandler for the context menu
  jeh=bsfCreateRexxProxy(.routines~contextMenuHandler,,"javafx.event.EventHandler")
  ctxtMenu~setOnShowing(jeh)     -- will fire right before PopupMenu window gets displayed
  return ctxtMenu                -- return the ContextMenu

/* ----------------------------------------------------------------------------------- */
/* Rather than using a class with methods to handle events for the Context menu, this
   demonstrates using a routine for that purpose. This event handler will set all the
   ContextMenu's MenuItems to the same disable state as the matching MenuItems in the
   form's File Menu.
   Before showing the context menu, set its menu items according to the File menu items.
*/
::routine "ContextMenuHandler"     -- set disable state according to the File MenuItems state
  use arg event, slotDir

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "   " .context~name"(event), event:" pp(event~toString)

  appDir=.my.app~demoTableView.fxml     -- get the directory with all current JavaFX objects having an fx:id
  ctxtMenu=event~source                -- get access to the ContextMenu object
  do menuItem over ctxtMenu~getItems   -- iterate over all ContextMenu MenuItems
      id=menuItem~getId                -- get ID (same as the one in the File's MenuItem)
      if \id~isNil then                -- separator does not have an id
      do
         if id="id_ctxt_menu_show_debug" then   -- the current state is contained in .showDebugOutput
            menuItem~setSelected(.my.app~showDebugOutput)
         else if id="id_ctxt_menu_enable_editing" then
         do
            isSelected=appDir~id_menu_enable_editing~isSelected
            menuItem~setSelected(isSelected)
         end
         else  -- reflect current state from File menu items
         do
            state=appDir~send(id)~isDisable  -- fetch File's MenuItem, get its disable state
            menuItem~setDisable(state)       -- set matching popup MenuItem's disable state accordingly
         end
      end
  end


/* =================================================================================== */
/* Return a string of the list of nodes from the intersected node to the root node.
   If a cell node is encountered query and show its getIndex() value. */
::routine ppNodeInfo
   use arg event

   node=event~pickResult~getIntersectedNode
   if node=.nil then return .nil
   return ppCreateNodePath(node)


::routine ppCreateNodePath
   use arg node
   mb=.MutableBuffer~new
   do until node=.nil
      if mb~length>0 then mb~append(" -> ", pp(node))
                     else mb~append(pp(node))

      if node~objectName~pos("Cell")>0 then
         mb~append("~getIndex()=[", node~getIndex, "] *** |")

      node=node~getParent
   end
   return mb~string


/* =================================================================================== */
/* Find and return Cell object. */
::routine findCellObject
   use arg event
      -- get the node of the event
   signal on syntax     -- in case event does not have the pickResult method
   node=event~pickResult~getIntersectedNode
   do while \node~isNil    -- find the Cell node going up the hierarchy
      if node~objectname~pos("Cell")>0 then  -- we found the Cell node
         return node
      node=node~getParent  -- get next parent node
   end
   return .nil

syntax:
   if .my.app~showDebugOutput=.true then
      say .line .dateTime~new .context~name"(event): SYNTAX condition (pickResult-method not present)"
   return .nil


/* =================================================================================== */
/* Return the event's Cell index in the TableView or -1 if not available.
*/
::routine getIndex
   use arg event
      -- get the node of the event
   cell=findCellObject(event)
   if cell~isNil then return -1
   return cell~getIndex


/* =================================================================================== */
/* Utility routine for debugging: dumps the content of a directory ordered by name. */
::routine dumpDir

  if .my.app~showDebugOutput=.false then return

  use arg dir, name=""
  say
  say "dumpDir():" name
  do counter i n over dir~allindexes~sort
     say i~right(3)":" pp(n) pp(dir~entry(n))
  end
  say


/* =================================================================================== */
::class Person

/* -------------------------------------------------------------------------------- */
::attribute experts  class    -- array of expert options
::attribute graphics class    -- stringtable (directory) of expert->Image/Graphic
::attribute testData class    -- array of generated persons
::attribute expertChoices class


::method    init     class
  use local tmp
  graphics=.stringtable~new
  testData=.array~new
   -- set up expert names and their corresponding image files
  experts  = "Bug", "Java", "Linux", "OpenSource", .nil  -- .nil indicate non-experts
  imageFileNames=.array~of("icons8-bug-64.png","icons8-java-64.png", -
                       "icons8-linux-64.png", "icons8-open-source-64.png")

   -- create an ObservableList with the choices of experts to be listed in the ComboBoxTableCell
  .context~package~local~nilString="<null>"
  expertChoices=bsf.loadClass("javafx.collections.FXCollections")~observableArrayList
  do tmp over experts
     if tmp~isNil then
        expertChoices~add(.nilString)
     else
        expertChoices~add(tmp)
  end

   -- define first and last names to be randomly combined
  arrFirstName="Anton", "Berta", "Caesar", "Dora", "Emil", "Friedrich", "Gustav", "Heidelinde", "Ida"
  arrFirstNameCount=arrFirstName~items
  arrLastName ="Barmwater", "Cowlishaw", "Davis", "Flatscher", "Fuller", "Jansen", "Jonsson", "McGuire", "Steinboeck", "Wolfers"
  arrLastNameCount=arrLastName~items

  expertsCount=experts~items
  counter=0


::attribute counter get class
  expose counter
  counter+=1
  return counter


::method createTestPerson class  -- returns a randomyl created Person instance
  expose graphics experts expertsCount arrFirstName arrFirstNameCount arrLastName arrLastNameCount

  currentYear=.dateTime~new~year -- get current year
  name=arrLastName[random(1,arrLastNameCount)]"," arrFirstName[random(1,arrFirstNameCount)]
  exp=experts[random(1,expertsCount)]  -- get expert status
  i=self~counter                 -- get next value
  if exp~isNil then
     p=.person~new(i,name)
  else   -- expert status set, create a "since" value in ISO date
  do
     since=random(1997,currentYear)"-"random(1,12)~right(2,0)"-"random(1,28)~right(2,0)
     p=.person~new(i,name,exp,graphics~entry(exp),since)
  end
  return p


::method    createTestdata class    -- will run after all class objects got iniitialized
  expose graphics testData imageFileNames experts expertsCount arrFirstName arrFirstNameCount arrLastName arrLastNameCount

  if graphics~items=0 then    -- create images, store them under the corresponding expert's name
  do
     do counter i name over experts
        if name~isNil then iterate
        img=.fx.Image~new(imageFileNames[i]) -- create an Image and store it
        graphics~setEntry(name,img)
     end
     .context~package~local~graphics=graphics   -- save in package local
  end

  -- create a few persons, add them to the observable list, will cause the created
  -- Rexx objects to be autoboxed as Java RexxProxy objects
  do i=1 to 5
     testData~append(self~createTestPerson)
  end


/* -------------------------------------------------------------------------------- */
::attribute nr
::attribute name
::attribute expert
::attribute graphic     -- really needed?
::attribute since

::method makeString
  use local
  return "a Person:[nr="nr",name="name",expert="expert",since="since"]"

::method init        -- fetch person's attribute values
  use local
  use strict arg nr, name, expert=.nil, graphic=.nil, since=.nil

  if .my.app~showDebugOutput=.true then
     say .line":" .datetime~new "///   " self"::init" "columnName="pp(columnName)



/* =================================================================================== */
/* Rexx implementation of a DatePickerTableCell.
*  Will handle the enter, tab key event and focus lost event to commit the new value.

   This example displays the date in the unedited cell as ISO_LOCAL_DATE ("yyyy-mm-dd").
*/
::class "RexxDatePickerTableCell"   -- extends javafx.scene.control.TableCell

/* -------------------------------------------------------------------------------- */
::attribute clzDateTimeFormatter    class
::attribute clzLocalDate            class

::attribute iso_local_date          class -- a DateTimeFormatter (ISO_LOCAL_DATE constant)
::attribute iso_local_date_pattern  class -- the string pattern "yyyy-mm-dd"

::attribute methods2proxy           class -- arguments for bsf.proxyClass
::attribute proxyClass              class -- the Java proxy class for this Rexx class

::attribute stringConverter         class -- a LocalDateStringConverter(iso_local_date,iso_local_date)
::attribute localDateStringConverter class -- to parse editor's date text value into a LocalDate


::method init class
  use local tmp               -- all local variables are attributes but "TMP"

  clzDateTimeFormatter=bsf.loadClass("java.time.format.DateTimeFormatter")
  clzLocalDate   =bsf.loadClass("java.time.LocalDate")
  iso_local_date = clzDatetimeformatter~iso_local_date
  iso_local_date_pattern="yyyy-mm-dd"  -- the String pattern of the ISO_LOCAL_DATE

   -- create proxy class and save it in the class attribute
  methods2proxy  ="javafx.scene.control.Cell startEdit", "javafx.scene.control.Cell updateItem"
   -- create the proxy class (a Java class forwarding method invocations to Rexx object)
  proxyClass     =bsf.createProxyClass("javafx.scene.control.cell.TextFieldTableCell", -
                                       "jRexxDatePickerTableCell", methods2proxy)

   -- we need a stringConverter for in-place edits
  localDateStringConverter=.bsf~new("javafx.util.converter.LocalDateStringConverter")
  stringConverter=.bsf~new("javafx.util.converter.DefaultStringConverter")


/* -------------------------------------------------------------------------------- */
::method init
  use local                   -- all are attributes
  use strict arg columnName

  selfClass=self~class
  datePicker=.nil
  dateFormatter=selfclass~iso_local_date

  isCancelled=.true     -- start out with .true, such that startEdit's focus change does not commit the empty initial cell

  if .my.app~showDebugOutput=.true then
     say .line":" .datetime~new "///   " self"::init" "columnName="pp(columnName)

/* This method fetches the cell value and updates the datePicker and the corresponding
*  Rexx object's 'since' attribute.
*/

::method commitCellUpdate
  expose columnName datePicker selfClass dateFormatter bAlreadyCommitted
  use strict arg jCell

   -- already new Cell value committed (TAB), now called again because of Focus lost, return
  if bAlreadyCommitted=.true then return

   -- get the current cell's string value
  cellStringValue=datePicker~editorProperty~get~getText  -- in the format of the localDateStringConverter
   -- create a LocalDate object from it
  ldEditedText=selfClass~localDateStringConverter~fromString(cellStringValue)
   -- set datePicker to this local date as well
  datePicker~setValue(ldEditedText)    -- make sure datePicker gets set to the new value

   -- turn local date into a string rendering that we use in Rexx and in the column if not in editing mode
  newStringValue=ldEditedText~format(dateFormatter)      -- get as formatted string
  datePicker~editorProperty~get~setText(newStringValue)  -- set datepicker's editor

   -- get RexxProxy in order to change its date value
  index=jCell~getIndex    -- get 0-based index of this cell (= row)
  rexxProxy=jCell~tableview~getItems~get(index) -- fetch the corresponding RexxProxy entry
  rexxobj=bsfRexxProxy(rexxProxy)               -- extract Rexx object from the Java RexxProxy

  if rexxObj~send(columnName)<>newStringValue then -- only update, if value has changed
     rexxObj~send(columnName"=",newStringValue)    -- assign new value using the setter pattern

   -- remember that we have dealt with this cell already (via TAB followed by Focus change)
   bAlreadyCommitted=.true             -- remember that we have dealt with this cell already
   jCell~commitEdit(newStringValue)    -- now let us finish up



/* We need to intercept KeyCode.ENTER in order to become able to update our
   backing Person Rexx object with the new value. Because of intercepting all
   Key events, we need to explicitly invoke "cancelEdit" in case the user
   presses KeyCode.ESCAPE.
*/
-- in event capturing phase (set with event filter)
::method handle   /* implements the interface "javafx.event.EventHandler"   */
  expose jself isCancelled
  use arg event, slotDir

  strKeyCode=event~getCode~toString
  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "///-->" self"::".context~name":" "strKeyCode="pp(strKeyCode) "isCancelled="pp(isCancelled)
     say "09"x .line":" "event~source:" pp(event~source) "event~target:" pp(event~target)
  end

  select
      when wordpos(strKeyCode, "ENTER TAB")>0 then -- ENTER or TAB pressed, commit Cell changes
            self~commitCellUpdate(jSelf)

      when strKeyCode="ESCAPE" then -- ESCAPE pressed, remember it
            isCancelled=.true       -- make sure that focus change does not change cell's value!

      otherwise nop
  end



/* In case the user moves the focus to somewhere else with a mouse click or TAB key, we must
   assure that the (potentially changed) value in the editor gets used to update the backing
   Person Rexx object. Also the ESCAPE key will cause a focus change by default, make sure
   that in such a case the cell's content does not get committed.
   Also, in JavaFX8 one needs to explicitly set the datePicker
   as well and refresh the TableView in order to have it update the value.
*/
-- focus event on Cell
::method changed unguarded    /* implements the interface "javafx.beans.value.ChangeListener" */
  expose jself isCancelled
  use arg observable, oldValue, newValue

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "///-->" self"::".context~name", focus changed:" "observable="pp(observable)", oldValue="pp(oldValue)", newValue="pp(newValue) "isCancelled="pp(isCancelled)

  if oldValue=.true, newValue=.false, jself~getIndex>-1, isCancelled=.false then -- focus leaving? make sure we set the DatePicker
  do
     self~commitCellUpdate(jself)   -- now let us finish up
     jSelf~getTableView~refresh     -- MUST on Java_FX8, otherwise cached values distort cell's value on focus traversals
  end


::method getDate        -- return localDate date
  expose selfClass dateFormatter
  use arg item

  if item~isNil | item="" then
     date=selfClass~clzLocalDate~now  -- supply today's date
  else
    date=selfClass~clzLocalDate~parse(item,dateFormatter)

  return date

::method startEdit
  expose selfClass jself datePicker isCancelled bAlreadyCommitted
  use arg slotDir

  jself=slotDir~javaObject       -- get Java peer object
  index=jself~getIndex

  if .my.App~tableViewData~get(index)~isNil then
  do
     .my.app~alert.audioclip~play   -- use JavaFX to have an "oops" clip played on all platforms alike
     return
  end

  oriValue=jself~getText         -- save current text value from cell, before forwardToSuper
  jself~startEdit_forwardToSuper -- will cause the event to fire, in case there are registered event handlers

  if datePicker~isNil then       -- create a datePicker and assign filter and focus listener
  do
     datePicker=.bsf~new("javafx.scene.control.DatePicker")
     rp=BSFCreateRexxProxy(self, ,"javafx.beans.value.ChangeListener" ,"javafx.event.EventHandler")

         -- ATTENTION: must be an event filter, otherwise ESCAPE key event may be consumed and cannot be seen in bubbling up phase
     jself~addEventFilter(.fx.KeyEvent~key_pressed, rp)
         -- this will cause the changed message upon focus change (e.g. ENTER, TAB, ESCAPE, mouse click
     datePicker~focusedProperty~addListener(rp)
  end

  oriDate=self~getDate(oriValue) -- get a localDate object
  datePicker~setValue(oriDate)   -- set datePicker to that date

  jself~setText(.nil)            -- remove text field
  jself~setGraphic(datePicker)   -- add datpicker UI
  datePicker~requestFocus

  isCancelled=.false             -- we set this, if cancelEdit gets invoked
  bAlreadyCommitted=.false       -- if changes occur in Cell allow to commit them



::method updateItem
  expose datePicker columnName
  use arg item, empty, slotDir

  jSelf=slotDir~javaObject
  index=jSelf~getIndex
  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "/// -->" self"::".context~name": jself:" pp(jself) "columnName="pp(columnName) "| item="pp(item)",empty="pp(empty)",index="pp(index)

  jSelf~updateItem_forwardToSuper(item,empty)   -- will cause the event to fire, in case there are registered event handlers

  if item=.nil | empty=.true then
  do
     jSelf~setText(.nil)
     jSelf~setGraphic(.nil)
  end
  else   -- we have a value and are not empty
  do
     if \datePicker~isNil then
     do
        datePicker~setValue(self~getDate(item))
        jself~setText(.nil)
        jself~setGraphic(datePicker)
     end
     else
     do
        jself~setText(item)
        jself~setGraphic(.nil)
     end
  end

   -- style the cell's background
   -- note: one can use a CSS definition like: ".list-cell:odd { -fx-background-color: #dbffdb; }"
  if index>-1, .my.app~tableview_stylesheet_kind=3, index//2  then      -- colorize the background of the odd entries
  do
      -- we need to make sure that any predefined style (like -fx-alignment) remains intact
     jSelf~setStyle(jself~getStyle .my.app~tableview_style_kind3)
  end




/* =================================================================================== */
/* Rexx implementation of a ComboBoxTableCell.
* Will handle the enter key event (and commit the new value), but not the focus lost event as
  on JavaFX 8 the ComboBox does not yield the new value in the editor field.

  Warning: do not use "javafx.scene.control.cell.ComboBoxTableCell" for this purpose, it
           has many, many problems for which bugs got reported as numereous null pointer
           exceptions may get issued (at least in JavaFX 8); the following implementation
           is stable and straight forward by using "javafx.scene.control.ComboBox" directly !
           (Besides the implementation uses has exactly the same pattern as the DatePicker above!)
*/
::class "RexxComboBoxTableCell"   -- extends javafx.scene.control.TableCell

/* -------------------------------------------------------------------------------- */
::attribute methods2proxy           class -- arguments for bsf.proxyClass
::attribute proxyClass              class -- the Java proxy class for this Rexx class
::attribute stringConverter         class

::method init class
  use local tmp               -- all local variables are attributes but "TMP"

   -- create proxy class and save it in the class attribute
  methods2proxy="javafx.scene.control.Cell startEdit", "javafx.scene.control.Cell updateItem"
   -- create the proxy class (a Java class forwarding method invocations to Rexx object)
  proxyClass   =bsf.createProxyClass("javafx.scene.control.cell.TextFieldTableCell", -
                                       "jRexxComboBoxTableCell", methods2proxy)

   -- we need a stringConverter for in-place edits
  stringConverter=.bsf~new("javafx.util.converter.DefaultStringConverter")


/* -------------------------------------------------------------------------------- */
::method init
  use local                   -- all are attributes
  use strict arg columnName

  selfClass=self~class
  comboBox=.nil

  isCancelled=.true     -- start out with .true, such that startEdit's focus change does not commit the empty initial cell

  if .my.app~showDebugOutput=.true then
     say .line":" .datetime~new "///   " self"::init" "columnName="pp(columnName)

/* This method fetches the cell value and updates the comboBox and the corresponding
*  Rexx object's 'since' attribute.
*
*/
::method commitCellUpdate
  expose columnName comboBox selfClass dateFormatter bAlreadyCommitted
  use strict arg jCell

   -- already new Cell value committed (TAB), now called again because of Focus lost, return
  if bAlreadyCommitted=.true then return

   -- get the current cell's string value
  newStringValue=comboBox~getValue     -- get current value in comboBox
  if newStringValue=.nilString then    -- picked the .nilString from ComboBox
     newStringValue=.nil

   -- get RexxProxy in order to change its date value
  index=jCell~getIndex    -- get 0-based index of this cell (= row)
  rexxProxy=jCell~tableview~getItems~get(index) -- fetch the corresponding RexxProxy entry
  rexxObj=bsfRexxProxy(rexxProxy)               -- extract Rexx object from the Java RexxProxy

  if rexxObj~expert<>newStringValue then  -- only update, if value has changed
  do
     rexxObj~expert=newStringValue        -- assign new value to attribute
     if newStringValue~isNil then         -- remove value of since
        rexxObj~since=.nil                -- nullify
     else                                 -- changed expert status, update "since" attribute
        rexxObj~since=date('iso')         -- use today's date
  end

   -- remember that we have dealt with this cell already (via TAB followed by Focus change)
   bAlreadyCommitted=.true             -- remember that we have dealt with this cell already
   jCell~commitEdit(newStringValue)   -- now let us finish up



/* We need to intercept KeyCode.ENTER in order to become able to update our
   backing Person Rexx object with the new value. Because of intercepting all
   Key events, we need to explicitly invoke "cancelEdit" in case the user
   presses KeyCode.ESCAPE.
*/
-- in event capturing phase (set with event filter)
::method handle   /* implements the interface "javafx.event.EventHandler"   */
  expose jself isCancelled
  use arg event, slotDir

  strKeyCode=event~getCode~toString
  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "///-->" self"::".context~name":" "strKeyCode="pp(strKeyCode) "isCancelled="pp(isCancelled)
     say "09"x .line":" "event~source:" pp(event~source) "event~target:" pp(event~target)
  end

  select
      when wordpos(strKeyCode, "ENTER TAB")>0 then -- ENTER or TAB pressed, commit Cell changes
            self~commitCellUpdate(jSelf)

      when strKeyCode="ESCAPE" then -- ESCAPE pressed, remember it
            isCancelled=.true       -- make sure that focus change does not change cell's value!

      otherwise nop
  end



/* In case the user moves the focus to somewhere else with a mouse click or TAB key, we must
   assure that the (potentially changed) value in the editor gets used to update the backing
   Person Rexx object. Also the ESCAPE key will cause a focus change by default, make sure
   that in such a case the cell's content does not get committed.
   Also, in JavaFX8 one needs to explicitly set the comboBox
   as well and refresh the TableView in order to have it update the value.
*/
-- focus event on Cell
::method changed unguarded    /* implements the interface "javafx.beans.value.ChangeListener" */
  expose jself isCancelled
  use arg observable, oldValue, newValue

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "///-->" self"::".context~name", focus changed:" "observable="pp(observable)", oldValue="pp(oldValue)", newValue="pp(newValue) "isCancelled="pp(isCancelled)

  if oldValue=.true, newValue=.false, jself~getIndex>-1, isCancelled=.false then -- focus leaving? make sure we set the ComboBox
  do
     self~commitCellUpdate(jself)   -- now let us finish up
     jSelf~getTableView~refresh     -- MUST on Java_FX8, otherwise cached values distort cell's value on focus traversals
  end


::method startEdit
  expose selfClass jself comboBox isCancelled bAlreadyCommitted
  use arg slotDir

  jself=slotDir~javaObject       -- get Java peer object
  index=jself~getIndex
  if .my.App~tableViewData~get(index)~isNil then
  do
     .my.app~alert.audioclip~play   -- use JavaFX to have an "oops" clip played on all platforms alike
     return
  end

  oriValue=jself~getText         -- save current text value from cell, before forwardToSuper
  jself~startEdit_forwardToSuper -- will cause the event to fire, in case there are registered event handlers

  if comboBox~isNil then       -- create a comboBox and assign filter and focus listener
  do
     comboBox=.bsf~new("javafx.scene.control.ComboBox")
     comboBox~setItems(.person~expertChoices)

      -- create a RexxComboListCell to be used for the button in edit mode (allows graphic
      -- and text to be displaced like in the drop-down list as using the same implementation
     listCell=.RexxComboBoxCellFactory~clzOoRexxComboBoxListCell~new(.RexxComboBoxListCell~new)
     listCell~setConverter(.RexxComboBoxCellFactory~stringConverter)
     comboBox~setButtonCell(listCell)

     -- the ComboBox uses a ListView, which we now want to format such that the graphic is shown as well
     comboBox~setCellFactory(BsfCreateRexxProxy(.RexxComboBoxCellFactory~new, , .fx.Callback))
     rp=BSFCreateRexxProxy(self, ,"javafx.beans.value.ChangeListener" ,"javafx.event.EventHandler")
         -- ATTENTION: must be an event filter, otherwise ESCAPE key event may be consumed by someone
         -- else and cannot be seen in the bubbling up phase of a plain setEvent-handler
     jself~addEventFilter(.fx.KeyEvent~key_pressed, rp)
         -- this will notify us of the changed message upon focus change (e.g. TAB, ESCAPE, mouse click)
     comboBox~focusedProperty~addListener(rp)
  end

  comboBox~setValue(oriValue) -- set comboBox to the cell's value
  jself~setText(.nil)         -- remove text field
  jself~setGraphic(comboBox)  -- add comboBox UI
  comboBox~requestFocus

  isCancelled=.false          -- we set this, if cancelEdit gets invoked
  bAlreadyCommitted=.false    -- if changes occur in Cell allow to commit them


::method updateItem
  expose comboBox columnName
  use arg item, empty, slotDir

  jSelf=slotDir~javaObject
  index=jSelf~getIndex
  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "/// -->" self"::".context~name": jself:" pp(jself) "columnName="pp(columnName) "| item="pp(item)",empty="pp(empty)",index="pp(index)

  jSelf~updateItem_forwardToSuper(item,empty)   -- will cause the event to fire, in case there are registered event handlers

  if item=.nil | empty=.true then
  do
     jSelf~setText(.nil)
     jSelf~setGraphic(.nil)
  end
  else   -- we have a value and are not empty
  do
  -- eventuell: ~isEditing die comboBox, sonst den Text

     if \comboBox~isNil then
     do
        comboBox~setValue(self~getDate(item))
        jself~setText(.nil)
        jself~setGraphic(comboBox)
     end
     else
     do
        jself~setText(item)
        jImageView=.fx.ImageView~new(.graphics~entry(item))
        jSelf~setGraphic(jImageView ~~setPreserveRatio(.true) ~~setFitHeight(30))
        jSelf~~setContentDisplay(.fx.contentDisplay~top)
     end
  end

   -- style the cell's background
   -- note: one can use a CSS definition like: ".list-cell:odd { -fx-background-color: #dbffdb; }"
  if index>-1, .my.app~tableview_stylesheet_kind=3, index//2  then      -- colorize the background of the odd entries
  do
      -- we need to make sure that any predefined style (like -fx-alignment) remains intact
     jSelf~setStyle(jself~getStyle .my.app~tableview_style_kind3)
  end



/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) used by PropertyValueFactory */
/* this supplies the ComboBox Cell objects (items) and sets up callbacks for formatting them */
/* only needed, if we set a factory for the ComboBox instance                 */
::class RexxComboBoxCellFactory -- Rexx class implementing "javafx.util.Callback"'s "call" method

/* -------------------------------------------------------------------------------- */
::attribute clzOoRexxComboBoxListCell class get  -- a class attribute
::attribute stringConverter   class get  -- a class attribute

::method init class     -- the Rexx constructor at the class level: create and memorize a proxy class for "javfx.scene.control.ListCell"
  expose clzOoRexxComboBoxListCell stringConverter
   -- use a TextFieldListCell to allow in place edits
  clzOoRexxComboBoxListCell=bsf.createProxyClass("javafx.scene.control.cell.TextFieldListCell", "jRexxComboBoxListCell", "javafx.scene.control.Cell updateItem")
   -- we need a stringConverter for in-place edits (in case the ComboBox was set to editable)
  stringConverter=.bsf~new("javafx.util.converter.DefaultStringConverter")

/* -------------------------------------------------------------------------------- */
  /* create and setup a cell, assign stringConverter for in-place edits */
::method call           -- implements "javafx.util.Callback.call(Object o)": in this case an instance of a ListCell (or a subclass of it) must be returned!
  use arg obj, slotDir
               -- create the ListCell handler, supply a RexxComboBoxListCell object to carry out "updateItem"
  myClz=self~class      -- get Rexx class object to ease access to its class attributes
  RexxComboBoxCellHandler=myClz~clzOoRexxComboBoxListCell~new(.RexxComboBoxListCell~new)  -- will have updateItem() invoked
  RexxComboBoxCellHandler~setConverter(myClz~stringConverter) -- needed for edits with TextFieldListCell

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "///-->" self pp(obj)", RETURNING:" pp(RexxComboBoxCellHandler)

  return RexxComboBoxCellHandler


/* =================================================================================== */
/* This is an ooRexx class, subclassing the JavaFX "javafx.scene.control.ListCell" class
   in order to become able to edit/format ListCells by intercepting the "updateItem" method.
*/
::class RexxComboBoxListCell     -- this formats the ComboBox ListView items (cells)
::method updateItem
  -- void ListCell.updateItem (SimpleStringProperty name, boolean empty)
  use arg name, empty, slotDir

  jSelf=slotDir~javaObject
  index=jSelf~getIndex     -- get 0-based index (row) of this cell

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "///-->" self"::".context~name": jself:" pp(jself) "| name="pp(name)",empty="pp(empty)",index="pp(index)

     -- we need to invoke the method in the superclass first
  jSelf~updateItem_forwardToSuper(name,empty)
  jself~setStyle("")

  if name=.nil | empty=.true then
  do
     jSelf~setText(.nil)
     jSelf~setGraphic(.nil)
  end
  else   -- we have a value and are not empty
  do
     jSelf~setText(name)

     jImageView=.fx.ImageView~new(.graphics~entry(name))
     jSelf~setGraphic(jImageView ~~setPreserveRatio(.true) ~~setFitHeight(20))
     jSelf~~setContentDisplay(.fx.contentDisplay~left)
  end

   -- style the cell's background
   -- note: one can use a CSS definition like: ".list-cell:odd { -fx-background-color: #dbffdb; }"
  if index>-1, .my.app~listview_stylesheet_kind=3 then      -- colorize the background of the odd entries
  do
      -- we need to make sure that any predefined style (like -fx-alignment) remains intact
     if index//2 then jSelf~setStyle(jself~getStyle .my.app~tableview_style_kind3)
  end




/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2020 Rony G. Flatscher

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

