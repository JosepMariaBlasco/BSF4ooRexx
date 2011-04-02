/*************************************************************************
*
*  $RCSfile: ScriptEditorForooRexx.java,v $
*
*  $Revision: 293 $
*
*  last change: $Author: rony $ $Date: 2009-04-06 17:51:23 +0200 (Mon, 06 Apr 2009) $
*
*  changed: 2006-01-17, ---rgf: using Thread.currentThread().getContextClassLoader().loadClass(...)
*                               instead of Class.forName(...);
*
*           2006-01-28, ---rgf: taking care of the new BSF4Rexx behaviour, which throws a Java exception
*                               supplying the Rexx execution error messages via the exception object;
*                               will now extract the error line number, position on that line and
*                               give the error message for the developer

*           2008-06-23, ---rgf: changed logic to parse the line number from Rexx error messages
*
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

/* changed: 2008-06-14, ---rgf, now using "ScriptProviderForooRexx.makeFilenameLedgible()" to
                                make the filename in the title bar more ledgible
   changed: 2009-02-21, ---rgf, added mnemonics to the editor buttons to allow
                                accelerator key navigation
*/

package com.sun.star.script.framework.provider.oorexx;

import org.apache.bsf.BSFException;     // rgf, 2006-01-28

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

// import bsh.Interpreter;      // rgf, 2005-07-08
import java.awt.event.KeyEvent;     // rgf, 2009-02-21

import com.sun.star.script.provider.XScriptContext;
import com.sun.star.script.framework.provider.ScriptEditor;
import com.sun.star.script.framework.container.ScriptMetaData;
import com.sun.star.script.framework.provider.ClassLoaderFactory;

