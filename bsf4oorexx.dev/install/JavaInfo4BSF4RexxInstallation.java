/** Class to help installing BSF4Rexx, will output all <code>java.lang.System.getProperties()</code>
 *  as a &quot;<code>propertyName=[value]</code>&quote; pair, each delimited with
 *  &quot;<code>\\n</code>&quot;. This allows RxQueue'ing the output into any Rexx program for further
 *  analysis.
 *
 * <p>
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2005-2014 Rony G. Flatscher
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
 *
 * @author Rony G. Flatscher (c) 2005-2014
 * @version 1.5
 *
 */

 /*
    changes:
        2014-06-16: ---rgf, unoinfo returns "1" indicator for UTF-16LE, however in AOO 4
                            the encoded data may indeed be ASCII encoded with a "\0\0"
                            indicating end of path; adjust getClassPath4OOo() to cater for
                            this situation (will convert from UTF-16LE only, if sequence of
                            "\0\0\0" can be found in the returned unoinfo string)

        2014-01-12: method getClassPath4OOo() simply does a replaceAll(), if unoinfo data
                    is UTF-16LE encoded

        2013-07-15: determining classpath for OOo/LO; it seems that starting with Java 1.7
                    Runtime.exec(cmd) was changed such, that runtime errors occur that inhibit
                    the fetching of the classpath values needed for OOo/LO, if the path to
                    unoinfo contains a blank

        2013-07-02: OOo-support: add variant "getClassPath4OOo-only" which will replace
                    the '\0' in the string "unoinfo java" returns by the system's file
                    separator, but will not prepend or append anything; this way shell
                    scripts can get easily at the needed classpath values for OOo/LO

        2008-07-12: OOo-support: added processing of an argument "getClassPath4OOo", which
                    may be optionally followed by the fully qualified (enquoted) path to
                    "unoinfo"; prints "CLASSPATH" and the necessary CLASSPATH string, delimited
                    with a single blank; this string can be used to set/define the CLASSPATH
                    environment, removed all additions of 2008-06-15, as not needed anymore

        2008-06-15: refactored code, added fall back to listing the most important Java
                    properties in case a SecurityManager hinders to get at all defined
                    Java properties;

                    added argument handling for:

                        name ... assumed to be a Java property; prints property value to stdout

                        the following expects two arguments, the first indicating an encoding which
                        the second argument represents in form of a byte string; returns a Java
                        string that has the byte-string accordingly converted

                        /US-ASCII       byte-string ... Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
                        /ISO-8859-1     byte-string ... ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
                        /UTF-8          byte-string ... Eight-bit UCS Transformation Format
                        /UTF-16BE       byte-string ... Sixteen-bit UCS Transformation Format, big-endian byte order
                        /UTF-16LE       byte-string ... Sixteen-bit UCS Transformation Format, little-endian byte order
                        /UTF-16         byte-string ... Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark


        2007-01-28: using Thread.currentThread().getContextClassLoader().loadClass(...) instead of Class.forName(...),
                    now returns information on availability of BSF and BSF4Rexx, e.g. (if not available,
                    version string is empty string):

                        org.apache.bsf.BSFManager.available=[1]
                        org.apache.bsf.BSFManager.version=[242.20070128]
                        org.rexxla.bsf.engines.rexx.RexxEngine.available=[0]
                        org.rexxla.bsf.engines.rexx.RexxEngine.version=[]

                    if argument is given, returns only what java.lang.System.getProperty(arg) returns
                    (does not list all properties!)

        2006-08-01: now tests for presence of "org.apache.commons.logging.LogFactory"; returning
                    "rgf.commons.logging.available=[1]" if loadable, else
                    "rgf.commons.logging.available=[0]"

   last change: $Revision: 601 $ $Author: rony $ $Date: 2010-03-18 18:52:11 +0100 (Thu, 18 Mar 2010) $
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class JavaInfo4BSF4RexxInstallation
{
    /** Version field for introspection of this class. */
    public final String version="105.20140616";

    public static void main (String argv[]) throws java.lang.Exception // java.io.UnsupportedEncodingException
    {

/*
System.err.println("main(), # argv: ["+argv.length+"]");
for (int i=0; i<argv.length; i++)
{
    System.err.println("\targ["+i+"]=["+argv[i]+"]");
}
*/


        if (argv.length==0)
        {
            try
            {
                showAllProperties();    // try to show all defined properties
            }
            catch (SecurityException se)
            {
                showSomeProperties();   // make sure the most important properties are shown
            }
        }
        else
        {
            String argv0=argv[0].toLowerCase();

                // "getClassPath4OOo" (add prefix "CLASSPATH " for Rexx parsing), "getClassPath4OOo-only"
            boolean bQueryOOoClassPath      =  argv0.startsWith("getclasspath4ooo");
            boolean bQueryOOoClassPathPrefix=!(argv0.endsWith("-only"));
            // boolean bQueryOOoClassPath=argv[0].equalsIgnoreCase("getClassPath4OOo");

            String res="";
            if (argv.length==1)     // get and return just the value
            {
                if  (bQueryOOoClassPath==false)
                {
                    res=nullString(System.getProperty(argv[0]));
                }
                else        // no fully qualified path to executable "unoinfo" given!
                {
                    res=getClassPath4OOo(null,bQueryOOoClassPathPrefix);
                }
            }
            else if (bQueryOOoClassPath && argv.length==2)    // two argv
            {
                res=getClassPath4OOo(argv[1],bQueryOOoClassPathPrefix);
            }
            else
            {
                throw new Exception (">>> Panic, unknown arguments, aborting... <<<");
            }

            System.out.println(res);
        }

        // showSomeProperties();
        System.exit(0);
    }

    static char [] letters={ '0', '1', '2', '3', '4', '5', '6', '7',
                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


    /* System.getProperties() may fail due to a SecurityManager, however
     * System.getProperty(...) will still work
    */
    static void showSomeProperties()
    {
        StringBuffer sb=new StringBuffer();    // create a StringBuffer
        String       nl=System.getProperty("line.separator");


        String keys[]={ "file.separator", "path.separator", "line.separator",
                        "java.class.version", "java.ext.dirs", "java.home",
                        "java.specification.version", "java.vendor.url",
                        "java.vendor", "java.version", "os.arch", "os.name",
                        "os.version", "user.name", "user.home", "user.dir",
                        "java.library.path", "java.class.path"
                      };

        for (int i=0; i<keys.length; i++)
        {
            getAndShowProperty(keys[i], sb, nl);
        }

        showAdditionalInformation(sb, nl);
    }


       /* System.getProperties() may fail due to a SecurityManager, however */
    static void showAllProperties()
    {
        java.util.Enumeration prop_names=System.getProperties().propertyNames();   // get all the property names

        StringBuffer sb=new StringBuffer();    // create a StringBuffer
        String       nl=System.getProperty("line.separator");
        String       key="";

        while (prop_names.hasMoreElements())
        {
            key=(String) prop_names.nextElement();
            getAndShowProperty(key, sb, nl);

            //sb=sb.append(key).append("=[").append( nullString(System.getProperty(key))).append("]").append(nl);;
        }

        showAdditionalInformation(sb, nl);
    }

    /* Creates additional information.
    */
    static void showAdditionalInformation (StringBuffer sb, String nl)
    {
            // test for availability of org.apache.commons.logging
        sb=sb.append( get_commons_logging_availability             ()  ).append(nl);
        sb=sb.append( get_org_apache_bsf_availability              (nl) ).append(nl);
        sb=sb.append( get_org_rexxla_bsf_engines_rexx_availability (nl) ).append(nl);

        System.out.println(sb);
    }


    static void getAndShowProperty (String key, StringBuffer sb, String nl)
    {
        sb.append(key).append("=[").append( nullString(System.getProperty(key))).append("]").append(nl);;
    }

    /* Returns empty string, if null; turns strings with control chars into hexstrings led in by "0x".
    */
    static String nullString(String o)      // if null, return empty String instead
    {
        if (o==null) return "";

        // check for chars < x20, encode them
        StringBuffer sb=new StringBuffer();
        char         ch=0;
        for (int i=0; i<o.length(); i++)
        {
            if (o.charAt(i)>31)
               continue;
            // o.k., char < 0x20: we need to transform this string into a hex-string "0xHEXDATA"
            sb.append("0x");
            for (int k=0; k<o.length(); k++)
            {
                ch=o.charAt(k);
                sb.append(letters[(ch >>> 16)]);
                sb.append(letters[(ch & 0x0f) ]);
            }
            return sb.toString();
        }

        return o;
    }



    static String get_commons_logging_availability()
    {
        String kw="org.apache.commons.logging.LogFactory=[";    // define keyword and leadin
        try
        {
            // try to load the class dynamically
            // Class lf=Class.forName("org.apache.commons.logging.LogFactory");

            Class lf=Thread.currentThread().getContextClassLoader().loadClass("org.apache.commons.logging.LogFactory");
        }
        catch (ClassNotFoundException e)
        {
            return kw+"0]"; // return Rexx character for .false
        }
        return kw+"1]";     // return Rexx character for .true

    }



    // use reflection to deal with this class (could be absent)
    static String get_org_apache_bsf_availability(String nl)
    {
        String clzName="org.apache.bsf.BSFManager";
        String kw=clzName+".available=[";   // define keyword and leadin
        Class  clz=null;

        try
        {
                // load class
            clz= Thread.currentThread().getContextClassLoader().loadClass(clzName);
            kw=kw+"1]"; // "1": Rexx character for .true
        }
        catch (ClassNotFoundException e)
        {
            kw=kw+"0]"; // "0": Rexx character for .false
        }

        kw=kw+nl+clzName+".version=[";     // now try to get version, if possible at all
        if (clz==null)      // no class, hence no value
        {
            kw=kw+"]";
        }
        else
        {
            try
            {
                    // get static field with Class object argument
                java.lang.reflect.Method m=clz.getMethod("getVersion", null);

                kw=kw+m.invoke(null, null).toString()+"]";   // a static field, hence "object" argument is ignored and may be null
            }
            catch (Exception e)
            {
                kw=kw+"]";
            }
        }

        return kw;     // return Rexx character for .true
    }




    // use reflection to deal with this class (could be absent)
    static String get_org_rexxla_bsf_engines_rexx_availability(String nl)
    {
        String clzName="org.rexxla.bsf.engines.rexx.RexxEngine";
        String kw=clzName+".available=[";   // define keyword and leadin
        Class  clz=null;


        try
        {
                // load class
            clz= Thread.currentThread().getContextClassLoader().loadClass(clzName);
            kw=kw+"1]"; // "1": Rexx character for .true
        }
        catch (ClassNotFoundException e)
        {
            kw=kw+"0]"; // "0": Rexx character for .false
        }

        kw=kw+nl+clzName+".version=[";     // now try to get version, if possible at all
        if (clz==null)      // no class, hence no value
        {
            kw=kw+"]";
        }
        else
        {
            try
            {
                    // get static field with Class object argument
                java.lang.reflect.Field f=clz.getField("version");
                kw=kw+f.get(null).toString()+"]";   // a static field, hence "object" argument is ignored and may be null
            }
            catch (Exception e)
            {
                kw=kw+"]";
            }
        }

        return kw;     // return Rexx character for .true
    }


    static byte[] getHexStringAsBytes(String hexstring)
    {
            // make sure we have enough bytes
        int hsLength=hexstring.length();
        byte bytes[]=new byte [hsLength/2] ;

            // create byte values from byte string
        int idx=0;
        int i  =0;
        int endPos=0;
        for (i=0; i<hsLength; i+=2, idx++)
        {
            endPos=i+2;
System.out.println("  idx=["+idx+"]: ["+hexstring.substring(i, endPos)+"]...");
           bytes[idx]=Byte.parseByte(hexstring.substring(i, endPos), 16);
        }
        return bytes;
    }



    // ---------------------------------------------------------------------
    // OpenOffice.org 3.x configuration related methods, rgf, 2008-07-12

    /** Runs the 'unoinfo' executable to learn from the OOo 3.0 installation how to setup
     *  the CLASSPATH environment variable, in order to be able to address {@link http://www.OpenOffice.org}.
     *
     *  Cf. sb's posting as of 2008-05-07 in *  {@link http://qa.openoffice.org/issues/show_bug.cgi?id=88687}.
     *
     * @param path2unoinfo    fully qualified path to the 'unoinfo' executable, or null if available via PATH
     * @return                string to be used to define or extend the CLASSPATH environment variable
     */
    static private String getClassPath4OOo (String path2unoinfo, boolean bWithPrefix)
    {
        String res="";
        if (path2unoinfo==null)       // no path given, hence assume "unoinfo" is available via PATH
        {
            path2unoinfo="unoinfo";
        }

        try {
            String line=null, prefix="CLASSPATH ";

            if (bWithPrefix==false)     // do not add the prefix (prefix needed by setup*.rex for parsing)
            {
                prefix="";
            }

            Process p=null;
            // p = Runtime.getRuntime().exec(path2unoinfo+" java");  // add argument to command
            String args[]={ path2unoinfo, "java"};
            p = Runtime.getRuntime().exec(args);  // add argument to command

            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));
            line = input.readLine();      // read the single line
            input.close();

            if (line==null)
            {
                return "";
            }

            if (line.charAt(0)=='0')    // 8-Bit-ASCII encoded, '\0' char delimits paths
            {
                res=prefix+line.substring(1).       // skip first char
                         replace('\0', System.getProperty("path.separator").charAt(0));
            }

            else    // "UTF-16LE" encoded, two nul-bytes ("\0\0") delimit paths
            {
                // TODO: check for the following assumptions in the future (as of 2014-06-16):
                // as AOO 4.x returns wrongly encoded data with indicator (reported on Windows) '1'
                // for UTF-16LE we need to guess the encoding by checking for "\0\0\0"
                // (two "\0\0" are path delimiters, the third one introduces another escaped char)
                String tmpStr=line.substring(1);    // skip first (indicator) character, not part of the encoded string

                if (tmpStr.indexOf("\0\0\0") != -1) // assume we have indeed an UTF-16LE encoding!
                {
                    byte b[]=tmpStr.getBytes();
                    tmpStr=new String(b,0,b.length, "UTF-16LE");
                    tmpStr=tmpStr.replace('\0',System.getProperty("path.separator").charAt(0));
                }
                else
                {
                    tmpStr=tmpStr.replaceAll("\0\0",System.getProperty("path.separator"));
                }

                res=prefix+tmpStr;
            }
        }
        catch (Exception err) {
                // indicate something went wrong
            System.err.println(">>> Java exception: ["+err.toString()+
                               "] when trying to run 'unoinfo java' or processing its output ! <<<");
            return "";
        }
        return res;
    }

}

