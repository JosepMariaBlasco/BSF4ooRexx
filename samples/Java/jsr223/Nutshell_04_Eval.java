import javax.script.*;
import java.awt.Dimension;
import java.io.FileReader;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.jsr223.*;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015 Rony G. Flatscher

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
*/

/** Demonstrate evaluating a Rexx script, once without supplying arguements and once supplying
 *  arguments using the scripting language's default <code>ScriptContext</code>.
 *
 *  @author Rony G. Flatscher
 *  @since 2015-06-07
 */
public class Nutshell_04_Eval
{
    public static void main (String args[])
    {
        // direct creation of a RexxScriptEngine
        // RexxScriptEngine rse= new RexxScriptEngine();

        // Using a ScriptManager to fetch the Rexx script enginge
        ScriptEngineManager manager = new ScriptEngineManager();
        RexxScriptEngine rse=(RexxScriptEngine) manager.getEngineByName("Rexx");

        try
        {
            ScriptContext sc=rse.getContext();  // get the default ScriptContext

            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSSSS").format(new Date());
            sc.setAttribute("started_at", timeStamp, ScriptContext.GLOBAL_SCOPE);

                // add the fileName to the ENGINE_SCOPE bindings
            String filename="nutshell_04a.rex";
            sc.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);

            Dimension d1= new Dimension( 25, 30),
                      d2= new Dimension( 50, 30),
                      d3= new Dimension( 75, 60),
                      d4= new Dimension(100, 60),
                      d5= new Dimension(125, 90),
                      d6= new Dimension(150, 90),
                      d7= new Dimension(175,120),
                      d8= new Dimension(200,120),
                      dimArr[]={d1,d2,d3,d4,d5,d6,d7,d8};

                // define four Dimension objects that Rexx should use to add up the widht and the height and return a new Dimension object
            sc.setAttribute("d1", d1, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d2", d2, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d3", d3, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d4", d4, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d5", d5, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d6", d6, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d7", d7, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute("d8", d8, ScriptContext.ENGINE_SCOPE);

            System.err.println("(Java) executing ["+filename+"] without arguments:");
            Dimension obj=(Dimension) rse.eval(new FileReader(filename), sc);  // now let us execute the Rexx script

            // Dimension obj=(Dimension) rse.getCurrentScript().eval(sc);         // now let us re-execute the Rexx script
            System.out.println("(Java) Evaluated Rexx script ["+filename+"] returned: ["+obj.toString()+"]");

            System.out.println("--------------------");
            // now evaluate "nutshell_04b.rex"!
            filename="nutshell_04b.rex";
            sc.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);
            System.err.println("(Java) executing ["+filename+"] without arguments:");
            obj=(Dimension) rse.eval(new FileReader(filename), sc);  // now let us execute the Rexx script
            System.out.println("(Java) Evaluated Rexx script ["+filename+"] returned: ["+obj.toString()+"]");

            System.out.println("--------------------");
            // now evaluate "nutshell_04c.rex"!
            filename="nutshell_04c.rex";
            sc.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);
            sc.setAttribute(ScriptEngine.ARGV,     dimArr,   ScriptContext.ENGINE_SCOPE);
            System.err.println("(Java) executing ["+filename+"] (Rexx program contains the Rexx script annotation /* @get(d1 d2 d3 d4 d5 d6 d7 d8 started_at) */ to fetch all attributes from the ScriptContext Bindings as Rexx variables):");
            obj=(Dimension) rse.eval(new FileReader(filename), sc);  // now let us execute the Rexx script
            System.out.println("(Java) Evaluated Rexx script ["+filename+"] returned: ["+obj.toString()+"]");

            System.out.println("--------------------");
            // now evaluate "nutshell_04d.rex"!
            filename="nutshell_04d.rex";
            sc.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);
            // add the ARGV entry which will cause the RexxScriptEngine to supply the array elements as
            // individual arguments; it then will append another Rexx directory with an entry "ScriptContext"
            sc.setAttribute(ScriptEngine.ARGV,     dimArr,   ScriptContext.ENGINE_SCOPE);
            System.err.println("(Java) executing ["+filename+"] WITH arguments (ScriptEngine.ARGV):");
            obj=(Dimension) rse.eval(new FileReader(filename), sc);  // now let us execute the Rexx script
            System.out.println("(Java) Evaluated Rexx script ["+filename+"] returned: ["+obj.toString()+"]");
        }
        catch (Exception exc)
        {
            // exc.printStackTrace();
            System.err.println(exc);
            System.exit(-1);
        }
    }
}

