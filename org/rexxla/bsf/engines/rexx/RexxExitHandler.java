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
 * @since 2012-02-17
 */
public interface RexxExitHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "1.20120217";

    // --------------------------------------------------------------------------------
    //    Exit return actions
    /** Exit handled exit event     */  final static public int RXEXIT_HANDLED     =  0;
    /** Exit passes on exit event   */  final static public int RXEXIT_NOT_HANDLED =  1;
    /** Exit handler error occurred */  final static public int RXEXIT_RAISE_ERROR = -1;


    // --------------------------------------------------------------------------------
    // "eternal" TODO: check, if new exits get added to ooRexx and adapt accordingly the constants in here!
    //
    // System Exit function and sub-function definitions

    /** End of exit list.           */  final static public int RXENDLST       =            0;

    /** Process external functions. */  final static public int RXFNC          =       2;
    /** subcode value.              */  final static public int    RXFNCCAL    =  1;
    /** Process host commands.      */  final static public int RXCMD          =       3;
    /** subcode value.              */  final static public int    RXCMDHST    =  1;
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
    /** Return linelength(N/A OS/2) */  final static public int    RXSIOTLL    =  5;
    /** Halt processing.            */  final static public int RXHLT          =       7;
    /** Clear HALT indicator        */  final static public int    RXHLTCLR    =  1;
    /** Test HALT indicator         */  final static public int    RXHLTTST    =  2;
    /** Test ext trace indicator.   */  final static public int RXTRC          =       8;
    /** subcode value.              */  final static public int    RXTRCTST    =  1;
    /** Initialization processing.  */  final static public int RXINI          =       9;
    /** subcode value.              */  final static public int    RXINIEXT    =  1;
    /** Termination processing.     */  final static public int RXTER          =      10;
    /** subcode value.              */  final static public int    RXTEREXT    =  1;

    /** scripting function call     */  final static public int RXEXF          =      12;
    /** subcode value.              */  final static public int    RXEXFCAL    =  1;
    /** NOVALUE exit                */  final static public int RXNOVAL        =      13;
                                        final static public int    RXNOVALCALL =  1;
    /** VALUE function exit         */  final static public int RXVALUE        =      14;
                                        final static public int    RXVALUECALL =  1;
    /** Process external functions using
    object values. */                   final static public int RXOFNC         =      15;
    /** subcode value.              */  final static public int    RXOFNCCAL   =  1;


    /** 1 + largest exit number.    */  final static public int RXNOOFEXITS    =           16;

    /** Names of Rexx system exit function numbers between number 0 and 15.
     *  (As baseline is Java 1.4 enumerations cannot be used.)
     */
    static public String RX_EXIT_NAMES[]={"0=n/a!", "1=n/a!", "RXFNC", "RXCMD",
                            "RXMSQ", "RXSIO", "6=n/a!", "RXHLT", "RXTRC", "RXINI", "RXTER",
                            "11=n/a!", "RXEXF", "RXNOVAL", "RXVALUE", "RXOFNC"};

    /** Exit handler signature (called via native code) according to the ooRexx API
     *  documentation in <code>rexxpg.pdf</code>.
     *
     * @param slot this is an opaque <code>Object</code> argument which must be supplied
     *             unchanged to the direct Rexx API methods in {@link RexxHandler}
     * @param exitNumber the Rexx exit number
     * @param subFunction the Rexx exit subfunction number
     * @param parmBlock an <code>Object[]</code> which is structured according to the ooRexx
     *                  exit documentation in <code>rexxpg.pdf</code>; each structure is itself
     *                  contained in an <code>Object[]</code>
     */
    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock);


/** The following documents briefly the parmBlock-array

    system exit # RXNOVAL/13:

        parmBlock[0]: variable_name - a String
        parmBlock[1]: value         - any value the exit handler wants the variable to be assigned to

    system exit ...

*/

}



