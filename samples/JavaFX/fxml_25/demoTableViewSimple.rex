#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2020-07-15

   changes: 2020-08-12, rgf: - changing ppCondition2() to the new BSF.CLS routine ppJavaExceptionChain()
                             - removing dependency on "rgf_util2.rex"
            2021-06-19, rgf: - removed unused class RexxCellValueFactory

            2022-04-20, rgf: - demoTableViewSimple.fxml - putting call to "demoTableViewSimpleController.rex"
                               before the children section to make its public members available
                               to all event code

            2022-08-09, rgf: - force picking remove(int) version by using box.strictArg()
                             - corrected wrong "\selectedIndex~isNil" with "selectedIndex>-1"
                             - at startup make sure that tableViewData is empty to match display
                             - correcting interface logic errors (in menu and popup menu):
                               - added missing listener to id_menu_remove
                               - make sure id_tableview gets set to tableViewData on first id_menu_add
                               - make sure CTL-Click only operational if in edit mode

             2022-08-11, rgf: - demoTableViewSimple.fxml: changed "select" property of 'CheckMenuItem
                                fx:id="id_menu_show_debug"' to "false", henc start out without
                                debug mode (change to "true" to see the debug output on start-up
                                of this program)

   purpose: demonstrate - TableView with editing, binding Person's simple properties with cell values
                        - Setting TableView item to null as a whole as opposed to removing the item
                        - Enabling/disabling menu options
                        - ContextMenu and demonstrating an ooRexx routine to serve as the callback
                          target from Java right before the context menu is displayed to allow for
                          dis/enabling the menu items to match the File's menu items
                        - Playing AudioClip (works on all operating systems) if clicking on a
                          displayed cell that is not backed by data
                        - Animation

   additional infos on the web, e.g.:

         <https://docs.oracle.com/javafx/2/ui_controls/table-view.htm>, 2020-07-15

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx           demoTableViewSimple.rex
         rexxj.{cmd|sh} demoTableViewSimple.rex

   needs: ooRexx 5.0 or higher, and BSF4ooRexx 6.00 or highter
*/


/** A <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableView.html">TableView</a>
    consists of one or more
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableColumn.html">TableColumn</a>s
    of
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableCell.html">TableCell</a>s,
    each representing an <em>item</em> (object with fields represented by the <em>TableColumn</em>s) from the backing
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/collections/FXCollections.html#observableArrayList--">ObservableArrayList</a>.

    <br>

    A <em>TextView</em> will create cells for its <em>TableColumn</em>s by using a
    <em>cell value factory</em> (cf. callback method <code>change</code>)
    which creates and returns the cells to be used by the <em>TextView</em>, cf. the
    <code>RexxCellFactory</code> class. In this implementation the
    JavaFX class <a href="javafx.scene.control.cell.TextFieldTableCell">TextFieldTableCell</a>
    gets used in order to become able to edit the cell values which get bound to the
    simple properties of an ooRexx person object. As a result cell changes (user must press the
    &gt;enter&lt; key) are automatically reflected in the person's ooRexx object and vice versa,
    changes to a person's simple property get reflected in the corresponding cell.
*
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

-- call bsf.import    "javafx.scene.control.TableCell",              "fx.TableCell"

call bsf.import    "javafx.scene.control.cell.TextFieldTableCell","fx.TextFieldTableCell"
call bsf.import    "javafx.util.converter.DefaultStringConverter","fx.DefaultStringConverter"

   -- create Rexx object that will control the application
rexxApp=.RxMainApplication~new
.my.app~mainApp=rexxApp       -- store the Rexx MainApp object in .my.app

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxApp
jRexxApp=BsfCreateRexxProxy(rexxApp, ,"javafx.application.Application")

signal on syntax
   -- launch the application, invoke "start" and then stay up until the application closes
jRexxApp~launch(jRexxApp~getClass, .nil)  -- need to use this version of launch in order to work
call sysSleep 0.01            -- let ooRexx clean up a little bit
exit

syntax:
   co=condition("object")
   say ppJavaExceptionChain(co, .true) -- display Java exception chain and stack trace of original Java exception
   say " done. "~center(100, "-")
   exit -1
                              -- the information in a condition object to ease debugging
::requires "BSF.CLS"          -- get Java support


/* =================================================================================== */
/** This Rexx class implements the abstract JavaFX class
    <a href="javafx.application.Application">Application</a> by implementing the
    abstract method <code>start</code> that sets up the JavaFX GUI.
*/
::class RxMainApplication

