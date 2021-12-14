import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test


class CommentTest {

    @Test
    fun pipeAtTheEndOfCommentTest() {
        val ooRexxSource =
            """
/* ------------------------------------------------------------------------- */
::method onEventSelected unguarded
/* ------------------------------------------------------------------------- */
/* an event has been selected                                                */
expose elist dtev thisevent PB_Regs custEd lastdatetime alist clist
use arg id, index, state, force? = .false

   if state == 'UNSELECTED'                                -- no event selected
   then PB_Regs~disable                           -- so disable regulars button
   else do
      uid = elist~itemText(index,1)                           -- find the event

                                -- it is a reselection of the current event
      if thisEvent~isA(.icalEvent) -
      ,  uid = thisEvent~uid       -
      , force? \= .true
      then RETURN                                                          -->|

   -- set the current event variable to point to this
      custEd~title = ''
      do event over dtev
         if event~uid = uid
         then thisEvent = event
      end /* DO */

   -- ensure event is in mirror
      eventID = self~getclassId(thisEvent)

      if thisEvent~BookOnline = .true
      then do
         clist~deleteAll
         alist~deleteAll
         if .nil = thisEvent~BookingUrl
         then tag = 'BOOK ONLINE'
         else do
            tag = 'BOOK ONLINE' thisEvent~BookingURL
            self~start('EVENTBRITEATTENDEES' -
                      ,thisevent             -
                      ,thisevent~UID~copy)                       -- fork --v->|
         end /* DO */
         ord = alist~addRow(,,'','','Book','Online',tag)
         alist~setColumnWidthpx(3,'AUTO')
         alist~select(ord)
      end /* DO */
      else self~refreshRegularsAndBooked  -- refresh the regulars and booked customer lists

   end

   self~title = 'WLBC Studio -' lastDateTime~normalDate '-' ,
                 thisEvent~dtStart~normalTime~left(5) ICAL_unescape(thisEvent~summary)

   self~statBarTitle  -- clear StatBar
   .my.log~logQ('event selected:' ICAL_unescape(thisEvent~summary))
"""
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}