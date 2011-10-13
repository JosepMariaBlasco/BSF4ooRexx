This program will run starting with Java version 1.4 as it contains
the XSL support in its distribution.

The program demonstrates how one can use XSLT as a scripting language
to fill in a Java data structure using some XML data and a stylesheet.

To run it, type:

	java TableFiller style1.xsl table-data.xml
or
	java TableFiller style2.xsl table-data.xml

or
        rexx TableFiller.jrexx  style{1|2}.xsl table-data.xml
        rexx TableFiller-eh.rxj style{1|2}.xsl table-data.xml

*IMPORTANT Note* on using Java 1.5, Java 1.6 or higher:

        - if the sample does not work in these environments then, copy the Xalan-jars from
          "http://xml.apache.org/xalan-j/downloads.html" distribution into the "endorsed"
          directory of these Java versions (e.g. "JAVA_JRE_HOME /jre/lib/endorsed/"); make
          sure that the archive "xalan.jar" (from the xalan-tool jar) is available as well;
          tested with Xalan 2.7 and 2.8 on Java 1.5 and Java 1.6
          [alternatively you may add the downloaded jars to the CLASSPATH]

If it works, then be impressed and have fun!
;-)

---rgf, 2006-09-07, 2010-05-24

