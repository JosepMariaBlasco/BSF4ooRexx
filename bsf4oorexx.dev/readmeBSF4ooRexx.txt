BSF4ooRexx850                                            January 2024 (Beta)
=============                                            ===================

This is a package that creates a bridge between ooRexx and Java. For that
reason Java must be installed on your machine as well, which is easy and
a one click away.

- ooRexx download page (use ooRexx 5 or later):

  <https://sourceforge.net/projects/oorexx/files/oorexx/>

- Java download pages, e.g.:

         - azul.com <https://www.azul.com/downloads/?package=jdk#download-openjdk>

                - On the download page choose either "JDK FX" or "JRE FX" to
                  get the Java version with the great JavaFX support

         - bellsoft <https://bell-sw.com/pages/downloads/>

                - On the download page choose "Full JDK" or "Full JRE" to
                  get the Java version with the great JavaFX support

         - Oracle <http://www.java.com> (note the license!)

   See box below for an important information.

PLEASE NOTE: this version of BSF4ooRexx is for Java 1.8/8 and newer

*IMPORTANT* !

    *AFTER DOWNLOADING* the installation zip-archive (e.g.  BSF4ooRexx850_install_*zip)
    but *BEFORE UNZIPPING* make sure that no "security" attributes are attached to it.

    On *WINDOWS*: right mouse click on the zip-file and choose "Unblock" from the
                  Property menu, *then* unzip.

                  [If you do not do this, unexpected/unexplainable errors may
                  occur, when using the new .NET support, as the Windows
                  unzip utility would mark all unzipped files as a security risk
                  as well.]

                  Cf. e.g. <https://winaero.com/how-to-unblock-files-downloaded-from-internet-in-windows-10/>
                  which also shows various possibilities via the PowerShell.

    On *MacOS*: Apple marks all files from the Internet with a quarantine attribute
                and does not allow for running such programs; open a Terminal window
                and go to the download directory and issue the command

                       xattr -d com.apple.quarantine zipOrDmgPkg-file

                where "zipOrDmgPkg-file" is the name of the downloaded zip-archive or
                dmg/pkg installation package


*******************************************************************************
* *ATTENTION*  Java's bitness must match the bitness of ooRexx ("rexx -v"):   *
*                                                                             *
*    - if ooRexx is 32-bit, then you must install the 32-bit version of Java, *
*    - if ooRexx is 64-bit, then you must install the 64-bit version of Java  *
*                                                                             *
*      -> consult after unzipping:  bsf4oorexx/information/0_startHere.html   *
*                                                                             *
* Advice: if you can, use the 64-bit versions of ooRexx and Java              *
*                                                                             *
*******************************************************************************


===============================================================================


1) If there is a previous version of BSF4Rexx or BSF4ooRexx850 installed on your
   system, please deinstall it first! (Make sure all instances of OpenOffice or
   LibreOffice are closed, including the quickstarter!)

   Best use the menu "BSF4ooRexx850 -> Installation -> Uninstall BSF4ooRexx850".

           [A fallback, manual deinstallation, would be to locate the path to the
           installation directory "bsf4oorexx", named "path2-bsf4oorexx" in the
           following commands, and then run (e.g. by double-clicking) the
           appropriate uninstall script:
           - "path2-bsf4oorexx\install\uninstallBSF.cmd" (Windows) or
           - "sudo path2-bsf4oorexx/install/uninstallBSF.sh" (Linux)
           ]

2) Download the software (ooRexx, Java, BSF4ooRexx850)

   - on MacOS and Windows: remove the security/quarantine attributes as described
     above!

3) Unzip the downloaded installation archive "BSF4ooRexx850_install-*.zip" (this
   will also unzip the Apache OpenOffice/LibreOffice scripting-support for ooRexx)

4) change into the unzipped subdirectory "bsf4oorexx/install/windows" or
   "bsf4oorexx/install/linux" or "bsf4oorexx/install/macosx", depending on
   your operating system and then run

   - the setup program named "install.cmd" (Windows) or "sh install.sh"
    (Linux, MacOSX), e.g. by opening a console/terminal changing to the
    unzip location and enter "install.cmd" (Windows) or "sh install.sh" (Unix)
    manually which will cause elevation to Administrator/uperuser/sudo.

Good luck and have fun!

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe)

