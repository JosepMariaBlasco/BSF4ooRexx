// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RgfExtendClass: extended class "TestAbstract" to create "TestAbstract_$RgfExtendClass$_19336051"

import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxProxy;

public class TestAbstract_$RgfExtendClass$_19336051 extends TestAbstract
{

    public boolean test_Void_boolean()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_boolean";
        String s1 = "()Z";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = !((String) (obj1)).equals("1") && !((String) (obj1)).equalsIgnoreCase("true") ? ((Object) (Boolean.FALSE)) : ((Object) (Boolean.TRUE));
        }
        if(!(obj1 instanceof Boolean))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Boolean)obj1).booleanValue();
    }

    public byte test_Void_byte()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_byte";
        String s1 = "()B";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Byte.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Byte))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Byte)obj1).byteValue();
    }

    public char test_Void_char()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_char";
        String s1 = "()C";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Character.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Character))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Character)obj1).charValue();
    }

    public short test_Void_short()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_short";
        String s1 = "()S";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Short.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Short))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Short)obj1).shortValue();
    }

    public int test_Void_int()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_int";
        String s1 = "()I";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Integer.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Integer)obj1).intValue();
    }

    public long test_Void_long()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_long";
        String s1 = "()J";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Long.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Long))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Long)obj1).longValue();
    }

    public float test_Void_float()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_float";
        String s1 = "()F";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Float.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Float))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Float)obj1).floatValue();
    }

    public double test_Void_double()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_double";
        String s1 = "()D";
        Object obj = null;
        Object obj1 = rexxproxy.invoke(this, s, s1, obj);
        if(obj1 == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
        Object obj2 = obj1;
        if(obj1 instanceof String)
        {
            obj1 = (String)obj1;
            obj1 = Double.valueOf(((String) (obj1)));
        }
        if(!(obj1 instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj2.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Double)obj1).doubleValue();
    }

    public Boolean test_Void_Boolean()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Boolean";
        String s1 = "()Ljava/lang/Boolean;";
        Object obj = null;
        return (Boolean)rexxproxy.invoke(this, s, s1, obj);
    }

    public Byte test_Void_Byte()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Byte";
        String s1 = "()Ljava/lang/Byte;";
        Object obj = null;
        return (Byte)rexxproxy.invoke(this, s, s1, obj);
    }

    public Character test_Void_Character()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Character";
        String s1 = "()Ljava/lang/Character;";
        Object obj = null;
        return (Character)rexxproxy.invoke(this, s, s1, obj);
    }

    public Short test_Void_Short()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Short";
        String s1 = "()Ljava/lang/Short;";
        Object obj = null;
        return (Short)rexxproxy.invoke(this, s, s1, obj);
    }

    public Integer test_Void_Integer()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Integer";
        String s1 = "()Ljava/lang/Integer;";
        Object obj = null;
        return (Integer)rexxproxy.invoke(this, s, s1, obj);
    }

    public Long test_Void_Long()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Long";
        String s1 = "()Ljava/lang/Long;";
        Object obj = null;
        return (Long)rexxproxy.invoke(this, s, s1, obj);
    }

    public Float test_Void_Float()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Float";
        String s1 = "()Ljava/lang/Float;";
        Object obj = null;
        return (Float)rexxproxy.invoke(this, s, s1, obj);
    }

    public Double test_Void_Double()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Double";
        String s1 = "()Ljava/lang/Double;";
        Object obj = null;
        return (Double)rexxproxy.invoke(this, s, s1, obj);
    }

    public Object test_Void_Object()
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Void_Object";
        String s1 = "()Ljava/lang/Object;";
        Object obj = null;
        return rexxproxy.invoke(this, s, s1, obj);
    }

    public boolean test_boolean_boolean(boolean flag)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_boolean_boolean";
        String s1 = "(Z)Z";
        Object aobj[] = null;
        aobj = (new Object[] {
            Boolean.valueOf(flag)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = !((String) (obj)).equals("1") && !((String) (obj)).equalsIgnoreCase("true") ? ((Object) (Boolean.FALSE)) : ((Object) (Boolean.TRUE));
        }
        if(!(obj instanceof Boolean))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Boolean)obj).booleanValue();
    }

    public byte test_byte_byte(byte byte0)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_byte_byte";
        String s1 = "(B)B";
        Object aobj[] = null;
        aobj = (new Object[] {
            Byte.valueOf(byte0)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Byte.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Byte))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Byte)obj).byteValue();
    }

    public char test_char_char(char c)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_char_char";
        String s1 = "(C)C";
        Object aobj[] = null;
        aobj = (new Object[] {
            Character.valueOf(c)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Character.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Character))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Character)obj).charValue();
    }

    public short test_short_short(short word0)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_short_short";
        String s1 = "(S)S";
        Object aobj[] = null;
        aobj = (new Object[] {
            Short.valueOf(word0)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Short.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Short))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Short)obj).shortValue();
    }

    public int test_int_int(int i)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_int_int";
        String s1 = "(I)I";
        Object aobj[] = null;
        aobj = (new Object[] {
            Integer.valueOf(i)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Integer.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Integer)obj).intValue();
    }

    public long test_long_long(long l)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_long_long";
        String s1 = "(J)J";
        Object aobj[] = null;
        aobj = (new Object[] {
            Long.valueOf(l)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Long.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Long))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Long)obj).longValue();
    }

    public float test_float_float(float f)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_float_float";
        String s1 = "(F)F";
        Object aobj[] = null;
        aobj = (new Object[] {
            Float.valueOf(f)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Float.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Float))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Float)obj).floatValue();
    }

    public double test_double_double(double d)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_double_double";
        String s1 = "(D)D";
        Object aobj[] = null;
        aobj = (new Object[] {
            Double.valueOf(d)
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Double.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Double)obj).doubleValue();
    }

    public boolean test_Boolean_boolean(Boolean boolean1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Boolean_boolean";
        String s1 = "(Ljava/lang/Boolean;)Z";
        Object aobj[] = null;
        aobj = (new Object[] {
            boolean1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = !((String) (obj)).equals("1") && !((String) (obj)).equalsIgnoreCase("true") ? ((Object) (Boolean.FALSE)) : ((Object) (Boolean.TRUE));
        }
        if(!(obj instanceof Boolean))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Boolean)obj).booleanValue();
    }

    public byte test_Byte_byte(Byte byte1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Byte_byte";
        String s1 = "(Ljava/lang/Byte;)B";
        Object aobj[] = null;
        aobj = (new Object[] {
            byte1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Byte.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Byte))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Byte)obj).byteValue();
    }

    public char test_Character_char(Character character)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Character_char";
        String s1 = "(Ljava/lang/Character;)C";
        Object aobj[] = null;
        aobj = (new Object[] {
            character
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Character.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Character))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Character)obj).charValue();
    }

    public short test_Short_short(Short short1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Short_short";
        String s1 = "(Ljava/lang/Short;)S";
        Object aobj[] = null;
        aobj = (new Object[] {
            short1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Short.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Short))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Short)obj).shortValue();
    }

    public int test_Integer_int(Integer integer)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Integer_int";
        String s1 = "(Ljava/lang/Integer;)I";
        Object aobj[] = null;
        aobj = (new Object[] {
            integer
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Integer.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Integer))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Integer)obj).intValue();
    }

    public long test_Long_long(Long long1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Long_long";
        String s1 = "(Ljava/lang/Long;)J";
        Object aobj[] = null;
        aobj = (new Object[] {
            long1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Long.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Long))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Long)obj).longValue();
    }

    public float test_Float_float(Float float1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Float_float";
        String s1 = "(Ljava/lang/Float;)F";
        Object aobj[] = null;
        aobj = (new Object[] {
            float1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Float.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Float))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Float)obj).floatValue();
    }

    public double test_Double_double(Double double1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Double_double";
        String s1 = "(Ljava/lang/Double;)D";
        Object aobj[] = null;
        aobj = (new Object[] {
            double1
        });
        Object obj = rexxproxy.invoke(this, s, s1, aobj);
        if(obj == null)
            throw new ClassCastException((new StringBuilder()).append("Returned result is 'null', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
        Object obj1 = obj;
        if(obj instanceof String)
        {
            obj = (String)obj;
            obj = Double.valueOf(((String) (obj)));
        }
        if(!(obj instanceof Double))
            throw new ClassCastException((new StringBuilder()).append("Returned result type \"").append(obj1.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
        else
            return ((Double)obj).doubleValue();
    }

    public Boolean test_Boolean_Boolean(Boolean boolean1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Boolean_Boolean";
        String s1 = "(Ljava/lang/Boolean;)Ljava/lang/Boolean;";
        Object aobj[] = null;
        aobj = (new Object[] {
            boolean1
        });
        return (Boolean)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Byte test_Byte_Byte(Byte byte1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Byte_Byte";
        String s1 = "(Ljava/lang/Byte;)Ljava/lang/Byte;";
        Object aobj[] = null;
        aobj = (new Object[] {
            byte1
        });
        return (Byte)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Character test_Character_Character(Character character)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Character_Character";
        String s1 = "(Ljava/lang/Character;)Ljava/lang/Character;";
        Object aobj[] = null;
        aobj = (new Object[] {
            character
        });
        return (Character)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Short test_Short_Short(Short short1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Short_Short";
        String s1 = "(Ljava/lang/Short;)Ljava/lang/Short;";
        Object aobj[] = null;
        aobj = (new Object[] {
            short1
        });
        return (Short)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Integer test_Integer_Integer(Integer integer)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Integer_Integer";
        String s1 = "(Ljava/lang/Integer;)Ljava/lang/Integer;";
        Object aobj[] = null;
        aobj = (new Object[] {
            integer
        });
        return (Integer)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Long test_Long_Long(Long long1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Long_Long";
        String s1 = "(Ljava/lang/Long;)Ljava/lang/Long;";
        Object aobj[] = null;
        aobj = (new Object[] {
            long1
        });
        return (Long)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Float test_Float_Float(Float float1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Float_Float";
        String s1 = "(Ljava/lang/Float;)Ljava/lang/Float;";
        Object aobj[] = null;
        aobj = (new Object[] {
            float1
        });
        return (Float)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Double test_Double_Double(Double double1)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Double_Double";
        String s1 = "(Ljava/lang/Double;)Ljava/lang/Double;";
        Object aobj[] = null;
        aobj = (new Object[] {
            double1
        });
        return (Double)rexxproxy.invoke(this, s, s1, aobj);
    }

    public Object test_Object_Object(Object obj)
        throws RexxException
    {
        target;
        if(target != null) goto _L2; else goto _L1
_L1:
        defaultHandler;
_L2:
        RexxProxy rexxproxy;
        rexxproxy;
        String s = "test_Object_Object";
        String s1 = "(Ljava/lang/Object;)Ljava/lang/Object;";
        Object aobj[] = null;
        aobj = (new Object[] {
            obj
        });
        return rexxproxy.invoke(this, s, s1, aobj);
    }

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

    private static Object classLock = new Object();
    private static RexxProxy defaultHandler = null;
    RexxProxy target;


    public TestAbstract_$RgfExtendClass$_19336051(RexxProxy rexxproxy)
        throws RexxException
    {
        super.target = null;
        super.target = rexxproxy;
    }
}
