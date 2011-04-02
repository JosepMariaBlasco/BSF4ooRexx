// ---rgf, 2009-02-11 - 2010-06-26, Java program to invoke "create_UNO_API_info.rxo" from Java
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

class HowtoCreateApiInfo {
    public static void main (String args[]) {
        // excerpted from "HardFormatting.java" from the OOo development package
        XDesktop              xDesktop = null;
        XMultiComponentFactory xMCF     = null;
        XMultiServiceFactory   xMSF     = null;

        try {
            XComponentContext xContext = null;

            // bootstrap the UNO runtime environment
            xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();

            // get the service manager
            xMCF = xContext.getServiceManager();
            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

            if (xMSF!=null)
            {
                System.out.println("Java: connected to the bootstrapped office ...\n---");

                // get XDispatchProvider from XDesktop
                Object oDesktop = xMSF.createInstance("com.sun.star.frame.Desktop");
                xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, oDesktop);
                XDispatchProvider xDispatchProvider=(XDispatchProvider)
                        UnoRuntime.queryInterface(XDispatchProvider.class, xDesktop);


                Object sDispatchHelper= xMSF.createInstance("com.sun.star.frame.DispatchHelper");
                XDispatchHelper xDispatchHelper=(XDispatchHelper)
                        UnoRuntime.queryInterface(XDispatchHelper.class, sDispatchHelper);

                // invoke the ooRexx script to document the UNO object/IDL

                // define Rexx dispatch target, library "wu_tools", script name "create_UNO_API_info.rxo", location "share"
                String location   ="share",             // case sensitive, other possible values: "share" (all users), "application"
                       libraryName="wu_tools",          // case sensitive, name of the Rexx macro library
                       scriptName ="UNO_API_info.rxo",  // case sensitive, name of the Rexx script
                       langName   ="ooRexx";            // case sensitive, OOo name of the scripting language

                    // build 'macroUrl' string for the dispatcher
                String macroUrl="vnd.sun.star.script:"+libraryName+"."+scriptName+
                                          "?language="+langName+
                                          "&location="+location;

                // define one argument (an UNO service object from the running program, namely 'sDispatchHelper')
                PropertyValue args1[]={createProperty("arg1", sDispatchHelper) };

                System.out.println("Java: dispatching to 'create_UNO_API_info.rxo' using an UNO object from the running program...");
                // dispatch, supplying arguments
                xDispatchHelper.executeDispatch(
                            xDispatchProvider,  // XDispatchProvider
                            macroUrl,           // URL
                            "",                 // TargetFrameName
                            0,                  // SearchFlags
                            args1);        // Arguments


                // ============================== next dispatch uses an UNO IDL string and options
                System.out.println("---");

                   // define options
                PropertyValue options[]= {
                    createProperty("NrOfLayers",           new Integer(2)), // 2="show two levels deep"
                    createProperty("View",                 new Integer(1)), // 1="view in writer"
                    createProperty("DocumentationSource",  new Integer(1)), // 1="use Internet" (base url)
                    createProperty("NumberingTypeLevel_1", new Integer(0)), // 0="Alpha Uppercase"
                    createProperty("NumberingTypeLevel_2", new Integer(4)), // 4="arabic"
                    createProperty("NumberingTypeLevel_3", new Integer(3)), // 3="roman lower"
                    createProperty("FontName",             "DejaVu Sans Condensed")
                };

                // define two arguments
                PropertyValue args2[]=
                {
                    createProperty("arg1", "com.sun.star.frame.Desktop"), // analyze UNO IDL name
                    createProperty("arg2", options)                       // rendering options
                };

                System.out.println("Java: dispatching to 'create_UNO_API_info.rxo' using an UNO IDL string and rendering options...");
                // dispatch, supplying arguments
                xDispatchHelper.executeDispatch(
                            xDispatchProvider,  // XDispatchProvider
                            macroUrl,           // URL
                            "",                 // TargetFrameName
                            0,                  // SearchFlags
                            args2);             // Arguments
            }
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.err.println("end of program run.");
        System.exit(0);
    }

    // utility method to ease creation of PropertyValue objects
    static PropertyValue createProperty(String n, Object v)
    {

        PropertyValue prop=new PropertyValue();
        prop.Name =n;
        prop.Value=v;
        return prop;
    }
}


/*
    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2009-2010 Rony G. Flatscher

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
