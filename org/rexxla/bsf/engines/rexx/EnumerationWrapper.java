package org.rexxla.bsf.engines.rexx;

import java.util.*;

/** Class to allow Rexx enumerating objects of classes implementing the Enumeration interface, if running under Java 1.1.
 *
 *  It turns out that on all Java versions (tested on Sun's Windows versions of: 1.1.8, 1.2.2,
 *  1.3.1_06, 1.4.1_01) &quot;<code>Method.invoke()</code>&quot; throws an &quot<code>IllegalAccessException</code>&quot;,
 *  if accessing public methods of inner classes. Java 1.5 does not allow identifiers to be named 'enum', hence renamed the
 *  variable.
 *
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2006 Rony G. Flatscher
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
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 * @version 1.2, 2006-01-01
 */
public class EnumerationWrapper implements Enumeration
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "102.20060101";


    /** Private object storing the object to enumerate. */
    private Object enumObj;

    // public EnumerationWrapper (Object enumObject)
    public EnumerationWrapper (Enumeration enumObject)
    {
        this.enumObj=enumObject;
    }

    // Enumeration interface implementation
    /** Implements the interface {@link java.util.Enumeration#hasMoreElements() java.util.Enumeration.hasMoreElements()}.
     */
    public boolean hasMoreElements()
    {
        return ((Enumeration) enumObj).hasMoreElements();
    }

    /** Implements the interface {@link java.util.Enumeration#nextElement() java.util.Enumeration.nextElement()}.
     *
     * @exception NoSuchElementException raised, if using the Enumerator interface and moving
     *                                   beyond the end of the supplier object.
     */
    public Object nextElement()
    {
        return ((Enumeration) enumObj).nextElement();
    }
}
