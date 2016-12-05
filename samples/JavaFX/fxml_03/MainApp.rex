/*
   author:  Rony G. Flatscher
   date:    2016-11-30, preliminary

   purpose: this is the ooRexx version of the pure Java tutorial on how to use JavaFX, demonstrating
            among other things also TableView and ObservableList;
            cf. the excellent tutorial:

            There are two versions of the tutorial demonstrating the same application as of 2016-12-04:

            - Java 1.7/7 ("JavaFX 2"): <http://code.makery.ch/library/javafx-2-tutorial/>
            - Java 1.8/8 ("JavaFX 8"): <http://code.makery.ch/library/javafx-8-tutorial/>

            The address book icon is from the source given in the above tutorial, i.e.
            <https://www.iconfinder.com/icons/86957/address_book_icon#size=32>, as of 2016-12-04

            Creation of the FXML-files was done exactly as the above Java tutorials suggested
            using the SceneBuilder and then adding the process instruction to use Rexx and
            loading and executing "put_FXID_objects_into.my.app.rex".

            The ooRexx code takes advantage of ooRexx features like the .local environment to
            store the primaryStage to ease coupling different parts of the Rexx application.

            All controllers are coded in one Rexx file ("Rexx package") as Rexx classes.

            added features:
               - double-click in TableView will open the PersonEditDialog with the selected person (added
                 mouse clicked event listener to TableView)
               - menus have accelerators and show mnemonic character underlined (added CSS .mnemonic-underline)


   license: Apache License 2.0


   invoke:
            rexx MainApp.rex
            rexxj.{cmd|sh} Main.rex

*/


.environment~my.app=.directory~new   -- directory to contain objects relevant to this application
   -- starting with JavaFX 8u40 true dialogs got introduced; taking advantage of them if running on Java 1.8 or higher

   -- import JavaFX classes that we may use more often
call bsf.import "javafx.fxml.FXMLLoader",                      "fx.FXMLLoader"
call bsf.import "javafx.scene.Scene",                          "fx.Scene"
call bsf.import "javafx.beans.property.SimpleStringProperty",  "fx.SimpleStringProperty"
call bsf.import "javafx.beans.property.SimpleIntegerProperty", "fx.SimpleIntegerProperty"

call bsf.import "javafx.collections.FXCollections",            "fx.FXCollections"
call bsf.import "javafx.stage.Modality",                       "fx.Modality"

   -- create Rexx object that will control the application
rexxApp=.RxMainApplication~new
if arg()>0 then      -- if a command line JSON filename was given, use it to set the attribute
do
   parse arg jsonFile
   rexxApp~personFilePath=strip(jsonFile)
end

.my.app~mainApp=rexxApp        -- store the Rexx MainApp object in .my.app
   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
jRexxApp=BsfCreateRexxProxy(rexxApp, ,"javafx.application.Application")
   -- launch the application, invoke "start" and then stay up until the application closes
jRexxApp~launch(jRexxApp~getClass, .nil)     -- need to use this version of launch in order to work


::requires "BSF.CLS"          -- get Java support
::requires "json-rgf.cls"     -- get JSON support for ooRexx (to load and store person data)

-- ::requires "rgf_util2.rex"    -- get Rexx utilities (installed with BSF4ooRexx) to ease debugging (cf. dump2(), ppCondition2()...)


/* =================================================================================== */
/* implements the abstract method "start" for the Java class "javafx.application.Application"
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java).
   It also controls
*/
::class RxMainApplication

   /* for tutorial Part 2: create an ObservableList and create and store Person data in it */
::attribute personData

::attribute primaryStage   -- stores the primaryStage supplied via the start method by JavaFX

   -- tutorial, part 5
::attribute personFilePath -- .nil or the file path to the file containing the attribute records (in JSON format)


