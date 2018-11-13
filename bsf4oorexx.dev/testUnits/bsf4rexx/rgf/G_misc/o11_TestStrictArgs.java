// ---rgf, 2018-11-10: Java class to allow testing the new RexxStrictArgument class and BSF.CLS box.strictArg() routine


package rgf.G_misc;

public class o11_TestStrictArgs     // Class that uses all primitive, wrapper types plus String and Object to allow testing RexxStrictArgument usages
{
    // keep the field names caselessly equal, such that we really are able to test whether
    // picking the right fields and methods works indeed

    // instance fields
    public boolean      i_bool  =false;
    public Boolean      i_Bool  =false;

    public byte         i_byte  =0;
    public Byte         i_Byte  =0;

    public char         i_char  ='0';
    public Character    i_Char  ='0';

    public short        i_short =0;
    public Short        i_Short =0;

    public int          i_int   =0;
    public Integer      i_Int   =0;

    public float        i_float =0;
    public Float        i_Float =new Float(0);

    public long         i_long  =0;
    public Long         i_Long  =new Long(0);

    public String       i_String="";

    public Object       i_Object=new Object();

    // instance fields: getter and setters
        // instance getter
        public boolean      getI_bool    () {  return     i_bool  ;   }
        public Boolean      getI_Bool    () {  return     i_Bool  ;   }

        public byte         getI_byte    () {  return     i_byte  ;   }
        public Byte         getI_Byte    () {  return     i_Byte  ;   }

        public char         getI_char    () {  return     i_char  ;   }
        public Character    getI_Char    () {  return     i_Char  ;   }

        public short        getI_short   () {  return     i_short ;   }
        public Short        getI_Short   () {  return     i_Short ;   }

        public int          getI_int     () {  return     i_int   ;   }
        public Integer      getI_Int     () {  return     i_Int   ;   }

        public float        getI_float   () {  return     i_float ;   }
        public Float        getI_Float   () {  return     i_Float ;   }

        public long         getI_long    () {  return     i_long  ;   }
        public Long         getI_Long    () {  return     i_Long  ;   }

        public String       getI_String  () {  return     i_String;   }

        public Object       getI_Object  () {  return     i_Object;   }

        // instance setter
        public void     setI_bool   ( boolean   tmp ) {  i_bool    = tmp; }
        public void     setI_Bool   ( Boolean   tmp ) {  i_Bool    = tmp; }

        public void     setI_byte   ( byte      tmp ) {  i_byte    = tmp; }
        public void     setI_Byte   ( Byte      tmp ) {  i_Byte    = tmp; }

        public void     setI_char   ( char      tmp ) {  i_char    = tmp; }
        public void     setI_Char   ( Character tmp ) {  i_Char    = tmp; }

        public void     setI_short  ( short     tmp ) {  i_short   = tmp; }
        public void     setI_Short  ( Short     tmp ) {  i_Short   = tmp; }

        public void     setI_int    ( int       tmp ) {  i_int     = tmp; }
        public void     setI_Int    ( Integer   tmp ) {  i_Int     = tmp; }

        public void     setI_float  ( float     tmp ) {  i_float   = tmp; }
        public void     setI_Float  ( Float     tmp ) {  i_Float   = tmp; }

        public void     setI_long   ( long      tmp ) {  i_long    = tmp; }
        public void     setI_Long   ( Long      tmp ) {  i_Long    = tmp; }

        public void     setI_String ( String    tmp ) {  i_String  = tmp; }

        public void     setI_Object ( Object    tmp ) {  i_Object  = tmp; }


        // static fields
        static public boolean      static_bool  =false;
        static public Boolean      static_Bool  =false;

        static public byte         static_byte  =0;
        static public Byte         static_Byte  =0;

        static public char         static_char  ='0';
        static public Character    static_Char  ='0';

        static public short        static_short =0;
        static public Short        static_Short =0;

        static public int          static_int   =0;
        static public Integer      static_Int   =0;

        static public float        static_float =0;
        static public Float        static_Float =new Float(0);

        static public long         static_long  =0;
        static public Long         static_Long  =new Long(0);

        static public String       static_String="";

        static public Object       static_Object=new Object();

        // static fields: getter and setters
            // static getter
        static public boolean      getStatic_bool    () {  return     static_bool  ;   }
        static public Boolean      getStatic_Bool    () {  return     static_Bool  ;   }

        static public byte         getStatic_byte    () {  return     static_byte  ;   }
        static public Byte         getStatic_Byte    () {  return     static_Byte  ;   }

        static public char         getStatic_char    () {  return     static_char  ;   }
        static public Character    getStatic_Char    () {  return     static_Char  ;   }

        static public short        getStatic_short   () {  return     static_short ;   }
        static public Short        getStatic_Short   () {  return     static_Short ;   }

        static public int          getStatic_int     () {  return     static_int   ;   }
        static public Integer      getStatic_Int     () {  return     static_Int   ;   }

        static public float        getStatic_float   () {  return     static_float ;   }
        static public Float        getStatic_Float   () {  return     static_Float ;   }

        static public long         getStatic_long    () {  return     static_long  ;   }
        static public Long         getStatic_Long    () {  return     static_Long  ;   }

        static public String       getStatic_String  () {  return     static_String;   }

        static public Object       getStatic_Object  () {  return     static_Object;   }

            // static setter
        static public void     setStatic_bool   ( boolean   tmp ) {  static_bool    = tmp; }
        static public void     setStatic_Bool   ( Boolean   tmp ) {  static_Bool    = tmp; }

        static public void     setStatic_byte   ( byte      tmp ) {  static_byte    = tmp; }
        static public void     setStatic_Byte   ( Byte      tmp ) {  static_Byte    = tmp; }

