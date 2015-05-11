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
        }
        catch (Exception exc)
        {
            // exc.printStackTrace();
            System.err.println(exc);
            System.exit(-1);
        }
    }
}

