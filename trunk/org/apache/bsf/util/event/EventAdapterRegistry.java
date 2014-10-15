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
 */

package org.apache.bsf.util.event;

import java.util.Hashtable;

import org.apache.bsf.util.event.generator.EventAdapterGenerator;
import org.apache.bsf.BSFManager;

/**
 * The <em>EventAdapterRegistry</em> is the registry of event adapters.
 * If a desired adapter is not found, the adapter will be dynamically
 * generated when lookup is attempted. Set the <code>dynamic</code> property
 * to <code>false</code> to disable this feature.
 * <p>
 * This implementation first looks for an adapter in its lookup table
 * and if it doesn't find one looks for a standard implementation of
 * that adapter in the org.apache.bsf.util.event.adapters package with a
 * standard naming convention. The naming convention it assumes is the
 * following: for event listener type <tt>a.b.c.FooListener</tt>,
 * it loads an adapter of type
 * <tt>org.apache.bsf.util.event.adapters.a_b_c_FooAdapter</tt>.
 * If both the loading and the dynamic generation fail, then a
 * <code>null</code> is returned.
 * <p>
 *
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @see      EventAdapter
 */


 /* changed:
    2009-09-10: rgf, take into account that a context class loader may not be set

    2007-09-21: Rony G. Flatscher, new class loading sequence:

        - Thread's context class loader
        - settable class loader stored with EventAdapterRegistry
        - BSFManager's defining class loader
 */

public class EventAdapterRegistry {
  private static Hashtable reg = new Hashtable ();
  private static ClassLoader cl = null;
  private static String adapterPackage = "org.apache.bsf.util.event.adapters";
  private static String adapterSuffix = "Adapter";
  private static boolean dynamic = true;

  public static Class lookup (Class listenerType) {
	String key = listenerType.getName().replace ('.', '_');
	Class adapterClass = (Class) reg.get (key);

	if (adapterClass == null) {
	  String en = key.substring (0, key.lastIndexOf ("Listener"));
	  String cn = adapterPackage + "." + en + adapterSuffix;


// System.err.println("\n*** [EventAdapaterRegistry.lookup("+listenerType+")]: loading/creating cn=["+cn+"]...");

//------------------------------
              if (adapterClass==null) {     // CTXCL
                  // rgf, 2009-09-10
                  ClassLoader tccl=Thread.currentThread().getContextClassLoader();

                  if (tccl!=null)
                  {
                      try {     // try supplied class loader
    // System.err.println("==> [EventAdapaterRegistry.lookup("+listenerType+")]: CTXCL=["+Thread.currentThread().getContextClassLoader()+"]...");
                          adapterClass=Thread.currentThread().getContextClassLoader().loadClass(cn);
    // System.err.println("--> [EventAdapaterRegistry.lookup("+listenerType+")]: adapterClass=["+adapterClass+"]...");
                      }
                      catch (ClassNotFoundException e02) {}
                  }
              }


              try {     // try ClassLoader set in this object (cf. this.setClassLoader())
                  if (cl !=null)
                  {
// System.err.println("==> [EventAdapaterRegistry.lookup("+listenerType+")]: cl=["+cl+"]...");

                      adapterClass=cl.loadClass(cn);
// System.err.println("--> [EventAdapaterRegistry.lookup("+listenerType+")]: adapterClass=["+adapterClass+"]...");
                  }
              }
              catch (ClassNotFoundException e01) {}


              if (adapterClass==null) {     // Defined CL
                  try {     // try supplied class loader
                      ClassLoader defCL=BSFManager.getDefinedClassLoader();
                      if (cl != defCL) {
// System.err.println("==> [EventAdapaterRegistry.lookup("+listenerType+")]: defCL=["+defCL+"]...");
                          adapterClass=defCL.loadClass(cn);
// System.err.println("--> [EventAdapaterRegistry.lookup("+listenerType+")]: adapterClass=["+adapterClass+"]...");
                      }
                  }
                  catch (ClassNotFoundException e03) {}
              }


              if (adapterClass==null && dynamic) {
                // Unable to resolve one, try to generate one.
// System.err.println("==> [EventAdapaterRegistry.lookup("+listenerType+")]: dynamically creating an adapter class!");
                adapterClass = // if second argument is set to 'true', then the class file will be stored in the filesystem
                      EventAdapterGenerator.makeEventAdapterClass (listenerType, false);
// System.err.println("--> [EventAdapaterRegistry.lookup("+listenerType+")]: adapterClass=["+adapterClass+"]...");
              }


//------------------------------


// System.err.println("... [EventAdapaterRegistry.lookup("+listenerType+")]: adapterClass=["+adapterClass+"]...");
	  if (adapterClass != null) {
		reg.put (key, adapterClass);
	  }
	}

	return adapterClass;
  }
  public static void register (Class listenerType, Class eventAdapterClass) {
	String key = listenerType.getName().replace('.', '_');
	reg.put (key, eventAdapterClass);
  }
  /**
   * Class loader to use to load event adapter classes.
   */
  public static void setClassLoader (ClassLoader cloader) {
	cl = cloader;
  }
  /**
   * Indicates whether or not to dynamically generate adapters; default is
   * <code>true</code>.
   * <p>
   * If the <code>dynamic</code> property is set to true, and the
   * <code>ClassLoader</code> is unable to resolve an adapter, one will be
   * dynamically generated.
   *
   * @param dynamic whether or not to dynamically generate adapters.
   */
  public static void setDynamic (boolean dynamic) {
	EventAdapterRegistry.dynamic = dynamic;
  }
}
