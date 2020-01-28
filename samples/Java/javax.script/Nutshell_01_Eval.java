import javax.script.*;
import java.io.FileReader;

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.jsr223.*;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2020 Rony G. Flatscher

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

    changed:    2020-01-26, rgf: spelling of "Rexx" to "rexx"
*/

/** Demonstrate evaluating a Rexx script, once without supplying arguements and once supplying
 *  arguments using the scripting language's default <code>ScriptContext</code>.
 *
 *  @author Rony G. Flatscher
 *  @since 2015-05-12
 */
public class Nutshell_01_Eval
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
            ScriptContext sc=rse.getContext();  // get the default ScriptContext
                // add the fileName to the ENGINE_SCOPE bindings
            String filename="nutshell_01.rex";
            sc.setAttribute(ScriptEngine.FILENAME,filename,ScriptContext.ENGINE_SCOPE);

            System.err.println("(Java) executing ["+filename+"] without arguments:");
            rse.eval(new FileReader(filename), sc);  // now let us execute the Rexx script

            System.err.println("-------------------------");
            System.err.println("(Java) executing ["+filename+"], now with four arguments, the third one being 'null':");
                // add arguments for the script to the ENGINE_SCOPE bindings
            sc.setAttribute(ScriptEngine.ARGV, new Object[] {"one", "zwei", null, "quatre"}, ScriptContext.ENGINE_SCOPE);
                // the RexxScriptEngine always compiles the last script and makes it available with the getCurrentScript() method
            rse.getCurrentScript().eval();         // now let us re-execute the Rexx script
        }
        catch (Exception exc)
        {
            // exc.printStackTrace();
            System.err.println(exc);
            System.exit(-1);
        }
    }
}