/** This Rexx method will be called by the <em>launch</em> method and allows to set up
    the JavaFX application. It loads the FXML document which defines the GUI elements
    declaratively, adds an animation to it, creates the scene and displays it in the
    supplied <em>primaryStage</em>.

    @param primaryStage stage (window) supplied by <em>Application</em>'s
                        <code>launch</code> method
    @param slotDir      not used (a Rexx directory supplied by BSF4ooRexx)
*/
::method start    -- will be invoked by the Java "launch" method
  expose locale primaryStage
  use arg primaryStage  -- we fetch the stage (Window) to use for our user interface

   -- set window's system icon and title text
  primaryStage~getIcons~add(.bsf~new("javafx.scene.image.Image", "file:oorexx_032.png"))
  primaryStage~setTitle("TableView with Graphics (ooRexx)")

  .my.app~tableview_stylesheet_kind=0     -- we start out without customized formatting
   -- define the style for stylesheet style kind=3:
  .my.app~tableview_style_kind3="-fx-background-color: #dbffdb; -fx-text-fill: black;"

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  demoTableViewSimpleUrl=.bsf~new("java.net.URL", "file:demoTableViewSimple.fxml")
  rootNode        =.fx.FXMLLoader~load(demoTableViewSimpleUrl) -- load the fxml document
   -- get current selection state from corresponding menu object, now that .my.app is populated
  .my.app~showDebugOutput=.my.app~demoTableViewSimple.fxml~id_menu_show_debug~isSelected
   -- as .my.app got populated, all the FXML fx:id objects are available to Rexx
  call setupApplicationData   -- now that the graphics subsystem is initialized
  .my.app~tableViewData~clear  -- make sure list is empty

   -- demo action! :) will fade on and off for a minute while user can interact with GUI!
  ft=.bsf~new("javafx.animation.FadeTransition", .fx.Duration~millis(1000),rootNode)
  ft ~~setFromValue(.2) ~~setToValue(1) ~~setCycleCount(3) ~~setAutoReverse(.true) ~~play

  scene=.fx.scene~new(rootNode)   -- create a scene from the parsed FXML tree
  primaryStage~setScene(scene)         -- set the stage to our scene

   -- this will setup and register the event handlers
  .demoTableViewSimpleController~new
  primaryStage~show              -- show the stage (and thereby its scene)


/* =================================================================================== */
/** This Rexx class manages the user interaction with the FXML form named
    &quot;demoTableViewSimple.fxml&quot;.
*/
::class demoTableViewSimpleController

/** Constructor method that caches the JavaFX objects in ooRexx attributes for easier access
    and sets up (configures) the four <em>TableColumns</em>. */
::method init
  expose id_vbox id_tableview id_menu_setitems id_menu_clear id_menu_quit  -
         id_menu_remove id_label id_menu_show_debug id_menu_enable_editing -
         id_button_add id_button_delete

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them to attributes
  appDir=.my.app~demoTableViewSimple.fxml

  if .my.app~showDebugOutput=.true then
    call dumpDir appDir, .line":" .dateTime~new self"::".context~name": .my.app~demoTableViewSimple.fxml:"

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
  id_menu_setItems  ~setOnAction(rp)
  id_menu_remove    ~setOnAction(rp)
  id_menu_clear     ~setOnAction(rp)
  id_menu_quit      ~setOnAction(rp)

  /* create and assign context menu */
  ctxtMenu=createContextMenu(rp)
  id_tableview~setContextMenu(ctxtMenu)

  /* set callback for button press actions */
  id_button_add   =appDir~id_button_add    ~~setOnAction(rp)
  id_button_delete=appDir~id_button_delete ~~setOnAction(rp)


  /* now set up the four TableColumns */
  /* - a CellFactory creates the TableCells for the column and can be used
       for formatting the display of it
     - a CellValueFactory gets used to fetch the cell's value from the item
       in the ObservableArrayList that backs the TableView
  */
  id_column_nr=appDir~id_column_nr     -- get TableColumn
  id_column_nr~setEditable(.false)     -- user must not edit the "nr" (id) column
      -- will create and return the TableCell that TableView is to use
  id_column_nr~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("nr"), , .fx.Callback))
      -- will return the value that gets used by the TableCell in an Observable
  id_column_nr~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("nr"), ,.fx.Callback))
   ----------------------------------------------------------

  id_column_name=appDir~id_column_name -- get TableColumn
      -- will create and return the TableCell that TableView is to use
  id_column_name~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("name"), , .fx.Callback))
      -- will return the value that gets used by the TableCell in an Observable
  id_column_name~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("name"), ,.fx.Callback))
   ----------------------------------------------------------

  id_column_expertise=appDir~id_column_expertise   -- get TableColumn
      -- will create and return the TableCell that TableView is to use
  id_column_expertise~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("expertise"), , .fx.Callback))
      -- will return the value that gets used by the TableCell in an Observable
  id_column_expertise~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("expertise"), ,.fx.Callback))
   ----------------------------------------------------------

  id_column_since=appDir~id_column_since     -- get TableColumn
      -- will create and return the TableCell that TableView is to use
  id_column_since~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new("since"), , .fx.Callback))
      -- will return the value that gets used by the TableCell in an Observable
  id_column_since~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("since"), ,.fx.Callback))
   ----------------------------------------------------------


