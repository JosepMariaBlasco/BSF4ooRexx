package org.rexxla.bsf.engines.rexx.jsr223;
import java.util.Vector;
import javax.script.*;

import org.apache.bsf.BSFException;             // BSF support

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxProxy;

/** Class that extends the <code>javax.script.CompiledScript</code> class and implements
 *  the <code>javax.script.Invocable</code> interface as this only makes sense in this
 *  class for {@link public <T> T getInterface(Class<T> clasz)}. Therefore the author
 *  (Rony G. Flatscher) who served on the JSR-223 specification committee) thinkgs that the documentation
 *  in <code>javax.script.Invocable</code> is false, as it states that <code>Invocable</code> gets implemented by a
 *  <code>ScriptEngine</code>, somehow indicating it has to operate on some &quot;state&quot; of the script engine.
 *  <br>Please note: the JSR-223 specification does not imply that ScriptEngine should implement this
 *  interface!
 * <br>The [oo]Rexx implementation of JSR-223 tries to be as flexible as possible and also implements
 * this interface in its implementation of <code>ScriptEngine</code> sharing as much code between
 * the tow (and caching compiled scripts for invoking the aforementioned method in a sensible way.
 *
 * @author Rony G. Flatscher
 * @since  2015-05-11
 *
 */
public class RexxCompiledScript extends CompiledScript implements Invocable
{

    private static final boolean bDebug=false; // true; // false; // true;

    /** Field that refers to the RexxScriptEngine instance which created this RexxCompiledScript. */
    RexxScriptEngine rse;


    /** If set to <code>true</code> the <code>BSFException</code> (may contain a
     *  <code>RexxException</code> as cause) will be used to create and throw the <code>ScriptException</code>.
     * If set to <code>false</code> the (Rexx formatted) message and the line number, if available, are used
     * to create and throw the <code>ScriptException</code>.
     */
    boolean bSupplyBSFExceptionObject=false;

    /** Getter for the Rexx script which got compiled (tokenized).
     *  @return the string representing of the compiled Rexx script
    */
    public boolean getSupplyBSFExceptionObject()
    {
        return bSupplyBSFExceptionObject;
    }

    /** Setter for the Rexx script which got compiled (tokenized).
     *  @return the string representing of the compiled Rexx script
    */
    public void setSupplyBSFExceptionObject(boolean newValue)
    {
        bSupplyBSFExceptionObject=newValue;
    }



    /** The filename used for evaluating (executing) the script or invoking functions or methods from the script */
    String filename;

    public String getFilename()
    {
        return filename;
    }

    /** Getter for the Rexx script which got compiled (tokenized).
     *  @return the string representing of the compiled Rexx script
    */

    /** The script that was compiled (tokenized). */
    String scriptSource;

    /** Getter for the Rexx script which got compiled (tokenized).
     *  @return the string representing of the compiled Rexx script
    */
    public String getScriptSource()
    {
        return scriptSource;
    }

    /** The RexxProxy representing the compiled (tokenized) script (a Rexx routine object). */
    RexxProxy rexxScriptObject;

    /** Getter for the RexxProxy representing the Rexx routine object the script got compiled (tokenized) into.
     * @return a RexxProxy representing the Rexx routine object
     */
    public RexxProxy getScriptObject()
    {
        return rexxScriptObject;
    }


    /** A RexxProxy representing the script's package (a Rexx package object). */
    RexxProxy rexxPackageObject;

    /** Getter for the RexxProxy representing the Rexx routine object the script got compiled (tokenized) into.
     * @return a RexxProxy representing the Rexx package object of this compiled script
     */
    public RexxProxy getPackageObject()
    {
        return rexxPackageObject;
    }



