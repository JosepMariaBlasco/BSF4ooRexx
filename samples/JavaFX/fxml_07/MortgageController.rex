#!/usr/bin/env rexx
/* called by FXMLLoader on the "FX Application Thread" */
/* invoked via JavaFX using the Java scripting framework, hence RexxScriptEngine executes the prolog code */

d1=.dateTime~new     -- save invocation date and time
parse source s . fn  -- get fully qualified file name as supplied by the RexxScriptEngine

bDebug=.true         -- change to .false to inhibit the debug output
if bDebug=.true then
do
   say "--- MortageController.rex ("pp(fn)"), at begin of prolog code" pp(d1)
   say "// MortgageController.rex, dumping SlotDir:"
   call dumpSlotDir arg(1)
   say
   say "// MortgageController.rex, dumping SlotDir~scriptContext:"
   call dumpScriptContext arg(1)~scriptContext
   say
end

call bsf.loadClass "java.text.NumberFormat", "NumberFormat"
call bsf.loadClass "java.math.RoundingMode", "RoundingMode"
call bsf.loadClass "java.lang.Math",         "Math"
call bsf.loadClass "javafx.scene.input.KeyEvent", "KeyEvent"

   -- bsf.importClass allows ooRexx "new" messages to be sent to the imported class to create Java instances
call bsf.importClass "javafx.scene.control.Tooltip"        , "Tooltip"
call bsf.importClass "javafx.scene.control.Alert"          , "fxAlert"
   -- nested enumeration class, hence "$" instead of "." in the following Java class name
call bsf.importclass "javafx.scene.control.Alert$AlertType", "fxAlertType"

   -- needed for beeping in a platform independent form
.context~package~local~toolkit=bsf.loadClass("java.awt.Toolkit")~getDefaultToolkit

-- store a properly configured currency number formatter in the package's local directory (new in ooRexx 5.0)
pkgDir=.context~package~local    -- get package's local environment directory
pkgDir~currencyF=.NumberFormat~getCurrencyInstance ~~setRoundingMode(.RoundingMode~half_up)
pkgDir~decimalF=bsf.loadClass('java.text.DecimalFormat')~getinstance

   /* use RexxScriptEngine  facility "Rexx script annotation" to fetch ScriptContext supplied objects*/
/* @get( idGridPane ) */
jrxObj=bsfCreateRexxProxy(.filterEventHandler~new,,"javafx.event.EventHandler")
idGridPane~addEventFilter(.keyEvent~any, jrxObj)   -- let us act on the events directed at any TextField component

   /* fetch TextField/Label/Slider objects add listeners to the sliders to update their corresponding TextFields: */
/* @get( idCreditTextField idInterestTextField idMonthsTextField )   */
/* @get( idCreditLabel     idInterestLabel     idMonthsLabel       ) */
/* @get( idCreditSlider    idInterestSlider    idMonthsSlider )      */
/* @get( idFormattedCreditTF )                                       */
chL="javafx.beans.value.ChangeListener"
idCreditSlider  ~valueProperty~addListener(BsfCreateRexxProxy(.rexxKreditBetragSlider~new,idCreditTextField,chL))
idInterestSlider~valueProperty~addListener(BsfCreateRexxProxy(.rexxZinsSatzSlider~new,    idInterestTextField,chL))
idMonthsSlider  ~valueProperty~addListener(BsfCreateRexxProxy(.rexxLaufZeitSlider~new,    idMonthsTextField,chL))

   /* format the credit amoung with the currency formatter to make it better legible for the user: */
idFormattedCreditTF~text=.currencyF~format(idCreditTextField~text)

   /* add tooltips to text fields, labels and sliders, using the slider's min and max values */
call addTooltip idCreditTextField,   idCreditLabel,   idCreditSlider
call addTooltip idInterestTextField, idInterestLabel, idInterestSlider
call addTooltip idMonthsTextField,   idMonthsLabel,   idMonthsSlider

d2=.dateTime~new
say "--- MortageController.rex ("pp(fn)"), at end of prolog code" pp(d2) "duration:" pp(d2-d1)
say

::requires "BSF.CLS"

