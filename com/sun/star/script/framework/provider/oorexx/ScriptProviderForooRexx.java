/*************************************************************************
*
*  $RCSfile: ScriptProviderForooRexx.java,v $
*
*  $Revision: 572 $
*
*  last change: $Author: rony $ $Date: 2010-02-16 21:53:17 +0100 (Tue, 16 Feb 2010) $
*
*  The Contents of this file are made available subject to the terms of
*  either of the following licenses
*
*         - GNU Lesser General Public License Version 2.1
*         - Sun Industry Standards Source License Version 1.1
*
*  Sun Microsystems Inc., October, 2000
*
*  GNU Lesser General Public License Version 2.1
*  =============================================
*  Copyright 2000 by Sun Microsystems, Inc.
*  901 San Antonio Road, Palo Alto, CA 94303, USA
*
*  This library is free software; you can redistribute it and/or
*  modify it under the terms of the GNU Lesser General Public
*  License version 2.1, as published by the Free Software Foundation.
*
*  This library is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*  Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public
*  License along with this library; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place, Suite 330, Boston,
*  MA  02111-1307  USA
*
*
*  Sun Industry Standards Source License Version 1.1
*  =================================================
*  The contents of this file are subject to the Sun Industry Standards
*  Source License Version 1.1 (the "License"); You may not use this file
*  except in compliance with the License. You may obtain a copy of the
*  License at http://www.openoffice.org/license.html.
*
*  Software provided under this License is provided on an "AS IS" basis,
*  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
*  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
*  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
*  See the License for the specific provisions governing your rights and
*  obligations concerning the Software.
*
*  The Initial Developer of the Original Code is: Sun Microsystems, Inc.
*
*  Copyright: 2000 by Sun Microsystems, Inc.
*
*  All Rights Reserved.
*
*  Contributor(s): _______________________________________
*
*
************************************************************************/

/* changed: 2008-06-14, ---rgf, now supplying filename to ooRexx, added utiltiy
                        method "ScriptProviderForooRexx.makeFilenameLedgible()"

            2008-06-17, ---rgf, now supplying the fully qualified file name in the
                                operating system dependent form

            2008-06-28, ---rgf,
                        - getSystemPathFromFileURL(): now taking care of scripts that
                          are part of an extension package placed (unpacked) in a cache
                          directory
                        - using metaData's "getDescription" for determining the physical file-name
                          of the script; this is because of researching parcel-descriptor's DTD

            2008-07-21, ---rgf
                        - using metaData's "getLogicalName" instead of "getDescription",
                          because of <http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/Scripting/Writing_Macros#Compiling_and_Deploying_Java_macros>

            2008-08-09, ---rgf
                        - adapting code to cater for the signature change in OOo 300: now
                          ScriptContext.createContext(...) is dynamically executed, such that
                          it can cater for OOo 2.x *and* 3.x (in 3.x the signature has changed,
                          rendering it incompatible with earlier versions of OOo!)

            2008-08-12, ---rgf
                        - fixing a (probably ClassLoader) problem in the above introduced
                          dynamic code in ScriptImpl.makeScriptContext(): no explicit cast
                          to ScriptContext [will cause a cast problem, probably due to
                          different class loaders used in this program and
                          ScriptContext.createContext()]

            2009-02-06, ---rgf
                        - fixed an error in nr of arguments meant for Rexx (one too many,
                          related to original implementation of script's context UNO object)

            2009-10-30, ---rgf
                        - idea, prepare first steps and wait until ooRexx allows for registering
                          variable values upfront to store invocation related information there
                          (currently invocation arguments are passed via the BSFManager registry
                          which is not thread safe; e.g. two Rexx script invocations on separate
                          threads may interfere with each other; therefore do not activate the
                          new mode, but prepare the two methods for fetching/releasing the
                          cached BSFManager object)

                            - keep an instance of BSFManager in a static field and re-use it on every
                              Rexx invocation: this has become possible with ooRexx 4.0 and BSF4ooRexx:
                              - allows to reuse the required UNO.CLS (and its required BSF.CLS) on
                                subsequent Rexx script invocations

                                - allows to reuse the cached UNO class objects as well

                              - makes sure that RexxProxy objects are using the same Rexx interpreter
                                instance in which they got created and being able to resolve all
                                Java proxy objects referring to Java objects stored in the BSFRegistry

                            - add getBSFManager() method which returns the cached BSFManager object (and
                              creates and stores it, if not created yet)

                            - add terminateBSFManager() method which terminates the BSFManager and
                              resets the bsfManager field

            2009-10-31, ---rgf
                        - furthering the idea of 2009-10-30 above: now supplying adding a slot parameter
                          (dubbed "slotParam") as the very last argument of type Hashtable, which contains
                          all the invocation related OOo information: this information is unique per
                          invocation and should be used from now on. For compatibility reasons the entries
                          are also still added to the BSFRegistry (but note, from now on it could be the
                          case that concurrently invoked Rexx scripts could change the BSFRegistry values
                          and mix each other up)

                        - UNO.CLS has been adjusted to accept that slot parameter in the following related
                          public routines:

                              uno.getScriptContext([slotParam]),
                              uno.getScriptProviderVersion([slotParam]),
                              uno.getScriptFileName([slotParam]),
                              uno.getScriptMetaData([slotParam]),
                              uno.getScriptSourceModel([slotParam]),
                              uno.getScriptPath([scriptUri][,slotParam])

                          If "slotParam" (a Hashtable) is not supplied the BSFRegistry is looked up as
                          before.


*/

