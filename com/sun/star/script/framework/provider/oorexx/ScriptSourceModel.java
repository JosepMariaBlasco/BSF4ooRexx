/*************************************************************************
*
*  $RCSfile: ScriptSourceModel.java,v $
*
*  $Revision: 559 $
*
*  last change: $Author: rony $ $Date: 2009-10-31 21:22:29 +0100 (Sat, 31 Oct 2009) $
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

/* changed: 2008-06-14, ---rgf, now using "ScriptProviderForooRexx.makeFilenameLedgible()",
                                now supplying filename to ooRexx

            2014-03-24, ---rgf
                        - fixed typo in method name "ScriptProviderForooRexx.makeFilenameLedgible()"
                          to "ScriptProviderForooRexx.makeFilenameLegible()" (removed 'd')
*/
package com.sun.star.script.framework.provider.oorexx;

import org.apache.bsf.BSFManager;       // rgf, 2005-07-08
import org.rexxla.bsf.engines.rexx.RexxEngine;  // rgf, 2008-08-28, 20090406

import java.util.Hashtable;         // rgf, 2009-10-31
import java.util.Vector;                // rgf, 2005-07-08

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;

import java.lang.reflect.InvocationTargetException;
import com.sun.star.script.provider.XScriptContext;
import com.sun.star.script.framework.container.ScriptMetaData; // 20070920


public class ScriptSourceModel {

    private int currentPosition = -1;
    private URL file = null;
    private ScriptSourceView view = null;

    public ScriptSourceModel(URL file ) {
        this.file = file;
    }

    private String load() throws IOException {
        StringBuffer buf = new StringBuffer();
        InputStream in = file.openStream();

        byte[] contents = new byte[1024];
        int len = 0;

        while ((len = in.read(contents, 0, 1024)) != -1) {
            buf.append(new String(contents, 0, len));
        }

        try {
            in.close();
        }
        catch (IOException ignore) {
        }

        return buf.toString();
    }

    public String getText() {
        String result = "";

        try {
            result = load();
        }
        catch (IOException ioe) {
            // do nothing, empty string will be returned
        }

        return result;
    }

    public int getCurrentPosition() {
        return this.currentPosition;
    }

    public void setView(ScriptSourceView view) {
        this.view = view;
    }

