package java8.defaultMethods;
// implements static   method   staticMeth(String s)
// implements instance method instanceMeth()


public class Base1
{
    public static void main (String args[])
    {
        Base1 o=new Base1();

        System.out.println("staticMeth? [1], instanceMeth? [1]\n"
                           +
        "o=["+o+"]"
                           +", o.instanceMeth()=["+o.instanceMeth() +"]"
                           +", o.staticMeth(\"via o\")=["+o.staticMeth("via o")+"]"
                           );

    }

    public static String staticMeth (String s)
    {
        return "|--///Base1.staticMeth(s=["+s+"])\\\\\\--|";
    }


    public String instanceMeth ()
    {
        return "{Base1.instanceMeth<<<" + staticMeth("<Base1.instanceMeth()>")+">>>}";
    }

}

