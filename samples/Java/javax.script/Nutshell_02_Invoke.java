import javax.script.*;
import java.io.FileReader;

import org.rexxla.bsf.engines.rexx.*;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2022 Rony G. Flatscher

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-----------------------------------------------------------------------------

    changed:    - 2020-01-26, rgf: spelling of "Rexx" to "rexx"
                - 2022-01-29, rgf: expicitly do a System.exit(0)
                - 2022-08-06, rgf: 'org.rexxla.bsf.engines.rexx.jsr223.' got changed to
                                   'org.rexxla.bsf.engines.rexx': adjust import statement
*/

/** Demonstrate the usage of the <code>Invocable</code> methods, namely <code>invokeMethod(...)</code>
 *  and <code>invokeFunction(...)</code>.
 *
 *  @author Rony G. Flatscher
 *  @since 2015-05-12
 */
public class Nutshell_02_Invoke
{
    public static void main (String args[])
    {
        // direct creation of a RexxScriptEngine
        // RexxScriptEngine rse= new RexxScriptEngine();

        // Using a ScriptManager to fetch the Rexx script enginge
        ScriptEngineManager manager = new ScriptEngineManager();
        RexxScriptEngine rse=(RexxScriptEngine) manager.getEngineByName("rexx");

        try
        {
            String filename="nutshell_02.rex";
            Bindings b=rse.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
            if (b==null)        // if no ENGINE_SCOPE bindings available, create a Bindings
            {
                b=rse.createBindings();
            }
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
        System.exit(0);
    }
}

