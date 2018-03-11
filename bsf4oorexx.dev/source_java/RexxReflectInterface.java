package org.rexxla.bsf.engines.rexx;

import org.apache.bsf.BSFException;
// import static org.rexxla.bsf.engines.rexx.RexxReflectUtil.ReflectionType.*;

import        java.lang.reflect.Modifier;

/**
 * This interface class defines the reflect protocol instrumentated by RexxAndJava.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2018 Rony G. Flatscher
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
 * @version 1.0, 2018-01-05
 * @since BSF4ooRexx 600 (implementation started: 2018-01-05)
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */


/** This interface class allows {@link RexxAndJava} to use Java reflection for different Java versions, if necessary.
 *  The baseline of BSF4ooRexx was set to Java 1.6/6 (released in December 2006 according to
 *  <a href="https://en.wikipedia.org/wiki/Java_(programming_language)#Versions">Wikipedia</a>) in 2017.
 *
 * <p>Here is an overview of the Java versions according to
 * <a href="https://en.wikipedia.org/wiki/Java_(programming_language)#Versions">Wikipedia</a>
 and what is considered relevant for the <em>BSF4ooRexx</em> implementation:
 *
 * <ul>
 * <li>Java 1.6, a.k.a. Java 6: released 2006-12-11;
 *      <a href="http://www.oracle.com/technetwork/articles/javase/beta2-135158.html">new features</a> relevant to <em>BSF4ooRexx</em>:
 *      <ul>
 *      <li> <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/scripting/index.html">Scripting for the Java Platform</a>
 *           (defined by <a href="http://jcp.org/en/jsr/detail?id=223">JSR-223</a>),
 *           package <code>javax.script</code>
 *      </ul>
 *
 * <li>Java 1.7, a.k.a. Java 7: released 2011-07-28
 *      <a href="http://www.oracle.com/technetwork/java/javase/jdk7-relnotes-418459.html">new features</a> relevant to <em>BSF4ooRexx</em>:
 *      <ul>
 *      <li> introduction of java.lang.invoke (MethodHandle, etc.); however cannot be used to fully replace core reflection
 *           invocations (java.lang.reflect)
 *      </ul>
 *
 * <li>Java 1.8, a.k.a. Java 8: released 2014-03-18;
 *      <a href="http://www.oracle.com/technetwork/java/javase/8-whats-new-2157071.html">new features</a> relevant to <em>BSF4ooRexx</em>:
 *      <ul>
 *      <li> <a href="https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html">Default and static methods in Java</a>
 *      </ul>
 *
 * <li>Java 9:                  released 2017-09-21;
 *      <a href="https://docs.oracle.com/javase/9/whatsnew/toc.htm#JSNEW-GUID-C23AFD78-C777-460B-8ACE-58BE5EA681F6">new features</a> relevant to <em>BSF4ooRexx</em>:
 *      <ul>
 *      <li> <a href="http://openjdk.java.net/projects/jigsaw/">New module system (project Jigsaw)</a>, which does not allow access
 *           via reflection to members of a closed module, although reading it is still possible
 *      </ul>
 *
 *
 * </ul>
 */
interface RexxReflectInterface
{
    /** Entry point to do carry out the reflection for locating appropriage {@link java.lang.Field}, {@link java.lang.Method} and
     *  {@link java.lang.Constructor} objects and invoke them.
     */
    void reflect(final RexxReflectUtil rru) throws BSFException;

    /** Returns the current setting, which affects the reflection operation on Java 9 or higher.
     *
     * @return <code>false</code>, if no restriction to a {@link MethodHandles.Lookup} <ocde>publicLookup</code> is in effect (cannot be
     *                reversed for a {@link RexxAndJava} instance, <code>true</code> else
     */
    boolean isPublicLookup();

    /** Will set the current {@link MethodHandles.Lookup} lookup object to the <ocde>publicLookup</code>
     *  object, thereby restricting all opeartions to the reference class <code>java.lang.Object</code>
     *  for the current {@link RexxAndJava} instance.
     *  This operation cannot be reverted for security reasons.
     */
    void    setPublicLookup();
}




