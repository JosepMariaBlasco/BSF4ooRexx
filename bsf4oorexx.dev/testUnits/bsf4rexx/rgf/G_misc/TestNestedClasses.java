package rgf.G_misc;
// rgf, 2021-02-05: test embedding classes

// as of 2021-01-06
// cf. <https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html>
// cf. <https://docs.oracle.com/javase/tutorial/java/javaOO/localclasses.html>

public class TestNestedClasses
{
    // static String A_static_public;       // if defined, will hide access to static class of same name
    // String A_static_public;              // if defined, will hide access to static class of same name

    public static void main (String args[])
    {
        System.out.println(TestNestedClasses.class+".main(): arrived\n");

        System.out.println("\nTestNestedClasses.A_static_public=["+TestNestedClasses.A_static_public.class+"]");
        System.out.println("A_static_public.TODAY=["+A_static_public.TODAY+"]");
        System.out.println("A_static_public.A_static_public_Sub.BLUE=["+A_static_public.A_static_public_Sub.BLUE+"]");
        System.out.println("A_static_public.A_static_protected_Sub.BLUE=["+A_static_public.A_static_protected_Sub.BLUE+"]");
        System.out.println("A_static_public.A_static_private_Sub.BLUE=["+A_static_public.A_static_private_Sub.BLUE+"]");
        System.out.println("A_static_public.A_static_package_private_Sub.BLUE=["+A_static_public.A_static_package_private_Sub.BLUE+"]");


        System.out.println("\nTestNestedClasses.B_static_protected=["+TestNestedClasses.B_static_protected.class+"]");
        System.out.println("B_static_protected.TODAY=["+B_static_protected.TODAY+"]");
        System.out.println("B_static_protected.B_static_public_Sub.BLUE=["+B_static_protected.B_static_public_Sub.BLUE+"]");
        System.out.println("B_static_protected.B_static_protected_Sub.BLUE=["+B_static_protected.B_static_protected_Sub.BLUE+"]");
        System.out.println("B_static_protected.B_static_private_Sub.BLUE=["+B_static_protected.B_static_private_Sub.BLUE+"]");
        System.out.println("B_static_protected.B_static_package_private_Sub.BLUE=["+B_static_protected.B_static_package_private_Sub.BLUE+"]");


        System.out.println("\nTestNestedClasses.C_static_private=["+TestNestedClasses.C_static_private.class+"]");
        System.out.println("C_static_private.TODAY=["+C_static_private.TODAY+"]");
        System.out.println("C_static_private.C_static_public_Sub.BLUE=["+C_static_private.C_static_public_Sub.BLUE+"]");
        System.out.println("C_static_private.C_static_protected_Sub.BLUE=["+C_static_private.C_static_protected_Sub.BLUE+"]");
        System.out.println("C_static_private.C_static_private_Sub.BLUE=["+C_static_private.C_static_private_Sub.BLUE+"]");
        System.out.println("C_static_private.C_static_package_private_Sub.BLUE=["+C_static_private.C_static_package_private_Sub.BLUE+"]");


        System.out.println("\nTestNestedClasses.D_static=["+TestNestedClasses.D_static_package_private.class+"]");
        System.out.println("D_static_package_private.TODAY=["+D_static_package_private.TODAY+"]");
        System.out.println("D_static_package_private.D_static_public_Sub.BLUE=["+D_static_package_private.D_static_public_Sub.BLUE+"]");
        System.out.println("D_static_package_private.D_static_protected_Sub.BLUE=["+D_static_package_private.D_static_protected_Sub.BLUE+"]");
        System.out.println("D_static_package_private.D_static_private_Sub.BLUE=["+D_static_package_private.D_static_private_Sub.BLUE+"]");
        System.out.println("D_static_package_private.D_static_package_private_Sub.BLUE=["+D_static_package_private.D_static_package_private_Sub.BLUE+"]");


        System.out.println("\n"+TestNestedClasses.class+".main(): musings with inner class ");

        class XYZ   // inner class
        {
            int AHA = 5;
            int OHO = 6;
        }

        XYZ xyzObject = new XYZ();
        System.out.println("XYZ=["+XYZ.class+"]");
        System.out.println("-> XYZ.getName()=["+XYZ.class.getName()+"]");
        System.out.println("-> XYZ.getCanonicalName()=["+XYZ.class.getCanonicalName()+"]");
        System.out.println("-> XYZ.getSimpleName()=["+XYZ.class.getSimpleName()+"]");
        System.out.println("xyzObject.AHA=["+xyzObject.AHA+"]\n");

// -> only reflect inner classes from a class object, not its instances if
//    an invoke without arguments on a class object is sought (as if it was a static getter)

/* cannot compile, reference to static classes not via instance variable
        TestNestedClasses tc = new TestNestedClasses();
        System.out.println("\nnow working via an instance of TestNestedClasses, tc=["+tc+"]\n");

        System.out.println("tc.A_static_public=["+tA_static_public.AHA+"]");
        System.out.println("tc.A_static_public.TODAY=["+tc.A_static_public.TODAY+"]\n");

        System.out.println("tc.A_static_public_Sub=["+tc.A_static_public.A_static_public_Sub.class+"]");
        System.out.println("tc.A_static_public_Sub.BLUE=["+tc.A_static_public.A_static_public_Sub.BLUE+"]\n");
*/


        System.out.println(TestNestedClasses.class+".main(): leaving");
        System.out.println("\n----------------------------------------------------------------\n");

    }


