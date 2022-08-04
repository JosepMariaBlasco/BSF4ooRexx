#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2020-07-12 (2020-06-22)

   changes: 2020-08-12, rgf: - changing ppCondition2() to the new BSF.CLS routine ppJavaExceptionChain()
                             - removing dependency on "rgf_util2.rex"

            2020-08-30, rgf: - demonstrate usage of BSF's bsf.exit-method

            2022-04-20, rgf: - demoListView.fxml - putting call to "demoListViewController.rex"
                               before the children section to make its public members available
                               to all event code

   purpose: demonstrate - ListView with cells containing text and graphics
                        - Setting ListView cell to null as opposed to removing the cell
                        - Setting ToolTips with a graphic and a text to the cells in the RexxCellFactory
                        - Enabling/disabling menu options
                        - ContextMenu and demonstrating an ooRexx routine to serve as the callback
                          target from Java right before the context menu is displayed to allow for
                          dis/enabling the menu items to match the File's menu items
                        - control displaying text and/or graphic in ListCell via the context menu
                        - Playing AudioClip (works on all operating systems) if clicking on a
                          displayed cell that is not backed by data
                        - Animation

   additional infos on the web, e.g.:

         <https://docs.oracle.com/javafx/2/ui_controls/list-view.htm>, 2020-06-21

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx demoListView.rex
         rexxj.{cmd|sh} tpgm_demoListView.rex

   needs: ooRexx 5.0 or higher, and BSF4ooRexx 6.00 or highter
*/


/** A <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html">ListView</a>
    consists of a single column of
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListCell.html">ListCell</a>s,
    each representing an <em>item</em> (object) from the backing
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/collections/FXCollections.html#observableArrayList--">ObservableArrayList</a>.

    <br>

    A <em>ListView</em> will create cells by using a <em>cell factory</em>
    (cf. callback method <code>change</code>)
    which creates and returns the cells to be used by the ListView, cf. the
    <code>RexxCellFactory</code> class. In this implementation the
    <code>RexxCell</code> class is used, which extends the JavaFX class
    <a href="javafx.scene.control.cell.TextFieldListCell">TextFieldListCell</a>
    in order to become able to represent the value as a text and with a graphic.
    It is responsible therefore to maintain the appearance of each cell
    (cf. callback method <code>updateItem</code>).

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

   -- import JavaFX classes that we may use more often
call bsf.import    "javafx.fxml.FXMLLoader",                      "fx.FXMLLoader"
call bsf.import    "javafx.scene.Scene",                          "fx.Scene"
-- call bsf.import    "javafx.beans.property.SimpleStringProperty",  "fx.SimpleStringProperty"

call bsf.import    "javafx.scene.image.Image",                    "fx.Image"
call bsf.import    "javafx.scene.image.ImageView",                "fx.ImageView"

call bsf.import    "javafx.collections.FXCollections",            "fx.FXCollections"
call bsf.loadClass "javafx.util.Callback",                        "fx.Callback"
call bsf.loadClass "javafx.util.Duration",                        "fx.Duration"


   -- create Rexx object that will control the application
rexxApp=.RxMainApplication~new
.my.app~mainApp=rexxApp        -- store the Rexx MainApp object in .my.app

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxApp
jRexxApp=BsfCreateRexxProxy(rexxApp, ,"javafx.application.Application")

signal on syntax
   -- launch the application, invoke "start" and then stay up until the application closes
jRexxApp~launch(jRexxApp~getClass, .nil)  -- need to use this version of launch in order to work

  -- .BSF-bsf.exit([returnCode][,msecsToWaitBeforeShuttingDownJava=100ms])
.bsf~bsf.exit     -- inhibit callbacks from Java to Rexx, shutdown JVM in 0.01 seconds
exit

syntax:
   co=condition("object")
   say ppJavaExceptionChain(co, .true) -- display Java exception chain and stack trace of original Java exception
   say " done. "~center(100, "-")
   exit -1

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
::method start    -- will be invoked by the "launch" method
  expose primaryStage
  use arg primaryStage  -- we get the stage to use for our UI

  -- set window's system icon and title text
  primaryStage~getIcons~add(.bsf~new("javafx.scene.image.Image", "file:oorexx_032.png"))
  primaryStage~setTitle("ListView with Graphics (ooRexx)")

  -- this may be changed via the Stylesheet menu processed by demoListViewController.rex,
  -- store globally, we start out without customized formatting
  .my.app~listview_stylesheet_kind=0

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  demoListViewUrl=.bsf~new("java.net.URL", "file:demoListView.fxml")
  rootNode       =.fx.FXMLLoader~load(demoListViewUrl) -- load the fxml document
  -- get current selection state from corresponding menu object, now that .my.app is populated
  .my.app~showDebugOutput=.my.app~demoListView.fxml~id_menu_show_debug~isSelected

   -- as .my.app is populated, all the FXML fx:id objects are available to Rexx
  call setupApplicationData   -- now that the graphics subsystem is initialized

   -- define an animation effect for the root node (i.e. the entire scene)
   -- demo action! :)
  rt=.bsf~new("javafx.animation.RotateTransition", .fx.Duration~millis(750),rootNode)
  rt ~~setByAngle(270) ~~setCycleCount(2) ~~setAutoReverse(.true) ~~play

  scene=.fx.scene~new(rootNode)   -- create a scene for our document
  primaryStage~setScene(scene)      -- set the stage to our scene

   -- this will setup and register the event handlers
  .demoListViewController~new

  primaryStage~show        -- now show the stage (and thereby our scene)



/* =================================================================================== */

