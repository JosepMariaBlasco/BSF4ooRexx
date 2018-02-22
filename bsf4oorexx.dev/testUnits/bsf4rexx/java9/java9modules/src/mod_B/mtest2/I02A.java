package mtest2;

public interface I02A extends mtest1.I01A
{
    // public static String myClassName = "interface-mtest2.I02A";


    static public String  getMyClassNameStatic()    // static method in interface
    {
        System.err.println("\t<<<(static public getMyClassNameStatic() method from ["+mtest2.I02A.class+"])>>>");
        return "interface-mtest2.I02A"; // myClassName;
    }

    default public String getMyClassName()          // default method in interface
    {
        System.err.println("\t<<<(default public getMyClassName() method from ["+mtest2.I02A.class+"])>>>");
        return "interface-mtest2.I02A"; // myClassName;
    }
}