/** Selects and focuses a <em>TableCell</em> in a specific <em>TableColumn</em> for a specific row.

   @param pos  0-based position (row) of the <em>TableCell</em> to select and focus
   @param tableColumn the <em>TableColumn</em> to work with
*/
::method selectTableColumnToEdit
  expose id_tableview
  use strict arg pos, tableColumn

  if .my.app~showDebugOutput=.true then   -- show extensive information about event
     say .line":" .dateTime~new "\\\" self"::".context~name";" "arg()="pp(arg()) "| pos="pp(pos)", nameTableColumn="pp(nameTableColumn)

  id_tableView~requestFocus
  id_tableView~getSelectionModel~select(pos,tableColumn)
  id_tableView~getFocusModel~focus(pos,tablecolumn)   -- if not put in edit mode, <enter> or mouse-click will do that
   -- we could put the cell directly into edit mode with: "id_tableView~edit(pos,tableColumn)"


/** Event handler for most events that are of interest for our little application.
    @param event the JavaFX event object
    @param slotDir      not used (a Rexx directory supplied by BSF4ooRexx)
*/
::method handle   /* implements the interface "javafx.event.EventHandler"   */
  expose id_tableview id_menu_setitems id_menu_clear id_menu_quit id_menu_remove id_label -
         id_menu_show_debug id_menu_enable_editing id_button_add id_button_delete
  use arg event

  source=event~source
  target=event~target
  eventType=event~eventType~toString
  isMouseEvent=(event~objectname~startsWith("javafx.scene.input.MouseEvent@"))

  if .my.app~showDebugOutput=.true then   -- show extensive information about event
  do

    say
    say .line":" .dateTime~new "\\\" self"::".context~name"; eventType="pp(eventType) "event:" pp(event)
    say "09"x .line":" "    event~source="pp(source) "~toString:"pp(source~toString)
    if target=.nil then say "09"x .line":" "    event~target="pp(target) "<--- !"
                   else say "09"x .line":" "    event~target="pp(target) "~toString:"pp(target~toString)
    say "09"x .line":" "    event~toString:" pp(event~tostring)

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
      mb~append("@ eventSource: x=",event~getX,      "/y=",event~getY,"/z=",event~getZ)

      say "09"x .line":" "MOUSE_CLICKED -->" pp(mb~string)

      selectedIndex=id_tableView~getSelectionModel~getSelectedIndex
      selectedItem =id_tableView~getSelectionModel~getSelectedItem
      say "09"x .line":" "selectedIndex:" pp(selectedIndex) "selectedItem:" pp(selectedItem)
      say "09"x .line":" "event~eventType~toString="pp(eventType)", event~button~toString="pp(event~button~toString)", event~getClickCount="pp(event~getClickCount) "\\\"
      say "09"x .line":" "/// ---> pickResult~intersectedNode-Infos:"
      say "09"x .line":" ppNodeInfo(event)
      say "09"x .line":" "\\\ <---"
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
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              .my.app~showDebugOutput=id_menu_show_debug~isSelected
              return
           end

     when tgtId="id_ctxt_menu_show_debug" then  -- reflect change from context menu in File menu item as well
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              showDebugOutput=\id_menu_show_debug~isSelected
              id_menu_show_debug~setSelected(showDebugOutput)
              .my.app~showDebugOutput=showDebugOutput
              return
           end

     when tgtId="id_menu_enable_editing" then   -- get selected-state, use it for setting TableView edit mode
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              isSelected=id_menu_enable_editing~isSelected
              id_tableview~setEditable(isSelected)
              id_button_add   ~setDisable(\isSelected)
              id_button_delete~setDisable(\isSelected)
              id_menu_enable_editing~setSelected(isSelected)
              return
           end

     when tgtId="id_ctxt_menu_enable_editing" then -- reflect change from context menu in File menu item as well
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              isSelected=event~source~isSelected   -- get ContextMenu's CheckMenuItem object
              id_tableview~setEditable(isSelected)
              id_button_add   ~setDisable(\isSelected)
              id_button_delete~setDisable(\isSelected)
              id_menu_enable_editing~setSelected(isSelected)
              return
           end

     when tgtId="id_menu_setitems" then   -- fill the TableView, set the menu and button states
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              call resetTableViewData     -- reset the tableviewdata to inital state, such setitems will show them all again
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
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              do idx=tableViewData~size-1 to 0 by -1     -- remove empty cells
                 if tableViewData~get(idx)~isNil then
                 do
                     -- there are different remove() methods to pick from, unfortunately
                     -- also one remove(Object) which might get picked instead of the
                     -- remove(int) version that we need here; to force remove(int) we
                     -- can either use the message tableViewData~bsf.invokeStrict('remove', 'int',idx)
                     -- or create a RexxStrictArgument of type 'int' with box.strictArg('int',idx)
                    tableViewData~remove(box.strictArg('int',idx))  -- make sure integer version of remove() gets used
                 end
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
              id_menu_enable_editing~setSelected(.false)
              id_label~setText("TableView has" tableViewData~size "row(s)")

              return
           end

     when tgtId="id_menu_clear" then
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              tableViewData~clear
              id_menu_setitems~setDisable(.false)
              id_menu_remove  ~setDisable(.true)
              id_menu_clear   ~setDisable(.true)
              id_label~setText("TableView has" tableViewData~size "cell(s)")
              id_button_add   ~setDisable(.true)
              id_button_delete~setDisable(.true)
              id_menu_enable_editing~setSelected(.false)
              return
           end

     when tgtId="id_menu_quit" then
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              self~handleExit
              return
           end

      when tgtId="id_button_add" then
            do
               if .my.app~showDebugOutput=.true then
                  say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
               iin=.my.app~imageIndexNames
               if tableViewData~size=0 then   -- if yet empty, make sure id_listview gets set to listViewData
                  id_tableview~setItems(tableViewData)
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
                  say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)
               selectedIndex=id_tableView~getSelectionModel~getSelectedIndex

               if .my.app~showDebugOutput=.true then
                  say "09"x .line":" "id_button_delete, selectedIndex="pp(selectedIndex)

               if selectedIndex>-1 then
               do
                  -- there are different remove() methods to pick from, unfortunately
                  -- also one remove(Object) which might get picked instead of the
                  -- remove(int) version that we need here; to force remove(int) we
                  -- can either use the message listViewData~bsf.invokeStrict('remove','int',idx)
                  -- or create a RexxStrictArgument of type 'int' with box.strictArg('int',idx)
                  tableViewData~remove(box.strictArg('int',selectedIndex))  -- force remove(int) version to be used!

                  id_tableView~refresh
                  id_label~setText("TableView has" tableViewData~size "row(s)")
               end
               else if .my.app~showDebugOutput=.true then
                   say "09"x .line":" "id_button_delete, selectedIndex="pp(selectedIndex) "(nothing to delete)"
               return
            end

     when eventType="EDIT_CANCEL" then    -- force a refresh to make sure UI gets syncrhonized with state
           do
              if .my.app~showDebugOutput=.true then
                 say "09"x .line":" "*** handling" pp(eventType) "for" pp(tgtId)

              id_tableview~refresh        -- force a refresh of the TableView
              return
           end

     otherwise nop
  end

   -- if Ctrl-click on a cell with a value, then set it to .nil, otherwise reinstate value
  idx=getIndex(event)
  if idx>-1, isMouseEvent, event~getClickCount=1 then
  do
      if idx<tableViewData~size, event~isControlDown, id_menu_enable_editing~isSelected then      -- clicked cell index is within bounds
      do
         val=tableViewData~get(Idx)

         if .my.app~showDebugOutput=.true then
            say "09"x .line":" "*** handling" pp("Ctl-"eventType) "for index="pp(idx)", value="pp(val)

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
            tableViewData~set(idx, .nil)     -- nullify
            id_menu_remove~setDisable(.false)-- enable "remove" menu
         end
      end
      else  -- left mouse button click at a cell outside of the range of our currently defined data
      do
         if (event~isControlDown & \id_menu_enable_editing~isSelected) | -
            (event~button~toString="PRIMARY" & idx>(tableViewData~size-1)) then   -- this way only left mouse button clicks
         do
            call beep 250,100             -- beep via Rexx (may not beep on some Unixes)
            .my.app~alert.audioclip~play  -- use JavaFX to have an "oops" clip played on all platforms alike!
         end
      end
  end

  if .my.app~showDebugOutput=.true then   -- show current entries in ObservableArrayList
  do
     say "09"x .line":" "// iterate over cells (observableArrayList):"
     if \tableViewData~isNil then
     do
        say "0909"x"current tableViewData~size:" pp(tableViewData~size)
        do i=0 to tableViewData~size - 1 --  o over id_tableview~getItems
            o=tableViewData~get(i)
            info=""
            if o~objectName~pos("RexxProxy@")>0 then info=pp(BsfRexxProxy(o)~makestring)
            else if \o~isNil, o~isA(.bsf) then info=pp("o~toString="o~toString)
            if info~pos(o~objectname)>0 then
               say "0909"x"    " i~right(3)":" pp(o)
            else
               say "0909"x"    " i~right(3)":" pp(o) info
        end
     end
  end

  if eventType~startsWith("EDIT") then    -- handle edit_cancel event, show information about edit events
  do
     idx=event~getIndex
     if .my.app~showDebugOutput=.true then
         .error~say("09"x .line ":" "//" pp(eventType) "event: ~getIndex=" pp(idx)", event~getSource~getEditingIndex="pp(event~getSource~getEditingIndex))

     if eventType="EDIT_CANCEL" then   -- force updateItem
              id_tableview~refresh
        -- event~getSource~refresh  -- force a refresh of the TableView
  end


 /** Closes the JavaFX application. */
