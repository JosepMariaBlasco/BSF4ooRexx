/*
 * Copyright 2004-2009 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author   Rony G. Flatscher (added BSF_Log[Factory] to allow BSF to run without org.apache.commons.logging present)
 */

 /* changes:

    2009-09-10, rgf: take into account that the current context class loader may be null

    2008-07-04, rgf: if classes cannot be defined or found, try to use the current Thread's
                     content class loader using a new inner class "LocalThreadClassLoader"
 */

package org.apache.bsf.util.event.generator;

import org.apache.bsf.BSF_Log;
import org.apache.bsf.BSF_LogFactory;

import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.*;



public class AdapterClassLoader extends ClassLoader
{
  private static Hashtable classCache = new Hashtable();
  private Class c;

  // private Log logger = LogFactory.getLog(this.getClass().getName());
  private BSF_Log logger = null;

  public AdapterClassLoader()
  {
	super();
        logger = BSF_LogFactory.getLog(this.getClass().getName());
  }


  public synchronized Class defineClass(String name, byte[] b)
        throws Throwable
  {
	if ((c = getLoadedClass(name)) == null)
	{
          final String tmpName=name.replace('/','.');

          try
          {
              c = defineClass(tmpName, b, 0, b.length);   // rgf, 2006-02-03
          }
          catch (NoClassDefFoundError e)  // note "Error": Java thread would be killed otherwise!
          {
              // rgf, 2009-09-10
              // now try the Thread's current context class loader, but don't cache it
              ClassLoader tccl=Thread.currentThread().getContextClassLoader();
              if (tccl!=null)
              {
                  try
                  {
                  //    LocalThreadClassLoader ltcl=new LocalThreadClassLoader(Thread.currentThread().getContextClassLoader());
                      LocalThreadClassLoader ltcl=new LocalThreadClassLoader(tccl);

                      return ltcl.defineClass(tmpName,b);
                  }
                  catch (NoClassDefFoundError e1) // (NoClassDefFoundError e1)
                  {
    /*
    RgfMsgBox("AdapterClassLoader.defineClass(): intercepted throwable! \n"+
              "      name=["+name+"] -> tmpName=["+tmpName+"]\n\n"+
              "      e.toString=["+e.toString()+"]:\n\n"+getPrintStackAsString(e),
                     '2');
    */

                      logger.error("AdapterClassLoader: NoClassDefFoundError ERROR for class ["+tmpName+"]!");
                      throw e1;      // rethrow error
                  }
              }
              else
              {
                  logger.error("AdapterClassLoader: NoClassDefFoundError ERROR for class ["+tmpName+"] (info: Thread context class loader is 'null'.)!");
                  throw e;      // rethrow error
              }
          }

	  put(name, c);
	}
        else
        {
            logger.error("AdapterClassLoader: "+c+" previously loaded. Can not redefine class.");
        }

	return c;
  }


  final protected Class findClass(String name)
  {
	return get(name);
  }

  final protected Class get(String name)
  {
	return (Class)classCache.get(name);
  }


  public synchronized Class getLoadedClass(String name)
  {
	Class c = findLoadedClass(name);

	if (c == null)
	{
	  try
	  {
		c = findSystemClass(name);
	  }
	  catch (ClassNotFoundException e)
	  {
	  }
	}

	if (c == null)
	{
	  c = findClass(name);
	}

        // rgf, 2008-07-04
        if (c==null)        // not found so far, try to use the current Thread's context class loader instead
        {
            LocalThreadClassLoader ltcl=new LocalThreadClassLoader(Thread.currentThread().getContextClassLoader());

            c = ltcl.findLoadedClass(name,'0');

            if (c == null)
            {
              try
              {
                    c = ltcl.findSystemClass(name,'0');
              }
              catch (ClassNotFoundException e)
              {
                  try
                  {
                      c = ltcl.findClass(name,'0');
                  }
                  catch (ClassNotFoundException e1)
                  {
                  }
              }
            }
        }

	return c;
  }

  protected synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
  {
	Class c = getLoadedClass(name);

	if (c != null && resolve)
	{
	  resolveClass(c);
	}

	return c;
  }
  // Not in JDK 1.1, only in JDK 1.2.
//  public AdapterClassLoader(ClassLoader loader)
//  {
//    super(loader);
//  }

  final protected void put(String name, Class c)
  {
	classCache.put(name, c);
  }


  /** Inner class to create a ClassLoader with the current Thread's class loader as parent.
  */
  class LocalThreadClassLoader extends ClassLoader {
      // public LocalThreadClassLoader(){super (Thread.currentThread().getContextClassLoader());};
      public LocalThreadClassLoader (ClassLoader cl)
      {
          super (cl);
      }

      public Class defineClass(String name, byte[] b)
      {
          return defineClass(name, b, 0, b.length);     // protected in ClassLoader, hence invoking it this way
      }

            // use a signature that allows invoking super's protected method via inheritance resolution
      Class findLoadedClass(String name, char nixi)
      {
          return findLoadedClass(name);
      }

            // use a signature that allows invoking super's protected method via inheritance resolution
      Class findClass(String name, char nixi)          throws ClassNotFoundException
      {
          return findClass(name);
      }

            // use a signature that allows invoking super's protected method via inheritance resolution
      Class findSystemClass(String name, char nixi)    throws ClassNotFoundException
      {
          return findSystemClass(name);
      }

  }




/////
     // rgf, 2005-05-07
    /** Returns <code>printStackTrace()</code> as a <code>String</code>.
     *
      * @param e an Exception object.
      * @return  a String
    */
    static private String getPrintStackAsString(Throwable t)
    {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       PrintStream           ps   = new PrintStream(baos);
       t.printStackTrace(ps);
       return baos.toString();
    }


    static private void RgfMsgBox (String message, char kind) {

        final JFrame frame = new JFrame("RgfMsgBox");

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e) {
                    // System.exit(0);     // kill JVM ???
                    frame.dispose();
                    return;
                }
            }
        );

        if (kind=='1')
        {
            JOptionPane.showMessageDialog(frame, message,"information", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(frame, message,"error"      , JOptionPane.ERROR_MESSAGE      );
        }

        frame.dispose();
    }

}
