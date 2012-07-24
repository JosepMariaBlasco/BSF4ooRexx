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
public class Test extends FileFilter implements FilenameFilter
{
    private String  description=null;           // description string for this filter (JFileChooser)
    private String  pattern=null;               // regular pattern expression, if any

/*
    / ** Constructor which accepts the description for this filter.
     * @param description
     * /
    public RgfFilter (String description)       // constructor allowing reception of the descriptive String
    {
        this.description=description;
    }

*/

    /** Returns the present description for the filter.
     */
    public String getDescription()              // allow access to description
    {
        return this.description;
    }

    public boolean accept(File f)               // FileFilter for javax.swing.JFrame
    {
        return true;
    }

    // from FileNameFilter
    /** Method which gets invoked by java.awt.FileDialog to test the file for inclusion in the file list.
     * @param dir the directory of the file
     * @param fname the file's name
     * @return true, if the file should be included in the file list, false else
     */
    public boolean accept(File dir, String fname)   // FileNameFilter for java.awt.FileDialog
    {
        return false;
    }

}