/** This Rexx class manages the user interaction with the FXML form named
    &quot;demoListView.fxml&quot;.
*/
::class demoListViewController

/** Caches the JavaFX objects in ooRexx attributes for easier access. */
::method init
  expose id_vbox id_listview id_menu_setitems id_menu_clear id_menu_quit   -
         id_menu_remove id_label id_menu_show_debug id_menu_enable_editing -
         id_button_add id_button_delete

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them to attributes
  appDir=.my.app~demoListView.fxml

  if .my.app~showDebugOutput=.true then
    call dumpDir appDir, .line":" .dateTime~new self"::".context~name": .my.app~demoListView.fxml:"

  id_listview       =appDir~id_listview
  id_vbox           =appDir~id_vbox      -- is parent, can have stylesheets
  id_label          =appDir~id_label
  id_menu_show_debug=appDir~id_menu_show_debug
  id_menu_enable_editing=appDir~id_menu_enable_editing

  appDir~is_id_ctxt_menu_display_text    =.true -- enable by default: display text in ListCell?
  appDir~is_id_ctxt_menu_display_graphic=.true -- enable by default: display graphics in ListCell?

  id_menu_setItems  =appDir~id_menu_setitems
  id_menu_remove    =appDir~id_menu_remove
  id_menu_clear     =appDir~id_menu_clear
  id_menu_quit      =appDir~id_menu_quit
  id_menue          =appDir~id_menu

   -- add self as the EventHandler (we have the method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.event.EventHandler")

  /* set callback for creating TableView rows to which we add us as an event listener to get mouse click events on the rows */
  id_listview~setOnMouseClicked(rp) -- allows us to get the mouse clicked event
  label=.bsf~new("javafx.scene.control.Label","<empty>")~~setStyle("-fx-text-fill: silver; -fx-font-size: 17pt;")
  id_listview~setPlaceHolder(label)

   /* set callback for menu click */
  id_menu_show_debug~setOnAction(rp)
  id_menu_enable_editing~setOnAction(rp)
  id_menu_setitems  ~setOnAction(rp)
  id_menu_clear     ~setOnAction(rp)
  id_menu_quit      ~setOnAction(rp)

  /* create and assign context menu */
  ctxtMenu=createContextMenu(rp)
  id_listview~setContextMenu(ctxtMenu)

   /* set callback for filling the individual ListView cells */
  id_listview~setCellFactory(BsfCreateRexxProxy(.RexxCellFactory~new, , .fx.Callback))

  /* set callback for committed ListCell edits */ /* not necessary to handle ourselves */
  id_listview~setOnEditCancel(rp)   -- use to force repaint (to do an updateItem/repaint)

  /* set callback for button press actions */
  id_button_add   =appDir~id_button_add    ~~setOnAction(rp)
  id_button_delete=appDir~id_button_delete ~~setOnAction(rp)


/** Event handler for most events that are of interest for our little application.
    @param event   the JavaFX event object
    @param slotDir      not used (a Rexx directory supplied by BSF4ooRexx)
*/
::method handle   /* implements the interface "javafx.event.EventHandler"   */
  expose id_listview id_menu_setitems id_menu_clear id_menu_quit id_menu_remove id_label -
         id_menu_show_debug id_menu_enable_editing id_button_add id_button_delete

  use arg event

  source=event~source
  target=event~target
  eventType=event~eventType~toString
  isMouseEvent=(event~objectname~startsWith("javafx.scene.input.MouseEvent@"))

  indent="09"x

  if .my.app~showDebugOutput=.true then
  do
     say
     say .line":" .dateTime~new "\\\" self"::handle; eventType="pp(eventType) "event:" pp(event)
     say indent .line":" "event~toString:" pp(event~tostring)
     say indent .line":" "    event~source="pp(source) "~toString:"pp(source~toString)
     if target=.nil then say indent .line":" "    event~target="pp(target) "<--- !"
                    else say indent .line":" "    event~target="pp(target) "~toString:"pp(target~toString)

    if isMouseEvent, eventType="MOUSE_CLICKED" then
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

      say indent .line":" "MOUSE_CLICKED -->" pp(mb~string)

      selectedIndex=id_listView~getSelectionModel~getSelectedIndex
      selectedItem =id_listView~getSelectionModel~getSelectedItem
      say indent .line":" "selectedIndex:" pp(selectedIndex) "selectedItem:" pp(selectedItem)

      say indent .line":" "   " pp(eventType)", event~button="pp(event~button~toString)", clickCount="pp(event~getClickCount) "\\\"
      say indent .line":" "/// ---> pickResult~intersectedNode-Infos:"
      say indent .line":" ppNodeInfo(event)
      say indent .line":" "\\\ <---"
    end

  end



  /* Handle menu and button events. Note: the id values of the context MenuItems are set
     to the same id values defined in the FXML file such that the same actions get carried
     out. */
  tgtId=target~getId
  if .my.app~showDebugOutput=.true then
     say indent .line":" "*** ---> target~id="pp(tgtId)

  listViewData=.my.app~listViewData    -- fetch observable list

  select
     when tgtId="id_menu_show_debug" then   -- get selected-state, use it for setting .showDebugOutput
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

     when tgtId="id_menu_enable_editing" then   -- get selected-state, use it for setting ListView edit mode
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              isSelected=id_menu_enable_editing~isSelected
              id_listview~setEditable(isSelected)
              id_button_add   ~setDisable(\isSelected)
              id_button_delete~setDisable(\isSelected)
              return
           end

     when tgtId="id_ctxt_menu_display_text" | tgtId="id_ctxt_menu_display_graphic" then
           do
               .my.app~demoListView.fxml~setEntry("is_"tgtId,source~isSelected)  -- save new state
               id_listView~refresh     -- update ListView
           end

     when tgtId="id_ctxt_menu_enable_editing" then -- reflect change from context menu in File menu item as well
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              bSelected=event~source~isSelected    -- get ContextMenu's CheckMenuItem object
              id_listview~setEditable(bSelected)   -- set ListView editing mode
              id_menu_enable_editing~setSelected(bSelected)

              if id_menu_clear~isDisable=.false then
              do
                 id_button_add   ~setDisable(\bSelected)
                 id_button_delete~setDisable(\bSelected)
              end

              return
           end

     when tgtId="id_menu_setitems" then      -- fill the ListView, set the menu and button states
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              call resetListViewData   -- reset the listviewdata to inital state, such setitems will show them all again
              id_listview~setItems(listViewData)
              id_menu_setitems~setDisable(.true)
              id_menu_remove  ~setDisable(.true)
              id_menu_clear   ~setDisable(.false)
              id_label~setText("ListView has" listViewData~size "cell(s)")
              if id_menu_enable_editing~isSelected then
              do
                 id_button_add   ~setDisable(.false)
                 id_button_delete~setDisable(.false)
              end
              return
           end

     when tgtId="id_menu_remove" then  -- remove .nil entries from ArrayList
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              do idx=listViewData~size-1 to 0 by -1     -- remove empty cells
                 if listViewData~get(idx)~isNil then
                    listViewData~remove(box('int',idx))  -- make sure integer version of remove() gets used
              end

              if listViewData~size>0 then               -- set state of menu items
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
              id_label~setText("ListView has" listViewData~size "cell(s)")

              return
           end

     when tgtId="id_menu_clear" then
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              listViewData~clear
              id_menu_setitems~setDisable(.false)
              id_menu_remove  ~setDisable(.true)
              id_menu_clear   ~setDisable(.true)
              id_label~setText("ListView has" listViewData~size "cell(s)")
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
               listViewData~add("<NEW VALUE ADDED>, randomly picked: ("iin~at(random(1,iin~items))"):" .dateTime~new)
               id_listView~refresh
               size=listViewData~size
               id_label~setText("ListView has" size "cell(s)")
               id_listView~scrollTo(size-1)
               -- id_listView~getFocusModel~focus(size-1)
               id_listView~refresh
               id_listView~edit(listViewData~size)
               return
            end

      when tgtId="id_button_delete" then
            do
               if .my.app~showDebugOutput=.true then
                  say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
               selectedIndex=id_listView~getSelectionModel~getSelectedIndex

               if .my.app~showDebugOutput=.true then
                  say indent .line":" "id_button_delete, selectedIndex="pp(selectedIndex)

               if \selectedIndex~isNil then
               do
                  listViewData~remove(box('int',selectedIndex))
                  id_listView~refresh
                  id_label~setText("ListView has" listViewData~size "cell(s)")
               end
               else if .my.app~showDebugOutput=.true then
                   say indent .line":" "id_button_delete, selectedIndex="pp(selectedIndex)
               return
            end

     when eventType="EDIT_CANCEL" then    -- force updateItem
           do
              if .my.app~showDebugOutput=.true then
                 say indent .line":" "*** handling" pp(eventType) "for" pp(tgtId)
              event~getSource~refresh  -- force a refresh of the ListView
              return
           end

     otherwise nop
  end

   -- if Ctrl-click on a cell with a value, then set it to .nil, otherwise reinstate value
  idx=getIndex(event)
  if idx>-1, isMouseEvent, event~getClickCount=1 then
  do
      if idx<listViewData~size, event~isControlDown then      -- clicked cell index is within bounds
      do
         val=listViewData~get(Idx)

         if .my.app~showDebugOutput=.true then
            say indent .line":" "*** handling" pp("Ctl-"eventType) "for index="pp(idx)", value="pp(val)

         if val~isNil then
         do
            val=.my.app~entryNames[idx+1] -- try to reinstate from the predefined initial four values
            if val~isNil then    -- if index>3 then we need to create a new value
            do
               iin=.my.app~imageIndexNames
               val="<ANOTHER NEW VALUE ADDED>, randomly picked: ("iin~at(random(1,iin~items))"):" .dateTime~new
            end
            listViewData~set(idx,val)  -- re-instate

            bDisable=.true          -- if .nil entries, then enable id_menu_remove
            do o over listViewData  -- iterate over all items to determine state of "remove" menu
               if o~isNil then      -- o.k. one is .nil, allow removal in menu
               do
                  bDisable=.false
                  leave
               end
            end
            id_menu_remove~setDisable(bDisable)
         end
         else  -- nullify
         do
            listViewData~set(idx, .nil)                 -- nullify
            id_menu_remove~setDisable(.false)            -- enable "remove" menu
         end
      end
      else  -- left mouse button click at a cell outside of the range of our currently defined data
      do
         if event~button~toString="PRIMARY", idx>(listViewData~size-1) then   -- this way only left mouse buttong clicks
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
     if \listViewData~isNil then
     do
        say indent tab"current listViewData~size:" pp(listViewData~size)
        do i=0 to listViewData~size - 1 --  o over id_listview~getItems
            o=listViewData~get(i)
            info=""
            if \o~isNil, o~isA(.bsf) then info=pp("o~toString="o~toString)

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
        event~getSource~refresh  -- force a refresh of the ListView
  end
  else   -- an unhandled, maybe unknown/unexpected event!
  do
     if .my.app~showDebugOutput=.true then
     do
        .error~say("--> ---> ---->  "~copies(10))
        .error~say(self"::handle," eventType "event:" pp(event~toString))
        .error~say("<-- <--- <----  "~copies(10))
        .error~say
     end
  end


 /** Closes the application. */
::method handleExit
  bsf.loadClass("javafx.application.Platform")~exit   -- unload JavaFX, but let Rexx continue in main thread


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) used by PropertyValueFactory */
/** Rexx class that creates the cell objects that get used by <em>ListView</em> for its
    <em>ListCell</em>s. It implements the JavaFX interface
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/util/Callback.html">Callback</a>,
    i.e. the method <code>call</code>.
*/
::class RexxCellFactory -- Rexx class implementing "javafx.util.Callback"'s "call" method

  /* ------------------------------------------------------------------------------ */
