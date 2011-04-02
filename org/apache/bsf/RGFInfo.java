
package org.apache.bsf;

class RGFInfo {
    static StringBuffer sb=new StringBuffer();

    static String       fn="C:\\debugRGFInfo.log";
    static String       ps=System.getProperty("path.separator");



    static void append (StringBuffer arg)
    {
        sb.append(arg);
    }

    static void append (String arg)
    {
        sb.append(arg);
    }

    static void append (char arg)
    {
        sb.append(arg);
    }

    static void newSection(String str)
    {
        sb.append("\n\n--- New run --- ");
        sb.append(fn+" --- ");
        sb.append(new java.util.Date().toString()+"--- ");
        sb.append("\n    --- "+str+"--- ");
        sb.append("\n\n");
    }

    static void endSection(String strInfo)
    {
        sb.append("--- The-end-of-this-run ("+strInfo+") ---\n");

        boolean bAppend=true;
        try
        {
            java.io.FileWriter fw=new java.io.FileWriter(fn, bAppend);
            String str=sb.toString();
            fw.write(str, 0, str.length());
            fw.flush();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void dumpEnvironment()
    {

        String props[]={ "java.class.path", "java.endorsed.dirs", "java.ext.dirs",
                         "java.home", "java.io.tmpdir", "java.library.path",
                         "sun.boot.class.path", "sun.boot.library.path",
                         "user.dir", "user.home", "user.name"
                       };

        int i=0;
        for ( i=0;i<props.length;i++)
        {
            sb.append(pp(props[i], ps));
        }
        sb.append("\n");

    }

    static StringBuffer pp(String key, String ps)
    {
        StringBuffer sb = new StringBuffer();
        String value=java.lang.System.getProperty(key);
        sb.append("\""+key+"\":\n");

        String arr[]=value.split(ps);

        int i=0;
        for (i=0;i<arr.length;i++)
        {
            sb.append("\t["+arr[i]+"]\n");
        }
        return sb.append("\n");
    }

    static void dumpClassLoaderInfos (Class clz, String title)
    {
        sb.append("\n------- dumpClassLoaderInfos() -------\n");
        ClassLoader contextCL =Thread.currentThread().getContextClassLoader();
        ClassLoader definingCL=clz.getClassLoader();
        ClassLoader systemCL  =ClassLoader.getSystemClassLoader();
        ClassLoader clzCL     = Class.class.getClassLoader();

System.err.println("contextCL= \t["+contextCL+"]");
System.err.println("definingCL=\t["+definingCL+"]");
System.err.println("systemCL=  \t["+systemCL+"]");
System.err.println("clzCL=     \t["+clzCL+"]");

        sb.append("definingCL=\t["+definingCL.toString()+"], parent: "+definingCL.getParent()+"],\n\thashCode=["+definingCL.hashCode()+"]\n");
        sb.append("systemCL  =\t["+systemCL.toString()+"], parent: "+systemCL.getParent()+"],\n\thashCode=["+systemCL.hashCode()+"]\n");
        // sb.append("contextCL =["+contextCL.toString()+"],\n\thashCode=["+contextCL.hashCode()+"]\n");

        sb.append("contextCL =\t[" +
                  (contextCL==null ? "null" : contextCL.toString()+"], parent: "+contextCL.getParent())+"],\n\thashCode=["+contextCL.hashCode() +
                  "]\n");

        sb.append("clzCL     =\t[" +
                  (clzCL==null ? "null" : clzCL.toString()+"], parent: "+clzCL.getParent()+"],\n\thashCode=["+clzCL.hashCode()) +
                  "]\n");
        sb.append("\n--------------------------------------\n");
    }
}

