// rgf, 2009-08-31, 2009-09-05
// define an interface with methods that test the primitives as arguments and return values
package rgf.D_abstractClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public abstract class AbstractTest_03
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
    public AbstractTest_03 ()       // default constructor
    {
        super();
    }

    // another constructor
    public AbstractTest_03 (Object o1)
    {
        super();
        fld_O1=o1;
    }

    // a rather complex constructor:
    public AbstractTest_03 (boolean b1, Boolean b2,
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
    public String    instance_String_Method (String val)    // return inverse of argument
    {
        return val+" instance_String_Method from Java";      // return inverse value
    }

    // concrete static method
    static public String    static_String_Method (String val)   // return inverse of argument
    {
        return val+" static_String_Method from Java";        // return inverse value
    }


    // abstract methods

    public abstract Object    dummy_Method (Object obj);// dummy method that only is used for testing
                                                        // proper creation of slotDir in callbacks

    public abstract Boolean   Boolean_Method (Boolean val);

    public abstract double    double_Method (double val);           // return received argument unchanged
    public abstract Double    Double_Method (Double val);

    public abstract String    String_Method (String val);           // return last three chars of argument

    public abstract Object    Object_Method (Object val);           // return received argument unchanged

    public abstract void      void_Method();                        // just return

    public abstract String    all_args_Method(boolean b1, Boolean b2,
                              byte  b3,   Byte b4,
                              char  c1,   Character c2,
                              short s1,   Short s2,
                              int   i1,   Integer i2,
                              long  l1,   Long l2,
                              float f1,   Float f2,
                              double d1,  Double d2
                             );

    public abstract RexxProxy getTestRunner();  // return the test runner Rexx proxy (to allow for registering test results)
}