/* ------------------------------------------------------------------------- */
::routine addTooltip    -- create Tooltip text from slider's min/max values, add it to slider and textField
  use arg textField, label, slider

  call setDecimalPattern textField  -- set the formatting pattern
  min=.decimalF~format(slider~min)  -- get and format number
  max=.decimalF~format(slider~max)  -- get and format number
  t=.tooltip~new("Numeric value must be between min="pp(min) "and max="pp(max))
  t~setStyle("-fx-font-size: 14")   -- make the font larger to improve legibility
  textField~tooltip=t               -- assign tooltip
  label    ~tooltip=t               -- assign tooltip
  slider   ~tooltip=t               -- assign tooltip

/* ------------------------------------------------------------------------- */
::routine setDecimalPattern   -- determine type and set decimal formatting pattern accordingly
  use arg fxComponent
  id=fxComponent~id           -- determine number of decimal places
  if id~pos("Credit")>0        then .decimalF~applyPattern("#,##0.00")-- format to two decimal places
  else if id~pos("Interest")>0 then .decimalF~applyPattern("#,##0.0") -- format to a single decimal place
                               else .decimalF~applyPattern("#,##0")   -- format to not show any decimal places

/* ------------------------------------------------------------------------- */
::routine dumpScriptContext   -- dump all current Bindings of ScriptContext
  use arg scriptContext
  if scriptContext~isA(.slot.argument) then scriptContext=scriptContext~scriptContext

  names=.directory~of( (100,"ENGINE"), (200,"GLOBAL") )  -- standardized scope names
  do scope over scriptContext~getScopes
     if names~hasentry(scope) then info= "("names~entry(scope)")"
                              else info= ""
     say "scope #" pp(scope) info
     bindings=scriptContext~getBindings(scope)
     dir=.directory~new
     len=0
     do with index idx item it over bindings
        dir~setEntry(idx, (idx, it))
        len=max(len,idx~length)  -- determine longest entry
     end
     len+=2                      -- account for enclosing brackets
     do idx over dir~allindexes~sort
        val=dir[idx]
        say "   " pp(idx)~left(len,".") || ":" pp(val[1])~left(len,".") "->" pp(val[2])
     end
     say
  end

/* ------------------------------------------------------------------------- */
::routine dumpSlotDir            -- dump content of slotDir
  use arg slotDir
  do idx over slotDir~allindexes~sort
      say "   " pp(idx)~left(31,".") || ":" pp(slotDir[idx])
  end

/* ------------------------------------------------------------------------- */
/* will be invoked via JavaFX using the Java scripting framework, ie. RexxScriptEngine */
::routine rexxCalculateButtonPressed public
  /* @get( idCreditTextField idInterestTextField idMonthsTextField idRateTextField idTotalAccumulatedPaymentsTextField ) */
  c=idCreditTextField~text       -- get current value
  m=idMonthsTextField~text       -- get current value
  i=idInterestTextField~text     -- get current value

  t = .Math~pow(1+(i/100)/12,m)
  if i=0 then
     rate = c/m
  else
     rate = (c*t * ((1+(i/100)/12)-1) / (t-1))

  idRateTextField~text                    =.currencyF~format(rate)
  idTotalAccumulatedPaymentsTextField~text=.currencyF~format(rate*m)

/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx (no RexxScript annotations available) */
::class  rexxKreditBetragSlider public -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 2
  .my.app~Mortgage.fxml~idFormattedCreditTF~text=.currencyF~format(newValue)

/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx (no RexxScript annotations available) */
::class rexxZinsSatzSlider public      -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 1

/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx (no RexxScript annotations available) */
::class rexxLaufZeitSlider public      -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 0

/* ------------------------------------------------------------------------- */
::routine updateTextField  -- save caret position, change value, reset caret position
  use arg textField, newValue, nr
  numeric digits 20
  caretPos=textField~caretPosition     -- get current caret position
  textField~text=format(newValue,,nr)  -- will format the value to "nr" decimal positions
  textField~positionCaret(caretPos)    -- make sure caret is reset


