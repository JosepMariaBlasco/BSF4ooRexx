ooRexx Plugin for IntelliJ IDEA
-------------------------------
Author: Alexander Seik
Date: ##date##
License: AL 2.0
Version: ##version##

Changelog:
	- Test Fixes
	- Build Improvements

Please report any bug you find or RFEs (request for enhancement) here: https://sourceforge.net/p/bsf4oorexx/oorexxplugin/milestone/2.0/

Prerequisite:
- IntelliJ IDEA ##intelliJVersion## Community Edition or Ultimate
(older IntelliJ IDEA versions are currently not supported. The update can be done by clicking: Help -> Check for updates... )
        - URL: <https://www.jetbrains.com/idea/download/>

Installing the plugin
- Download OoRexxPlugin-##version##.zip
- Do not unzip this the file. IntelliJ IDEA will import this archive.
- In IntelliJ IDEA: File -> Settings (or "Configure" depending on the IntelliJ IDEA version) -> Plugins -> Press the gear icon -> Install plugin from disc... -> Choose OoRexxPlugin-##version##.zip.
- Restart IntelliJ IDEA.

Try the plugin
- Add a new ooRexx project (File -> New -> Project -> select ooRexx) and/or you can add files with ooRexx file-extensions e.g. hello_world.rexx (File -> New -> File) or open ooRexx files via Open -> Open File . These files will be considered by the plugin.
- Run ooRexx files via the context menu of the file -> Run '...'.
- It is also possible change the text mode of the plugin to Mainframe-Rexx via File -> Settings -> Languages & Frameworks -> ooRexx -> checkbox 'allow special characters'
- To run ooRexxDoc (documentation generation tool): In the file tree right click on an ooRexx file or a directory containing an ooRexx file -> Create 'ooRexxDoc' Documentation ...

ooRexxDoc comments will be included in the generated documentation. An ooRexxDoc comment is a normal Rexx block comment
that starts with the with "/**" (note the second asterisk).
If an ooRexxDoc comment includes the ooRexxDoc annotations (first character is the at-sign '@'),
ooRexxDoc will process them and format them accordingly, here the current list of these annotations:

@author     ... name of the author(s)
@condition  ... condition that may be raised
@deprecated ... class, method etc. that may be removed in some future version
@param      ... parameter/argument description
@return     ... information about the return value
@see        ... other file, class, method, routine, etc.
@since      ... version number of introducing class, method, routine, etc.
@version    ... version number of class, method, routine, etc.

Comments are evaluated for: packages, classes, methods, routines, attributes, constants, and resources.

Here is an example for a valid ooRexxDoc comment:

/** This is an ooRexxDoc comment for a class.
*   @author John & Mary Doe
*   @since   1.0 (2020-05-14)
*   @version 1.0
*/
::class my_class

or without leading asterisk per line:

/** This is an ooRexxDoc comment for a class.
   @author John & Mary Doe
   @since   1.0 (2020-05-14)
   @version 1.0
*/
::class my_class
