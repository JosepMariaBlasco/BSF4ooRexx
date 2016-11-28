/* TODO: create another Rexx-Sample matching <http://docs.oracle.com/javafx/2/get_started/hello_world.htm>
*/

/*

   purpose: this is the ooRexx version of the pure Java tutorial on how to use JavaFX, demonstrating
            among other things also TableView and ObservableList;
            cf. the excellent tutorial:

            There are two versions of the tutorial demonstrating the same application:

            - Java 1.7/7 ("JavaFX 2"): <http://code.makery.ch/library/javafx-2-tutorial/>
            - Java 1.8/8 ("JavaFX 8"): <http://code.makery.ch/library/javafx-8-tutorial/>

            Creation of the FXML-files was done exactly as the above Java tutorials suggested.

            The ooRexx code takes advantage of ooRexx features like the .local environment to
            store the primaryStage to ease coupling different parts of the Rexx application


   invoke:
            rexx fxml_02
            rexx fxml_02 en
               ... use English properties file
            rexx fxml_02 de
               ... use German properties file
*/

   /* only "de" has an effect and will use the German translation for the */
parse arg locale .

.environment~my.app=.directory~new   -- directory to contain objects relevant to this application

   -- create Rexx object that will control the FXML set up
rexxHandler=.RxMainAppHandler~new
.my.app~mainApp=rexxHandler    -- store the Rexx MainApp object in .my.app

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
rxApp=BsfCreateRexxProxy(rexxHandler,,"javafx.application.Application")

   -- launch the application, invoke "start" and then stay up until the application closes
rxApp~launch(rxApp~getClass, .nil)     -- need to use this version of launch in order to work
say center(" after rxApp~launch ", 70, "-")

::requires "BSF.CLS"    -- get Java support

::requires "rgf_util2.rex"    -- get Rexx utilities (installed with BSF4ooRexx) to ease debugging (cf. dump2(), ppCondition2()...)


/* =================================================================================== */
/* implements the abstract method "start" for the Java class javafx.application.Application
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java)
*/
::class RxMainAppHandler

   /* for tutorial Part 2: create an ObservableList and create and store Person data in it */
::attribute personData
-- ::attribute arrayListPersonData
::method init  -- constructor
  expose personData -- arrayListPersonData
   -- create and store an ObservableList
  personData=bsf.loadClass("javafx.collections.FXCollections")~observableArrayList
      -- this creates ooRexx Person objects that get stored as Java RexxProxy objects
  personData~add(.person~new("Hans", "Muster"))
  personData~add(.person~new("Ruth", "Mueller"))
  personData~add(.person~new("Heinz", "Kurz"))
  personData~add(.person~new("Cornelia", "Meier"))
  personData~add(.person~new("Werner", "Meyer"))
  personData~add(.person~new("Lydia", "Kunz"))
  personData~add(.person~new("Anna", "Best"))
  personData~add(.person~new("Stefan", "Meier"))
  personData~add(.person~new("Martin", "Mueller"))

/*
  al=.bsf~new("java.util.ArrayList")
  aL~add(.person~new("Hans", "Muster"))
  aL~add(.person~new("Ruth", "Mueller"))
  aL~add(.person~new("Heinz", "Kurz"))
  aL~add(.person~new("Cornelia", "Meier"))
  aL~add(.person~new("Werner", "Meyer"))
  aL~add(.person~new("Lydia", "Kunz"))
  aL~add(.person~new("Anna", "Best"))
  aL~add(.person~new("Stefan", "Meier"))
  aL~add(.person~new("Martin", "Mueller"))
  arrayListPersonData=aL

  personData=bsf.loadClass("javafx.collections.FXCollections")~observableList(al)
*/



   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  expose locale
  use arg stage   -- we get the stage to use for our UI

  .my.app~primaryStage=stage      -- save in .my.app
  stage~setTitle("AddressApp")

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootLayoutUrl=.bsf~new("java.net.URL", "file:RootLayout.fxml")
  rootLayout=bsf.loadClass("javafx.fxml.FXMLLoader")~load(rootLayoutUrl) -- load the fxml document
  .my.app~rootLayout=rootLayout   -- save in .my.app

  scene=.bsf~new("javafx.scene.Scene", rootLayout) -- create a scene for our document
  .my.app~rootScene=scene
  stage~setScene(scene)                         -- set the stage to our scene

  -- scene~getStyleSheets~add("fxml_02.css")       -- use this CSS to style the UI

  -- img=.bsf~new("javafx.scene.image.Image", "oorexx_032.png")
  -- stage~getIcons~add(img)                       -- set application icon

  say "/// ... ---> ... now getting at and filling the TableColumns ... <--- ... ///"
  self~showPersonOverview     -- get the TableView, fill it with our data
  say "done."

  stage~show                  -- now show the stage (and thereby our scene)



::method showPersonOverview
/*
     // Load the fxml file and set into the center of the main layout
   FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/PersonOverview.fxml"));
   AnchorPane overviewPage = (AnchorPane) loader.load();
   rootLayout.setCenter(overviewPage);
*/
   overviewUrl=.bsf~new("java.net.URL", "file:PersonOverview.fxml")
   overviewPage=bsf.loadClass("javafx.fxml.FXMLLoader")~load(overviewUrl) -- load the fxml document, AnchorPage (root) returned
  .my.app~overviewPage=overviewPage
  .my.app~rootLayout~setCenter(overviewPage)
  .my.app~personOverviewController=.PersonOverviewController~new

