using System;
using System.IO;
using System.Linq;
using System.Reflection;


/** author: Rony G. Flatscher
 *  date:   2016-08-13
 *  version: 100.20160813
 *  purpose: allow a gacutil.exe like program to install and remove an assembly 
 *  remarks:    - a console application 
 *              - changed signature of Main() to return an int (as a return code) instead of using Environment.Exit(returnCode);
 *              - minimum: .Net Framework 4.5 (since 2012-08-15, Windows 8; cf. <https://en.wikipedia.org/wiki/.NET_Framework>)
 *              - a reference to the assembly named "System.EnterpriseServices" must be added: 
 *                Project -> Add References ... -> Assemblies -> System.EnterpriseServices (check it)
 *              - execute with Administrator rights, then: 
 *                Project -> Add New Item ... -> "Application Manifest File" press <Add>-key, if needed, change 
 *                '<requestedExecutionLevel  level="asInvoker" uiAccess="false" />' to 
 *                '<requestedExecutionLevel  level="requireAdministrator" uiAccess="false" />'
 *                
 *  usage: RgfGCUtil {i | r | v} quotedPathToAssembly
 *         -l ... list assembly information (using assembly display name, can be full display name as well)
 *         -i ... install (using full assembly file name, can be fully qualified with path)
 *         -u ... uninstall (using full assembly file name, can be fully qualified with path)
 *         -r ... remove (using assembly display name, can be full display name as well)
 *         -v ... version
 *              
 *              
 *  license: Apache License 2.0 (AL 2.0)            
 *  
 *   *  ------------------------ Apache Version 2.0 license -------------------------
 *     Copyright (C) 2016 Rony G. Flatscher
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *  -----------------------------------------------------------------------------
 *  
 */

namespace GacUtilRgf
{
    class GAC
    {
        const String pgmName = "gacutilrgf.exe";        // name of the program 
        const String version = "100.20160813";          // version: major*100 '.' YYYYMMDD
        const String validOptions = "-V -I -U -R -L";   // valid options
        const String assemblyNameOptions = "-R -L";     // options that use the assemblyName (FullName/DisplayName) only

