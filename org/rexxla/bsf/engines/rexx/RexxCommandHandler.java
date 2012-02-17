package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...
/**

   Interface class for Rexx command handlers. Cf. ooRexx' <code>rexxpg.pdf</code> documentation.

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
public interface RexxCommandHandler
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "1.20120217";


    /** Command handler signature (called via native code) according to the ooRexx API
     *  documentation in <code>rexxpg.pdf</code>.
     *
     * @param slot this is an opaque <code>Object</code> argument which must be supplied
     *             unchanged to the direct Rexx API methods in {@link RexxHandler}
     * @param address a String denoting the name of the command environment for which this
     *                method got invoked
     * @param command a String containing the command passed from Rexx
     */
    public Object handleCommand(Object slot, String address, String command);

}
