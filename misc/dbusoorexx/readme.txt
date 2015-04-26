"dbusoorexx" Package
====================

This ooRexx package enables ooRexx scripts to take full advantage of DBus as
clients, but also as servers (providing DBus services to any DBus client). It
needs at ooRexx version 4.2.0 or higher.

For an introduction to DBus and "dbusoorexx" scripts see the supplied PDF
article "dbusoorexx-introduction.pdf". Also look for the keywords "DBus" or
"D-Bus" on <http://wi.wu.ac.at/rgf/diplomarbeiten/>.

files in the "dbusoorexx" package:

        dbusoorexx/dbus.cls             ... DBus ooRexx package
        dbusoorexx/dbusdoc.css          ... CSS for dbusdoc.rex output
        dbusoorexx/dbusdoc.rex          ... creates on-the-fly HTML
                                            documentation of DBus services
        dbusoorexx/dbusListObjectPaths.rex ... lists DBus object paths of
                                               a DBus service, if any
        dbusoorexx/dbusoorexx-introduction.pdf ... introduction to dbusoorexx
        dbusoorexx/readme.txt           ... this document
        dbusoorexx/rgf_util2.rex        ... optional ooRexx utility package, cf.
                                            <http://wi.wu.ac.at/rgf/rexx/orx20/>

        ------------------------------------------------------------------------

        dbusoorexx/examples/demoHelloClient.rex   ... ooRexx DBus client using
                                                      any one of the ooRexx DBus
                                                      services below

                Demonstrating different coding styles for ooRexx DBus services:
        dbusoorexx/examples/demoHelloService1.rex ... ooRexx DBus service
        dbusoorexx/examples/demoHelloService2.rex ... ooRexx DBus service
        dbusoorexx/examples/demoHelloService3.rex ... ooRexx DBus service
        dbusoorexx/examples/demoHelloService3.xml ... Introspection data
        dbusoorexx/examples/demoHelloService4.rex ... ooRexx DBus service

        dbusoorexx/examples/helloWorld.rex   ... uses a DBus Notification service
        dbusoorexx/examples/margiolKate.rex  ... interacts with the Kate editor
        dbusoorexx/examples/margiolVLC.rex   ... interacts with the VLC media player
        dbusoorexx/examples/signalListener.rex ... listens to and dumps DBus signals

        ------------------------------------------------------------------------

        dbusoorexx/install/install_dbusoorexx.rex ... installation ooRexx script

        dbusoorexx/install/linux/install.sh      ... installs "dbusoorexx"
        dbusoorexx/install/linux/reinstall.sh    ... reinstalls "dbusoorexx"
        dbusoorexx/install/linux/uninstall.sh    ... uninstalls "dbusoorexx"

        dbusoorexx/install/macosx/install.sh     ... installs "dbusoorexx"
        dbusoorexx/install/macosx/reinstall.sh   ... reinstalls "dbusoorexx"
        dbusoorexx/install/macosx/uninstall.sh   ... uninstalls "dbusoorexx"

        dbusoorexx\install\windows\install.cmd   ... installs "dbusoorexx"
        dbusoorexx\install\windows\reinstall.cmd ... reinstalls "dbusoorexx"
        dbusoorexx\install\windows\uninstall.cmd ... uninstalls "dbusoorexx"
        dbusoorexx\install\windows\kickoff.rex   ... elevates install script
        dbusoorexx\install\windows\Elevate32.exe ... 32-Bit elevate utility
        dbusoorexx\install\windows\Elevate64.exe ... 64-Bit elevate utility

        ------------------------------------------------------------------------

        dbusoorexx/install/libs/dbusoorexx-32.dll  ... 32-Bit Windows DLL
        dbusoorexx/install/libs/dbusoorexx-64.dll  ... 32-Bit Windows DLL
        dbusoorexx/install/libs/libdbusoorexx.dylib ... MacOSX fat library
        dbusoorexx/install/libs/libdbusoorexx32.so ... 32-Bit Linux library
        dbusoorexx/install/libs/libdbusoorexx64.so ... 64-Bit Linux library

        ------------------------------------------------------------------------

If there are any questions or problems, please use either the newsgroup
<news:comp.lang.rexx> or one of the e-mail lists of the BSF4ooRexx project
at <http://sourceforge.net/projects/bsf4oorexx/>.

Rony G. Flatscher (author), Vienna, Austria, April 2015



=============================== Installation ===============================
- Linux installation
====================

  - change into the directory "dbusoorexx/install/linux" and run "install.sh";
    this will copy the "dbusoorexx" package to "/opt/dbusoorexx" and link the
    executables to "/usr/bin" and the appropriate library to "/usr/lib"

  - to deinstall run "/opt/dbusoorexx/install/linux/uninstall.sh"

- MacOSX installation
=====================

  - change into the directory "dbusoorexx/install/macosx" and run "install.sh";
    this will copy the "dbusoorexx" package to "/opt/dbusoorexx" and link the
    executables to "/usr/bin" and the appropriate library to "/usr/lib"

  - to deinstall run "/opt/dbusoorexx/install/macosx/uninstall.sh"

  - Please note: on MacOSX DBus is not installed by default; you can get and
    install DBus for MacOSX e.g. from the macport project at
    <http://www.macports.org/> or see the Wikipedia article
    <http://en.wikipedia.org/wiki/MacPorts>


- Windows installation
======================

  - change into the directory "dbusoorexx\install\windows" and run
    "install.cmd"; this will copy the "dbusoorexx" package to the Windows
    %ProgramFile% or %ProgramFile(x86)", depending on the bitness of the
    installed ooRexx interpreter; the executable DLL and Rexx scripts are
    copied to %REXX_HOME% such that you can use immediately "dbbusoorexx"

  - to deinstall find "dbusoorexx" on "%ProgramFile%" or "%ProgramFile(x86)" and
    run "dbusoorexx\install\windows\uninstall.cmd"

  - Please note! On Windows DBus is not installed by default; you can download
    an experimental and untested version built from the original sources from
    freedesktop.org from <http://wi.wu.ac.at/rgf/rexx/orx22/work/>; please
    study the "readme.txt" file there


=============================== URLs ===============================

- dbusoorexx Download Page: <http://sourceforge.net/projects/bsf4oorexx/files/GA/sandbox/dbusoorexx/> -
- dbusoorexx Source Code:   <https://sourceforge.net/p/bsf4oorexx/code/HEAD/tree/sandbox/rgf/misc/dbusoorexx/>

- Official D-Bus Homepage:  <http://www.freedesktop.org/wiki/Software/dbus/>
- Wikipedia on D-Bus:       <http://en.wikipedia.org/wiki/D-Bus>

- ooRexx Homepage:                             <http://www.ooRexx.org>
- Rexx Language Association (RexxLA) Homepage: <http://www.RexxLA.org>

- Information about "rgf_util2.rex": <http://wi.wu.ac.at/rgf/rexx/orx20/>

- BSF4ooRexx Homepage:      <http://sourceforge.net/projects/bsf4oorexx/>

- DBus for Windows:         <http://wi.wu.ac.at/rgf/rexx/orx22/work/>

- MacPorts Homepage:        <http://www.MacPorts.org/>
- Wikipedia on macports:    <http://en.wikipedia.org/wiki/MacPorts>