         static int Main(string[] args)  // process arguments
        {
            if (args.Count() == 0)
            {
                ShowUsage();
                return -1;      // indicate that no arguments were given
            }

            String uOption = args[0].ToUpper();
            if ((validOptions.Contains(uOption) == false) || (args.Count() == 1 && args[0].Equals("-")))
            {
                Console.Error.WriteLine("{0}: unknown option [{1}]\n", pgmName, args[0]);
                ShowUsage();
                return -10;     // unknown option
            }

            if (uOption.Equals("-V"))       // show version
            {
                Console.WriteLine("{0} - version: [{1}]", pgmName, version);
                return 0;
            }

            if (args.Count() != 2)  
            {
                Console.Error.WriteLine("{0}: need exactly two arguments, however [{1}] argument(s) supplied\n", pgmName, args.Count());
                ShowUsage();
                return -11;     // wrong number of arguments
            }

            Assembly ass = null;        
            if (assemblyNameOptions.Contains(uOption))   // check existence of assembly
            {
                try
                {
                    ass = Assembly.ReflectionOnlyLoad(args[1]); // cf. <https://msdn.microsoft.com/en-us/library/0et80c7k(v=vs.110).aspx> as of 2016-08-10
                }
                catch (Exception exc)
                {
                    if (exc is ArgumentNullException) // assembly file is null
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName,  exc.Message, args[1]);
                        return -20;     
                    }
                    else if (exc is ArgumentException)  // assembly file is invalid
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                        return -21;
                    }
                    else if (exc is FileNotFoundException) // assembly file not found
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                        return -22;
                    }
                    else if (exc is BadImageFormatException)    // assembly file is not an assembly
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                        return -23;
                    }
                    else if (exc is FileLoadException) // assembly or module was loaded twice with two different sets of evidenc
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                        return -24;
                    }
                    else
                    {
                        Console.Error.WriteLine("{0}: ReflectionOnlyLoad(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                        return -25;
                    }
                }
            }

            if (uOption.Equals("-L"))   // give infos about the assembly we have found
            {
                Console.WriteLine("AssemblyName:     [{0}]", args[1]);
                Console.WriteLine("Display/FullName: [{0}]", ass.FullName);
                Console.WriteLine("Location:         [{0}]", ass.Location);     
                Console.WriteLine("CodeBase:         [{0}]", ass.CodeBase);     // could be different, e.g. a URL into the web
//                Console.WriteLine("CodeBase as LocalPath: [{0}]", new Uri(ass.CodeBase).LocalPath);
            }


            // either GacInstall() or GacRemove() desired, now get the Publish instance
            System.EnterpriseServices.Internal.Publish publish = new System.EnterpriseServices.Internal.Publish();
 
            if (uOption.Equals("-I") || uOption.Equals("-U"))       // install, uninstall, do a GacInstall() or GacRemove()
            {
                if (! File.Exists(args[1]))
                {
                    Console.Error.WriteLine("{0}: assembly file [{1}] not found\n", pgmName, args[1]);
                    ShowUsage();
                    return -30;     // assembly file does not exist
                }

                if (uOption.Equals("-I"))
                {
                    Console.WriteLine("{0}: installing [{1}] into GAC (global assembly cache) ...", pgmName, args[1]);
                }
                else
                {
                    Console.WriteLine("{0}: uninstalling [{1}] from GAC (global assembly cache) ...", pgmName, args[1]);
                }

                try
                {
                    if (uOption.Equals("-I"))
                    { 
                        publish.GacInstall(args[1]);    // install assembly into global assembly cache (GAC)
                    }
                    else
                    { 
                        publish.GacRemove(args[1]);    // install assembly into global assembly cache (GAC)
                    }

                }
                catch (Exception exc)
                {
                    if (uOption.Equals("-I"))
                    {
                        Console.Error.WriteLine("{0}: exception [{1}] raised while trying to install assembly [{2}] into GAC (global assembly cache)", pgmName, exc.ToString(), args[1]);
                        return -40;     // indicate an exception has occurred while doing a GacInstall()
                    }
                    else
                    {
                        Console.Error.WriteLine("{0}: exception [{1}] raised while trying to uninstall assembly [{2}] from GAC (global assembly cache)", pgmName, exc.ToString(), args[1]);
                        return -55;     // indicate an exception has occurred while doing a GacInstall()
                    }
                }

                return 0;
            }


            if (uOption.Equals("-R"))       // remove via assembly name, do a GacRemove()
            {

                Console.WriteLine("{0}: uninstalling [{1}] from GAC (global assembly cache) ...", pgmName, args[1]);
                try
                {
                    // publish.GacRemove(args[1]);    // remove assembly from global assembly cache (GAC)
                    publish.GacRemove(new Uri(ass.CodeBase).LocalPath);
                }
                catch (Exception exc)
                {
                    Console.Error.WriteLine("{0}: exception [{1}] raised while trying to install assembly [{2}]", pgmName, exc.ToString(), args[1]);
                    return -50;     // indicate an exception has occurred while doing a GacRemove()
                }

                return 0;
            }
            

            return 0;
        }

        static void ShowUsage()
        {
            Console.Error.WriteLine("{0}: installs, checks and uninstalls assemblies to/in/from the GAC (global assembly cache)\n", pgmName);
            Console.Error.WriteLine("usage: {0} -v                       ... displays current version", pgmName);
            Console.Error.WriteLine("       {0} -l assemblyDisplayName   ... list information for assembly display name (Name, FullName, Location, CodeBase)", pgmName);
            Console.Error.WriteLine("       {0} -i assemblyPath2File     ... install assembly in GAC (using assembly file name)", pgmName);
            Console.Error.WriteLine("       {0} -u assemblyPath2File     ... uninstall assembly from GAC (using any existing assembly file name)", pgmName);
            Console.Error.WriteLine("       {0} -r assemblyDisplayName   ... remove assembly from GAC (using assembly display name)", pgmName);

            Console.Error.WriteLine("\nexample: assemblyDisplayName=\"xyzAssembly\", assemblyPath2File=\"c:\\Users\\Test\\xyzAssembly.dll\" \n", pgmName);
            Console.Error.WriteLine("{0} -i \"c:\\Users\\Test\\xyzAssembly.dll\"   ... installs assembly \"c:\\Users\\Test\\xyzAssembly.dll\" into GAC", pgmName);
            Console.Error.WriteLine("{0} -l xyzAssembly                       ... lists some information about assembly named \"xyzAssembly\" (can be full display name)", pgmName);
            Console.Error.WriteLine("{0} -u \"c:\\Users\\Test\\xyzAssembly.dll\"   ... uninstalls assembly \"c:\\Users\\Test\\xyzAssembly.dll\" from GAC", pgmName);
            Console.Error.WriteLine("{0} -r xyzAssembly                       ... uninstalls assembly \"xyzAssembly\" (name can be full display name) from GAC", pgmName);
        }
    }
}
