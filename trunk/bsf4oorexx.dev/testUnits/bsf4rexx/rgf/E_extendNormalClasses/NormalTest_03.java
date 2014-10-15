// rgf, 2009-08-31
// define an interface with methods that test the primitives as arguments and return values
package rgf.E_extendNormalClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public class NormalTest_03
{
    public    Object fld_O1=null;

    public   boolean fld_b1 = true;
    public   Boolean fld_b2 = Boolean.TRUE;
    public     byte  fld_b3 = -1;
    public      Byte fld_b4 = new Byte((byte)-1);
    public     char  fld_c1 = 'a';
    public Character fld_c2 = new Character('a');
    public     short fld_s1 = -1;
    public     Short fld_s2 = new Short((short)-1);
    public       int fld_i1 = -1;
    public   Integer fld_i2 = new Integer(-1);
    public      long fld_l1 = -1L;
    public      Long fld_l2 = new Long(-1L);
    public     float fld_f1 = -1.0F;
    public     Float fld_f2 = new Float(-1.0);
    public    double fld_d1 = -1.0D;
    public    Double fld_d2 = new Double(-1.0);

    // constructors (if given, must be always concrete)
    public NormalTest_03 ()       // default constructor
    {
        super();
    }

    // another constructor
    public NormalTest_03 (Object o1)
    {
        super();
        fld_O1=o1;
    }

    // a rather complex constructor:
    public NormalTest_03 (boolean b1, Boolean b2,
                              byte  b3,   Byte b4,
                              char  c1,   Character c2,
                              short s1,   Short s2,
                              int   i1,   Integer i2,
                              long  l1,   Long l2,
                              float f1,   Float f2,
                              double d1,  Double d2
                             )
    {
        super();
        fld_b1 = b1;
        fld_b2 = b2;
        fld_b3 = b3;
        fld_b4 = b4;
        fld_c1 = c1;
        fld_c2 = c2;
        fld_s1 = s1;
        fld_s2 = s2;
        fld_i1 = i1;
        fld_i2 = i2;
        fld_l1 = l1;
        fld_l2 = l2;
        fld_f1 = f1;
        fld_f2 = f2;
        fld_d1 = d1;
        fld_d2 = d2;
    }

    // concrete instance method
    public String    instance_String_Method (String val)   // return inverse of argument
    {
        return val+" instance_String_Method from Java";      // return inverse value
    }

    // concrete static method
    static public String    static_String_Method (String val)   // return inverse of argument
    {
        return val+" static_String_Method from Java";      // return inverse value
    }

    //*******************************************
    // --------------------------------------------------------------------------------------
    public Object    dummy_Method (Object obj) // dummy method that only is used for testing
                                                        // proper creation of slotDir in callbacks
    {
        return obj;
    }

    // --------------------------------------------------------------------------------------
    public Boolean   Boolean_Method (Boolean val)
    {
        if (val.booleanValue())
        {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

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

    public RexxProxy getTestRunner()   // return the test runner Rexx proxy (to allow for registering test results)
    {
        return null;
    }
}


