2020-09-07, Rony G. Flatscher

        - Resurrected the Apache BSF taglib for distribution in BSF4ooRexx

        - ooRexx programs can use bsf.lookup("name") for the following registered
          beans:

                "application"
                "config"
                "exception"
                "out"
                "page"
                "pageContext"
                "request"
                "response"
                "session"

        - hint: use .input~destination(bsf.OutputStream~new(out,,"0d0a"x)) to have ooRexx SAY
                statement write to the "out" JSPWriter

        - in order to compile the Java sources the compiler needs access to Tomcat's:

                lib\servlet-api.jar
                lib\jsp-api.jar



Great resources:

        <https://tomcat.apache.org/taglibs/site/tutorial.html>

        <https://www.javatpoint.com/jsp-tutorial>

*Great resources*
        Home: <https://www.ntu.edu.sg/home/ehchua/programming/index.html>

        Tomcat Servlets/JSP: <https://www.ntu.edu.sg/home/ehchua/programming/howto/Tomcat_HowTo.html>
        JSP: <https://www.ntu.edu.sg/home/ehchua/programming/java/JavaServerPages.html>



 ? <https://www.mulesoft.com/tcat/tomcat-context>


<https://docs.oracle.com/javaee/6/api/constant-values.html#javax.servlet.jsp.PageContext.PAGE_SCOPE>
        javax.servlet.jsp.PageContext
        public static final java.lang.String 	APPLICATION 	"javax.servlet.jsp.jspApplication"
        public static final int 	APPLICATION_SCOPE 	4
        public static final java.lang.String 	CONFIG 	"javax.servlet.jsp.jspConfig"
        public static final java.lang.String 	EXCEPTION 	"javax.servlet.jsp.jspException"
        public static final java.lang.String 	OUT 	"javax.servlet.jsp.jspOut"
        public static final java.lang.String 	PAGE 	"javax.servlet.jsp.jspPage"
        public static final int 	PAGE_SCOPE 	1
        public static final java.lang.String 	PAGECONTEXT 	"javax.servlet.jsp.jspPageContext"
        public static final java.lang.String 	REQUEST 	"javax.servlet.jsp.jspRequest"
        public static final int 	REQUEST_SCOPE 	2
        public static final java.lang.String 	RESPONSE 	"javax.servlet.jsp.jspResponse"
        public static final java.lang.String 	SESSION 	"javax.servlet.jsp.jspSession"
        public static final int 	SESSION_SCOPE 	3

        scopes 1 (page), 2 (request), 3 (session), 4 (application)

