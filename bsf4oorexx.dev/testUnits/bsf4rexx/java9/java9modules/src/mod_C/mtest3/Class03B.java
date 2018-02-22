package mtest3;

public class Class03B extends mtest2.Class02B
{

    static {
        String clzName="mtest1.Class01A";
        System.err.println("---> "+Class03B.class+": static block, trying to load \""+clzName+"\" --->");
        Class clz;
        try
        {
            clz=Class.forName(clzName);
            System.err.println("---  "+Class03B.class+": success, could load clz=["+clz+"], clz.toString()=["+clz.toString()+"]");
        }
        catch (Exception e)
        {
            System.err.println("... tsk :-(, could not load \""+clzName+"\" exception=["+e.toString()+"]");

        }
        System.err.println("<--- "+Class03B.class+": static block <---");
    }

    // public static String myClassName = "class-mtest3.Class03B";

    public static void main (String args[]) {
        System.err.println(Class03B.class+".main() ...");

        System.err.println("       getMyClassNameStatic()=["+  getMyClassNameStatic()+"], yup, from superclass!");
        System.err.println("       getMyName1()          =["+  getMyName1()          +"], yup, from superclass!");
        Class03B o=new Class03B();
        System.err.println("     o.getMyName2()          =["+o.getMyName2()          +"]");
    }

/*
    public String getMyClassName()                  // default method in interface
    {
        return myClassName;
    }
*/

}