    public static class A_static_public         // static scope
    {
        public static int TODAY = 1;
        public static int TOMORROW = 2;

        public static class A_static_public_Sub // static scope
        {
            static int BLUE = 3;
            static int GREEN = 4;
        }

        protected static class A_static_protected_Sub // static scope
        {
            static int BLUE = 30;
            static int GREEN = 40;
        }

        private static class A_static_private_Sub // static scope
        {
            static int BLUE = 300;
            static int GREEN = 400;
        }

                static class A_static_package_private_Sub // static package private scope
        {
            static int BLUE = 3000;
            static int GREEN = 4000;
        }

    }

    protected static class B_static_protected      // static scope
    {
        public static int TODAY = 5;
        public static int TOMORROW = 6;


        public static class B_static_public_Sub // static scope
        {
            static int BLUE = 7;
            static int GREEN = 8;
        }


        protected static class B_static_protected_Sub // static scope
        {
            static int BLUE = 70;
            static int GREEN = 80;
        }

        private static class B_static_private_Sub // static scope
        {
            static int BLUE = 700;
            static int GREEN = 800;
        }

                static class B_static_package_private_Sub // static package private scope
        {
            static int BLUE = 7000;
            static int GREEN = 8000;
        }

    }

    private static class C_static_private        // static scope
    {
        public static int TODAY = 9;
        public static int TOMORROW = 10;


        public static class C_static_public_Sub // static scope
        {
            static int BLUE = 11;
            static int GREEN = 12;
        }

        protected static class C_static_protected_Sub // static scope
        {
            static int BLUE = 110;
            static int GREEN = 120;
        }

        private static class C_static_private_Sub // static scope
        {
            static int BLUE = 1100;
            static int GREEN = 1200;
        }

                static class C_static_package_private_Sub // static package private scope
        {
            static int BLUE = 11000;
            static int GREEN = 12000;
        }

    }

    static class D_static_package_private         // static package private scope
    {
        public static int TODAY = 12;
        public static int TOMORROW = 13;


        public static class D_static_public_Sub // static scope
        {
            static int BLUE = 14;
            static int GREEN = 15;
        }

        protected static class D_static_protected_Sub // static scope
        {
            static int BLUE = 140;
            static int GREEN = 150;
        }

        private static class D_static_private_Sub // static scope
        {
            static int BLUE = 1400;
            static int GREEN = 1500;
        }

        static class D_static_package_private_Sub // static package private cope
        {
            static int BLUE = 14000;
            static int GREEN = 15000;
        }

    }


}
