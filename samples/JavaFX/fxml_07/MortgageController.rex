/* invoked via JavaFX using the Java scripting framework, hence RexxScriptEngine executes the prolog code */
-- rgf, 2019-02-11 -> 16
d1=.dateTime~new     -- save invocation date and time
parse source s . fn

bDebug=.true      -- change to .false to inhibit the debug output
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

   -- bsf.importClass allows ooRexx "new" messages to be sent to the imported class to create instances
call bsf.importClass "javafx.scene.control.Tooltip"        , "Tooltip"
call bsf.importClass "javafx.scene.control.Alert"          , "fxAlert"
call bsf.importclass "javafx.scene.control.Alert$AlertType", "fxAlertType"   -- nested enumeration class, hence "$" instead of "."
.context~package~local~toolkit=bsf.loadClass("java.awt.Toolkit")~getDefaultToolkit

-- the following object will be stored in this package's local directory (since ooRexx 5.0)
.context~package~local~currency=.NumberFormat~getCurrencyInstance ~~setRoundingMode(.RoundingMode~half_up)

/* use RexxScriptEngine  facility "Rexx script annotation" to fetch ScriptContext supplied objects*/
/* @get( idGridPane ) */
   /* supply related JavaFX component object as second argument, callback will be directly via Rexx */
jrxObj=bsfCreateRexxProxy(.filterEventHandler~new,,"javafx.event.EventHandler")
idGridPane~addEventFilter(.keyEvent~any, jrxObj)   -- let us act on the events directed at any TextField component

-- fetch Slider/Label/TextField objects add listeners to the sliders to update their corresponding TextFields:
/* @get( idCreditTextField idInterestTextField idMonthsTextField ) */
/* @get( idCreditLabel   idInterestLabel   idMonthsLabel       ) */
/* @get( idCreditSlider    idInterestSlider    idMonthsSlider ) */
/* @get( idFormattedCreditTF ) */

   /* add tooltips to text fields, labels and sliders, using the slider's min and max values */
call addTooltip idCreditTextField,   idCreditLabel,   idCreditSlider
call addTooltip idInterestTextField, idInterestLabel, idInterestSlider
call addTooltip idMonthsTextField,   idMonthsLabel,   idMonthsSlider

idFormattedCreditTF~text=.currency~format(idCreditTextField~text)
chL="javafx.beans.value.ChangeListener"
idCreditSlider  ~valueProperty~addListener(BsfCreateRexxProxy(.rexxKreditBetragSlider~new,idCreditTextField,chL))
idInterestSlider~valueProperty~addListener(BsfCreateRexxProxy(.rexxZinsSatzSlider~new,    idInterestTextField,chL))
idMonthsSlider  ~valueProperty~addListener(BsfCreateRexxProxy(.rexxLaufZeitSlider~new,    idMonthsTextField,chL))
d2=.dateTime~new
say "--- MortageController.rex ("pp(fn)"), at end of prolog code" pp(d2) "duration:" pp(d2-d1)
say

::requires "BSF.CLS"

/* ------------------------------------------------------------------------- */
::routine dumpSlotDir         -- dump content of slotDir
  use arg slotDir
  do idx over slotDir~allindexes~sort
      say "   " pp(idx)~left(31,".") || ":" pp(slotDir[idx])
  end

/* ------------------------------------------------------------------------- */
::routine dumpScriptContext   -- dump all current Bindings of ScriptContext
  use arg scriptContext
  if scriptContext~isA(.slot.argument) then scriptContext=scriptContext~scriptContext

  names=.directory~of( (100,"ENGINE"), (200,"GLOBAL") )
  do scope over scriptContext~getScopes
     if names~hasentry(scope) then info= "("names~entry(scope)")"
                              else info= ""
     say "scope #" pp(scope) info
     bindings=scriptContext~getBindings(scope)
     dir=.directory~new
     do with index idx item it over bindings
        dir~setEntry(idx, (idx, it))
     end
     do idx over dir~allindexes~sort
        val=dir[idx]
        say "   " pp(idx)~left(31,".") || ":" pp(val[1])~left(31,".") "->" pp(val[2])
     end
     say
  end

/* ------------------------------------------------------------------------- */
::routine addTooltip    -- create Tooltip text from slider's min/max values, add it to slider and textField
  use arg textField, label, slider
  t=.tooltip~new("Numeric value must be between min="pp(slider~min) "and max="pp(slider~max))
  textField~tooltip=t
  label    ~tooltip=t
  slider   ~tooltip=t

/* ------------------------------------------------------------------------- */
/* will be invoked via JavaFX using the Java scripting framework, ie. RexxScriptEngine */
::routine rexxCalculateButtonPressed public
  /* @get( idCreditTextField idInterestTextField idMonthsTextField idRateTextField idTotalAccumulatedPaymentsTextField ) */
  c=idCreditTextField~text   -- get current value
  m=idMonthsTextField~text   -- get current value
  i=idInterestTextField~text -- get current value

  t = .Math~pow(1+(i/100)/12,m)
  if i=0 then
     rate = c/m
  else
     rate = (c*t * ((1+(i/100)/12)-1) / (t-1))

  idRateTextField~text                    =.currency~format(rate)
  idTotalAccumulatedPaymentsTextField~text=.currency~format(rate*m)


/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx */
::class  rexxKreditBetragSlider public    -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 2
  .my.app~Mortgage.fxml~idFormattedCreditTF~text=.currency~format(newValue)

/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx */
::class rexxZinsSatzSlider public         -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 1

/* ------------------------------------------------------------------------- */
/* will be invoked directly via Rexx */
::class rexxLaufZeitSlider public         -- update TextField, if slider gets moved
::method changed
  use arg ov, oldValue, newValue, slotDir
  call updateTextField slotDir~userData, newValue, 0

/* ------------------------------------------------------------------------- */
::routine updateTextField  -- save caret position, change value, reset caret position
  use arg textField, newValue, nr
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
     tfValue=target~text     -- get the current text, should be a number
     pkgDir=.context~package~local
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
               headerText="The currently entered value" pp(tfValue) "exceeds the defined range!"
               contentText="Defined range: min="pp(slider~min) "and max="pp(slider~max) -
                           "0a"x"Resetting to previous value:" pp(pkgDir~key_typed_text)
            end
            else  -- checks are alright, now update the slider marker
            do
               slider~value=tfValue   -- position marker on slider accordingly
               if id="idCreditTextField" then    -- reflect new value in legible form
                  myApp~idFormattedCreditTF~text=.currency~format(tfValue)
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
