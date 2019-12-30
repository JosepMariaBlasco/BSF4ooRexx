package org.oorexx.doc.input.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.StringUtils;
 
public class FileTypeFilter extends FileFilter {
    private List<String> extensions;
    private String description;
 
    public FileTypeFilter(List<String> extensions, String description) {
        this.extensions = extensions;
        this.description = description;
    }
    
    public FileTypeFilter(String extension, String description) {
        this.extensions = new ArrayList<String>();
        this.extensions.add(extension);
        this.description = description;
    }
 
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        
        for(String extension: extensions)
        	if(file.getName().toLowerCase().endsWith(extension.toLowerCase()))
        		return true;
        
        return false;
    }
 
    public String getDescription() {
        return description + StringUtils.join(extensions, ",");
    }
}