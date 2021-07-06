/*
*  2020-09-04, based on

      svn co https://svn.apache.org/repos/asf/jakarta/taglibs/deprecated/bsf/ bsftaglibs

   changes:
        - changed package name to "org.rexxla.taglibs.bsf" (using Apache BSF)
        - introduced optional attribute 'name' for debugging (will be used for filename)
        - introduced optional attribute 'throwException': default is 'false', if 'true'
          then an exceptoin will cause JSPException to be thrown which will stop creating
          the page and instead depict an error screen with the stacktrace
        - introduced optional attribute 'debug': default is 'false', if 'true' then a
          red table with debug information will be placed into the page (for debugging)

        - uppercased Java class name
        - update: use EVAL_BODY_BUFFERED instead of deprecated EVAL_BODY_TAG, cf.
          <https://tomcat.apache.org/tomcat-9.0-doc/jspapi/index.html?javax/servlet/jsp/tagext/BodyTag.html>
        - registering beans ordered by attribute names
        - refactor intoa an abstract base implementation and two extensions, one for
          scriptlet and one for expression execution
        - changed tag name from "scriptlet" to "script", and "expression" to "expr"

        - 2020-10-22: make sure that flush() does not get used in bsf.outputStream as it
                      would implicitly commit the response object!

        - 2020-11-09: make sure that for ooRexx 'out' gets used only at the very first invocation
                      on a page (otherwise each Rexx script/expr would needleslly redirect over and over)

        - 2020-11-12: if no BSFManager was created yet, do not use it for terminating engines

        - 2021-01-22: added attribute "slot": can be used to pass additional information
                      from the JSP author (e.g. configuration data controlling the overall
                      execution of script/expression)

        - 2021-02-01: add "namespace" information to TAGLIBINFORMATION

        - 2021-02-02: add "reflect" attribute: defaults to "false", if "true" supplies an implicit
                      HashMap object under the name TAGLIBINFORMATION containing taglib related
                      information and attribute settings in effect

        - 2021-02-03: make sure that impolicit HashMap object TAGLIBINFORMATION gets also created,
                      if the attribute "slot" has a value, such that the script programmer can get
                      access to it

        - 2021-02-25: make sure we remove the TAGLIBINFORMATION entry after we have executed the script

        - 2021-03-14: - shorten key for the implicit HashMap object that supplies ScriptTagLibs information
                      - change case from "taglib" to "TagLib"

        - 2021-07-06: - remove special expression handling for Rexx; programmers must explicitly
                        use the return keyword statement as we cannot differentiate between command
                        and intended return value
*/

/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * Copyright 2020-2021 Rony G., Flatscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rexxla.taglibs.bsf ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException ;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Vector;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.* ;
import jakarta.servlet.jsp.tagext.* ;

import org.apache.bsf.* ;


abstract public class BaseImpl implements BodyTag
{
    static final protected String version = "100.20210314"; // version of this taglib

    static final protected String TAGLIBINFORMATION ="scriptTagLibInfos";

    static final protected String scriptTagLib="BsfTagLib";
    // used to determine whether any tag handler runs for the first time for this page
    static final protected String pageScopeAttrName="BsfTagLibPageScope";
    // used to determine whether first invocation of Rexx is also the first invocation for the page
    static final protected String rexxOutRedirected="RexxOutRedirected";
    // will be used for retrieving/storing in pageContext
    static final protected String pageScopeManagerName=scriptTagLib+".Manager";
    static final protected String pageScopeEnginesName=scriptTagLib+".Engines";

    protected abstract String  getTagName();
    protected abstract boolean isExpression();

    // caches the content of the scripts loaded because of the 'src' attribute
    static protected HashMap<String,String> cachedSrcScripts;

    // cache engines at the class level, it may be the case that the instance
    // changes in the midst of processing the same JSP page
    //
    static private ThreadLocal<HashMap<String,BSFEngine>> tlCachedEngines = new ThreadLocal();

