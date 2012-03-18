/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2012 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -----------------------------------------------------------------------------
 *
 * purpose: demonstrate the possibilities Java implemented system exit handlers allow for
 * needs:   testRexxWithSystemdExits.rxj
 *
 * @author  Rony G. Flatscher
 * @since   2012-02-19
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;


public class JavaRunRexxWith_RXINI_RXTER_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxConf+"]\n");

            // add system exits
            // we handle RXINI and RXTER by the same handler, i.e. this instance
        JavaRunRexxWith_RXINI_RXTER_Exit obj=new JavaRunRexxWith_RXINI_RXTER_Exit(rexxEngine, rexxConf);
        rexxConf.addExitHandler(RexxExitHandler.RXINI, obj );
        rexxConf.addExitHandler(RexxExitHandler.RXTER, obj );


        // invoke the interpreter and run the Rexx program
        System.err.println("edited  rexxconf=["+rexxConf+"]\n===> requiring (initializing) BSF.CLS ===>");
        rexxEngine.apply ("JavaRunRexxWithSystemExits.java", 0, 0, "::requires 'BSF.CLS'", null, null);
        System.err.println("<=== Java - after requiring 'BSF.CLS'");

            // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXINI_RXTER_Exit.rxj'  ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        System.err.println("\n(Java-side) after initializing BSF.CLS, we now execute ["+rexxCode+"]...\n");
        System.err.println("edited  rexxconf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        rexxEngine.terminate();
        System.err.println("<=== Java - after Rexx program has finished. <===");
    }



        // implementation of a RXINI_RXTER exit handler
    JavaRunRexxWith_RXINI_RXTER_Exit (RexxEngine rexxEngine, RexxConfiguration rexxConf)
    {
        this.rexxEngine=rexxEngine;         // get and save the RexxEngine
        this.rexxConf=rexxConf;
    }

    RexxEngine        rexxEngine=null;
    RexxConfiguration rexxConf=null;

    int counter=0;      // count # of invocations,
    int RXINI_count=0;
    int RXTER_count=0;

    /* NOTE: - RXINI and RXTER run for every initialization/termination of a Rexx program/routine;
               as such these exits get invoked when BSF.CLS itself gets loaded initially, causing
               a potential recursion problem, if pure Java objects are supplied to Rexx functions
               like setContextVariable() and the like; to inhibit this potential recursion, make
               sure in the RXINI handler that RXINI handling is set off before such calls, and make
               sure to re-enable them after the call
    */

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;

        if (exitNumber==RexxExitHandler.RXINI)
        {
            RXINI_count++;
            System.err.println("(Java-side) [RXINI_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], setting Rexx context variable 'RXSIO_START' to a new GregorianCalendar() value, counter=["+counter+"], RXINI_count=["+RXINI_count+"]");

                // do NOT do any RXINI/RXTER handling while setting the context variable
                // otherwise you might get recursions, if supplying a Java object, eventually
                // leading to a crash of the JVM
            RexxExitHandler old=null;
            try
            {
                    // deactivate RXINI/RXTER exits temporarily to not cause a recursive handling of Java objects by BSF.CLS
                    // WARNING: this will cause wrong RXINI vs. RXTER counters !
                old=rexxConf.setExitHandler(exitNumber, null);

                    // now set Rexx variable to a Java object (will be wrapped up appropriately)
                RexxHandler.setContextVariable(slot, "RXINI_START", new GregorianCalendar());

                    // reactivate RXINI/RXTER exits again
                old=rexxConf.setExitHandler(exitNumber, old);
            }
            catch (Throwable t) {}
        }

        else if (exitNumber==RexxExitHandler.RXTER)
        {
            RXTER_count++;
            System.err.println("(Java-side) [RXTER_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], fetching Rexx context variable 'RXINI_START' to calculate duration, counter=["+counter+"], RXTER_count=["+RXTER_count+"]");
            GregorianCalendar gcEnd  =new GregorianCalendar();
            Object gcOStart=RexxHandler.getContextVariable(slot, "RXINI_START");

                // NOTE: it may be the case that this handler is invoked many times before
                //       our Rexx program runs; in that case the BSF support has not been
                //       set up on the Rexx side and hence the value returned from Rexx
                //       is the beanName (a String), instead of the Java object
            Object oStart=RexxHandler.getContextVariable(slot, "RXINI_START");
            GregorianCalendar gcStart=null;
            if (oStart instanceof GregorianCalendar)    // o.k. infrastructure fully initialized, we received the pure Java object
            {
                gcStart=(GregorianCalendar) RexxHandler.getContextVariable(slot, "RXINI_START");
            }

            else if (oStart instanceof String)   // is String a beanName, if so try to fetch bean from BSF registry
            {   // as we know BSF.CLS will be used, we know that it has not been set up in full yet,
                // hence assume string is a beanName, so we attempt to do a manual lookup
                try // try to get Java object from BSF registry:
                {
                        // this version of lookupBean() will accept the "pure" beanName or the
                        // annotated one, that is led in either with "<O>" or "<S>"
                    Object o2=rexxEngine.lookupBean((String) oStart);
                    if (o2 instanceof GregorianCalendar)
                    {
                        gcStart=(GregorianCalendar) o2;
                    }
                }
                catch (Throwable t){}
            }

            if (oStart instanceof GregorianCalendar)
            {
                long start=gcStart.getTimeInMillis();
                long end  =gcEnd.getTimeInMillis();
                System.err.println("(Java-side) duration: ["+(end-start)+"] start (millis): ["+start+"], end (millis): ["+end+"], counter=["+counter+"]");
            }
        }
        else
        {
            System.err.println("(Java-side) ===> [RXINI_RXTER_exit] unhandled exit by this handler, counter=["+counter+"], RXINI_count=["+RXINI_count+"], RXTER_count=["+RXTER_count+"] <===");
            return RexxExitHandler.RXEXIT_NOT_HANDLED;
        }

        return RexxExitHandler.RXEXIT_HANDLED;
    }
}

