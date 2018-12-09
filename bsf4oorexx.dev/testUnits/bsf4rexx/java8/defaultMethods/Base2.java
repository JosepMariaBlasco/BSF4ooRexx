package java8.defaultMethods;
// implements static   method   staticMeth(String s)



public class Base2
{
    public static void main (String args[])
    {
        Base2 o=new Base2();

        System.out.println("o=["+o+"]"
                           +", o.staticMeth(\"via o\")=["+o.staticMeth("via o")+"]"
                           );

    }

    public static String staticMeth (String s)
    {
        // return "from_Base2.staticMeth("+s+")";
        return "|--///Base2.staticMeth(s=["+s+"])\\\\\\--|";
    }



}