    // cache the names of the three arguments
    static protected Vector paramNames;
    static {
        cachedSrcScripts = new HashMap();   // caches the content of the scripts loaded because of the 'src' attribute

        paramNames=new Vector();
        paramNames.addElement("request");
        paramNames.addElement("response");
        paramNames.addElement("out");
    }

    static private ThreadLocal <BSFManager> tlMgr = new ThreadLocal() ;

    public void finalize() throws Exception // make sure the BSF engines get a chance to clean up, if need be
    {
        if (tlMgr!=null && tlMgr.get()!=null)
        {
            try {
                tlMgr.get().terminate();    // makes sure all enginges get terminated as well
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace(System.err);
                throw new JspException( e );
            }
        }
    }

    protected Tag          parent ;
    protected BodyContent  bodyOut ;
    protected PageContext  pageContext ;

    protected String       type ;                       // attribute 'type': script language name
    protected boolean      isRexx = false;              // determine whether we have rexx in hand
    protected String       name ;                       // optional: name for the script in the JSP (for debugging)
    protected String       src  ;                       // optional: src: relative path to script to run
    protected boolean      bCacheSrc = true;            // optional: if "false" reread from file, default: true
    protected boolean      bThrowException = false ;    // optional: if "true" an exception causes SkipPageException to be thrown, defaults to "false"
    protected boolean      bDebug = false;              // optional: if "true" show debug information
    protected boolean      bArguments = true;           // optional: if "true" supply the three implicit objects "request",
                                                        //           "response", "out" as arguments in that order
    protected String       slot ;                       // optional: attribute 'slot': JSP/script developer can use this for any purpose;
                                                        //           if present the implicit HashMap object named TAGLIBINFORMATION
                                                        //           gets created to allow access to it
    protected boolean      bReflect = false;            // optional: if "true" supply an implicit HashMap object named TAGLIBINFORMATION

    public void setType    (String value)           { type = value ;    // HTML now uses 'type' instead of 'language'
                                                      // determine whether using ooRexx is needed
                                                      isRexx = (type.toLowerCase().equals("rexx"));
                                                    }; // HTML now uses 'type' instead of 'language'
    public void setName    (String value)           { name = value ; };
    public void setSrc     (String value)           { src  = value ; };
    public void setCacheSrc (String value)          { bCacheSrc  = Boolean.valueOf(value) ; };
    public void setThrowException (String value)    { bThrowException = Boolean.valueOf(value) ; };
    public void setDebug   (String value)           { bDebug = Boolean.valueOf(value) ; };
    public void setArguments (String value)         { bArguments = Boolean.valueOf(value) ; };
    public void setSlot    (String value)           { slot = value ; };
    public void setReflect (String value)           { bReflect = Boolean.valueOf(value) ; };