    RexxCompiledScript(RexxScriptEngine scriptEngine,
                       String    scriptSource,
                       String    filename,
                       boolean   bSupplyBSFExceptionObject,
                       RexxProxy scriptRexxProxy,
                       RexxProxy scriptRexxPackage)
    {
        super();
        this.rse                      =scriptEngine;
        this.scriptSource             =scriptSource;
        this.filename                 =filename;
        this.bSupplyBSFExceptionObject=bSupplyBSFExceptionObject;
        this.rexxScriptObject         =scriptRexxProxy;
        this.rexxPackageObject        =scriptRexxPackage;
    }

    /** Executes the program stored in this RexxCompiledScript object.
     */
    public Object eval(ScriptContext sc) throws ScriptException
    {
if (bDebug==true) { System.err.println("---> RCS ---> eval(), sc=["+sc+"]");}
        if (sc==null)
        {
            throw new NullPointerException("Received \"ScriptContext sc\" argument null");
        }

        Object arrArgV[];
        try
        {
            arrArgV=createArgV((Object[])sc.getAttribute(ScriptEngine.ARGV), sc, rse);
        }
        catch (BSFException exc)
        {
            throw createScriptException(exc);
        }

if (bDebug==true)
{
    System.err.println("\t eval(), double check, arrArgV.length=["+arrArgV.length+"]");
    for (int i=0;i<arrArgV.length;i++)
    {
        System.err.println("\t\tarrArgV["+i+"]=["+arrArgV[i]+"]");
    }
}

        String filename=(String) sc.getAttribute(ScriptEngine.FILENAME);
        if (filename==null)     // no filename given
        {
            filename="filename_created_by_"+this.toString().replace('.','_').replace('@','-')+"_at_"+RexxScriptEngine.getGMTFormattedDate4Filename();
        }
if (bDebug==true) { System.err.println("---> RCS ---> filename=["+filename+"]");}

        try {
if (bDebug==true) { System.err.println("... eval(): before rse.updateRexxEngine(sc)");}

            rse.updateRexxEngine(sc);   // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly
if (bDebug==true)
{
    System.err.println("... eval(): after rse.updateRexxEngine(sc)");
    for (int i=0;i<arrArgV.length;i++)
    {
        System.err.println("... ... RexxCompiledScript.eval(), arrArgV["+i+"]=["+arrArgV[i]+"]");
    }
    System.err.println("... eval(): rexxScriptObject=["+rexxScriptObject+"]");
}
            return rexxScriptObject.sendMessage1("CALLWITH", arrArgV);  // execute routine object
        }
        catch (BSFException exc)
        {
            throw createScriptException(exc);
        }
    }



