using System;
using System.IO;
using System.Linq;
using System.Reflection;


/** author: Rony G. Flatscher
 *  date:   2016-08-09
 *  version: 100.20160809
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
 *  usage: RgfGCUtil {i | r | v} quotedPathToAssembly
 *         -l ... list assembly information
 *         -i ... install 
 *         -r ... remove
 *         -v ... version
 *              
 *              
 *  license: Apache License 2.0 (AL 2.0)            
 */

namespace GacUtilRgf
{
    class GAC
    {
        const String pgmName = "gacutilrgf.exe";     // name of the program 
        const String version = "100.20160810";  // version: major*100 '.' YYYYMMDD
        const String options = "-V -I -U -L";      // valid options
        const String anOptions = "-U -L";          // options that use the assemblyName (FullName/DisplayName) only

         static int Main(string[] args)  // process arguments
        {
            /*
                        Console.WriteLine("{0}: args.Count()=[{1}]:", pgmName, args.Count());
                        for (int i = 0; i < args.Count(); i++)
                        {
                            Console.WriteLine("\t arg[{0,3}]=[{1}]", i, args[i]);
                        }
                        Console.WriteLine();
            */

            if (args.Count() == 0)
            {
                ShowUsage();
                return -1;      // indicate that no arguments were given
            }
            else
            {
                String uOption = args[0].ToUpper();
                if (options.Contains(uOption) == false)
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


/*
                // debug-only
                // if (bDebug)
                {
                    try
                    {
                        Assembly aha = Assembly.LoadWithPartialName(args[1]);

                        Console.WriteLine("LoadWithPartialName(), assembly=[{0}]:\n", args[1]);
                        Console.WriteLine("assemblyName:     [{0}]", args[1]);
                        Console.WriteLine("Full/DisplayName: [{0}]", aha.FullName);
                        Console.WriteLine("CodeBase:         [{0}]", aha.CodeBase);
                        Console.WriteLine("LocalPath:        [{0}]", new Uri(aha.CodeBase).LocalPath);
                        Console.WriteLine("---");

                    }
                    catch (Exception exc)
                    {
                        Console.Error.WriteLine("{0}: LoadWithPartialName(\"{2}\"), exception [{1}] raised", pgmName, exc.Message, args[1]);
                    }
                    Assembly a = Assembly.LoadWithPartialName(args[1]);

                }
*/

                Assembly ass = null;
                if (anOptions.Contains(uOption))   // check existence of assembly
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
                    Console.WriteLine("assemblyName:     [{0}]", args[1]);
                    Console.WriteLine("Full/DisplayName: [{0}]", ass.FullName);
                    Console.WriteLine("CodeBase:         [{0}]", ass.CodeBase);
                    Console.WriteLine("LocalPath:        [{0}]", new Uri(ass.CodeBase).LocalPath);
                }


                // either GacInstall() or GacRemove() desired, now get the Publish instance
                System.EnterpriseServices.Internal.Publish publish = new System.EnterpriseServices.Internal.Publish();
 
                if (uOption.Equals("-I"))       // install, do a GacInstall()
                {
                    if (! File.Exists(args[1]))
                    {
                        Console.Error.WriteLine("{0}: assembly file [{1}] not found\n", pgmName, args[1]);
                        ShowUsage();
                        return -30;     // assembly file does not exist
                    }


                    Console.WriteLine("{0}: installing [{1}] into GAC (global assembly cache) ...", pgmName, args[1]);
                    try
                    {
                        publish.GacInstall(args[1]);    // install assembly into global assembly cache (GAC)
                    }
                    catch (Exception exc)
                    {
                        Console.Error.WriteLine("{0}: exception [{1}] raised while trying to install assembly [{2}]", pgmName, exc.ToString(), args[1]);
                        return -40;     // indicate an exception has occurred while doing a GacInstall()
                    }

                    return 0;
                }


                if (uOption.Equals("-U"))       // uninstall, do a GacRemove()
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
            }

            return 0;
        }

        static void ShowUsage()
        {
            Console.Error.WriteLine("{0}: installs, checks and uninstalls assemblies to/in/from the GAC (global assembly cache)\n", pgmName);
            Console.Error.WriteLine("usage: {0} -v                       ... displays current version", pgmName);
            Console.Error.WriteLine("       {0} -l assemblyDisplayName   ... list assembly information (Name, FullName, CodeBase, LocalPath)", pgmName);
            Console.Error.WriteLine("       {0} -i assemblyPath2File     ... install assembly (fully qualified file name) in GAC", pgmName);
            Console.Error.WriteLine("       {0} -u assemblyDisplayName   ... remove assembly from GAC", pgmName);

            Console.Error.WriteLine("\nexample: assemblyDisplayName=\"xyzAssembly\", assemblyPath2File=\"c:\\Users\\Test\\xyzAssembly.dll\" \n", pgmName);
            Console.Error.WriteLine("{0} -i \"c:\\Users\\Test\\xyzAssembly.dll\"  ... installs assembly in GAC (global assembly cache)", pgmName);
            Console.Error.WriteLine("{0} -l xyzAssembly  ... gives FullName, CodeBase and LocalPath to assembly", pgmName);
            Console.Error.WriteLine("{0} -u xyzAssembly  ... uninstalls assembly with the given short, partial or full name", pgmName);
        }
    }
}
