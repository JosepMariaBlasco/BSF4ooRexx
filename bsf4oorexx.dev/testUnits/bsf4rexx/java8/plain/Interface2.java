package java8.plain;

public interface Interface2 extends Interface1
{
    static int    INT_i2   =22;
    static String STRING_i2="ISTRING_22 (static)";

    // add: static methods + default methods (since Java 1.8)
    static String interface2staticMethod()
    {
        return "interface2static";
    }

    default String interface2defaultMethod()
    {
        return "interface2default";
    }
}