/*
  call "rgf_util2.rex"
  call dump2 .environment
  call dump2 .my.app~PERSONOVERVIEW.FXML, .my.app~PERSONOVERVIEW.FXML~location~toString
  call dump2 .my.app~RootLayout.fxml    , .my.app~rootLayout.fxml    ~location~toString
*/

/* =================================================================================== */
/* for tutorial part 2: define the PersonOverviewController */
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

   -- add us as the event handler to the buttons
  btnNew   ~setOnAction(rp)
  btnEdit  ~setOnAction(rp)
  btnDelete~setOnAction(rp)

  self~setItems      -- fill the TableView
  return

syntax:
  co=condition('object')
  call dumpConditionObject co -- show condition information
  raise propagate

::method refresh        -- force a refresh
  expose personTable
say self"::refresh ..."
  personTable~setItems(.nil)  -- remove all entries
  self~setItems         -- add current personData

::method setItems
  expose personTable
say self"::setItems ..."
  personTable~setItems(.my.app~mainApp~personData) -- add current personData

/* for tutorial part 3: fill-in the Person's details */
::method showPersonDetails
  expose firstNameLabel lastNameLabel streetLabel postalCodeLabel cityLabel birthdayLabel
  use arg p
say "showPersonDetails: person="pp(p)
-- if p<>.nil then p=p~getValue

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
say self"::handleDeletePerson: selectedIndex="pp(selectedIndex)

  if selectedIndex >= 0 then
  do
     -- IMPORTANT: we must use bsf.invokeStrict() to become able to give explicitly the argument's type as otherwise
     --            it may be possible that the wrong "remove(Object)" method is used instead of "remove(int)" (both
     --            methods behave differntly)
     personTable~getItems~bsf.invokeStrict("remove", "int", selectedIndex)

     -- do m over .my.app~mainApp~personData;say m; end  -- show current objects in the ObservableArrayList

  end
  else   -- .bsf.dialog is defined in BSF.CLS and uses swing which may cause troubles
     .bsf.dialog~messageBox("Please select a person in the table.", "No Selection", "warning")

/*
PersonEditDialog.fxml

        <Button mnemonicParsing="false" onAction="#handleOk" prefWidth="80.0" text="OK" />
        <Button mnemonicParsing="false" onAction="#handleCancel" prefWidth="80.0" text="Cancel" />

*/


   -- JavaFX event handlers implemented in Rexx
::method changed  /* implements the interface "javafx.beans.value.ChangeListener" */
  use arg observable, oldValue, newValue
  self~showPersonDetails(newValue)  -- fill in the labels to show currently selected Person's details

::method handle   /* implements the interface "javafx.event"   */
  expose btnNew btnEdit btnDelete
  use arg event, slotDir
say self":handle event:" pp(event~toString)
-- call dump2 slotDir, self"::handle - slotDir"
-- say "event~getTarget:" pp(event~getTarget) pp(event~getTarget~toString) "getEventType:" pp(event~getEventType) pp(event~getEventType~toString) "getName:" pp(event~getEventType~getName)
-- say "slotDir~METHODOBJECT~toString:" pp(slotDir~METHODOBJECT~toString)
  tgtObjectName=event~getTarget~objectname
  select
     when tgtObjectName=btnNew~objectName    then say "BUTTON NEW  was the target!"
     when tgtObjectName=btnEdit~objectName   then say "BUTTON EDIT was the target!"
     when tgtObjectName=btnDelete~objectName then self~handleDeletePerson
     otherwise say "UNKNOWN target:" pp(tgt) pp(tgt~tostring)
  end


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) for PropertyValueFactory */
::class PropertyValueFactory
::method init
  expose  propName   -- name of property getter method
  use arg propName

::method call
  expose propName
  use arg o          -- an observable value for the ooRexx person object boxed in a Java RexxProxy object
  return BsfRexxProxy(o~getValue)~send(propName)



/* =================================================================================== */
/* for tutorial part 2: define The Model Class */
::class Person
::method init class  -- class initializer
   -- import this Java class into ooRexx, make the ooRexx proxy class available as "SimpleStringProperty"
  call bsf.importClass "javafx.beans.property.SimpleStringProperty",  "SimpleStringProperty"
   -- import this Java class into ooRexx, make the ooRexx proxy class available as "SimpleIntegerProperty"
  call bsf.importClass "javafx.beans.property.SimpleIntegerProperty", "SimpleIntegerProperty"

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


  firstName=.SimpleStringProperty~new(strFirstName)
  lastName =.SimpleStringProperty~new(strLastName)

  -- some initial dummy data
  street     = .SimpleStringProperty~new("some street")
  postalCode = .SimpleIntegerProperty~new(1234)
  city       = .SimpleStringProperty~new("some city")
  birthday   = .SimpleStringProperty~new(.DateTime~new~standardDate("-"))    -- ooRexx .DateTime


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