/**  Callback method that creates and returns the cell object to be used for a <em>ListCell</em>.

    @param listView   the <em>ListView</em> for which this method gets invoked
    @param slotDir      not used (a Rexx directory supplied by BSF4ooRexx)

*/
::method call           -- implements "javafx.util.Callback.call(Object o)": in this case an instance of a ListCell (or a subclass of it) must be returned!
  use arg listView

         -- create the ListCell handler, supply a RexxListCell object to carry out "updateItem"
  rexxCellHandler=.RexxListCell~clzOoRexxListCell~new(.RexxListCell~new)  -- will have updateItem() invoked
  rexxCellHandler~setTooltip(.my.app~cell.tooltip)             -- set ToolTip to this Cell
  rexxCellHandler~setConverter(.RexxListCell~stringConverter)  -- needed for edits with TextFieldListCell

  if .my.app~showDebugOutput=.true then
  do
     say
     say .line":" .dateTime~new "///-->" self ", RETURNING:" pp(rexxCellHandler) "for:" pp(jStrip(listView))
  end
  return rexxCellHandler


/* =================================================================================== */
/* This is an ooRexx class, subclassing the JavaFX "javafx.scene.control.ListCell" class
   in order to become able to edit/format ListCells by intercepting the "updateItem" method.
*/

