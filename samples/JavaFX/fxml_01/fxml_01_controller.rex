/* *************************************************************************************************** */
/* --------------------------------------------------------------------------------------------------- */
/* This routine will be called from the Rexx code defined with the "onAction" attribute in Button's definition */
::routine klickButtonAction public
       /* the following Rexx script annotation will incorporate the attribute (the Label
          object named "label" as this is its fx:id attribute in the FXML file and make
          it immediately available as a Rexx variable named "label" */
    /*@get("label")*/
    /* now the local Rexx variable named 'label' is available and can be immediately used: */
    label~setText("Clicked at:" .dateTime~new~string)

