import javax.script.*;
import java.io.FileReader;

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.jsr223.*;


/** Demonstrate using the Invocable methods.
 */
public class Nutshell_03_Interface
{
    public static void main (String args[])
    {
        // TODO: change to use ScriptManager to fetch Rexx script enginge
        RexxScriptEngine rse= new RexxScriptEngine();

        try
        {
            String filename="nutshell_03.rex";
            Bindings b=rse.createBindings();
            b.put(ScriptEngine.FILENAME,filename);

                // use public <T> T getInterface(Class<T> clasz)
            System.err.println("(Java) using \"<T> T getInterface(Class<T> clasz)\" using the <RexxScriptEngine>:");
            System.err.println("(Java) executing ["+filename+"] fetching resulting ooRexx object:");
            RexxProxy rp=(RexxProxy) rse.eval(new FileReader(filename), b);  // now let us execute the Rexx script
            System.err.println("---");

            Runnable r=rse.getInterface(Runnable.class);    // get an interface implementation
            System.err.println("(Java) creating new Thread supplying ["+r+"]:");
            Thread th = new Thread(r);
            th.start();
            th.sleep(100);  // should be enough time for the thread to end (after running the Rexx code)
            System.err.println("---");

                // use public <T> T getInterface(Object thiz, Class<T> clasz)
            System.err.println("(Java) using \"<T> T getInterface(Object thiz, Class<T> clasz)\" using the <RexxScriptEngine>:");
            r=rse.getInterface(rp, Runnable.class);         // get an interface implementation
            System.err.println("(Java) creating new Thread supplying ["+r+"]:");
            r=rse.getInterface(rp, Runnable.class);    // get an interface implementation
            th = new Thread(r);
            th.start();
            th.sleep(100);  // should be enough time for the thread to end (after running the Rexx code)
            System.err.println("---");

            System.err.println("--- now using the RexxCompiledScript methods instead ---");
            System.err.println("---");
            RexxCompiledScript rcs=rse.getCurrentScript();  // get current script

                // use public <T> T getInterface(Class<T> clasz)
            System.err.println("(Java) using \"<T> T getInterface(Class<T> clasz)\" using the <RexxCompiledScript>:");
            r=rcs.getInterface(Runnable.class);    // get an interface implementation
            System.err.println("(Java) creating new Thread supplying ["+r+"]:");
            th = new Thread(r);
            th.start();
            th.sleep(100);  // should be enough time for the thread to end (after running the Rexx code)
            System.err.println("---");

                // use public <T> T getInterface(Object thiz, Class<T> clasz)
            System.err.println("(Java) using \"<T> T getInterface(Object thiz, Class<T> clasz)\" using the <RexxCompiledScript>:");
            r=rcs.getInterface(rp, Runnable.class);         // get an interface implementation
            System.err.println("(Java) creating new Thread supplying ["+r+"]:");
            r=rcs.getInterface(rp, Runnable.class);    // get an interface implementation
            th = new Thread(r);
            th.start();
            th.sleep(100);  // should be enough time for the thread to end (after running the Rexx code)


/*
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
*/

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

