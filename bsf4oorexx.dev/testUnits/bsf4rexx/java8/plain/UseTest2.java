import java8.plain.Test1;
import java8.plain.Test2;
import java8.plain.Interface1;
import java8.plain.Interface2;
import java8.plain.Interface3;

// import java8.plain.*;

public class UseTest2
{

    public static void main (String args[]) {
        System.out.println(UseTest2.class+" in main() method...");

        Test2 obj1=new Test2();

        System.out.println("\nobj1="+obj1+":\n");

        System.out.println("obj1.STRING_i1 ="+obj1.STRING_i1);
        System.out.println("obj1.STRING_i2 ="+obj1.STRING_i2);
        System.out.println("obj1.STRING_i3 ="+obj1.STRING_i3);
        System.out.println("obj1.INT_T1    ="+obj1.INT_T1);
        System.out.println("obj1.INT_i1    ="+obj1.INT_i1);
        System.out.println("obj1.INT_i2    ="+obj1.INT_i2+"\n");
        // System.out.println("obj1.INT_i3   ="+obj1.INT_i3); // cannot access "private" in Test1
        System.out.println("obj1.INT_T2    ="+obj1.INT_T2+" (static)");
        obj1.INT_T2=291;
        System.out.println("obj1.INT_T2    ="+obj1.INT_T2+" (static)\n");

        System.out.println("obj1.INT_T2_obj="+obj1.INT_T2_obj);
        obj1.INT_T2_obj=294;
        System.out.println("obj1.INT_T2_obj="+obj1.INT_T2_obj+"\n");

        // Test1:
        System.out.println("obj1.INT_T1    ="+obj1.INT_T1+" (static)");
        obj1.INT_T1=191;
        System.out.println("obj1.INT_T1    ="+obj1.INT_T1+" (static)\n");

        System.out.println("obj1.INT_i1    ="+obj1.INT_i1);
        obj1.INT_i1=191;
        System.out.println("obj1.INT_i1    ="+obj1.INT_i1+"\n");

        // System.out.println("obj1.INT_i3    ="+obj1.INT_i3+"\n"); // <-- does not compile, because it exists as private field in Test2 !
        System.out.println("obj1.INT_i3    ="+((Interface3)obj1).INT_i3+"\n"); // this does compile!
        System.out.println("----");

        try
        {
            Object objx=obj1.getClass().getField("INT_i2");
            System.out.println(Test1.class+": getField(\"INT_i2\")="+objx+", declared in: "+((java.lang.reflect.Field)objx).getDeclaringClass());
                // will not be found in the class, need to iterate over Interfaces "manually"
            objx=obj1.getClass().getDeclaredField("INT_i3");        // at runtime: NoSuchFieldException
            System.out.println(Test1.class+": getDeclaredField(\"INT_i3\")="+objx);
        }
        catch (Throwable t)
        {
            System.err.println("Throwable "+t+":");
            t.printStackTrace();
            System.exit(-1);
        }

        System.out.println();
        System.out.println("obj1.interface1defaultMethod()="+obj1.interface1defaultMethod());
        System.out.println("Interface1.interface1staticMethod ()="+Interface1.interface1staticMethod ());
        System.out.println();
        System.out.println("obj1.interface2defaultMethod()="+obj1.interface2defaultMethod());
        System.out.println("Interface2.interface2staticMethod ()="+Interface2.interface2staticMethod ());
        System.out.println();
        System.out.println("obj1.interface3defaultMethod()="+obj1.interface3defaultMethod());
        System.out.println("Interface3.interface3staticMethod ()="+Interface3.interface3staticMethod ());

        System.out.println();
        System.out.println("obj1.test2infoStatic        ()="+obj1.test2infoStatic());
        System.out.println("obj1.test2info              ()="+obj1.test2info      ());

        System.out.println();
        System.out.println("obj1.test1infoStatic        ()="+obj1.test1infoStatic());
        System.out.println("obj1.test1info              ()="+obj1.test1info      ());

        System.out.println();
        System.out.println("Interface1.i1sm_returning_arg(\"hey\")()="+Interface1.i1sm_returning_arg("hey"));


        System.out.println("\n---various tests---\n");
        System.out.println("boolean.class.isAssignable(Boolean.class)="+boolean.class.isAssignableFrom(Boolean.class));
        System.out.println("Boolean.class.isAssignable(boolean.class)="+Boolean.class.isAssignableFrom(boolean.class));
        System.out.println("\nBoolean.TRUE instanceof Boolean="+(Boolean.TRUE instanceof Boolean));

        // boolean val=true;
        // System.out.println("boolean.class.isAssignable(val.getClass())="+boolean.class.isAssignableFrom(val.getClass()));
        // System.out.println("boolean.class.isAssignable(val.getClass())="+val instanceof boolean.class);

    }
}