        static public void     setStatic_char   ( char      tmp ) {  static_char    = tmp; }
        static public void     setStatic_Char   ( Character tmp ) {  static_Char    = tmp; }

        static public void     setStatic_short  ( short     tmp ) {  static_short   = tmp; }
        static public void     setStatic_Short  ( Short     tmp ) {  static_Short   = tmp; }

        static public void     setStatic_int    ( int       tmp ) {  static_int     = tmp; }
        static public void     setStatic_Int    ( Integer   tmp ) {  static_Int     = tmp; }

        static public void     setStatic_float  ( float     tmp ) {  static_float   = tmp; }
        static public void     setStatic_Float  ( Float     tmp ) {  static_Float   = tmp; }

        static public void     setStatic_long   ( long      tmp ) {  static_long    = tmp; }
        static public void     setStatic_Long   ( Long      tmp ) {  static_Long    = tmp; }

        static public void     setStatic_String ( String    tmp ) {  static_String  = tmp; }

        static public void     setStatic_Object ( Object    tmp ) {  static_Object  = tmp; }


        // ---------------------------------------------------------------------------
        // constructor
        public o11_TestStrictArgs()     // default constructor
        {
            super();
        }

        public o11_TestStrictArgs       // Constructor A: all instance variable constructor
        (
        boolean   tmp1  ,
        Boolean   tmp2  ,
        byte      tmp25 ,
        Byte      tmp26 ,
        char      tmp3  ,
        Character tmp4  ,
        short     tmp5  ,
        Short     tmp6  ,
        int       tmp7  ,
        Integer   tmp8  ,
        float     tmp9  ,
        Float     tmp10 ,
        long      tmp11 ,
        Long      tmp12 ,
        String    tmp13 ,
        Object    tmp14
        )
        {
            i_bool    = tmp1;
            i_Bool    = tmp2;
            i_byte    = tmp25;
            i_Byte    = tmp26;
            i_char    = tmp3;
            i_Char    = tmp4;
            i_short   = tmp5;
            i_Short   = tmp6;
            i_int     = tmp7;
            i_Int     = tmp8;
            i_float   = tmp9;
            i_Float   = tmp10;
            i_long    = tmp11;
            i_Long    = tmp12;
            i_String  = tmp13;
            i_Object  = tmp14;
        }

        public o11_TestStrictArgs       // Constructor B: all instance variable constructor: exchange sequence pair-wise
        (
        Boolean   tmp2  ,
        boolean   tmp1  ,
        Byte      tmp26 ,
        byte      tmp25 ,
        Character tmp4  ,
        char      tmp3  ,
        Short     tmp6  ,
        short     tmp5  ,
        Integer   tmp8  ,
        int       tmp7  ,
        Float     tmp10 ,
        float     tmp9  ,
        Long      tmp12 ,
        long      tmp11 ,
        Object    tmp14 ,
        String    tmp13
        )
        {
            i_bool    = tmp1;
            i_Bool    = tmp2;
            i_byte    = tmp25;
            i_Byte    = tmp26;
            i_char    = tmp3;
            i_Char    = tmp4;
            i_short   = tmp5;
            i_Short   = tmp6;
            i_int     = tmp7;
            i_Int     = tmp8;
            i_float   = tmp9;
            i_Float   = tmp10;
            i_long    = tmp11;
            i_Long    = tmp12;
            i_String  = tmp13;
            i_Object  = tmp14;
        }




    // ---------------------------------------------------------------------------
    // test a few little things ...
    public static void main (String args[]) {
        o11_TestStrictArgs o11=new o11_TestStrictArgs();
        System.out.println("class:    "+o11_TestStrictArgs.toStringStatic());
        System.out.println();
        System.out.println("instance: "+o11);
        System.out.println("---");


        o11=new o11_TestStrictArgs(
        false,
        false,
        (byte) 0x0A,
        new Byte((byte) 0x0B),
        (char) 0x33,
        (char) 0x34,
        (short) 35,
        new Short((short) 36),
        37,
        38,
        40f,
        new Float(41f),
        50,
        new Long(51),
        "hello,world",
        new Object()
        );
        System.out.println("class:    "+o11_TestStrictArgs.toStringStatic());
        System.out.println();
        System.out.println("instance: "+o11);
    }

    public String toString()
    {
        return (this.getClass().getName()+"@"+this.hashCode()+"[i_bool="+i_bool+",i_Bool="+i_Bool
           +"/i_byte="+i_byte+",i_Byte="+i_Byte
           +"/i_char="+i_char+",i_Char="+i_Char+"/i_short="+i_short+",i_Short="+i_Short
           +"/i_int="+i_int+",i_Int="+i_Int+"/i_float="+i_float+",i_Float="+i_Float+"/i_long="+i_long+",i_Long="+i_Long
           +"/i_String=["+i_String+"]/i_Object=["+i_Object+"]]");
    }

    static public String toStringStatic()
    {
        return (o11_TestStrictArgs.class.getClass().getName()+"@"+o11_TestStrictArgs.class.hashCode()+"[static_bool="+static_bool+",static_Bool="+static_Bool
           +"/static_byte="+static_byte+",static_Byte="+static_Byte
           +"/static_char="+static_char+",static_Char="+static_Char+"/static_short="+static_short+",static_Short="+static_Short
           +"/static_int="+static_int+",static_Int="+static_Int+"/static_float="+static_float+",static_Float="+static_Float+"/static_long="+static_long+",static_Long="+static_Long
           +"/static_String=["+static_String+"]/static_Object=["+static_Object+"]]");
    }

}

