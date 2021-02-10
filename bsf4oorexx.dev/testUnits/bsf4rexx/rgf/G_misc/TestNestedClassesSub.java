package rgf.G_misc;

public class TestNestedClassesSub extends TestNestedClasses {
    public static void main (String args[]) {
        TestNestedClassesSub tcs=new TestNestedClassesSub();
        System.out.println("tcs=["+tcs+"]");

        System.out.println(TestNestedClassesSub.class+".main(): arrived\n");

        System.out.println("\nTestNestedClasses.A_static_public=["+TestNestedClasses.A_static_public.class+"]");
        System.out.println("TestNestedClassesSub.A_static_public=["+TestNestedClassesSub.A_static_public.class+"]");
        System.out.println("A_static_public.TODAY=["+A_static_public.TODAY+"]");
        System.out.println("A_static_public.A_static_public_Sub.BLUE=["+A_static_public.A_static_public_Sub.BLUE+"]");
        System.out.println("A_static_public.A_static_protected_Sub.BLUE=["+A_static_public.A_static_protected_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("A_static_public.A_static_private_Sub.BLUE=["+A_static_public.A_static_private_Sub.BLUE+"]");
        System.out.println("A_static_public.A_static_package_private_Sub.BLUE=["+A_static_public.A_static_package_private_Sub.BLUE+"]");


        System.out.println("\nTestNestedClasses.B_static_protected=["+TestNestedClasses.B_static_protected.class+"]");
        System.out.println("TestNestedClassesSub.B_static_protected=["+TestNestedClassesSub.B_static_protected.class+"]");
        System.out.println("B_static_protected.TODAY=["+B_static_protected.TODAY+"]");
        System.out.println("B_static_protected.B_static_public_Sub.BLUE=["+B_static_protected.B_static_public_Sub.BLUE+"]");
        System.out.println("B_static_protected.B_static_protected_Sub.BLUE=["+B_static_protected.B_static_protected_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("B_static_protected.B_static_private_Sub.BLUE=["+B_static_protected.B_static_private_Sub.BLUE+"]");
        System.out.println("B_static_protected.B_static_package_private_Sub.BLUE=["+B_static_protected.B_static_package_private_Sub.BLUE+"]");


// cannot access from subclass:        System.out.println("\nTestNestedClasses.C_static_private=["+TestNestedClasses.C_static_private.class+"]");
// cannot access from subclass:        System.out.println("C_static_private.TODAY=["+C_static_private.TODAY+"]");
// cannot access from subclass:        System.out.println("C_static_private.C_static_public_Sub.BLUE=["+C_static_private.C_static_public_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("C_static_private.C_static_protected_Sub.BLUE=["+C_static_private.C_static_protected_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("C_static_private.C_static_private_Sub.BLUE=["+C_static_private.C_static_private_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("C_static_private.C_static_package_private_Sub.BLUE=["+C_static_private.C_static_package_private_Sub.BLUE+"]");


        System.out.println("\nTestNestedClasses.D_static=["+TestNestedClasses.D_static_package_private.class+"]");
        System.out.println("TestNestedClassesSub.D_static=["+TestNestedClassesSub.D_static_package_private.class+"]");
        System.out.println("D_static_package_private.TODAY=["+D_static_package_private.TODAY+"]");
        System.out.println("D_static_package_private.D_static_public_Sub.BLUE=["+D_static_package_private.D_static_public_Sub.BLUE+"]");
        System.out.println("D_static_package_private.D_static_protected_Sub.BLUE=["+D_static_package_private.D_static_protected_Sub.BLUE+"]");
// cannot access from subclass:        System.out.println("D_static_package_private.D_static_private_Sub.BLUE=["+D_static_package_private.D_static_private_Sub.BLUE+"]");
        System.out.println("D_static_package_private.D_static_package_private_Sub.BLUE=["+D_static_package_private.D_static_package_private_Sub.BLUE+"]");

        System.out.println("\n"+TestNestedClassesSub.class+".main(): leaving");

        System.out.println("\n----------------------------------------------------------------\n");

    }
}
