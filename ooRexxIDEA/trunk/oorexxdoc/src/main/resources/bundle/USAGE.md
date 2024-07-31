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
    <templateName>classic</templateName>
</OoRexxDocXml>
```
The example configuration shows multiple `<input>`-tags: two files and a directory containing rexx sources.
There is also a single output directory in the `<output>`-tag. The documentation title can be specified in the `<title>`-tag.
Header data which is shown in the top right corner can be placed in the `<header>`-tag (see example). 
It is also possible to specify Freemarker templates in the value inside the `<header>`-tag.
On the front page the overview data is shown. There is also the possibility to filter directives (see `<settings>`-tag).
There are two built-in templates: classic and markdown. To use one of them set the `<templateName>`-tag to either "classic" or "markdown" as shown above.
At ooRexxDoc initialization the two packed built-in templates are copied to a temporary directory and are deleted at JVM shutdown.

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

### Example 3: Create a Markdown documentation
```
java -cp "<path-to-oorexxdoc>/oorexxdoc-<version>.jar:<path-to-intellij>/lib/*" org.oorexx.doc.Application ./ooRexxDoc-markdown.xml
```
The output is in ./example/doc-markdown.

## Custom templates
Custom templates can be provided to create custom ooRexxDoc documentations.
ooRexxDoc uses Freemarker as templating engine. 
So one can provide Freemarker template files as shown below to extend ooRexxDoc. 

ooRexxDoc searches the following paths for templates:
- JVM-System property: -DooRexxDoc.templatesDir
- Path set for the environment variable "OOREXXDOC_TEMPLATE_HOME"
- User Home: $user.home/ooRexxDoc/templates

The path has to be a directory. Inside the directory there should be one sub-dir for every template. 
```
.
|____classic
| |____template.json
| |____search-data-template.ftl
| |____js
| | |____typeahead-bootstrap.js
| | |____jquery.min.js
| | |____bootstrap.min.js
| |____main.ftl
| |____package.ftl
|____markdown
| |____template.json
| |____markdown.ftl

```
Each sub-dir should contain a `template.json` file.

Example template.json:
```json
{
  "descriptor": {
    "name": "classic",
    "mainTemplateFileName": "main.ftl",
    "mainTargetFileName": "oorexxdoc.html"
  },
  "templateFiles": [
    {
      "file": "main.ftl"
    },
    {
      "file": "package.ftl"
    },
    {
      "file": "search-data-template.ftl"
    }
  ],
  "supportingFiles": [
    {
      "file": "./js/"
    }
  ]
}
```

- the `name` is the name of the plugin. This name is the name that should be provided in the `<templateName>`.
- `mainTemplateFileName` is the starting template, inside a template other templates can be used via e.g.: `${filler.fill(fillContext, "package.ftl", "./packages/${package.name}.html", package)}`
  - `fill(fillContext: FillContext, templateName: String, fileName: String, currentElement: Any?)`
- `mainTargetFileName` is the destination name of the main template, e.g.: index.html.
- `templateFiles` are available via the `filler.fill(...)` call as shown above and as the main `mainTemplateFileName`.
- `supportingFiles` files and dirs are copied to the output destination top-level, e.g.: javascript libraries. This can be `null`.

Both built-in templates are provided in the examples to support understanding custom templates

In the provided `javadoc-templating` there is the data model that is used in the templates. 
At start is a list of OoRexxPackage that can be accessed with: `${fillContext.ooRexxPackages}`.
A good starting point would be to have a look at the markdown template. 
For multi-file templates, have a look at the classic template.