package com.sun.star.script.framework.provider.oorexx;

import org.apache.bsf.BSFManager;       // rgf, 2005-07-08
import org.rexxla.bsf.engines.rexx.RexxEngine;  // rgf, 2008-08-28, 2009-04-06
import org.apache.bsf.BSFException;     // rgf, 2009-10-31

// import com.sun.star.script.provider.xScriptURIHelper;   // rgf, 2008-06-17
// import com.sun.star.script.provider.XScriptURIHelper;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.util.XMacroExpander;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.lang.XTypeProvider;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XInitialization;
import com.sun.star.frame.XModel;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.Any;

import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.reflection.InvocationTargetException;

import com.sun.star.script.CannotConvertException;

import java.util.Hashtable;         // rgf, 2009-10-31
import java.lang.reflect.Method;    // rgf, 2008-08-09 (for OOo 3.0 support)
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map;
import java.io.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import com.sun.star.script.provider.XScriptContext;

import com.sun.star.script.provider.XScriptProvider;
import com.sun.star.script.provider.XScript;
import com.sun.star.script.provider.ScriptErrorRaisedException;
import com.sun.star.script.provider.ScriptExceptionRaisedException;
import com.sun.star.script.provider.ScriptFrameworkErrorException;
import com.sun.star.script.provider.ScriptFrameworkErrorType;


import com.sun.star.script.framework.provider.*;
import com.sun.star.script.framework.log.*;
import com.sun.star.script.framework.container.ScriptMetaData;


public class ScriptProviderForooRexx
{
    static final String OOREXX_LANGUAGE           = "ooRexx";    // define string for script language
    static final String OOREXX_BSFLANGUAGE        = "rexx";    // define string for script language

        // define string for extension of scripts
    static final String OOREXX_EXTENSION          = "rxo"; // rgf, 20091018;  "rex"; // "oos"; ---rgf, 2005-08-31

    // define strings to be used as keynames for entering the information into the BSF registry
        // define a prefix-string which should not be creatable by accident
    static final String OOREXX_BSFREGISTRY_PREFIX = "<BSFRegistry-kEy>";

    static final String OOREXX_XSCRIPTCONTEXT_KEY = "OOo.xScriptContext";
    static final String OOREXX_VERSION_KEY        = "OOo.ooRexxScriptProvider.version";
    static final String OOREXX_VERSION_VALUE      = "108.20091031";       // 20070921
    static final String OOREXX_FILENAME           = "OOo.fileName";       // 20070920
    static final String OOREXX_SCRIPTMETADATA     = "OOo.ScriptMetaData"; // 20070920

    static final String OOREXX_SOURCE_MODEL       = "OOo.ScriptSourceModel"; // 20080624

