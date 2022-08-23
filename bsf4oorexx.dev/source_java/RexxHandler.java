package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...
/**

   Abstract class to allow Rexx exit and command handlers implemented in Java to use
   some of the ooRexx exit APIs directly.

   <p><em>Please note:</em> these methods allow interfacing with the running instance
   of the Rexx interpreter instance on the <em>same (!) thread</em> in which the Rexx command
   or Rexx exit Java handler got invoked on.
   It is necessary to supply the opaque argument named <em>slot</em> from the Java handler
   methods <em>unaltered</em>.
   <br>
   If neither of these two restrictions holds, then the results of executing any of these
   static methods will be undefined, mostlikely tearing down ooRexx and Java!

   <p>

 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2012-2022 Rony G. Flatscher
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
 * @version 850.0
 * @since 2022-08-22
 */

/* changes: 2022-08-22, rgf - now that Java 8 is the base we take advantage of the
                              Java interface additions, therefore

                              - turning abstract class into an interface

                              - turning all static methods into default methods
                                such that Rexx[Redirecting]CommandHandler, RexxExitHandler
                                get direct access to them
*/

// public abstract class RexxHandler
public interface RexxHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "850.20220822";


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
    default void raiseCondition(Object slot, String conditionName, String description, Object[] additional, Object result)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniRaiseCondition(arrSlot, conditionName, description, additional, result);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber  <code>errorNumber*1000+subcode</code>
     * @param substitutions an Object array containing substitution values for the error message (if
     *        more entries get added, then they can be retrieved by ooRexx using the additional method
     *        of the condition object)
     *
    */
    default void raiseException(Object slot, long definedErrorNumber, Object[] substitutions)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniRaiseException(arrSlot, definedErrorNumber, substitutions);
    }

    /** Raises a Rexx error without substitution values of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber  <code>errorNumber*1000+subcode</code>
    */
    default void raiseException0(Object slot, long definedErrorNumber)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniRaiseException0(arrSlot, definedErrorNumber);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber  <code>errorNumber*1000+subcode</code>
     * @param substitution1 String used as the first substition value for the error message
    */
    default void raiseException1(Object slot, long definedErrorNumber, String substitution1)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniRaiseException1(arrSlot, definedErrorNumber, substitution1);
    }

    /** Raises a Rexx error of the given error number, call immediately before returning from
     * Java. Please note: the value of <code>definedErrorNumber</code> has to be calculated as
     *  <code>errorNumber*1000+subcode</code>. Cf. the ooRexx reference manual (<code>rexxref.pdf</code>),
     *  &quot;Appendix C. Error Numbers and Messages&quot;.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param definedErrorNumber  <code>errorNumber*1000+subcode</code>
     * @param substitution1 String used as the first substition value for the error message
     * @param substitution2 String used as the second substition value for the error message
    */
    default void raiseException2(Object slot, long definedErrorNumber, String substitution1, String substitution2)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniRaiseException2(arrSlot, definedErrorNumber, substitution1, substitution2);
    }



    /** Fetches and returns a Rexx directory containing all context variables.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return a @{link RexxProxy} representing a Rexx directory with the currently defined context variables
     */
    default Object  getAllContextVariables(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetAllContextVariables(arrSlot);
    }


    /** Fetches and returns the Rexx variable value in the Rexx program for which the handler runs.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param variableName name of the Rexx variable
     * @return value of the Rexx variable
     */
    default Object getContextVariable(Object slot, String variableName)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetContextVariable(arrSlot, variableName);
    }

    /** Allows to set a variable with a value in the Rexx program for which the handler runs.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param variableName name of the Rexx variable
     * @param variableValue the Rexx variable should be set to
     */
    default void setContextVariable(Object slot, String variableName, Object variableValue)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniSetContextVariable(arrSlot, variableName, variableValue);
    }


    /** Allows to set a variable to <code>.nil</code> in the Rexx program for which the handler runs.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param variableName name of the Rexx variable
     */
    default void setContextVariableToNil(Object slot, String variableName)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniSetContextVariableToNil(arrSlot, variableName);
    }


    /** Drops a Rexx variable from the context.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param variableName name of the Rexx variable
     */
    default void dropContextVariable(Object slot, String variableName)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniDropContextVariable(arrSlot, variableName);
    }




    /** Checks to see if any conditions have resulted from a call to a Rexx API (cf &quot;rexxpg.pdf&quot;).
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return <code>true</code>, if Rexx condition is set, <code>false</code> else
    */
    default boolean checkCondition(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniCheckCondition(arrSlot);
    }

    /** Clears any pending condition status (cf &quot;rexxpg.pdf&quot;).
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
    */
    default void clearCondition(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniClearCondition(arrSlot);
    }

    /** Returns a directory object containing the condition information; this is equivalent to calling
     *  <code>condition(&apos;O&apos;)</code> from within Rexx code (cf &quot;rexxpg.pdf&quot;).
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return returns a {@link RexxProxy} for a Rexx Directory object or <code>NULL</code>, if no
     *         pending Rexx condition
    */
    default Object getConditionInfo(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetConditionInfo(arrSlot);
    }


    /** Returns the Rexx context object <code>.context</code> that can be analyzed to learn about the context
     *  of the command/exit.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return returns a {@link RexxProxy} for the Rexx <code>.context</code>  object
    */
    default Object  getCallerContext(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetCallerContext(arrSlot);
    }


    /**  Sets the interactive trace state for the current thread.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @param state new state for interactive trace
     */
    default void  setThreadTrace(Object slot, boolean state)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniSetThreadTrace(arrSlot, state);
    }

    /**  Raises a <code>HALT</code> condition on the current (Rexx) thread.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     */
    default void  haltThread(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        ((RexxAndJava) arrSlot[0]).jniHaltThread(arrSlot);
    }


    /**   Returns a reference to this Rexx interpreter instance <code>.local</code> environment directory.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return a {@link RexxProxy} for the Rexx <code>.local</code> object
     */
    default Object getLocalEnvironment(Object slot)
    // static Object getLocalEnvironment(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetLocalEnvironment(arrSlot);
    }


    /**   Returns a reference to the Rexx interpreter global <code>.environment</code> directory.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return a {@link RexxProxy} for the Rexx <code>.environment</code> object
     */
    default Object getGlobalEnvironment(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniGetGlobalEnvironment(arrSlot);
    }


    // 2012-02-25, rgf
    /** Returns a reference to the Rexx sentinel value <code>.nil</code>.
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return a {@link RexxProxy} referring to the <code>.nil</code> object
     */
    default Object getNil(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniNil(arrSlot);
    }

    /** (Copied from the ooRexx rexxpg.pdf documentation) &quot;Returns
     *  the version of the interpreter. The returned version is
     * encoded in the 3 least significant bytes of the returned value,
     * using 1 byte each for the interpreter version, release, and revision
     * values. For example, on a 32-bit platform, this value would be
     * <code>0x00050000</code> for version 5.0.0.&quot;
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return the ooRexx interpreter version
     */
    default long getInterpreterVersion(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniInterpreterVersion(arrSlot);
    }


    /** (Copied from the ooRexx rexxpg.pdf documentation) &quot;Returns the language level of
     *  the interpreter. The returned language level is encoded in the 2 least
     *  significant bytes of the returned value, using 1 byte each for the
     *  interpreter version, release, and revision values. For example, on a 32-bit
     *  platform, this value would be <code>0x00000603</code> for language level 6.03.&quot;
     *
     * @param slot an opaque Object array as passed to the {@link RexxExitHandler} or {@link RexxCommandHandler} object
     * @return the ooRexx language level
     */
    default long getLanguageLevel(Object slot)
    {
        Object [] arrSlot=(Object[]) slot;
        return ((RexxAndJava) arrSlot[0]).jniLanguageLevel(arrSlot);
    }




}
