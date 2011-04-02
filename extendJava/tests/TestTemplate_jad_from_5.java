// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TestTemplate.java

import org.rexxla.bsf.engines.rexx.RexxProxy;

class TestTemplate
{

    public static RexxProxy getDefaultHandler()
    {
        return defaultHandler;
    }

    public static void setDefaultHandler(RexxProxy rexxproxy)
    {
        synchronized(classLock)
        {
            defaultHandler = rexxproxy;
        }
    }

    TestTemplate(RexxProxy rexxproxy)
    {
        target = null;
        target = rexxproxy;
    }

    TestTemplate()
    {
        target = null;
        target = defaultHandler;
    }

    void testVoid()
        throws Throwable
    {
        String s = "testVoid";
        Object obj = null;
        if(target == null)
            defaultHandler.invoke(s, obj);
        else
            target.invoke(s, obj);
    }

    void testPrimitiveBooleanVoid_1(boolean flag)
        throws Throwable
    {
        String s = "testPrimitiveBooleanVoid_1";
        Object aobj[] = {
            Boolean.valueOf(flag)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    boolean testPrimitiveBooleanPrimitiveBoolean_1(boolean flag)
        throws Throwable
    {
        String s = "testPrimitiveBooleanPrimitiveBoolean_1";
        Object aobj[] = {
            Boolean.valueOf(flag)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Boolean((String)obj);
        if(!(obj instanceof Boolean))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Boolean).append("'.").toString());
        else
            return ((Boolean)obj).booleanValue();
    }

    void testPrimitiveByteVoid_1(byte byte0)
        throws Throwable
    {
        String s = "testPrimitiveByteVoid_1";
        Object aobj[] = {
            new Byte(byte0)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    byte testPrimitiveBytePrimitiveByte_1(byte byte0)
        throws Throwable
    {
        String s = "testPrimitiveBytePrimitiveByte_1";
        Object aobj[] = {
            new Byte(byte0)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Byte((String)obj);
        if(!(obj instanceof Byte))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Byte).append("'.").toString());
        else
            return ((Byte)obj).byteValue();
    }

    void testPrimitiveCharacterVoid_1(char c)
        throws Throwable
    {
        String s = "testPrimitiveCharacterVoid_1";
        Object aobj[] = {
            new Character(c)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    char testPrimitiveCharacterPrimitiveCharacter_1(char c)
        throws Throwable
    {
        String s = "testPrimitiveCharacterPrimitiveCharacter_1";
        Object aobj[] = {
            new Character(c)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Character(((String)obj).charAt(0));
        if(!(obj instanceof Character))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Character).append("'.").toString());
        else
            return ((Character)obj).charValue();
    }

    void testPrimitiveShortVoid_1(short word0)
        throws Throwable
    {
        String s = "testPrimitiveShortVoid_1";
        Object aobj[] = {
            new Short(word0)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    short testPrimitiveShortPrimitiveShort_1(short word0)
        throws Throwable
    {
        String s = "testPrimitiveShortPrimitiveShort_1";
        Object aobj[] = {
            new Short(word0)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Short((String)obj);
        if(!(obj instanceof Short))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Short).append("'.").toString());
        else
            return ((Short)obj).shortValue();
    }

    void testPrimitiveIntegerVoid_1(int i)
        throws Throwable
    {
        String s = "testPrimitiveIntegerVoid_1";
        Object aobj[] = {
            new Integer(i)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    int testPrimitiveIntegerPrimitiveInteger_1(int i)
        throws Throwable
    {
        String s = "testPrimitiveIntegerPrimitiveInteger_1";
        Object aobj[] = {
            new Integer(i)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Integer((String)obj);
        if(!(obj instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Integer).append("'.").toString());
        else
            return ((Integer)obj).intValue();
    }

    void testPrimitiveLongVoid_1(long l)
        throws Throwable
    {
        String s = "testPrimitiveLongVoid_1";
        Object aobj[] = {
            new Long(l)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    long testPrimitiveLongPrimitiveLong_1(long l)
        throws Throwable
    {
        String s = "testPrimitiveLongPrimitiveLong_1";
        Object aobj[] = {
            new Long(l)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Long.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Long((String)obj);
        if(!(obj instanceof Long))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Long).append("'.").toString());
        else
            return ((Long)obj).longValue();
    }

    void testPrimitiveFloatVoid_1(float f)
        throws Throwable
    {
        String s = "testPrimitiveFloatVoid_1";
        Object aobj[] = {
            new Float(f)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    float testPrimitiveFloatPrimitiveFloat_1(float f)
        throws Throwable
    {
        String s = "testPrimitiveFloatPrimitiveFloat_1";
        Object aobj[] = {
            new Float(f)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Float.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Float((String)obj);
        if(!(obj instanceof Float))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Float).append("'.").toString());
        else
            return ((Float)obj).floatValue();
    }

    void testPrimitiveDoubleVoid_1(double d)
        throws Throwable
    {
        String s = "testPrimitiveDoubleVoid_1";
        Object aobj[] = {
            new Double(d)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    double testPrimitiveDoublePrimitiveDouble_1(double d)
        throws Throwable
    {
        String s = "testPrimitiveDoublePrimitiveDouble_1";
        Object aobj[] = {
            new Double(d)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Double.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Double((String)obj);
        if(!(obj instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Double).append("'.").toString());
        else
            return ((Double)obj).doubleValue();
    }

    void testBooleanVoid_1(Boolean boolean1)
        throws Throwable
    {
        String s = "testBooleanVoid_1";
        Object aobj[] = {
            boolean1
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Boolean testBooleanBoolean_1(Boolean boolean1)
        throws Throwable
    {
        String s = "testBooleanBoolean_1";
        Object aobj[] = {
            boolean1
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Boolean((String)obj);
        if(!(obj instanceof Boolean))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Boolean).append("'.").toString());
        else
            return (Boolean)obj;
    }

    void testByteVoid_1(Byte byte1)
        throws Throwable
    {
        String s = "testByteVoid_1";
        Object aobj[] = {
            byte1
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Byte testByteByte_1(Byte byte1)
        throws Throwable
    {
        String s = "testByteByte_1";
        Object aobj[] = {
            byte1
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Byte((String)obj);
        if(!(obj instanceof Byte))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Byte).append("'.").toString());
        else
            return (Byte)obj;
    }

    void testCharacterVoid_1(Character character)
        throws Throwable
    {
        String s = "testCharacterVoid_1";
        Object aobj[] = {
            character
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Character testCharacterCharacter_1(Character character)
        throws Throwable
    {
        String s = "testCharacterCharacter_1";
        Object aobj[] = {
            character
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Character(((String)obj).charAt(0));
        if(!(obj instanceof Character))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Character).append("'.").toString());
        else
            return (Character)obj;
    }

    void testIntegerVoid_1(Integer integer)
        throws Throwable
    {
        String s = "testIntegerVoid_1";
        Object aobj[] = {
            integer
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Integer testIntegerInteger_1(Integer integer)
        throws Throwable
    {
        String s = "testIntegerInteger_1";
        Object aobj[] = {
            integer
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Integer((String)obj);
        if(!(obj instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Integer).append("'.").toString());
        else
            return (Integer)obj;
    }

    void testLongVoid_1(Long long1)
        throws Throwable
    {
        String s = "testLongVoid_1";
        Object aobj[] = {
            long1
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Long testLongLong_1(Long long1)
        throws Throwable
    {
        String s = "testLongLong_1";
        Object aobj[] = {
            long1
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Long((String)obj);
        if(!(obj instanceof Long))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Long).append("'.").toString());
        else
            return (Long)obj;
    }

    void testFloatVoid_1(Float float1)
        throws Throwable
    {
        String s = "testFloatVoid_1";
        Object aobj[] = {
            float1
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Float testFloatFloat_1(Float float1)
        throws Throwable
    {
        String s = "testFloatFloat_1";
        Object aobj[] = {
            float1
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Float((String)obj);
        if(!(obj instanceof Float))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Float).append("'.").toString());
        else
            return (Float)obj;
    }

    void testDoubleVoid_1(Double double1)
        throws Throwable
    {
        String s = "testDoubleVoid_1";
        Object aobj[] = {
            double1
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    Double testDoubleDouble_1(Double double1)
        throws Throwable
    {
        String s = "testDoubleDouble_1";
        Object aobj[] = {
            double1
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Double((String)obj);
        if(!(obj instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Double).append("'.").toString());
        else
            return (Double)obj;
    }

    void testStringVoid_1(String s)
        throws Throwable
    {
        String s1 = "testStringVoid_1";
        Object aobj[] = {
            s
        };
        if(target == null)
            defaultHandler.invoke(s1, aobj);
        else
            target.invoke(s1, aobj);
    }

    String testStringString_1(String s)
        throws Throwable
    {
        String s1 = "testStringString_1";
        Object aobj[] = {
            s
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s1, aobj);
        else
            obj = target.invoke(s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the  return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(!(obj instanceof String))
            obj = (new StringBuilder()).append("").append(obj).toString();
        if(!(obj instanceof String))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/String).append("'.").toString());
        else
            return (String)obj;
    }

    void TestPrimitivesVoid(boolean flag, byte byte0, char c, short word0, int i, long l, 
            float f, double d, String s)
        throws Throwable
    {
        String s1 = "TestPrimitivesVoid";
        Object aobj[] = {
            Boolean.valueOf(flag), new Byte(byte0), new Short(word0), new Integer(i), new Long(l), new Float(f), new Double(d), s
        };
        if(target == null)
            defaultHandler.invoke(s1, aobj);
        else
            target.invoke(s1, aobj);
    }

    Object TestPrimitivesObject(boolean flag, byte byte0, char c, short word0, int i, long l, 
            float f, double d, String s)
        throws Throwable
    {
        String s1 = "TestPrimitivesObject";
        Object aobj[] = {
            Boolean.valueOf(flag), new Byte(byte0), new Short(word0), new Integer(i), new Long(l), new Float(f), new Double(d), s
        };
        if(target == null)
            return defaultHandler.invoke(s1, aobj);
        else
            return target.invoke(s1, aobj);
    }

    public void testEmptyVoid()
        throws Throwable
    {
        String s = "testEmptyVoid";
        if(target == null)
            defaultHandler.invoke(s, null);
        else
            target.invoke(s, null);
    }

    public Object testEmptyObject()
        throws Throwable
    {
        String s = "testEmptyObject";
        if(target == null)
            return defaultHandler.invoke(s, null);
        else
            return target.invoke(s, null);
    }

    public void testOneObjectVoid(Object obj)
        throws Throwable
    {
        String s = "testOneObjectVoid";
        Object aobj[] = {
            obj
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    public Object testOneObjectObject(Object obj)
        throws Throwable
    {
        String s = "testOneObjectObject";
        Object aobj[] = {
            obj
        };
        if(target == null)
            return defaultHandler.invoke(s, aobj);
        else
            return target.invoke(s, aobj);
    }

    public void testOneIntVoid(int i)
        throws Throwable
    {
        String s = "testOneIntVoid";
        Object aobj[] = {
            new Integer(i)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    public Object testOneIntObject(int i)
        throws Throwable
    {
        String s = "testOneObjectObject";
        Object aobj[] = {
            new Integer(i)
        };
        Object obj = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        return obj;
    }

    public Double testOneDblDouble_1(double d)
        throws Throwable
    {
        String s = "testOneDblDouble_1";
        Object aobj[] = {
            new Double(d)
        };
        Object obj = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj != null)
        {
            Object obj1 = obj;
            if(obj instanceof String)
                obj = new Double((String)obj);
            if(!(obj instanceof Double))
                throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Double).append('"').toString());
        }
        return (Double)obj;
    }

    public Integer testOneIntInteger_2(int i)
        throws Throwable
    {
        String s = "testOneIntInteger_2";
        Object aobj[] = {
            new Integer(i)
        };
        Object obj = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        return (Integer)obj;
    }

    public Object testOneIntInt_2(int i, Object obj)
        throws Throwable
    {
        String s = "testOneIntInt_2";
        Object aobj[] = {
            new Integer(i), obj
        };
        Object obj1 = null;
        if(target == null)
            obj1 = defaultHandler.invoke(s, aobj);
        else
            obj1 = target.invoke(s, aobj);
        return obj1;
    }

    public int testOneIntInt_1(int i)
        throws Throwable
    {
        String s = "testOneIntInt_1";
        Object aobj[] = {
            new Integer(i)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = Integer.valueOf((String)obj);
        if(!(obj instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Integer).append('"').toString());
        else
            return ((Integer)obj).intValue();
    }

    public void testOneIntVoid_1(int i)
        throws Throwable
    {
        String s = "testOneIntVoid_1";
        Object aobj[] = {
            new Integer(i)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    public double testOneDoubleDouble_1(double d)
        throws Throwable
    {
        String s = "testOneDoubleDouble_1";
        Object aobj[] = {
            new Double(d)
        };
        Object obj = null;
        Object obj1 = null;
        if(target == null)
            obj = defaultHandler.invoke(s, aobj);
        else
            obj = target.invoke(s, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: '").append(Integer.TYPE).append('"').toString());
        obj1 = obj;
        if(obj instanceof String)
            obj = new Double((String)obj);
        if(!(obj instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Result type '").append(obj1.getClass()).append("' cannot be cast to return type '").append(java/lang/Double).append('"').toString());
        else
            return ((Double)obj).doubleValue();
    }

    public void testOneDoubleVoid_1(double d)
        throws Throwable
    {
        String s = "testOneInt_1";
        Object aobj[] = {
            new Double(d)
        };
        if(target == null)
            defaultHandler.invoke(s, aobj);
        else
            target.invoke(s, aobj);
    }

    private static Object classLock = new Object();
    private static volatile RexxProxy defaultHandler = null;
    RexxProxy target;

}
