/* *************************************************************************************************** */
/* --------------------------------------------------------------------------------------------------- */
/* This routine will be called from the Rexx code defined with the "onAction" attribute in Button's definition */
::routine klickButtonAction public
  say .dateTime~new": arrived in code defined for Button's setOnAction method, i.e. the public routine 'klickButtonAction"

       /* the following Rexx script annotation will incorporate the attribute (the Label
          object named "label" as this is its fx:id attribute in the FXML file and make
          it immediately available as a Rexx variable named "label" */
    /*@get("label")*/
    /* now the local Rexx variable named 'label' is available and can be immediately used: */
  say '... current value of 'pp(label)': label~getText='pp(label~text)  ;
  label~text="Clicked at:" .dateTime~new  -- set the label

  say .dateTime~new': returning from the event handler' ;
  say
