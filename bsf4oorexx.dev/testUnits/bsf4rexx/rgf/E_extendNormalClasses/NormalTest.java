// rgf, 2009-09-04
// define a class with methods that test the primitives as arguments and return values
package rgf.E_extendNormalClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public class NormalTest
{
    // --------------------------------------------------------------------------------------
    public Object    dummy_Method (Object obj) // dummy method that only is used for testing
                                                        // proper creation of slotDir in callbacks
    {
        return obj;
    }

    // --------------------------------------------------------------------------------------
    public boolean   boolean_Method (boolean val)          // return inverse of argument
    {
        return !val;
    }

    public Boolean   Boolean_Method (Boolean val)
    {
        if (val.booleanValue())
        {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    // --------------------------------------------------------------------------------------
    public byte      byte_Method (byte val)                // return half the value of argument
    {
        return (byte)(val/2);
    }
    public Byte      Byte_Method (Byte val)
    {
        return new Byte(""+(byte)(val.byteValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public char      char_Method      (char val)          // return received argument unchanged
    {
        return val;
    }

    public Character Character_Method (Character val)
    {
        return val;
    }

    // --------------------------------------------------------------------------------------
    public short     short_Method      (short val)         // return half the value of argument
    {
        return (short) (val/2);
    }
    public Short     Short_Method      (Short val)
    {
        return new Short(""+(short)(val.shortValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public int       int_Method (int val)                  // return half the value of argument
    {
        return (int) (val/2);
    }
    public Integer   Integer_Method (Integer val)
    {
        return new Integer(""+(int)(val.intValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public long      long_Method (long val)                // return half the value of argument
    {
        return (long) (val/2L);
    }
    public Long      Long_Method (Long val)
    {
        return new Long(""+(long)(val.longValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public float     float_Method (float val)              // return received argument unchanged
    {
        return (float) (val/2);
    }
    public Float     Float_Method (Float val)
    {
        return new Float(""+(float)(val.floatValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public double    double_Method (double val)            // return received argument unchanged
    {
        return (double) (val/2);
    }
    public Double    Double_Method (Double val)
    {
        return new Double(""+(double)(val.doubleValue()/2));
    }

    // --------------------------------------------------------------------------------------
    public String    String_Method (String val)            // return last three chars of argument
    {
        int len=val.length();
        if (len>3)
        {
            return val.substring(len-3);
        }
        return val;
    }

    // --------------------------------------------------------------------------------------
    public Object    Object_Method (Object val)            // return received argument unchanged
    {
        return val;
    }


    // --------------------------------------------------------------------------------------
    public void      void_Method()                         // just return
    {
        return;
    }

    // --------------------------------------------------------------------------------------
    public String    all_args_Method(boolean b1, Boolean b2,
                     byte  b3,   Byte b4,
                     char  c1,   Character c2,
                     short s1,   Short s2,
                     int   i1,   Integer i2,
                     long  l1,   Long l2,
                     float f1,   Float f2,
                     double d1,  Double d2
                    )
    {
        return   ""+(b1 ? 1 : 0) +" "+ (b2==Boolean.TRUE ? 1 : 0)
               +" "+b3+" "+b4.byteValue()
               +" "+c1+" "+c2.charValue()
               +" "+s1+" "+s2.shortValue()
               +" "+i1+" "+i2.intValue()
               +" "+l1+" "+l2.longValue()
               +" "+f1+" "+f2.floatValue()
               +" "+d1+" "+d2.doubleValue();
    }

    // --------------------------------------------------------------------------------------
    public RexxProxy getTestRunner()   // return the test runner Rexx proxy (to allow for registering test results)
    {
        return null;
    }
}


