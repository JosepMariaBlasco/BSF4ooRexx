package java8.plain;

public interface Interface1
{
    static int    INT_i1   =11;
    static String STRING_i1="ISTRING_11 (static)";

    // add: static methods + default methods (since Java 1.8)
    static String interface1staticMethod()
    {
        return "interface1static";
    }

    static String i1sm_returning_arg(String val)
    {
        return val;
    }

    default String interface1defaultMethod()
    {
        return "interface1default";
    }
}
