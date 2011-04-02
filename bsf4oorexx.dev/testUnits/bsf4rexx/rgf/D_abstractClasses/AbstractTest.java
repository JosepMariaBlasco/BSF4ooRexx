// rgf, 2009-08-26, 2009-09-05
// define an interface with methods that test the primitives as arguments and return values
package rgf.D_abstractClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public abstract class AbstractTest
{

    public abstract Object    dummy_Method (Object obj);// dummy method that only is used for testing
                                                        // proper creation of slotDir in callbacks

    public abstract boolean   boolean_Method (boolean val);         // return inverse of argument
    public abstract Boolean   Boolean_Method (Boolean val);

    public abstract byte      byte_Method (byte val);               // return half the value of argument
    public abstract Byte      Byte_Method (Byte val);

    public abstract char      char_Method      (char val);          // return received argument unchanged
    public abstract Character Character_Method (Character val);

    public abstract short     short_Method      (short val);        // return half the value of argument
    public abstract Short     Short_Method      (Short val);

    public abstract int       int_Method (int val);                 // return half the value of argument
    public abstract Integer   Integer_Method (Integer val);

    public abstract long      long_Method (long val);               // return half the value of argument
    public abstract Long      Long_Method (Long val);

    public abstract float     float_Method (float val);             // return received argument unchanged
    public abstract Float     Float_Method (Float val);

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