    /** Returns the ScriptEngine whose compile method created this CompiledScript.
     */
    public ScriptEngine getEngine()
    {
        return rse;
    }


// -----------------------------------------------------------------------------------------------------
// Invocable methods
    /** Returns an implementation of an interface using functions compiled in the interpreter.
     */
    public <T> T getInterface(Class<T> clasz)
    {

        if (clasz==null)
        {
            throw new IllegalArgumentException("Received 'clasz' argument is null");
        }
        if (! (clasz.isInterface()) )
        {
            throw new IllegalArgumentException("Received 'clasz' argument is not an interface class");
        }

        ScriptContext sc=rse.getContext();  // get default ScriptContext
/*
        // define a Rexx class that gets the routine object and its package object
        // to dispatch Interface methods iva publich Rexx routines in its unknown method
        String code=
            "use arg rexxScript, rexxPackage, scriptContext                                                             \n" +
            "    -- create and return dispatcher object                                                                 \n" +
            "return .jsr223.DispatchInterfaceMethodsToPublicRexxRoutines~new(rexxScript, rexxPackage, scriptContext)    \n" +
            "                                                                                                           \n" +
            "::requires BSF.CLS                              -- get Java camouflaging support                           \n" +
            "                                                                                                           \n" +
            "    -- dispatcher class for Invocable' <T> T getInterface(Class<T> clasz)                                  \n" +
            "::class 'jsr223.DispatchInterfaceMethodsToPublicRexxRoutines'                                              \n" +
            "                                                                                                           \n" +
            "::method init              -- constructor                                                                  \n" +
            "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                            \n" +
            "  use strict arg rexxScriptObject, rexxScriptPackage, jsr223ScriptContext                                  \n" +
            "                                                                                                           \n" +
            "::method unknown           -- dispatch public routines instead                                             \n" +
            "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                            \n" +
            "  use strict arg methName, methArrArgv          -- unknown message name and its arguments                  \n" +
            "                                                                                                           \n" +
            "  pubRoutines=rexxScriptPackage~publicRoutines  -- get the public routines directory                       \n" +
            "  if pubRoutines~hasentry(methName)=.false then -- if public routine not available raise error             \n" +
            "     raise syntax 93.1 array (methName)         -- routine not found!                                      \n" +
            "                                                                                                           \n" +
            "  -- add ScriptContext to BSF4ooRexx' slot argument (a Rexx directory added as last argument on callbacks) \n" +
            "  methArrArgv~lastItem~setentry('SCRIPTCONTEXT', jsr223ScriptContext) -- add ScriptContext                 \n" +
            "                                                                                                           \n" +
            "  pubRoutines~callWith(methName, methArrArgv)   -- invoke routine                                          \n" +
            "  if var('RESULT') then return result           -- if a result object got returned, return it              \n" ;

        Vector argVec=new Vector();     // define and set the arguments
        argVec.add(rexxScriptObject);
        argVec.add(rexxPackageObject);
            // get and supply the default ScriptContext
        argVec.add(sc);                     // supply it as last argument
*/

        RexxProxy rp;
        try {
                // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly
            rse.updateRexxEngine(sc);

                // create and get the ooRexx dispatching object, working on the available public routines
            // rp=(RexxProxy) rse.getBsfRexxEngine().apply(this.toString()+"_create_interface_to_public_routine_dispatching_object", 0, 0, code, null, argVec); // new Vector(), new Vector());

if (bDebug==true) { System.err.println("---> ---> RexxCompiledScript() BEVOR trying to get access to object."); }

            rp=(RexxProxy) rse.getRoutineWrapper().sendMessage3("CALL", getScriptObject(), getPackageObject(), sc);

if (bDebug==true) { System.err.println("<--- <--- RexxCompiledScript() AFTER trying to get access to object."); }
        }
        catch (Exception exc)        // if a BSFException, return null
        {
                // cannot throw a ScriptException because it would violate the Interface definition of this method;
                // try to inform the user anyway and give as much useful information as possible about the exception
            System.err.println("Exception ["+exc+"] raised while trying to create dispatching object, cause: ["+exc.getCause()+"]");
            System.err.println("Exception ["+exc+"], stack trace:");
            exc.printStackTrace();
            System.err.println("Exception ["+exc+"], returning null to indicate failure");
            return null;
        }

        //  now create a JavaProxy where the Interface method invocations will forward a message by
        //  the name of the invoked method to the contained Rexx object
        Object[] interfaces = new Object[1];
        interfaces[0]=clasz;

        return (T) rp.newJavaProxyInstance(interfaces);     // create and return the Java proxy for the interface
    }


    /** Returns an implementation of an interface using member functions of a scripting object compiled in the interpreter.
     *  Uses the implementation in {@link RexxScriptEngine} to not duplicate code (and thereby making maintenance safer).
    */
    public <T> T getInterface(Object thiz, Class<T> clasz)
    {

// TODO: - use a special class to embed thiz, which then adds the default SC as (second to?) last argument, such that
//         the methods can get access to the defualt SC?
//       - add an attribute jsr223ScriptContext, such that it can be changed later on?

            // use the implementation in RexxScriptEngine as this method is not CompiledScript related
        return rse.getInterface(thiz,clasz);
    }

