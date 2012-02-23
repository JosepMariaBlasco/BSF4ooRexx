package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ...

import org.apache.bsf.*;   // BSF support
// import java.io.*;       // exception handling
import java.util.*;         // BitSet

/**
 * This class allows to configure a Rexx interpreter instance, before it gets created employing
 *  {@link RexxAndJava#createRexxInterpreterInstance(RexxConfiguration rc) RexxAndJava.createRexxInterpreterInstance(RexxConfiguration rc)}.
 * Cf. ooRexx' <code>rexxpg.pdf</code> documentation for the Rexx interpreter creation options.
 *
 * All final static fields that use ooRexx names also use the ooRexx constant values from
 * <code>rexxapidefs.h</code> and <code>oorexxapi.h</code>.
 *
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
 * @version 0.01
 * @since 2012-02-04
 */

/* 2012-02-09, rgf: - ArrayList needs to be initialized, such that size() matches the number of exits
 */

public class RexxConfiguration {
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change. */
    final static public String version = "1.20120209";


    // ------------------------------------------------------------------------------------
    /** Allows to have Rexx lookup additional paths, default for this field is <code>null</code>. */
    private String external_call_path=null;

    /** Allows to set additional directories for Rexx to lookup called Rexx programs.
     *  Please note: on Windows the delimiter character is a semicolon (;), on Unix the colon (:).
     *  <code>null</code> resets the external call path.
    */
    public void setExternalCallPath(String externalCallPath) throws BSFException
    {
        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not change value");
        }
        this.external_call_path=externalCallPath;
    }

    /** Returns the current value of the externalCallPath field.
     * @return the current value of the externalCallPath field
     */
    public String getExternalCallPath()
    {
        return this.external_call_path;
    }

    // ------------------------------------------------------------------------------------
    /** Allows to have Rexx search additional file extensions,
        default for this field is <code>&quot;.rxj,.rxo,.rxjo,.jrexx&quot;</code>.
        */
    private String external_call_extensions=".rxj,.rxo,.rxjo,.jrexx";

    /** Allows to set additional directories for Rexx to lookup. This field is preset to
        <code>&quot;.rxj,.rxo,.rxjo,.jrexx&quot;</code>.
     *  <code>null</code> resets the external call extension.
    */
    public void setExternalCallExtensions(String externalCallExtensions) throws BSFException
    {
        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not change value");
        }
        this.external_call_extensions=externalCallExtensions;
    }

    /** Returns the current value of the externalCallExtensions field.
     * @return the current value of the externalCallExtensions field
     *
     */
    public String getExternalCallExtensions()
    {
        return this.external_call_extensions;
    }

    // ------------------------------------------------------------------------------------
    /** Holds native library names that should get required when the Rexx interpreter instance gets created. */
    private ArrayList load_required_library = new ArrayList();

    /** Allows to add a required native (implemented in C++) library that should be loaded when the Rexx interpreter instance
     *  gets created.
     *
     *  @param libraryName the name of the native required library as used in an ooRexx &quot;::REQUIRES&quot; directive
    */
    public void addRequiredLibrary(String libraryName) throws BSFException
    {
        if (libraryName==null)
        {
            throw new BSFException("Required library name must not be null!");
        }

        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not change value");
        }

        load_required_library.add(libraryName);
    }

    /** Returns an array of native library names.
     *
     * @return array of type String containing all required native library names that should get loaded
     *         with the creation of the Rexx interpreter instance
     */
    public String [] getRequiredLibrary()
    {
        return (String[]) load_required_library.toArray();
    }


    /** Returns an array of native library names or <code>null</code>, if none were defined.
     *
     * @return array of type String containing all required native library names that should get loaded
     *         with the creation of the Rexx interpreter instance or <code>null</code>, if none defined
     */
    public String [] getRequiredLibrary4JNI()
    {
        if (load_required_library.size()==0)    // if none defined, return null
        {
            return null;
        }
        return getRequiredLibrary();
    }



    // ------------------------------------------------------------------------------------
    /** Define valid system exits (as of ooRexx 4.1) exit numbers. */
    private static BitSet validExits;

    static {
        // defined Rexx exits as of ooRexx 4.1
        validExits=new BitSet(RexxExitHandler.RXNOOFEXITS);     // create BitSet
        validExits.set(RexxExitHandler.RXFNC);
        validExits.set(RexxExitHandler.RXCMD);
        validExits.set(RexxExitHandler.RXMSQ);
        validExits.set(RexxExitHandler.RXSIO);
        validExits.set(RexxExitHandler.RXHLT);
        validExits.set(RexxExitHandler.RXTRC);
        validExits.set(RexxExitHandler.RXINI);
        validExits.set(RexxExitHandler.RXTER);
        validExits.set(RexxExitHandler.RXEXF);
        validExits.set(RexxExitHandler.RXNOVAL);
        validExits.set(RexxExitHandler.RXVALUE);
        validExits.set(RexxExitHandler.RXOFNC);
    }


    /** Stores RexxExitHandler objects, if any defined. Index is the Rexx exit number. */
    private ArrayList exitHandlers       =new ArrayList(RexxExitHandler.RXNOOFEXITS);


    public RexxConfiguration()
    {
            // add null-elements to ArrayList, such that its size() matches the maximum number of items
        for (int i=0;i<RexxExitHandler.RXNOOFEXITS;i++)
        {
            exitHandlers.add(i,null);
        }
    }

    /** Returns an array with two elements, where the first element
     *  is an int array of exit numbers and the second element is an array of
     *  RexxExitHandler with the matching Java exit handler object (or <code>null</code>, if
     *  not yet handled by the Java side).
     *
     * @return array of Object where at index <code>[0]</code> an <code>int[]</code> and
     *                               at index <code>[1]</code> a <code>RexxExitHandler[]</code>
     *                               array object gets returned
    */
    public Object [] getExitHandlers()
    {
        int nrDefined=definedExits.cardinality();   // number of exit handlers defined
        int             exitNr[]=new int [nrDefined];
        RexxExitHandler eh[]    =new RexxExitHandler[nrDefined];
        Object tmpExitHandlers[]={exitNr, eh};

        int idx=0;      // determines index for exitNr[] and eh[]
        for(int i=definedExits.nextSetBit(0); i>=0; i=definedExits.nextSetBit(i+1))
        {
            exitNr[idx]=i;                      // save exit number
            eh    [idx]=(RexxExitHandler) exitHandlers.get(i);  // save RexxExitHandler
            idx++;
        }

        return tmpExitHandlers;
    }

    /** Returns an array with two elements, where the first element
     *  is an int array of exit numbers and the second element is an array of
     *  RexxExitHandler with the matching Java exit handler object (or <code>null</code>, if
     *  not yet handled by the Java side). If no exit handlers got defined, then <code>null</code>
     *  gets returned.
     *
     * @return int array containing the exit numbers or null, if no exit handlers should be defined
    */
    public int [] getExitHandlers4JNI()
    {
        int nrDefined=definedExits.cardinality();   // number of exit handlers defined
        if (nrDefined==0)   // no exit handlers defined, return null
        {
            return null;
        }

        int exitNr[]=new int [nrDefined];           // create array of matching size

        int idx=0;      // determines index for exitNr[] and eh[]
        for(int i=definedExits.nextSetBit(0); i>=0; i=definedExits.nextSetBit(i+1))
        {
            exitNr[idx]=i;                      // save exit number
            idx++;
        }
        return exitNr;                          // return exit handler array
    }


    /** Stores information about exit functions for which RexxExitHandlers got defined. */
    private BitSet definedExits=new BitSet(RexxExitHandler.RXNOOFEXITS);     // create BitSet


    /** Returns a cloned BitSet object indicating which Rexx exits got defined to be handled.
     *
     * @return a cloned BitSet object
    */
    public BitSet getDefinedExits()
    {
        return (BitSet) definedExits.clone();
    }


    // ------------------------------------------------------------------------------------
    /** Stores RexxCommandHandler objects, index is address name, value is RexxCommandHandler object. */
    private HashMap commandHandlers=new HashMap();

    /** Returns an array with two elements, where the first element
     *  is a String array denoting the environment name and the second element is an array of type
     *  RexxCommandHandler with the matching Java command handler object.
     *
     * @return array of Object where at index <code>[0]</code> an <code>int[]</code> and
     *                               at index <code>[1]</code> a <code>RexxCommandHandler[]</code>
     *                               array object gets returned
    */
    public Object[] getCommandHandlers()
    {
        int nrDefined=commandHandlers.size();   // number of command handlers defined
        String            name[]   =new String            [nrDefined];
        RexxCommandHandler rch[]   =new RexxCommandHandler[nrDefined];
        Object tmpCommandHandlers[]={name,rch};

        int idx=0;      // determines index for name[] and rch[]
        Iterator it = commandHandlers.keySet().iterator();

        while (it.hasNext())
        {
            Object key=it.next();
            name[idx]=(String) key;
            rch [idx]=(RexxCommandHandler) commandHandlers.get(key);
            idx++;
        }
        return tmpCommandHandlers;
    }


    /** Returns an array with two elements, where the first element
     *  is a String array denoting the environment name and the second element is an array of type
     *  RexxCommandHandler with the matching Java command handler object.
     *  If no command handlers got defined, then <code>null</code> gets returned.
     *
     * @return a String array of the environment names or <code>null</code> if no
     *         RexxCommandHandlers got defined
    */
    public String [] getCommandHandlerNames4JNI()
    {
        int nrDefined=commandHandlers.size();   // number of command handlers defined
        if (nrDefined==0)       // no command handlers defined, return null
        {
            return null;
        }

        String   names[]=new String [nrDefined];

        int idx=0;      // determines index for name[] and rch[]
        Iterator it = commandHandlers.keySet().iterator();

        while (it.hasNext())
        {
            Object key=it.next();
            names[idx]=(String) key;
            idx++;
        }

        return names;
    }



    // ------------------------------------------------------------------------------------
    /** If set, determines the name of the default Rexx address environment. */
    protected String initial_address_environment=null;

    /** Allows to set the default Rexx address environment.
     *  <code>null</code> resets the external call path.
     *
     * @param name the name of the environment that Rexx should address by default, if <code>null</code>,
     *        then Rexx' default address environment (<code>CMD</code>) is used
    */
    public void setInitialAddressEnvironment(String name) throws BSFException
    {
        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not change value");
        }
        initial_address_environment=name;
    }

    /** Returns the default address environment or <code>null</code>, if none set.
     *
     * @return the default address environment or <code>null</code>, if none set
     */
    public String getInitialAddressEnvironment()
    {
        return initial_address_environment;
    }


    // ------------------------------------------------------------------------------------
    /** If flag is false, then the configuration cannot be changed anymore. It is possible however,
     *  to nullify handler objects, by using the respective methods
     *  native-code TODO: setExitHandler(),
     *  native-code TODO: setCommandHandler().
     */
    protected boolean modifiable=true;

    /** Access method for field {@link #modifiable}
     *
     * @return current setting of field {@link #modifiable}
    */
    public boolean isModifiable()
    {
        return modifiable;
    }

    // TODO: fields for rexxInterface and rii !


    // ------------------------------------------------------------------------------------
    /** Allows to define a {@link RexxExitHandler} to serve the given Rexx exit. The
     *  {@link RexxExitHandler} object may be null, which causes the Rexx exit handling
     *  to be switched on for this Rexx interpreter instance; in this case it becomes
     *  possible at runtime to change to a RexxExitHandler object using
     *  {@link RexxConfiguration#setExitHandler(int function, RexxExitHandler exitHandler)}.
     *
     * @param function the Rexx exit function number
     * @param exitHandler a {@link RexxExitHandler} object
    */
    public void addExitHandler(int function, RexxExitHandler exitHandler) throws BSFException
    {
        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not add new exit handlers. To change a RexxExitHandler object use 'setExitHandler()' instead.");
        }

        if (validExits.get(function)==false)    // test whether a valid exit function
        {
            throw new BSFException("Invalid Rexx exit function number ["+function+"]");
        }

        exitHandlers.add(function,exitHandler); // save exit handler wit
        definedExits.set(function);             // remember this
    }


    /** Allows to replace a defined {@link RexxExitHandler} with a new one or to remove it.
     *
     * @param function the Rexx exit function number
     * @param exitHandler a RexxExitHandler object or <code>null</code>, if the exit should not be served anymore
    */
    public RexxExitHandler setExitHandler(int function, RexxExitHandler exitHandler) throws BSFException
    {

        if (validExits.get(function)==false)    // test whether a valid exit function
        {
            throw new BSFException("Invalid Rexx exit function number ["+function+"]");
        }

        if (definedExits.get(function)==false)  // if exit was not defined, raise an error
        {
            throw new BSFException("No exit handling defined for exit function number ["+function+"]");
        }

        RexxExitHandler old=null;
        synchronized (this) {   // "critical section"
            old=(RexxExitHandler) exitHandlers.get(function);
            exitHandlers.set(function,exitHandler); // save exit handler wit
        }

        return old;
    }

    public RexxExitHandler getExitHandler(int function)
    {
        if (validExits.get(function)==false)    // test whether a valid exit function
        {
            return null;
        }

        if (definedExits.get(function)==false)  // if exit was not defined, raise an error
        {
            return null;
        }

        RexxExitHandler reh=null;
        synchronized (this) {   // "critical section"
            reh=(RexxExitHandler) exitHandlers.get(function);
        }

        return reh;
    }


    // ------------------------------------------------------------------------------------
    /** Allows to define an environment name and a {@link RexxCommandHandler} to serve the given environment.
     *
     * @param name the Rexx environment name
     * @param commandHandler a RexxCommandHandler object
    */
    public void addCommandHandler(String name, RexxCommandHandler commandHandler) throws BSFException
    {
        if (modifiable==false)
        {
            throw new BSFException("RexxConfiguration already deployed, must not add new command handlers. To change a RexxCommandHandler object use 'setCommandHandler()' instead.");
        }

        if (name==null)
        {
            throw new BSFException("Rexx environment name must not be null!");
        }

        if (commandHandler==null)
        {
            throw new BSFException("RexxCommandHandler must not be null!");
        }

        commandHandlers.put(name,commandHandler); // save exit handler wit
    }

    /** Allows to replace a defined {@link RexxCommandHandler} with a new one.
     *
     * @param name the Rexx environment name
     * @param commandHandler a RexxCommandHandler object
     * @return the previous stored RexxCommandHandler object
    */
    public RexxCommandHandler setCommandHandler(String name, RexxCommandHandler commandHandler) throws BSFException
    {
        if (name==null)
        {
            throw new BSFException("Rexx environment name must not be null!");
        }

        if (commandHandlers.containsKey(name)==false)    // test whether a valid command environment
        {
            throw new BSFException("Rexx environment name ["+name+"] was not defined");
        }

        if (commandHandler==null)
        {
            throw new BSFException("RexxCommandHandler must not be null!");
        }
        RexxCommandHandler old=null;
        synchronized (this) {  // "critical section"
            old=(RexxCommandHandler) commandHandlers.get(name);
            commandHandlers.put(name,commandHandler); // save exit handler with
        }

        return old;
    }


    /** Returns the {@link RexxCommandHandler} object associated with the supplied command
     *  <code>name</code> or <code>null</code>, if none found. This will be used from native
     *  code as well.
     *
     *  @param name a String denoting the name of the command environment
     *  @return the {@link RexxCommandHandler} object or <code>null</code> if none found
     */
    public RexxCommandHandler getCommandHandler(String name)
    {
        if (name==null)
        {
            return null;
        }

        RexxCommandHandler rch=null;
        synchronized (this) {  // "critical section"
            rch=(RexxCommandHandler) commandHandlers.get(name);
        }

        return rch;
    }


    /** Returns an array of type Object that contains the following options:
     *  <ul>
     *      <li>index <code>0</code>: <code>INITIAL_ADDRESS_ENVIRONMENT</code> (null or String)
     *      <li>index <code>1</code>: <code>EXTERNAL_CALL_PATH</code> (null or String)
     *      <li>index <code>2</code>: <code>EXTERNAL_CALL_EXTENSIONS</code> (null or String)
     *      <li>index <code>3</code>: <code>LOAD_REQUIRED_LIBRARY</code> (null or String[])
     *      <li>index <code>4</code>: <code>DIRECT_EXITS</code> (null or Object[]={int[],RexxExitHandler[]})
     *      <li>index <code>5</code>: <code>DIRECT_ENVIRONMENTS</code> (null or Object[]={String[],RexxCommandHandler[]})
     *  </ul>
    */
    public Object[] getArguments4JNI()
    {
        Object args4jni [] =
            {
                this,                               // [0}: this RexxConfiguration object
                initial_address_environment,        // [1]: INITIAL_ADDRESS_ENVIRONMENT (null or String)
                external_call_path,                 // [2]: EXTERNAL_CALL_PATH (null or String)
                external_call_extensions,           // [3]: EXTERNAL_CALL_EXTENSIONS (null or String)
                getRequiredLibrary4JNI(),           // [4]: LOAD_REQUIRED_LIBRARY  (null or String[])
                getExitHandlers4JNI(),              // [5]: DIRECT_EXITS  (null or int[] exits)
                getCommandHandlerNames4JNI()        // [6]: DIRECT_ENVIRONMENTS  (null or String[] commandNames)
            };
        return args4jni;
    }



    /** Returns a String representation of the configuration.
     *
     */
    public String toString ()
    {
        StringBuffer sb=new StringBuffer();

        // sb.append(RexxConfiguration.class.getName());
        sb.append(this.getClass().getName());
        sb.append('[');
        sb.append("initialAddressEnvironment=[").append(initial_address_environment).append(']');
        sb.append(',');
        sb.append("externalCallPath=[")     .append(external_call_path)     .append(']');
        sb.append(',');
        sb.append("externalCallExtensions=[").append(external_call_extensions).append(']');
        sb.append(',');

        sb.append("loadRequiredLibrary="); requiredLibraryToString(sb);
        sb.append(',');

        sb.append("exitHandlers=");        exitHandlersToString(sb);
        sb.append(',');

        sb.append("commandHandlers=");     commandHandlersToString(sb);
        sb.append(']');

        return sb.toString();
    }

    /** Create a String rendering of the required library ArrayList.
     *  @param sb StringBuffer to append to
     */
    private void requiredLibraryToString(StringBuffer sb)
    {
        boolean addComma=false;
        sb.append('{');
        for (int i=0; i<load_required_library.size(); i++)
        {
            if (addComma==true)
            {
                sb.append(',');
            }
            sb.append(load_required_library.get(i));
            addComma=true;
        }
        sb.append('}');
    }


    /** Create a String rendering of the command handler HashMap.
     *  @param sb StringBuffer to append to
     */
    private void commandHandlersToString(StringBuffer sb)
    {
        boolean addComma=false;
        sb.append('{');

        Iterator it = commandHandlers.keySet().iterator();

        while (it.hasNext())
        {
            if (addComma==true)
            {
                sb.append(',');
            }
            Object key=it.next();
            sb.append(key) .append('=') .append(commandHandlers.get(key));
            addComma=true;
        }
        sb.append('}');
    }


    /** Create a String rendering of the exit handler ArrayList.
     *  @param sb StringBuffer to append to
     */
    private void exitHandlersToString(StringBuffer sb)
    {
        boolean addComma=false;
        sb.append('{');

        for(int i=definedExits.nextSetBit(0); i>=0; i=definedExits.nextSetBit(i+1))
        {
            if (addComma==true)
            {
                sb.append(',');
            }
            sb.append(RexxExitHandler.RX_EXIT_NAMES[i]);
            sb.append('/').append(i).append('/');
            sb.append(exitHandlers.get(i));
            addComma=true;
        }

        sb.append('}');
    }
}



