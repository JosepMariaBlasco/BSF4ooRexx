interface ABC_Interface
{
    static public int  int_abc=19;
    public final long long_abc=23L;
    short short_abc=11;

    String string_abc="abc";
    Object object_abc=new Object();


    public void testVoid() throws Throwable;
    void testPrimitiveBooleanVoid_1 (boolean argObj) throws Throwable;
    void testBooleanVoid_1 (Boolean argObj) throws Throwable;
}
