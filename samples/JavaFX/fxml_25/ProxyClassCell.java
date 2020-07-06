/// ClassAdapter.createProxiedJavaClass(...) ---> begin of generated code for class: [startEdit], now: [startEdit]
import org.rexxla.bsf.engines.rexx.RexxProxy; 
import org.apache.bsf.BSFException; 

public class startEdit extends javafx.scene.control.Cell
{
	public  static          Object    classLock     =new Object(); 
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


	public synchronized RexxProxy getTargetRexxProxy() 
	{
		return target; 
	}


	public synchronized void setTargetRexxProxy(RexxProxy newTarget) 
	{
		target=newTarget; 
	}


	// --- creating methods according to String array of methods to proxy (if any) 

	public void commitEdit(java.lang.Object a1) 
	{
		Object []obj=new Object[] { a1 };
		Object    res=null;     
		RexxProxy rpTarget=(target==null ? defaultHandler : target);     
		try {
			res=rpTarget.invoke(this, "commitEdit", "public void commitEdit(java.lang.Object)", obj); 
		} 
		catch (BSFException be)
		{ 
			throw new RuntimeException(be.toString(), be); 
		}
	}


	public void commitEdit_forwardToSuper(java.lang.Object a1) 
	{
		super.commitEdit(a1);    
	}


	public void cancelEdit() 
	{
		Object []obj=new Object[] {  };
		Object    res=null;     
		RexxProxy rpTarget=(target==null ? defaultHandler : target);     
		try {
			res=rpTarget.invoke(this, "cancelEdit", "public void cancelEdit()", obj); 
		} 
		catch (BSFException be)
		{ 
			throw new RuntimeException(be.toString(), be); 
		}
	}


	public void cancelEdit_forwardToSuper() 
	{
		super.cancelEdit();    
	}


	public void updateItem(java.lang.Object a1, boolean a2) 
	{
		Object []obj=new Object[] { a1, Boolean.valueOf(a2) };
		Object    res=null;     
		RexxProxy rpTarget=(target==null ? defaultHandler : target);     
		try {
			res=rpTarget.invoke(this, "updateItem", "public void updateItem(java.lang.Object,boolean)", obj); 
		} 
		catch (BSFException be)
		{ 
			throw new RuntimeException(be.toString(), be); 
		}
	}


	public void updateItem_forwardToSuper(java.lang.Object a1, boolean a2) 
	{
		super.updateItem(a1, a2);    
	}


	// --- creating base (default) constructor 

	public startEdit() 
	{
		super();                    
		target=defaultHandler;      
	}


	public startEdit(RexxProxy rexxproxy)  
	{
		super();                 
		target=rexxproxy;        
		if (defaultHandler==null)   
		{                           
			defaultHandler=rexxproxy; 
		}                           
	}


	// --- creating constructors from existing public and protected constructors using [javafx.scene.control.Cell] (if any) 
}

<--- end of generated code \\\
clz=[class startEdit]
creating source  (in milliseconds): 31
compilation time (in milliseconds): 138
load time        (in milliseconds): 0