::method handleExit
  bsf.loadClass("javafx.application.Platform")~exit   -- unload JavaFX, but let Rexx continue in main thread


/* =================================================================================== */

/** An ooRexx class representing persons that get displayed in the <em>TableView</em>.
    The attribute values are JavaFX simple properties which allows the <em>TableView</em>
    to bind the table cells to them. If the user edits a cell value the binding will
    automatically update the simple property automatically.
*/
::class Person

/* -------------------------------------------------------------------------------- */
/** An array of expertises options. */
::attribute expertises  class    -- array of expertise options

/** A <em>StringTable</em> storing the JavaFX image objects with their corresponding
    name of the expertise created from the icons in the application's directory.
*/
::attribute graphics class    -- stringtable (directory) of expertise->Image/Graphic

/** An <em>ObservableArrayList</em> containing the Rexx person objects that are
    used initially for the <em>TableView</em>. */
::attribute testData class    -- array of generated persons

/** An <em>ObservableArrayList</em> containing the Rexx person objects that are
    used initially for the <em>TableView</em>. */
::attribute expertiseChoices class  -- ObservableArrayList of expertise choices

/** Class constructor that sets up and caches data (collections and attributes) for later use.
*/
::method    init     class
  use local tmp               -- except of 'tmp' all variables are attributes
  graphics=.stringtable~new
  testData=.array~new
   -- set up expertise names and their corresponding image files
  expertises  = "Bug", "Java", "Linux", "OpenSource", .nil  -- .nil indicate non-expertises
  imageFileNames=.array~of("icons8-bug-64.png","icons8-java-64.png", -
                       "icons8-linux-64.png", "icons8-open-source-64.png")

   -- create an ObservableList with the choices of expertises to be listed in the ComboBoxTableCell
  .context~package~local~nilString="<null>"
  expertiseChoices=bsf.loadClass("javafx.collections.FXCollections")~observableArrayList
  do tmp over expertises
     if tmp~isNil then
        expertiseChoices~add(.nilString)
     else
        expertiseChoices~add(tmp)
  end

   -- define first and last names to be randomly picked and combined
  arrFirstName="Anton", "Berta", "Caesar", "Dora", "Emil", "Friedrich", "Gustav", "Heidelinde", "Ida"
  arrFirstNameCount=arrFirstName~items
  arrLastName ="Barmwater", "Cowlishaw", "Davis", "Flatscher", "Fuller", "Jansen", "Jonsson", "McGuire", "Pichler", "Steinboeck", "Wolfers"
  arrLastNameCount=arrLastName~items

  expertiseCount=expertises~items
  counter=0

