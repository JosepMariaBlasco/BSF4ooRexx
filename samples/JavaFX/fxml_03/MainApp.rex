/*
   author:  Rony G. Flatscher
   date:    2016-11-30, preliminary

   purpose: this is the ooRexx version of the pure Java tutorial on how to use JavaFX, demonstrating
            among other things also TableView and ObservableList;
            cf. the excellent tutorial:

            There are two versions of the tutorial demonstrating the same application:

            - Java 1.7/7 ("JavaFX 2"): <http://code.makery.ch/library/javafx-2-tutorial/>
            - Java 1.8/8 ("JavaFX 8"): <http://code.makery.ch/library/javafx-8-tutorial/>

            Creation of the FXML-files was done exactly as the above Java tutorials suggested.

            The ooRexx code takes advantage of ooRexx features like the .local environment to
            store the primaryStage to ease coupling different parts of the Rexx application.

            All controllers are coded in one Rexx file ("Rexx package") as Rexx classes.


   license: Apache License 2.0


   invoke:
            rexx Main.rex
            rexxj.{cmd|sh} Main.rex

*/


.environment~my.app=.directory~new   -- directory to contain objects relevant to this application
   -- starting with JavaFX 8u40 true dialogs got introduced; taking advantage of them if running on Java 1.8 or higher

   -- import JavaFX classes that we may use more often
call bsf.import "javafx.fxml.FXMLLoader",                      "fx.FXMLLoader"
call bsf.import "javafx.scene.Scene",                          "fx.Scene"
call bsf.import "javafx.beans.property.SimpleStringProperty",  "fx.SimpleStringProperty"
call bsf.import "javafx.beans.property.SimpleIntegerProperty", "fx.SimpleIntegerProperty"

   -- since JavaFX 8u40
call bsf.import "javafx.scene.control.Alert",                  "fx.Alert"
   -- note "AlertType" is public "inner" enum class, i.e. it is defined within the class "Alert", hence
   -- the JavaDocs refer to "AlertType" as "Alert.AlertType": however the Java compiler produces the
   -- class file as "Alert$AlertType"
call bsf.import "javafx.scene.control.Alert$AlertType",        "fx.Alert.Type"


   -- create Rexx object that will control the FXML set up
rexxApp=.RxMainApplication~new
.my.app~mainApp=rexxApp        -- store the Rexx MainApp object in .my.app
   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
jRexxApp=BsfCreateRexxProxy(rexxApp, ,"javafx.application.Application")
   -- launch the application, invoke "start" and then stay up until the application closes
jRexxApp~launch(jRexxApp~getClass, .nil)     -- need to use this version of launch in order to work


::requires "BSF.CLS"    -- get Java support

::requires "rgf_util2.rex"    -- get Rexx utilities (installed with BSF4ooRexx) to ease debugging (cf. dump2(), ppCondition2()...)


/* =================================================================================== */
/* implements the abstract method "start" for the Java class "javafx.application.Application"
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java).
   It also controls
*/
::class RxMainApplication

   /* for tutorial Part 2: create an ObservableList and create and store Person data in it */
::attribute personData
-- ::attribute arrayListPersonData
::method init  -- constructor
  expose personData        -- observableArrayList (maintained by TableView)

   -- create and store an ObservableList
  personData=bsf.loadClass("javafx.collections.FXCollections")~observableArrayList
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



   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  expose locale
  use arg stage   -- we get the stage to use for our UI

   -- starting with JavaFX 8u40 true dialogs got introduced; taking advantage of them if running on Java 1.8 or higher
   -- get the JavaFX runtime version (only available when running it), e.g. "8.0.111-b14"
  .my.app~Dialogs.Alerts.Available=(.java.lang.system~getProperty("javafx.runtime.version")>8)  -- determine availability

  .my.app~primaryStage=stage      -- save in .my.app
  stage~setTitle("AddressApp")

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootLayoutUrl=.bsf~new("java.net.URL", "file:RootLayout.fxml")
  rootLayout   =.fx.FXMLLoader~load(rootLayoutUrl) -- load the fxml document
  .my.app~rootLayout=rootLayout   -- save in .my.app

  scene=.fx.scene~new(rootLayout) -- create a scene for our document
  .my.app~rootScene=scene
  stage~setScene(scene)                         -- set the stage to our scene

  -- scene~getStyleSheets~add("fxml_02.css")       -- use this CSS to style the UI

  -- img=.bsf~new("javafx.scene.image.Image", "oorexx_032.png")
  -- stage~getIcons~add(img)                       -- set application icon

--  say "/// ... ---> ... now getting at and filling the TableColumns ... <--- ... ///"
--  self~showPersonOverview     -- get the TableView, fill it with our data
      -- load PersonOverview.fxml, place it into the rootLayout, create the Rexx object controlling the PersonOverview form
  overviewUrl=.bsf~new("java.net.URL", "file:PersonOverview.fxml")
  overviewPage=.fx.FXMLLoader~load(overviewUrl)   -- load the fxml document, AnchorPage (root) returned
  .my.app~overviewPage=overviewPage
  .my.app~rootLayout~setCenter(overviewPage)
  .my.app~personOverviewController=.PersonOverviewController~new

--  say "done."

  stage~show                  -- now show the stage (and thereby our scene)