public class ScriptEditorForooRexx
    implements ScriptEditor, ActionListener
{
    private JFrame frame;
    private String filename;

    private ScriptSourceModel model;
    private ScriptSourceView view;

    private XScriptContext context;
    private URL scriptURL = null;
    private ClassLoader  cl = null;

    // rgf, 20070920: allow to pass to Model.execute() as an argument
    private ScriptMetaData scriptMetaD=null;

    // global ScriptEditorForooRexx returned for getEditor() calls
    private static ScriptEditorForooRexx theScriptEditorForooRexx;

    // global list of ScriptEditors, key is URL of file being edited
    private static Map BEING_EDITED = new HashMap();

    // template for new ooRexx scripts
    private static String OOSTEMPLATE;

    // try to load the template for ooRexx scripts
    static {
        try {
            URL url =
                ScriptEditorForooRexx.class.getResource("template." + ScriptProviderForooRexx.OOREXX_EXTENSION);

            InputStream in = url.openStream();
            StringBuffer buf = new StringBuffer();
            byte[] b = new byte[1024];
            int len = 0;

            while ((len = in.read(b)) != -1) {
                buf.append(new String(b, 0, len));
            }

            in.close();

            OOSTEMPLATE = buf.toString();
        }
        catch (IOException ioe) {
            OOSTEMPLATE = "-- " + ScriptProviderForooRexx.OOREXX_LANGUAGE + " script, IOException: [" +
                                  ioe.getMessage()+"] has occurred in [ScriptEditorForooRexx.java]] !";
        }
        catch (Exception e) {
            OOSTEMPLATE = "-- " + ScriptProviderForooRexx.OOREXX_LANGUAGE + " script, Exception: [" +
                                  e.getMessage()+"] has occurred in [ScriptEditorForooRexx.java] !";
            ;
        }
    }

    /**
     *  Returns the global ScriptEditorForooRexx instance.
     */
    public static ScriptEditorForooRexx getEditor()
    {
        if (theScriptEditorForooRexx == null)
        {
            synchronized(ScriptEditorForooRexx.class)
            {
                if (theScriptEditorForooRexx == null)
                {
                    theScriptEditorForooRexx =
                        new ScriptEditorForooRexx();
                }
            }
        }
        return theScriptEditorForooRexx;
    }

    /**
     *  Get the ScriptEditorForooRexx instance for this URL
     *
     * @param  url         The URL of the script source file
     *
     * @return             The ScriptEditorForooRexx associated with
     *                     the given URL if one exists, otherwise null.
     */
    public static ScriptEditorForooRexx getEditor(URL url)
    {
        return (ScriptEditorForooRexx)BEING_EDITED.get(url);
    }

    /**
     *  Returns whether or not the script source being edited in this
     *  ScriptEditorForooRexx has been modified
     */
    public boolean isModified()
    {
        return view.isModified();
    }

    /**
     *  Returns the text being displayed in this ScriptEditorForooRexx
     *
     *  @return            The text displayed in this ScriptEditorForooRexx
     */
    public String getText()
    {
        return view.getText();
    }

    /**
     *  Returns the template text for ooRexx scripts
     *
     *  @return            The template text for ooRexx scripts
     */
    public String getTemplate() {
        return OOSTEMPLATE;
    }

    /**
     *  Returns the default extension for ooRexx scripts
     *
     *  @return            The default extension for ooRexx scripts
     */
    public String getExtension() {
        return ScriptProviderForooRexx.OOREXX_EXTENSION;        // "oos";
    }


    /**
     *  Indicates the line where error occured
     *
     */
    public void indicateErrorLine( int lineNum )
    {
        model.indicateErrorLine( lineNum );
        // didn't make a change, couldn't find the original sources to study
        // ((PlainSourceView) view).rgfSetLineNr(lineNum); // rgf, 2006-01-28, ???

            // try to force visually the positioning on erroneous line ..., rgf, 2006-01-30
        // view.setCaretPosition(view.getLineStartOffset(lineNum-1));
    }


    public void setScriptMetaData (ScriptMetaData smd)
    {
        this.scriptMetaD = smd;
    }


    /**
     *  Executes the script edited by the editor
     *
     */
    public Object execute() throws Exception {
        // showErrorMessage("SEFoR.execute(): ==> scriptMetaD=["+scriptMetaD+"],\n"+"==> model=["+model+"]\n"+"==> this=["+this+"]");
        frame.toFront();
        // return model.execute( context, cl );

        // 20070921, rgf: the following does not work, because for unknown
        // reason this instance is *not* the same that stored "scriptMetaD" !!
        return model.execute( context, cl, scriptMetaD);
    }
    /**
     *  Opens an editor window for the specified ScriptMetaData.
     *  If an editor window is already open for that data it will be
     *  moved to the front.
     *
     * @param  context     The context in which to execute the script
     * @param  entry       The metadata describing the script
     *
     */
    public void edit(XScriptContext context, ScriptMetaData entry) {
        if (entry != null ) {
            try {
                // following is useless, as real editor will be another instance
                // for unknown reasons
                // scriptMetaD=entry;     // 20070920, save for later use

                ClassLoader cl = null;
                try {
                    cl = ClassLoaderFactory.getURLClassLoader( entry );
                }
                catch (Exception ignore) // TODO re-examine error handling
                {
                }
                String sUrl = entry.getParcelLocation();
                if ( !sUrl.endsWith( "/" ) )
                {
                    sUrl += "/";
                }
                sUrl +=  entry.getLanguageName();
                URL url = entry.getSourceURL();

                // check if there is already an editing session for this script
                if (BEING_EDITED.containsKey(url))
                {
                    ScriptEditorForooRexx editor =
                        (ScriptEditorForooRexx) BEING_EDITED.get(url);

                    editor.frame.toFront();
                }
                else
                {
                    // new ScriptEditorForooRexx(context, cl, url);
                    new ScriptEditorForooRexx(context, cl, url, entry);
                }
            }
            catch (IOException ioe) {
                showErrorMessage( "Error loading file: " + ioe.getMessage() );
            }
        }
    }

    private ScriptEditorForooRexx() {
    }

    // private ScriptEditorForooRexx(XScriptContext context, ClassLoader cl, URL url)
    private ScriptEditorForooRexx(XScriptContext context, ClassLoader cl, URL url, ScriptMetaData smd) // 20070921, rgf
    {
        this.context   = context;
        this.scriptURL = url;
        this.model     = new ScriptSourceModel(url);
        this.filename  = url.getFile();
        this.cl = cl;
        this.scriptMetaD = smd;     // 20010921, rgf
        try {
            // Class c = Class.forName(
/*
            Class c = Thread.currentThread().getContextClassLoader().loadClass (    // rgf, 2006-01-17
                "org.openoffice.netbeans.editor.NetBeansSourceView");
*/

            // 20070920, rgf, trying original statement, which uses the class' defining class loader
            // Class c = Class.forName("org.openoffice.netbeans.editor.NetBeansSourceView");

            // 20070920, rgf: use supplied class loader
            Class c = cl.loadClass("org.openoffice.netbeans.editor.NetBeansSourceView");

            Class[] types = new Class[] { ScriptSourceModel.class };

            java.lang.reflect.Constructor ctor = c.getConstructor(types);

            if (ctor != null) {
                Object[] args = new Object[] { this.model };
                this.view = (ScriptSourceView) ctor.newInstance(args);
            }
            else {
                this.view = new PlainSourceView(model);
            }
        }
        catch (java.lang.Error err) {
            this.view = new PlainSourceView(model);
        }
        catch (Exception e) {
            this.view = new PlainSourceView(model);
        }

        this.model.setView(this.view);
        initUI();
        // frame.show();        // deprecated starting with 1.5
        frame.setVisible(true); // rgf, 20090211: available since 1.1,

        frame.toFront(); // 20070920, rgf: make sure frame is in foreground


        BEING_EDITED.put(url, this);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message,
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initUI() {

        String title= ScriptProviderForooRexx.makeFilenameLedgible(filename);

        // frame = new JFrame(ScriptProviderForooRexx.OOREXX_LANGUAGE + " Debug Window: " + filename);
        // 20070921, rgf: use shorter title
        frame = new JFrame(ScriptProviderForooRexx.OOREXX_LANGUAGE + " Macro: ["+title+"]");

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e) {
                    doClose();
                }
            }
        );

        String [] labels   = {"Run",          "Clear",       "Save",        "Close"};
        int    [] mnemonics = { KeyEvent.VK_R, KeyEvent.VK_L, KeyEvent.VK_S, KeyEvent.VK_C};