    // public Object execute(final XScriptContext context, ClassLoader cl )
    /**
     * This method will be used to execute a script/macro if the user chooses
     * &quot;Run&quot; from the editor window.
     */
    public Object execute(final XScriptContext xScriptContext, ClassLoader cl, ScriptMetaData metaData )
        throws Exception
    {
        Object result = null;

        Thread t=Thread.currentThread();    // 20070920
        ClassLoader clTmp=t.getContextClassLoader(); // 20070920 rgf: get current context CL
        t.setContextClassLoader(cl);        // 20070920, rgf: set to supplied CL

        try {
            // BSFManager mgr = new BSFManager();  // rgf, 2005-07-08
            BSFManager mgr = ScriptProviderForooRexx.getBSFManager();  // rgf, 2009-10-31

                // either directly as an argument, or pre-save it into BSF-registry
            // Vector args=new Vector();           // rgf, 2005-07-08
            // args.addElement(xScriptcontext);    // rgf, 2005-07-08 make XScriptContext available to script
                                                // rgf, 2005-07-08

            Hashtable <String,Object>slotArgument=new Hashtable <String,Object> (); // rgf, 2009-10-31: slot for passing useful data
            Vector <Object>args=new Vector <Object> ();               // rgf, 2009-10-31
            args.add(slotArgument);                 // add the slotArgument object to args Vector


            // rgf, 2006-01-06, set entries into BSF registry for retrieval by invoked Rexx script (available in every Rexx scope)
            mgr.registerBean(ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                             ScriptProviderForooRexx.OOREXX_XSCRIPTCONTEXT_KEY, xScriptContext);
            slotArgument.put(ScriptProviderForooRexx.OOREXX_XSCRIPTCONTEXT_KEY, xScriptContext);

            mgr.registerBean(ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                             ScriptProviderForooRexx.OOREXX_VERSION_KEY, ScriptProviderForooRexx.OOREXX_VERSION_VALUE);
            slotArgument.put(ScriptProviderForooRexx.OOREXX_VERSION_KEY, ScriptProviderForooRexx.OOREXX_VERSION_VALUE);

            // supply ScriptMetaData for this script
            mgr.registerBean(ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                             ScriptProviderForooRexx.OOREXX_SCRIPTMETADATA, metaData);
            slotArgument.put(ScriptProviderForooRexx.OOREXX_SCRIPTMETADATA, metaData);


            // rgf, 2007-09-20: get the filename from the URL
            // String filename=(file==null ? null : file.getFile());

            // rgf, 2008-06-17
            String filename=ScriptProviderForooRexx.getSystemPathFromFileURL(
                            (com.sun.star.uno.XComponentContext) xScriptContext.getComponentContext(),
                            metaData
                            );

            mgr.registerBean(ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                             ScriptProviderForooRexx.OOREXX_FILENAME, filename);
            slotArgument.put(ScriptProviderForooRexx.OOREXX_FILENAME, filename);


            // 20080624, rgf: allow Rexx script access to mode, such that using its public methods
            //                from the ooRexx script becomes possible
            mgr.registerBean(ScriptProviderForooRexx.OOREXX_BSFREGISTRY_PREFIX+
                             ScriptProviderForooRexx.OOREXX_SOURCE_MODEL, this);
            slotArgument.put(ScriptProviderForooRexx.OOREXX_SOURCE_MODEL, this);


            // RexxEngine re=(RexxEngine) mgr.loadScriptingEngine("rexx"); // get Rexx BSF scripting engine

            // result=mgr.apply(ScriptProviderForooRexx.OOREXX_BSFLANGUAGE, "OOo.ooRexx-editor", 0, 0, ( view.isModified() ? view.getText() : getText()), null, args);
// TODO: invokeLater ?? or is it already executing on a non-AppKit-thread ?
            result=mgr.apply(ScriptProviderForooRexx.OOREXX_BSFLANGUAGE,
                             // "OOo.ooRexx-editor ["+filename+"]",
                             // (filename==null ? "OOo.ScriptProviderForooRexx-ScriptSourceModel.execute()" : ScriptProviderForooRexx.makeFilenameLegible (filename)),
                             filename,
                             0, 0,
                             ( view.isModified() ? view.getText() : getText()),
                             null,
                             args);
                             // null);

      // rgf, 2009-10-31   re.terminate();     // let ooRexx halt the main thread of this invocation

/*
            RexxEngine re=(RexxEngine) mgr.loadScriptingEngine("rexx"); // get Rexx BSF scripting engine
            // re.setThreadedRexxStart(true); // rgf, 2008-08-13: tell engine to invoke the Rexx interpreter on a separate Java thread
            result=re.apply(
                             // "OOo.ooRexx-editor ["+filename+"]",
                             // (filename==null ? "OOo.ScriptProviderForooRexx-ScriptSourceModel.execute()" : ScriptProviderForooRexx.makeFilenameLegible (filename)),
                             filename,
                             0, 0,
                             ( view.isModified() ? view.getText() : getText()),
                             null,
                             new Vector());
*/

        }
        catch (Exception e) {
            t.setContextClassLoader(clTmp);     // 20070920, rgf: re-set to original CL
            throw e;            // re-throw exception
        }

        t.setContextClassLoader(clTmp);     // 20070920, rgf: re-set to original CL
        return result;
    }
    public void indicateErrorLine( int lineNum )
    {
        // System.out.println(ScriptProviderForooRexx.OOREXX_LANGUAGE + " indicateErrorLine " + lineNum );
        currentPosition = lineNum - 1;      // reactivated on 2006-01-30, --- rgf
        view.update();

        // System.out.println(ScriptProviderForooRexx.OOREXX_LANGUAGE + " indicateErrorLine " + lineNum );

        return;
    }

    public void clearErrorLine()      // rgf, 2006-01-30: test whether we can remove the arrow
    {
        if (currentPosition>-1)
        {
            currentPosition=-1;     // set to -1
            view.update();          // force an update
        }
    }
}