    public void setParent(Tag parent)               { this.parent = parent ; };
    public Tag  getParent()                         { return( this.parent ); };
    public void doInitBody()  throws JspException   { };
    public int  doAfterBody() throws JspException   { return 0; };
    public void setBodyContent(BodyContent bodyOut) { this.bodyOut = bodyOut;};
    public int  doStartTag() throws JspException    { return(EVAL_BODY_BUFFERED);};
    public void release()                           { };

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext ;
    };

    private void register( BSFManager mgr, String name, Object bean )
                     throws Exception {
	if ( bean == null ) return ;
        try {
            mgr.declareBean( name, bean, bean.getClass() );
	}
	catch( Exception e ) {
	    throw e ;
	}
    };

    public int doEndTag() throws JspException {
        BSFManager mgr = null;
        try {
//	    if ( bodyOut == null ) return( EVAL_PAGE );

            BSFEngine  engine=null;
            // for a page there may be multiple instances of the tag handler (one per script/expression)
            // which all execute on the same thread; hence caching engines at the thread scope;
            // it maybe the case that a some other code (Java) on the page ran already and
            // defined a manager and an engine cache, if so reuse them
            if ( pageContext.getAttribute(pageScopeAttrName, pageContext.PAGE_SCOPE) == null)
            {
                pageContext.setAttribute(pageScopeAttrName, new Object(), pageContext.PAGE_SCOPE);

                // manager and engines already created, if so, use them
                mgr = (BSFManager) pageContext.getAttribute(pageScopeManagerName, pageContext.PAGE_SCOPE);
                HashMap <String,BSFEngine> engines = (HashMap <String,BSFEngine>) pageContext.getAttribute(pageScopeEnginesName, pageContext.PAGE_SCOPE);

                if (mgr==null)  // create the manager, store it in the pageContext for others
                {
                    mgr = new BSFManager();
                    pageContext.setAttribute(pageScopeManagerName, mgr, pageContext.PAGE_SCOPE);
                }
                tlMgr.set(mgr);

                if (engines==null)  // create the engine cache, store it in the pageContext for others
                {
                    engines = new HashMap<String,BSFEngine>();
                    pageContext.setAttribute(pageScopeEnginesName, engines, pageContext.PAGE_SCOPE);
                }
                tlCachedEngines.set(engines);
                engine = engines.get(type);
            }
            else    // ThreadLocal already set, so cache operational, engine in cache?
            {
                // get a script engine for the language defined in type attribute
                engine = tlCachedEngines.get().get(type);
            }

            if (engine==null)   // engine not in cache
            {
                try {
                    engine = tlMgr.get().loadScriptingEngine(type);
                }
                catch (Exception e1)    // catch to give same error message as JSR223 taglib
                {
                    throw new Exception("Could not locate ScriptEngine for [" + type + "]", e1);
                }
                tlCachedEngines.get().put(type,engine);     // cache engine
            }

            ServletContext  application =pageContext.getServletContext(); // ServletContext
            HttpServletRequest  request =(HttpServletRequest) pageContext.getRequest();     // HttpServletRequest
            HttpServletResponse response=(HttpServletResponse) pageContext.getResponse();   // HttpServletResponse
            JspWriter out = pageContext.getOut();

            if (mgr==null)      // do we need to get the BSFManager?
            {
                mgr = tlMgr.get();
            }
            register( mgr, "application", application );
            register( mgr, "config"     , pageContext.getServletConfig() );
            register( mgr, "exception"  , pageContext.getException() );
            register( mgr, "out"        , out );
            register( mgr, "page"       , pageContext.getPage() );
            register( mgr, "pageContext", pageContext );
            register( mgr, "response"   , response );
            register( mgr, "session"    , pageContext.getSession() );
            register( mgr, "request"    , request );

            // allow scripts to also fetch this information
            register( mgr, "scriptTagLibTag" , scriptTagLib+"."+getTagName());

            String actualContextPath  = request.getContextPath();   // actual contextPath
            String actualRequestURI   = request.getRequestURI ();   // actual URL
            // get virtualPath path which does not contain the contextPath; needed for getRealPath()
            String virtualPath        = actualRequestURI.substring(actualContextPath.length());
            String realPath           = application.getRealPath(virtualPath);

            String srcVirtualPath=null, srcRealPath = null;

            String filename, scriptCode = null;

            if (src==null)
            {
                filename=realPath+"!"+(name==null?"(unknown)":name);
                if (bodyOut==null)
                {
                    throw new JspException(scriptTagLib+": empty tag and \"src\" attribute not supplied instead");
                }
                scriptCode = bodyOut.getString();   // code from JSP
            }
            else    // code in external file
            {
                if (src.startsWith("/"))    // absolute path (relative to contextPath)
                {
                    srcVirtualPath=src;
                }
                else    // relative path to location of JSP file
                {
                    int lastPos=virtualPath.lastIndexOf('/');
                    srcVirtualPath=virtualPath.substring(0,lastPos+1) + src;
                }

                srcRealPath =  application.getRealPath(srcVirtualPath);
                filename=srcRealPath;

                // if cached, reuse it immediately
                if (bCacheSrc)
                {
                    scriptCode = cachedSrcScripts.get(srcVirtualPath);  // fetch from cache
                }

                if (scriptCode==null && srcRealPath!=null)
                {
                    InputStreamReader scriptReader = new InputStreamReader(new FileInputStream(srcRealPath));
                    try {   // make sure we close the file in case of a read error
                        StringBuilder sb = new StringBuilder();
                        final int bufSize = 8*1024;
                        char[] charBuffer = new char[bufSize];
                        int n;
                        do
                        {
                            n = scriptReader.read(charBuffer,0,bufSize);
                            if (n > 0)
                            {
                                sb.append(new String(charBuffer,0,n));
                            }
                        } while (n > -1);
                        scriptCode = sb.toString();
                    }
                    catch (IOException ioe) {
                        throw ioe;  // re-throw
                    }
                    finally {
                        scriptReader.close();
                    }

                    if (bCacheSrc)
                    {
                        cachedSrcScripts.put(srcVirtualPath, scriptCode);
                    }
                }
            }

            // allow scripts to get at the taglib information and attribute settings
            if (bReflect || slot!=null)
            {
                HashMap <String,Object> attr = new HashMap();
                attr.put("arguments"        , bArguments      );
                attr.put("cacheSrc"         , bCacheSrc       );
                attr.put("debug"            , bDebug          );
                attr.put("name"             , name            );
                attr.put("namespace"        , "jakarta"       );
                attr.put("reflect"          , bReflect        );
                attr.put("slot"             , slot            );
                attr.put("src"              , src             );
                attr.put("tagLibName"       , scriptTagLib    );
                attr.put("tagName"          , getTagName()    );
                attr.put("throwException"   , bThrowException );
                attr.put("type"             , type            );    // language
                attr.put("version"          , version         );
                register( mgr, TAGLIBINFORMATION, attr);
            }

            if (bDebug)
            {
                String actualRequestURL         = request.getRequestURL ().toString();   // actual URL

                String style="style=\"border: 1px solid maroon; padding: 5px; margin: 5px;\"";

                out.println("<hr style=\"color: maroon;\"/>");
                out.println("<center>");

                // ----------------------------

                out.println("<table "+style+">");
                out.println("<tr><th colspan='2'>Debug Information</th></tr>");

                out.print  ("<tr><td "+style+">request.getContextPath()</td>");
                out.println("<td "+style+">"+actualContextPath+"</td></tr>");

                out.print  ("<tr><td "+style+">request.getRequestURL()</td>");
                out.println("<td "+style+">"+actualRequestURL+"</td></tr>");

                out.print  ("<tr><td "+style+">request.getRequestURI()</td>");
                out.println("<td "+style+">"+actualRequestURI+"</td></tr>");

                out.print  ("<tr><td "+style+">virtualPath (without contextPath)</td>");
                out.println("<td "+style+">"+virtualPath+"</td></tr>");

                out.print  ("<tr><td "+style+">application.getRealPath(virtualPath)</td>");
                {
                    File fn=new File(realPath);
                    out.println("<td "+style+">"+realPath+"<br><em>exists? ("+fn.exists()+")</em></td></tr>");
                }

                out.println("</table>");

                // ----------------------------
                out.println("&nbsp;");
                out.println("<table "+style+">");
                out.println("<tr><th colspan='2'>Debug Information (Taglib Invocation Related)</th></tr>");

                out.print  ("<tr><td "+style+">Current thread</td>");
                {
                    Thread currThr=Thread.currentThread();
                    String tinfos="id=["+currThr.getId()+"]: " +
                                  "name=["+currThr.getName()+ "]"+
                                  ", threadGroup=["+currThr.getThreadGroup()+ "]"+
                                  ", priority=["+currThr.getPriority()+ "]"+
                                  ", state=["+currThr.getState()+ "]";
                    out.println("<td "+style+">"+tinfos+"</td></tr>");
                }

                out.print  ("<tr><td "+style+">Script manager</td>");
                out.println("<td "+style+">"+mgr+"</td></tr>");

                out.print  ("<tr><td "+style+">Script engine</td>");
                out.println("<td "+style+">"+engine+"</td></tr>");

                {
                    out.print  ("<tr><td "+style+">scriptCode</td>");
                    String tmpString=scriptCode;
                    int totalLength=(tmpString==null ? -1 : tmpString.length());
                    if (totalLength>-1) // check length of lines, cut them, if necessary
                    {
                        int maxLineLength=60, currLength, maxLines=10;
                        int startPos=0, posLF=0;
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<maxLines && posLF>-1; i++)
                        {
                            posLF=tmpString.indexOf('\n', (startPos==0 ? 0 : startPos+1) );
                            currLength = ( posLF==-1 ? totalLength-startPos : posLF-startPos+1 );

                            sb.append( escape(tmpString.substring(startPos, startPos+(currLength>maxLineLength ? maxLineLength : currLength ))) );

                            if (currLength>maxLineLength)   // did we have to cut the line?
                            {
                                sb.append( "&hellip;<br> \n" );
                            }
                            startPos=posLF+1;
                        }
                        if (posLF>-1)   // if code left, indicate it
                        {
                            sb.append( "&hellip; cut &hellip;  <br> \n" );
                        }
                        tmpString=sb.toString();
                    }
                    else
                    {
                        tmpString=escape(tmpString);
                    }
                    out.println("<td "+style+"><code>"+tmpString+"<code></td></tr>");
                }

                // ---

                out.println("<tr><td colspan=\"2\"></td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;arguments&quot;</td>");
                out.println("<td "+style+">"+bArguments+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;cacheSrc&quot;</td>");
                out.println("<td "+style+">"+bCacheSrc+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;debug&quot;</td>");
                out.println("<td "+style+">"+bDebug+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;name&quot;</td>");
                out.println("<td "+style+">"+name+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;reflect&quot;</td>");
                out.println("<td "+style+">"+bReflect+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;reflect&quot;</td>");
                out.println("<td "+style+">"+bReflect+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;slot&quot;</td>");
                out.println("<td "+style+">"+slot+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;src&quot;</td>");
                out.println("<td "+style+">"+src+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;throwException&quot;</td>");
                out.println("<td "+style+">"+bThrowException+"</td></tr>");

                out.print  ("<tr><td "+style+">Attribute &quot;type&quot;</td>");
                out.println("<td "+style+">"+type+"</td></tr>");

                // ---

                out.println("<tr><td colspan=\"2\"></td></tr>");

                out.print  ("<tr><td "+style+">cachedSrcScripts.size()</td>");
                out.println("<td "+style+">"+cachedSrcScripts.size()+"</td></tr>");

                out.print  ("<tr><td "+style+">filename</td>");
                {
                    File fn=new File(filename);
                    out.println("<td "+style+">"+filename+"<br><em>exists? ("+fn.exists()+")</em></td></tr>");
                }

                out.print  ("<tr><td "+style+">getTagName()</td>");
                out.println("<td "+style+">"+getTagName()+"</td></tr>");

                out.print  ("<tr><td "+style+">isExpression()</td>");
                out.println("<td "+style+">"+isExpression()+"</td></tr>");

                out.print  ("<tr><td "+style+">scriptTagLib</td>");
                out.println("<td "+style+">"+scriptTagLib+"</td></tr>");

                out.print  ("<tr><td "+style+">srcRealPath</td>");
                {
                    if (srcRealPath==null)
                    {
                        out.println("<td "+style+">"+srcRealPath+"</td></tr>");
                    }
                    else
                    {
                        File fn=new File(srcRealPath);
                        out.println("<td "+style+">"+srcRealPath+"<br><em>exists? ("+fn.exists()+")</em></td></tr>");
                    }
                }

                out.print  ("<tr><td "+style+">srcVirtualPath</td>");
                out.println("<td "+style+">"+srcVirtualPath+"</td></tr>");


                out.println("</table>");
                // -----------------------------

                out.println("&nbsp;");
                out.println("<table "+style+">");
                out.println("<tr><th colspan='2'>Debug Information (Implicit Objects)</th></tr>");

                out.print  ("<tr><td "+style+">application</td>");
                out.println("<td "+style+">"+mgr.lookupBean("application")+"</td></tr>");

                out.print  ("<tr><td "+style+">config</td>");
                out.println("<td "+style+">"+mgr.lookupBean("config")+"</td></tr>");

                out.print  ("<tr><td "+style+">exception</td>");
                out.println("<td "+style+">"+mgr.lookupBean("exception")+"</td></tr>");

                out.print  ("<tr><td "+style+">out</td>");
                out.println("<td "+style+">"+mgr.lookupBean("out")+"</td></tr>");

                out.print  ("<tr><td "+style+">page</td>");
                out.println("<td "+style+">"+mgr.lookupBean("page")+"</td></tr>");

                out.print  ("<tr><td "+style+">pageContext</td>");
                out.println("<td "+style+">"+mgr.lookupBean("pageContext")+"</td></tr>");

                out.print  ("<tr><td "+style+">request</td>");
                out.println("<td "+style+">"+mgr.lookupBean("request")+"</td></tr>");

                out.print  ("<tr><td "+style+">response</td>");
                out.println("<td "+style+">"+mgr.lookupBean("response")+"</td></tr>");

                out.print  ("<tr><td "+style+">session</td>");
                out.println("<td "+style+">"+mgr.lookupBean("session")+"</td></tr>");

                out.print  ("<tr><td "+style+">scriptTagLibTag</td>");
                out.println("<td "+style+">"+mgr.lookupBean("scriptTagLibTag")+"</td></tr>");

                out.println("</table>");
                // -----------------------------

                out.println("</center>");
                out.println("<hr style=\"color: maroon;\"/>");
            }

            // if Rexx, make sure BSF.CLS is required, such that the Scriptlet code
            // invocation will be able to fetch the arguments directly as BSF proxies
            // with USE ARG; in addition redirect the standard streams to Java and
            // .output specifically to "out" such that SAY statements get directed to it
            if (isRexx)     // redirect .output target to out-Java object
            {
                // redirect .output to 'out' ?
                if (pageContext.getAttribute(rexxOutRedirected, pageContext.PAGE_SCOPE) == null)
                {
                    // indicate that for this page we have Rexx .output redirected to not do it repeatedly
                    pageContext.setAttribute(rexxOutRedirected, new Object(), pageContext.PAGE_SCOPE);
                    // setup the Rexx instance to use the implicit 'out' object for its .output,
                    // if needed (only the first time)

                        // make sure we always set 'out'
                    String code = "out=bsf.lookupBean('out')                                   \n" +
                                  // make sure no prefix gets inserted, and CR-LF gets used as endOfLine
                                  // also make sure no flushing takes place (will implicitly commit response object)
                                  "jspWriter=.bsf.outputStream~new(out, .nil, '0d0a'x, .false) \n" +
                                  // redirect Rexx standard streams to Java, make sure .output uses jspWriter
                                  "call bsf.redirectTo 'Java', , jspWriter                     \n" +
                                  "::requires BSF.CLS  -- load camouflaging support"   ;
                    engine.apply("org.rexxla.taglibs.bsf.BaseImpl", 0, 0, code, null, null);
                }
            }

            // make it easy for the script to get at the three most important objects
            // by fetching them as arguments
            Vector args=null;
            if (bArguments)
            {
                args = new Vector();
                args.addElement(request);
                args.addElement(response);
                args.addElement(out);
            }

	    // engine.exec (filename, 0, 0, bodyOut.getString() );
	    Object result = engine.apply (filename, 0, 0, scriptCode, paramNames, args );
            if (result!=null && isExpression())
            {
                out.print(result.toString());  // just output String value in place
            }

            if (bReflect || slot!=null)     // make sure we remove the reflect map entry
            {
                mgr.undeclareBean(TAGLIBINFORMATION);
            }
	}
        catch( Exception e )
        {
            if (bReflect || slot!=null)     // make sure we remove the reflect map entry
            {
                try {
                    mgr.undeclareBean(TAGLIBINFORMATION);
                } catch (BSFException bsfe)
                {};
            }
	    // System.out.println( e.toString() );
	    e.printStackTrace();
            if (bThrowException)         // throw a JSPException?
            {
                throw new JspException(e);
            }
	}

        return( EVAL_PAGE );
    };

    static String escape(String str)
    {
        if (str==null) return str;

        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                  .replace(" ", "&nbsp;").replace("\r", " ").replace("\n","<br>");
    }
}
