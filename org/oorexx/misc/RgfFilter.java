//   last change: $Revision: 148 $ $Author: rony $ $Date: 2006-02-06 00:46:26 +0100 (Mon, 06 Feb 2006) $

package org.oorexx.misc;

// ---rgf, 2006-01-19: utility class to allow defining file filters by file extension
//                     should allow Lee Pedin to exploit the FileDialog and JFileChooser

/** This class allows defining file types for filters for java.awt.FileDialog
 *  and javax.swing.JFileChooser.
 *
 * <p>
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2006 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
 * @author Rony G. Flatscher
 * @version 1.0, 2006-01-22
 */

import java.io.File;
import java.io.FilenameFilter;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;


// make sure that this class can be used for java.awt.FileDialog (FileNameFilter), and javax.swing.JFrame (FileFilter)
public class RgfFilter extends FileFilter implements FilenameFilter
{
    private Vector vecExtensions=new Vector();  // Vector of file extensions
    private boolean bIgnoreCase=true;           // default to case insensitiveness
    private boolean hasPattern=false;           // indicates wheter a regular expression pattern was set

    private String  description=null;           // description string for this filter (JFileChooser)
    private String  pattern=null;               // regular pattern expression, if any

    /** Constructor which accepts the description for this filter.
     * @param description
     */
    public RgfFilter (String description)       // constructor allowing reception of the descriptive String
    {
        this.description=description;
    }

    /** Set a regular expression pattern (available starting with Java 1.4).
     *  If file extensions were set (cf. {@link #addExtension(String extension)}), it is invalidated after invocation of this method, ie. it
     *  will not get used anymore.
     * @param pattern  regular expression pattern (<code>java.util.regex.Pattern</code>, since Java 1.4)
     */
    public void setPattern (String pattern)
    {
        this.pattern=pattern;                   // save pattern to use
        this.hasPattern=true;                   // from now on use pattern only
    }

    /** Returns the present description for the filter.
     */
    public String getDescription()              // allow access to description
    {
        return this.description;
    }

    /** Allows setting the description for the filter.
     * @param description
     */
    public void setDescription(String description)  // allow to set a description
    {
        this.description=description;
    }

    /** Method which gets invoked by javax.swing.JFrame to test the file for inclusion in the file list.
     * @param f the file object to test
     * @return true, if the file should be included in the file list, false else
     */
    public boolean accept(File f)               // FileFilter for javax.swing.JFrame
    {
        if (f.isDirectory()==true)              // include directories in any case
        {
            return true;
        }
        return accept(f.getName());             // do the check
    }


    /** Method which gets invoked by java.awt.FileDialog to test the file for inclusion in the file list.
     * @param dir the directory of the file
     * @param fname the file's name
     * @return true, if the file should be included in the file list, false else
     */
    public boolean accept(File dir, String fname)   // FileNameFilter for java.awt.FileDialog
    {
        return accept(fname);
    }

    // this really does the work to determine whether the file should be included or not
    private boolean accept(String fname)        // test whether extension is accepted
    {
        if (this.hasPattern==true)              // use pattern matching only, no case translation of fname
        {                                       // only possible starting with Java 1.4
            return fname.matches(this.pattern); // return result of matching operation
        }


        String ufname=null;

        Enumeration e = this.vecExtensions.elements();  // get all elements

        while (e.hasMoreElements()==true)
        {
            String ext=(String) e.nextElement();

            if (fname.endsWith(ext)==true) return true;             // ends with file type
            if (bIgnoreCase==true)                                  // ends with file type
            {
                if (fname.toUpperCase().endsWith(ext.toUpperCase())==true) return true;
            }
        }
        return false;
    }



    /** Allows to define the extension for files that should be included. If you accept multiple extensions,
     *  then invoke this method accordingly, each time with a different file extension you want to accept.
     *  If a regular pattern expression was set (cf. {@link #setPattern (String pattern)}), it is invalidated after invocation of this method, ie. it
     *  will not get used anymore.
     * @param extension the file extension without any wild cards
     */
    public void addExtension(String extension)  // allow to add an extension
    {
        this.vecExtensions.add(extension);
        this.hasPattern=false;                  // from now on use file extension tests only
    }

    /** Allows setting the boolean value that controls whether the test for an existing file extension
     *  should be carried out case-insensitve (the default) or not.
     * @param b if true, then ignore case in comparisons
    */
    public void setIgnoreCase(boolean b)        // allow to set whether case should be ignored or not
    {
        this.bIgnoreCase=b;
    }

    /** Returns the present setting.
     * @return true, if comparisons are carried out case insensitively, false else
     */
    public boolean isIgnoreCase()               // returns whether case is ignored or not
    {
        return this.bIgnoreCase;
    }
}