::method init  -- constructor
  expose personData prefs personFilePath    -- observableArrayList (maintained by TableView)

   -- create and save an ObservableList
  personData=.fx.FXCollections~observableArrayList

   -- do we have a file save already, such that a "filePath" preference was set at the user's root node?
  prefs=bsf.importClass("java.util.prefs.Preferences")~userRoot   -- get the user's root preference node, tutorial part # 5
  personFilePath=prefs~get("filePath", .nil) -- get filePath preference, if any
  if personFilePath<>.nil then
  do
     if sysFileExists(personFilePath) then
        self~loadPersonDataFromFile(personfilePath)
     else   -- if file does not exist, do not show it
        personFilePath=.nil
  end
  else   -- temporarily make sure we have (debug) data available to us
  do
         -- this creates ooRexx Person objects that get stored as Java RexxProxy objects, to be used for the TableView
     personData~add(.person~new("Hans", "Muster"))
     personData~add(.person~new("Ruth", "Mueller"))
     personData~add(.person~new("Heinz", "Kurz"))
     personData~add(.person~new("Cornelia", "Meier"))
     personData~add(.person~new("Werner", "Meyer"))
     personData~add(.person~new("Lydia", "Kunz"))
     personData~add(.person~new("Anna", "Best"))
     personData~add(.person~new("Stefan", "Meier"))
     personData~add(.person~new("Martin", "Mueller"))
  end


   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  expose locale primaryStage personFilePath
  use arg primaryStage  -- we get the stage to use for our UI

   -- starting with JavaFX 8u40 true dialogs got introduced; taking advantage of them if running on Java 1.8 or higher
   -- get the JavaFX runtime version (only available when running it), e.g. "8.0.111-b14"

  bAlertsAvailable=Dialogs.Alerts.Available=(.java.lang.system~getProperty("javafx.runtime.version")>8)
  .my.app~bDialogsAlertsAvailable=bAlertsAvailable  -- determine availability
  if bAlertsAvailable then    -- available since JavaFX 8u40
  do
      call bsf.import "javafx.scene.control.Alert",                  "fx.Alert"
         -- note "AlertType" is public "inner" enum class, i.e. it is defined within the class "Alert", hence
         -- the JavaDocs refer to "AlertType" as "Alert.AlertType": however the Java compiler produces the
         -- class file as "Alert$AlertType"
      call bsf.import "javafx.scene.control.Alert$AlertType",        "fx.Alert.Type"
  end

  if personFilePath=.nil then primaryStage~setTitle("AddressApp")
                         else primaryStage~title="AddressApp -" personFilePath

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootLayoutUrl=.bsf~new("java.net.URL", "file:RootLayout.fxml")
  rootLayout   =.fx.FXMLLoader~load(rootLayoutUrl) -- load the fxml document
--  .my.app~rootLayout=rootLayout     -- save in .my.app
  .rootLayoutController~new(self)   -- create an instance of the controller

  scene=.fx.scene~new(rootLayout)   -- create a scene for our document
--  .my.app~rootScene=scene
  primaryStage~setScene(scene)      -- set the stage to our scene

   -- add application icon
  img=.bsf~new("javafx.scene.image.Image", "address_book_32.png")
  primaryStage~getIcons~add(img)

   -- load PersonOverview.fxml, place it into the rootLayout, create the Rexx object controlling the PersonOverview form
  overviewUrl=.bsf~new("java.net.URL", "file:PersonOverview.fxml")
  overviewPage=.fx.FXMLLoader~load(overviewUrl)   -- load the fxml document, AnchorPage (root) returned
  .my.app~overviewPage=overviewPage
--   .my.app~rootLayout~setCenter(overviewPage)
  rootLayout~setCenter(overviewPage)
  .my.app~personOverviewController=.PersonOverviewController~new

  primaryStage~show        -- now show the stage (and thereby our scene)


/* tutorial, step 5: get filePath via Java's Preferences mechanism   */
::method getPersonFilePath
  expose prefs
  filePath=prefs~get("filePath", .nil)
  return filePath


/* tutorial, step 5: set filePath via Java's Preferences mechanism (supplying .nil will remove the preference)   */
::method setPersonFilePath
  expose prefs primaryStage
  use arg filePath

  if file<>.nil then
  do
     prefs~put("filePath", filePath)
     primaryStage~title="AddressApp -" filePath
  end
  else
  do
     prefs~remove("filePath")
     primaryStage~title="AddressApp"
  end


/* tutorial, step 5: using JSON instead of XML as file format */
::method loadPersonDataFromFile
  expose personData     -- get access to the ObservableList
  use arg filePath

  s=.stream~new(filepath)~~open("read")   -- open stream
  jsonStr=s~charIn(1, s~query("size"))    -- read entire content
  s~close                                 -- close stream
  persons=.Json~new~fromJSON(jsonStr)
  personData~clear      -- clear the ObservableList
  do p over persons     -- add persons to ObservableList
     personData~add(.person~new( p["firstName"], p["lastName"], p["street"], p["postalCode"], p["city"], p["birthday"]))
  end


