package mtest2;

public class Class02A extends mtest1.Class01A implements I02A
{
    public static String myName1     = "from Class02A (static)";

    public        String myName2     = "from Class02A (instance)";

    public static void main (String args[]) {
        System.err.println(Class02A.class+".main() ...");

        System.err.println("       getMyClassNameStatic()=["+  getMyClassNameStatic()+"]");
        System.err.println("       getMyName1()          =["+  getMyName1()          +"]");
        Class02A o=new Class02A();
        System.err.println("     o.getMyName2()          =["+o.getMyName2()          +"]");
        System.err.println("     o.getMyClassName()      =["+o.getMyClassName()      +"]");
    }

    public String getMyClassName()        // default method in interface
    {
        return myClassName;
    }

    protected String getMyName2()
    {
        return myName2;
    }
}