/** This Rexx class creates and maintains the appearence (cf. callback method
    <code>updateItem</code>) of <em>ListCell</em> cell. In order to become able
    to set a text and a graphic to represent a cell value in the ListCell it
    subclasses the JavaFX class
    <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/cell/TextFieldListCell.html>TextFieldListCell</a>.
*/
::class RexxListCell

/** Caches the JavaFX proxy class for this Rexx class. */
::attribute clzOoRexxListCell class get  -- a class attribute

/** Caches the string converter for the <em>ListCell</em>. */
::attribute stringConverter   class get  -- a class attribute

/** Class constructor method that creates and caches the JavaFX proxy class
    as well as the string converter object to be used by this <em>ListCell</em>
    implementation.
*/
::method init class     -- the Rexx constructor at the class level: create and memorize a proxy class for "javfx.scene.control.ListCell"
  expose clzOoRexxListCell stringConverter
   -- use a TextFieldListCell to allow in place edits
  clzOoRexxListCell=bsf.createProxyClass("javafx.scene.control.cell.TextFieldListCell", "jRexxListCell", "javafx.scene.control.Cell updateItem")
   -- we need a stringConverter for in-place edits
  stringConverter=.bsf~new("javafx.util.converter.DefaultStringConverter")


/** This implements the
   <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html#updateItem-T-boolean-">updateItem</a>
   method in Rexx. It controls what gets displayed in <em>ListCell</em>s (text and/or graphic).

   @param item    the item to be displayed
   @param empty   whether this is for an empty cell
   @param slotDir a Rexx directory supplied by BSF4ooRexx
*/
::method updateItem
  use arg item, empty, slotDir

  jSelf=slotDir~javaObject
  index=jSelf~getIndex     -- get 0-based index (row) of this cell

  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "///-->" self"::".context~name": jself:" pp(jself)
     say "09"x .line":" "jSelf" pp(jSelf) "| item="pp(item)",empty="pp(empty)",index="pp(index)
  end
     -- we need to invoke the method in the superclass first
  jSelf~updateItem_forwardToSuper(item,empty)
  jself~setStyle("")

  if item=.nil | empty=.true then
  do
     jSelf~setText(.nil)
     jSelf~setGraphic(.nil)
  end
  else   -- we have a value and are not empty
  do
     appDir=.my.app~demoListView.fxml
     if appDir~is_id_ctxt_menu_display_text then
        jSelf~setText(item)
     else
        jSelf~setText(.nil)

     if appDir~is_id_ctxt_menu_display_graphic then
     do
        parse var item "(" pic ")"  -- extract picture name if any, set graphics to it
        jSelf~~setGraphic(.fx.ImageView~new(.my.app~imageViewDir~entry(pic))) ~~setGraphicTextGap(20)
     end
     else
        jSelf~setGraphic(.nil)
  end

   -- style the cell's background
   -- note: one can use a CSS definition like: ".list-cell:odd { -fx-background-color: #dbffdb; }"
  if index>-1, .my.app~listview_stylesheet_kind=3 then      -- colorize the background of the odd entries
     if index//2 then jSelf~setStyle("-fx-background-color: #dbffdb; -fx-text-fill: black;")




