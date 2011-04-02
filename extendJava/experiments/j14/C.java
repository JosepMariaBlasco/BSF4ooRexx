class C extends B
{
    public static void main (String args[])
    {
        System.err.println(C.staticSomeString("from class C's main() method, using C.staticMethod(...)"));
        System.err.println(B.staticSomeString("from class C's main() method, using B.staticMethod(...)"));
        System.err.println(A.staticSomeString("from class C's main() method, using A.staticMethod(...)"));

        System.err.println("---");

        C c=new C();
        System.err.println(c.tus("from class C's main() method"));
    }

    public String tus (String string)
    {
        return super.instanceSomeString("in class C, received: ["+string+"].");
    }


    public static String staticSomeString (String string)
    {
        return "in class C.staticSomeString(String), received: ["+string+"].";
    }

}

/*
------------------

class B extends A
{
    public static void main (String args[])
    {
        System.err.println(B.staticSomeString("from class B's main() method, using B.staticMethod(...)"));
        System.err.println(A.staticSomeString("from class B's main() method, using A.staticMethod(...)"));

        System.err.println("---");

        B b=new B();
        System.err.println(b.instanceSomeString("from class B's main() method"));
    }

    public String instanceSomeString (String string)
    {
        return super.instanceSomeString("in class B.instanceSomeString(String), received: ["+string+"].");
    }

    public static String staticSomeString (String string)
    {
        return "in class B.staticSomeString(String), received: ["+string+"].";
    }

}
*/
