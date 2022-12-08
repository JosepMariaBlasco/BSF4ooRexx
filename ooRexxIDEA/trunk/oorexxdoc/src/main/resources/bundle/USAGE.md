---
title: ooRexxDoc Standalone Usage
author: Alexander Seik  
date: ##date##  
ooRexxDoc-version: ##version##
---
# ooRexxDoc Standalone
ooRexxDoc is a tool for creating an HTML documentation from ooRexx code.
There are two ways to use ooRexxDoc Standalone from command line
- via XML configuration file.
- via XML configuration input string

## Requirements
ooRexxDoc is based on Java and needs a Java Runtime Environment (JRE) >= Java 17. The tool is also integrated in the
ooRexxPlugin and can be run via file context menu. ooRexxDoc is heavily based on the IntelliJ IDEA framework.
Therefore, a user of ooRexxDoc Standalone must install https://www.jetbrains.com/idea/ (19.11.2022). IntelliJ Community Edition is sufficient for running ooRexxDoc

The following shows how to run ooRexxDoc and two usage examples for the two possible ways to use ooRexxDoc.
## Running ooRexxDoc
ooRexxDoc needs the IntelliJ IDEA libraries on classpath. Locate /lib folder inside the IntelliJ installation Path. . 
Inside the installation is a /lib folder that needs to be added to the java classpath. Also, ooRexxDoc must be available on the classpath.

A running example:  
`java -cp "<path-to-oorexxdoc>/oorexxdoc-<version>.jar:<path-to-intellij>/lib/*" org.oorexx.doc.Application <ooRexxDoc-command-line-args>`

## Examples
For both examples three sample ooRexx sources will be used: BSF.CLS, json.cls, and FileManager.rxj.
```
|____sources
| |____ooRexx
| | |____json.cls
| | |____CPLv1.0.txt
| | |____README.txt
| |____bsf4ooRexx
| | |____BSF.CLS
| | |____NOTICE.txt
| | |____README.txt
| | |____FileManager.rxj
```

ooRexxDoc can be run with an XML configuration. The XML configuration can look as following:
```
<?xml version="1.0" encoding="UTF-8"?>
<OoRexxDocXml>
    <parser>
        <output>./doc</output>
        <input>./sources/bsf4ooRexx/FileManager.rxj</input>
        <input>./sources/bsf4ooRexx/BSF.CLS</input>
        <input>./sources/ooRexx</input>
    </parser>
    <title>My Documentation</title>
    <header>
        <property>
            <key>Run-Date</key>
            <value>${.now?datetime}</value>
        </property>
    </header>
    <overview>My Overview</overview>
    <settings>
        <includePublic>true</includePublic>
        <includePrivate>true</includePrivate>
        <includePackage>true</includePackage>
        <includeVariableReferences>true</includeVariableReferences>
    </settings>
</OoRexxDocXml>
```
The example configuration shows multiple <input>-tags: two files and a directory containing rexx sources.
There is also a single output directory in the <output>-tag. The documentation title can be specified in the <title>-tag.
Header data which is shown in the top right corner can be placed in the <header>-tag (see example). 
It is also possible to specify Freemarker templates in the value inside the <header>-tag.
On the front page the overview data is shown. There is also the possibility to filter directives (see <settings>-tag).

### Example 1: Run via XML Configuration String
This example shows how to run ooRexxDoc with an input string.
```
java -cp "<path-to-oorexxdoc>/oorexxdoc-<version>.jar:<path-to-intellij>/lib/*" org.oorexx.doc.Application "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><OoRexxDocXml><parser><output>./doc</output><input>./sources/bsf4ooRexx/FileManager.rxj</input><input>./sources/bsf4ooRexx/BSF.CLS</input><input>./sources/ooRexx</input></parser><title>MyDocumentation</title><header><property><key>Run-Date</key><value>\${.now?datetime}</value></property></header><overview>MyOverview</overview><settings><includePublic>true</includePublic><includePrivate>true</includePrivate><includePackage>true</includePackage><includeVariableReferences>true</includeVariableReferences></settings></OoRexxDocXml>"
```

The output is in ./example/doc.
### Example 2: Run via XML Configuration File
```
java -cp "<path-to-oorexxdoc>/oorexxdoc-<version>.jar:<path-to-intellij>/lib/*" org.oorexx.doc.Application ./ooRexxDoc.xml
```
The output is in ./example/doc.