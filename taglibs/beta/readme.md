## Purpose ##

The taglibs jar files in this directory allow Rexx to be used in Java server pages (JSP)
to create code and text for the HTML text to be sent back to the client.

There are two scripting frameworks employed:

* Apache Bean Scripting Framework (BSF): use the following taglib definition at the top
  of the JSP, if the tld file is stored in the application's WEB-INF subdirectory:
~~~
        <%@ taglib uri="/WEB-INF/script-bsf.tld" prefix="s" %>
~~~

* JSR-223 (Java's "javax.script" package: use the following taglib definition at the top
  of the JSP, if the tld file is stored in the application's WEB-INF subdirectory:
~~~
        <%@ taglib uri="/WEB-INF/script-script.tld" prefix="s" %>
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
return the expression value explicitly with the appropriate return statement (not necessary
in the case of the script language Rexx). This example uses open object Rexx
indicated by the type attribute (type="rexx"):
~~~
        <p>It is now exactly <s:expr type="rexx">.dateTime~new~string</s:expr>.</p>
~~~

Remark: the invocation of the scripts will always supply the following three implicit
        objects as arguments: `request`, `response`, `out`. Scripts can use the `out`
         object's `print` or `println` methods to output HTML text
        at the position of the script (some script languages expect the implicit
        object to be prefixed with the `$` character like `$out`).

## Taglib Attributes ##

The taglib libraries implement the following attributes for the `script` and `expr` elements:

Attribute          | Purpose
------------------ | -------
`type`             | Mandatory, defines the script language to use for executing the code
`name`             | Optional, a string that will be used as a pseudo file-name for the script (debugging)
`src`              | Optional, a string that represents a relative path to the file where the script program to execute is located
`cacheSrc`         | Optional (defaults to "true"), will cache the source: while developping set to "false" to have the source reread each time the JSP gets invoked
`compileSrc`       | JSR-223 only! Optional (defaults to "true"), will compile the script and run the compiled form
`throwException`   | Optional (defaults to "false"), will stop processing JSP in case of a script error
`debug`            | Optional (defaults to "false"), will inject information about JSP invocation (debugging)
`arguments`        | Optional (defaults to "true"), controls supplying the three arguments `request`, `response` and `out` in that order for each script/expr


## Files ##

File name                   | Purpose
--------------------------- | --------------------------
`readme.md`                 | brief infos, text file
`javax.ScriptTagLibs.jar`   | contains the taglibs, uses "javax." package name, binary file (e.g. Tomcat 9 or earlier); copy into the application's `WEB-INF/lib` directory
`jakarta.ScriptTagLibs.jar` | contains the taglibs, uses "jakarta." package name, binary file (TODO, e.g. Tomcat 10 or later); copy into the application's `WEB-INF/lib` directory
`script-bsf.tld`            | describes BSF taglib, text file; copy into the application's `WEB-INF` directory
`script-jsr223.tld`         | describes JSR-223 taglib, text file; copy into the application's `WEB-INF` directory
`ronyDemoRexx20201111-updated.war`  | use your Java-based web server's deploy "war"/"web module" feature to deploy (you need the latest ooRexx 5 and BSF4ooRexx installed on the system)
`demoRexx20201029.zip`      | examples that demonstrate the taglibs for the Rexx language (e.g. Tomcat: unzip into the "webapps" directory)
`IntelliJ-oorexx-language-injections.xml` | Import file for ooRexx language injection support in IntelliJ (see addendum below)


## Example Using Taglibs with Tomcat 9 (`javax.` namespace) ##

* Install Tomcat, e.g. from <https://tomcat.apache.org/> or e.g. on a Mac via Homebrew etc.
* Create the manager and the manager roles in Tomcat's "conf\tomcat-users.xml", e.g.
~~~
        <role rolename="manager-gui"/>
        <role rolename="admin-gui"/>
        <user username="tomcat" password="some_password" roles="admin-gui,manager-gui"/>
~~~
* copy "javax.ScriptTagLibs.jar" to Tomcat's "lib" directory (makes it for all JSPs available)
* if testing the Rexx examples
* * MacOSX (needs Java installed already, see addendum below for Java downloads)
* * * get the latest version of BSF4ooRexx (ooRexx-Java bridge) which also includes ooRexx from <https://sourceforge.net/projects/bsf4oorexx/files/beta/20200928/> and install it
* * Linux and Windows (needs Java installed already, see addendum below for Java downloads)
* * * get the latest version of ooRexx from <https://sourceforge.net/projects/oorexx/files/oorexx/5.0.0beta/> and install it
* * * get the latest version of BSF4ooRexx (ooRexx-Java bridge) from <https://sourceforge.net/projects/bsf4oorexx/files/beta/20200928/> and install it
* * either
* * * deploy "ronyDemoRexx20201111-updated.war" using your Java-based web server management tool (e.g. Tomcat 9: http://localhost:8080/manager/, section "WAR file to deploy")
* * or (deprecated)
* * * copy "bsf4ooRexx-v641-YYYYMMDD-bin.jar" (location can be found by inspecting the environment variable CLASSPATH) to Tomcat's "lib" directory (makes it for all JSPs available)
* * * unzip "demoRexx20201029.zip" into Tomcat's "webapps" directory

* load the Tomcat manager interface into your browser, e.g. "localhost:8080/manager"
* * click on "/demoRexx" and pick the JSP examples for Rexx that you wish to look at

## Addendum: IntelliJ ooRexx Support ##

There is an ooRexx plugin for IntelliJ available from <https://sourceforge.net/projects/bsf4oorexx/files/Sandbox/aseik/ooRexxIDEA/GA/>.
Download the latest plugin zip archive and point IntelliJ Plugin configuration to it in order to load.

<em>IntelliJ</em> is an "integrated development application" (IDEA) for which a free "community" edition exists
which can be downloaded from <https://www.jetbrains.com/idea/download/>.

Then save `IntelliJ-oorexx-language-injections.xml` and import it in the "Settings -> Editor -> Language Injections"
to allow ooRexx code to be highlighted in HTML, XML and JSP text.


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

