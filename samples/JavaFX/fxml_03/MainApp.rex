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


/* =================================================================================== */
/* implements the abstract method "start" for the Java class javafx.application.Application
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java)
*/
::class RxMainAppHandler

   /* for tutorial Part 2: create an ObservableList and create and store Person data in it */
::attribute personData
::method init  -- constructor
  expose personData
   -- create and store an ObservableList
  personData=bsf.loadClass("javafx.collections.FXCollections")~observableArrayList
/*
    public MainApp() {
        // Add some sample data
        personData.add(new Person("Hans", "Muster"));
        personData.add(new Person("Ruth", "Mueller"));
        personData.add(new Person("Heinz", "Kurz"));
        personData.add(new Person("Cornelia", "Meier"));
        personData.add(new Person("Werner", "Meyer"));
        personData.add(new Person("Lydia", "Kunz"));
        personData.add(new Person("Anna", "Best"));
        personData.add(new Person("Stefan", "Meier"));
        personData.add(new Person("Martin", "Mueller"));
*/
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


   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  expose locale
  use arg stage   -- we get the stage to use for our UI

  .my.app~primaryStage=stage      -- save in .my.app
  stage~setTitle("AddressApp")

/*
// Load the root layout from the fxml file
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

           showPersonOverview();
*/


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
  stage~show                                    -- show the stage (and thereby our scene)
  self~showPersonOverview



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
  .my.app~personOverviewController=.PersonOverviewController~new(overviewPage)
  call "rgf_util2.rex"
  call dump2 .environment
  call dump2 .my.app~PERSONOVERVIEW.FXML, .my.app~PERSONOVERVIEW.FXML~location~toString
  call dump2 .my.app~RootLayout.fxml    , .my.app~rootLayout.fxml    ~location~toString

/* =================================================================================== */
/* for tutorial part 2: define the PersonOverviewController */
::class PersonOverviewController
::attribute personTable
::attribute firstNameColumn
::attribute lastNameColumn

::method init
  expose personTable firstNameColumn lastNameColumn
  use arg overviewPage
  poDir=.my.app~personOverview.fxml    -- fetch the directory containing the JavaFX fx:id controls and assign them to attribute
  personTable    =poDir~personTable
  firstNameColumn=poDir~firstNameColumn
say "firstNameColumn         :" pp(firstNameColumn) pp(firstNameColumn~getClass~toString) "rii_id:" pp(firstNameColumn~rii_id)
say "firstNameColumn~toString:" pp(firstNameColumn~toString)

  lastNameColumn =poDir~lastNameColumn

signal on syntax
   -- now set the callback method to fill the firstName column using the JavaFX property
   juclz=bsf.loadClass("javafx.util.Callback")
   say "--> juclz:" pp(juclz) pp(juclz~toString)
   jval=BsfCreateRexxProxy(.PropertyValueFactory~new("firstNameProperty"), ,"javafx.util.Callback")
   say "--> jval:" pp(jval) pp(jval~toString)
   say "--> juclz~isInstance(jval):" pp(juclz~isInstance(jval))

  firstNameColumn~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("firstNameProperty"), ,"javafx.util.Callback"))
  lastNameColumn~setCellValueFactory(BsfCreateRexxProxy(.PropertyValueFactory~new("lastNameProperty"),  ,"javafx.util.Callback"))


/* this works, still we are not able to get at the JavaFX objects inside FXML's FXML ... (could be
   done, but veeery cumbersome, rather employing the RexxScriptEngine instead ...)

  -- say 'rootNode~lookup("tableView"):' pp(rootNode~lookup("tableView"))
  say "overviewPage:" pp(overviewPage) pp(overviewPage~toString) pp(overviewPage~getScene)
  scene=overviewPage~getScene~getRoot  -- lookupAll only in root !
  -- scene=overviewPage~getScene
id="#colFirstName"
id="#tableView"
id="#rootId"
id="*"
  say "scene:" pp(scene) pp(scene~toString)
  say "id:" pp(id)
  say "  scene~lookUp(id)   :" pp(scene~lookup(id))
  lookupAll=scene~lookupAll(id)
  say "  scene~lookupAll(id):" pp(lookupAll)
  do node over lookupAll
     say "    " pp(node) "scene:" pp(node~getScene)
  end

--- */

  self~setItems
  return

syntax:
  co=condition('object')
  call dumpConditionObject co -- show condition information
  raise propagate

::method refresh        -- force a refresh
  expose personTable
  personTable~setItems(.nil)  -- remove all entries
  self~setItems         -- add current personData

::method setItems
  expose personTable
  personTable~setItems(.my.app~mainApp~personData) -- add current personData


/* =================================================================================== */
/* implements "R javafx.util.Callback<P,R>(P o) for PropertyValueFactory */
::class PropertyValueFactory
::method init
  expose  propName      -- name of property getter method
  use arg propName

::method call
  expose propName
  use arg o
--  say "...rexx: propName="pp(propName) "o~getValue="pp(o~getValue) "o~getClass~toString:" pp(o~getValue~getClass~toString)
  -- say "... propName="pp(propName) "o="pp(o) "o~getValue="pp(o~getValue) "o~getClass~toString:" pp(o~getValue~getClass~toString)
  -- return o~getValue~send(propName)        -- return the property

   -- we get a RexxProxy so we send it the Rexx message via Java, works:
  -- return o~getValue~sendMessage0(propName)        -- return the property

   -- we get the Rexx object off the RexxProxy and send it directly the Rexx message
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

::requires "rgf_util2.rex"
