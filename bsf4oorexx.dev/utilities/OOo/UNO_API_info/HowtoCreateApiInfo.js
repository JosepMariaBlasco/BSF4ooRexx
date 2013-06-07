// ---rgf, 2010-06-25, JavaScript program to invoke "create_UNO_API_info.rxo" from JavaScript
importClass(Packages.com.sun.star.beans.PropertyValue);
importClass(Packages.com.sun.star.frame.XDispatchHelper);
importClass(Packages.com.sun.star.frame.XDispatchProvider);
importClass(Packages.com.sun.star.uno.UnoRuntime);

// utility method to ease creation of PropertyValue objects
function createProperty(n, v)
{
    prop=new PropertyValue();
    prop.Name =n;
    prop.Value=v;
    return prop;
}


// get important objects from the XSCRIPTCONTEXT
xDispatchProvider = UnoRuntime.queryInterface(XDispatchProvider, XSCRIPTCONTEXT.getDesktop());
ctx = XSCRIPTCONTEXT.getComponentContext();
smgr = ctx.getServiceManager();

// define Rexx dispatch target, library "wu_tools", script name "create_UNO_API_info.rxo", location "share"
location   ="share";
libraryName="wu_tools";
scriptName ="UNO_API_info.rxo";
langName   ="ooRexx";
// build -- macroUrl-- string for the dispatcher
macroUrl = "vnd.sun.star.script:"+libraryName+"."+scriptName+"?language="+langName+"&location="+location;

// create an UNO service object
sdh = smgr.createInstanceWithContext("com.sun.star.frame.DispatchHelper", ctx);
xDispatchHelper = UnoRuntime.queryInterface(XDispatchHelper, sdh);

args = new Array;
args[0]= createProperty("arg1", sdh);

// dispatch the ooRexx macro to document the 'sdh' service object using the default settings
xDispatchHelper.executeDispatch(xDispatchProvider, macroUrl, "", 0, args);
// --------------------------------------------------------------------------


// must define this PropertyValue array as a Java array as otherwise the bridge will not be
// able to correctly convert the JavaScript dynamic array in the executeDispatch invocation
options = java.lang.reflect.Array.newInstance(PropertyValue, 7);
options[0]= createProperty("NrOfLayers",           "2" );   // 2="show two levels deep"
options[1]= createProperty("View",                 "1" );   // 1="view in writer"
options[2]= createProperty("DocumentationSource",  "1" );   // 1="use Internet" (base url)
options[3]= createProperty("NumberingTypeLevel_1", "0" );   // 0="Alpha Uppercase"
options[4]= createProperty("NumberingTypeLevel_2", "4" );   // 4="arabic"
options[5]= createProperty("NumberingTypeLevel_3", "3" );   // 3="roman lower"
options[6]= createProperty("FontName",             "DejaVu Sans Condensed");

// define two arguments
args[0] = createProperty("arg1", "com.sun.star.frame.Desktop"); // analyze UNO IDL name
args[1] = createProperty("arg2", options);                      // rendering options

// dispatch the ooRexx macro to document the UNO IDL 'com.sun.star.frame.Desktop' using specific settings
xDispatchHelper.executeDispatch(xDispatchProvider, macroUrl, "", 0, args);