/** Getter class attribute that increments and then returns the counter value used for the
    &quot;nr&quot; attribute.

    @return a new unique counter value
*/
::attribute counter get class
  expose counter
  counter+=1
  return counter

/** A class method that generates and returns a new <em>Person</em> instance.

   @return a new, generated <em>Person</em>
*/
::method createTestPerson class  -- returns a randomyl created Person instance
  expose expertises expertiseCount arrFirstName arrFirstNameCount arrLastName arrLastNameCount

  currentYear=.dateTime~new~year -- get current year
  name=arrLastName[random(1,arrLastNameCount)]"," arrFirstName[random(1,arrFirstNameCount)]
  exp=expertises[random(1,expertiseCount)]  -- get expertise status
  i=self~counter                 -- get next value
  if exp~isNil then
     p=.person~new(i,name)
  else   -- expertise status set, create a "since" value in ISO date
  do
     since=random(1997,currentYear)"-"random(1,12)~right(2,0)"-"random(1,28)~right(2,0)
     p=.person~new(i,name,exp,since)
  end
  return p


/** A class method that generates test persons and sets up the image data for the
    icons to be displayed with the expertise.
*/
::method    createTestdata class    -- will run after all class objects got iniitialized
  expose graphics testData imageFileNames expertises

  if graphics~items=0 then    -- create images, store them under the corresponding expertise's name
  do
     do counter i name over expertises
        if name~isNil then iterate
        img=.fx.Image~new("file:"imageFileNames[i]) -- create an Image and store it
        graphics~setEntry(name,img)
     end
     .context~package~local~graphics=graphics   -- save in package local
  end

  -- create a few persons, add them to the observable list, will cause the created
  -- Rexx objects to be autoboxed as Java RexxProxy objects by BSF4ooRexx
  do i=1 to 5
     testData~append(self~createTestPerson)
  end


