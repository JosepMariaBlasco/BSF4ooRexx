package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...
/**

   Interface class to allow typing Rexx handlers implemented in Java.

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
 * @since 2012-02-04
 */
public abstract class RexxHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "1.20120208";


    /** Raises a condition in the current Rexx thread, pass-through method to the Rexx API, which
     *  should be called immediately before returning to Rexx.
     *  Cf. the ooRexx 4.1 documentation in <code>rexxpg.pdf</code>, <em>9.14.126. RaiseCondition</em>.
     *
     * @param slot opaque argument supplied to the handler methods by the Rexx native
     *             code interface
     * @param conditionName  Rexx condition name, usually <code>&quot;ERROR&quot;</code> or <code>&quot;FAILURE&quot;</code>
     * @param description    optional String describing the condition
     * @param additional     optional additional object
     * @param result         optional result object, if routine or method does not trap the condition;
     *                       may contain the error or failure number
     */
    public static void raiseCondition(Object[] slot, String conditionName, String description, Object[] additional, Object result)
    {
        ((RexxAndJava) slot[0]).jniRaiseCondition(slot, conditionName, description, additional, result);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber:  <code>errorNumber*1000+subcode</code>
     * @param substitutions an Object array containing substitution values for the error message (if
     *        more entries get added, then they can be retrieved by ooRexx using the additional method
     *        of the condition object)
     *
    */
    public static void raiseException(Object[] slot, long definedErrorNumber, Object[] substitutions)
    {
        ((RexxAndJava) slot[0]).jniRaiseException(slot, definedErrorNumber, substitutions);
    }

    /** Raises a Rexx error without substitution values of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber:  <code>errorNumber*1000+subcode</code>
    */
    public static void raiseException0(Object[] slot, long definedErrorNumber)
    {
        ((RexxAndJava) slot[0]).jniRaiseException0(slot, definedErrorNumber);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber:  <code>errorNumber*1000+subcode</code>
     * @param substitution1 String used as the first substition value for the error message
    */
    public static void raiseException1(Object[] slot, long definedErrorNumber, String substitution1)
    {
        ((RexxAndJava) slot[0]).jniRaiseException1(slot, definedErrorNumber, substitution1);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber:  <code>errorNumber*1000+subcode</code>
     * @param substitution1 String used as the first substition value for the error message
     * @param substitution2 String used as the second substition value for the error message
    */
    public static void raiseException2(Object[] slot, long definedErrorNumber, String substitution1, String substitution2)
    {
        ((RexxAndJava) slot[0]).jniRaiseException2(slot, definedErrorNumber, substitution1, substitution2);
    }

    /** Fetches and returns the Rexx variable value in the Rexx program for which the handler runs.
     */
    public static Object getContextVariable(Object[] slot, String variableName)
    {
        return ((RexxAndJava) slot[0]).jniGetContextVariable(slot, variableName);
    }

    /** Allows to set a variable with a value in the Rexx program for which the handler runs.
     */
    public static void setContextVariable(Object[] slot, String variableName, Object variableValue)
    {
        ((RexxAndJava) slot[0]).jniSetContextVariable(slot, variableName, variableValue);
    }



}