/* =================================================================================== */
/** This routine can only work, once the internal graphics of JavaFX got initilaized
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

   -- ListView will use the observableArrayList and maintains it as well
  listViewData=.fx.FXCollections~observableArrayList  -- StringProperties to be shown in ListView

  imageViewDir=.directory~new      -- maps the entryName to its icon/grahpics
  do counter i name over entryNames
     listViewData~add(name)            -- ObservableArrayList to be displayed in the list
     img=.fx.Image~new("file:"imageNames[i])
     imageViewDir~setEntry(name,img)
     imageViewDir~setEntry(imageIndexNames[i],img)
  end

   -- save in directory accessible via the environment symbol .my.app
  .my.app~entryNames     =entryNames
  .my.app~imageIndexNames=imageIndexNames
  .my.app~imageViewDir   =imageViewDir

  .my.app~listViewData=listViewData
  .my.app~alert.audioclip=.bsf~new("javafx.scene.media.AudioClip", "file:oops.wav")


  tipText="If editing enabled: <enter> or double-click to edit," "0a"x"Ctl-click to nullify cell/undo cell nullify"
  tooltip =.bsf~new("javafx.scene.control.Tooltip", tipText)
  imgView=.fx.ImageView~new("file:icons8-mailbox-opened-flag-up-64.png")
  tooltip~~setGraphic(imgView)~~setGraphicTextGap(20)
   -- Cf. <https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html> (as of 2020-07-05)
  tooltip~setStyle("-fx-font-size: 15pt; -fx-text-fill: blue; -fx-padding: 5; -fx-background-color: lightyellow ;")   -- make sure font-size is legible
  .my.app~cell.tooltip=tooltip


/* =================================================================================== */
/** Recreates the <em>listViewData</em> that backs the <em>ListView</em> content from
    the <code>.my.app~entryNames</code> cache.
*/
::routine resetListViewData   -- clears .app.dir~listViewData and sets its entries according to .my.app~entryNames
  listViewData=.my.app~listViewData
  listViewData~clear
  do counter i name over .my.app~entryNames
     listViewData~add(name)   -- ObservableArrayList to be displayed in the list
  end


