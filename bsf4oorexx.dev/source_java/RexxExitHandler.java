package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...

/**
   Interface class for Rexx exit handlers. Cf. ooRexx' <code>rexxpg.pdf</code> documentation.

 * <p>All final static fields that use ooRexx names also use the ooRexx constant values from
 * the ooRexx include file <code>rexxapidefs.h</code>, from which the descriptions and values
 * got copied from.
 *

 * <pre>------------------------ Apache Version 2.0 license -------------------------
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
 * ----------------------------------------------------------------------------- </pre>
 *
 *
 * @author Rony G. Flatscher
 * @version 1.0
 * @since 2012-02-25
 */

/* 2012-02-26, rgf  - fixed some formatting
*/

public interface RexxExitHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "100.20120225";

    // --------------------------------------------------------------------------------
    //    Exit return actions
    /** Exit handled exit event     */  final static public int RXEXIT_HANDLED     =  0;
    /** Exit passes on exit event   */  final static public int RXEXIT_NOT_HANDLED =  1;
    /** Exit handler error occurred */  final static public int RXEXIT_RAISE_ERROR = -1;


    // --------------------------------------------------------------------------------
    // "eternal" TODO: check, if new exits get added to ooRexx and adapt accordingly the constants in here!
    //
    // System Exit function and sub-function definitions

    /** End of exit list.           */  final static public int RXENDLST       =       0;

    /** Process external functions. */  final static public int RXFNC          =       2;
    /** Processes calls to external
        functions.                  */  final static public int    RXFNCCAL    =  1;

    /** Process host commands.      */  final static public int RXCMD          =       3;
    /** Calls a named subcommand
        handler.                    */  final static public int    RXCMDHST    =  1;

    /** Manipulate queue.           */  final static public int RXMSQ          =       4;
    /** Pull a line from queue      */  final static public int    RXMSQPLL    =  1;
    /** Place a line on queue       */  final static public int    RXMSQPSH    =  2;
    /** Return num of lines on queue*/  final static public int    RXMSQSIZ    =  3;
    /** Set active queue name       */  final static public int    RXMSQNAM    = 20;

    /** Session I/O.                */  final static public int RXSIO          =       5;
    /** SAY a line to STDOUT        */  final static public int    RXSIOSAY    =  1;
    /** Trace output                */  final static public int    RXSIOTRC    =  2;
    /** Read from char stream       */  final static public int    RXSIOTRD    =  3;
    /** DEBUG read from char stream */  final static public int    RXSIODTR    =  4;
    /** Return linelength (n/a OS/2)*/  final static public int    RXSIOTLL    =  5;

    /** Halt processing.            */  final static public int RXHLT          =       7;
    /** Clear HALT indicator        */  final static public int    RXHLTCLR    =  1;
    /** Test HALT indicator         */  final static public int    RXHLTTST    =  2;

    /** Test ext trace indicator.   */  final static public int RXTRC          =       8;
    /** Tests the external trace
        indicator.                  */  final static public int    RXTRCTST    =  1;

    /** Initialization processing.  */  final static public int RXINI          =       9;
    /** Initialization exit.        */  final static public int    RXINIEXT    =  1;

    /** Termination processing.     */  final static public int RXTER          =      10;
    /** Termination exit.           */  final static public int    RXTEREXT    =  1;

    /** scripting function call     */  final static public int RXEXF          =      12;
    /** Processes calls to external
        functions.                  */  final static public int    RXEXFCAL    =  1;

    /** NOVALUE exit                */  final static public int RXNOVAL        =      13;
    /** Processes a Rexx NOVALUE
        condition.                  */  final static public int    RXNOVALCALL =  1;

    /** VALUE function exit         */  final static public int RXVALUE        =      14;
    /** Processes an extended call
        to the VALUE() built-in
        function.                   */  final static public int    RXVALUECALL =  1;

    /** Process external functions
        using object values.        */  final static public int RXOFNC         =      15;
    /** Processes calls to external
        functions.                  */  final static public int    RXOFNCCAL   =  1;


    /** 1 + largest exit number.    */  final static public int RXNOOFEXITS    =           16;

    /** Names of Rexx system exit function numbers between number 0 and 15.
     *  (As baseline is Java 1.4 enumerations cannot be used.)
     */
    static final public String RX_EXIT_NAMES[]={"0=n/a!", "1=n/a!", "RXFNC", "RXCMD",
                            "RXMSQ", "RXSIO", "6=n/a!", "RXHLT", "RXTRC", "RXINI", "RXTER",
                            "11=n/a!", "RXEXF", "RXNOVAL", "RXVALUE", "RXOFNC"};

    /** Exit handler signature (called via native code) according to the ooRexx API
     *  documentation in <code>rexxpg.pdf</code>.
     *
     *

       <p>
       The following documents briefly the <code>parmBlock</code>-array that gets
       supplied to the Java exit handler, depending on the exit number and subfunction.

       Please refer also to the ooRexx documentation &quot;rexxpg.pdf&quot;, section
       entitled &quot;Context Exit Definitions&quot; on which this implementation is
       based upon.

        <table border>
        <tr>
            <th>Exit name/number
            <th>subfunction name/number
            <th>Description

        <tr> <!-- ================================================ -->
            <td><code>RXFNC/2</code>
            <td><code>RXFNCCAL/1</code>
            <td>Processes calls to external functions.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxfnc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfferr</code>, settable (&quot;invalid call to routine&quot;)
                        <li><code>[1] rxffnfnd</code>, settable (&quot;function not found&quot;)
                        <li><code>[2] rxffsub</code>, if <code>true</code> then called as subroutine, else as function
                        </ul>

                <tr>
                    <td><code>[1] rxfnc_name</code>, subroutine/function name, type: <code>String</code>

                <tr>
                    <td><code>[2] rxfnc_que</code>, current queue name, type: <code>String</code>

                <tr>
                    <td><code>[3] rxfnc_argv</code>, supplied arguments, type: <code>Object[]</code>, can be <code>null</code>

                <tr>
                    <td><code>[4] rxfnc_retc</code>, return value by handler, if any, type: <code>String</code>

                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXCMD/3</code>
            <td><code>RXCMDHST/1</code>
            <td>Calls a named subcommand handler.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxfnc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxffail</code>, settable (&quot;command failed (trap FAILURE condition)&quot;)
                        <li><code>[1] rxffcerr</code>, settable (&quot;command raises an error (trap ERROR condition)&quot;)
                        </ul>

                <tr>
                    <td><code>[1] rxcmd_address</code>, address name, type: <code>String</code>

                <tr>
                    <td><code>[2] rxcmd_dll</code>, DLL name, if handler is in a DLL, empty string else, type: <code>String</code>

                <tr>
                    <td><code>[3] rxcmd_command</code>, command, type: <code>String</code>

                <tr>
                    <td><code>[4] rxcmd_retc</code>, return value by handler, if any, type: <code>String</code>

                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXMSQ/4</code>
            <td><code>RXMSQPLL/1</code>
            <td>Pulls a line from the external data queue.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxmsq_retc</code>, return value by handler, if any, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXMSQPSH/2</code>
            <td>Places a line in the external data queue.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxfnc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfmlifo</code>, if set <code>LIFO (QUEUE)</code>, if <code>false FIFO (PUSH)</code>
                        </ul>

                <tr>
                    <td><code>[1] rxmsq_value</code>, entry to be pushed/queued, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXMSQSIZ/3</code>
            <td>Returns the number of lines in the external data queue.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxmsq_size</code>, number of lines in queue, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXMSQNAM/20</code>
            <td>Sets the name of the active external data queue.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxmsq_name</code>, new external data queue name, type: <code>String</code>
                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXSIO/5</code>
            <td><code>RXSIOSAY/1</code>
            <td>Writes a line to the standard output stream.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxsio_string</code>, string to display, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXSIOTRC/2</code>
            <td>Writes trace and error message output to the standard error stream.
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxsio_string</code>, trace line to display, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXSIOTRD/3</code>
            <td>Reads from standard input stream.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxsiotrd_retc</code>, return value by handler, if any, type: <code>String</code>
                </table>

        <tr>
            <td>
            <td><code>RXSIODTR/4</code>
            <td>Interactive debug input.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxsiodtr_retc</code>, return value by handler, if any, type: <code>String</code>
                </table>

        <tr> <!-- ================================================ -->
            <td><code>RXHLT/7</code>
            <td><code>RXHLTCLR/1</code>
            <td>Clears the HALT condition.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxhlt_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfhhalt</code>, settable, if set to <code>true</code>, the <code>HALT</code> condition will be cleared
                        </ul>
                </table>


        <tr>
            <td>
            <td><code>RXHLTTST/2</code>
            <td>Tests the halt indicator.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxhlt_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfhhalt</code>, settable, if set to <code>true</code>, the <code>HALT</code> condition will be raised
                        </ul>
                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXTRC/8</code>
            <td><code>RXTRCTST/1</code>
            <td>Tests the external trace indicator.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxtrc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxftrace</code>, settable, if set to <code>true</code>, then tracing will take place
                        </ul>
                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXINI/9</code>
            <td><code>RXINIEXT/1</code>
            <td>Initializing processing.


        <tr> <!-- ================================================ -->
            <td><code>RXTER/10</code>
            <td><code>RXTEREXT/1</code>
            <td>Termination processing.


        <tr> <!-- ================================================ -->
            <td><code>RXEXF/12</code>
            <td><code>RXEXFCAL/1</code>
            <td>Processes calls to external functions (ooRexx version), at end of search for an external routine.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxfnc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfferr</code>, settable (&quot;invalid call to routine&quot;)
                        <li><code>[1] rxffnfnd</code>, settable (&quot;function not found&quot;)
                        <li><code>[2] rxffsub</code>, if <code>true</code> then called as subroutine, else as function
                        </ul>

                <tr>
                    <td><code>[1] rxfnc_name</code>, subroutine/function name, type: <code>String</code>

                <tr>
                    <td><code>[2] rxfnc_argv</code>, supplied arguments, type: <code>Object[]</code>, can be <code>null</code>

                <tr>
                    <td><code>[3] rxfnc_retc</code>, return value by handler, if any, type: <code>Object</code>

                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXNOVAL/13</code>
            <td><code>RXNOVALCALL/1</code>
            <td>Processes a Rexx <code>NOVALUE</code> condition.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] variable_name</code>, variable name, type: <code>String</code>

                <tr>
                    <td><code>[1] value</code>, return value by handler, if any, type: <code>Object</code>

                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXVALUE/14</code>
            <td><code>RXVALUECALL/1</code>
            <td>Processes an extended call to the <code>VALUE()</code> built-in function (&quot;BIF&quot;)
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] selector</code>, the environment selector name, type: <code>String</code>

                <tr>
                    <td><code>[1] variable_name</code>, variable name, type: <code>String</code>

                <tr>
                    <td><code>[2] value</code>, return value by handler, if any, type: <code>Object</code>

                </table>


        <tr> <!-- ================================================ -->
            <td><code>RXOFNC/15</code>
            <td><code>RXOFNCCAL/1</code>
            <td>Processes calls to external functions (ooRexx version), at beginning of the search for an external routine.<br>
                <code>parmBlock</code>:
                <table>
                <tr>
                    <td><code>[0] rxfnc_flags</code>, type: <code>boolean[]</code>
                        <ul>
                        <li><code>[0] rxfferr</code>, settable (&quot;invalid call to routine&quot;)
                        <li><code>[1] rxffnfnd</code>, settable (&quot;function not found&quot;)
                        <li><code>[2] rxffsub</code>, if <code>true</code> then called as subroutine, else as function
                        </ul>

                <tr>
                    <td><code>[1] rxfnc_name</code>, subroutine/function name, type: <code>String</code>

                <tr>
                    <td><code>[2] rxfnc_argv</code>, supplied arguments, type: <code>Object[]</code>, can be <code>null</code>

                <tr>
                    <td><code>[3] rxfnc_retc</code>, return value by handler, if any, type: <code>Object</code>

                </table>

       </table>
     *
     * @param slot this is an opaque <code>Object</code> argument which must be supplied
     *             unchanged to the direct Rexx API methods in {@link RexxHandler}
     * @param exitNumber the Rexx exit number
     * @param subFunction the Rexx exit subfunction number
     * @param parmBlock an <code>Object[]</code> which is structured according to the ooRexx
     *                  exit documentation in <code>rexxpg.pdf</code>; each structure is itself
     *                  contained in an <code>Object[]</code>
     *
     * @return RXEXIT_HANDLED (0), RXEXIT_NOT_HANDLED (1) or RXEXIT_RAISE_ERROR (-1)
     */
    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock);
}


