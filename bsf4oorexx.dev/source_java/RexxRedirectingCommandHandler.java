package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...
import java.util.BitSet;
/**

   Interface class for Rexx redirecting command handlers. Cf. ooRexx'
   <code>rexxapi.pdf</code> documentation. Defines all new redirecting methods
   as default methods.

 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2022 Rony G. Flatscher
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
 * @since 2022-08-18 (BSF4ooRexx 850, baseline Java=8, ooRexx=5.0)
 */
/*
 * The opaque slot argument is in effect an Object [] with:
 *    - [0] object: RexxAndJava
 *    - [1] object: RexxConfiguration
 *    - [2] object: pointer to context object
 *    - [3] String: "0"=ThreadContext, "1"=MethodContext, "2"=CallContext, "3"=ExitContext
 *    for redirecting command handlers in addition
 *    - [4] object: pointer to RexxIO context object
 *    - [5] object: BitSet with the invocation's redirection flags
*/
public interface RexxRedirectingCommandHandler extends RexxCommandHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "2.20220819";

    // define constants for indexing the BitSet
    final static int IS_REDIRECTION_REQUESTED         =  0;
    final static int IS_INPUT_REDIRECTED              =  1;
    final static int IS_OUTPUT_REDIRECTED             =  2;
    final static int IS_ERROR_REDIRECTED              =  3;
    final static int ARE_OUTPUT_AND_ERROR_SAME_TARGET =  4;


    /** Indicates that the command handler is redirectable (a new feature
     *  in ooRexx 5.0).
     *
     * @return returns true
     */
    default boolean  isRedirectable()
    {
        return true;
    }

    /** Determines whether this invocation employs redirection.
     *
     * @return <code>true</code> if redirection is in effect, <code>false</code> else
     */
    default boolean isRedirectionRequested(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4)  // invocation is not for a redirected handler, hence return false
        {
            return false;
        }
        return ((BitSet) arrSlot[5] ).get(IS_REDIRECTION_REQUESTED);
    }

    /** Determines whether this invocation redirects input.
     *
     * @return <code>true</code> if input redirection is in effect, <code>false</code> else
     */
    default boolean isInputRedirected(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4)  // invocation is not for a redirected handler, hence return false
        {
            return false;
        }
        return ((BitSet) arrSlot[5] ).get(IS_INPUT_REDIRECTED);
    }

    /** Determines whether this invocation redirects output.
     *
     * @return <code>true</code> if output redirection is in effect, <code>false</code> else
     */
    default boolean isOutputRedirected(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4)  // invocation is not for a redirected handler, hence return false
        {
            return false;
        }
        return ((BitSet) arrSlot[5] ).get(IS_OUTPUT_REDIRECTED);
    }

    /** Determines whether this invocation redirects error.
     *
     * @return <code>true</code> if error redirection is in effect, <code>false</code> else
     */
    default boolean isErrorRedirected(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4)  // invocation is not for a redirected handler, hence return false
        {
            return false;
        }
        return ((BitSet) arrSlot[5] ).get(IS_ERROR_REDIRECTED);
    }

    /** Determines whether output and error are the same target.
     *
     * @return <code>true</code> if output and error are the same target, <code>false</code> else
     */
    default boolean areOutputAndErrorSameTarget(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4)  // invocation is not for a redirected handler, hence return false
        {
            return false;
        }
        return ((BitSet) arrSlot[5] ).get(ARE_OUTPUT_AND_ERROR_SAME_TARGET);
    }


// ===========================================================================
// --- from here on native methods of RexxAndJava object get employed if in redirection mode and redirected

    /** Reads the next line from input.
     *
     * @return the line read from input or null if nothing could be read
     */
    default String readInput(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isInputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return null;
        }

/*
        // 0=readInput, 1=readInputBuffer
        return ((RexxAndJava) arrSlot[0]).jniRCH_Read(arrSlot, 0);
*/

        return "n/a ";
    }

    /** Reads the next line from input, does not apply a codepage translation to Java but
     *  returns the data as a byte array.
     *
     * @return the line read from input or null if nothing could be read
     */
    default byte[] readInputAsByteArray(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isInputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return null;
        }