/* tutorial, step 5: using JSON instead of XML as file format */
::method savePersonDataToFile
  expose personData     -- get access to the ObservableList
  use arg filePath

  arr=.array~new
  do p over personData  -- iterate over ObservableList
     dir=.directory~new
     dir["firstName"] =p~firstName
     dir["lastName"]  =p~lastName
     dir["street"]    =p~street
     dir["postalCode"]=p~postalCode
     dir["city"]      =p~city
     dir["birthday"]  =p~birthday
     arr~append(dir)
  end

  s=.stream~new(filepath)~~open("write replace")   -- open stream for replacement
  jsonStr=s~charout(.json~new~toJson(arr)) -- create and write JSON string to stream
  s~close                              -- close stream
  self~setPersonFilePath(filePath)     -- save this one as preference for next load



/* Load the fxml-form, define a stage for it, create an instance of the Rexx class controlling
   the fxml-form.
   Unlike the original tutorial we do not create the dialog and controller over and over again, we
   just cache them using two attributes and reuse them.
*/
::method showPersonEditDialog
  expose pedDialog pedController primaryStage
  use arg person

-- signal on syntax

  if \var("pedDIALOG") then   -- not yet created, setup PersonEdit data
  do
     -- Load the fxml file and create a new stage for the popup
     personEditDialogUrl=.bsf~new("java.net.URL", "file:PersonEditDialog.fxml")
     page  = .fx.FXMLLoader~load(personEditDialogUrl)
     scene = .fx.Scene~new(page)

     -- AnchorPane page = (AnchorPane) loader.load();
     dialogStage = .bsf~new("javafx.stage.Stage")
     dialogStage~setTitle("Edit Person")
     dialogStage~initModality(.fx.Modality~WINDOW_MODAL)

     dialogStage~initOwner(primaryStage)
     dialogStage~setScene(scene)

     -- Set the person into the controller
     pedController = .PersonEditDialogController~new
     pedController~dialogStage=dialogStage   -- this way the pedController is able to close the stage
  end

  pedController~setPerson(person)
  pedController~okClicked=.false          -- make sure we start out with .false

  -- Show the dialog and wait until the user closes it
  dialogStage~showAndWait
  return pedController~OkClicked

syntax:
  co=condition('object')
  call dumpConditionObject co -- show condition information
  raise propagate


::method showPersonStatistics
  expose primaryStage personData

  -- Load the fxml file and create a new stage for the popup
  birthdayStatisticsUrl=.bsf~new("java.net.URL", "file:BirthdayStatistics.fxml")
  page  = .fx.FXMLLoader~load(birthdayStatisticsUrl)
  scene = .fx.Scene~new(page)

  -- AnchorPane page = (AnchorPane) loader.load();
  dialogStage = .bsf~new("javafx.stage.Stage")
  dialogStage~setTitle("Birthday Statistics")
  dialogStage~initModality(.fx.Modality~WINDOW_MODAL)

  dialogStage~initOwner(primaryStage)
  dialogStage~setScene(scene)

  -- Set the persons into the controller
  controller = .BirthdayStatisticsController~new
  controller~setPersonData(personData)
  dialogStage~show



/* =================================================================================== */
/* for tutorial part 2: define the PersonOverviewController

   This Rexx class controls the interaction with the fxml form named "PersonOverview.fxml".

*/
::class PersonOverviewController
::attribute personTable
::attribute firstNameColumn
::attribute lastNameColumn

::method init
  expose personTable firstNameColumn lastNameColumn firstNameLabel lastNameLabel streetLabel -
                     postalCodeLabel cityLabel birthdayLabel  -
                     btnNew btnEdit btnDelete

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them to attributes
  poDir=.my.app~personOverview.fxml
  personTable    =poDir~personTable
  firstNameColumn=poDir~firstNameColumn
  lastNameColumn =poDir~lastNameColumn

  firstNameLabel =poDir~firstNameLabel
  lastNameLabel  =poDir~lastNameLabel
  streetLabel    =poDir~streetLabel
  postalCodeLabel=poDir~postalCodeLabel
  cityLabel      =poDir~cityLabel
  birthdayLabel  =poDir~birthdayLabel

  btnNew         =poDir~btnNew
  btnEdit        =poDir~btnEdit
  btnDelete      =poDir~btnDelete

   -- add self as the ChangeListener (we have its method "changed" implemented) and EventHandler (method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.beans.value.ChangeListener", "javafx.event.EventHandler")

  /* set callback for creating TableView rows to which we add us as an event listener to get mouse click events on the rows */
  personTable~setOnMouseClicked(rp) -- allows us to get the mouse clicked event

   /* set callback for filling the individual TableView cells */
  firstNameColumn~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("firstNameProperty"), ,"javafx.util.Callback"))
  lastNameColumn ~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("lastNameProperty"),  ,"javafx.util.Callback"))

   /* for tutorial part 3: fill-in the Person's details */
  self~showPersonDetails(.nil)   -- clear the labels
   --  auto resize columns
  personTable~setColumnResizePolicy(personTable~CONSTRAINED_RESIZE_POLICY)    -- TableView constant

   -- add self as the ChangeListener (we have its method "changed" implemented) and EventHandler (method "handle" implemented)
  personTable~getSelectionModel ~selectedItemProperty ~addListener(rp)

   -- add us as the event handler to the buttons, the event handler (method "handle") will be able to get at the pressed button
  btnNew   ~setOnAction(rp)
  btnEdit  ~setOnAction(rp)
  btnDelete~setOnAction(rp)

  self~setItems      -- fill the TableView