    // rgf, 2009-10-31
    static final String OOREXX_SCRIPT_PARAMS            = "OOo.ScriptParameters";
    static final String OOREXX_SCRIPT_OUT_PARAMS_INDEX  = "OOo.ScriptOutParametersIndex";
    static final String OOREXX_SCRIPT_OUT_PARAMS        = "OOo.ScriptOutParameters";



// ---->
    /* * A BSFManager object which can be (re-)used in ScriptProviderForooRexx.java
     *  and ScriptSourceModel.java.
     */
    static protected BSFManager bsfManager = null;  // 2009-10-30, rgf

    /* * Returns the BSFManager object stored in the {@link #bsfManager} field.
     *  If the BSFManager object has not been created yet,
     *  this method will first create and store it in the {@link #bsfManager} field.
     *
     * @return
     */
    // protected static BSFManager getBSFManager() throws BSFException     // 2009-10-30, rgf
    public static BSFManager getBSFManager() throws BSFException    // 2009-10-31, after releasing Beta 2
    {
        if (bsfManager==null)
        {
            bsfManager=new BSFManager();    // create a new manager
            // initialize the UNO support for this Rexx instance, such that any Rexx script thereafter
            // that requires UNO.CLS just re-uses it (exploiting the new requires-behaviour in ooRexx 4.0)
            bsfManager.apply(OOREXX_BSFLANGUAGE,
                                // source ("program name"):
                             "com.sun.star.script.framework.provider.oorexx.ScriptProviderForooRexx.requires_UNO.CLS.rex",
                             0, 0,
                             "::requires UNO.CLS",     // Rexx code to execute
                             null, null);
        }
        return bsfManager;
    }

    /* * Terminate the BSFManager (which will terminate all its engines) and reset the
     *  {@link #bsfManager} field to <code>null</code>.
     */
    // protected static void terminateBSFManager()
    public static void terminateBSFManager()        // 2009-10-31, after releasing Beta 2
    {
        if (bsfManager!=null)
        {
            bsfManager.terminate();
            bsfManager=null;
        }
    }
// <----

    /** Removes curly brackets at the beginning of the string and an optionally trailing
     * '/ucb/' string and returns result.
     *
     * @return string that contains the shortened (URL) path with file name of the script/macro
    */
    public static String makeFilenameLedgible (String filename)
    {
        String chunks[]=filename.split("}");    // split at closing curly bracket
        if (chunks.length>1)
        {
            filename=chunks[chunks.length-1];   // pick up chunk after last "}"
        }

        int pos=filename.indexOf("/ucb/");      // now remove trailing "/ucb/"
        if (pos>0)
        {
            return filename.substring(0,pos);   // extract portion without "/ucb/" and whatever follows
        }

        return filename;
    }


    /** Returns the fully qualified, operating system dependent path to the script, if it
     *  is located in the 'user' or 'share' location. Otherwise returns what
     *  'makeFilenameLedgible(String fileName)' returns.
     *
     * @return string fully qualified, operating system dependent path to the script in the
     *                'user' or 'share' location, otherwise a short form of the OOo URL-string
    */
    public static String getSystemPathFromFileURL (XComponentContext xContext, ScriptMetaData metaData)
                                            // throws com.sun.star.lang.IllegalArgumentException,java.net.MalformedURLException,com.sun.star.uno.Exception,java.io.IOException
    {
        String location=(String) metaData.getLocationPlaceHolder();

            try     // to get a physical path: if anything goes wrong in here, then instead show name according metaData's
            {
                    // get the XMultiComponentFactory service manager from the context
                XMultiComponentFactory xMCF = (XMultiComponentFactory) xContext.getServiceManager();

                XMacroExpander xME=(XMacroExpander) UnoRuntime.queryInterface(
                                    XMacroExpander.class,
                                    xContext.getValueByName("/singletons/com.sun.star.util.theMacroExpander"));

                String tmpStorage=metaData.getParcelLocation()+"/"+metaData.getLogicalName();
                tmpStorage=xME.expandMacros(tmpStorage.substring(tmpStorage.indexOf(':')+1));

                    // convertFromURL():
                XFileIdentifierConverter xFileConverter =
                    (XFileIdentifierConverter) UnoRuntime.queryInterface(
                                    XFileIdentifierConverter.class,
                                    xContext.getServiceManager().createInstanceWithContext(
                                    "com.sun.star.ucb.FileContentProvider", xContext));

                // make sure we produce and return a canonical file name (free of ".." etc.)
                return (new java.io.File(xFileConverter.getSystemPathFromFileURL(tmpStorage)))
                        .getCanonicalFile().toString();
            }
            catch (Exception e)
            {
                ;
            }

            // only get here, if an exception has occurred above, so location cannot be
            // turned into a canonical path for the used file system (e.g. neither "user"
            // nor "share"; so let's play it safe...
        String filename=null;

        try // try to get name from sourceURL
        {
            filename=(String) metaData.getSourceURL().toString();
        }
        catch (java.net.MalformedURLException e)     // oops, then let's use the following
        {
            filename=(String) metaData.getShortFormScriptURL();
        }


        int pos=filename.indexOf("/ucb/");      // now remove trailing "/ucb/"
        if (pos>0)
        {
            filename=filename.substring(0,pos);   // extract portion without "/ucb/" and whatever follows
        }

        return filename;        // prepends a location dependent vnd. string value
    }





