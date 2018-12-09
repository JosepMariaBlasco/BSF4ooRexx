package java8.defaultMethods;
// implements instance method instanceMeth()


public class Base3
{
    public static void main (String args[])
    {
        Base3 o=new Base3();

        System.out.println("o=["+o+"]"
                           +", o.instanceMeth()=["+o.instanceMeth() +"]"
                           );

    }


    public String instanceMeth ()
    {
        return "<Base3.instanceMeth()>";
        // return "{from Base3.instanceMeth<<<" + Base3.instanceMeth("from_<Base3.instanceMeth()>")+">>>}";
    }

}

