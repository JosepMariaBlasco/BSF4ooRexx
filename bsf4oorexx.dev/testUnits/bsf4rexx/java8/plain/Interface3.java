package java8.plain;

public interface Interface3 extends Interface2
{
    static int    INT_i3   =33;
    static String STRING_i3="ISTRING_33 (static)";

    // add: static methods + default methods (since Java 1.8)
    static String interface3staticMethod()
    {
        return "interface3static->"+Interface2.interface2staticMethod()+"->"+Interface1.interface1staticMethod();
    }

    default String interface3defaultMethod()
    {
        return "interface3default";
    }
}