/* =================================================================================== */
/** Routine that creates the context menu. We cannot refer to existing <em>MenuItems</em>
    as long as they are part of a JavaFX node (tree) that is being used in a scene,
    therefore we must create proper context <code>MenuItems</code>.

    @param eventHandler the event handler that processes the user's menu interactions
*/
::routine createContextMenu
  use arg eventHandler
  ctxtMenu=.bsf~new("javafx.scene.control.ContextMenu")
  items=ctxtMenu~getItems

  sdo=.bsf~new("javafx.scene.control.CheckMenuItem","Show Debug Output") -
      ~~setSelected(.my.app~showDebugOutput)                             -
      ~~setId("id_ctxt_menu_show_debug")~~setOnAction(eventHandler)
  items~add(sdo)

  ee=.bsf~new("javafx.scene.control.CheckMenuItem","Enable Editing")     -
      ~~setId("id_ctxt_menu_enable_editing")~~setOnAction(eventHandler)
  items~add(ee)

  items~add(.bsf~new("javafx.scene.control.SeparatorMenuItem"))

  et=.bsf~new("javafx.scene.control.CheckMenuItem","Display Text in ListCell?")     -
      ~~setId("id_ctxt_menu_display_text")~~setOnAction(eventHandler)
  items~add(et)

  eg=.bsf~new("javafx.scene.control.CheckMenuItem","Display Graphic in ListCell?")     -
      ~~setId("id_ctxt_menu_display_graphic")~~setOnAction(eventHandler)
  items~add(eg)

  fxMI=bsf.importClass("javafx.scene.control.MenuItem")
  items~add(.bsf~new("javafx.scene.control.SeparatorMenuItem"))
      -- use same id as the File menu items, such that the context menu items cause the same behaviour
  items~add(fxMI~new("Fill ListView with Testdata")~~setId("id_menu_setitems")~~setOnAction(eventHandler) )
  items~add(fxMI~new("RemoveEmptyCells")~~setId("id_menu_remove")  ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Clear ListView")           ~~setId("id_menu_clear")   ~~setOnAction(eventHandler) )
  items~add(fxMI~new("Quit")            ~~setId("id_menu_quit")    ~~setOnAction(eventHandler) )

  if .my.app~showDebugOutput=.true then
     say .line":" .dateTime~new "\\\"  "createContextMenu(eventHandler): ctxtMenu~toString="pp(ctxtMenu~toString)

   -- use an ooRexx routine object as the Java EventHandler for the context menu
  jeh=bsfCreateRexxProxy(.routines~contextMenuHandler,,"javafx.event.EventHandler")
  ctxtMenu~setOnShowing(jeh)     -- will fire right before PopupMenu window gets displayed
  return ctxtMenu                -- return the ContextMenu

/* ----------------------------------------------------------------------------------- */
/** This demonstrates using a routine (instead of a method) for event handling instead
    of a method. This event handler will set all the <em>ContextMenu</em>'s <em>MenuItems</em>
    to the same state as the matching <em>MenuItem</em>s in the scene's <em>Menu</em>s.

    @param event  the JavaFX event to process
    @param slotDir  not used (a Rexx directory supplied by BSF4ooRexx)
    @return the <em>ContextMenu</em> object
*/
::routine "ContextMenuHandler"     -- set disable state according to the File MenuItems state
  use arg event

  if .my.app~showDebugOutput=.true then
  do
     say .line":" .dateTime~new "   " .context~name"(event), event:" pp(event~toString)
     say
  end

  appDir=.my.app~demoListView.fxml     -- get the directory with all current JavaFX objects having an fx:id
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
/** A routine for debugging that creates and returns a string of the list of nodes from
    the intersected node to the root node. If a <em>Cell</em> node is encountered it will
    be queried of its <em>index</em> (0-based position) in the <em>ListView</em>.

    @param event  the JavaFX event object to analyze
    @return  <code>.nil</code> if no intersecting node available, otherwise a string
             of nodes from the intersecting node to the root node; if a <em>Cell</em>
             node is encountered it will be augmented with its index position in the
             <em>ListView</em>
*/
::routine ppNodeInfo
   use arg event

   node=event~pickResult~getIntersectedNode
   if node=.nil then return .nil

   mb=.MutableBuffer~new
   do until node=.nil
      if mb~length>0 then mb~append(" -> ", pp(node))
                     else mb~append(pp(node))

      if node~objectName~pos("Cell")>0 then
         mb~append(".getIndex()=",pp(node~getIndex), " *** |")

      node=node~getParent
   end
   return mb~string


/* =================================================================================== */
/** Find and return <em>Cell</em> object from an event.

    @param event  the JavaFX event to process
    @return  <code>.nil</code> if no intersecting or <em>Cell</em> node available,
             otherwise the <em>Cell</em> node
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
/** Returns the event's <em>Cell</em> index (0-based position) in the <em>ListView</em> or
    <code>-1</code> if not available or meaningful.

    @param event  the JavaFX event to process
    @return       index (0-based position/row) in the <em>ListView</em> or <code>-1</code> if
                  not available or meaningful
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
/** Utility routine for debugging: dumps the content of a Rexx directory ordered by name. */
::routine dumpDir

  if .my.app~showDebugOutput=.false then return

  use arg dir, name=""
  say
  say "dumpDir():" name
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