/* -------------------------------------------------------------------------------- */
/** This attribute serves as the primary key in a <em>SimpleIntegerProperty</em>. */
::attribute nr
/** This attribute stores the name in a <em>SimpleStringProperty</em>. */
::attribute name
/** This attribute stores the expertise of the person in a <em>SimpleStringProperty</em>. */
::attribute expertise
/** If an expertise is set for the person, then this attribute stores the date since
    when it was acquired/assigned in a <em>SimpleStringProperty</em>. */
::attribute since

::method makeString
  use local          -- all variables are attributes
  return "a Person:[nr="nr~get",name="name~get",expertise="expertise~get",since="since~get"]"

::method init        -- fetch person's attribute values
  expose nr name expertise since
  use strict arg valNr, valName, valExpertise=.nil, valSince=.nil

   -- this version uses JavaFX properties which the TableView will bind and update its values if necessary
  nr       =.fx.simpleIntegerProperty~new(valNr)   -- allows TableView to sort correctly by number
  name     =.fx.simpleStringProperty ~new(valName)
  expertise=.fx.simpleStringProperty ~new(valExpertise)
  since    =.fx.simpleStringProperty ~new(valSince)

  if .my.app~showDebugOutput=.true then
     say .line":" .datetime~new "///   " self


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) for PropertyValueFactory */

/* This class allows instances that remember the message to be sent to person instances to
   return the property of the attribute that should be shown in the table cell.
*/
::class PropertyValueFactory
::method init
  expose  columnName --handler -- name of property getter method
  use strict arg columnName      -- memorize columnName

::method call
  expose columnName  -- handler
  use arg o          -- an observable value for the ooRexx person object boxed in a Java RexxProxy object

  if o~getValue~isNil then return .nil
  aRexxObj=BsfRexxProxy(o~getValue) -- unbox Rexx object
  /*
  if aRexxObj~isNil then            -- entire entry is .nil
     return .nil
*/
  return aRexxObj~send(columnName)  -- get attribute's value from the Rexx object


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) used e.g. by PropertyValueFactory */

/** This class can be used to define specific <em>TableCell</em> implementations for a column,
    if we want to allow specific behaviour like specific formatting of the cells or using
    specific JavaFX controls in editing mode of the cell (like e.g., a TextField, a DatePicker
    or a ComboBox) or allowing autocommit-behaviour in editing mode (in addition to <em>ENTER</em>
    also <em>TAB</em> and focus-change with a mouse-click commit editing, i.e. writing back
    the changed value).

    <p>
    This implementation uses Rexx <em>TableCell</em> implementations which supply the
    Java proxy class in the class attribute named <code>proxyClass</code> and the matching
    string converter in the class attribute named <code>stringConverter</code>.
*/
::class "RexxCellFactory" -- Rexx class implementing "javafx.util.Callback"'s "call" method

