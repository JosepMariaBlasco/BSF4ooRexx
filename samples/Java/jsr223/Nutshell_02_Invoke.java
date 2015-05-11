import javax.script.*;
import java.io.FileReader;

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.jsr223.*;


/** Demonstrate using the Invocable methods.
 */
public class Nutshell_02_Invoke
{
    public static void main (String args[])
    {
        // TODO: change to use ScriptManager to fetch Rexx script enginge
        RexxScriptEngine rse= new RexxScriptEngine();

        try
        {
            String filename="nutshell_02.rex";
            Bindings b=rse.createBindings();
            b.put(ScriptEngine.FILENAME,filename);

            System.err.println("(Java) executing ["+filename+"] fetching resulting ooRexx object:");
            RexxProxy rp=(RexxProxy) rse.eval(new FileReader(filename), b);  // now let us execute the Rexx script

                // use invokeMethod(...)
            System.err.println("(Java) using invokeMethod() using the <RexxScriptEngine>:\n");
            System.err.println("(Java) invokeMethod('one'): "+rse.invokeMethod(rp, "ONE"));
            System.err.println("(Java) invokeMethod('two'): "+rse.invokeMethod(rp, "two", "un", "deux", "trois"));
            System.err.println("(Java) invokeMethod('now'): "+rse.invokeMethod(rp, "now"));
            System.err.println("---\n");

                // use invokeFunction(...)
            System.err.println("(Java) using invokeFunction() using the <RexxScriptEngine>:\n");
            System.err.println("(Java) invokeFunction('routine_eins'): "+rse.invokeFunction("ROUTINE_EINS"));
            System.err.println("(Java) invokeFunction('routine_zwei'): "+rse.invokeFunction("routine_zwei", "eins", "zwei", "drei"));
            System.err.println("---\n");

            RexxCompiledScript rcs=rse.getCurrentScript();  // get current script

                // use invokeMethod(...)
            System.err.println("(Java) using invokeMethod() using the the <RexxCompiledScript> directly:\n");
            System.err.println("(Java) invokeMethod('one'): "+rcs.invokeMethod(rp, "ONE"));
            System.err.println("(Java) invokeMethod('two'): "+rcs.invokeMethod(rp, "two", "un", "deux", "trois"));
            System.err.println("(Java) invokeMethod('now'): "+rcs.invokeMethod(rp, "now"));
            System.err.println("---\n");

                // use invokeFunction(...)
            System.err.println("(Java) using invokeFunction() using the the <RexxCompiledScript> Rexx script> directly:\n");
            System.err.println("(Java) invokeFunction('routine_eins'): "+rcs.invokeFunction("ROUTINE_EINS"));
            System.err.println("(Java) invokeFunction('routine_zwei'): "+rcs.invokeFunction("routine_zwei", "eins", "zwei", "drei"));
            System.err.println("---\n");


/*
                // this should cause an exception, because the method 'three' does not exist
            System.err.println("(Java) using invokeMethod() using the <RexxScriptEngine>:\n");
            System.err.println("(Java) invokeMethod('three'): "+rse.invokeMethod(rp, "three"));

                // this should cause an exception, because 'routine_drei' is defined as private
            System.err.println("(Java) using invokeFunction() using the <RexxScriptEngine>, supplying an inaccessible (private) routine:");
            System.err.println("(Java) invokeFunction('routine_drei'): "+rse.invokeFunction("routine_drei"));
*/
        }
        catch (Exception exc)
        {
            // exc.printStackTrace();
            System.err.println(exc);
            System.exit(-1);
        }
    }
}