    /** Used to call top-level procedures and functions (accessible public Rexx routines) defined in scripts. */
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException
    {
        if (name==null)
        {
            throw new NullPointerException("Received 'name' argument (function name) is null");
        }

        try {
            RexxProxy publicRoutines=(RexxProxy) rexxPackageObject.sendMessage0("PUBLICROUTINES");
            String result=(String) publicRoutines.sendMessage1("HASENTRY", name);

if (bDebug==true) { System.err.println("... ["+this+"]: checking for public routine \""+name+"\": result=["+result+"], result.getClass()="+result.getClass());}

            if ( result.equals("0")==true)    // returned .false ?
            {
                throw new NoSuchMethodException("Received 'name' argument \""+name+"\" does not denote an existing, public Rexx routine");
            }

                // get the public routine, prepare arguments, invoke it
            Object arrArgV[]=createArgV(args, rse.getContext(), rse);    // supply default context
            RexxProxy routine=(RexxProxy) publicRoutines.sendMessage1("ENTRY",name);    // get routine
            return routine.sendMessage1("CALLWITH",arrArgV);
        }
        catch (Exception exc)
        {
        //    throw new ScriptException(exc);
            throw createScriptException(exc);       // create ScriptException and throw it
        }
    }


    /** Calls a method on a script object compiled during a previous script execution, which is retained in the state of the ScriptEngine.
     *  Uses the implementation in {@link RexxScriptEngine} to not duplicate code (and thereby making maintenance safer).
     *
    */
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException
    {
            // use the implementation in RexxScriptEngine as this method is not CompiledScript related
        return rse.invokeMethod(thiz, name, args);
    }


    // --------------- utility methods

    ScriptException createScriptException(Exception exc)
    {
        if (bSupplyBSFExceptionObject==true )    // return a plain ScriptException supplying the causing exception
        {
            return new ScriptException(exc);
        }

            // just a plain BSFException or RexxException without the Rexx condition object: return causing exception
        if (! (exc instanceof RexxException) || (((RexxException)exc).getRexxConditionObject()==null) )
        {
            return new ScriptException(exc);
        }

        // o.k., we are supposed to fill in as much information as possible, instead of supplying the BSFException
        RexxProxy rco=((RexxException)exc).getRexxConditionObject();    // get Rexx condition object

        // TODO: create the values
        String message =createRexxLikeConditionMessage(rco);
        String filename="n/a";
        String position=null;
        try {
            filename=(String)rco.sendMessage0("PROGRAM");
            position=(String)rco.sendMessage0("POSITION");
        }
        catch (Exception bsfexc) {}

        int    line    =-1;      // indicate unknown line

        if (position instanceof String)
        {
            line= Integer.valueOf(position).intValue();
        }
        return new ScriptException(message, filename, line);
    }



