# Rony G. Flatscher, 2010-06-23
from com.sun.star.beans import PropertyValue

def createApiInfo( ):
    """Uses the ooRexx macro UNO_API_info.rxo to document an OOo service object and a UNO IDL type string"""

# get the OOo Desktop and the ServiceManager
    desktop = XSCRIPTCONTEXT.getDesktop()
    ctx = XSCRIPTCONTEXT.getComponentContext()
    smgr = ctx.ServiceManager

# define Rexx dispatch target, library "wu_tools", script name "create_UNO_API_info.rxo", location "share"
    location   ="share"
    libraryName="wu_tools"
    scriptName ="UNO_API_info.rxo"
    langName   ="ooRexx"
    # build -- macroUrl-- string for the dispatcher
    macroUrl = "vnd.sun.star.script:"+libraryName+"."+scriptName+"?language="+langName+"&location="+location

    # create an UNO service object
    sdh  = smgr.createInstance("com.sun.star.frame.DispatchHelper")
    a1 = createPropertyValue("arg1", sdh)

# dispatch the ooRexx macro to document the 'sdh' service object using the default settings
    sdh.executeDispatch(desktop, macroUrl, "", 0, (a1,))

    # --- document an UNO IDL string, change the formatting default values
    a1.Value = "com.sun.star.frame.Desktop"            # an UNO IDL string

    p1=createPropertyValue("NrOfLayers",           2)  # 2="show two levels deep"
    p2=createPropertyValue("View",                 1)  # 1="view in writer"
    p3=createPropertyValue("DocumentationSource",  1)  # 1="use Internet" (base url)
    p4=createPropertyValue("NumberingTypeLevel_1", 0)  # 0="Alpha Uppercase"
    p5=createPropertyValue("NumberingTypeLevel_2", 4)  # 4="arabic"
    p6=createPropertyValue("NumberingTypeLevel_3", 3)  # 3="roman lower"
    p7=createPropertyValue("FontName",             "DejaVu Sans Condensed")

    a2=createPropertyValue("arg2", (p1,p2,p3,p4,p5,p6,p7))

# dispatch the ooRexx macro to document the UNO IDL definitions using the supplied settings
    sdh.executeDispatch(desktop, macroUrl, "", 0, (a1,a2))

def createPropertyValue (name, value):
    """Utility function to ease creation of PropertyValues"""
    pv = PropertyValue()
    pv.Name = name          # assign name
    pv.Value = value        # assing value
    return pv               # return PropertyValue object

# list those functions that should be shown in the OOo-UI
g_exportedScripts = createApiInfo,

