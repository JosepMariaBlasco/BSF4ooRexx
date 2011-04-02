// 2009-07-25, ---rgf
// define an abstract class with abstract methods only

import java.lang.IllegalArgumentException;
import java.lang.NoSuchMethodException;



public abstract class TestAbstract
{
    public abstract void    test_VoidVoid();                                               // #1

    // test methods returning a primitive value
    public abstract boolean test_Void_boolean();                                           // #2
    public abstract byte    test_Void_byte   ();                                           // #3
    public abstract char    test_Void_char   ();                                           // #4
    public abstract short   test_Void_short  ();                                           // #5
    public abstract int     test_Void_int    ();                                           // #6
    public abstract long    test_Void_long   ();                                           // #7
    public abstract float   test_Void_float  ();                                           // #8
    public abstract double  test_Void_double ();                                           // #9

    // test methods returning a value (note capital letters in type name)
    public abstract Boolean   test_Void_Boolean  ();                                       // #10
    public abstract Byte      test_Void_Byte     ();                                       // #11
    public abstract Character test_Void_Character ();                                      // #12
    public abstract Short     test_Void_Short    ();                                       // #13
    public abstract Integer   test_Void_Integer  ();                                       // #14
    public abstract Long      test_Void_Long     ();                                       // #15
    public abstract Float     test_Void_Float    ();                                       // #16
    public abstract Double    test_Void_Double   ();                                       // #17
    public abstract Object    test_Void_Object   ();                                       // #18


    // test methods accepting a primitive value, returning a primitive value
    public abstract boolean  test_boolean_boolean    (boolean bo);                         // #19
    public abstract byte     test_byte_byte          (byte by);                            // #20
    public abstract char     test_char_char          (char c);                             // #21
    public abstract short    test_short_short        (short s);                            // #22
    public abstract int      test_int_int            (int i);                              // #23
    public abstract long     test_long_long          (long l);                             // #24
    public abstract float    test_float_float        (float f);                            // #25
    public abstract double   test_double_double      (double d);                           // #26


    // test methods accepting a wrapper value, returning a primitive value
    public abstract boolean  test_Boolean_boolean    (Boolean bo);                         // #27
    public abstract byte     test_Byte_byte          (Byte by);                            // #28
    public abstract char     test_Character_char     (Character c);                        // #29
    public abstract short    test_Short_short        (Short s);                            // #30
    public abstract int      test_Integer_int        (Integer i);                          // #31
    public abstract long     test_Long_long          (Long l);                             // #32
    public abstract float    test_Float_float        (Float f);                            // #33
    public abstract double   test_Double_double      (Double d);                           // #34


    // test methods accepting a wrapper value, returning a wrapper value
    public abstract Boolean   test_Boolean_Boolean    (Boolean bo);                        // #35
    public abstract Byte      test_Byte_Byte          (Byte by);                           // #36
    public abstract Character test_Character_Character(Character c);                       // #37
    public abstract Short     test_Short_Short        (Short s);                           // #38
    public abstract Integer   test_Integer_Integer    (Integer i);                         // #39
    public abstract Long      test_Long_Long          (Long l);                            // #40
    public abstract Float     test_Float_Float        (Float f);                           // #41
    public abstract Double    test_Double_Double      (Double d);                          // #42
    public abstract Object    test_Object_Object      (Object o);                          // #43

    // test methods with many arguments
    public abstract void      test_Void_ManyArguments (String s,
           boolean bo1, byte by1, char c1,      short s1, int i1,     long l1, float f1, double d1,
           Boolean bo2, Byte by2, Character c2, Short s2, Integer i2, Long l2, Float f2, Double d2,
           Object o);



    // ------------------------ concrete methods, including static ones
    public void    test_concrete_VoidVoid() throws IllegalArgumentException, NoSuchMethodException
    {
        System.out.println("-> \"test_concrete_VoidVoid()\"");
        return;
    }

    // test methods returning a primitive value
    public boolean test_concrete_Void_boolean()                                            // #2
    {
        System.out.println("-> \"test_concrete_Void_boolean()\"");
        return true;
    }

    // test methods returning a value (note capital letters in type name)
    public Boolean   test_concrete_Void_Boolean  ()                                       // #10
    {
        System.out.println("-> \"test_concrete_Void_Boolean()\"");
        return Boolean.TRUE;
    }


    // test methods accepting a primitive value, returning a primitive value
    public boolean  test_concrete_boolean_boolean    (boolean bo)                          // #19
    {
        System.out.println("-> \"test_concrete_boolean_boolean()\"");
        return bo;
    }

    // test methods accepting a wrapper value, returning a primitive value
    public boolean  test_concrete_Boolean_boolean    (Boolean bo)                          // #27
    {
        System.out.println("-> \"test_concrete_Boolean_Boolean()\"");
        return bo.booleanValue();
    }

    // test methods with many arguments
    public Object test_concrete_Void_ManyArguments (String s,
           boolean bo1, byte by1, char c1,      short s1, int i1,     long l1, float f1, double d1,
           Boolean bo2, Byte by2, Character c2, Short s2, Integer i2, Long l2, Float f2, Double d2,
           Object o)
    {
        return ""+bo1+by1+c1+s1+i1+l1+f1+f1+d1+
                  bo2+by2+c2+s2+i2+l2+f2+f2+d2+ o;
    }


}
