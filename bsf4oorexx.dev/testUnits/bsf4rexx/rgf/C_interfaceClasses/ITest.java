// rgf, 2009-08-26
// define an interface with methods that test the primitives as arguments and return values
package rgf.C_interfaceClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public interface ITest
{
    Object    dummy_Method (Object obj);            // dummy method that only is used for testing
                                                    // proper creation of slotDir in callbacks

    boolean   boolean_Method (boolean val);         // return inverse of argument
    Boolean   Boolean_Method (Boolean val);

    byte      byte_Method (byte val);               // return half the value of argument
    Byte      Byte_Method (Byte val);

    char      char_Method      (char val);          // return received argument unchanged
    Character Character_Method (Character val);

    short     short_Method      (short val);        // return half the value of argument
    Short     Short_Method      (Short val);

    int       int_Method (int val);                 // return half the value of argument
    Integer   Integer_Method (Integer val);

    long      long_Method (long val);               // return half the value of argument
    Long      Long_Method (Long val);

    float     float_Method (float val);             // return received argument unchanged
    Float     Float_Method (Float val);

    double    double_Method (double val);           // return received argument unchanged
    Double    Double_Method (Double val);

    String    String_Method (String val);           // return last three chars of argument

    Object    Object_Method (Object val);           // return received argument unchanged


    void      void_Method();                        // just return

    String    all_args_Method(boolean b1, Boolean b2,
                              byte b3,    Byte b4,
                              char c1,    Character c2,
                              short s1,   Short s2,
                              int   i1,   Integer i2,
                              long  l1,   Long l2,
                              float f1,   Float f2,
                              double d1,  Double d2
                             );

    RexxProxy getTestRunner();  // return the test runner Rexx proxy (to allow for registering test results)
}


