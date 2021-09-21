## Purpose ##

The taglibs jar files in this directory allow Rexx to be used in Java server pages (JSP)
to create code and text for the HTML text to be sent back to the client. This version
needs BSF4ooRexx 641.20210706 (the ooRexx-Java bridge) or newer.

An initial presentation was done at the 2020 International Rexx Symposium
(https://www.rexxla.org/events/2020/schedule.html) for which the following
materials got created:

* "(BSF4)ooRexx and Java Web Servers"
  article: <https://www.rexxla.org/events/2020/presentations/202011-ooRexxAndJavaWebServers-article.pdf>,
  and a zip-file with accompanying war-files at:
  <https://www.rexxla.org/events/2020/presentations/202011-ooRexxAndJavaWebServers-article-code.zip>

As the above article and some JSP files include ooRexx code, a very brief introduction to ooRexx:

* "Resurrecting REXX, Introducing ooRexx"
  article: <http://wi.wu.ac.at/rgf/rexx/misc/ecoop06/ECOOP2006_RDL_Workshop_Flatscher_Paper.pdf>
  slides:  <http://wi.wu.ac.at/rgf/rexx/misc/ecoop06/2006_ecoop_ooRexx.pdf>

There are two scripting frameworks supported:

* Apache Bean Scripting Framework (BSF), allows older scripting languages to be used,
  use the following taglib definition at the top of the JSP:
~~~
        <%@ taglib uri="http://rexxla.org/taglibs/bsf" prefix="s" %>
~~~

* _Preferred! JSR-223 (Java's `javax.script` package), allows newer scripting languages to be used,_
   use the following taglib definition at the top of the JSP:
~~~
        <%@ taglib uri="http://rexxla.org/taglibs/jsr223" prefix="s" %>
~~~

Then, anywhere in the JSP file where you wish to employ script code you would use the
"script" tag (no matter which taglib you are using). This example uses open object Rexx
indicated by the type attribute (type="rexx"):
~~~
        <s:script type="rexx">
           say '<p>Hello, world, this is Rexx speaking at:' .dateTime~new '</p>'
        </s:script>
~~~

If you wish to merely fetch the returned result of an expression defined in a scripting language
you would use the "expr" tag. Note, depending on the script language, you might need to
return the expression value explicitly with the appropriate return statement.
This example uses open object Rexx (ooRexx) indicated by the type attribute (type="rexx"):
~~~
        <p>It is now exactly <s:expr type="rexx">return .dateTime~new~string</s:expr>.</p>
~~~

Remark: the invocation of the scripts will always supply the following three implicit
        objects as arguments: `request`, `response`, `out`. Scripts can use the `out`
        object's `print` or `println` methods to output HTML text
        at the position of the script (some script languages expect the implicit
        object to be prefixed with the `$` character like `$out`).



---

## Taglib Attributes ##

The taglib libraries implement the following attributes for the `script` and `expr` elements:

Attribute          | Purpose
------------------ | -------
`type`             | Mandatory, defines the script language to use for executing the code
`arguments`        | Optional (defaults to "true"), controls supplying the three arguments `request`, `response` and `out` in that order for each script/expr
`cacheSrc`         | Optional (defaults to "true"), will cache the source: while developping set to "false" to have the source reread each time the JSP gets invoked
`compile`          | JSR-223 only! Optional (defaults to "true"), will compile the script and run the compiled form
`debug`            | Optional (defaults to "false"), will inject information about JSP invocation (debugging)
`name`             | Optional, a string that will be used as a pseudo file-name for the script (debugging)
`reflect`          | Optional (defaults to "false"), if "true" creates implicit HashMap object named "ScriptTaglibs.TaglibNamespaceAttributevalues" with taglib information, namespace and current attribute values
`src`              | Optional, a string that represents a relative path to the file where the script program to execute is located
`slot`             | Optional, a string that a JSP/script developer can use for any purpose (if value present it can be fetched from the implicit HashMap object named "ScriptTaglibs.TaglibNamespaceAttributevalues")
`throwException`   | Optional (defaults to "false"), will stop processing JSP in case of a script error


---

## Files ##

File name                   | Purpose
--------------------------- | --------------------------
`readme.md`                 | this text file: brief infos, text file
**`javax.`**`ScriptTagLibs.jar` | taglib library, uses "**javax.**" (JavaEE) as the top level package name, binary file (Java EE 8 or earlier, e.g. Tomcat 9 or earlier);   copy into the shared `lib` directory of your Java based web server (e.g. Tomcat: `$CATALINA_HOME/lib`)
**`jakarta.`**`ScriptTagLibs.jar` | taglib library, uses "**jakarta.**" (JakartaEE) as the top level package name, binary file (Jakarta EE 9 or later, e.g. Tomcat 10 or later); copy into the shared `lib` directory of your Java based web server (e.g. Tomcat: `$CATALINA_HOME/lib`)
`demoASF2021.war`           | use your Java-based web server's deploy "war"/"web module" feature to deploy (you need the latest ooRexx 5 and BSF4ooRexx installed on the system) or copy it into your Java based web server's `webapps` directory; used for the ApacheCon 2021 presentation
`demoRexx.war`              | use your Java-based web server's deploy "war"/"web module" feature to deploy (you need the latest ooRexx 5 and BSF4ooRexx installed on the system) or copy it into your Java based web server's `webapps` directory; demonstrates usage of ooRexx
`demoSTL-Core-SQL_ooRexx.war` | demonstrates STL-SQL and ooRexx ounterparts, includes SQLLite (first usage: run "ooRexx_reset_database.jsp"), JavaEE STL libraries
`demoSTL-jakarta-Core-SQL_ooRexx.war` | demonstrates STL-SQL and ooRexx ounterparts, includes SQLLite (first usage: run "ooRexx_reset_database.jsp"), includes JakartaEE STL libraries
`IntelliJ-oorexx-language-injections.xml` | Import file for ooRexx language injection support in IntelliJ (see addendum below)
`demoJavaScript.war`        | use your Java-based web server's deploy "war"/"web module" feature to deploy or copy it into your Java based web server's `webapps` directory; demonstrates usage of Nashorn/JavaScript
`demoRhino.war`             | use your Java-based web server's deploy "war"/"web module" feature to deploy or copy it into your Java based web server's `webapps` directory; demonstrates usage of Rhino/JavaScript
`script-bsf.tld`            | describes BSF taglib, text file, included in ScriptTagLibs.jar's META-INF directory;
`script-jsr223.tld`         | describes JSR-223 taglib, text file, included in ScriptTagLibs.jar's META-INF directory;


---

## Example Using Taglibs with Tomcat 9 (`javax.` namespace) or 10 (`jakarta.` namespace) ##

* Install Tomcat, e.g. from <https://tomcat.apache.org/> or e.g. on a Mac via Homebrew etc.
* Create the manager and the manager roles in Tomcat's "conf\tomcat-users.xml", e.g.
~~~
        <role rolename="manager-gui"/>
        <role rolename="admin-gui"/>
        <user username="tomcat" password="some_password" roles="admin-gui,manager-gui"/>
~~~



---

* Testing the ooRexx samples in `demoRexx.war` needs the latest ooRexx 5.0 or later and BSF4ooRexx (20201217 distribution or later) and deploying `demoRexx.war`:

    + MacOSX (needs Java installed already, see addendum below for Java downloads)

          - get the latest version of BSF4ooRexx (ooRexx-Java bridge) which also includes ooRexx from <https://sourceforge.net/projects/bsf4oorexx/files/beta/20200928/> and install it

    + Linux and Windows (needs Java installed already, see addendum below for Java downloads)

          - get the latest version of ooRexx from <https://sourceforge.net/projects/oorexx/files/oorexx/5.0.0beta/> and install it

          - get the latest version of BSF4ooRexx (ooRexx-Java bridge) from <https://sourceforge.net/projects/bsf4oorexx/files/beta/20200928/> and install it


* copy the file `bsf4ooRexx-v6??-202?????-bin.jar` from your BSF4ooRexx installation into your
  Java based web application server's shared `lib` directory usually located in the installation/home
  directory (otherwise classloader problems may show up):

    -> for "**Java EE**" (e.g. Tomcat 9 or earlier) copy **`javax.`**`ScriptTagLibs.jar` into your
       Java based web application server's shared `lib` directory (e.g. Tomcat `$CATALINA_HOME/lib`)

    -> for "**Jakarta EE**" (e.g. Tomcat 10 or later) copy **`jakarta.`**`ScriptTagLibs.jar` into your
       Java based web application server's shared `lib` directory (e.g. Tomcat `$
       CATALINA_HOME/lib`)

* deploy `demoRexx.war` (web application archive)

    + either copy `demoRexx.war` to your Java based web server's `webapps` directory or use its deployment management tool

    + load the Tomcat manager interface into your browser, e.g. "localhost:8080/manager" (adjust the port number to your installation),

* click on "/demoRexx" and pick the JSP examples for Rexx that you wish to look at (alternatively use the URL
    "localhost:8080/demoRexx", adjust the port number to your installation)

---

---

## Addendum: Namespace "**javax.**" versus "**jakarta.**" ##

Cf. <https://www.baeldung.com/java-enterprise-evolution> and <https://en.wikipedia.org/wiki/Jakarta_EE>  (as of 2020-11-12).

In essence: earlier implementations of Java based web servers implementing
the "enterprise edition" specifications used the package name "javax.".
Oracle approached the open-source Eclipse foundation that took over
development of future "Java enterprise editionS (EE)" and told them not to
use neither "Java EE" nor "javax." (as the top level package name)!

This has an impact on quite a few packages, including taglib libraries
that implement e.g.  the servlet specifications for up to and including
Java EE 8 (released 2017) which use "javax." as the top level package
name.  Starting with version 9 (released 2018, "Jakarta EE 9")
the top level package name got changed to "jakarta." in
all Java packages implementing the Jakarte EE spedifications.

Therefore there are two versions of the ScriptTagLibs.jar, one indicating
to refer to the top level package name "javax." (for Java EE 8 or earlier,
hence named **`javax.`**`ScriptTagLibs.jar`) and one that refers to "jakarta."
(for Jakarte EE 9 or later, hence named **`jakarta.`**`ScriptTagLibs.jar`).

---

## Addendum: IntelliJ ooRexx Support ##

There is an ooRexx plugin for IntelliJ available from <https://sourceforge.net/projects/bsf4oorexx/files/Sandbox/aseik/ooRexxIDEA/GA/>.
Download the latest plugin zip archive and point IntelliJ Plugin configuration to it in order to load.

<em>IntelliJ</em> is an "integrated development application" (IDEA) for which a free "community" edition exists
which can be downloaded from <https://www.jetbrains.com/idea/download/>.

Then save `IntelliJ-oorexx-language-injections.xml` and import it in the "Settings -> Editor -> Language Injections"
to allow ooRexx code to be highlighted in HTML, XML and JSP text.


---

## Addendum: JavaScript with Nashorn ##

* Testing the Nashorn/JavaScript samples in `demoJavaScript.war` needs Nashorn (included up to Java 14, later add the Nashorn module to your installation, cf. <https://openjdk.java.net/projects/nashorn/>) and deploying `demoJavaScript.war`:

* deploy `demoJavaScript.war` (web application archive)

    - either copy `demoJavaScript.war` to your Java based web server's `webapps` directory or use its deployment management tool

    - load the Tomcat manager interface into your browser, e.g. "localhost:8080/manager" (adjust the port number to your installation),

* click on "/demoJavaScript" and pick the JSP examples for Nashorn/JavaScript that you wish to look at (alternatively use the URL
  "localhost:8080/demoJavaScript", adjust the port number to your installation)

---

## Addendum: JavaScript with Rhino ##

* Testing the Rhino/JavaScript samples in `demoRhino.war` needs Rhino (tested against Rhino 1.7.13
  from the bottom of <https://github.com/mozilla/rhino/releases/tag/Rhino1_7_13_Release>):

    - get `rhino-runtime-1.7.13.jar` and copy it to your web server's shared `lib` directory (e.g. Tomcat's `$CATALINA_HOME/lib`)

    - get `rhino-engine-1.7.13.jar`(JSR-223 engine) and copy it to your web server's shared `lib` directory (e.g. Tomcat's `$CATALINA_HOME/lib`)

* deploy `demoRhino.war` (web application archive)

    - either copy `demoRhino.war` to your Java based web server's `webapps` directory or use its deployment management tool

    - load the Tomcat manager interface into your browser, e.g. "localhost:8080/manager" (adjust the port number to your installation),

* click on "/demoRhino" and pick the JSP examples for Rhino/JavaScript that you wish to look at (alternatively use the URL
  "localhost:8080/demoRhino", adjust the port number to your installation)


---


## Addendum: Java Downloads ##

There is a proprietary Java implementation by Oracle, but there are numerous open-source
Java installation packages from the same open source by other distributors. If you do
not have Java installed yet, then try to locate an installation package that includes "JavaFX" ('OSS' means
'open source software', 'LTS' means 'long term support'):
<ul>
    <li>Liberica (look for 'full Liberica', OSS): <https://bell-sw.com/>
    <li>Zulu (look for 'FX' versions, OSS): <https://www.azul.com/downloads/>
    <li>AdoptOpenJDK (OSS): <https://adoptOpenJDK.net>
    <li>Oracle (note license):   <https://www.java.com>
</ul>

---

rgf, as of: 2021-09-21