/* ------------------------------------------------------------------------- */
/* cf. <https://docs.oracle.com/javafx/2/events/processing.htm>,
*       <https://www.tutorialspoint.com/javafx/javafx_event_handling.htm>
*
* "Event Capturing Phase": each node from root to component can intercept an event and
*                          even consume() it, such that it does not arrive at the targeted
*                          component
*  - event filtering should be carried out in the parent node of the TextField components,
*    i.e. in the GridPane node, hence using addFilter on it in the prolog code above
*
*  - using addEventFilter() to check key presses in the TextFields: saves the value
*    at KEY_PRESSED time to have a backup value to restore to, if at KEY_RELEASED
*    it turns out that the new value is not numeric anymore (in this case a JavaFX
*    Alert error popup will communicate that fact)
*
*   note: will be invoked directly via Rexx (no RexxScript annotations available)
*/
::class filterEventHandler public
::method handle   -- method that controls numeric input within range in all three TextField's
  use arg event, slotDir

-- say "--> filterEventHandler: event="pp(event~toString)
  target=event~target
  id=target~id                   -- get "id" attribute value, can be .nil
  if id=.nil then return         -- not interested in nodes that have no "id" set
  if id~pos("TextField")=0 then return -- interested in key presses in TextField nodes only

  strEventType=event~eventType~toString

  if strEventType="KEY_PRESSED" then   -- save TextField's object, text and caret position
  do
-- say "KEY_PRESSED:" pp(event~toString)
     pkgDir=.context~package~local
     pkgDir~key_typed_target       =target
     pkgDir~key_typed_text         =target~text
     pkgDir~key_typed_caretPosition=target~caretPosition
     return
  end

  if strEventType="KEY_TYPED" then
  do
-- say "KEY_TYPED:" pp(event~toString)
     return
  end

  else if strEventType="KEY_RELEASED" then
  do
-- say "KEY_RELEASED:" pp(event~toString)
     tfValue=target~text               -- get the current text, should be a number
     pkgDir=.context~package~local     -- get access to package's local environment (sinc ooRexx 5.0)
     if target~objectname=pkgDir~key_typed_target~objectName then   -- same object as previous "KEY_TYPED" target
     do
         title="" -- if set, an error title was defined
         if \datatype(tfValue,"Number") then   -- check for a valid number
         do
             title      ="Not a Number (NaN)"
             headerText ="The currently entered value" pp(tfValue) "is not a valid number!"
             contentText="Resetting to previous value:" pp(pkgDir~key_typed_text)
         end
         else    -- check and format the different fields
         do
            myapp=.my.app~Mortgage.fxml           -- get access to directory of JavaFX components with id value
            slider=myapp~entry(id~changeStr("TextField","Slider"))  -- get corresponding slider object
              -- check whether value is outside the defined range
            if tfValue<slider~min | tfValue>slider~max then
            do
               title="Value Exceeds Range"
               call setDecimalPattern slider
               headerText="The currently entered value" pp(.decimalF~format(tfValue)) "exceeds the defined range!"
               min=.decimalF~format(slider~min)
               max=.decimalF~format(slider~max)
               oldValue=pkgDir~key_typed_text
               if oldValue>=1000 then  -- format for better legibility
                  oldValue=oldValue "/" .decimalF~format(oldValue)

               contentText="Defined range: min="pp(min) "and max="pp(max) -
                           "0a0a"x"Resetting to previous value:" pp(oldValue)
            end
            else  -- checks are alright, now update the slider marker
            do
               slider~value=tfValue   -- position marker on slider accordingly
               if id="idCreditTextField" then    -- reflect new value in legible form
                  myApp~idFormattedCreditTF~text=.currencyF~format(tfValue)
            end
         end

              -- error condition?
         if title<>"" then   -- error information available, display it
         do
            .toolkit~beep    -- beep to alert
             alert=.fxAlert~new(.fxAlertType~ERROR)          -- create Alert dialog
             alert~~setTitle(title) ~~setHeaderText(headerText) ~~setContentText(contentText)
             res=alert~showAndWait      -- show the dialog, wait until user closes it, returns a java.util.Optional (isPresent(), get(), ...)
             target~text=pkgDir~key_typed_text
             target~positionCaret(pkgDir~key_typed_caretPosition)
             return
         end
     end
  end
  return