    public static class _ScriptProviderForooRexx extends ScriptProvider
    {
        private static ScriptEditorForooRexx myScriptEditorForooRexx;

        public _ScriptProviderForooRexx(XComponentContext ctx)
        {
            super (ctx, ScriptProviderForooRexx.OOREXX_LANGUAGE);
        }

        public XScript getScript( /*IN*/String scriptURI )
            throws com.sun.star.uno.RuntimeException,
                   ScriptFrameworkErrorException
        {
            ScriptMetaData scriptData = null;
            try
            {
                scriptData = getScriptData( scriptURI );
                ScriptImpl script = new ScriptImpl( m_xContext, scriptData, m_xModel );
                return script;
            }
            catch ( com.sun.star.uno.RuntimeException re )
            {
                throw new ScriptFrameworkErrorException( "Failed to create script object: " + re.getMessage(),
                    null, scriptData.getLanguageName(), language, ScriptFrameworkErrorType.UNKNOWN );
            }
        }

        public boolean hasScriptEditor()
        {
            return true;
        }

        public ScriptEditor getScriptEditor()
        {
            return ScriptEditorForooRexx.getEditor();
        }
    }

    /**
     * Returns a factory for creating the service.
     * This method is called by the <code>JavaLoader</code>
     * <p>
     *
     * @param  implName      the name of the implementation for which a service is desired
     * @param  multiFactory  the service manager to be used if needed
     * @param  regKey        the registryKey
     * @return               returns a <code>XSingleServiceFactory</code> for creating
     *                          the component
     * @see                  com.sun.star.comp.loader.JavaLoader
     */
    public static XSingleServiceFactory __getServiceFactory( String implName,
            XMultiServiceFactory multiFactory,
            XRegistryKey regKey )
    {
        XSingleServiceFactory xSingleServiceFactory = null;

        if ( implName.equals( ScriptProviderForooRexx._ScriptProviderForooRexx.class.getName() ) )
        {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(
                ScriptProviderForooRexx._ScriptProviderForooRexx.class,
                "com.sun.star.script.ScriptProviderForooRexx",
                multiFactory,
                regKey );
        }

        return xSingleServiceFactory;
    }


    /**
     * Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>
     * <p>
     *
     * @param  regKey  the registryKey
     * @return         returns true if the operation succeeded
     * @see            com.sun.star.comp.loader.JavaLoader
     */
    public static boolean __writeRegistryServiceInfo( XRegistryKey regKey )
    {
        String impl = "com.sun.star.script.framework.provider.oorexx." +
            "ScriptProviderForooRexx$_ScriptProviderForooRexx";

        String service1 = "com.sun.star.script.provider." + "ScriptProvider";
        String service2 = "com.sun.star.script.provider." + "LanguageScriptProvider";
        String service3 = "com.sun.star.script.provider." + "ScriptProviderForooRexx";
        String service4 = "com.sun.star.script.browse."   + "BrowseNode";

        if ( FactoryHelper.writeRegistryServiceInfo(impl, service1, regKey) &&
             FactoryHelper.writeRegistryServiceInfo(impl, service2, regKey) &&
             FactoryHelper.writeRegistryServiceInfo(impl, service3, regKey) &&
             FactoryHelper.writeRegistryServiceInfo(impl, service4, regKey) )
        {
                return true;
        }
        return false;
    }

}   // end of ScriptProviderForooRexx

