import java.util.ArrayList;
import java.util.HashSet;
import org.objectweb.asm.Type;
import org.rexxla.bsf.engines.rexx.RexxException;

class TestType
{
    public static void main (String args[])
    {
        Object []strTypes= { "java/lang/Boolean", "java/lang/Byte", "java/lang/Character",

                             "java/util/HashSet", "org/rexxla/bsf/engines/rexx/RexxException",
                             "max/und/moritz", "[boolean", "[[[java/lang/Byte",
                             Type.BOOLEAN_TYPE, Type.BYTE_TYPE, Type.CHAR_TYPE,
                             Type.DOUBLE_TYPE, Type.FLOAT_TYPE, Type.INT_TYPE,
                             Type.LONG_TYPE,   Type.SHORT_TYPE, Type.VOID_TYPE

                             };

        for (int i=0; i<strTypes.length; i++)
        {
            Type t1=null;
            if (strTypes[i] instanceof String)
            {
                t1=Type.getObjectType((String) strTypes[i]);
            }
            else
            {
                t1=(Type) strTypes[i];
            }

            System.out.println("strTypes["+i+"]="+t1);
            System.out.println("\tgetSort()="+t1.getSort()+", getDescriptor()="+t1.getDescriptor()+", getSize()="+t1.getSize());

            int sort=t1.getSort();
            String info="";

            if (sort==Type.ARRAY)
            {
                info=info+"getDimensions()="+t1.getDimensions();
                // info=info+"getElementType()="+t1.getElementType();
            }
            else
            {
                if (sort==Type.OBJECT)
                {
                    info=info+"getInternalName()="+t1.getInternalName()+", ";
                }
                info=info+"getClassName()="+t1.getClassName();
            }

            System.out.println("\t"+info);
        }
    }
}
