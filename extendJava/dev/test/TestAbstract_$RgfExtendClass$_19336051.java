//  Decompiled by jdec
//  DECOMPILER HOME PAGE: jdec.sourceforge.net
//  Main HOSTING SITE: sourceforge.net
//  Copyright (C)2006,2007,2008 Swaroop Belur.
//  jdec comes with ABSOLUTELY NO WARRANTY;
//  This is free software, and you are welcome to redistribute
//  it under certain conditions;
//  See the File 'COPYING' For more details.

/**** List of All Imported Classes ***/

import TestAbstract_.RgfExtendClass._19336051;
import java.lang.Boolean;
import java.lang.Byte;
import java.lang.Character;
import java.lang.Class;
import java.lang.ClassCastException;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Short;
import java.lang.String;
import java.lang.StringBuilder;
import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxProxy;

// End of Import

class  TestAbstract_$RgfExtendClass$_19336051 extends  TestAbstract

{

 /***
 **Class Fields
 ***/
 Object classLock ;
 RexxProxy defaultHandler ;
 RexxProxy target ;


//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public     TestAbstract_$RgfExtendClass$_19336051(  org.rexxla.bsf.engines.rexx.RexxProxy aRexxProxy1)
 throws RexxException
 {
    super();
    this.target =null;
    this.target =aRexxProxy1;
    return;

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    boolean test_Void_boolean( )
 throws RexxException
 {
    Object object2= null;
    Object object3= null;
    Object object4= null;
    RexxProxy aRexxProxy2= null;
    String string1= null;
    String string2= null;
    if(this.target== null)
    {

    }
    aRexxProxy2=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string2="test_Void_boolean";
    string1="()Z";
    object2=null;
    object3=aRexxProxy2.invoke(this,string2,string1,object2);
    if(object3== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object4=object3;
    if((object3 instanceof String)!=false)
    {
      string3 =(String)object3;
      if(string3.equals("1")!=false || string3.equalsIgnoreCase("true")!=false)

      {
        aBoolean1 =Boolean.TRUE;

      }
      else
      {
        aBoolean1 =Boolean.FALSE;

      }

    }
    if((aBoolean1 instanceof Boolean)!=true)
    {
      ClassCastException JdecGenerated131 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object4.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated131;

    }
    return (Boolean)aBoolean1.booleanValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    byte test_Void_byte( )
 throws RexxException
 {
    Object object6= null;
    Object object7= null;
    Object object8= null;
    RexxProxy aRexxProxy3= null;
    String string4= null;
    String string5= null;
    if(this.target== null)
    {

    }
    aRexxProxy3=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string5="test_Void_byte";
    string4="()B";
    object6=null;
    object7=aRexxProxy3.invoke(this,string5,string4,object6);
    if(object7== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object8=object7;
    if((object7 instanceof String)!=false)
    {
      string7 =(String)object7;
      aByte1 =Byte.valueOf(string7);

    }
    if((aByte1 instanceof Byte)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object8.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Byte)aByte1.byteValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    char test_Void_char( )
 throws RexxException
 {
    Object object10= null;
    Object object11= null;
    Object object12= null;
    RexxProxy aRexxProxy4= null;
    String string8= null;
    String string9= null;
    if(this.target== null)
    {

    }
    aRexxProxy4=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string9="test_Void_char";
    string8="()C";
    object10=null;
    object11=aRexxProxy4.invoke(this,string9,string8,object10);
    if(object11== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object12=object11;
    if((object11 instanceof String)!=false)
    {
      string11 =(String)object11;
      aCharacter1 =Character.valueOf(string11);

    }
    if((aCharacter1 instanceof Character)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object12.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Character)aCharacter1.charValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    short test_Void_short( )
 throws RexxException
 {
    Object object14= null;
    Object object15= null;
    Object object16= null;
    RexxProxy aRexxProxy5= null;
    String string12= null;
    String string13= null;
    if(this.target== null)
    {

    }
    aRexxProxy5=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string13="test_Void_short";
    string12="()S";
    object14=null;
    object15=aRexxProxy5.invoke(this,string13,string12,object14);
    if(object15== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object16=object15;
    if((object15 instanceof String)!=false)
    {
      string15 =(String)object15;
      aShort1 =Short.valueOf(string15);

    }
    if((aShort1 instanceof Short)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object16.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Short)aShort1.shortValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    int test_Void_int( )
 throws RexxException
 {
    Object object18= null;
    Object object19= null;
    Object object20= null;
    RexxProxy aRexxProxy6= null;
    String string16= null;
    String string17= null;
    if(this.target== null)
    {

    }
    aRexxProxy6=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string17="test_Void_int";
    string16="()I";
    object18=null;
    object19=aRexxProxy6.invoke(this,string17,string16,object18);
    if(object19== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object20=object19;
    if((object19 instanceof String)!=false)
    {
      string19 =(String)object19;
      anInteger1 =Integer.valueOf(string19);

    }
    if((anInteger1 instanceof Integer)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object20.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Integer)anInteger1.intValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    long test_Void_long( )
 throws RexxException
 {
    Object object22= null;
    Object object23= null;
    Object object24= null;
    RexxProxy aRexxProxy7= null;
    String string20= null;
    String string21= null;
    if(this.target== null)
    {

    }
    aRexxProxy7=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string21="test_Void_long";
    string20="()J";
    object22=null;
    object23=aRexxProxy7.invoke(this,string21,string20,object22);
    if(object23== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object24=object23;
    if((object23 instanceof String)!=false)
    {
      string23 =(String)object23;
      aLong1 =Long.valueOf(string23);

    }
    if((aLong1 instanceof Long)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object24.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Long)aLong1.longValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    float test_Void_float( )
 throws RexxException
 {
    Object object26= null;
    Object object27= null;
    Object object28= null;
    RexxProxy aRexxProxy8= null;
    String string24= null;
    String string25= null;
    if(this.target== null)
    {

    }
    aRexxProxy8=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string25="test_Void_float";
    string24="()F";
    object26=null;
    object27=aRexxProxy8.invoke(this,string25,string24,object26);
    if(object27== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object28=object27;
    if((object27 instanceof String)!=false)
    {
      string27 =(String)object27;
      aFloat1 =Float.valueOf(string27);

    }
    if((aFloat1 instanceof Float)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object28.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Float)aFloat1.floatValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    double test_Void_double( )
 throws RexxException
 {
    Object object30= null;
    Object object31= null;
    Object object32= null;
    RexxProxy aRexxProxy9= null;
    String string28= null;
    String string29= null;
    if(this.target== null)
    {

    }
    aRexxProxy9=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string29="test_Void_double";
    string28="()D";
    object30=null;
    object31=aRexxProxy9.invoke(this,string29,string28,object30);
    if(object31== null)
    {
      ClassCastException JdecGenerated39 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
      throw JdecGenerated39;

    }
    object32=object31;
    if((object31 instanceof String)!=false)
    {
      string31 =(String)object31;
      aDouble1 =Double.valueOf(string31);

    }
    if((aDouble1 instanceof Double)!=true)
    {
      ClassCastException JdecGenerated107 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object32.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated107;

    }
    return (Double)aDouble1.doubleValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Boolean test_Void_Boolean( )
 throws RexxException
 {
    Object object34= null;
    RexxProxy aRexxProxy10= null;
    String string32= null;
    String string33= null;
    if(this.target== null)
    {

    }
    aRexxProxy10=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string33="test_Void_Boolean";
    string32="()Ljava/lang/Boolean;";
    object34=null;
    return (Boolean)aRexxProxy10.invoke(this,string33,string32,object34);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Byte test_Void_Byte( )
 throws RexxException
 {
    Object object36= null;
    RexxProxy aRexxProxy11= null;
    String string34= null;
    String string35= null;
    if(this.target== null)
    {

    }
    aRexxProxy11=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string35="test_Void_Byte";
    string34="()Ljava/lang/Byte;";
    object36=null;
    return (Byte)aRexxProxy11.invoke(this,string35,string34,object36);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Character test_Void_Character( )
 throws RexxException
 {
    Object object38= null;
    RexxProxy aRexxProxy12= null;
    String string36= null;
    String string37= null;
    if(this.target== null)
    {

    }
    aRexxProxy12=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string37="test_Void_Character";
    string36="()Ljava/lang/Character;";
    object38=null;
    return (Character)aRexxProxy12.invoke(this,string37,string36,object38);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Short test_Void_Short( )
 throws RexxException
 {
    Object object40= null;
    RexxProxy aRexxProxy13= null;
    String string38= null;
    String string39= null;
    if(this.target== null)
    {

    }
    aRexxProxy13=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string39="test_Void_Short";
    string38="()Ljava/lang/Short;";
    object40=null;
    return (Short)aRexxProxy13.invoke(this,string39,string38,object40);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Integer test_Void_Integer( )
 throws RexxException
 {
    Object object42= null;
    RexxProxy aRexxProxy14= null;
    String string40= null;
    String string41= null;
    if(this.target== null)
    {

    }
    aRexxProxy14=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string41="test_Void_Integer";
    string40="()Ljava/lang/Integer;";
    object42=null;
    return (Integer)aRexxProxy14.invoke(this,string41,string40,object42);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Long test_Void_Long( )
 throws RexxException
 {
    Object object44= null;
    RexxProxy aRexxProxy15= null;
    String string42= null;
    String string43= null;
    if(this.target== null)
    {

    }
    aRexxProxy15=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string43="test_Void_Long";
    string42="()Ljava/lang/Long;";
    object44=null;
    return (Long)aRexxProxy15.invoke(this,string43,string42,object44);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Float test_Void_Float( )
 throws RexxException
 {
    Object object46= null;
    RexxProxy aRexxProxy16= null;
    String string44= null;
    String string45= null;
    if(this.target== null)
    {

    }
    aRexxProxy16=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string45="test_Void_Float";
    string44="()Ljava/lang/Float;";
    object46=null;
    return (Float)aRexxProxy16.invoke(this,string45,string44,object46);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Double test_Void_Double( )
 throws RexxException
 {
    Object object48= null;
    RexxProxy aRexxProxy17= null;
    String string46= null;
    String string47= null;
    if(this.target== null)
    {

    }
    aRexxProxy17=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string47="test_Void_Double";
    string46="()Ljava/lang/Double;";
    object48=null;
    return (Double)aRexxProxy17.invoke(this,string47,string46,object48);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Object test_Void_Object( )
 throws RexxException
 {
    Object object50= null;
    RexxProxy aRexxProxy18= null;
    String string48= null;
    String string49= null;
    if(this.target== null)
    {

    }
    aRexxProxy18=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string49="test_Void_Object";
    string48="()Ljava/lang/Object;";
    object50=null;
    return aRexxProxy18.invoke(this,string49,string48,object50);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    boolean test_boolean_boolean(  boolean boolean2)
 throws RexxException
 {
    Object object52= null;
    Object object53= null;
    Object object54= null;
    RexxProxy aRexxProxy19= null;
    String string50= null;
    String string51= null;
    if(this.target== null)
    {

    }
    aRexxProxy19=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string51="test_boolean_boolean";
    string50="(Z)Z";
    object52=null;
    Object []  JdecGeneratedVar25= new Object[]
    {
      Boolean.valueOf(boolean2)
     };
    aarray1 =JdecGeneratedVar25;
    object53=aRexxProxy19.invoke(this,string51,string50,aarray1);
    if(object53== null)
    {
      ClassCastException JdecGenerated54 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
      throw JdecGenerated54;

    }
    object54=object53;
    if((object53 instanceof String)!=false)
    {
      string52 =(String)object53;
      if(string52.equals("1")!=false || string52.equalsIgnoreCase("true")!=false)

      {
        aBoolean2 =Boolean.TRUE;

      }
      else
      {
        aBoolean2 =Boolean.FALSE;

      }

    }
    if((aBoolean2 instanceof Boolean)!=true)
    {
      ClassCastException JdecGenerated146 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object54.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated146;

    }
    return (Boolean)aBoolean2.booleanValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    byte test_byte_byte(  byte byte2)
 throws RexxException
 {
    Object object56= null;
    Object object57= null;
    Object object58= null;
    RexxProxy aRexxProxy20= null;
    String string53= null;
    String string54= null;
    if(this.target== null)
    {

    }
    aRexxProxy20=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string54="test_byte_byte";
    string53="(B)B";
    object56=null;
    Object []  JdecGeneratedVar25= new Object[]
    {
      Byte.valueOf(byte2)
     };
    aarray2 =JdecGeneratedVar25;
    object57=aRexxProxy20.invoke(this,string54,string53,aarray2);
    if(object57== null)
    {
      ClassCastException JdecGenerated54 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
      throw JdecGenerated54;

    }
    object58=object57;
    if((object57 instanceof String)!=false)
    {
      string56 =(String)object57;
      aByte2 =Byte.valueOf(string56);

    }
    if((aByte2 instanceof Byte)!=true)
    {
      ClassCastException JdecGenerated122 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object58.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated122;

    }
    return (Byte)aByte2.byteValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    char test_char_char(  char char2)
 throws RexxException
 {
    Object object60= null;
    Object object61= null;
    Object object62= null;
    RexxProxy aRexxProxy21= null;
    String string57= null;
    String string58= null;
    if(this.target== null)
    {

    }
    aRexxProxy21=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string58="test_char_char";
    string57="(C)C";
    object60=null;
    Object []  JdecGeneratedVar25= new Object[]
    {
      Character.valueOf(char2)
     };
    aarray3 =JdecGeneratedVar25;
    object61=aRexxProxy21.invoke(this,string58,string57,aarray3);
    if(object61== null)
    {
      ClassCastException JdecGenerated54 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
      throw JdecGenerated54;

    }
    object62=object61;
    if((object61 instanceof String)!=false)
    {
      string60 =(String)object61;
      aCharacter2 =Character.valueOf(string60);

    }
    if((aCharacter2 instanceof Character)!=true)
    {
      ClassCastException JdecGenerated122 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object62.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated122;

    }
    return (Character)aCharacter2.charValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    short test_short_short(  short short2)
 throws RexxException
 {
    Object object64= null;
    Object object65= null;
    Object object66= null;
    RexxProxy aRexxProxy22= null;
    String string61= null;
    String string62= null;
    if(this.target== null)
    {

    }
    aRexxProxy22=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string62="test_short_short";
    string61="(S)S";
    object64=null;
    Object []  JdecGeneratedVar25= new Object[]
    {
      Short.valueOf(short2)
     };
    aarray4 =JdecGeneratedVar25;
    object65=aRexxProxy22.invoke(this,string62,string61,aarray4);
    if(object65== null)
    {
      ClassCastException JdecGenerated54 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
      throw JdecGenerated54;

    }
    object66=object65;
    if((object65 instanceof String)!=false)
    {
      string64 =(String)object65;
      aShort2 =Short.valueOf(string64);

    }
    if((aShort2 instanceof Short)!=true)
    {
      ClassCastException JdecGenerated122 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object66.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated122;

    }
    return (Short)aShort2.shortValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    int test_int_int(  int int2)
 throws RexxException
 {
    Object object68= null;
    Object object69= null;
    Object object70= null;
    RexxProxy aRexxProxy23= null;
    String string65= null;
    String string66= null;
    if(this.target== null)
    {

    }
    aRexxProxy23=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string66="test_int_int";
    string65="(I)I";
    object68=null;
    Object []  JdecGeneratedVar25= new Object[]
    {
      Integer.valueOf(int2)
     };
    aarray5 =JdecGeneratedVar25;
    object69=aRexxProxy23.invoke(this,string66,string65,aarray5);
    if(object69== null)
    {
      ClassCastException JdecGenerated54 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated54;

    }
    object70=object69;
    if((object69 instanceof String)!=false)
    {
      string68 =(String)object69;
      anInteger2 =Integer.valueOf(string68);

    }
    if((anInteger2 instanceof Integer)!=true)
    {
      ClassCastException JdecGenerated122 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object70.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated122;

    }
    return (Integer)anInteger2.intValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    long test_long_long(  long long2)
 throws RexxException
 {
    Object object72= null;
    Object object73= null;
    Object object74= null;
    RexxProxy aRexxProxy24= null;
    String string69= null;
    String string70= null;
    if(this.target== null)
    {

    }
    aRexxProxy24=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string70="test_long_long";
    string69="(J)J";
    object72=null;
    Object []  JdecGeneratedVar28= new Object[]
    {
      Long.valueOf(long2)
     };
    aarray6 =JdecGeneratedVar28;
    object73=aRexxProxy24.invoke(this,string70,string69,aarray6);
    if(object73== null)
    {
      ClassCastException JdecGenerated58 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
      throw JdecGenerated58;

    }
    object74=object73;
    if((object73 instanceof String)!=false)
    {
      string72 =(String)object73;
      aLong2 =Long.valueOf(string72);

    }
    if((aLong2 instanceof Long)!=true)
    {
      ClassCastException JdecGenerated126 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object74.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated126;

    }
    return (Long)aLong2.longValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    float test_float_float(  float float2)
 throws RexxException
 {
    Object object76= null;
    Object object77= null;
    Object object78= null;
    RexxProxy aRexxProxy25= null;
    String string73= null;
    String string74= null;
    if(this.target== null)
    {

    }
    aRexxProxy25=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string74="test_float_float";
    string73="(F)F";
    object76=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      Float.valueOf(float2)
     };
    aarray7 =JdecGeneratedVar27;
    object77=aRexxProxy25.invoke(this,string74,string73,aarray7);
    if(object77== null)
    {
      ClassCastException JdecGenerated56 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
      throw JdecGenerated56;

    }
    object78=object77;
    if((object77 instanceof String)!=false)
    {
      string76 =(String)object77;
      aFloat2 =Float.valueOf(string76);

    }
    if((aFloat2 instanceof Float)!=true)
    {
      ClassCastException JdecGenerated124 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object78.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated124;

    }
    return (Float)aFloat2.floatValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    double test_double_double(  double double2)
 throws RexxException
 {
    Object object80= null;
    Object object81= null;
    Object object82= null;
    RexxProxy aRexxProxy26= null;
    String string77= null;
    String string78= null;
    if(this.target== null)
    {

    }
    aRexxProxy26=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string78="test_double_double";
    string77="(D)D";
    object80=null;
    Object []  JdecGeneratedVar28= new Object[]
    {
      Double.valueOf(double2)
     };
    aarray8 =JdecGeneratedVar28;
    object81=aRexxProxy26.invoke(this,string78,string77,aarray8);
    if(object81== null)
    {
      ClassCastException JdecGenerated58 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
      throw JdecGenerated58;

    }
    object82=object81;
    if((object81 instanceof String)!=false)
    {
      string80 =(String)object81;
      aDouble2 =Double.valueOf(string80);

    }
    if((aDouble2 instanceof Double)!=true)
    {
      ClassCastException JdecGenerated126 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object82.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated126;

    }
    return (Double)aDouble2.doubleValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    boolean test_Boolean_boolean(  Boolean aBoolean4)
 throws RexxException
 {
    Object object84= null;
    Object object85= null;
    Object object86= null;
    RexxProxy aRexxProxy27= null;
    String string81= null;
    String string82= null;
    if(this.target== null)
    {

    }
    aRexxProxy27=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string82="test_Boolean_boolean";
    string81="(Ljava/lang/Boolean;)Z";
    object84=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aBoolean4
     };
    aarray9 =JdecGeneratedVar27;
    object85=aRexxProxy27.invoke(this,string82,string81,aarray9);
    if(object85== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Boolean.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object86=object85;
    if((object85 instanceof String)!=false)
    {
      string83 =(String)object85;
      if(string83.equals("1")!=false || string83.equalsIgnoreCase("true")!=false)

      {
        aBoolean5 =Boolean.TRUE;

      }
      else
      {
        aBoolean5 =Boolean.FALSE;

      }

    }
    if((aBoolean5 instanceof Boolean)!=true)
    {
      ClassCastException JdecGenerated145 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object86.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated145;

    }
    return (Boolean)aBoolean5.booleanValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    byte test_Byte_byte(  Byte aByte4)
 throws RexxException
 {
    Object object88= null;
    Object object89= null;
    Object object90= null;
    RexxProxy aRexxProxy28= null;
    String string84= null;
    String string85= null;
    if(this.target== null)
    {

    }
    aRexxProxy28=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string85="test_Byte_byte";
    string84="(Ljava/lang/Byte;)B";
    object88=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aByte4
     };
    aarray10 =JdecGeneratedVar27;
    object89=aRexxProxy28.invoke(this,string85,string84,aarray10);
    if(object89== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Byte.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object90=object89;
    if((object89 instanceof String)!=false)
    {
      string87 =(String)object89;
      aByte5 =Byte.valueOf(string87);

    }
    if((aByte5 instanceof Byte)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object90.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Byte)aByte5.byteValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    char test_Character_char(  Character aCharacter4)
 throws RexxException
 {
    Object object92= null;
    Object object93= null;
    Object object94= null;
    RexxProxy aRexxProxy29= null;
    String string88= null;
    String string89= null;
    if(this.target== null)
    {

    }
    aRexxProxy29=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string89="test_Character_char";
    string88="(Ljava/lang/Character;)C";
    object92=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aCharacter4
     };
    aarray11 =JdecGeneratedVar27;
    object93=aRexxProxy29.invoke(this,string89,string88,aarray11);
    if(object93== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Character.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object94=object93;
    if((object93 instanceof String)!=false)
    {
      string91 =(String)object93;
      aCharacter5 =Character.valueOf(string91);

    }
    if((aCharacter5 instanceof Character)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object94.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Character)aCharacter5.charValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    short test_Short_short(  Short aShort4)
 throws RexxException
 {
    Object object96= null;
    Object object97= null;
    Object object98= null;
    RexxProxy aRexxProxy30= null;
    String string92= null;
    String string93= null;
    if(this.target== null)
    {

    }
    aRexxProxy30=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string93="test_Short_short";
    string92="(Ljava/lang/Short;)S";
    object96=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aShort4
     };
    aarray12 =JdecGeneratedVar27;
    object97=aRexxProxy30.invoke(this,string93,string92,aarray12);
    if(object97== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Short.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object98=object97;
    if((object97 instanceof String)!=false)
    {
      string95 =(String)object97;
      aShort5 =Short.valueOf(string95);

    }
    if((aShort5 instanceof Short)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object98.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Short)aShort5.shortValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    int test_Integer_int(  Integer anInteger4)
 throws RexxException
 {
    Object object100= null;
    Object object101= null;
    Object object102= null;
    RexxProxy aRexxProxy31= null;
    String string96= null;
    String string97= null;
    if(this.target== null)
    {

    }
    aRexxProxy31=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string97="test_Integer_int";
    string96="(Ljava/lang/Integer;)I";
    object100=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      anInteger4
     };
    aarray13 =JdecGeneratedVar27;
    object101=aRexxProxy31.invoke(this,string97,string96,aarray13);
    if(object101== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object102=object101;
    if((object101 instanceof String)!=false)
    {
      string99 =(String)object101;
      anInteger5 =Integer.valueOf(string99);

    }
    if((anInteger5 instanceof Integer)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object102.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Integer)anInteger5.intValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    long test_Long_long(  Long aLong4)
 throws RexxException
 {
    Object object104= null;
    Object object105= null;
    Object object106= null;
    RexxProxy aRexxProxy32= null;
    String string100= null;
    String string101= null;
    if(this.target== null)
    {

    }
    aRexxProxy32=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string101="test_Long_long";
    string100="(Ljava/lang/Long;)J";
    object104=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aLong4
     };
    aarray14 =JdecGeneratedVar27;
    object105=aRexxProxy32.invoke(this,string101,string100,aarray14);
    if(object105== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Long.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object106=object105;
    if((object105 instanceof String)!=false)
    {
      string103 =(String)object105;
      aLong5 =Long.valueOf(string103);

    }
    if((aLong5 instanceof Long)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object106.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Long)aLong5.longValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    float test_Float_float(  Float aFloat4)
 throws RexxException
 {
    Object object108= null;
    Object object109= null;
    Object object110= null;
    RexxProxy aRexxProxy33= null;
    String string104= null;
    String string105= null;
    if(this.target== null)
    {

    }
    aRexxProxy33=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string105="test_Float_float";
    string104="(Ljava/lang/Float;)F";
    object108=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aFloat4
     };
    aarray15 =JdecGeneratedVar27;
    object109=aRexxProxy33.invoke(this,string105,string104,aarray15);
    if(object109== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Float.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object110=object109;
    if((object109 instanceof String)!=false)
    {
      string107 =(String)object109;
      aFloat5 =Float.valueOf(string107);

    }
    if((aFloat5 instanceof Float)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object110.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Float)aFloat5.floatValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    double test_Double_double(  Double aDouble4)
 throws RexxException
 {
    Object object112= null;
    Object object113= null;
    Object object114= null;
    RexxProxy aRexxProxy34= null;
    String string108= null;
    String string109= null;
    if(this.target== null)
    {

    }
    aRexxProxy34=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string109="test_Double_double";
    string108="(Ljava/lang/Double;)D";
    object112=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aDouble4
     };
    aarray16 =JdecGeneratedVar27;
    object113=aRexxProxy34.invoke(this,string109,string108,aarray16);
    if(object113== null)
    {
      ClassCastException JdecGenerated53 = new ClassCastException(new StringBuilder().append("Returned result is \'null\', which cannot be cast to the primitive return type: \"").append(Double.TYPE).append("\"").toString());
      throw JdecGenerated53;

    }
    object114=object113;
    if((object113 instanceof String)!=false)
    {
      string111 =(String)object113;
      aDouble5 =Double.valueOf(string111);

    }
    if((aDouble5 instanceof Double)!=true)
    {
      ClassCastException JdecGenerated121 = new ClassCastException(new StringBuilder().append("Returned result type \"").append(object114.getClass()).append("\" cannot be cast to the primitive return type: \"").append(Integer.TYPE).append("\"").toString());
      throw JdecGenerated121;

    }
    return (Double)aDouble5.doubleValue();

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Boolean test_Boolean_Boolean(  Boolean aBoolean7)
 throws RexxException
 {
    Object object116= null;
    RexxProxy aRexxProxy35= null;
    String string112= null;
    String string113= null;
    if(this.target== null)
    {

    }
    aRexxProxy35=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string113="test_Boolean_Boolean";
    string112="(Ljava/lang/Boolean;)Ljava/lang/Boolean;";
    object116=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aBoolean7
     };
    aarray17 =JdecGeneratedVar27;
    return (Boolean)aRexxProxy35.invoke(this,string113,string112,aarray17);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Byte test_Byte_Byte(  Byte aByte7)
 throws RexxException
 {
    Object object118= null;
    RexxProxy aRexxProxy36= null;
    String string114= null;
    String string115= null;
    if(this.target== null)
    {

    }
    aRexxProxy36=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string115="test_Byte_Byte";
    string114="(Ljava/lang/Byte;)Ljava/lang/Byte;";
    object118=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aByte7
     };
    aarray18 =JdecGeneratedVar27;
    return (Byte)aRexxProxy36.invoke(this,string115,string114,aarray18);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Character test_Character_Character(  Character aCharacter7)
 throws RexxException
 {
    Object object120= null;
    RexxProxy aRexxProxy37= null;
    String string116= null;
    String string117= null;
    if(this.target== null)
    {

    }
    aRexxProxy37=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string117="test_Character_Character";
    string116="(Ljava/lang/Character;)Ljava/lang/Character;";
    object120=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aCharacter7
     };
    aarray19 =JdecGeneratedVar27;
    return (Character)aRexxProxy37.invoke(this,string117,string116,aarray19);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Short test_Short_Short(  Short aShort7)
 throws RexxException
 {
    Object object122= null;
    RexxProxy aRexxProxy38= null;
    String string118= null;
    String string119= null;
    if(this.target== null)
    {

    }
    aRexxProxy38=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string119="test_Short_Short";
    string118="(Ljava/lang/Short;)Ljava/lang/Short;";
    object122=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aShort7
     };
    aarray20 =JdecGeneratedVar27;
    return (Short)aRexxProxy38.invoke(this,string119,string118,aarray20);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Integer test_Integer_Integer(  Integer anInteger7)
 throws RexxException
 {
    Object object124= null;
    RexxProxy aRexxProxy39= null;
    String string120= null;
    String string121= null;
    if(this.target== null)
    {

    }
    aRexxProxy39=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string121="test_Integer_Integer";
    string120="(Ljava/lang/Integer;)Ljava/lang/Integer;";
    object124=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      anInteger7
     };
    aarray21 =JdecGeneratedVar27;
    return (Integer)aRexxProxy39.invoke(this,string121,string120,aarray21);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Long test_Long_Long(  Long aLong7)
 throws RexxException
 {
    Object object126= null;
    RexxProxy aRexxProxy40= null;
    String string122= null;
    String string123= null;
    if(this.target== null)
    {

    }
    aRexxProxy40=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string123="test_Long_Long";
    string122="(Ljava/lang/Long;)Ljava/lang/Long;";
    object126=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aLong7
     };
    aarray22 =JdecGeneratedVar27;
    return (Long)aRexxProxy40.invoke(this,string123,string122,aarray22);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Float test_Float_Float(  Float aFloat7)
 throws RexxException
 {
    Object object128= null;
    RexxProxy aRexxProxy41= null;
    String string124= null;
    String string125= null;
    if(this.target== null)
    {

    }
    aRexxProxy41=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string125="test_Float_Float";
    string124="(Ljava/lang/Float;)Ljava/lang/Float;";
    object128=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aFloat7
     };
    aarray23 =JdecGeneratedVar27;
    return (Float)aRexxProxy41.invoke(this,string125,string124,aarray23);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Double test_Double_Double(  Double aDouble7)
 throws RexxException
 {
    Object object130= null;
    RexxProxy aRexxProxy42= null;
    String string126= null;
    String string127= null;
    if(this.target== null)
    {

    }
    aRexxProxy42=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string127="test_Double_Double";
    string126="(Ljava/lang/Double;)Ljava/lang/Double;";
    object130=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      aDouble7
     };
    aarray24 =JdecGeneratedVar27;
    return (Double)aRexxProxy42.invoke(this,string127,string126,aarray24);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 public    Object test_Object_Object(  Object object134)
 throws RexxException
 {
    Object object133= null;
    RexxProxy aRexxProxy43= null;
    String string128= null;
    String string129= null;
    if(this.target== null)
    {

    }
    aRexxProxy43=TestAbstract_$RgfExtendClass$_19336051.defaultHandler;
    string129="test_Object_Object";
    string128="(Ljava/lang/Object;)Ljava/lang/Object;";
    object133=null;
    Object []  JdecGeneratedVar27= new Object[]
    {
      object134
     };
    aarray25 =JdecGeneratedVar27;
    return aRexxProxy43.invoke(this,string129,string128,aarray25);

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 RexxProxy getDefaultHandler( )
 {
    return TestAbstract_$RgfExtendClass$_19336051.defaultHandler;

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 void setDefaultHandler(  org.rexxla.bsf.engines.rexx.RexxProxy aRexxProxy46)
 {
    Object   object135= null;
    object135 =TestAbstract_$RgfExtendClass$_19336051.classLock;
    synchronized(object135)
    {
      TestAbstract_.RgfExtendClass._19336051.defaultHandler = this;

    }

 }

//  CLASS: TestAbstract_$RgfExtendClass$_19336051:
 static      //[Static Initializer]
 {

    Object JdecGenerated2 = new Object();
    TestAbstract_.RgfExtendClass._19336051.classLock = JdecGenerated2;
    TestAbstract_.RgfExtendClass._19336051.defaultHandler = null;

 }


}