class ScriptImpl implements XScript
{

    // the following static fields (populated in the static anonymous block) is
    // used for caching purposes;
    private static Method mSC_createContext=null;   // "createContext" method object to use
    private static int    mSC_argc=0;               // argc=3 on pre OOo 3x, 4 else

    // get and save appropriate (OOo 2.x or 3.x) ScriptProvider.createContext(...) method object;
    // used in private makeScriptContext() method
    static {
            // needed for allowing provider to work on OOo 2.x and 3.x (which introduced a fourth argument in ScriptContext
        Method m[]=ScriptContext.class.getMethods();   // get all public methods
        for (int i=0; i<m.length; i++)  // iterate over methods look for "createContext"
        {
            if (m[i].getName()=="createContext")   // found!
            {
                // defined by the ScriptContext class?
                if (m[i].getDeclaringClass().getName().equals("com.sun.star.script.framework.provider.ScriptContext"))
                {
                    Class argTypes[]=m[i].getParameterTypes();
                    if (argTypes.length==3 ) // probably OOo 2.x version, check for sure
                    {
                        if (
                              argTypes[0].getName().equals("com.sun.star.frame.XModel") &&
                              argTypes[1].getName().equals("com.sun.star.uno.XComponentContext") &&
                              argTypes[2].getName().equals("com.sun.star.lang.XMultiComponentFactory")
                           )
                        {
                            mSC_argc=3;
                            mSC_createContext=m[i];
                            break;
                        }
                    }
                    else if (argTypes.length==4) // probably OOo 3.x version, check for sure
                    {
                        if (
                            argTypes[0].getName().equals("com.sun.star.frame.XModel") &&
                            argTypes[1].getName().equals("com.sun.star.document.XScriptInvocationContext") &&
                            argTypes[2].getName().equals("com.sun.star.uno.XComponentContext") &&
                            argTypes[3].getName().equals("com.sun.star.lang.XMultiComponentFactory")
                           )
                        {
                            mSC_argc=4;
                            mSC_createContext=m[i];
                            break;
                        }
                    }
                }
            }
        }
    }


    private ScriptMetaData metaData;
    private XComponentContext m_xContext;
    private XMultiComponentFactory m_xMultiComponentFactory;
    private XModel m_xModel;

    // starting with OOo 3.0, the following is available and of
    // type "com.sun.star.document.XScriptInvocationContext"; using "Object" to allow compilation
    // on pre 3.0 versions, where the XScriptInvocationContext class did not exist
    private Object m_xInvocContext;   // new for OOo 3.0
    // private ScriptContext m_xInvocContext;   // new for OOo 3.0

    ScriptImpl( XComponentContext ctx, ScriptMetaData metaData, XModel xModel ) throws com.sun.star.uno.RuntimeException
    {
        this.metaData = metaData;
        this.m_xContext = ctx;
        this.m_xModel = xModel;

        try
        {
            this.m_xMultiComponentFactory = m_xContext.getServiceManager();
        }
        catch ( Exception e )
        {
            LogUtils.DEBUG( LogUtils.getTrace( e ) );
            throw new com.sun.star.uno.RuntimeException(
                "Error constructing  ScriptImpl [oorexx]: "
                + e.getMessage() );
        }

        LogUtils.DEBUG("ScriptImpl [oorexx] script data = " + metaData );
    }

    // new for OOo 3.0, make sure that it can be compiled for pre OOo 3.0 versions
    ScriptImpl( XComponentContext ctx, ScriptMetaData metaData, XModel xModel, Object xInvocContext ) throws com.sun.star.uno.RuntimeException
    {
        this(ctx, metaData, xModel);        // let the existing constructor process this
        this.m_xInvocContext=xInvocContext; // if given, should be of type "com.sun.star.document.XScriptInvocationContext"
    }


