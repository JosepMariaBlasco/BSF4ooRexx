package mtest1;

public interface I01A
{
    // public static String myClassName = "interface-mtest1.I01A";


    static public String  getMyClassNameStatic()    // static method in interface
    {
        System.err.println("\t<<<(static public getMyClassNameStatic() method from ["+mtest1.I01A.class+"])>>>");
        return "interface-mtest1.I01A"; // myClassName;
    }

    default public String getMyClassName()          // default method in interface
    {
        System.err.println("\t<<<(default public getMyClassName() method from ["+mtest1.I01A.class+"])>>>");
        return "interface-mtest1.I01A"; // myClassName;
    }
}