/*
  return

syntax:
  co=condition('object')
  call dumpConditionObject co -- show condition information
  raise propagate
*/


::method setItems
  expose personTable
  personTable~setItems(.my.app~mainApp~personData) -- add current personData

/* for tutorial part 3: fill-in the Person's details */
::method showPersonDetails
  expose firstNameLabel lastNameLabel streetLabel postalCodeLabel cityLabel birthdayLabel
  use arg p

  if p=.nil then  -- reset details
  do
     firstNameLabel  ~setText("")
     lastNameLabel   ~setText("")
     streetLabel     ~setText("")
     postalCodeLabel ~setText("")
     cityLabel       ~setText("")
     birthdayLabel   ~setText("")
  end
  else   -- set Person's details
  do
     firstNameLabel  ~setText(p~firstName)
     lastNameLabel   ~setText(p~lastName)
     streetLabel     ~setText(p~street)
     postalCodeLabel ~setText(p~postalCode)
     cityLabel       ~setText(p~city)
     birthdayLabel   ~setText(p~birthday)
  end


::method handleDeletePerson   /* invoked when the user clicks the delete button   */
  expose personTable

  selectedIndex=personTable~getSelectionModel~getSelectedIndex

  if selectedIndex >= 0 then
  do
     -- IMPORTANT: we must use bsf.invokeStrict() to become able to give explicitly the argument's type as otherwise
     --            it may be possible that the wrong "remove(Object)" method is used instead of "remove(int)" (both
     --            methods behave differntly)
     personTable~getItems~bsf.invokeStrict("remove", "int", selectedIndex) -- we want the remove method with the primitive int argument!
  end
  else
  do
      if .my.app~bDialogsAlertsAvailable then
      do
         alert=.fx.alert~new(.fx.Alert.Type~WARNING)   -- create a warning alert

         alert~setTitle("No Selection")
         alert~setHeaderText(.nil)
         alert~setContentText("Please select a person in the table.")
         alert~showAndWait
      end
      else  -- use BSF.CLS' .bsf.dialog utility class
      do
         -- .bsf.dialog is defined in BSF.CLS and uses swing
        .bsf.dialog~messageBox("Please select a person in the table.", "No Selection", "warning")
      end
  end



/* for tutorial part 3: fill-in the Person's details */
::method handleNewPerson
  tempPerson=.Person~new

  okClicked = .my.app~mainApp~showPersonEditDialog(tempPerson)
  if okClicked then
     .my.app~mainApp~personData~add(tempPerson)


::method handleEditPerson
  expose personTable

  selectedPerson = personTable~getSelectionModel~getSelectedItem
  if selectedPerson <> .nil then
  do
      person=BSFRexxProxy(selectedPerson) -- unbox Rexx object
      okClicked = .my.app~mainApp~showPersonEditDialog(person)
      if okClicked=.true then
          self~showPersonDetails(person)
  end
  else
  do
      if .my.app~bDialogsAlertsAvailable then
      do
         alert=.fx.alert~new(.fx.Alert.Type~WARNING)   -- create a warning alert

         alert~setTitle("No Selection")
         alert~setHeaderText(.nil)
         alert~setContentText("Please select a person in the table.")
         alert~showAndWait
      end
      else  -- use BSF.CLS' .bsf.dialog utility class
      do
         -- .bsf.dialog is defined in BSF.CLS and uses swing
        .bsf.dialog~messageBox("Please select a person in the table.", "No Selection", "warning")
      end
  end


   -- JavaFX event handlers implemented in Rexx
::method changed  /* implements the interface "javafx.beans.value.ChangeListener" */
  use arg observable, oldValue, newValue
  self~showPersonDetails(newValue)  -- fill in the labels to show currently selected Person's details

