package mtest1;

public interface I01B
{
    static public String myClassName = "interface-mtest1.I01B";


    static public String  getMyClassNameStatic()    // static method in interface
    {
        return myClassName;
    }

    default public String getMyClassName()          // default method in interface
    {
        return myClassName;
    }
}

