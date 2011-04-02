class A
{
    public static void main (String args[])
    {
        System.err.println(A.staticSomeString("from class A's main() method, using A.staticMethod(...)"));
        System.err.println(  staticSomeString("from class A's main() method, using   staticMethod(...)"));

        System.err.println("---");
        A a=new A();
        System.err.println(a.instanceSomeString("from class A's main() method, using instanc3"));

    }

    public String instanceSomeString (String string)
    {
        return "in class A.instanceSomeString(String), received: ["+string+"].";
    }


    public static String staticSomeString (String string)
    {
        return "in class A.staticSomeString(String), received: ["+string+"].";
    }


}