::method handle   /* implements the interface "javafx.event"   */
  expose btnNew btnEdit btnDelete personTable
  use arg event, slotDir

  tgtObjectName=event~getTarget~objectname
  select
     when tgtObjectName=btnNew~objectName    then self~handleNewPerson
     when tgtObjectName=btnEdit~objectName   then self~handleEditPerson
     when tgtObjectName=btnDelete~objectName then self~handleDeletePerson
     otherwise --
     do
        if event~getSource~objectName=personTable~objectName then -- TableView the source of the event?
        do
            if event~getClickCount>1 then    -- if a double-click, then go into edit record mode
               self~handleEditPerson
        end
        else   -- a truly unknown/unexpected event!
        do
           .error~say(self"::handle, UNKNOWN event:" pp(event~toString))
        end
     end
  end



/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) for PropertyValueFactory */

/* This class allows instances that remember the message to be sent to person instances to
   return the property of the attribute that should be shown in the table cell.
*/
::class PropertyValueFactory
::method init
  expose  propName   --handler -- name of property getter method
  use strict arg propName -- , handler

::method call
  expose propName   -- handler
  use arg o          -- an observable value for the ooRexx person object boxed in a Java RexxProxy object
-- say self"::call, o="pp(o) pp(o~toString)
--  o~getTableColumn~setOnMouseClicked(handler)

  return BsfRexxProxy(o~getValue)~send(propName)


/*
::class RowFactory
::method init
  expose handler     -- the Rexx proxy object that is supposed to handle the mouse click events (cf. PersonOverviewController)
  use arg handler

::method call
  expose handler
  row=.bsf~new("javafx.scene.control.TableRow")
  row~setOnMouseClicked(handler)
say "RowFactory, call(): handler="pp(handler) pp(handler~toString) "row:" pp(row) pp(row~toString)
  return row
*/

/* =================================================================================== */
/* for tutorial part 3: define the PersonEditDialogController */
/*
   This Rexx class controls the interaction with the fxml form named "PersonEditDialog.fxml".
*/
::class PersonEditDialogController
::attribute okClicked
::attribute dialogStage
::attribute person         -- the Person object to edit