        /**
         * This method will be used to execute a script/macro if the user chooses
         * &quot;Extras -&gt; Macro -&gt; execute&quot;.
         *
         *                          documentStorageID and document reference
         *                          for use in script name resolving
         *
         * @param aParams           All parameters; pure, out params are
         *                          undefined in sequence, i.e., the value
         *                          has to be ignored by the callee
         *
         * @param aOutParamsIndex   Out indices
         *
         * @param aOutParams        Out parameters
         *
         * @return                  The value returned from the function
         *                          being invoked
         *
         * @throws IllegalArgumentException If there is no matching script name
         *
         * @throws CannotConvertException   If args do not match or cannot
         *                                  be converted the those of the
         *                                  invokee
         *
         * @throws InvocationTargetException If the running script throws
         *                                   an exception this information
         *                                   is captured and rethrown as
         *                                   this exception type.
         */

        public Object invoke( /*IN*/  Object[]   aParams,
                              /*OUT*/  short[][] aOutParamsIndex,
                              /*OUT*/ Object[][] aOutParams )
            throws ScriptFrameworkErrorException,
                InvocationTargetException
        {
            Hashtable slotArgument=new Hashtable(); // rgf, 2009-10-31: slot for passing useful data

            slotArgument.put(ScriptProviderForooRexx.OOREXX_SCRIPT_PARAMS, aParams );

            // don't supply the out-parameters functionality
            // slotArgument.put(ScriptProviderForooRexx.OOREXX_SCRIPT_OUT_PARAMS_INDEX, aOutParamsIndex );
            // slotArgument.put(ScriptProviderForooRexx.OOREXX_SCRIPT_OUT_PARAMS      , aOutParams      );

            // Initialise the out parameters (otherwise a runtime error occurs) - not used at the moment
            aOutParamsIndex[0] = new short[0];
            aOutParams[0]      = new Object[0];

            ClassLoader cl = null;
            URL sourceUrl = null;
            try {
                cl = ClassLoaderFactory.getURLClassLoader( metaData );
                sourceUrl = metaData.getSourceURL();
            }
            catch ( java.net.MalformedURLException mfu )
            {
                // Framework error
                throw new ScriptFrameworkErrorException(
                    mfu.getMessage(), null,
                    metaData.getLanguageName(), metaData.getLanguage(),
                    ScriptFrameworkErrorType.UNKNOWN );
            }
            catch ( NoSuitableClassLoaderException nsc )
            {
                // Framework error
                throw new ScriptFrameworkErrorException(
                    nsc.getMessage(), null,
                    metaData.getLanguageName(), metaData.getLanguage(),
                    ScriptFrameworkErrorType.UNKNOWN );
            }

            // Set class loader to be used for class files
            // and jar files
            Thread t = Thread.currentThread();           // 20070920 rgf
            ClassLoader clTmp=t.getContextClassLoader(); // 20070920 rgf: get current context CL
            // Thread.currentThread().setContextClassLoader(cl);
            t.setContextClassLoader(cl);                 // 20070920 rgf

            BSFManager mgr = null;                          // rgf, 2009-10-31
            // BSFManager mgr = new BSFManager();           // rgf, 2005-07-08
            // Vector     args= new Vector(aParams.length);    // rgf, 2005-07-08/2009-02-06
            Vector     args= new Vector(aParams.length+1);  // rgf, 2009-10-31
            // debug:
            // Vector     args= new Vector(aParams.length+1);    // rgf, 2005-07-08/2009-02-06


            try // create arguments for ooRexx
            {
                mgr = ScriptProviderForooRexx.getBSFManager();               // rgf, 2009-10-31, could throw an exception
                int i;

                // rgf, 2005-07-08 make XScriptContext available to script
                // args.addElement(ScriptContext.createContext(m_xModel, m_xContext, m_xMultiComponentFactory));

                // rgf, 2006-01-06, set entries into BSF registry for retrieval by invoked Rexx script (available in every Rexx scope)
                // mgr.registerBean(ScriptProviderForooRexx.OOREXX_XSCRIPTCONTEXT_KEY,  ScriptContext.createContext(m_xModel, m_xContext, m_xMultiComponentFactory));

/* */
                // XScriptContext tmpSC=makeScriptContext(); // reflective version of creating script context
                Object tmpSC=makeScriptContext(); // reflective version of creating script context
                mgr.registerBean( ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                                  ScriptProviderForooRexx.OOREXX_XSCRIPTCONTEXT_KEY,  tmpSC);
                slotArgument.put(ScriptProviderForooRexx.OOREXX_XSCRIPTCONTEXT_KEY,  tmpSC);

/* */
                mgr.registerBean( ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                                 ScriptProviderForooRexx.OOREXX_VERSION_KEY,         ScriptProviderForooRexx.OOREXX_VERSION_VALUE);
                slotArgument.put(ScriptProviderForooRexx.OOREXX_VERSION_KEY,         ScriptProviderForooRexx.OOREXX_VERSION_VALUE);

                // 20070920, rgf, add ScriptMetaData object, parcel filename
                mgr.registerBean( ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                                 ScriptProviderForooRexx.OOREXX_SCRIPTMETADATA, metaData);
                slotArgument.put(ScriptProviderForooRexx.OOREXX_SCRIPTMETADATA, metaData);

                Object oSourceUrl=sourceUrl.getFile();
                mgr.registerBean( ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                                 ScriptProviderForooRexx.OOREXX_FILENAME,       oSourceUrl);
                slotArgument.put(ScriptProviderForooRexx.OOREXX_FILENAME,       oSourceUrl);


                    // add arguments to Vector
                for (i=0;i<aParams.length;i++)
                {
                    // args.addElement(aParams[i]);    // add argument
                    args.add(i, aParams[i]);    // add argument, rgf, 2009-02-11
                }

                args.add(slotArgument);     // rgf, 2009-10-31, add slotArgument to the end of the argument list

                // debug:
                // args.add(i, "aParams.length=["+aParams.length+"], args.size()=["+args.size()+"], "+"args.capacity()=["+args.capacity()+"], i=["+i+"]"+"\n aParams[i-1].getClass()=["+aParams[i-1].getClass()+"]");
            }
            catch (Exception e) {
                t.setContextClassLoader(clTmp); // 20070920 rgf: re-set CL
                // Framework error setting up context
/*
                throw new ScriptFrameworkErrorException(
                    e.getMessage(), null,
                    metaData.getLanguageName(), metaData.getLanguage(),
                    ScriptFrameworkErrorType.UNKNOWN );
*/

                // rgf, 20091017: maybe toString() should be replaced by code assembling
                //                the full error message as reported by Rexx ?
                throw new ScriptFrameworkErrorException(
                    e.toString(), null,
                    metaData.getLanguageName(), metaData.getLanguage(),
                    ScriptFrameworkErrorType.UNKNOWN );

            }


            try {
                String source = null;
                Object result = null;

                ScriptEditorForooRexx editor =
                    ScriptEditorForooRexx.getEditor(sourceUrl );

                if ( editor != null )
                {
                    result = editor.execute();

                    t.setContextClassLoader(clTmp); // 20070920 rgf: re-set CL
                    if (result == null)
                    {
                        return new Any(new Type(), null);
                    }
                    return result;
                }

                metaData.loadSource();
                source = metaData.getSource();

                if ( source == null || source.length() == 0 )
                {
                        t.setContextClassLoader(clTmp); // 20070920 rgf: re-set CL
                	throw new ScriptFrameworkErrorException(
                        "Failed to read script", null,
                    	metaData.getLanguageName(), metaData.getLanguage(),
                    	ScriptFrameworkErrorType.UNKNOWN );
                }


                URL scriptSourceUrl=metaData.getSourceURL();    // get source URL from meta data

                // String filename=(scriptSourceUrl==null ?"OOo.ScriptProviderForooRexx-ScriptProviderForooRexx.invoke()" :ScriptProviderForooRexx.makeFilenameLedgible (scriptSourceUrl.getFile()));

                // rgf, 2008-06-17
                String filename=ScriptProviderForooRexx.getSystemPathFromFileURL(m_xContext,metaData);

                // RexxEngine re=(RexxEngine) mgr.loadScriptingEngine("rexx"); // get Rexx BSF scripting engine

                   // rgf, 2005-07-08
                result=mgr.apply(ScriptProviderForooRexx.OOREXX_BSFLANGUAGE,
                                 filename,
                                 // (sourceUrl==null ? "OOo.ScriptProviderForooRexx-ScriptProviderForooRexx.invoke()" : ScriptProviderForooRexx.makeFilenameLedgible (sourceUrl.getFile())+" ScriptProviderForooRexx"),
                                 // "OOo.ScriptProviderForooRexx",
                                 0, 0, source, null, args);

            // rgf, 2009-10-31    re.terminate();     // let ooRexx halt the main thread of this invocation

/*
                RexxEngine re=(RexxEngine) mgr.loadScriptingEngine("rexx"); // get Rexx BSF scripting engine
                // re.setThreadedRexxStart(true); // rgf, 2008-08-13: tell engine to invoke the Rexx interpreter on a separate Java thread
                result=re.apply(
                                filename,
                                // (sourceUrl==null ? "OOo.ScriptProviderForooRexx-ScriptProviderForooRexx.invoke()" : ScriptProviderForooRexx.makeFilenameLedgible (sourceUrl.getFile())+" ScriptProviderForooRexx"),
                                // "OOo.ScriptProviderForooRexx",
                                0, 0, source, null, args);
*/


                t.setContextClassLoader(clTmp); // 20070920 rgf: re-set CL
                if (result == null)
                {
                    return new Any(new Type(), null);
                }

                return result;
            }
            catch ( Exception e )
            {
                t.setContextClassLoader(clTmp); // 20070920 rgf: re-set CL
                throw new ScriptFrameworkErrorException(
                    // "Failed to read script", null,
                    e.getMessage(), null,
                    metaData.getLanguageName(), metaData.getLanguage(),
                    ScriptFrameworkErrorType.UNKNOWN );
            }
        }
        private void raiseEditor( int lineNum )
        {
            ScriptEditorForooRexx editor = null;
            try
            {
                URL sourceUrl = metaData.getSourceURL();
                editor = ScriptEditorForooRexx.getEditor( sourceUrl );
                if ( editor == null )
                {
                    editor = ScriptEditorForooRexx.getEditor();
                    editor.edit(
                        // ScriptContext.createContext(m_xModel, m_xContext, m_xMultiComponentFactory)
                        (ScriptContext) makeScriptContext()
                        , metaData );

                    editor = ScriptEditorForooRexx.getEditor( sourceUrl );
                }
                if ( editor != null )
                {
                    editor.indicateErrorLine( lineNum );
                }
            }
            catch( Exception ignore )
            {
            }
        }