    /** Utility method which should (after debugging) be moved to RexxException to serve all Java clients.
     *  Modelled after <code>BSF4ooRexx.cc</code>'s  <code>RgfCreateRexxlikeErrorInfo</code> function.
     *  @param rco a Rexx proxy allowing access to the Rexx condtion object (a Rexx directory)
     */
    String createRexxLikeConditionMessage(RexxProxy rco)
    {
        StringBuffer sb=new StringBuffer();
        String conditionName=null;

        try
        {
            conditionName=(String) rco.sendMessage0("CONDITION");
            if (conditionName.equals("SYNTAX")) // format a syntax condition
            {
                // line # 1
                sb.append((String) ((RexxProxy) rco.sendMessage0("TRACEBACK")).sendMessage0("FIRSTITEM"));
                sb.append('\n');

                // line # 2
                sb.append(String.format("Error %s running %s line %s:  %s",
                                        (String) rco.sendMessage0("RC"),
                                        (String) rco.sendMessage0("PROGRAM"),
                                        (String) rco.sendMessage0("POSITION"),
                                        (String) rco.sendMessage0("ERRORTEXT")));
                sb.append('\n');
                sb.append(String.format("Error %s:  %s",
                                        (String) rco.sendMessage0("CODE"),
                                        (String) rco.sendMessage0("MESSAGE")));
            }

            else  if (conditionName.equals("NOVALUE"))  // format a novalue condition
            {
                // line # 1
                sb.append((String) ((RexxProxy) rco.sendMessage0("TRACEBACK")).sendMessage0("FIRSTITEM"));
                sb.append('\n');

                // line # 2
                sb.append(String.format("Rexx condition [%s] raised for variable [%s] running %s line: %s",
                                        conditionName,
                                        (String) rco.sendMessage0("DESCRIPTION"),
                                        (String) rco.sendMessage0("PROGRAM"),
                                        (String) rco.sendMessage0("POSITION")));
            }
                // as of ooRexx 4.2.0 for "failure", "error", "user": only entries CONDITION, RC (for failure and error) available
            else
            {
                // line # 1
                sb.append(String.format("Rexx condition [%s] raised running %s",
                                        conditionName, filename ));

                if (! (conditionName.startsWith("USER")))   // a failure or error condition has a RC value going with it
                {
                    sb.append(String.format(", return code [%s]",
                              (String) rco.sendMessage0("RC")));
                }

            }
        }
        catch (Exception exc)
        {
            throw new IllegalArgumentException("accessing argument \"rco\" with conditionname ["+conditionName+"] caused the exception: ["+exc+"]");
        }
        return sb.toString();
    }


    /** Creates and returns the argument array, append ScriptContext sc as last argument
     *  for the Rexx script to be able to fetch it. As of ooRexx 4.2.0 the routine's <code>CALLWITH</code>
     *  method sends a <code>MAKEARRAY</code> message which will compact the Java array by removing <code>null</code>
     *  values (behaviour of {@link ArrayWrapper} to mimickry the ooRexx <code>Array MAKEARRAY</code> behaviour
     *  (ooRexx arrays are capapble of distinguishing whether an entry is empty or is explicitly set
     *  to <code>.nil</code>). To keep the argument array intact when there are Java array entries with the
     *  value <code>null</code> a {@link RexxProxy} for the ooRexx <code>.nil</code> object is used for replacement.
     *  the Rexx code fetching such an argument needs to unwrap the contained ooRexx <code>.nil</code> object using
     *  the external BSF function <code>BsfRexxProxy(rexxProxy,"ooRexxObject")</code>.
     *
    *   @param args Object array with arguments to be supplied to the Rexx script
     *  @param sc ScriptContext to be appended to the arguments for the Rexx script
     *  @return an Object array containing all arguments to be passed on to the Rexx script
    */
    static Object[] createArgV(Object[] args, ScriptContext sc, RexxScriptEngine rse) throws ScriptException, BSFException
    {
        int argsLength= (args!=null ? args.length : 0);
        Object arrArgV[]=new Object[argsLength+1];
        int i=0;

        if (args!=null && args.length>0)    // if ARGV array is given and has entries, then create argument vector
        {
            Object rexxNil=rse.getNil();
            for (i=0; i<args.length; i++)            // loop over arguments
            {

if (bDebug) { System.err.println("===> createArgV(), args["+i+"]=["+args[i]+"]"); }

                Object o=args[i];
                arrArgV[i]=( o==null ? rexxNil : o );
            }
        }

            // add as last argument, so scripts can always fetch the current ScriptContext;
            // make sure last argument is a Rexx directory, with an entry "SCRIPTCONTEXT" storing the ScriptContext object
            // (this way Rexx routines - even those invoked from JavaProxy invocations - can rely that
            // the last argument is a directory containing an entry "SCRIPTCONTEXT")

if (bDebug)
{
   System.err.println("===> createArgV(), about to add ScriptContext argument!");
   RexxProxy gcd=rse.getCreateDirectory();
   System.err.println("                   gcd=["+gcd+"]");
}

        arrArgV[i]=rse.getCreateDirectory().sendMessage2("CALL","SCRIPTCONTEXT",sc);
        return arrArgV;
    }


}
