package java8.defaultMethods;

public interface Interface
{
    static String staticMeth (String s)
    {
        // return "\"Interface.staticMeth<\""+s+"\">";

        return "|--///Interface.staticMeth(s=["+s+"])\\\\\\--|";
    }

    default String instanceMeth ()
    {
        // return staticMeth("from_dynamic_Interface.instanceMeth()");

        return "{Instance.instanceMeth<<<" + staticMeth("<Instance.instanceMeth()>")+">>>}";
    }

}