::method init
  expose firstNameField lastNameField streetField postalCodeField cityField birthdayField -
         btnOK btnCancel -
         okClicked

  okClicked=.false

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them to attributes
  pedDir=.my.app~personEditDialog.fxml
  firstNameField =pedDir~firstNameField
  lastNameField  =pedDir~lastNameField
  streetField    =pedDir~streetField
  postalCodeField=pedDir~postalCodeField
  cityField      =pedDir~cityField
  birthdayField  =pedDir~birthdayField

  btnOK          =pedDir~btnOK
  btnCancel      =pedDir~btnCancel

   -- add self as the EventHandler (method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.event.EventHandler")
   -- add us as the event handler to the buttons, the event handler (method "handle") will be able to get at the pressed button
  btnOK    ~setOnAction(rp)
  btnCancel~setOnAction(rp)


::method handle      -- method defined "javafx.event.EventHandler"
  expose btnOK btnCancel
  use arg event

  tgtObjectName=event~getTarget~objectname
  select
     when tgtObjectName=btnOK~objectName     then self~handleOK         -- say "BUTTON OK    was the target!"
     when tgtObjectName=btnCancel~objectName then self~handleCancel     -- say "BUTTON CANCEL was the target!"
     otherwise .error~say("UNKNOWN target:" pp(event~getTarget) pp(event~getTarget~tostring))
  end


::method setPerson
  expose firstNameField lastNameField streetField postalCodeField cityField birthdayField -
         person
  use arg person  -- get person (assigning it to the attribute)

  firstNameField ~setText(person~firstName)
  lastNameField  ~setText(person~lastName)
  streetField    ~setText(person~street)
  postalCodeField~setText(person~postalCode)
  cityField      ~setText(person~city)
  birthdayField  ~setText(person~birthday)
  birthdayField  ~setPromptText("yyyy-mm-dd")


::method handleCancel
  expose dialogStage

  dialogStage~hide


::method handleOK
  expose firstNameField lastNameField streetField postalCodeField cityField birthdayField -
         person okClicked dialogStage

  if (self~isInputValid) then
  do
      person~firstName  = firstNameField ~getText
      person~lastName   = lastNameField  ~getText
      person~street     = streetField    ~getText
      person~postalCode = postalCodeField~getText
      person~city       = cityField      ~getText
      person~birthday   = birthdayField  ~getText

      okClicked = .true
      dialogStage~hide
  end

::method isInputValid
  expose firstNameField lastNameField streetField postalCodeField cityField birthdayField -
         dialogStage


  errorMessage = ""
  LF = "0a"x     -- line feed character

  if (firstNameField~getText = .nil | firstNameField~getText~length = 0) then
     errorMessage = LF"No valid first name"

  if (lastNameField~getText = .nil | lastNameField~getText~length = 0) then
     errorMessage ||= LF"No valid last name"

  if (streetField~getText = .nil | streetField~getText~length = 0) then
     errorMessage ||= LF"No valid street"

  if (postalCodeField~getText = .nil | postalCodeField~getText~length = 0) then
     errorMessage ||= LF"No valid postal code"
  else
     if \ DataType(postalCodeField~getText, "Whole") then
         errorMessage ||= LF"No valid postal code (must be an integer)"

  if (cityField~getText = .nil | cityField~getText~length = 0) then
     errorMessage ||= LF"No valid city"

  if (birthdayField~getText = .nil | birthdayField~getText~length = 0) then
     errorMessage ||= LF"No valid birthday"
  else
     if \ validString(birthdayField~getText) then
         errorMessage ||= LF"No valid birthday. Use the format yyyy-mm-dd"

  if (errorMessage~length = 0) then
     return .true

  -- .bsf.dialog is defined in BSF.CLS and uses swing
  if .my.app~bDialogsAlertsAvailable then
  do
     alert=.fx.alert~new(.fx.Alert.Type~ERROR)  -- create a warning alert
     alert~setTitle("Invalid Fields")
     alert~setHeaderText("Please correct the listed errors!")
     alert~setContentText("Invalid values:" LF errorMessage)
     alert~showAndWait
  end
  else  -- use BSF.CLS' .bsf.dialog utility class
  do
     -- .bsf.dialog is defined in BSF.CLS and uses swing
    .bsf.dialog~messageBox("Please correct the listed invalid fields:" LF errorMessage, "Invalid Fields", "error")
  end

   return .false



/* =================================================================================== */
/* for tutorial part 2: define The Model Class */

/* This class defines a person, its attributes (backed by JavaFX properties), the
   appropriate getter and setters.

   (Hint: as there is a clear pattern defining attributes and the needed setters and getters,
   one could apply ooRexx metaprogramming to define them in the class constructor. This is
   left as an excercise for the interested reader.)
*/
::class Person
::method init class  -- class initializer
   -- the following attribute definitions follow a pattern that could be exploited by creating
   -- them dynamically when this class constructor runs:
   -- - the attribute's storage is a JavaFX property
   -- - the getter method returns the value stored in the property
   -- - the setter method sets the value stored in the property
   ----------------------------------------------------------------------------------------
::attribute firstName get
  expose firstName
  return firstName~get

::attribute firstName set
  expose firstName
  use arg val
  return firstName~set(val)

::attribute firstNameProperty get
  expose firstName
  return firstName

   ----------------------------------------------------------------------------------------
::attribute lastName get
  expose lastName
  return lastName~get

::attribute lastName set
  expose lastName
  use arg val
  return lastName~set(val)

::attribute lastNameProperty get
  expose lastName
  return lastName

   ----------------------------------------------------------------------------------------
::attribute street get
  expose street
  return street~get

::attribute street set
  expose street
  use arg val
  return street~set(val)

::attribute lastStreetProperty get
  expose street
  return street

   ----------------------------------------------------------------------------------------
::attribute postalCode get
  expose postalCode
  return postalCode~get

::attribute postalCode set
  expose postalCode
  use arg val
  return postalCode~set(val)

::attribute postalCodeProperty get
  expose postalCode
  return postalCode

   ----------------------------------------------------------------------------------------
::attribute city get
  expose city
  return city~get

::attribute city set
  expose city
  use arg val
  return city~set(val)

::attribute cityProperty get
  expose city
  return city

   ----------------------------------------------------------------------------------------
::attribute birthday get
   expose birthday
   return birthday~get

::attribute birthday set
   expose birthday
  use arg val
   return birthday~set(val)

::attribute birthdayProperty get
   expose birthday
   return birthday

   ----------------------------------------------------------------------------------------
::method init        -- constructor
  expose firstName lastName street postalCode city birthday
  use arg strFirstName, strLastName, strStreet=.nil, strPostalCode=.nil, strCity=.nil, strBirthday=.nil

  firstName=.fx.SimpleStringProperty~new(strFirstName)
  lastName =.fx.SimpleStringProperty~new(strLastName)

  if strStreet=.nil then street = .fx.SimpleStringProperty~new("some street")
                    else street = .fx.SimpleStringProperty~new(strStreet)

  if strPostalCode=.nil then postalCode = .fx.SimpleIntegerProperty~new(1234)
                        else postalCode = .fx.SimpleIntegerProperty~new(strPostalCode)

  if strCity=.nil then city = .fx.SimpleStringProperty~new("some city")
               else city = .fx.SimpleStringProperty~new(strCity)

        -- ooRexx .DateTime
  if strBirthday=.nil then birthday = .fx.SimpleStringProperty~new(.DateTime~new~standardDate("-"))
                   else birthday = .fx.SimpleStringProperty~new(strBirthday)


::routine dumpConditionObject
  use arg co
  say ppCondition2(co)
  co~Additional[2]~printStackTrace
  say "<---"





/* tutorial, part 5 */
::class RootLayoutController

::method init        -- constructor
  expose mainApp -
         menuNew menuOpen menuSave menuSaveAs menuAbout menuExit menuBirthdayStatistics

  use arg mainApp -- fetch and save object in attribute

   -- add self as the ChangeListener (we have its method "changed" implemented) and EventHandler (method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.event.EventHandler")

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them us as for event handling (cf. method "handle")
  rlDir=.my.app~rootLayout.fxml
  menuNew   =rlDir~menuNew   ~~setOnAction(rp)
  menuOpen  =rlDir~menuOpen  ~~setOnAction(rp)
  menuSave  =rlDir~menuSave  ~~setOnAction(rp)
  menuSaveAs=rlDir~menuSaveAs~~setOnAction(rp)
  menuAbout =rlDir~menuAbout ~~setOnAction(rp)
  menuExit  =rlDir~menuExit  ~~setOnAction(rp)
  -- tutorial, part 6
  menuBirthdayStatistics=rlDir~menuBirthdayStatistics ~~setOnAction(rp)


/* create an empty address book */
::method handleNew
  expose mainApp

  mainApp~personData~clear
  mainApp~personFilePath=.nil
  mainApp~primaryStage~title="AddressApp"

/* load person data from JSON file */
::method handleOpen
  expose mainApp

  fileChooser=.bsf~new("javafx.stage.FileChooser")
   -- crate an array of file extensions (only one in this case)
  jarrExtensions=bsf.createJavaArrayOf("java.lang.String", "*.json")
  extFilter=  .bsf~new("javafx.stage.FileChooser$ExtensionFilter", "JSON files (*.json)", jarrExtensions)
  fileChooser~getExtensionFilters~add(extFilter)
  fileChooser~setInitialDirectory(.bsf~new("java.io.File", "."))  -- set current directory

  -- Show open file dialog
  file = fileChooser~showOpenDialog(mainApp~primaryStage)
  if file<>.nil then
  do
     filePath=file~getPath
     mainApp~loadPersonDataFromFile(filePath)   -- supply the full path, not the Java file object
     mainApp~primaryStage~title="AddressApp -" filePath
  end


/* Saves the file to the person file that is currently open. If there is no
 * open file, the "save as" dialog is shown. */
 ::method handleSave
  expose mainApp

  if mainApp~personFilePath<>.nil then
     mainApp~savePersonDataToFile(mainApp~personFilePath)
  else
     self~handleSaveAs


 /* Opens a FileChooser to let the user select a file to save to. */
::method handleSaveAs
  expose mainApp

  fileChooser=.bsf~new("javafx.stage.FileChooser")
   -- crate an array of file extensions (only one in this case)
  jarrExtensions=bsf.createJavaArrayOf("java.lang.String", "*.json")
  extFilter=  .bsf~new("javafx.stage.FileChooser$ExtensionFilter", "JSON files (*.json)", jarrExtensions)
  fileChooser~getExtensionFilters~add(extFilter)

  filePath=mainApp~personFilePath
  if filePath=.nil then
     fileChooser~setInitialDirectory(.bsf~new("java.io.File", "."))  -- set current directory
  else
     fileChooser~setInitialDirectory(.bsf~new("java.io.File", filespec("Directory",filePath)))  -- set directory

  -- Show save file dialog
  file = fileChooser~showSaveDialog(mainApp~primaryStage)
  if file<>.nil then
  do
     filePath=file~getPath
     if filePath~right(5)<>".json" then   -- Make sure it has the correct extension
        filePath||=".json"
     mainApp~savePersonDataToFile( filePath )
  end


/* Opens an about dialog. */
::method handleAbout
  expose mainApp

  lf="0a"x
  text="Original author of the Java version: Marco Jakob" lf"Website: http://code.makery.ch"                             -
     lf"Java tutorial: http://code.makery.ch/library/javafx-8-tutorial/" lf                                              -
     lf"Author of the ooRexx version: Rony G. Flatscher" lf"Website of Rexx related technologies: http://www.RexxLA.org" -
     lf"Using ooRexx (Open Object Rexx), website: https://sourceforge.net/projects/oorexx/ or http://www.ooRexx.org"     -
     lf"Using BSF4ooRexx (ooRexx-Java-bridge), website: https://sourceforge.net/projects/bsf4oorexx/"

  if .my.app~bDialogsAlertsAvailable then
  do
     alert=.fx.alert~new(.fx.Alert.Type~information)  -- create an information alert

     alert~setTitle("AddressApp")
     alert~setHeaderText(.nil)
     alert~setContentText(text)
     alert~showAndWait
  end
  else  -- use BSF.CLS' .bsf.dialog utility class
  do
     -- .bsf.dialog is defined in BSF.CLS and uses swing
    .bsf.dialog~messageBox(text, "AddressApp", "information")
  end


 /* Closes the application. */
::method handleExit
  .java.lang.System~exit(0)   -- the Java System class is always available by its environment name


::method handle   /* implements the interface "javafx.event"   */
  expose menuNew menuOpen menuSave menuSaveAs menuAbout menuExit mainApp menuBirthdayStatistics
  use arg event, slotDir

  tgtObjectName=event~getTarget~objectname

-- say self"::handle: event~toString:" pp(event~toString)

  select
     when tgtObjectName=menuNew   ~objectName then self~handleNew
     when tgtObjectName=menuOpen  ~objectName then self~handleOpen
     when tgtObjectName=menuSave  ~objectName then self~handleSave
     when tgtObjectName=menuSaveAs~objectName then self~handleSaveAs
     when tgtObjectName=menuAbout ~objectName then self~handleAbout
     when tgtObjectName=menuExit  ~objectName then self~handleExit

      -- tutorial, part 6:
     when tgtObjectName=menuBirthdayStatistics~objectName then mainApp~showPersonStatistics
     otherwise .error~say("UNKNOWN target:" pp(event~getTarget) pp(event~getTarget~tostring))
  end



/* tutorial, part 6 */
::class BirthdayStatisticsController

::method init        -- constructor
  expose monthNames barChart xAxis

  -- fetch the directory containing the JavaFX fx:id JavaFX objects and assign them us as for event handling (cf. method "handle")
  bsDir=.my.app~BirthdayStatistics.fxml
  barChart=bsDir~barChart
  xAxis   =bsDir~xAxis

  -- create and set the monthNames
  -- Get an array with the English month names.
  dfsClz=bsf.loadClass("java.text.DateFormatSymbols")
  locClz=bsf.loadClass("java.util.Locale")
  months = dfsClz~getInstance(locClz~ENGLISH)~getMonths
  --  Convert it to a list and add it to an ObservableList of months.
  monthNames=.fx.FXCollections~observableArrayList ~~addAll(bsf.loadClass("java.util.Arrays")~asList(months))
  xAxis~setCategories(monthNames)


/*  Sets the persons to show the statistics for. */
::method  setPersonData
  expose barChart

  use arg persons
  monthCounter=bsf.createJavaArray("int.class", 12)
  do p over persons
     parse value p~birthDay with "-" month "-"
     monthCounter[month]+=1
  end
  series=self~createMonthDataSeries(monthCounter)
  barChart~getData~add(series)


/* Creates a XYChart.Data object for each month. All month data is then returned as a series. */
::method createMonthDataSeries
  expose monthNames
  use arg monthCounter

  series=.bsf~new("javafx.scene.chart.XYChart$Series")
  seriesData=series~getData
  do i=1 to monthCounter~size
      -- force number by boxing montCounter value to int
     monthData=.bsf~new("javafx.scene.chart.XYChart$Data", monthNames~get(i-1), box("i",monthCounter[i]))
     seriesData~add(monthData)
  end
  return series


/* =================================================================================== */
/* for tutorial part 3: interacting with the user */
   /* Instead of a class we define a Rexx routine that carries out the checking */
::Routine validString    -- returns .true, if date is valid, .false else
  parse arg date
  signal on syntax
  .DateTime~fromStandardDate(date, "-")
  return .true
syntax:     -- Rexx condition raised
  return .false