/** This constructor memorizes the column name (the Rexx object's attribute name)
    which this instance serves.

    @param columnName save the attribute name for determining the type of <em>TableCell</em>
           to create
*/
::method init
  expose columnName
  use strict arg columnName


/** This callback method will create a <em>TableCell</em> from the Rexx classes
    <code>.RexxTableCell</code> (used for 'id' and 'name' cells),
    <code>.RexxComboBoxTableCell</code> (used for 'expertise' cells), and
    <code>.RexxDatePickerTableCell</code> (used for 'since' cells), set the
    corresponding string converters and set the tooltip on each.

    @param  tableColumn not used
    @param  slotDir     not used (a Rexx directory supplied by BSF4ooRexx)

    @return a <em>TableCell</em> with a type depending on the columnName/attribute to serve
*/
::method call           -- implements "javafx.util.Callback.call(Object o)": in this case an instance of a TableCell (or a subclass of it) must be returned!
  expose columnName     -- the name of the column/attribute we have to serve

  if .my.app~showDebugOutput=.true, columnName="nr" then
  do
     say
     say "09"x .line": ... create cells for a new record ..."
  end

  jCell=.fx.TextFieldTableCell~new
  if columnName<>"nr" then
     jCell~setConverter(.fx.DefaultStringConverter~new)  -- assign string converter to use

  jCell~setTooltip(.my.app~cell.tooltip)     -- assign tooltip

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "///<--" self"::call" "for" pp(columnName)~left(11)  "| RETURNING jCell:" pp(jStrip(jCell))

  return jCell         -- return the Java cell


/* =================================================================================== */
/** This routine can only work, once the internal graphics of JavaFX got initilaized
   (e.g. in the Application's start method). It sets up the items to be viewed in the
   <em>TableView</em> and saves all collections in the globally available
   <em>.MY.APP</em> directory (globally available as it is stored in <em>.environment</em>).
*/
::routine setupApplicationData
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
     img=.fx.Image~new("file:"imageNames[i])
     imageViewDir~setEntry(name,img)
     imageViewDir~setEntry(imageIndexNames[i],img)
  end

   -- save in directory accessible via the environment symbol .my.app
  .my.app~entryNames     =entryNames
  .my.app~imageIndexNames=imageIndexNames
  .my.app~imageViewDir   =imageViewDir

  .my.app~tableViewData=tableViewData
  .my.app~alert.audioclip=.bsf~new("javafx.scene.media.AudioClip", "file:oops.wav")

  tipText="If editing enabled: <enter> or click to edit," "0a"x"Ctl-click to nullify row/undo row nullify"
  tooltip =.bsf~new("javafx.scene.control.Tooltip", tipText)
  imgView=.fx.ImageView~new("file:icons8-mailbox-opened-flag-up-64.png")
  imgView~~setPreserveRatio(.true) ~~setFitHeight(50)
  tooltip~~setGraphic(imgView)~~setGraphicTextGap(15)
   -- Cf. <https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html> (as of 2020-07-05)
  tooltip~setStyle("-fx-font-size: 14pt; -fx-text-fill: black; -fx-padding: 5; -fx-background-color: moccasin ;")
  .my.app~cell.tooltip=tooltip


/* =================================================================================== */
/** This routine clears and sets the <em>tableViewData</em> content to the test data
    created at startup of this application.
*/
::routine resetTableViewData   -- clears .app.dir~tableViewData and sets its entries according to .my.app~entryNames
  tableViewData=.my.app~tableViewData
  tableViewData~clear      -- clear
  do counter i o over .person~testData
     tableViewData~add(o)  -- ObservableArrayList to be displayed in the list
  end


/* =================================================================================== */
/* This routine sets up the context menu. We cannot refer to existing <em>MenuItems</em>
  (they will not be honored/shown as already being used in the application's <em>Menu</em>),
  therefore we need to create proper context <em>MenuItems</em>.

  @param eventHandler the RexxProxy to be used for callbacks when menu items get selected
  @return the created <em>ContextMenu</em>
*/
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
  items~add(fxMI~new("Fill TableView with Testdata")~~setId("id_menu_setitems")~~setOnAction(eventHandler) )
  items~add(fxMI~new("Remove Empty Rows") ~~setId("id_menu_remove")  ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Clear TableView")     ~~setId("id_menu_clear")   ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Quit")            ~~setId("id_menu_quit")    ~~setOnAction(eventHandler) )

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "\\\"  "createContextMenu(eventHandler): ctxtMenu~toString="pp(ctxtMenu~toString)

   -- use an ooRexx routine object as the Java EventHandler for the context menu
  jeh=bsfCreateRexxProxy(.routines~contextMenuHandler,,"javafx.event.EventHandler")
  ctxtMenu~setOnShowing(jeh)     -- will fire right before PopupMenu window gets displayed
  return ctxtMenu                -- return the ContextMenu