/*
        return ((RexxAndJava) arrSlot[0]).jniRCH_ReadAsByteArray(arrSlot);
*/
        return "n/a".getBytes();
    }

    /** Reads all remaining items from input separated by the platform specific line-end character(s).
     *
     * @return all remaining items from input separated by the platform
     *         specific line-end character(s) or null if nothing could be read
     */
    default String readInputBuffer(Object slot)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isInputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return null;
        }

/*
        // 0=readInput, 1=readInputBuffer
        return ((RexxAndJava) arrSlot[0]).jniRCH_Read(arrSlot,1);
*/
        return "n/a";
    }

    /** Writes a single line of String data.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param line the String data to write to output
     */
    default void writeOutput(Object slot, String line)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isOutputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
        // 0=writeOutput, 1=writeOutputBuffer, 2=writeError, 3=writeErrorBuffer
        ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, 0, line);
*/
    }

    /** Writes all byte[] data as a single line, does not apply a codepage translation.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param data the byte array to write to output
     */
    default void writeOutput(Object slot, byte[] data)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isOutputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
        // byte[] data
        ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, flag=0, byte[] data);
*/
    }

    /** Writes the supplied string data, ooRexx will split it into individual lines
     *  at CR-LF boundaries.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param data the String to write to output
     */
    default void writeOutputBuffer(Object slot, String data)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isOutputRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
    // 0=writeOutput, 1=writeOutputBuffer, 2=writeError, 3=writeErrorBuffer
        ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, 1, data);
*/
    }

    // 0=writeOutput, 1=writeOutputBuffer, 2=writeError, 3=writeErrorBuffer
    /** Writes a single line of String data.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param line the String data to write to error
     */
    default void writeError(Object slot, String line)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isErrorRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
        // 0=writeError, 1=writeErrorBuffer
        return ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, 2, line);
*/
    }

    /** Writes all byte[] data as a single line, does not apply a codepage translation.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param data the byte array to write to error
     */
    default void writeError(Object slot, byte[] data)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isErrorRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
        // byte[] data
        return ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, 2, data);
*/
    }

    /** Writes all String data.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param data the String to write to error
     */
    default void writeErrorBuffer(Object slot, String data)
    {
        Object arrSlot[] =(Object []) slot;
        if (arrSlot.length<=4 || !isErrorRedirected(slot))  // invocation is not for a redirected handler, hence return false
        {
            return;
        }
/*
    // 0=writeOutput, 1=writeOutputBuffer, 2=writeError, 3=writeErrorBuffer
        return ((RexxAndJava) arrSlot[0]).jniRCH_Write(arrSlot, 3, data);
*/
    }

    /** Create a string rendering of this environment handler with current settings.
     *
     * @param slot supply the received slot argument
     * @param environmentName supply the environment name this handler handles
     * @return the string rendering of this instance
     */
    default String toString (Object slot, String environmentName)
    {
        StringBuffer sb=new StringBuffer();

        sb.append(this.getClass().getName());
        sb.append('[');
        sb.append("environmentName="           ).append(environmentName      )            .append(',');
        sb.append("isRedirectable="            ).append(this.isRedirectable())            .append(',');
        sb.append("isRedirectionRequested="    ).append(this.isRedirectionRequested(slot)).append(',');
        sb.append("isInputRedirected="         ).append(this.isInputRedirected     (slot)).append(',');
        sb.append("isOutputRedirected="        ).append(this.isOutputRedirected    (slot)).append(',');
        sb.append("isErrorRedirected="         ).append(this.isErrorRedirected     (slot)).append(',');
        sb.append("areOutputAndErrorSameTarget=").append(this.areOutputAndErrorSameTarget(slot));
        sb.append(']');
        return sb.toString();
    }
}
