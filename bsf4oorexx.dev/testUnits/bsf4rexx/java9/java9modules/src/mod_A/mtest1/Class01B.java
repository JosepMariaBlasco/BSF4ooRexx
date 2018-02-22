package mtest1;

public class Class01B implements I01B
{
    public static String myClassName = "class-mtest1.Class01B";

    public static String myName1     = "from Class01B (static)";

    public        String myName2     = "from Class01B (instance)";

    static {
        String clzName="mtest1.Class01B";
        System.err.println("---> "+Class01B.class+": static block, trying to load \""+clzName+"\" --->");
            Class clz;
        try
        {
            clz=Class.forName(clzName);
            System.err.println("---  "+Class01B.class+": clz=["+clz+"], clz.toString()=["+clz.toString()+"]");
        }
        catch (Exception e)
        {
            System.err.println("... tsk :-(, could not load \""+clzName+"\" exception=["+e.toString()+"]");

        }
        System.err.println("<--- "+Class01B.class+": static block <---");
    }

    public static void main (String args[]) {
        System.err.println(Class01B.class+".main() ...");
        System.err.println("       getMyClassNameStatic()=["+  getMyClassNameStatic()+"]");
        System.err.println("       getMyName1()          =["+  getMyName1()          +"]");
        Class01B o=new Class01B();
        System.err.println("     o.getMyName2()          =["+o.getMyName2()          +"]");
        System.err.println("     o.getMyClassName()      =["+o.getMyClassName()      +"]");
    }

    static public String  getMyClassNameStatic()    // static method in interface
    {
        return myClassName;
    }

/*
    public String getMyClassName()                  // default method in interface
    {
        return myClassName;
    }
*/

    static public String getMyName1()
    {
        return myName1;
    }

    public String getMyName2()
    {
        return myName2;
    }


}
