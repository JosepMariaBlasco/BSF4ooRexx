import java.lang.reflect.Method;


class UseTest
{
    public static void main (String args[])
    {
        Test o=new Test();
        Method [] m=o.getClass().getMethods();
        System.out.println(m.length+" method(s):");

        for(int i=0; i<m.length; i++)
        {
            System.out.println(i+": "+m[i].hashCode()+"/"+
                               +System.identityHashCode(m[i])+
                               " - "+m[i].toString());
            Class [] params=m[i].getParameterTypes();
            if (params.length>0)
            {
                System.out.println("\t"+params.length+" parameter(s):");
                for (int k=0; k<params.length; k++)
                {
                    System.out.println("\t\t"+k+": "+params[k].toString());
                }
            }
        }
    System.out.println("---");
    String a="Hallo";
    String b="Hallo";

    System.out.println("hash()        : a="+a.hashCode()+", b="+b.hashCode());
    System.out.println("identityHash(): a="+System.identityHashCode(a)+", b="+System.identityHashCode(b));
    System.out.println("---");
    System.out.println("identityHash(): a="+System.identityHashCode(a.toString())+", b="+System.identityHashCode(b.toString()));



    }
}

/*
o=.bsf~new("Test")

m=o~getClass~getMethods
tab="09"x
tab2="0909"x

say m~items "methods:"

do i=1 to m~items
   mo=m[i]
   say i":" tab mo~toString
   p=mo~getParameterTypes
   if p~items>0 then
      say tab p~items "parameter(s):"
   do k=1 to p~items
      say tab2 p[k]~toString
   end
end
say "---"
m=o~getClass~getDeclaredMethod("aha", .nil)
say "m:         " m
say "m~toString:" m~toString
say "now attempting to invoke method:"
m~invoke(o, .nil)

say "---"
clz=bsf.createArray("java.lang.Class", 1);
clz[1]=.bsf4rexx~integer.class
m=o~getClass~getDeclaredMethod("aha", clz)
say "m:         " m
say "m~toString:" m~toString

args=bsf.createArray("java.lang.Object", 1)
args[1]=box("int",1)
say "now attempting to invoke method:"
m~invoke(o, args)


::requires bsf.cls
*/