//
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());

        for (int i = 0; i < labels.length; i++) {
            JButton b = new JButton(labels[i]);
            b.setMnemonic(mnemonics[i]);
            b.addActionListener(this);
            p.add(b);

            if (labels[i].equals("Save") && filename == null) {
                b.setEnabled(false);
            }
        }

        frame.getContentPane().add((JComponent)view, "Center");
        frame.getContentPane().add(p, "South");
        frame.pack();
        // frame.setSize(590, 480);
        frame.setSize(800, 600);        // rgf, 2006-01-17
        // frame.setLocation(300, 200);
        frame.setLocation(350, 175);    // rgf, 20070921
    }

    private void doClose() {
        if (view.isModified()) {
            int result = JOptionPane.showConfirmDialog(frame,
                "The script has been modified. " +
                "Do you want to save the changes?");

            if (result == JOptionPane.CANCEL_OPTION)
            {
                // don't close the window, just return
                return;
            }
            else if (result == JOptionPane.YES_OPTION)
            {
                boolean saveSuccess = saveTextArea();
                if (saveSuccess == false)
                {
                    return;
                }
            }
        }
        frame.dispose();
        shutdown();
    }

    private boolean saveTextArea() {
        boolean result = true;

        if (!view.isModified()) {
            return true;
        }

        OutputStream fos = null;
        try {
            String s = view.getText();
            fos = scriptURL.openConnection().getOutputStream();
            if ( fos  != null) {
                fos.write(s.getBytes());
            }
            else
            {
                showErrorMessage("Error saving script: Could not open stream for file" );
                result = false;
            }
            view.setModified(false);
       }
       catch (IOException ioe) {
           showErrorMessage( "Error saving script: " + ioe.getMessage() );
           result = false;
       }
       catch (Exception e) {
           showErrorMessage( "Error saving script: " + e.getMessage() );
           result = false;
       }
        finally {
            if (fos != null) {
                try {
                    fos.flush();
                    if ( fos != null )
                    {
                        fos.close();
                    }
                }
                catch (IOException ignore) {
                }
            }
        }
        return result;
    }

    private void shutdown()
    {
        if (BEING_EDITED.containsKey(scriptURL)) {
            BEING_EDITED.remove(scriptURL);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Run")) {
            try
            {
                // showErrorMessage("SEFoR.actionPerformed(), 'run': scriptMetaD=["+scriptMetaD+"], "+"model=["+model+"]\n"+"this=["+this+"]");
                execute();
                //   already cleared by any update in the editor
                // model.clearErrorLine(); // ---rgf, 2006-01-30 in any case clear arrow after successful run
            }

            catch (BSFException bse)    // Rexx execution error
            {
                String bseMsg=bse.getMessage();     // get exception message
                int errNrLine=getRexxErrorLineNumber(bseMsg);   // parse for error line number
                if (errNrLine>0)                                // found?
                {
                    indicateErrorLine(errNrLine);               // indicate the error line
                }
                showErrorMessage(bseMsg);                       // display error message (= Exception's text)
            }

            catch (Exception invokeException ) {
                showErrorMessage(invokeException.getMessage());
            }
        }
        else if (e.getActionCommand().equals("Close")) {
            doClose();
        }
        else if (e.getActionCommand().equals("Save")) {
            saveTextArea();
        }
        else if (e.getActionCommand().equals("Clear")) {
            view.clear();
        }
    }



    /** Try to locate the error line number in the supplied Rexx error message and return it as an int.
     *  @param msg Rexx error message to parse
     *  @return error line number or -1, if no line number was found
     */
    static int getRexxErrorLineNumber(String msg)
    {
        if (msg==null)
        {
            return -1;      // return impossible line number
        }

        String arrStr[]=msg.split("Error .* running .* line "); // find Rexx error msg with line on it
        int i=arrStr.length;    // get number of elements
        if (i>1)            // o.k. we got more than one element, so proceed
        {
            arrStr=arrStr[i-1].split(": "); // try to find line number in last String element
            if (arrStr.length>0)
            {
                try // now try to turn the line number into an int
                {
                    return Integer.valueOf( ((String) arrStr[0]) ).intValue();
                }
                catch (Exception e) // didn't work, return impossible error line number
                {
                    return -1;
                }
            }
        }
        return -1;          // return impossible line number

    }
}