/* Load the fxml-form, define a stage for it, create an instance of the Rexx class controlling
   the fxml-form.
   Unlike the original tutorial we do not create the dialog and controller over and over again, we
   just cache them using two attributes and reuse them.
*/
::method showPersonEditDialog
  expose pedDialog pedController
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
     dialogStage~initModality(bsf.import("javafx.stage.Modality")~WINDOW_MODAL)

     dialogStage~initOwner(.my.app~primaryStage)
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

  firstNameColumn~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("firstNameProperty"), ,"javafx.util.Callback"))
  lastNameColumn ~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("lastNameProperty"),  ,"javafx.util.Callback"))

   /* for tutorial part 3: fill-in the Person's details */
  self~showPersonDetails(.nil)   -- clear the labels
   --  auto resize columns
  personTable~setColumnResizePolicy(personTable~CONSTRAINED_RESIZE_POLICY)    -- TableView constant

   -- add self as the ChangeListener (we have its method "changed" implemented) and EventHandler (method "handle" implemented)
  rp=BSFCreateRexxProxy(self, ,"javafx.beans.value.ChangeListener", "javafx.event.EventHandler")
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

/*
::method refresh        -- force a refresh
  expose personTable
say self"::refresh ..."
  personTable~setItems(.nil)  -- remove all entries
  self~setItems         -- add current personData
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
      if .my.app~Dialogs.Alerts.Available then
      do
         alert=.fx.alert~new(.fx.Alert.Type~WARNING)   -- create a warning alert

         alert~setTitle("No Selection")
         alert~setHeaderText(.nil)
         alert~setContentText("Please select a person in the table.")
         alert~showAndWait
      end
      else  -- use BSF.CLS' .bsf.dialog utility class
      do
         -- .bsf.dialog is defined in BSF.CLS and uses swing which may cause troubles
        .bsf.dialog~messageBox("Please select a person in the table.", "No Selection", "warning")
      end
  end



/* for tutorial part 3: fill-in the Person's details */
::method handleNewPerson
  tempPerson=.Person~new

  okClicked = .my.app~mainApp~showPersonEditDialog(tempPerson)
  if okClicked then
     .my.app~mainApp~getPersonData~add(tempPerson)


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
      if .my.app~Dialogs.Alerts.Available then
      do
         alert=.fx.alert~new(.fx.Alert.Type~WARNING)   -- create a warning alert

         alert~setTitle("No Selection")
         alert~setHeaderText(.nil)
         alert~setContentText("Please select a person in the table.")
         alert~showAndWait
      end
      else  -- use BSF.CLS' .bsf.dialog utility class
      do
         -- .bsf.dialog is defined in BSF.CLS and uses swing which may cause troubles
        .bsf.dialog~messageBox("Please select a person in the table.", "No Selection", "warning")
      end
  end


   -- JavaFX event handlers implemented in Rexx
::method changed  /* implements the interface "javafx.beans.value.ChangeListener" */
  use arg observable, oldValue, newValue
  self~showPersonDetails(newValue)  -- fill in the labels to show currently selected Person's details

::method handle   /* implements the interface "javafx.event"   */
  expose btnNew btnEdit btnDelete
  use arg event, slotDir


  tgtObjectName=event~getTarget~objectname
-- say self"::handle: event~toString:" pp(event~toString)
  select
     when tgtObjectName=btnNew~objectName    then self~handleNewPerson  -- say "BUTTON NEW  was the target!"
     when tgtObjectName=btnEdit~objectName   then self~handleEditPerson -- say "BUTTON EDIT was the target!"
     when tgtObjectName=btnDelete~objectName then self~handleDeletePerson
     otherwise say "UNKNOWN target:" pp(event~getTarget) pp(event~getTarget~tostring)
  end


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) for PropertyValueFactory */

/* This class allows instances that remember the message to be sent to person instances to
   return the property of the attribute that should be shown in the table cell.
*/
::class PropertyValueFactory
::method init
  expose  propName   -- name of property getter method
  use arg propName

::method call
  expose propName
  use arg o          -- an observable value for the ooRexx person object boxed in a Java RexxProxy object
  return BsfRexxProxy(o~getValue)~send(propName)



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
-- say self"::handle, event="pp(event~toString)

  tgtObjectName=event~getTarget~objectname
  select
     when tgtObjectName=btnOK~objectName     then self~handleOK         -- say "BUTTON OK    was the target!"
     when tgtObjectName=btnCancel~objectName then self~handleCancel     -- say "BUTTON CANCEL was the target!"
     otherwise say "UNKNOWN target:" pp(event~getTarget) pp(event~getTarget~tostring)
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

  -- .bsf.dialog is defined in BSF.CLS and uses swing which may cause troubles
  if .my.app~Dialogs.Alerts.Available then
  do
     alert=.fx.alert~new(.fx.Alert.Type~ERROR)  -- create a warning alert
     alert~setTitle("Invalid Fields")
     alert~setHeaderText("Please correct the listed errors!")
     alert~setContentText("Invalid values:" LF errorMessage)
     alert~showAndWait
  end
  else  -- use BSF.CLS' .bsf.dialog utility class
  do
     -- .bsf.dialog is defined in BSF.CLS and uses swing which may cause troubles
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
  use arg strFirstName, strLastName


  firstName=.fx.SimpleStringProperty~new(strFirstName)
  lastName =.fx.SimpleStringProperty~new(strLastName)

  -- some initial dummy data
  street     = .fx.SimpleStringProperty~new("some street")
  postalCode = .fx.SimpleIntegerProperty~new(1234)
  city       = .fx.SimpleStringProperty~new("some city")
  birthday   = .fx.SimpleStringProperty~new(.DateTime~new~standardDate("-"))    -- ooRexx .DateTime


::routine dumpConditionObject
  use arg co
  say ppCondition2(co)
  co~Additional[2]~printStackTrace
  say "<---"



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

