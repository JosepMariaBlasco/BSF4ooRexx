package mtest1;

abstract public class Class01A implements I01A
{
    // protected static String myClassName = "class-mtest1.Class01A";
    public    static String myClassName = "class-mtest1.Class01A";

    protected static String myName1     = "from Class01A (static)";

    protected     String myName2        = "from Class01A (instance)";

    public static void main (String args[]) {
        System.err.println(Class01A.class+".main() ...");
    }

    public static String  getMyClassNameStatic()    // static method in interface
    {
        return myClassName;
    }

    abstract public String getMyClassName() ;       // default method in interface

    static   protected String getMyName1()
    {
        return myName1;
    }

    abstract protected String getMyName2();
}
