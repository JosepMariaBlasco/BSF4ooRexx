package mtest2;

interface I02B extends mtest1.I01B
{
    static public String myClassName = "interface-mtest.I02B";


    static public String  getMyClassNameStatic()    // static method in interface
    {
        System.err.println("\t<<<(static public getMyClassNameStatic() method from ["+mtest2.I02B.class+"])>>>");
        return myClassName;
    }

    default public String getMyClassName()          // default method in interface
    {
        System.err.println("\t<<<(default public getMyClassName() method from ["+mtest2.I02B.class+"])>>>");
        return myClassName;
    }


}