/* ----------------------------------------------------------------------------------- */
/** Rather than using a class with methods to handle events for the context menu, this
   demonstrates using a routine for that very same purpose. This event handler will set all the
   <em>ContextMenu</em>'s <em>CheckMenuItem</em>s to the same disable state as the
   matching <em>CheckMenuItems</em> in the form's <em>File Menu</em>.

   @param  event    the JavaFX event
   @param  slotDir  not used (a Rexx directory supplied by BSF4ooRexx)
*/
::routine "ContextMenuHandler"     -- set disable state according to the File MenuItems state
  use arg event

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "   " .context~name"(event), event:" pp(event~toString)

  appDir=.my.app~demoTableViewSimple.fxml    -- get the directory with all current JavaFX objects having an fx:id
  ctxtMenu=event~source                -- get access to the ContextMenu object
  do menuItem over ctxtMenu~getItems   -- iterate over all ContextMenu MenuItems
      id=menuItem~getId                -- get ID (same as the one in the File's MenuItem)
      if \id~isNil then                -- separator does not have an id
      do
         select case id
            when "id_ctxt_menu_show_debug" then   -- the current state is contained in .showDebugOutput
               menuItem~setSelected(.my.app~showDebugOutput)

            when "id_ctxt_menu_display_text" then
               menuItem~setSelected(appDir~is_id_ctxt_menu_display_text)

            when "id_ctxt_menu_display_graphic" then
               menuItem~setSelected(appDir~is_id_ctxt_menu_display_graphic)

            when "id_ctxt_menu_enable_editing" then
               do
                  isSelected=appDir~id_menu_enable_editing~isSelected
                  menuItem~setSelected(isSelected)
               end

            otherwise   -- reflect current state from File menu items
            do
               state=appDir~send(id)~isDisable  -- fetch File's MenuItem, get its disable state
               menuItem~setDisable(state)       -- set matching popup MenuItem's disable state accordingly
            end
         end
      end
  end


/* =================================================================================== */
/** Return a string of the list of nodes from the intersected node to the root node.
   If a cell node is encountered query and show its <em>getIndex()</em> value.

   @param event to be analyzed for an intersected node which then is shown with its parent
                nodes if any
   @return a string with the node and all its parent nodes or <em>.nil</em> if no intersecting node
           contained in the event object
*/
::routine ppNodeInfo
   use arg event

   signal on syntax
   node=event~pickResult~getIntersectedNode
   if node=.nil then return .nil
   return ppNodePath(node)
syntax:
   return .nil

/** Creates and returns the node and all of its parent nodes.
   If a cell node is encountered query and show its <em>getIndex()</em> value.

   @param node the node to work with
   @return a string with the node and all its parent nodes or <em>.nil</em> if no intersecting node
           contained in the event object
*/
::routine ppNodePath
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
/** Find and return cell object from an event object.

   @param event to be analyzed for an intersected node which then gets resolved until
                a cell node is encountered
   @return cell node from the intersected node or <em>.nil</em> if no such node was found
*/
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
/** Find and return the cell's index value, if any.

   @param event to be analyzed for an intersected node which then gets resolved until
                a cell node is encountered which will be asked for its 0-based index value
   @return index value or <em>-1</em>, if none found
*/
::routine getIndex
   use arg event
      -- get the node of the event
   cell=findCellObject(event)
   if cell~isNil then return -1
   return cell~getIndex


/* =================================================================================== */
/** Routine to strop package name from Java object string.
   @param name the Java object string
   @return the Java object string without package name or string unaltered if no dot found
*/
::routine jStrip        -- remove package name from Java object strings
   parse arg name

   pos=name~lastPos(".")
   if pos=0 then return name     -- return unchanged
   return name~substr(pos+1)     -- return unqualified name


/* =================================================================================== */
/** Utility routine for debugging: dumps the content of a directory ordered by index.

   @param dir a Rexx directory to be dumped
   @param hint a string to be shown in the title
*/
::routine dumpDir
  if .my.app~showDebugOutput=.false then return
  use arg dir, hint=""

  say
  say "dumpDir():" hint
  do counter i n over dir~allindexes~sort
     say i~right(3)":" pp(n) pp(dir~entry(n))
  end
  say


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