        // rgf, 2008-08-09/12: use reflection for creating the ScriptContext object, reason:
        //          starting with OOo 3.0 the signature of ScriptContext.createContext()
        //          got changed; this solution allows to work on OOo 2.x as well
        // remark: note, that no casting to "ScriptContext" is carried out as this
        //         causes a cast runtime error (probably due to a ClassLoader mismatch in
        //         ScriptContext.createContext()
        private Object makeScriptContext() throws SecurityException,
                                             RuntimeException,
                                             java.lang.IllegalAccessException,
                                             java.lang.reflect.InvocationTargetException
        {
            Object tmpSC=null;              // ScriptContext object (but not typed to "ScriptContext"

            if (mSC_createContext==null)    // createContext() method object not assigned yet?
            {
                throw new com.sun.star.uno.RuntimeException(
                    "Error constructing ScriptContext object [oorexx]: neither OOo 2.x nor OOo 3.x matching ScriptContext.createContext(...) method object available.");
            }

            if (mSC_argc==3)    // running for a OOo 2.x version
            {
                Object args[]={m_xModel, m_xContext, m_xMultiComponentFactory};
                tmpSC=mSC_createContext.invoke(null, args);
            }
            else                // running for a OOo 3.x version, incorporate "m_xInvocContext"
            {
                Object args[]={m_xModel, m_xInvocContext, m_xContext, m_xMultiComponentFactory};
                tmpSC=mSC_createContext.invoke(null, args);
            }

            return tmpSC;   // return the created ScriptContext object
        }


        private static void showErrorMessage(String message) {
            javax.swing.JOptionPane.showMessageDialog(null, message,
                "Error", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }

}      // end of ScriptImpl
