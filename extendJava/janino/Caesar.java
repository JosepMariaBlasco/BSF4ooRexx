/// createProxiedJavaClass, created Java code in source:
import org.rexxla.bsf.engines.rexx.RexxProxy;

public class Caesar extends TestAbstract
{
	private static          Object    classLock     =new Object();
	private static volatile RexxProxy defaultHandler=null;
	private                 RexxProxy target        =null;


	public static RexxProxy getDefaultHandler()
	{
		return defaultHandler;
	}


	public static void setDefaultHandler(RexxProxy rexxproxy)
	{
		synchronized (classLock)
		{
			defaultHandler=rexxproxy;
		}
	}


	// --- creating methods according to String array of methods to proxy (if any)

	// --- creating base (default) constructor

	public Caesar()
	{
		super();
		target=defaultHandler;
	}


	// --- creating constructors from existing public constructors using [TestAbstract]

	public void test_VoidVoid()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_VoidVoid", "public void test_VoidVoid()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

	}


	public boolean test_Void_boolean()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_boolean", "public boolean test_Void_boolean()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'boolean'.");
		}
		if (res instanceof String)
		{
			res= new Boolean((String) res);
		}
		return ((Boolean) res).booleanValue();
	}


	public byte test_Void_byte()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_byte", "public byte test_Void_byte()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'byte'.");
		}
		if (res instanceof String)
		{
			res= new Byte((String) res);
		}
		return ((Byte) res).byteValue();
	}


	public char test_Void_char()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_char", "public char test_Void_char()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'char'.");
		}
		if (res instanceof String)
		{
			res=((String) res).charAt(0);
		}
		return ((Character) res).charValue();
	}


	public short test_Void_short()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_short", "public short test_Void_short()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'short'.");
		}
		if (res instanceof String)
		{
			res= new Short((String) res);
		}
		return ((Short) res).shortValue();
	}


	public int test_Void_int()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_int", "public int test_Void_int()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'int'.");
		}
		if (res instanceof String)
		{
			res= new Integer((String) res);
		}
		return ((Integer) res).intValue();
	}


	public long test_Void_long()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_long", "public long test_Void_long()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'long'.");
		}
		if (res instanceof String)
		{
			res= new Long((String) res);
		}
		return ((Long) res).longValue();
	}


	public float test_Void_float()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_float", "public float test_Void_float()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'float'.");
		}
		if (res instanceof String)
		{
			res= new Float((String) res);
		}
		return ((Float) res).floatValue();
	}


	public double test_Void_double()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_double", "public double test_Void_double()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'double'.");
		}
		if (res instanceof String)
		{
			res= new Double((String) res);
		}
		return ((Double) res).doubleValue();
	}


	public java.lang.Boolean test_Void_Boolean()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Boolean", "public java.lang.Boolean test_Void_Boolean()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Boolean((String) res);
		}
		return (java.lang.Boolean) res;
	}


	public java.lang.Byte test_Void_Byte()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Byte", "public java.lang.Byte test_Void_Byte()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Byte((String) res);
		}
		return (java.lang.Byte) res;
	}


	public java.lang.Character test_Void_Character()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Character", "public java.lang.Character test_Void_Character()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res=((String) res).charAt(0);
		}
		return (java.lang.Character) res;
	}


	public java.lang.Short test_Void_Short()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Short", "public java.lang.Short test_Void_Short()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Short((String) res);
		}
		return (java.lang.Short) res;
	}


	public java.lang.Integer test_Void_Integer()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Integer", "public java.lang.Integer test_Void_Integer()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Integer((String) res);
		}
		return (java.lang.Integer) res;
	}


	public java.lang.Long test_Void_Long()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Long", "public java.lang.Long test_Void_Long()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Long((String) res);
		}
		return (java.lang.Long) res;
	}


	public java.lang.Float test_Void_Float()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Float", "public java.lang.Float test_Void_Float()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Float((String) res);
		}
		return (java.lang.Float) res;
	}


	public java.lang.Double test_Void_Double()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Double", "public java.lang.Double test_Void_Double()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Double((String) res);
		}
		return (java.lang.Double) res;
	}


	public java.lang.Object test_Void_Object()
	{
		Object []obj=new Object[] {  };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_Object", "public java.lang.Object test_Void_Object()", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		return (java.lang.Object) res;
	}


	public boolean test_boolean_boolean(boolean a1)
	{
		Object []obj=new Object[] { Boolean.valueOf(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_boolean_boolean", "public boolean test_boolean_boolean(boolean)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'boolean'.");
		}
		if (res instanceof String)
		{
			res= new Boolean((String) res);
		}
		return ((Boolean) res).booleanValue();
	}


	public byte test_byte_byte(byte a1)
	{
		Object []obj=new Object[] { new Byte(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_byte_byte", "public byte test_byte_byte(byte)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'byte'.");
		}
		if (res instanceof String)
		{
			res= new Byte((String) res);
		}
		return ((Byte) res).byteValue();
	}


	public char test_char_char(char a1)
	{
		Object []obj=new Object[] { new Character(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_char_char", "public char test_char_char(char)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'char'.");
		}
		if (res instanceof String)
		{
			res=((String) res).charAt(0);
		}
		return ((Character) res).charValue();
	}


	public short test_short_short(short a1)
	{
		Object []obj=new Object[] { new Short(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_short_short", "public short test_short_short(short)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'short'.");
		}
		if (res instanceof String)
		{
			res= new Short((String) res);
		}
		return ((Short) res).shortValue();
	}


	public int test_int_int(int a1)
	{
		Object []obj=new Object[] { new Integer(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_int_int", "public int test_int_int(int)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'int'.");
		}
		if (res instanceof String)
		{
			res= new Integer((String) res);
		}
		return ((Integer) res).intValue();
	}


	public long test_long_long(long a1)
	{
		Object []obj=new Object[] { new Long(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_long_long", "public long test_long_long(long)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'long'.");
		}
		if (res instanceof String)
		{
			res= new Long((String) res);
		}
		return ((Long) res).longValue();
	}


	public float test_float_float(float a1)
	{
		Object []obj=new Object[] { new Float(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_float_float", "public float test_float_float(float)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'float'.");
		}
		if (res instanceof String)
		{
			res= new Float((String) res);
		}
		return ((Float) res).floatValue();
	}


	public double test_double_double(double a1)
	{
		Object []obj=new Object[] { new Double(a1) };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_double_double", "public double test_double_double(double)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'double'.");
		}
		if (res instanceof String)
		{
			res= new Double((String) res);
		}
		return ((Double) res).doubleValue();
	}


	public boolean test_Boolean_boolean(java.lang.Boolean a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Boolean_boolean", "public boolean test_Boolean_boolean(java.lang.Boolean)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'boolean'.");
		}
		if (res instanceof String)
		{
			res= new Boolean((String) res);
		}
		return ((Boolean) res).booleanValue();
	}


	public byte test_Byte_byte(java.lang.Byte a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Byte_byte", "public byte test_Byte_byte(java.lang.Byte)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'byte'.");
		}
		if (res instanceof String)
		{
			res= new Byte((String) res);
		}
		return ((Byte) res).byteValue();
	}


	public char test_Character_char(java.lang.Character a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Character_char", "public char test_Character_char(java.lang.Character)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'char'.");
		}
		if (res instanceof String)
		{
			res=((String) res).charAt(0);
		}
		return ((Character) res).charValue();
	}


	public short test_Short_short(java.lang.Short a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Short_short", "public short test_Short_short(java.lang.Short)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'short'.");
		}
		if (res instanceof String)
		{
			res= new Short((String) res);
		}
		return ((Short) res).shortValue();
	}


	public int test_Integer_int(java.lang.Integer a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Integer_int", "public int test_Integer_int(java.lang.Integer)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'int'.");
		}
		if (res instanceof String)
		{
			res= new Integer((String) res);
		}
		return ((Integer) res).intValue();
	}


	public long test_Long_long(java.lang.Long a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Long_long", "public long test_Long_long(java.lang.Long)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'long'.");
		}
		if (res instanceof String)
		{
			res= new Long((String) res);
		}
		return ((Long) res).longValue();
	}


	public float test_Float_float(java.lang.Float a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Float_float", "public float test_Float_float(java.lang.Float)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'float'.");
		}
		if (res instanceof String)
		{
			res= new Float((String) res);
		}
		return ((Float) res).floatValue();
	}


	public double test_Double_double(java.lang.Double a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Double_double", "public double test_Double_double(java.lang.Double)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res==null)
		{
			throw new ClassCastException("Result returned from RexxProxy '"+rpTarget+"' is 'null', which cannot be cast to the primitive return type 'double'.");
		}
		if (res instanceof String)
		{
			res= new Double((String) res);
		}
		return ((Double) res).doubleValue();
	}


	public java.lang.Boolean test_Boolean_Boolean(java.lang.Boolean a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Boolean_Boolean", "public java.lang.Boolean test_Boolean_Boolean(java.lang.Boolean)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Boolean((String) res);
		}
		return (java.lang.Boolean) res;
	}


	public java.lang.Byte test_Byte_Byte(java.lang.Byte a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Byte_Byte", "public java.lang.Byte test_Byte_Byte(java.lang.Byte)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Byte((String) res);
		}
		return (java.lang.Byte) res;
	}


	public java.lang.Character test_Character_Character(java.lang.Character a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Character_Character", "public java.lang.Character test_Character_Character(java.lang.Character)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res=((String) res).charAt(0);
		}
		return (java.lang.Character) res;
	}


	public java.lang.Short test_Short_Short(java.lang.Short a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Short_Short", "public java.lang.Short test_Short_Short(java.lang.Short)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Short((String) res);
		}
		return (java.lang.Short) res;
	}


	public java.lang.Integer test_Integer_Integer(java.lang.Integer a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Integer_Integer", "public java.lang.Integer test_Integer_Integer(java.lang.Integer)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Integer((String) res);
		}
		return (java.lang.Integer) res;
	}


	public java.lang.Long test_Long_Long(java.lang.Long a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Long_Long", "public java.lang.Long test_Long_Long(java.lang.Long)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Long((String) res);
		}
		return (java.lang.Long) res;
	}


	public java.lang.Float test_Float_Float(java.lang.Float a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Float_Float", "public java.lang.Float test_Float_Float(java.lang.Float)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Float((String) res);
		}
		return (java.lang.Float) res;
	}


	public java.lang.Double test_Double_Double(java.lang.Double a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Double_Double", "public java.lang.Double test_Double_Double(java.lang.Double)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		if (res instanceof String)
		{
			res= new java.lang.Double((String) res);
		}
		return (java.lang.Double) res;
	}


	public java.lang.Object test_Object_Object(java.lang.Object a1)
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Object_Object", "public java.lang.Object test_Object_Object(java.lang.Object)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

		return (java.lang.Object) res;
	}


	public void test_Void_ManyArguments(java.lang.String a1, boolean a2, byte a3, char a4, short a5, int a6, long a7, float a8, double a9, java.lang.Boolean a10, java.lang.Byte a11, java.lang.Character a12, java.lang.Short a13, java.lang.Integer a14, java.lang.Long a15, java.lang.Float a16, java.lang.Double a17, java.lang.Object a18)
	{
		Object []obj=new Object[] { a1, Boolean.valueOf(a2), new Byte(a3), new Character(a4), new Short(a5), new Integer(a6), new Long(a7), new Float(a8), new Double(a9), a10, a11, a12, a13, a14, a15, a16, a17, a18 };
		Object    res=null;
		RexxProxy rpTarget=(target==null ? defaultHandler : target);
		try {
			res=rpTarget.invoke(this, "test_Void_ManyArguments", "public void test_Void_ManyArguments(java.lang.String,boolean,byte,char,short,int,long,float,double,java.lang.Boolean,java.lang.Byte,java.lang.Character,java.lang.Short,java.lang.Integer,java.lang.Long,java.lang.Float,java.lang.Double,java.lang.Object)", obj);
		}
		catch (Throwable t) { throw new RuntimeException("Throwable caused by using 'RexxProxy.invoke(...)' with '"+rpTarget+"'", t); }

	}

}

