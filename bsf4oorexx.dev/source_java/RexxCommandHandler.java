package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...
/**

   Interface class for Rexx command handlers. Cf. ooRexx'
   <code>rexxapi.pdf</code> documentation.

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
 * @version 2.0
 * @since 2012-02-17 (BSF4ooRexx 641, baseline Java=6, ooRexx=4.1),
          2022-08-18 (BSF4ooRexx 850, baseline Java=8, ooRexx=5.0)
 */
/*
 * The opaque slot argument is in effect an Object [] with:
 *    - [0] object: RexxAndJava
 *    - [1] object: RexxConfiguration
 *    - [2] object: pointer to context object
 *    - [3] String: "0"=ThreadContext, "1"=MethodContext, "2"=CallContext, "3"=ExitContext
*/
/*
    changed:    2022-08-16, adding default methods (new in Java 8) to interface
                            to indicate that this interface is not redirectable
                2022-08-22, make command handler extend RexxHandler (newly an interface)
                            to ease access to its static methods
*/
public interface RexxCommandHandler extends RexxHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "2.20220818";

    /** Indicates that the command handler is not redirectable (a new feature
     *  in ooRexx 5.0).
     *
     * @return returns false
     */
    default boolean  isRedirectable()
    {
        return false;
    }


    /** Command handler signature (called via native code) according to the ooRexx API
     *  documentation in <code>rexxpg.pdf</code>.
     *
     * @param slot this is an opaque <code>Object</code> argument which must be supplied
     *             unchanged to the direct Rexx API methods in {@link RexxHandler}
     * @param address a String denoting the name of the command environment for which this
     *                method got invoked
     * @param command a String containing the command passed from Rexx
     *
     * @return any value, which can be retrieved on the Rexx side via the variable <code>RC</code> (result code)
     */
    public Object handleCommand(Object slot, String address, String command);


    /** Create a string rendering of this environment handler.
     *
     * @param slot supply the received slot argument
     * @param environmentName supply the environment name this handler got defined for
     * @return the string rendering of this instance
     */
    default String toString (Object slot, String environmentName)
    {
        StringBuffer sb=new StringBuffer();

        sb.append(this.getClass().getName());
        sb.append('[');
        sb.append("environmentName=").append(environmentName).append(',');
        sb.append("isRedirectable=" ).append(this.isRedirectable());
        sb.append(']');
        return sb.toString();
    }
}
