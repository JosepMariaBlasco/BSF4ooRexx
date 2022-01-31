#!/usr/bin/env rexx
/* <a href="http://www.ooRexx.org>Object Rexx</a> convenience class for making IBM's and
   Apache's Bean Scripting Framework (BSF) available in an OO-style as well: Java classes and
   Java objects appear as Object Rexx classes and objects and one can send Object Rexx
   messages to them.

   Object Rexx programmers should appreciate this little class ...
   ;)

   last change: $Revision: 927 $ $Author: Administrator $ $Date: 2010-12-26 18:34:19 +0100 (Sun, 26 Dec 2010) $

   needs: ooRexx 4.1.0 (cf. <http://www.ooRexx.org>) and BSF4ooRexx 4.0 or greater

   TODO:  once BSF4ooRexx defines ooRexx 5.0 to be the minimal supported ooRexx version, then
          its new features will be taken advantage of; one being ".context~package~local" to store
          the class objects for bsf.import() and bsf.loadClass() instead of ".environment" (used to
          be .local): such a change would ca
          use changes in BSF4ooRexx as well, which tries to locate
          .BSF in .environment;

   date:
         2022-01-20, ---rgf, - add fourth optional argument to bsf.compile(): by default .true
                               and returns the newClass processed with bsf.importClass() such
                               that it understands the ooRexx class message "new" and "newStrict";
                               in the case that someone needs to define an abstract class then
                               this fourth argument needs to not be .true such that the plain
                               Java class proxy gets returned unprocessed (bsf.import() would
                               raise an error, if the supplied Java class is abstract)
         2022-01-15, ---rgf, - add more error message hints in bsf.compile(), check arguments
                             - camouflage NetRexx class object as ooRexx class object (add
                               class methods new and newStrict)
                             - bsf.compile() NetRexx compiler expects unqualified class name for
                               compilation, but fully qualified for looking up the generated
                               class object; adjust for this such that packages can be defined
                               in inline NetRexx programs
         2022-01-13, ---rgf, - add ability to compile NetRexx programs to routine bsf.compile()
         2021-08-29, ---rgf, - add System's "os.arch" property in quotes to the Java chunk in
                               "display.version" entry

         2021-08-23, ---rgf, - if a weird "java.version" value gets returned that cannot be parsed
                               into a whole number for the "major" part use "java.specification.version"
                               instead

         2021-08-22, ---rgf, - "java.version" from early-access/beta versions of OpenJDKs may be of the
                               form "18-ea" and the like, rather than in the form "major.minor.rest";
                               cater for that by translating only numbers and dots and then parse that

         2021-08-06, ---rgf, - mark all attributes and methods explicitly as guarded which to
                               "defend" against side-effects of the new ooRexx 5.0 setting of
                               methods by default to "unguarded", if they do not contain either
                               the EXPOSE or the USE LOCAL statement; this has undesired side
                               effects, hence making sure that the guard state is explicitly
                               set to insulate from any future changes in this regards; this
                               fix is necessary to get "bsf4oorexx/samples/JavaFX/javafx_update_GUI-from-non-GUI-thread.rxj"
                               and "bsf4oorexx/samples/3-090_update_awtSwing_GUI-from-non-GUI-thread.rxj"
                               working again

         2021-02-16, ---rgf, - routine parseJavaOptions() will now add option "java.class.path" if
                               not defined using CLASSPATH environment variable (remove duplicates,
                               explode trailing asterisks '*'; in Java 6 explosion of asterisks was
                               done, at least starting with Java 8 we need to do that!)

         2021-02-05, ---rgf, - Bsf_Array_Reference::supplier will now use new ArrayWrapper.supplier(RexxProxy)
                               method and unbox the returned genuine ooRexx supplier object; this is to
                               allow the new ooRexx 5.0 "DO WITH" construct to work with wrapped Java arrays;
                               Supplier.java not used anymore by BSF4ooRexx

         2020-10-22, ---rgf, - add "string" methods to bsf.inputStream and bsf.outputStream
                             - allow to configure bsf.outputStream to not use flush method (attribute "doFlush")

         2020-09-12, ---rgf, - fix bsf.redirectTo:
                               - check if supplied objects are instances of bsf.inputStream or
                                 bsf.outputStream and change logic accordingly
                               - if bsf.inputStream or bsf.outputStream objects supplied do not change prefix at all
                               - fix reusing .input for debugInput and .error for traceOutput
                             - bsf.outputStream: expose ENDOFLINE in lineout method
         2020-09-07, ---rgf, - BSF.OutputStream now allows an optional third argument, the
                               end-of-line characters to use (defaults to .endOfLine); this
                               allows one to make sure that e.g. in the context of creating
                               HTML via J2EE the correct end-of-line character sequence
                               is being used ("0d0a"x) independent of the current operating
                               system's end-of-line

         2020-08-14, ---rgf, - reformatted ppJavaExceptionChain(co[,bStackTrace])
                             - add operating system infos from sysVersion() to .bsf4rexx~display.version

         2020-08-12, ---rgf, - added public routine ppJavaExceptionChain(co[,bStackTrace]) which
                               returns a string showing the Java exception chain to ease spotting
                               the source Java exception for debugging

         2020-07-27, ---rgf, - fix some ooRexxDoc formatting errors

         2019-08-25, ---rgf, - fixed bug creating interface class objects in bsf.createProxyClass()

         2019-08-24, ---rgf, - adjust bsf.createProxyClass() documentation according to effective behaviour of
                               ClassAdapter.java

         2019-08-21, ---rgf, - adjust for renaming "org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool" to
                               "org.rexxla.bsf.engines.rexx.ClassAdapter"

         2019-02-04, ---rgf, - used wrong versions the last two days: this version has the signature
                               "bsf.loadClass(javaClassName[,name4environment])" and replaces experimental
                               bsf.loadClass2Environment()

         2019-02-03, ---rgf, - changed error message in bsf.import(), if abstract class supplied

         2019-02-02, ---rgf, - to inhibit attempting to instantiate abstract classes indirectly via bsf.import()
                               the logic got changed; as a result routine "initialize.bsf.dir" got adapted to use
                               bsf.loadClass() instead of bsf.importClass() for it

                             - added new routine "bsf.loadClass2environment(jClzName[,indexName]": loads and
                               saves class object in environment (currently .environment)

                             - adapted BSF.InputStream and BSF.outputStream to use bsf.loadClass2bsf4rexx()
                               instead of bsf.import() for the abstract Java Reader and Writer classes

         2019-02-01, ---rgf, - propagate conditions in BSF's constructor method "init"
                             - adapted BSF-version string to indicate minimum ooRexx level as well:
                               minJavaVersion*100+minOoRexx*10 "." date-of-release

         2019-01-07, ---rgf, - change routine "unbox" to take advantage of new BSF subfunction "unbox" (which
                               returns the unboxed value as a string or .nil, if value was not of a boxed/wrapped
                               type); the routine "unbox" will return the supplied value, if that is not a Java
                               boxed/wrapped value without raising an exception

         2019-01-02, ---rgf, - change routine "unbox" to accept plain Rexx strings as well, in which
                               case that value gets returned

         2018-12-23, ---rgf, - new private routine "create_display.version_entry": encodes the
                               currently employed versions of ooRexx, BSF.CLS and Java in a
                               single string to ease debugging; fetch this value via .bsf4rexx~display.version

         2018-11-27, ---rgf, - add the Boolean truth objects named as "boolean.true" and "boolean.false"
                               to the .bsf4rexx directory, such that they can be immediately used in
                               Rexx implemented lambda function return values

         2018-11-02, ---rgf, - renamed 'runLaterPush' to 'runLaterLatestPush'
                             - added 'runLaterPush' that pushes the message, but does not remove
                               messages addressing the same object and message like 'runLaterLatestPush'

         2018-11-02, ---rgf, - leave original implementation of BOX() for backward compatibility, it
                               is semantically equivalent to the term in use for Java
                             - added a new public routine BOX.STRICTARG() which will box the values
                               in an instance of the new class RexxStrictArgument() and returns it;
                               values of this type will force the contained classObject to be exactly
                               in the signature in order for the candidate field, method or constructor
                               to qualify

         2018-11-01, ---rgf, - changed the routines BOX() and UNBOX() to adjust them to the new class
                               RexxStrictArgument which allows to strictly pick the corresponding
                               field, method or constructor candidates; the primitive types will
                               now be forced to strictly pick primitive types in the signatures

         2018-10-25, ---rgf, - changed how "rexx.version" gets parsed and assembled; revision part is omitted,
                               hence: major.minor

         2018-10-24, ---rgf, - added method "runLaterPush" to class "AbstractGuiThread": this method works
                               like "runLaterLatest", removing duplicate messages (addressed to the same object
                               using the same message name) from the queue, but pushes the new GUIMessage to
                               the queue; this way such a message will run before any other ones that are in
                               the queue at the time of the push

         2018-10-07, ---rgf, - add an entry "rexx.version" to .bsf4rexx

         2018-09-18, ---rgf, - routine bsf.contextClassLoader() now also accepts a list of URL strings/objects beside
                               a single array object of URL strings/objects

         2018-09-16, ---rgf, - add ".bsf4rexx~bsf.cls.fullPath" and ".bsf4rexx~bsf.cls.location" to allow querying
                               the location of this BSF.CLS from other Rexx programs easily

                             - new public routine "bsf.contextClassLoader [urlOrDirOrFileName | urlCollection]" that allows
                               to define the Thread's context class loader to find classes and resources in
                               the supplied urlOrDirOrFileName | urlCollection

                             - to ease the support for "bsf.contextClassLoader" the following Java classes:
                               'java.lang.ClassLoader', 'java.lang.Thread', 'java.io.File',
                               'java.net.URL', 'java.net.URLClassLoader'

         2018-09-14, ---rgf, - restructured routines bsf.import() and its synonym bsf.importClass() to match
                               arguments with class BSF's class method bsf.import()

         2018-07-22, ---rgf, - removed "unguarded" from AbstractGuiThread, FxGuiThread and AwtGuiThread methods
                               as refactoring removed deadlock potential (attributes are now in subclasses
                               and get fetched with getters if needed in AbstractGuiThread methods)

         2018-07-14, ---rgf, - refactoring the GUI related utility classes "FxGuiThread" and "AwtGuiThread" into
                               a common private abstract class "AbstractGUIThread" defining abstract attribute
                               and methods to be implemented by the refactored subclasses "FxGuiThread" and
                               "AwtGuiThread"

         2018-07-13, ---rgf, - added class "AwtGuiThread" as the awt/swing counterpart to "FxGuiThread" class
                               having the same protocol (same methods and behaviour, just adapted to awt/swing)
                             - corrected routine "BSF.terminateRexxEngine": .routines~rBsf with .routines~xBsf

         2018-06-07, ---rgf, - added method "util.setmethod" to class BSF_REFERENCE to allow setting methods
                               (with "float" scope) to individual instances; needed for decorating UNO.CLS
                               UNO_ENUM instances (adding methods 'toString', 'name' and 'value')

         2018-06-04, ---rgf, - added public routine bsf.iconv public(str,fromCp,toCp) to allow recoding
                               of a Rexx string

         2018-04-03, ---rgf, - adjust for ooRexx 4.1.3 restrictions (development and tests occurred
                               on the *much* better ooRexx 5.0.0)
                               - Array's appendAll is not documented, but operational, however does
                                 not return the receiver: cater for it using cascading messages instead
                               - .debugInput and .traceOutput may not exist

         2018-03-15, ---rgf, - correct type while fetching XBsfCreateRexxProxy() from .routines

         2018-03-12, ---rgf, - bsf.compile(): if the source of a syntax error is related to the supplied
                               Java class name a new syntax condition is raised with an error message hinting
                               at two possible causes to help the programmer; otherwise the raised syntax
                               gets propagated
                             - corrected typo ("java.awt.Taskbar") for Java 9 usage

         2018-03-05, ---rgf, - new public routine "bsf.compile(javaClassName,code)" which returns the
                               Java class object

         2018-02-26, ---rgf, - make INTERPRET-free
                             - removed routine "createJavaLoadStatement"
         2018-02-25, ---rgf, - finished rework of using rBSF/.routines~xBSF and the like
                             - now the class methods and routines bsf.createArrayOf/bsf.createJavaArrayOf
                               behave the same by not proxying .uno_reference objects (if OOo/AOO support
                               is active); reason being that .UNO objects will forward unknown messages
                               to their embedded BSF object peer
                             - tidying up

         2018-02-24, ---rgf, - fetch the most used native routines to have them incorporated into .routines,
                               change INTERPRET to call/callWith messages to those routines, which in total
                               should speed up the bridge quite a bit (approximately at least 10 times)

         2018-02-23, ---rgf, - UNINITs now use the new "BsfUninit4JavaBean(beanName)" function which is
                               able to be executed on an ooRexx created Rexx interpreter that has no
                               connection to a Java RexxAndJava object established
                             - routine "makeBSF4ooRexxLookLikeMacOSX": for Java 9 add Java 9 support
                               to add an icon to the dock; also make sure that the routine will not
                               stop running programs on MacOSX, if a syntax condition arises

         2018-02-20, ---rgf, - added new bsf4rexx-entries "java.version", "java.major.version" and
                               "java.minor.version"

         2017-12-23, ---rgf, - define a public class "Slot.Argument" here instead of native code (BSF4ooRexx.cc
                               and placing the class object in .environment); this class will be used to
                               add useful, but optional information which get added as a trailing (a "slot")
                               argument on callbacks from Java to Rexx objects
                             - relocated .local~bsf4rexx to new class "BSF4REXX" serving as a proxy for the
                               bsf4rexx-directory, making this important directory independent of .local
                             - removed ".local~ooRexx.version", not used anywhere
                             - bsf.import() now saves the class objects in .environment instead of .local
                             - explicitly adding .BSF and .Slot.Argument to .environment in order for FindClass()
                               to find them (in all contexts)

         2017-11-21, ---rgf, - make sure that in .FXGuiThread class "isGuiThread", "runLater" and
                               "runLaterLatest" are unguarded, but "run" is guarded; this avoids
                               hangs on Windows, reported as <https://sourceforge.net/p/oorexx/bugs/1505/>

         2017-11-20, ---rgf, - further testing <https://sourceforge.net/p/oorexx/bugs/1505/> indicates
                               that ungaurding the BSF.OutputStream would probably not fix a three-way-
                               deadlock, which is caused by SAY-debug statements in BSF.CLS in the
                               context of RexxScript usage; this can be remedied by not using SAY-statements
                               in BSF.CLS, but in case of thangs replace them with ".stdout~say(...)"
                               statements

         2017-11-19, ---rgf, - changed BSF.OutputStream: methods are unguarded, using guard on/off, if
                               attribute's values get changed; this inhibits a hang on Windows, where
                               special Windows message handling (for OLE support) may cause nested
                               re-entrent AttachThread() that may yield hangs in that class SAY method,
                               if SAY is used for debug statements

         2017-08-23, ---rgf, - changed logic in of "runLaterLatest" in class "FXGuiThread": before queueing
                               the message all queued messages by the same name to the same target will be
                               removed from the queue; this way only the latest message will be sent

         2017-08-21, ---rgf, - tidying up a little bit
                             - routine bsf.getSlotDir() now checks whether last argument is indeed an instance
                               of .SlotArgument

         2017-08-16, ---rgf, - deprecated routine bsf.ScriptContext2rexx() by renaiming it to bsf.ScriptContext2rexx_deprecated()
                             - removed experimental sentinel named "DOES.NOT.EXIST" from .jsr223

         2017-08-15, ---rgf, - correct error in setting up the "FxGuiThread" class (lazy initialization)

         2017-08-13, ---rgf, - make sure that in class "GUIMessage" the constructor method "init" checks that
                               the supplied message name is indeed of type .string and if arguments are supplied
                               that they are in form of an .array

         2017-08-05 thru 2017-08-11, ---rgf, - created classes "FxGuiThread" and "GUIMessage" (modelled after
                               ooRexx class "Message") to allow dispatching Rexx messages on the JavaFX GUI
                               thread to ease coding for Rexx programmers

         2017-04-30, ---rgf, - make sure that all "d2x" messages have a length of 17 (such that negative
                               values get appropriate leading "F"s)

         2017-03-09, ---rgf, - routine bsf.createProxyClass enhanced with the possibility to add any number
         2017-03-12            of blank delimited Java interface class names after the Java class name that
                               is to be extended (alternatively this can be an array with each item being
                               a string denoting the fully qualified class name or a Java class object);
                               ProxiedJavaClassTool.java got enhanced appropriately
                               (TODO: remove ASM from BSF4ooRexx); now possible to extend an interface
                               class and implement its methods in Rexx without using a java.reflect.Proxy!

         2017-02-13, ---rgf, - finalizing implementation of .JSR223's set() and get() methods

         2017-02-12, ---rgf, - .JSR223' "get" and "set" methods: adjust logic to desired functionality:
                               - honor: attribute names immediately followed by a colon and scope number
                               - if an attribute is in fact a scope number than process the following
                                 attribute names only in that scope (otherwise all scopes are inspected)

         2017-02-11, ---rgf, - made internal ("procedure") routine "escapeQuotes" a routine
                             - methods and routines bsf.createArrayOf and bsf.createJavaArrayOf will
                               now use "escapeQuotes" to escape Rexx strings as INTERPRET would
                               otherwise cause unexpected results

         2017-02-07, ---rgf, - .JSR223::getBindings(): also handle a .context object

         2017-01-20, ---rgf, - renamed operating system name from "PARSE SOURCE opsys ." from "DARWIN" to "MACOSX",
                               this quick fix should keep everything everywhere (in BSF4ooRexx) humming...;
                               TODO: check all BSF4ooRexx-supplied scripts for explicit usage of "MACOSX"

         2016-12-20, ---rgf, - on unregisterBean do not supply rii_id anymore (not needed anymore)

                             - initialize.bsf.dir(): make sure that the pre-registered classes get
                               their refCount increased (for multi Rexx-interpreter scenarios)

         2016-12-12, ---rgf, - make sure we return a BSF/BSF_REFERENCE object unchanged; allowing it
                               to be wrapped up as a BSF_REFERENCE again, may cause runtime errors
                               if the wrong number of uninits get executed (each decreases the refCount
                               on the Java side)

         2016-12-11, ---rgf, - removed public class "BSF_PROXY", which is not really safe to use, if
                               programmers do not know what to do with it; this class was created to
                               ease mixing BSF()-function style programming with ooRexx-style by
                               creating BSF or BSF_REFERENCE proxy objects to which we send the names
                               of the Java messages to invoke;
                               rather, from now on, use the public routine bsf.wrap() to create
                               BSF_REFERENCE proxy objects from Java objects returned from the Java
                               side;

         2016-12-09, ---rgf, - if Rexx started Java the unknown method of BSF if processing sendmessage
                               messages would load its RexxAndJava object, but not garbage collect it;
                               fixed by using bsf.wrap on the returned value

         2016-12-07, ---rgf, - .JSR223::SET method: second argument defaults to the null string ""

         2016-11-30, ---rgf, - make the UNKNOWN methods Unguarded; it is possible that a method invocation
                               on the Java object is taking place, while from the Java side (e.g. for an
                               event handler) a callback occurs (in a different RexxActivation) which from
                               the Rexx side invokes another Java method in the UNKNOWN method, which will
                               block; UNGUARDED solves this problem (and does not introduce a problem into
                               Rexx)

         2016-11-29, ---rgf, - make sure that .UNO_PROXY and .UNO_ARRAY_PROXY are Rexx class objects
                               before using them in isA-tests

         2016-11-19, ---rgf, - updated major version number to 6 to indicate new base level Java 1.6/6

         2016-11-10, ---rgf, - .jsr223: added class methods get and set to process the Rexx script
                               annotations

         2016-11-09, ---rgf, - bsf.redirectTo(): if no monitored objects are supplied the current one
                               gets removed (unless there would be no previous monitored object left),
                               before a default one gets created and assigned

         2016-11-08, ---rgf, - bsf.OutputStream: now also inserts the prefix after LF chars if the string to
                               output contains contains any
                             - bsf.OutputStream::charout() now has an optional third argument, if set to .true,
                               the prefix gets skipped (used by bsf.InputStream::linein)

         2016-11-07, ---rgf, - added ability to output a prefix-string for BSF.InputStream and BSF.OutputStream objects
                             - routine bsf.redirectTo now sets that prefix for  the redirectred standard Rexx files
                               monitored by .input, .output, .error, .debugInput, .traceOutput to
                               "REXXin?>", "REXXout>", "REXXerr>", "REXXdbgIn?>", "REXXtrc>" respectively;
                               in order to turn the redirected prefix on or off just set the "prefix" attribute to .nil,
                               in order to change the string just change the "prefix" attribute

         2016-11-06, ---rgf, if a BSF (Java) object gets a MAKEARRAY or SUPPLIER message sent to it which
                        cannot be resolved, then RexxAndJava.java checks whether it is a collection type
                        object (having one of the interfaces java.lang.Iterable, java.util.Collection,
                        java.util.Enumeration, java.util.Iterator or java.util.Map implemented; or being
                        a class object of type java.lang.Enum such that its values are regarded to be
                        attributes and returned);

         2016-10-27, rgf: - public routine bsf.ScriptContext2rexx now uses .JSR223~ScriptContext2RexxDir(...)

         2016-10-26, rgf: - JSR223-support:
                            - bsf.ScriptContext2rexx():
                              - allow .context argument instead of ScriptContext to ease using this routine
                              - make sure that the names (keys of SimpleBindings) are mapped to valid Rexx symbol names for using BSFContextVariables(...)
                            - new public utility class "JSR223" for easying interfacing with "javax.script" invocations
                              - defines constants "ENGINE_SCOPE", "GLOBAL_SCOPE"
                              - defines class methods getBindings(), normalizedName(), ScriptContext2RexxDir();
                                getAttribute(), getAttributes(), setAttribute()

         2016-08-15, rgf: - BSF.Dialog class: when fetching constants (static fields) use the
                            imported javax.swing.JOptionsPane class

         2015-08-11, rgf: - BSF.WRAP: also honor argument "increase reference counter" for Java array
                            objects (was overlooked!)

         2015-08-04, rgf: - adjust for storing RII_ID in BSF instance to be able to indicate RII_ID to
                            unregisterBean from when uninit runs (important, as BSF registry is always
                            RII dependent)

         2015-07-21, rgf: - bsf.wrap: honor new argument "bIncr", if set to .true, then the reference counter
                            in the BSF registry is increased (needed, if Java object is an argument
                            to RexxEngine.call() [native code: ...jniRexxRunProgram(),
                            ...jniRexxSendMessageToRexxObject(), both will supply .true as a new second arg]
                          - methods and routines "*ArrayOf": changed to automatically create RexxProxy objects
                            for Rexx objects that are neither .String, .BSF nor .nil

         2015-07-07, rgf: - routine "bsf.ScriptContext2rexx": fix bug uncovered by new test cases

         2015-07-05, rgf: - routine "stringToUtf8" deprecated,    created "bsf.stringToUtf8" instead
                          - routine "utf8toString" deprecated,    created "bsf.utf8toString" instead
                          - routine "utf8stringAsJSO" deprecated, created "bsf.utf8stringAsJSO" instead
                          - routine "JSOasUtf8string" deprecated, created "bsf.JSOasUtf8string" instead

         2015-07-01/02, rgf: - added attribute 'isReader' to .BSF.InputStream and 'isWriter' to .BSF.OutputStream
                            to allow testing this attribute in testunits
                          - fix logic in BSF.InputStream, adjust to the fact that a stream uses a byte-array
                            and a reader a char-array for reading
                          - fix bugs uncovered by developing a testgroup for this class
                          - fix bug in "bsf.redirectTo" uncovered by the new testunits, simplify assigning
                            monitors for .debugInput and .traceOutput

         2015-06-07, rgf: - added public routine BSF.ScriptContext2Rexx(ScriptContext[,scopes]) which
                            returns a Rexx directory that contains all ScriptContext values of the
                            given scopes; meant to be used in conjunction with the new external Rexx
                            function BSFContextVariables()

         2015-05-10, rgf: - changed BSF_ARRAY_REFERENCE's MAKEARRAY and AT method such that RexxProxy
                            entries in the Java array get replaced by their contained Rexx object
         2015-05-04, rgf: - corrected minimum ooRexx version to 4.1.0 (determined by BSF4ooRexx.cc)
         2015-05-02, rgf: - make sure in bsf.OutputStream that flush() is used to force the buffered
                            chars to be displayed for Writers and PrintStreams
         2014-10-07, rgf: - JSOasUtf8string: render Java String as UTF-8 encoded Rexx string
                          - utf8stringAsJSO: render UTF-8 Rexx encoded string as a Java String
         2014-10-06, rgf: - utf8toString - handle Javs objects
         2014-10-05, rgf: - now that BSF4ooRexx mandates ooRexx 4 and above, we can reinstate
                            the original directory accessible via the environment variable
                            .bsf4rexx and .b4r, adding an additional alias named .bsf4oorexx;
                            (ooRexx 4.0 and higher will have separate copies of .local per
                            Rexx interpreter instance); in effect this undoes the changes done
                            on 2008-08-28 and 2008-08-31
                          - added the entry "file.encoding" from Java to .bsf4rexx

         2014-10-03/04, rgf: - added methods to existing BSF-methods, adding a trailing "JSO" (Java
                               string object) to them, that will return a reference to a
                               java.lang.String or Netrexx object instead of transparently turning
                               it into a Rexx string; this may be helpful, if wishing to invoke
                               the Java String methods on that (returned) Java string object.

                               These are the added methods:

                               - bsf.invokeJSO
                               - bsf.invokeStrictJSO
                               - bsf.getFieldValueJSO
                               - bsf.getFieldValueStrictJSO
                               - bsf.getStaticValueJSO
                               - bsf.getStaticStrictValueJSO
                               - bsf.getPropertyValueJSO

                               These are the added routines:

                               - bsf.getStaticValueJSO
                               - bsf.getStaticStrictValueJSO
                               - bsf.getConstantJSO

         2014-08-31, rgf: - added routine bsf.extendJavaClass(jClz [, [newClzName] [, [methNames2proxy]]])

         2014-07-16, rgf: - routine bsf.redirectTo(): default .debugInput to .input~current and
                            .traceOutput to .error~current

         2014-07-15, rgf: - added code to BSF.InputStream to also work on java.io.Reader in addition to
                            java.io.InputStream (e.g. java.lang.System.in)

                          - renamed BSF.PrintStream to BSF.OutputStream, added code to also work on
                            java.io.Writer in addition to java.io.PrintStream (e.g. java.lang.System.{out|err})

         2014-07-06, rgf: - added public routine BSF.redirectTo (allows to redirect the ooRexx standard
                            monitors to Java's standard System streams and reset them to the standard
                            ooRexx standard streams)

                          - added public class BSF.InputStream (allows ooRexx to treat Java InputStream
                            objects as Rexx input streams)

                          - added public class BSF.PrintStream (allows ooRexx to treat Java PrintStream
                            objects - specialisation of OutputStream - as Rexx output streams)

         2014-05-17, rgf: - adjust bsf.terminateRexxEngine() to cater for the new behaviour
         2014-05-15, rgf: - moved MacOSX initialization code to public routine "makeBSF4ooRexxLookLikeMacOSX":
                            ooRexx macros in AOO 4.x on MacOSX do not run, as this setup code uses
                            an awt class which must nor run on the main thread on MacOSX. As a result
                            ooRexx macros on AOO 4.x cause a runtime exception and do not run. Moving
                            the initialization into a routine that needs to be explicitly called
                            should solve the problem.

         2013-06-10, rgf: - make BSF.terminateRexxEngine() return .true, if RexxEngine got terminated,
                            .false else
         2012-06-09, rgf: - added public routine BSF.terminateRexxEngine
         2012-06-05, rgf: - added uninit method to BSF_ARRAY_REFERENCE class to remove the
                            array proxy for indexing from the BSF registry, if not needed
                            anymore (i.e. when uninit runs)

         2012-02-11, rgf: - added public routines stringToUtf8(string) and utf8ToString(string)

         2012-02-09, rgf: - supply default values for some USE ARG arguments to inhibit the RXNOVAL exit to
                            be triggered

         2011-02-21, rgf: - define new entries in .bsf4rexx
                             - "OPSYS" ... operating system name from "PARSE SOURCE opsys ." plus
                               "OPSYS1", "OPSYS2", "OPSYS3" with the first one, two, three letters in uppercase
                             - MacOSX: "APPLE_APPLICATION" the Apple application object representing the running Rexx program
                             - "BSF4OOREXX_{016|032|048|064|128|256}": if defined path to PNG-icon
                             - "OOREXX4OOO_{016|032|048|064|128|256}": if defined path to PNG-icon

                          - MacOSX: remove About and Preferences Apple menu entries
                          - MacOSX: define Dock icon
         2011-02-20, rgf: - adding MacOSX related properties
         2010-07-12, rgf: - .bsf.dialog::inputBox: now accepting a Java Object array for options
         2010-06-10, rgf: - changed environment variable from 'BSF4Rexx.JavaStartupOptions' to
                            'BSF4Rexx_JavaStartupOptions'; reason: bash cannot process
                            environment variables that contain a dot

         2010-06-08, rgf: - corrected BsfLoadJava to supply Java startup arguments, if given
                            in the environment variable named 'BSF4Rexx.JavaStartupOptions'

         2010-06-05, rgf: - added check for minimum version of ooRexx being "4.0.1"

         2010-05-11, rgf: - added routine bsf.getSlotDir() which returns the last (slotDir) item

         2010-05-08, rgf: - BSF::UNKNOWN::CLASS - little optimization: forwarding directly to bsf.jco
                          - create[Java]Array[Of] - forward all arguments verbatimely (let Java side optimize)
                          - remove usage of bsf.getJavaClassObjectProxy(...)
                          - removed method bsf.class, instead send "getClass" to Java object
                          - removed class method bsf.getJavaClassObjectProxy
                          - removed class attribute bsf.JavaClassObjectProxies
                          - removed class attribute bsf.JavaClassObjects
                          - added public routine "bsf.importClass" (matching "bsf.loadClass") to
                            supercede "bsf.import"
                          - added class method "bsf.importClass" to class "BSF" (matching
                            "bsf.loadClass") to supercede "bsf.import"

         2010-04-18, rgf: - added public routine bsf.createProxyClass(), adapted .BSF~bsf.import
                            accordingly

         2010-02-20, rgf: - added public routine bsf.isA(jobject, jclass)

         2009-10-19, rgf: - adjust for renaming the library to "BSF4ooRexx"

         2009-10-04, rgf: - added special handling for RexxProxy objects

         2009-09-10, rgf: - added class methods "bsf.createJavaArray" and "bsf.createJavaArrayOf" to
                            class "BSF" (synonyms for "bsf.createArray" and "bsf.createArrayOf")
                          - added public routines "bsf.createJavaArray" and "bsf.createJavaArrayOf"
                            (synonyms for "bsf.createArray" and "bsf.createArrayOf")

         2009-07-30, rgf: - class "BSF": created class method "bsf.createArrayOf"
                          - created public routine "bsf.createArrayOf"

         2009-06-27, rgf: - now raises syntax errors in case too many arguments are supplied for the
                            (class) methods or public routines:

                             - bsf.postEventText
                             - bsf.pollEventText
                             - bsf.import
                             - bsf.getFieldValueStrict
                             - bsf.setRexxNullString
                             - bsf.sleep

         2009-06-25, rgf: - added method "size" to class BSF_ARRAY_REFERENCE

         2009-06-21, rgf: - default BSfShowErrorMessage() to .false
                            (turn off displaying the Java exception message (leftover from the
                            very early BSF4Rexx days))

         2009-06-08, rgf: - removed method 'bsf.addEventListenerReturningEventInfos'
                          - removed routine 'bsf.getEventInfoObject'
                          - removed private class "bsf_array_proxy.assure_BSFRegistry_Removal"

         2009-05-15: - add bsf.wrap as a class method to .BSF; this way it can be assured to be
                       reachable on a callback from the 4.0 API, if .bsf is placed in .local

         2009-04-23: - started to create an ooRexx 4.x specific version of this library,
                       in order to allow for allowing Java proxies to callback into ooRexx
                       by addressing matching Rexx proxy objects

         targeted version number: 400
                    ///////////////////////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
                    \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ ////////////////////////////////////////////////

          2009-04-04, rgf: - added public routine "bsf.haltThread(tid)" and class
                             method for .BSF "bsf.haltThread(tid)"

          2009-02-10, rgf: - bsf.wrap() now caters for unprefixed argument strings that
                             stem from RexxEngine by not raising an exception anymore,
                             but returning the received string unaltered

          2008-09-11, rgf: - boxing a Boolean: only accept the Rexx values:
                             .true, .false, "true" or "false"

          2008-09-09, rgf: - changed "bsfRegister[Strict]" by "new[Strict]" such that
                             error messages can be understood easier (it is self-evident
                             what "new[Strict]" means)

          2008-08-31, rgf: - removed "dir" class attribute from class "BSF"
                           - new "BSF4REXX" public class now behaves as a directory by
                             forwarding all unknown messages to its directory class
                             attribute; created "B4R" public class which acts as a
                             short-hand alias forwarding its unknown messages to
                             the "BSF4REXX" class

          2008-08-28, rgf: - moved ".bsf4rexx" directory object to the class attribute
                             "dir" of class ".BSF" and have ".bsf4rexx" and ".b4r"
                             just point to it; the code in here will be changed to use
                             only the BSF class attribute such that multiple interpreter
                             instances running in the same process do not influence that
                             content unadvertently (.local is shared in ooRexx < 4.0!)
                           - added "line.separator", "file.separator", "path.separator"
                             to .bsf~dir

          2008-07-20, rgf: - added dimensionality check to AT(), PUT(), and PUTSTRICT() in class
                             BSF_ARRAY_REFERENCE to correct possible misuse and as such increase
                             error checking and error reporting

          2008-07-09, rgf: - added public routine "bsf.version()" as pass-thru to BSF()-subfunction
                             "version"

          2008-06-23, rgf: - correcting further bugs related to the big change of yesterday

          2008-06-22, rgf: - found and removed a bug (changing RexxAndJava.java as well) that
                             mixed up strings and beans

          2008-06-16/17, rgf: - activating condition trapping in order to save BSF_ERROR_MESSAGE in the
                             .local directory environment such that it can be retrieved under the
                             environment symbol ".BSF_ERROR_MESSAGE"; before invoking any BSF()-function
                             that entry will be removed from .local

          2007-08-06, rgf: - renamed "BSF" class method "attachNewStrict" to "bsf.attachNewStrict"

          2007-05-23, rgf: - added class method getMessageType to BSF.DIALOG to allow correct
                             processing of supplying first character only

          2007-01-09, rgf: - added ability to supply Java startup options via the operating system
                             environment variable 'BSF4Rexx.JavaStartupOptions'; this allows one
                             to set e.g. the Java heap size etc.; either set the value in the
                             shell or a Rexx program that executes before 'BSF.CLS' (e.g. by
                             requiring such a Rexx program *before* requiring 'BSF.CLS'); the
                             public routine "parseJavaOptions(stringOfJavaStartupOptions)" returns
                             an array of parsed options, "createJavaLoadStatement(optionArray)"
                             creates the BsfLoadJava-statement supplying each option as an own
                             invocation argument (ooRexx 3.x cannot yet handle array arguments)

          2006-12-01, rgf: - added access to pre-registered java.lang.Thread class

          2006-11-29, rgf: - corrected a bug in method bsf.import, now class object is saved
                             in the private class attribute bsf.jco

          2006-10-17, rgf: - renamed method "dispatch" to "bsf.dispatch" to avoid name-clashes
                             with Java methods named "DISPATCH"

          2006-08-01, rgf: - BSF.WRAP() adjusted to process beanNames that have two
                             hexadecimal values after '@', delimited by '_': this has
                             been added to 'RexxAndJava.java' to handle constructors
                             for which Sun's Java prodcues identical (!) hashCode() values!

          2006-07-22, rgf: - cleaned program of commented code

          2006-07-11, rgf: - added method DISPATCH (as in OLEObject starting with ooRexx 3.1),
                             will be forwarded to method BSF.INVOKE

          2006-07-04, rgf: - added method "ENTRY" to "JavaStaticFields.Directory"

          2006-03-22, rgf: - added methods "[]", "AT", "[]=", "PUT" and making directory cache for retrieval
                             optionally to "JavaStaticFields.Directory"

          2006-03-21, rgf: - "JavaStaticFields.Directory:unknown": made sure that cached value is returned,
                             instead of looking up the Java side constantly

          2006-03-20, rgf: - removed a bug from "JavaStaticFields.Directory" reported by Lee Peedin
                             (Java class name was stored in an ooRexx class attribute instead of an
                             instance attribute); changed logic a bit, now that the Java calss name is
                             handled by an instance attribute (no need for dynamically setting the
                             unknown method)

          2006-03-08, rgf: - .bsf~bsf.import(): now allows storing references to already created
                             ooRexx proxy class objects under different names in .local

          2006-03-03, rgf: - fixed BSF's class method and also the public routine named "bsf.postEventText"
                             to use the optional priority value (0=low, 1=normal=default, 2=high)

          2006-03-02, rgf: - added routine "bsf.import(...)", a wrapper for ".bsf~bsf.import(...)"

          2006-02-24, rgf: - added method 'bsf.addEventListenerReturningEventInfos', routine
                             bsf.getEventInfoObject, private class "bsf_array_proxy.assure_BSFRegistry_Removal"
                             which makes sure that the eventObject gets removed from the BSFRegistry by
                             repeating "unregisterBean" until the Java object is removed from the registry

                           - added routine "bsf.getEventInfoObject" which must be called exactly once on
                             each eventText returned because of employing
                             'bsf.addEventListenerReturningEventInfos'; this will create a BSF proxy object
                             and takes care that the eventObject gets deregistered from the BSF registry

          2006-02-05, rgf: - allowing assignments to static Fields if wrapped with 'bsf.wrapStaticFields'

          2006-02-02, rgf: - added routine 'bsf.wrapStaticFields' which returns an instance
                             of "JavaStaticFields.Directory", which allows easy access to the
                             static fields of the indicated Java class/interface by merely
                             sending the names of the static fields or using the AT method
                             to access them

          2006-01-06, rgf: - now fixed bsf.import to use the routine bsf.loadClass(), changed the
                             code to use the new class method bsf.setBSF4RexxInfos to store the
                             Java class object proxy and save the fully qualified Java class name;
                             added public routines pp() and iif()

          2006-01-05, rgf: - now using the much better BSF-subfunction "loadClass" instead of the
                             bad variant "Class~forName()", added class method "bsf.loadClass" to "BSF",
                             and routine "bsf.loadClass()"

          2005-08-30, rgf: - now also assigning the beanName (index into BSF registry) as objectName
                             for Java class objects; this way no special consideration has to be
                             applied if JCOs are used as arguments; the ooRexx classes reveal the
                             fully qualified Java class name either via the defaultName-method, via
                             the class attribute "bsf.FullJavaClassName" or by sending them the
                             "toString()" message;
                             license change to Apache v2.0 and CPLv1.0

          2005-07-30, rgf: - fixed a bug in creation of array: now pre-registered names (entries in .bsf4rexx)
                             and any other Java class object proxy can be used to indicate component type for the
                             Java array

          2005-07-10, rgf: - added public routines bsf.lookupBean(), bsf.unregisterBean(),
                             bsf.pollEventText(), bsf.postEventText(), bsf.getStaticValue(),
                             bsf.getStaticValueStrict() and synonym bsf.getConstant(),
                             bsf.createArray(): makes it easier to code than the
                             versions ".bsf~bsf.xyz()"

          2005-07-06, rgf: - renamed "bsf.checkResult" into "bsf.wrap"

          2005-07-04, rgf: - corrected bug in MAKEARRAY: now the individual elements are wrapped
                             in BSF_REFERENCE, if they represent Java objects

          2005-06-07, rgf: - renamed "bsf.box" to "box" and "bsf.unbox" to "unbox"

          2005-06-06, rgf: - bsf.import changed: if second argument is .nil or the empty string,
                             then - and only then - there will be no entry in .local; if 2nd
                             argument is omitted, then the Java class name is used as the name
                             on ooRexx

          2005-06-05, rgf: - renamed BSF-pass through class methods to begin with "BSF.",
                           - using pre-registered Java-class proxies for box() and unbox(),
                           changed bsf.import logic to allow for dynamic allocating (if a
                           Java object proxy needs to access its Java class object):
                           - added instance method "bsf.class" to return the Java class object
                           proxy, which allows accessing the static methods and fields as well;


          2005-05-07, rgf: added bsf.getFieldValueStrict (strict case!),
                      methods having a trailing "=" are interpreted as setter methods, causing
                      the "setFieldValue" subfunction to be invoked using the method name without
                      the trailing equal sign as the name of the field

          2005-05-02, rgf, imported the primitive type wrapper classes into ooRexx
                           added box() and unbox(),
                           removed .box.true and .box.false (boxed as Booleans), -- 2005-05-03, rgf

          2004-03-04, rgf, test if BSFLoadJava() is registered before invoking it
          2004-02-22, rgf, trap exception on registering external Rexx-functions; needed
                      for Tobias Specht for debugging BWS on Konqueror;
                      partially removing a reported bug by TS (ORX not able handling literals
                      larger than 250 chars in INTERPRET)

          2003-11-19, rgf, added "RAISE PROPAGATE" statements in order for Rexx programs
                      being able to intercept exceptions which may occur in programs using
                      the proxy classes of this file

          2003-09-05, rgf, added "newStrict" floating constructor method for imported
                      Java classes and "addNewStrict" class method to attach it to
                      Object Rexx proxy classes by the "import" class method (i.e. setting
                      up an Object Rexx class as a proxy), to allow using strictly typed
                      arguments at instantiation time

          2003-06-01, rgf, added Apache license

          2003-05-10, rgf, changed "_new" to "Strict", changed arrays to never
                      use strict typing (not necessary as type can be inferred
                      from array itself)

          2003-05-07, rgf, using the new "invoke_new", which allows for not
                      using type information with the arguments

          2003-03-10, rgf, changed ".bsf.cls" to ".bsf4rexx" and ".JC" to ".class"
          2003-02-09, rgf, no dimensional limits on arrays anymore,
                           makearray(), supplier() now created at the Java side, merely re-using

          2003-01-29, Augsburg-->Vienna (train), adding direct JVM-loading support,
                      BSF_PROXY, .BSF.cls (for accessing pre-registered Java class objects)

          2003-01-28, Augsburg, renamed "BSF_ARRAY" to "BSF_ARRAY_PROXY" to denote its purpose
          2001-04-22, 2001-05-02 (on the airplane, returning home
                                  from the 12th International Rexx Symposium),
          2001-05-07 (fixed bug in bsf:init, if directly used to instantiate data)
          2001-05-26 (made method "bsf.exit" available at the instance level)
          2001-06-02 (BSF: made method "bsf.sleep" available at the instance level and
                      as "sleep" at the class level;
                      BSF_ARRAY_PROXY: MAKEARRAY and SUPPLIER now only return entries with
                                 values as does Object Rexx with native arrays)

   version:     6.41 (20210205)  -- "6" -> Java minimum level "1.6/6.0", 41 -> ooRexx minimum level "4.1"

  If this program is part of a distribution of an Apache foundation project (e.g. BSF,
  the Bean Scripting Framework), then the immediately following Apache foundation license
  applies, if it gets distributed via the "http://www.ooRexx.org" project , then the
  CPL 1.0 license which follows thereafter.


------------------------ Apache Version 2.0 license -------------------------
   Copyright 2001-2021 Rony G. Flatscher

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-----------------------------------------------------------------------------



------------------------ Common Public License 1.0 --------------------------
   Copyright 2001-2021 Rony G. Flatscher

   This program and the accompanying materials are made available under the
   terms of the Common Public License v1.0 which accompanies this distribution.

   It may also be viewed at: http://www.opensource.org/licenses/cpl1.0.php
-----------------------------------------------------------------------------

*/

/*
   -- last change:  - Revision: 194  - Author: rony  - Date: 2007-02-01 00:22:00 +0100 (Thu, 01 Feb 2007)
   .local~revision.rev   ="$Rev: 927 $"
   .local~revision.date  ="$Date: 2010-12-26 18:34:19 +0100 (Sun, 26 Dec 2010) $"
   .local~revision.author="$Author: Administrator $"
   .local~revision.url   ="$URL: file:///cygdrive/e/svnrgf/bsf4rexx/trunk/BSF4ooRexx.dev/bin/BSF.CLS $"
   .local~revision.id    ="$Id: BSF.CLS 927 2010-12-26 17:34:19Z Administrator $"
*/

bDebug=.false -- .true

if bDebug=.true then
do
   parse source s
   .stderr~say("BSF.CLS - arrived in PROLOG code ... BSF.CLS - arrived in PROLOG code ... BSF.CLS - arrived in PROLOG code ...")
   .stderr~say("BSF.CLS - parse source:" pp(s) "- editing it for new release, since Monday, 2018-02-19...")
   .stderr~say("... BSF.CLS ... prolog code ..."  ".bsf.rii_ID:" pp(.bsf.rii_ID)  ".bsf.primodalTID="pp(.bsf.primodalTID)  "TID:" pp(BsfGetTid()) "created in RII:" pp(rii_id))
end

   signal on syntax
   parse version "_" major '.' minor '.' revision "(";   -- extract version parts
   rexxVersion=major"."minor  -- create major/minor version number, ignore revisions

   minimumVersion="4.1"     -- minimum ooRexx version (same as BSF4ooRexx.cc)
   if rexxVersion < minimumVersion then
   do
       parse source s . name  -- extract name of this module
       errString=name": needs ooRexx" pp(minimumVersion) "or higher, current version:" pp(rexxVersion)
       .error~say(errString", aborting...")
       raise syntax 3.900 array (errString)
   end

   parse source opsys . thisFullPath      -- get name of operating system, rgf, 2010-02-20

   if opsys="DARWIN" then opsys="MACOSX"

   if BsfInvokedBy()=0 then   -- JVM not yet loaded
   do
         -- process Java runtime args, if any
      jso=value('BSF4Rexx_JavaStartupOptions', ,"ENVIRONMENT")

      if opsys="MACOSX" then  -- as of 2010-02-20: if loading Java via Rexx, awt-event thread does not work
         jso=(jso "-Djava.awt.headless=true")~strip   -- make sure that Java hints at the cause of the problem

      arr=parseJavaOptions(jso)  -- will also add -Djava.class.path with exploded CLASSPATH (exploding asterisks '*' if any)

      .routines["XBSFLOADJAVA"]~callWith(arr)
   end

   -- since 2008-06-22 (rgf) this forces RexxAndJava to prepend type indicators!
   .routines["XBSF"]~call("bsfPrefixReturnValue", .true) -- now "<O>" (object) or "<S>" (string) get prepended to the returned value

   -------------------------------------------------------------------------------------------------

      -- rgf, 2015-08-02: experimental: save this Rexx RII_ID in .local (usable from any Rexx object);
      --                  make sure that this function is called after Java got loaded (if Rexx had to load Java)
   if \.local~hasEntry("BSF.RII_ID") then
   do
      .local~bsf.rii_id=.routines~xBsf~call('get_rii_id') ~substr(4)
-- .stderr~say("BSF::prolog - after setting BSF.RII_ID in .local:" .bsf.rii_id)
   end

   if initialize.bsf.dir() then     -- setup .bsfrexx
   do
      .bsf4rexx~version="641.20220131" -- set version (date distribution got created)
      .bsf4rexx~opSys  =opsys          -- save operating system name as supplied by Rexx' PARSE SOURCE
      .bsf4rexx~opSys1 =opsys~left(1)~upper  -- save operating system name's initial letter in uppercase
      .bsf4rexx~opSys2 =opsys~left(2)~upper  -- save operating system name's first two letters in uppercase
      .bsf4rexx~opSys3 =opsys~left(3)~upper  -- save operating system name's first three letters in uppercase

      .bsf4rexx~rexx.version=rexxVersion  -- make Rexx version, e.g. "4.1", "5.0" etc available to the clients

      .bsf4rexx~bsf.cls.fullPath=thisFullPath      -- allows to query the full path for this BSF.CLS from other Rexx programs
      .bsf4rexx~bsf.cls.location=filespec("l",thisFullPath) -- allows to query the location for this BSF.CLS from other Rexx programs

      .bsf.dialog~setDefaultComponent  -- Initialize the default component object (cannot be done in class' constructor)

      .environment~bsf=.bsf            -- moved from .local to .environment in order for FindClass() to find it
      .environment~slot.argument=.slot.argument -- putting in .environment to allow FindClass() to find it

      call getAndSetPathToIcons
   end

   call create_display.version_entry      -- create the display version entry .bsf4rexx~display.version

   call BSfShowErrorMessage .false  -- turn off displaying the Java exception message (leftover from the very early BSF4Rexx days)

-- numeric digits 20 -- otherwise d2x may cause an error which is swallowed in destructors (uninit methods), but ends them
 -- .stderr~say("BSF.CLS - about to leave the PROLOG code, tid="pp(bsfGetTID()) ".bsf.rii_ID="pp(.bsf.rii_ID) ".bsf.primodalTID="pp(.bsf.primodalTID)  ".local~identityHash~d2x="pp(.local~identityHash~d2x(16)) ".bsf~identityhash~d2x="pp(.bsf~identityhash~d2x(16)) "<=== "~copies(5))
   return

syntax:
   raise propagate




-- ::requires "rgf_util2.rex"    -- 20180106, rgf, temporarily because of BSF's unknown usage

::requires "BSF4ooRexx" library


/* ================================================================================================= */
/* ================================================================================================= */
/* 2018-02-14, rgf: incorporate the native routines in the local .routines directory and invoke the routines
 *                  using call and callWith messages as this is about ten times faster according to tests
*/
::routine xBSF                   PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BSF                 "
::routine xBsfCreateRexxProxy    PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfCreateRexxProxy  "
::routine xBsfRexxProxy          PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfRexxProxy        "
::routine xBsfUninit4JavaBean    PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfUninit4JavaBean  "
::routine xBsfLoadJava           PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfLoadJava         "

/*
rBSF rBsfUninit4JavaBean rBsfRexxProxy

::routine xBsfRawBytes           PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfRawBytes         "
::routine xBsfContextVariables   PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfContextVariables "
::routine xBsfGetRIID            PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfGetRIID          "
::routine xBsfGetRTC             PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfGetRTC           "
::routine xBsfGetTID             PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfGetTID           "
::routine xBsfTestPing           PUBLIC   EXTERNAL "LIBRARY BSF4ooRexx BsfTestPing         "
*/




/* ================================================================================================= */
   /* ooRexx macros in AOO 4.x on MacOSX do not run, as this setup code uses an awt class which
      must nor run on the main thread on MacOSX. As a result ooRexx macros on AOO 4.x cause a
      runtime exception and do not run. Moving the initialization into a routine that needs to
      be explicitly called should solve the problem.
   */
::routine makeBSF4ooRexxLookLikeMacOSX public

   if .bsf4rexx~opsys<>"MACOSX" then return  -- this is for Macs only

   signal on syntax        -- make sure we do not stop MacOSX apps

   if .bsf4rexx~java.major.version<9 then
   do
      .java.lang.system~setProperty('apple.laf.useScreenMenuBar','true') -- just used on Macs: use Apple's menu dock, for JFrame-menus insteadof the window's menu bar
           -- by default hide "About" and "Preferences" menu in Apple's menu bar
      app=bsf.loadClass("com.apple.eawt.Application")~getApplication
      .bsf4rexx~apple_application=app   -- get and save Apple's application object to ease use in dependent programs
      app~removeAboutMenuItem
      app~removePreferencesMenuItem
      if .bsf4rexx~hasEntry("bsf4oorexx_256") then
         app~setDockIconImage(.bsf~new("javax.swing.ImageIcon",.bsf4rexx~bsf4oorexx_256)~getImage)
   end

-- TODO: testing, also remove menu items?
      -- starting with Java 9 (fall 2017) the java.awt.Taskbar and java.awt.Desktop classes have (new) support
      -- that works on MacOSX as well
   else
   do
      taskbar=bsf.loadClass("java.awt.Taskbar")~getTaskbar
      .bsf4rexx~apple_application=taskbar
      if .bsf4rexx~hasEntry("bsf4oorexx_256") then
         taskbar~setIconImage(.bsf~new("javax.swing.ImageIcon",.bsf4rexx~bsf4oorexx_256)~getImage)
   end
   return

syntax:
   co=condition("o") -- get condition object
   .error~say("BSF.CLS, line" .line": MacOSX support attempting to add dock icon caused an exeption:" pp(co~message))



/* ================================================================================================= */
::routine getAndSetPathToIcons	-- rgf, 2011-02-21: find path to icons, if found save icons
  opsys=.bsf4rexx~opsys~left(1)     -- get first letter of operating system (L=Linux, M=MacOSX, W=Windows, ...)

  parse source opsys +1 1 . kind path2this

  if opsys="W" then
  do
     paths=.array~of(filespec("Location", path2this)"install\images\linux\hicolor\")
  end
  else
  do
     paths=.array~of(filespec("Location", path2this)"install/images/linux/hicolor/", -  -- look in directory of this program (may be linked to somewhere else)
                     "/opt/BSF4ooRexx/install/images/linux/hicolor/",                -  -- fallback location
                     "/Applications/BSF4ooRexx.app/Current/Resources/",              -  -- hypothetic location on MacOSX (may need adjustment)
                     "/Users/rony/bsf4oorexx.201012/root201012/trunk/bsf4oorexx.dev/images/linux/hicolor/" - -- TODO: Rony's developer path (to be removed)
                     )
  end

  path2icon=""
  do entry over paths
     res=SysFileExists(entry)
     if res=1 then
     do
        path2icon=qualify(entry)
--        say "... BSF.CLS::getAndSetPathToIcons (debug): - path2icon found! ["path2icon"] ..."
     end
  end
  if path2icon="" then return     -- no path found

  path2icon=path2icon -- file separator already appended! || .bsf4rexx~file.separator   -- append file separator

	-- create entries to exisiting icon files in the given resolutions
  imageNames=.array~of("bsf4oorexx", "oorexx4ooo")
  do res over .array~of("016", "032", "064", "048", "128", "256")
     do name over imageNames
        tmpName=name"_"res
        fullPath=path2icon || tmpName".png"
        if SysFileExists(fullPath) then
        do
           .bsf4rexx~setEntry(tmpName, fullPath)
--           say "... BSF.CLS::getAndSetPathToIcons (debug):" tmpName "->" fullPath "..."
        end
     end
  end

  return





/* ================================================================================================= */
/* Initialize BSF.DIR which is stored with the BSF class object, such that
   each interpreter instance is separated from each other w.r.t. the objects
   stored in there.
*/
/* Starting with 2014-10-05 we again make the .bsf4rexx directory available via .local,
   adding the aliases, "b4r" and "bsf4oorexx" for it.
*/
/* Starting with 2017-12-23 we introduce the public class "BSF4REXX" again, serving
   as a proxy for the "bsf4rexx" directory which is located in .local; unfortunately,
   in a multi-Rexx-engine environment a Rexx object might execute in another Rexx
   interpreter instance (RII) where a different .local is then used, which might not
   have the entry! This solution will work independently of RII.
*/
::routine initialize.BSF.dir
  if .bsf4rexx~bsf.dir~isA(.directory) then return .false -- already set up!

  -- bsf.dir=.bsf4rexx~dir        -- get .BSF's directory object
  bsf.dir=.directory~new      -- create a directory for BSF4ooRexx programs
  .bsf4rexx~bsf.dir=bsf.dir   -- assign the directory object to the class attribute "bsf.dir"

/*
  .local~bsf4rexx  =bsf.dir   -- create an entry in .local for it

      -- TODO: rgf, 20171223: check whether assumption holds, if not supply two classes modelled after class "BSF4REXX"
      -- assume that no one is using the following two entries

  .local~b4r       =bsf.dir   -- create an entry in .local for it
  .local~bsf4oorexx=bsf.dir   -- create an entry in .local for it
*/
   /*
      pre-registered classes (in the BSF-registry) to make it easier to
      indicate the most important Java class-objects from the ooRexx side;

      just send the Java datatype name (bool, byte, char, int, long,
      float, double, or the Java class names representing the primitive datatypes
      as objects: boolean.class, byte.class, character.class, integer.class,
      long.class, float.class, double.class; in addtion the important Java class
      object names: class.class, object.class, method.class, array.class,
      string.class, system.class) to the directory ".bsf4rexx" and use the
      returned object as argument for the Java side
   */

   arr=.array~of( ,
      "Class.class",      'Class.class'        ,,   -- a Java Object class
      "Object.class",     'Object.class'       ,,   -- a Java Object class
      "Method.class",     'Method.class'       ,,   -- a Java Object class
       ,
      "Array.class",      'Array.class'        ,,   -- a Java Object class
      "String.class",     'String.class'       ,,   -- a Java Object class
      "System.class",     'System.class'       ,,   -- a Java Object class
      "Thread.class",     'Thread.class'       ,,   -- a Java Object class
       ,
      "boolean",          'boolean.class'      ,,   -- primitive
      "boolean.class",    'Boolean.class'      ,,   -- a Java Object class
      "byte",             'byte.class'         ,,   -- primitive
      "byte.class",       'Byte.class'         ,,   -- a Java Object class
      "char",             'char.class'         ,,   -- primitive
      "Character.class" , 'Character.class'    ,,   -- a Java Object class
      "double",           'double.class'       ,,   -- primitive
      "Double.class",     'Double.class'       ,,   -- a Java Object class
      "float",            'float.class'        ,,   -- primitive
      "Float.class",      'Float.class'        ,,   -- a Java Object class
      "int",              'int.class'          ,,   -- primitive
      "Integer.class",    'Integer.class'      ,,   -- a Java Object class
      "long",             'long.class'         ,,   -- primitive
      "Long.class",       'Long.class'         ,,   -- a Java Object class
      "short",            'short.class'        ,,   -- primitive
      "Short.class",      'Short.class'        ,,   -- a Java Object class
      "void",             'void.class'         ,,   -- primitive
      "Void.class",       'Void.class'         )    -- a Java Object class

   do i=1 to arr~items by 2    -- pre-registered classes (see above)
     bsf.dir~setentry(arr[i], .bsf_reference~new(arr[i+1], .true))
   end

   -- 20190201, rgf: abstract Java Object classes cannot be bsf.import(ed) anymore (they cannot be instantiated)
   --                hence use bsf.locadClass to load and save them
   arr=.array~of( 'java.lang.ClassLoader' )  -- an abstract Java Object class, for supporting routine bsf.contextClassLoader
   do jClassName over arr
      call bsf.loadClass jClassName, jClassName -- save in environment under the same name
   end

      -- Java classes for "String" and those representing the primitive datatypes
      -- creates ooRexx proxy classes
   arr=.array~of( ,
      'java.lang.String'            ,,   -- a Java Object class
      'java.lang.Boolean'           ,,   -- a Java Object class
      'java.lang.Byte'              ,,   -- a Java Object class
      'java.lang.Character'         ,,   -- a Java Object class
      'java.lang.Double'            ,,   -- a Java Object class
      'java.lang.Float'             ,,   -- a Java Object class
      'java.lang.Integer'           ,,   -- a Java Object class
      'java.lang.Long'              ,,   -- a Java Object class
      'java.lang.Short'             ,,   -- a Java Object class
      'java.lang.System'            ,,   -- a Java Object class --      'java.lang.ClassLoader'       ,,   -- a Java Object class, for supporting routine bsf.contextClassLoader
      'java.lang.Thread'            ,,   -- a Java Object class, for supporting routine bsf.contextClassLoader
      'java.io.File'                ,,   -- a Java Object class, for supporting routine bsf.contextClassLoader
      'java.lang.reflect.Modifier'  ,,   --
      'java.net.URL'                ,,   -- a Java Object class, for supporting routine bsf.contextClassLoader
      'java.net.URLClassLoader'     )    -- a Java Object class, for supporting routine bsf.contextClassLoader

   do i=1 to arr~items
      -- make these classes accessible as if they were ooRexx classes
      jClassName=arr[i]
      bsf.dir~setEntry(jClassName, .bsf~bsf.import(jClassName)) -- will also place in .environment (instead of .local since 20171223)
   end


   -- add Boolean.TRUE and Boolean.FALSE to help speed up lambda invocations (e.g. if it is clear that the apply() function should return a Boolean)
   bsf.dir~boolean.true =.java.lang.Boolean~new("true")  -- this will save the Boolean object
   bsf.dir~boolean.false=.java.lang.Boolean~new("false") -- this will save the Boolean object

   bsf.dir~setEntry("REXXSTRICTARGUMENT", .bsf~bsf.import("org.rexxla.bsf.engines.rexx.RexxStrictArgument",.nil)) -- rgf, 2018-11-01


   do key over .list~of("line.separator", "file.separator", "path.separator", "file.encoding")
      bsf.dir~setEntry(key,bsf.dir~java.lang.System~getProperty(key))
   end

  -- 2018-02-20, rgf: add "java.version",  "java.major.version" and "java.minor.version"
  javaVersion=bsf.dir~entry("java.lang.System")~getProperty("java.version")
  bsf.dir~setEntry("java.version",javaVersion)

  -- make sure this gets run only on Java 1.8/8 or higher; up to Java 9 "java.version" returned 1.6, 1.7 or 1.8, then 9.0

  -- 20210822: beta or early access version of OpenJDK may return a version string like "18-ea"
  filterChars="012345678."
  tmpJavaVersion=javaVersion~translate(filterChars, filterChars||xrange("00"x,"ff"x))

  parse var tmpJavaVersion major "." minor "." rest
  if \datatype(major,"whole") then  -- some weird non-whole version string (ea, beta, ...), use the specification version
  do     -- try to use the java.specification.version value instead
     tmpJavaVersion=bsf.dir~entry("java.lang.System")~getProperty("java.specification.version") -- would not contain "ea" etc.
     parse var tmpJavaVersion major "." minor "." rest
  end

  if major=1 then          -- 1.6, 1.7, 1.8? shift
  do
      major=minor
      minor=rest
  end
  bsf.dir~setEntry("java.major.version", major)
  bsf.dir~setEntry("java.minor.version", minor )   -- minor can be the empty string if java.version is "18-ea" or the like

--  say .line": BSF.CLS - java.version="pp(bsf.dir~java.version) "java.major.version="pp(bsf.dir~java.major.version) "java.minor.version="pp(bsf.dir~java.minor.version)

  return .true    -- indicate we created and populated .bsf4rexx


/* ================================================================================================= */
-- box primitive datatypes into their Java classes counterparts
-- BOolean, BYte, C, D, F, I, L, SHort, STring

::routine box  public
   parse upper arg type +2, val

   signal on syntax
   select
     when abbrev(type, "BO") then
          do
             if      val=.true  | val~caselessEquals("true")  then val="true"
             else if val=.false | val~caselessEquals("false") then val="false"
             else    -- raise a syntax error, in this case we expect a Boolean value!
                raise syntax 88.900 array ("BSF.CLS - BOX('Boolean', string): argument 'string' does not represent an acceptable Boolean value. Acceptable values are: '0', '1', '.true', '.false', and in addition 'true', 'false'. Received: '"val"'")

             return .java.lang.Boolean~new(val)
          end

     when abbrev(type, "BY") then return .java.lang.Byte~new(val)

     when abbrev(type, "C")  then return .java.lang.Character~new(arg(2)) -- use val verbatimly (retain mixed case)
     when abbrev(type, "D")  then return .java.lang.Double~new(val)

     when abbrev(type, "F")  then return .java.lang.Float~new(val)
     when abbrev(type, "I")  then return .java.lang.Integer~new(val)
     when abbrev(type, "L")  then return .java.lang.Long~new(val)
     when abbrev(type, "SH") then return .java.lang.Short~new(val)
     when abbrev(type, "ST") then return .java.lang.String~new(arg(2))    -- use val verbatimly (retain mixed case)

     otherwise return arg(2)
   end

syntax:
   raise propagate


/* ================================================================================================= */

/*
   This routine uses the supplied arguments to create and return a Java RexxStrictArgument which can be used
   as an argument when invoking methods or constructors. BSF4ooRexx will then only accept field, method or
   constructor candidates that expect exactly the class object supplied in the returned RexxStrictArgument.
   <br>
   usage:  <code>box.strictArg([type=.nil], value [, bPrimitive=.true])</code>

   @param type denotes the exact class object the argument must have in a signature,
               in order for the candidate field, method or constructor to qualify:
               <ul>
                  <li>if <code>.nil</code> (default), then value's class object gets strictly used

                  <li>if a string: one of the Rexx type indicator values: 'BO', 'BY', 'C', 'D', 'F', 'I', 'L', 'O', 'SH', ST'

                  <li>if a Java class object: must be assignable from value
               </ul>

  @param value a Java object, or a string value that is used to create the wrapper object

  @param bPrimitive optional, determines if a primitive value's class object should be the
                    primitive class object (<code>.true</code>) or the primitive's wrapper
                    class object (<code>.false</code)

   @author Rony G. Flatscher
   @since 2018-11-02
*/

::routine box.strictArg  public      -- new version using RexxStrictArgument, rgf, 2018-11-01
  use strict arg oType=.nil, oVal, bPrimitive=.true

  signal on syntax
  if oType=.nil then       -- use oVal's class strictly
  do
      if oVal=.nil then
          raise syntax 88.900 array ("BSF.CLS - box.strictArg(): if 'type' is [.nil], the argument 'val' must have a value different from [.nil]")

      return .bsf4rexx~RexxStrictArgument~new(oVal)      -- use oVal's type as strict class object
  end

  if oType~isA(.string) then  -- if a string then a type indicator is expected
  do
      if arg(2,'Omitted') then
         raise syntax 88.900 array ("BSF.CLS - box.strictArg(): argument 'type' ("oType") is a string that is considered a type indicator, hence argument 'val' must not be omitted")

      parse upper arg type +2
      val=oVal       -- save unedited version (case preserving for Byte, Character and String)

      signal on syntax
      select
        when abbrev(type, "BO") then
             do
                if      val=.true  | val~caselessEquals("true")  then val="true"
                else if val=.false | val~caselessEquals("false") then val="false"
                else    -- raise a syntax error, in this case we expect a Boolean value!
                do
                   raise syntax 88.900 array ("BSF.CLS - box.strictArg(): 'type' is [Boolean], but argument 'val' with value ["oVal"] is not a Boolean value. Acceptable values are: '0', '1', '.true', '.false', and in addition 'true', 'false'.")
                end

                return bsf.wrap(bsf('strictArg', type,val,bPrimitive))
             end

        when abbrev(type, "BY") then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))

        when abbrev(type, "C")  then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))   -- use val verbatimly (retain mixed case)
        when abbrev(type, "D")  then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))

        when abbrev(type, "F")  then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))
        when abbrev(type, "I")  then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))
        when abbrev(type, "L")  then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))

        when abbrev(type, "O" ) then return .bsf4rexx~RexxStrictArgument~new(oVal) -- use the object's type as strict class object

        when abbrev(type, "SH") then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))
        when abbrev(type, "ST") then return bsf.wrap(bsf('strictArg', type,val,bPrimitive))   -- use val verbatimly (retain mixed case)


        otherwise          -- unknown type
           raise syntax 88.900 array ("BSF.CLS - box.strictArg(): 'type' string value ["oType"] is not one of the Rexx type indicators: 'BO', 'BY', 'C', 'D', 'F', 'I', 'L', 'O', 'SH', ST'")
      end
  end

   -- if oVal is .nil, then it will be strictly used for oType class objects in a signature
  return .bsf4rexx~RexxStrictArgument~new(oType,oVal)   -- will force the value to be used only if oType is matched exactly in the candidates


syntax:
   raise propagate



/* ================================================================================================= */
-- unbox primitive datatype from their Java class counterpart, where they are embedded
-- BOolean, BYte, C, D, F, I, L, SHort, STring
-- new, rgf, 20181101: stay compatbile if Wrapper class objects are supplied
--                     process new RexxStrictArgument objects
::routine unbox public
   use arg o

   if o~isA(.bsf) then           -- if a Java proxy
   do
      objName=o~objectName    -- name will contain the fully qualified Java class name
      if pos("RexxStrictArgument@",objName)>0 then -- since 20181101
         return o~getValue4Rexx

      tmp=bsf.wrap(bsf("unbox",o))
      if tmp=.nil then return o  -- not a boxed value return submitted value unchanged
      return tmp                 -- return unboxed value
   end
   return o                      -- not a boxed value return submitted value unchanged


::routine unbox_ori public
   use arg o

   objName=o~objectName    -- name will contain the fully qualified Java class name
   if pos("RexxStrictArgument@",objName)>0 then -- since 20181101
      return o~getValue4Rexx

   if \o~isA(.bsf) then
   do
      if o~isA(.string) then return o  -- if a plain Rexx string supplied, return it unchanged
      raise syntax 88.900 array ("BSF.CLS - UNBOX(): argument ["o"] is not a Java object nor a plain Rexx string")
   end

   -- for backward compatibility
     -- extract class name from proxy's objectName (e.g. "java.lang.String@a3b0cdef")
   parse upper value objName with . "." . "." className "@" .

     -- if not a wrapper class return object unchanged
   if pos(className, "BOOLEAN BYTE CHARACTER DOUBLE FLOAT INTEGER LONG SHORT STRING")=0 then return o

     -- toString() would return instead of "1"/"0" the values "true"/"false"
   if abbrev(className, "BO")  then return o~booleanValue

   return o~toString -- Java string representation suffices for ooRexx




/* ================================================================================================= */
::routine  escapeQuotes
     return '"'||changestr('"', arg(1), '""')||'"'

/* ================================================================================================= */
/*
   parse Java startup options which can be given via the operating system environment
   variable named 'BSF4Rexx.JavaStartupOptions'; returns an array with each option
   stored in its own slot
*/
::routine parseJavaOptions public
  parse arg options
  a=.array~new
  i=0
  deli=" '" || '"'         -- delimiters for options/option-values
  cpPropertyNeedle="-Djava.class.path="  -- rgf, 2021-02-16
  cpPropertySeen? =.false

  do mainLoop=0 while options<>""
     pos=pos("-", options)
     if pos=0 then         -- no more options available?
     do
        if i=0 then
        do
           i=1             -- first (and only) entry
           a[i]=options    -- remainder of value for previous option
        end
        else
           a[i]=a[i] || options  -- remainder of value for previous option

        cpPropertySeen? = cpPropertySeen? | abbrev(a[i],cpPropertyNeedle)
        leave mainLoop
     end

     preVal=substr(options, 1, pos-1)  -- get string before dash, if any

     if preVal<>"" then
     do
        if preVal~length>1 then  -- more left than the delimiting blank char?
        do                    -- this value belongs to previous or first (?) option
           tmpVal=substr(preVal,1,length(preVal)-1)
           if i=0 then
           do
              i=1
              a[i]=tmpVal     -- some unknown first option?
           end
           else
              a[i]=a[i] || tmpVal   -- add to previous option's value

           cpPropertySeen? = cpPropertySeen? | abbrev(a[i],cpPropertyNeedle)
        end
     end

     -- new option to deal with
     tmpPos=pos
     i=i+1
     do findNextSwitch=1 while .true
        nextSwitchPos=pos(" -", options, tmpPos+1) -- get position of next switch
        if nextSwitchPos<>0 then             -- a new switch found ?
        do
           begQuot=pos('"', options, tmpPos+1)
           if begQuot<nextSwitchPos then     -- a quote, check whether " -" is part of it
           do
              endQuot=pos('"', options, begQuot+1)
              if endQuot>nextSwitchPos then  -- oops, " -" is part of string, ignore it
              do
                 tmpPos=endQuot              -- define new start for looking for next switch
                 iterate findNextSwitch      -- try to find next switch
              end
           end

           a[i]=substr(options, pos, nextSwitchPos-pos)
           cpPropertySeen? = cpPropertySeen? | abbrev(a[i],cpPropertyNeedle)

           options=substr(options, nextSwitchPos+1)
           iterate mainLoop
        end

        if nextSwitchPos=0 then  -- last option in hand
        do
           a[i]=substr(options, pos)
           cpPropertySeen? = cpPropertySeen? | abbrev(a[i],cpPropertyNeedle)
           leave mainLoop
        end
     end

  end

  -- if java.class.path not defined, use CLASSPATH (remove duplicates, explode trailing asterisks '*';
  -- in Java 6 explosion of asterisks was done, at least starting with Java 8 we need to do that!)
  if cpPropertySeen?=.false then
  do
     processedCP=get_processed_classpath()
     if processedCP<>"" then  -- if CLASSPATH was set, use it in the processed form
        a~append(cpPropertyNeedle || processedCP)
  end
  return a

/*
   2021-02-16: make sure we explode trailing asterisks (*) to individual paths to contained jar/JAR files,
   cf. <https://docs.oracle.com/javase/7/docs/technotes/tools/windows/classpath.html>;
   this routine will remove duplicates and non-existing paths, and replace '*' with the jar/JAR files
   in that directory
*/
get_processed_classpath: procedure
   newCP=""                      -- exploded (new) CLASSPATH
   pathSep=.file~pathSeparator   -- get system's path separator
   currCP=value("CLASSPATH",,"ENVIRONMENT")  -- get current value
   val=currCP
   if val~pos('*')>0 then     -- explode asterisks
   do
      processed =.set~new     -- paths to jar/JAR/zip already processed
      do until val=""
         parse var val path (pathSep) val
         if path="" | path="*" then iterate  -- skip empty paths or paths that consist of the asterisk only

         if processed~hasIndex(path) then iterate  -- already processed (a dupe), skip it
         processed~put(path)  -- memorize that we processed this

         if path~right(1)="*" then   -- explode
         do
             dir=path~left(path~length-1)   -- remove trailing asterisk
             if sysFileExists(dir)=.false then iterate   -- non-existing path, skip it
             do ext over .array~of('.jar', '.JAR') -- get and add the jar/JAR files found
                pattern=path || ext
                call sysfiletree pattern, "files.", "fo"
                do i=1 to files.0   -- add all found jar files
                   if processed~hasIndex(files.i) then iterate -- already on new CLASSPATH, skip
                   processed~put(files.i)
                   if newCP="" then newCP=files.i
                               else newCP=newCP || pathSep || files.i
                end
             end
         end
         else  -- just append
         do
             if sysFileExists(path)=.false then iterate   -- non-existing path, skip it
             if newCP="" then newCP=path
                         else newCP=newCP || pathSep || path
         end
      end
   end
   if newCP="" then return currCP
   return newCP


/* ================================================================================================= */
/* -------------------------------------------------------------
   routine which mimicks C/Java's "x ? a : b;"
   if arg(1) is true, returns second argument, else third
   ------------------------------------------------------------- */
   /* VBasic-like iif() function: if arg(1) is .true, then return arg(2), else arg(3)  */
::routine iif  public
  if arg(1)=.true then return arg(2)
  return arg(3)



/* ================================================================================================= */
/* -------------------------------------------------------------
   floating class method meant for attaching to imported Java
   classes (for which an Object Rexx proxy class is created)
   to construct an instance (a "bean") with strict arguments (i.e.
   type information are given for each argument; arguments are
   passed to BSF which uses them to create the bean instance
   from the given Java class, ---rgf, 2003-09-05
   ------------------------------------------------------------- */
::METHOD newStrict guarded
  USE ARG javaClass, args

  instance=.bsf_reference~new("uninitialized BSF-object!")

        -- create an instance of javaClass and register it with BSF,
        -- leave beanName empty, this way we get a unique name from Java
   if args~class = .array then tmpArr=args            -- array of arguments
                          else tmpArr=arg(1, "Array") -- create an array from arguments

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  arr=.array~of("newStrict", .nil, self~bsf.FullJavaClassName) ~~appendAll(tmpArr)
  beanName=self~rBsf~callWith(arr) ~substr(4)

        -- assign beanName to object, use the Java supplied name
  instance~ObjectName=beanName
  return instance

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ================================================================================================= */
/* -------------------------------------------------------------
   routine which checks whether data returned from the Java side
   represents a Java object; if so, an Object Rexx proxy is created
   and returned, else the received string value
   ------------------------------------------------------------- */
   -- 2009-02-10, rgf: prefixes are always set by BSF.CLS; however RexxEngine only prefixes
   --                  bean-names with the object identifier "<O>", but leaves all other
   --                  strings intact (if a string argument in RexxEngine starts with the
   --                  object identifier "<O>", then that string will be prefixed with the
   --                  string identifier "<S>", which gets processed by bsf.wrap() as
   --                  expected)
   -- 2015-07-21, rgf: honor new argument "bIncr", if set to .true, then the reference counter
   --                  in the BSF registry is increased (needed, if Java object is an argument
   --                  to RexxEngine.call() [native code: ...jniRexxRunProgram(),
   --                  ...jniRexxSendMessageToRexxObject()]
   -- 2016-12-12, rgf: make sure we return a BSF/BSF_REFERENCE object unchanged; allowing it
   --                  to be wrapped up as a BSF_REFERENCE again, may cause runtime errors
   --                  if the wrong number of uninits get executed (each decreases the refCount
   --                  on the Java side)
::ROUTINE bsf.wrap public
   if arg(1)~isA(.bsf) then   -- make sure we do not create a BSF_REFERENCE for an existing BSF, BSF_REFERENCE instance
      return arg(1)

      -- parse the string returned by the Java side, it will have either "<S>" (string) or "<O>" (Java object) prepended;
   parse arg indicator +3 data, bIncr
-- say .line ": BSF.CLS - bsf.wrap, arg(1)="pp(arg(1)) "indicator="pp(indicator) "data="pp(data) "bIncr="pp(bIncr)

   if indicator="<S>" then          -- genuine string
   do
      return data                   -- return string portion
   end
   else if indicator="<O>" then     -- Java object, i.e. index for BSFRegistry
   do
      if data=.bsf~bsf.null_string then
         return .nil

      dimension = verify(data, "[")-1  -- is it a Java array?
      if dimension>0 then   -- rgf, 2003-02-09, no upper limit anymore
      do
         if bIncr<>"" then
            return .bsf_array_reference~new(data, dimension, bIncr) -- return a BSF_ARRAY proxy
         else
            return .bsf_array_reference~new(data, dimension) -- return a BSF_ARRAY proxy
      end

      if bIncr<>"" then
         return .bsf_reference~new(data,bIncr)  -- return a BSF_REFERENCE, increase reference counter
      else
         return .bsf_reference~new(data)  -- return a BSF_REFERENCE
   end

   return arg(1)                    -- an unprefixed string argument from RexxEngine




/* ================================================================================================= */
/* ================================================================================================= */
/* *************************************************************
   Class:       "BSF4REXX"
   ************************************************************* */
/** Serves as a pseudo directory, will always be available as part of the package. Since: 20171223 (removing dependency on .local)
*   - the attribute 'bsf.dir' can be set only once
*/
::class BSF4REXX public

/* ------------------------------------------------------------------------------------------------- */
::method    init guarded class
  expose bsf.dir
  bsf.dir=.nil

/* ------------------------------------------------------------------------------------------------- */
::attribute bsf.dir get guarded class

/* ------------------------------------------------------------------------------------------------- */
::attribute bsf.dir set guarded class
  expose bsf.dir
  if bsf.dir<>.nil then return      -- already assigned, do not accept another one
  use arg bsf.dir

/* ------------------------------------------------------------------------------------------------- */
::method unknown     guarded class    -- forward any unknown messages to the attribute
  expose bsf.dir
  forward message (arg(1)) to (bsf.dir) arguments (arg(2))



/* ================================================================================================= */
/* *************************************************************
   Class:       IBM's "Bean Scripting Framework"
   Purpose:     the proxy super-class for the Java classes
   ************************************************************* */
::CLASS BSF PUBLIC

/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   class method which merely initializes the class attribute "bsf.null_string"
   to its default value
   ------------------------------------------------------------- */
::METHOD init           guarded CLASS
  -- EXPOSE bsf.null_string bsf.FullJavaClassName bsf.JavaClassObjectProxies bsf.JavaClassObjects dir
  EXPOSE bsf.null_string bsf.FullJavaClassName rBSF rBsfUninit4JavaBean rBsfRexxProxy rBsfCreateRexxProxy

  bsf.null_string=".NIL"
  bsf.FullJavaClassName=.nil     -- this is an Object Rexx path, hence no fully
                         -- qualified Java class available

   -- get the routine objects (of the native routines) and cache them in class attributes; rgf, 20180224
  rBSF               =.routines["XBSF"               ]
  rBsfUninit4JavaBean=.routines["XBSFUNINIT4JAVABEAN"]
  rBsfRexxProxy      =.routines["XBSFREXXPROXY"      ]
  rBsfCreateRexxProxy=.routines["XBSFCREATEREXXPROXY"]

/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE rBSF                 guarded CLASS
/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE rBsfUninit4JavaBean  guarded CLASS
/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE rBsfRexxProxy        guarded CLASS
/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE rBsfCreateRexxProxy  guarded CLASS


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   class method to allow invoking static methods via ooRexx
   ------------------------------------------------------------- */
::method unknown class unguarded
  expose bsf.jco  -- 2010-05-08, rgf

  signal on syntax
  if self~bsf.FullJavaClassName=.nil then   -- no fully qualified Java class ("path") given ?
  do
      -- Object "object" does not understand message "message"
     raise syntax 97.1 array(self~string, arg(1))
  end

     -- forward unknown message to the BSF proxy object representing the Java class object,
     -- this way it becomes possible to access the Java static (class) methods quite easily

-- 2010-05-08, rgf   -- rgf, 2005-06-11: use the Java class proxy object to dispatch the message
-- 2010-05-08, rgf  forward to (.bsf~bsf.getJavaClassObjectProxy(self~bsf.FullJavaClassName)) MESSAGE (arg(1)) ARGUMENTS (arg(2))
-- say "unknown::class, forwarding to" pp(bsf.jco) "..."
  forward to (bsf.jco) MESSAGE (arg(1)) ARGUMENTS (arg(2))  -- 2010-05-08, rgf


syntax:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ------------------------------------------------------------------------------------------------- */
::method bsf.jco attribute guarded class private -- contains the BSF proxy to the Java Class Object (JCO) in
                                         -- the BSF registry, such that it does not get garbage collected!


/* ------------------------------------------------------------------------------------------------- */
/* Utility method to allow storing the jco-object in an attribute as well as the full Java class name
 * this class represents.

 -->    new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)  -- rgf 2010-04-18
*/
::method bsf.setBSF4RexxInfos guarded class
  expose bsf.jco bsf.FullJavaClassName
  if arg(1)<>self | arg()<>3 then return .false

  bsf.jco=arg(2)
  bsf.FullJavaClassName=arg(3)

  return .true


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   Class method allows for creating an ooRexx proxy class for a Java class
   which is made available to this process by placing it (optionally) into the
   environment, unless the second argument is explicitly .nil

   @param java_path the fully qualified (binary) name of the Java class
   @param optional classname to be used to save the new class object in the environment
          if omitted or .nil there will be no entry in the environment
   @param extendecJavaClz optional class object representing a Java class object,
          which will be used to extend and supply it with the ooRexx new methods

   @return new class object representing the Java class object, but enhanced with
           the Rexx new and newStrict methods to ease creation of instancews for
           Rexx programmers
   ------------------------------------------------------------- */
::METHOD bsf.import guarded class
  signal on syntax
  USE STRICT ARG java_path, classname=.nil, extendedJavaClz=.nil  -- 2018-09-14 changed to STRICT

  bNoClassName=(arg(2, "Omitted") | className="" | .nil=className)

  if extendedJavaClz=.nil then      -- let Java create the class object, which we can refer to
     jco=bsf.loadClass(java_path)   -- get Java class object (can be a BSF_Reference !)
  else                              -- use the passed Java class object
     jco=extendedJavaClz

      -- 2019-02-02, rgf: check whether jco is a Class object and that it is not abstract
      -- determine whether jco is an instance of java.lang.Class
  parse value jco~getClass~objectName with clzName "@"
  if clzName<>"java.lang.Class" then
     raise syntax 40.900 array ("Java class """java_path""" is not a Java class object.")

      -- now check for abstract, just use java.lang.reflect.Modifier~isAbstract
  if bsf.loadClass("java.lang.reflect.Modifier")~isAbstract(jco~getModifiers) then
     raise syntax 40.900 array ("Java class """java_path""" is abstract; cannot be instantiated, hence 'bsf.import(...)' is illegal; use 'bsf.loadClass("""java_path""")' instead")

      -- now create proxy class object
  new_class = .BSF~subclass(java_path) -- create a subclass of .BSFClass (a proxy ooRexx class)
  new_class~bsf.setBSF4RexxInfos(new_class, jco, java_path)
  new_class~objectName=jco~objectname      -- now assign beanName

  -- 20100419, rgf, TODO: check whether beanName is also stored in the class attribute
  --                      if so, UNO.CLS can be changed to use that instead of .bsf~bsf.JavaClassObject*()

      -- attach a "newStrict" constructor method to the Object Rexx class object
      -- a MUST: bsf.attachNewStrict supplies the attribute "bsf.FullJavaClassName" with a correct value
  new_class~bsf.attachNewStrict( java_path )

      -- create code for the INIT-method of the new class
  method_txt = 'self~init:super("' || jco~string || '", ARG(1,"A"))'
  new_class~define('init', method_txt)  -- define the constructor instance method "INIT"

  if .nil=arg(2) then return new_class    -- if arg(2) is explicitly .nil, then do not store in Rexx environment, just return class

  -- save the new BSF proxy class in .local to make it available as any other ooRexx class
  if bNoClassName then className=java_path   --- use the Java class name on the ooRexx side as well

-- rgf, 20171223
  -- .local~setentry(className, new_class) -- put new ooRexx proxy class into the local environment
  .environment~setentry(className, new_class) -- put new ooRexx proxy class into the local environment

  return new_class

syntax:
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   a synonym for bsf.import(), remove code, forward to class method BSF.IMPORT
   ------------------------------------------------------------- */
::METHOD bsf.importClass guarded class
  FORWARD MESSAGE ("BSF.IMPORT")


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   allows to retrieve the fully qualified Java class name, if a
   Java class got imported (encapsulated in an Object Rexx class
   proxy); needs to be a method in order to be able to use Object's
   SETMETHOD
   ------------------------------------------------------------- */
::METHOD bsf.attachNewStrict guarded class
  use arg java_path
  if self~superclasses[1]<>.bsf then return   -- not a direct subclass of "BSF",
                                              -- hence not from "IMPORT"
  self~setmethod("newStrict", .methods["NEWSTRICT"])
  if self~bsf.FullJavaClassName=.nil then    -- rgf, 2005-08-28
     self~bsf.FullJavaClassName=java_path    -- save fully qualified Java class name


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   allows to retrieve the fully qualified Java class name, if a
   Java class got imported (encapsulated in an Object Rexx class
   proxy); not directly accessible from outside the class
   ------------------------------------------------------------- */
::method bsf.FullJavaClassName guarded class  -- can be read from any object
  expose bsf.FullJavaClassName
  return bsf.FullJavaClassName

::method  "bsf.FullJavaClassName=" guarded class private  -- can only be set via "self"
  expose  bsf.FullJavaClassName
  use arg bsf.FullJavaClassName



/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   class attribute method stores and allows access to the string
   which is used to indicate "null" for the Java side; upon return
   of values from Java, a Java "null" will be represented via
   Object Rexx' .nil object
   ------------------------------------------------------------- */
::ATTRIBUTE bsf.null_string get unguarded CLASS
/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE bsf.null_string set guarded CLASS


/* ------------------------------------------------------------------------------------------------- */
::attribute rii_id  set guarded private  -- stores the RII (Rexx interpreter instance) id that created the instance
/* ------------------------------------------------------------------------------------------------- */
::attribute rii_id  get guarded -- rgf, 2016-11-27, stores the RII (Rexx interpreter instance) id that created the instance

/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   method to construct an instance (a "bean"); arguments are
   passed to BSF which uses them to create the bean instance
   from the given Java class
   ------------------------------------------------------------- */
::METHOD init   guarded
  expose rii_id                    -- RII (Rexx interpreter nstance) where the bean gets stored in the BSF registry
  USE ARG javaClass, args

  rii_id=.bsf.rii_id -- save the RII that created this bean as its BSFManager's BSF registry is used to store its peer

        -- create an instance of javaClass and register it with BSF,
        -- leave beanName empty, this way we get a unique name from Java

   signal on syntax           -- reraise condition in caller

   if args~class = .array then tmpArr=args            -- array of arguments
                          else tmpArr=arg(2, "Array") -- create an array from arguments

   -- 2018-02-24, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  arr=.array~of("new", .nil, javaClass)~~appendAll(tmpArr)
  beanName=self~class~rBSF~callWith(arr)~substr(4)

        -- assign beanName to object, use the Java supplied name
  self~ObjectName=beanName
  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   destructor method (called by Object Rexx when object gets
   destroyed) to unregister the bean from the BSF registry; this
   then allows the JVM to garbage collect that Java object as well
   ------------------------------------------------------------- */
-- 2015-08-04, rgf: NOTE: according to RMG in his late answer it may be the case that a RII gets only created
--                        for the purpose to handle pending uninits on the Rexx side (having its own .local!);
--                        if so, it may be the case that BSF uninits run at a time in a thread that has no
--                        means to attach to Java; will leave it that way (a Rexx condition gets raised that
--                        does not bring down the garbage collector), as this condition means that Rexx is
--                        shutting down!
-- 2018-02-23, rgf: not returning anything from UNINIT; make sure we deregister the Java object even in the
--                  the case that a RexxAndJava object does not exist (TODO: BsfUnregisterJavaObject)
::METHOD uninit guarded
  expose rii_id

  signal on syntax name syntax1     -- trap any syntax conditions
  res=self~class~rBsfUninit4JavaBean~call(self~objectname)   -- use routine object
  if res<0 then .error~say(.line "BSF.CLS - BSF::UNINIT: "pp(self~objectName) "was not found in registry...")
  return

syntax1:  -- intercept and thereby clear any condition
   co=condition('o')
  .error~say( .line "BSF.CLS -" self":uninit -" pp(self~objectName)"~uninit - created on rii_id="pp(rii_id)", an (ignorable) error occurred" pp(BSF_ERROR_MESSAGE) pp(co~message) )



/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   dispatch method, which the programmers should use if they desire to
   directly dispatch a method on the Java side; this way they are not
   forced to use UNKNOWN directly (unless, of course, there exists a
   Java method "DISPATCH", which will force the direct usage of UNKNWON);
   will pass all received arguments (starting with the second argument)
   as an array object to UNKNOWN
   ------------------------------------------------------------- */
::METHOD bsf.dispatch guarded
  use ARG methodName
  forward message ("BSF.INVOKE")


/* ------------------------------------------------------------------------------------------------- */
/* 2016-11-06, ---rgf
   On ooRexx 5.0.0 beta the DO...OVER only works if the receiving object has a MAKEARRAY method and does
   not rely on the UNKNOWN method at all. For this reason we need to have a MAKEARRAY method present in
   the BSF class: starting with basing BSF4ooRexx on Java 1.6/6.0 in November 2016, all Java objects
   implementing java.lang.Iterable, java.util.Enumeration, java.util.Collection, java.util.Iterator or java.util.Map
   will get a "virtual ooRexx" MAKEARRAY and SUPPLIER method, returning an ooRexx array or an ooRexx supplier.

   The new Java code will create a Rexx array in Java and populate it, returning the RexxProxy object. The contained
   Rexx object can be extracted with the external Rexx function BsfRexxProxy(proxy,"object")

   It is expected that this particular message gets only invoked by DO...OVER, which expects (as of ooRexx 5.0.0beta)
   a genuine Rexx array object to be returned
*/
::method makearray  guarded  -- testing, 2016-11-06, rgf
   if arg()=0 then
   do
       res=self~bsf.dispatch("MAKEARRAY")    -- send the message directly to the Java object

       if res~objectName~abbrev("org.rexxla.bsf.engines.rexx.RexxProxy@") then -- if a proxied Rexx object, return Rexx object
          return self~class~rBsfRexxProxy~call(res,"o") -- get the proxied Rexx object, should be the Rexx array object
       else
          return res
   end

   -- arguments going with the message, let UNKNOWN forward this
   forward message ("unknown") array("MAKEARRAY", arg(1,"array"))


/* ------------------------------------------------------------------------------------------------- */
/* 2016-11-06, ---rgf
   Intercept supplier message as returned value might be a RexxProxy which we need to extract the
   Rexx object from.
*/
::method supplier   guarded  -- testing, 2016-11-06, rgf

   if arg()=0 then
   do
       res=self~bsf.dispatch("SUPPLIER")     -- send the message directly to the Java object
       if res~objectName~abbrev("org.rexxla.bsf.engines.rexx.RexxProxy@") then -- if a proxied Rexx object, return Rexx object
          return self~class~rBsfRexxProxy~call(res,"o") -- get the proxied Rexx object, should be the Rexx supplier object
       else
          return res
   end

   -- arguments going with the message, let UNKNOWN forward this
   forward message ("unknown") array("SUPPLIER", arg(1,"array"))



/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   unknown method (gets called by Object Rexx whenever an unknown
   message was sent to an instance of this class); forwards the
   message to BSF including all of the received arguments
   ------------------------------------------------------------- */
::METHOD unknown  unguarded
  USE ARG name, args

  signal on syntax               -- trap any exceptions
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  if args~class=.array then
  do
     if (right(name,1)="=") then -- field assignment?
     do
        items=args~items
        parse var name tmpName "=" . -- extract message name without '='
        if items=1 then             -- arg(1)=value to assign to field
           return self~bsf.setFieldValue(tmpName, args[1])
        else if items=2 then        -- assume arg(1)=typeindicator, arg(2)=value
           return self~bsf.setFieldValueStrict(tmpName, args[1], args[2])
     end
  end

   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  objName=self~objectName
  arr=.array~of("invoke", objName, name)
      -- if this is a RexxProxy and a message directed at it, use the current RexxAndJava instance to carry it out
  if objName~abbrev("org.rexxla.bsf.engines.rexx.RexxProxy@"), -
     "SENDMESSAGE0 SENDMESSAGE1 SENDMESSAGE2 SENDMESSAGE3 SENDMESSAGE4 SENDMESSAGE5 SENDMESSAGE"~caselessWordPos(name)>0 then
  do
     arr~append( bsf.wrap(self~class~rBSF~call('getRexxAndJava')))
  end
  arr~appendAll(args)                              -- add arguments, if any

  return bsf.wrap(self~class~rBSF~callWith(arr))   -- process result and return it


syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message

  raise propagate  -- raise the exception in caller/invoker on the Rexx side




/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   the following methods implement the individual BSF-calls as
   methods, allowing for invoking them in the OO style with the
   help of messages;

   "registerBean" is not made available explicitly, it is
   implicitly part of the creation of a new proxy object (see
   "INIT" method above)

   "unregisterBean" is not made available explicitly, it is
   implicitly part of the destruction of objects (see the
   "UNINIT" methods above)

   "getStaticValue" is not made available to instances as they
   are able to get (and even set) public (static) fields via
   "getFieldValue" resp. "setFieldValue"
   ------------------------------------------------------------- */

--   instance methods
   -- TODO: 2017-08-21, rgf: remove this eventually (pre ooRexx 4.0 code)
::METHOD bsf.addEventListener guarded  /* add event listener to the Java object*/
   use arg eventSetName, filter, text

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   return self~class~rBSF~call( "addEventListener", self~objectname, eventSetName, filter, text )~substr(4)

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate



/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.exit             guarded  /* exit Rexx                    */
   FORWARD MESSAGE ("BSF.EXIT") TO (self~class)


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.invoke           guarded
   parse arg methodName
   -- create UNKNOWN format (first argument is unknown method name)
   return self~unknown(methodName, arg(2, 'A'))


/* ------------------------------------------------------------------------------------------------- */
::method bsf.invokeJSO  guarded -- rgf, 20141003: return a reference to a Java or NetRexx object
  USE ARG name          -- name of method to be invoked
  args=arg(2, 'A')

  signal on syntax
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  arr=.array~of("invokeJSO", self~objectname , name)~~appendAll(args)
  return bsf.wrap(self~class~rBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.invokeStrict    guarded
  USE ARG name          -- name of method to be invoked
  args=arg(2, 'A')

  signal on syntax
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  arr=.array~of("invokeStrict", self~objectname , name)~~appendAll(args)
  return bsf.wrap(self~class~rBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.invokeStrictJSO guarded  -- rgf, 20141004: return a reference to a Java or NetRexx object
  USE ARG name          -- name of method to be invoked
  args=arg(2, 'A')

  signal on syntax
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  arr=.array~of("invokeStrictJSO", self~objectname , name)~~appendAll(args)
  return bsf.wrap(self~class~rBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getFieldValue   guarded
   use strict arg fieldName
   signal on syntax
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("getFieldValue", self~objectname , fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getFieldValueJSO guarded -- rgf, 2014-10-04
   use strict arg fieldName
   signal on syntax
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("getFieldValueJSO", self~objectname , fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getFieldValueStrict guarded -- case-sensitive fieldName !
   -- parse arg fieldName
   use strict arg fieldName

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("getFieldValueStrict", self~objectname , fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getFieldValueStrictJSO guarded -- rgf, 2014-10-04, case-sensitive fieldName !
   use strict arg fieldName
   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("getFieldValueStrictJSO", self~objectname , fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.setFieldValue       guarded
   use strict arg fieldName, newValue
   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("setFieldValue", self~objectname , fieldName, newValue))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.setFieldValueStrict guarded
  use strict arg fieldName, x, ...     -- at least two arguments a must

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  arr=.array~of("setFieldValueStrict", self~objectname , fieldName)~~appendAll(arg(2,"A"))
  return bsf.wrap(self~class~rBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getPropertyValue    guarded
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( self~class~rBSF~call("getPropertyValue", self~objectname, propertyName, index))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.getPropertyValueJSO guarded    -- rgf, 2014-10-04
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( self~class~rBSF~call("getPropertyValueJSO", self~objectname, propertyName, index))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.setPropertyValue    guarded
  parse arg propertyName, index

  signal on syntax
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string
  arr=.array~of('setPropertyValue', self~objectname, propertyName, index)~~appendAll(arg(3,"a"))
  return bsf.wrap(self~class~rBsf~callWith(arr) )

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD bsf.setPropertyValueStrict guarded
  parse arg propertyName, index
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string

  signal on syntax
  if arg(2, "Omitted") then index = "" -- no index given, set to empty string
  arr=.array~of('setPropertyValueStrict', self~objectname, propertyName, index)~~appendAll(arg(3,"a"))
  return bsf.wrap(self~class~rBsf~callWith(arr) )

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


-- rgf, 2010-02-20
::METHOD bsf.isA                 guarded
  use strict arg javaClass

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(self~class~rBSF~call("isA", self~objectname , javaClass))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ------------------------------------------------------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------- */
--   class  methods

/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   class method shuts down the Java Virtual Machine (JVM), which started
   this instance of Rexx, this causes Rexx to be shut down as well; the
   Java side waits "waitTime" msecs before terminating the JVM
   ------------------------------------------------------------- */
::METHOD bsf.exit class          guarded
  expose rBSF
  use arg exitValue, waitTime

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if \datatype(exitValue, "Whole Number") then return rBSF~call("exit")~substr(4)
  if \datatype(waitTime,  "W"           ) then return rBSF~call("exit", exitValue)~substr(4)
  return rBSF~call("exit", exitValue, waitTime)~substr(4)

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   class method to have Java sleep for us "sleepTime" seconds; resolution
   to a msec is calculated, although the Java side may deviate up to 15/100 secs
   (we are not talking realtime here ;)
   ------------------------------------------------------------- */
::METHOD bsf.sleep class         guarded
  expose rBSF
  use arg sleepTime
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.sleep, error 1: too many arguments (maximum: 1, found: "arg()")")

  return rBSF~call("sleep", sleepTime)~substr(4)

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.loadClass                 guarded class
  expose rBSF
  -- parse arg JavaClassName
  use arg javaClassName, name4env=.nil
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

   -- 2019-02-04
   jClz=bsf.wrap( rBSF~call("loadClass", javaClassName) )
   if name4env<>.nil then .environment~setEntry(name4env,jClz)
   return jClz

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.lookupBean                guarded class
  expose rBSF
  parse arg beanName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- 2018-02-25, rgf: no need to escape .nil on ooRexx, will be correctly translated to null
  return bsf.wrap(rBSF~call("lookupBean", beanName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.pollEventText             guarded class
  expose rBSF
  parse arg timeOut

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()>1 then    -- 2009-06-27, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.pollEventText, error 1: too many arguments (maximum: 1, found: "arg()")")

  if \datatype(timeOut, "Whole") then return bsf.wrap(rBSF~call("pollEventText"))
  return bsf.wrap(rBSF~call("pollEventText", timeout))    -- with timeout

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.getStaticValue            guarded class
  expose rBSF
  parse arg className, fieldName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBSF~call("getStaticValue", className, fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.getStaticValueJSO         guarded class   -- rgf, 2014-10-04
  expose rBSF
  parse arg className, fieldName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBSF~call("getStaticValueJSO", className, fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.getStaticValueStrict      guarded class
  expose rBSF
  parse arg className, fieldName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBsf~call("getStaticValueStrict", className, fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.getStaticValueStrictJSO   guarded class   -- rgf, 2014-10-04
  expose rBSF
  parse arg className, fieldName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBsf~call("getStaticValueStrictJSO", className, fieldName))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.postEventText             guarded  class
  expose rBSF
  parse arg eventText, priority
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.postEventText, error 1: too many arguments (maximum: 2, found: "arg()")")

  if arg()=1 then return bsf.wrap(rBsf~call("postEventText", eventText))
  return bsf.wrap(rBsf~call("postEventText", eventText, priority))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.wrapArray                  unguarded class
  expose rBSF
  parse arg beanName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBsf~call("wrapArray", beanName) )

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.createArray                guarded class
  expose rBSF

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  arr=.array~of("createArray")~~appendAll(arg(1,"A"))
  return bsf.wrap(rBsf~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking as is.
   20150721: make sure we wrap non-String and non-BSF Rexx objects as RexxProxy objects,
             reliefing the programmers to do so
*/
::METHOD     bsf.createArrayOf              guarded class  -- rgf, 2009-07-30
  expose rBSF rBsfCreateRexxProxy
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  bUnoProxy=.uno_proxy~isA(.class)     -- is UNO (AOO) support loaded?
  tmpArr=.array~new
  do i=1 to arg()
     a=arg(i)
      -- make sure we leave .UNO_PROXY objects unwrapped
     if bUnoProxy then aBSF=a~isA(.uno_proxy)
                  else aBSF=.false

     if i>1, \a~isA(.string), \a~isA(.bsf), \aBSF, a<>.nil then
         tmpArr~append(rBsfCreateRexxProxy~call(a))   -- make Rexx object available to Java
     else
         tmpArr~append(a)
  end

  arr=.array~of('createArrayOf')~~appendAll(tmpArr)
  return bsf.wrap( rBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.createJavaArray                guarded class          -- rgf, 2009-09-10
  forward message (bsf.createArray)


/* ------------------------------------------------------------------------------------------------- */
/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::METHOD     bsf.createJavaArrayOf              guarded class  -- rgf, 2009-09-10
  forward message (bsf.createArrayOf)


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.wrapEnumeration            guarded class
  expose rBSF
  parse arg beanName
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return bsf.wrap( rBsf~call("wrapEnumeration", beanName) )

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
   -- set the string which from now on represents the Java "null" on both sides,
   -- the Rexx and the Java side; this was very important for classical Rexx and Regina
::METHOD     bsf.setRexxNullString         guarded class
  expose rBSF
  parse arg newValue
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if arg()<>1 then   -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
     raise syntax 40.900 array ("BSF.CLS/class BSF/class method bsf.setRexxNullString, error 1: too many arguments (maximum: 1, found: "arg()")")

   -- rgf, 2018-02-19: rearrange
  .bsf~bsf.null_string = bsf.wrap(rBSF~call("setRexxNullString", newValue))                  -- set class attribute to the new value
  return .bsf~bsf.null_string

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::METHOD     bsf.haltThread guarded class
  expose rBSF
  use strict arg tid

  signal on syntax
  return bsf.wrap(rBSF~call("haltThread", tid)) -- set the Java side to the new value

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
/* This class method will be used by native code (BSF4ooRexx.cc).
*/
   -- 2015-07-21, rgf: honor new argument "bIncr", if set to .true, then the reference counter
   --                  in the BSF registry is increased (needed, if Java object is an argument
   --                  to RexxEngine.call() [native code: ...jniRexxRunProgram(),
   --                  ...jniRexxSendMessageToRexxObject(), both will supply .true as a new second arg]
::METHOD    bsf.wrap          unguarded class    -- 20090515: make routine BSF.WRAP() indirectly availabe (possible 4.0 API Problem)
  use arg obj, bIncr=.false
  return bsf.wrap(obj,bIncr)



/* ================================================================================================= */
/* ================================================================================================= */
/* *************************************************************
   Class:       BSF_REFERENCE
   Purpose:     the proxy class for objects returned by the Java side;
                objects of this class, if destroyed, cause via its superclass
                to remove one from the appropriate instance counter of the
                BSF registry
   Restriction: Should only be used by classes/routines in this file

   ************************************************************* */
::CLASS BSF_REFERENCE subclass BSF

/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   method to construct an instance (a "bean"); as the object was returned
   from the Java side, just remember its name, but do not call BSF's init
   again! This proxy object merely allows for sending messages through it
   to the Java object as well. If it goes out of scope, it will be garbage
   collected by Object Rexx which in turn deregisters the object from the
   Java side, decrementing the reference counter there

   - bsf.wrap: honor new argument "bIncr", if set to .true, then the reference counter
     in the BSF registry is increased (needed, if Java object is an argument
     to RexxEngine.call() [native code: ...jniRexxRunProgram(),
     ...jniRexxSendMessageToRexxObject(), both will supply .true as a new second arg]
   ------------------------------------------------------------- */
::METHOD init               guarded
   use arg beanName, bIncr=.false   -- should be .false by default, only when Java-args get proxied should ".true" be given
   self~ObjectName=beanName
   self~rii_id=.bsf.rii_id    -- remember RII that created this instance
   -- rgf, 2015-07-20: increment BSF registry counter for this Java object, such that only running its UNINIT removes Java object from BSF registry
   --                  (only done, if RexxEngine.call() or .apply() was used)
  if bIncr=.true then
      call bsf 'increaseRefCounter', beanName   -- increment BSF registry reference counter


::METHOD util.setmethod    guarded -- rgf, 20180607: allow individually to set methods (e.g. for UNO_ENUM in UNO.CLS)
  forward message ("setmethod")



/* ================================================================================================= */
/* ================================================================================================= */
/* *************************************************************
   Class:       BSF_ARRAY
   Purpose:     the proxy class for array objects returned by the Java side
   ************************************************************* */
::CLASS BSF_ARRAY_REFERENCE     subclass BSF

/* ------------------------------------------------------------------------------------------------- */
/* -------------------------------------------------------------
   method to construct an instance (a "bean"); as the object was returned
   from the Java side, just remember its name, but do not call BSF's init
   again! This proxy object merely allows for sending messages through it
   to the Java object as well. If it goes out of scope, it will be garbage
   collected by Object Rexx which in turn unregisters the object from the
   Java side, decrementing the reference counter there

   - bsf.wrap: honor new argument "bIncr", if set to .true, then the reference counter
     in the BSF registry is increased (needed, if Java object is an argument
     to RexxEngine.call() [native code: ...jniRexxRunProgram(),
     ...jniRexxSendMessageToRexxObject(), both will supply .true as a new second arg]
   ------------------------------------------------------------- */
::METHOD init        guarded
  expose items dimension indexArray    -- direct access to object variable
  use arg beanName, dimension, bIncr=.false  -- assign "dimension" directly to exposed object variable

  self~rii_id=.bsf.rii_id    -- remember RII that created this instance
  self~ObjectName=beanName
  items=-1                     -- set and indicate, that not calculated as of yet

  rBSF=self~class~rBSF

  signal on syntax name syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
   -- "indexArray" allows for indexing at's and put's with a Java int array, use exact signature

   -- set up "indexArray" attribute to match dimension and capacity of the base Java array
   -- wrapping up occurs in the statement after the next, hence no bsf.wrap() !
   -- "<O>" will be removed, such that plain bean-name is assigned to 'indexArray'
  -- just extract the beanName
  indexArray=rBsf~call("invokeStrict", .bsf4rexx~array.class, "newInstance", "o", .bsf4rexx~int, "int", dimension)~substr(4)
  indexArray=.bsf_array_proxy~new(indexArray, 1, dimension)

   -- rgf, 2015-07-21: increment BSF registry counter for this Java object, such that running UNINIT does not remove Java object from BSF registry
  if bIncr=.true then
     rBsf~call('increaseRefCounter',beanName)

  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::method uninit      guarded -- rgf, 20120604: make sure we also unregister the index array to avoid a memory leak
  expose indexArray

  signal on syntax name syntax1
  res=self~class~rBsfUninit4JavaBean~call(indexArray)

  if res<0 then .error~say(.line "BSF.CLS - BSF::UNINIT: indexArray="pp(indexArray) "was not found in registry...")

  signal on syntax name syntax2
  self~uninit:super  -- now let superclass clean up
  return

syntax1:    -- call unregisterBean, indexArray caused a syntax condition
  say .line "BSF_ARRAY_REFERENCE::UNINIT - SYNTAX1 ..., now about to do a: call BSF 'unregisterBean', indexArray"
  signal on syntax name syntax2
  self~class~rBsf~call('unregisterBean', indexArray)

  self~uninit:super  -- now let superclass clean up
  return

syntax2:    -- just clear condition
  say .line "BSF_ARRAY_REFERENCE::UNINIT - SYNTAX2 ..., dummy, just to clear condition"



/* ------------------------------------------------------------------------------------------------- */
::METHOD dimension   guarded
  expose dimension dimensions

  if arg(1, "Omitted") then return dimension    -- return number of dimensions

  if VAR( dimensions ) = .false then self~getData  -- no data available, get it
  return dimensions[arg(1)]                     -- return entries in this dimension


/* ------------------------------------------------------------------------------------------------- */
      -- get the needed information from ArrayWrapper, make it available with Object Rexx attributes
::METHOD getData guarded private
  expose dimensions items

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  rBSF=self~class~rBsf        -- fetch BSF routine object from .BSF

  aw    =bsf.wrap(rBsf~call('wrapArray', self~objectname))           -- get the array wrapper of this array
  items =rBsf~call('getFieldValue', aw, 'items') ~substr(4)          -- get calculated 'items' value
  dimArr=bsf.wrap(rBsf~call('getFieldValue', aw, 'sizeOfDimension')) -- get the Java array containing the dimensions

  dimensions=.array~new                         -- create an Object Rexx array
  do i=1 to rBsf~call('arrayLength', dimArr)~substr(4)          -- get length of 'dimensions' array
     dimensions[i]=bsf.wrap(rBsf~call('arrayAt', dimArr, i-1))  -- use Java indexing (starting with '0')
  end
  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD at          guarded
   expose indexArray  dimension
   use arg tmpArr

   signal on syntax
   .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

   rBSF=self~class~rBsf          -- fetch BSF routine object from .BSF

   if tmpArr~class<>.array then  -- if second argument is not an array, create one
      tmpArr=arg(1, "Array")     -- get arguments as an array

   if tmpArr~items<>dimension then  -- wrong number of subscripts ?
   do
      signal on syntax name syntax
      if tmpArr~items<dimension then            -- too few
          raise syntax 93.925 array (dimension)
      else                                      -- too many
          raise syntax 93.926 array (dimension)
   end

   do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
      rBsf~call("arrayPut", indexArray, tmpArr[i]-1, i-1)
   end

   -- 2015-05-10: special AT processing: if an element is a RexxProxy then replace it
   --             with its contained Rexx object; needed for JSR223-eval() implementation
   entry=rBsf~call('arrayAt', self~objectName, indexArray)   -- get entry from Java array
   res=bsf.wrap(entry)
   if pos("<O>org.rexxla.bsf.engines.rexx.RexxProxy@",entry)>0 then   -- a RexxProxy in hand?
      res=self~class~rBsfRexxProxy~call(res,"o")  -- special AT processing: replace RexxProxy with its contained Rexx object
   return res

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

syntax: raise propagate /* propagate dimension/subscript exception */


/* ------------------------------------------------------------------------------------------------- */
  -- allow for referring to array elements as if it was an Object Rexx array
::METHOD "[]"        guarded
  FORWARD MESSAGE ("AT")        -- synonym for "AT", use its implementation


/* ------------------------------------------------------------------------------------------------- */
  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD put         guarded
  expose indexArray dimension
  use arg value, tmpArr

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  rBSF=self~class~rBsf          -- fetch BSF routine object from .BSF

  if tmpArr~class<>.array then  -- if second argument is not an array, create one
     tmpArr=arg(2, "Array")     -- get rest of arguments as an array

  if tmpArr~items<>dimension then  -- wrong number of subscripts ?
  do
     signal on syntax name syntax
     if tmpArr~items<dimension then            -- too few
         raise syntax 93.925 array (dimension)
     else                                      -- too many
         raise syntax 93.926 array (dimension)
  end

  do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
     -- call bsf "arrayPutStrict", indexArray, "int", tmpArr[i]-1, i-1
     rBsf~call("arrayPut", indexArray, tmpArr[i]-1, i-1)
  end

  rBsf~call('arrayPut', self~objectName, value, indexArray)
  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side

syntax: raise propagate /* propagate dimension/subscript exception */


/* ------------------------------------------------------------------------------------------------- */
  -- Note: This array-proxy is "1"-based (not 0-based as Java), because Object Rexx arrays
  -- are "1", based, therefore subscript adjustments are necessary, before calling the Java side
  -- The indices can be given individually in comma-separated form or in form of a Rexx array
::METHOD putStrict   guarded
  expose indexArray dimension
  use arg type, value, tmpArr

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  rBSF=self~class~rBsf          -- fetch BSF routine object from .BSF

  if tmpArr~class<>.array then  -- if second argument is not an array, create one
     tmpArr=arg(3, "Array")     -- get rest of arguments as an array

  if tmpArr~items<>dimension then  -- wrong number of subscripts ?
  do
     signal on syntax name syntax
     if tmpArr~items<dimension then            -- too few
         raise syntax 93.925 array (dimension)
     else                                      -- too many
         raise syntax 93.926 array (dimension)
  end

  do i=1 to tmpArr~items        -- adjust indices to Java-style (0-based indices)
     rBsf~call("arrayPut", indexArray, tmpArr[i]-1, i-1)
  end

  rBsf~call('arrayPutStrict', self~objectName, type, value, indexArray)
  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


syntax: raise propagate /* propagate dimension/subscript exception */


/* ------------------------------------------------------------------------------------------------- */
  -- allow for referring to array elements as if it was an Object Rexx array;
  -- using this syntax variant has the following implication: the valueType and the
  -- value itself must be given as *one* string value; the first word is the valueType
  -- the rest the value!    e.g.: xyz[1, 2, 3] = "String Hi, there!"
::METHOD "[]="       guarded
  FORWARD MESSAGE ("PUT") -- let the PUT-method do the work, 2003-05-10


/* ------------------------------------------------------------------------------------------------- */
-- calculates (once) and returns number of individual elements in array
::METHOD items       guarded
  expose items  -- set access to object variable
  if items=-1 then self~getData     -- not yet set, get the data from the ArrayWrapper
  return items


/* ------------------------------------------------------------------------------------------------- */
-- Java arrays have a fixed size and their storage is pre-allocated: return same value as method items
::METHOD size        guarded
  return self~items


/* ------------------------------------------------------------------------------------------------- */
-- this method creates a supplier method
::METHOD  SUPPLIER   guarded

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  rBSF=self~class~rBsf          -- fetch BSF routine object from .BSF

  aw=bsf.wrap(rBsf~call('wrapArray', self~objectname))
   -- wrap up the Supplier object, such that it will be kept in the BSF registry as long
   -- as the Object Rexx BSF proxy exists
  -- return bsf.wrap( rBsf~call('invoke', aw, 'supplier', .true) )
  -- return a genuine ooRexx .supplier instance using the new ArrayWrapper.supplier(RexxProxy)
  return self~class~rBsfRexxProxy~call(bsf.wrap(rBsf~call('invoke', aw, 'supplier', .supplier)),"o")

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
-- this method is needed by "DO ... OVER" ad it requires a native Rexx array object!
::METHOD MAKEARRAY   guarded

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  rBSF=self~class~rBsf          -- fetch BSF routine object from .BSF

  aw=bsf.wrap(rBsf~call('wrapArray', self~objectname) )   -- wrap up the array
  arr=bsf.wrap(rBsf~call('invoke', aw, 'makearray', .false, .false)) -- get
  arr=bsf.wrap(rBsf~call('arrayAt', arr, 0))              -- get the appropriate Java array

  rexxArr=.array~new
  do i=1 to rBsf~call('arrayLength', arr)~substr(4)         -- get length of 'dimensions' array
     -- rexxArr[i]=bsf.wrap(bsf('arrayAt', arr, i-1))  -- use Java indexing (starting with '0')
     -- 2015-05-10: special MAKEARRAY processing: if an element is a RexxProxy then replace it
     --             with its contained Rexx object; needed for JSR223-eval() implementation
     entry=rBsf~call('arrayAt', arr, i-1)
     rexxArr[i]=bsf.wrap(entry)  -- use Java indexing (starting with '0')
     if pos("<O>org.rexxla.bsf.engines.rexx.RexxProxy@",entry)>0 then   -- a RexxProxy in hand?
        rexxArr[i]=self~class~rBsfRexxProxy~call(rexxArr[i],"o") -- special MAKEARRAY processing: replace RexxProxy with its contained Rexx object

  end
  return rexxArr

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ================================================================================================= */
/* ================================================================================================= */
   -- wraps the given Java object and allows for using the array methods on it;
   -- if the reference is not used anymore, it will *not* cause the Java object to be de-registered
   -- used in the initialisation of "BSF_ARRAY" to get primitive int array (for the dimensions) without initialising them as standalone BSF-arrays
   --
::CLASS BSF_ARRAY_PROXY SUBCLASS BSF_ARRAY_REFERENCE
/* ------------------------------------------------------------------------------------------------- */
::METHOD init        guarded
  use arg beanName, dimension, items         -- name of Java object as registered in the BSF registry
  self~rii_id=.bsf.rii_id    -- remember RII that created this instance

  self~objectName=beanName
  self~dimension =dimension
  self~items     =items

/* ------------------------------------------------------------------------------------------------- */
::method dimension guarded attribute
/* ------------------------------------------------------------------------------------------------- */
::method items     guarded attribute

/* ------------------------------------------------------------------------------------------------- */
::METHOD uninit    guarded         -- intercept destructor message, don't remove BSF registry entry

  NOP


/* ================================================================================================= */

/* -------------------------------------------------------------------------------------------------
*  Loads and returns the supplied Java class after adding the class methods new and newStrict to
*  it.
*
* @param javaClassName fully qualified (binary) Java class name to import to ooRexx
* @param name4env if supplied, the Java class proxy will be stored under this name in the environment
*                 such that it can be easily referred to with an environment symbol (a Rexx symbol that
*                 starts with a dot followed by the value of name4env
* @param extendedJavaClz optional, the class object representing a Java class object to be enhanced
*                 with the class methods new and newStrict; if omitted the Java class named javaClassName
*                 gets loaded first and enhanced with the class methods new and newStrict
*
* @return the Java class proxy (an ooRexx class object proxying the Java class object) enhanced with
*          the class methods new and newStrict
*/
::routine bsf.import                         public
   signal on syntax
   USE strict ARG java_path, name4env=.nil, extendedJavaClz=.nil
      -- if second argument is omitted, then allow BSF::bsf.import to determine that it got omitted as well
   if arg(2,"omitted") then return .bsf~bsf.import(java_path, , extendedJavaClz)

   return .bsf~bsf.import(java_path, name4env, extendedJavaClz)

syntax:
   raise propagate


/* ================================================================================================= */

/* -------------------------------------------------------------------------------------------------
*  Loads and returns the supplied Java class after adding the class methods new and newStrict to
*  it.
*
* @param javaClassName fully qualified (binary) Java class name to import to ooRexx
* @param name4env if supplied, the Java class proxy will be stored under this name in the environment
*                 such that it can be easily referred to with an environment symbol (a Rexx symbol that
*                 starts with a dot followed by the value of name4env
* @param extendedJavaClz optional, the class object representing a Java class object to be enhanced
*                 with the class methods new and newStrict; if omitted the Java class named javaClassName
*                 gets loaded first and enhanced with the class methods new and newStrict
*
* @return the Java class proxy (an ooRexx class object proxying the Java class object) enhanced with
*          the class methods new and newStrict
*/
::routine bsf.importClass                    public
   signal on syntax
   USE strict ARG java_path, name4env=.nil, extendedJavaClz=.nil

      -- if second argument is omitted, then allow BSF::bsf.import to determine that it got omitted as well
   if arg(2,"omitted") then return .bsf~bsf.import(java_path, , extendedJavaClz)

   return .bsf~bsf.import(java_path, name4env, extendedJavaClz)

syntax:
   raise propagate


/* ================================================================================================= */
/** Loads and returns the supplied Java class.
*
* @param javaClassName fully qualified Java class name to load
* @param name4env if supplied, the Java class proxy will be stored under this name in the environment
*                 such that it can be easily referred to with an environment symbol (a Rexx symbol that
*                 starts with a dot followed by the value of name4env
* @return the Java class proxy (an ooRexx class object proxying the Java class object)
*/
::routine bsf.loadClass                      public
   use arg javaClassName, name4env=.nil
   jClz=bsf.wrap( .routines~xBSF~call("loadClass", javaClassName) )
   if name4env<>.nil then .environment~setEntry(name4env,jClz)
   return jClz




/* ================================================================================================= */
::routine bsf.lookupBean                     public
   parse arg beanName
   return bsf.wrap( .routines~xBSF~call("lookupBean", beanName) )


/* ================================================================================================= */
::routine bsf.unregisterBean                 public
  parse arg beanName
  return .routines~xBSF~call("unregisterBean", beanName)~substr(4)


/* ================================================================================================= */
::routine bsf.pollEventText                  public
   parse arg timeOut
   signal on syntax
   if arg()>1 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.pollEventText(), error 1: too many arguments (maximum: 1, found: "arg()")")

   if \datatype(timeOut, "Whole") then return bsf.wrap( bsf("pollEventText"))
   return bsf.wrap( bsf("pollEventText", timeout))    -- with timeout

syntax:
   raise propagate


/* ================================================================================================= */
::routine bsf.postEventText                  public
   parse arg eventText, priority

   signal on syntax
   if arg()>2 then    -- 2009-06-26, hint by Mr. Lex (author of testunits for BSF4Rexx)
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.postEventText(), error 1: too many arguments (maximum: 2, found: "arg()")")

   if arg()=1 then return bsf.wrap(bsf("postEventText", eventText))
   return bsf.wrap(bsf("postEventText", eventText, priority))

syntax:
   raise propagate


/* ================================================================================================= */
::routine bsf.getStaticValue                 public
   parse arg className, fieldName
   signal on syntax
   return bsf.wrap( .routines~xBSF~call("getStaticValue", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::routine bsf.getStaticValueJSO              public
   parse arg className, fieldName
   signal on syntax
   return bsf.wrap( .routines~xBSF~call("getStaticValueJSO", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::routine bsf.getStaticValueStrict           public
   parse arg className, fieldName
   signal on syntax
   return bsf.wrap( .routines~xBSF~call("getStaticValueStrict", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::routine bsf.getStaticValueStrictJSO        public
   parse arg className, fieldName
   signal on syntax
   return bsf.wrap( .routines~xBSF~call("getStaticValueStrictJSO", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::routine bsf.getConstant                    public
  parse arg className, fieldName
  signal on syntax
  return bsf.wrap( .routines~xBSF~call("getStaticValueStrict", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::routine bsf.getConstantJSO                public
  parse arg className, fieldName
  signal on syntax
  return bsf.wrap( .routines~xBSF~call("getStaticValueStrictJSO", className, fieldName))
syntax: raise propagate


/* ================================================================================================= */
::ROUTINE bsf.createArray                    public
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  arr=.array~of('createArray')~~appendAll(arg(1,'A'))
  return bsf.wrap( .routines~xBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ================================================================================================= */
/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::ROUTINE bsf.createArrayOf                  public -- rgf, 2009-07-30
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  -- code from class method:
  bUnoProxy=.uno_proxy~isA(.class)     -- is UNO (AOO) support loaded?
  tmpArr=.array~new
  rBsfCreateRexxProxy=.routines~xBsfCreateRexxProxy   -- fetch routine Object
  do i=1 to arg()
     a=arg(i)
      -- make sure we leave .UNO_PROXY objects unwrapped
     if bUnoProxy then aBSF=a~isA(.uno_proxy)
                  else aBSF=.false

     if i>1, \a~isA(.string), \a~isA(.bsf), \aBSF, a<>.nil then
         tmpArr~append(rBsfCreateRexxProxy~call(a))   -- make Rexx object available to Java
     else
         tmpArr~append(a)
  end

  arr=.array~of('createArrayOf')~~appendAll(tmpArr)
  return bsf.wrap( .routines~xBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ================================================================================================= */
::ROUTINE bsf.createJavaArray                public         -- rgf, 2009-09-10
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  arr=.array~of('createJavaArray')~~appendAll(arg(1,'A'))
  return bsf.wrap( .routines~xBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ================================================================================================= */
/* Create single dimensioned Java array, taking the first argument as componentType
   (either a Java class object or as a fully qualified Java class name). All (optional)
   following arguments are being taking
*/
::ROUTINE bsf.createJavaArrayOf              public -- rgf, 2009-09-10
  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  bUnoProxy=.uno_proxy~isA(.class)     -- is UNO (AOO) support loaded?
  tmpArr=.array~new
  rBsfCreateRexxProxy=.routines~xBsfCreateRexxProxy   -- fetch routine Object
  do i=1 to arg()
     a=arg(i)
      -- make sure we leave .UNO_PROXY objects unwrapped
     if bUnoProxy then aBSF=a~isA(.uno_proxy)
                  else aBSF=.false

     if i>1, \a~isA(.string), \a~isA(.bsf), \aBSF, a<>.nil then
         tmpArr~append(rBsfCreateRexxProxy~call(a))   -- make Rexx object available to Java
     else
         tmpArr~append(a)
  end

  arr=.array~of('createJavaArrayOf')~~appendAll(tmpArr)
  return bsf.wrap( .routines~xBSF~callWith(arr))

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ================================================================================================= */
/* Routine for subfunction "version", rgf, 2008-07-09 */
::routine bsf.version public
   use strict arg kind=""

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if kind="" then
     return .routines~xBSF~call( "version")~substr(4)
  else
     return .routines~xBSF~call( "version", kind)~substr(4)

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate


/* ================================================================================================= */
/* Routine for subfunction "haltThread", rgf, 2009-04-04 */
::routine bsf.haltThread public
   use strict arg tid

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return .routines~xBSF~call("haltThread", tid)~substr(4) -- remove type indicator

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate



/* ================================================================================================= */
/* 2010-02-20: allow testing whether a Java object is an instance of a given
               Java class (either the class object or a string); returns
               .true (1) or .false (0)
*/
::routine bsf.isA public
  use strict arg javaObject, javaClass

   signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  return .routines~xBSF~call("isA", javaObject, javaClass)~substr(4)   -- remove type indicator

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate



/* ================================================================================================= */
/* 2010-05-11: extract and return slotDir argument, which is always the last argument in
               the supplied arg-array; this way it is easy to get the slotDir back in
               a one liner
*/
::routine bsf.getSlotDir public
  use strict arg argArray

  signal on syntax
  slotDir=argArray~at(argArray~last)   -- slotDir is always last item in argument array
  if \slotDir~isA(.slotArgument) then
     raise syntax 88.900 array ('Last item in "argumentArray" must be of type ""SlotArgument"" (a Directory or a StringTable), found: "'slotDir'"')
  return slotDir

syntax:
  raise propagate



/* ================================================================================================= */
   /* Create and return an Object Rexx directory representing all static fields of the
    * given Java class or Java interface.
   */
::routine bsf.wrapStaticFields public
  parse arg JavaClassName, bNoCache
  return .JavaStaticFields.Directory~new(JavaClassName, bNoCache)



/* ================================================================================================= */
/* ================================================================================================= */
/*  This allows to wrap any Java class or interface as a directory of fields. This way
    Java field constants can be easily addressed/used via BSF. Any looked up Java constant
    field will be stored in the directory, speeding up future look-ups.
*/
::class  "JavaStaticFields.Directory"  subclass directory

/* ------------------------------------------------------------------------------------------------- */
::METHOD init           guarded CLASS
  EXPOSE rBSF
   -- get the routine objects (of the native routines) and cache them in class attributes; rgf, 20180224
  rBSF               =.routines~xBSF

/* ------------------------------------------------------------------------------------------------- */
::ATTRIBUTE rBSF        guarded CLASS

/* ------------------------------------------------------------------------------------------------- */
::method JavaClassName   guarded attribute -- memorize name of Java class
/* ------------------------------------------------------------------------------------------------- */
::method JavaClassObject guarded attribute -- memorize Java class object
/* ------------------------------------------------------------------------------------------------- */
::method bUseCache       guarded attribute -- memorize Java class object
/* ------------------------------------------------------------------------------------------------- */
::method init            guarded
  parse arg JavaClassName, bNoCache

  self~bUseCache= (bNoCache<>.false)    -- default to use ooRexx directory as cache

  if .environment~hasentry(JavaClassName) then        -- if Java class exists already in the local environment, use it
     self~JavaClassObject=.environment~entry(JavaClassName)
  else                                          -- load Java class and save the class object (string reference)
     self~JavaClassObject=bsf.loadClass(JavaClassName)

  self~JavaClassName=JavaClassName        -- save the Java class name


/* ------------------------------------------------------------------------------------------------- */
  -- this method will be used to deal with UNKNOWN messages; it will retrieve the unknown message name
  -- as a fieldName into the Java class object stored with this class
::method unknown private  unguarded
  expose JavaClassObject bUseCache
  parse arg fieldName .

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old

  isPutMessage=(right(fieldName,1)='=')
  if isPutMessage then              -- put message
  do
     parse arg fieldName '='        -- get fieldName only
     newVal=arg(2)[1]               -- second argument (an array), first entry
     val=bsf.wrap(self~class~rBsf~call("setFieldValue", JavaClassObject, fieldName, newVal))
     self~setentry(fieldName, newVal)  -- save the value in the directory (= update cache)
     return newVal                  -- return the looked up value
  end
  else   -- look up the Java class for the appropriate field value;
  do
     if bUseCache & self~hasEntry(fieldName) then  -- already in directory, if so return cached value
     do
        return self~entry(fieldName)
     end

         -- not yet cached, save it in directory object
     val=bsf.wrap(self~class~rBsf~call("getStaticValue", JavaClassObject, fieldName))
     self~setentry(fieldName, val)  -- save the value in the directory
     return val                     -- return the looked up value
  end
  return

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side


/* ------------------------------------------------------------------------------------------------- */
::method "[]"            guarded            -- getter method
  forward message ("AT")

::method AT              guarded            -- get static field, honor case of letters
  expose JavaClassObject bUseCache
  parse arg fieldName

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  if bUseCache & self~hasindex(fieldName) then  -- already in directory, if so return cached value
  do
     return self~at:super(fieldName)
  end
      -- not yet cached, save it in directory object
  val=bsf.wrap(self~class~rBsf~call("getStaticValueStrict", JavaClassObject, fieldName))
  self~put:super(val, fieldName)    -- save the value in the directory

  return val                        -- return the looked up value

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ------------------------------------------------------------------------------------------------- */
::method "[]="           guarded            -- setter method
  forward message ("PUT")


/* ------------------------------------------------------------------------------------------------- */
::method entry           guarded            -- getter method, use uppercased name as index
  parse upper arg index
  forward message ("AT") array (index)


/* ------------------------------------------------------------------------------------------------- */
::method PUT             guarded            -- set static field, honor case of letters
  expose JavaClassObject
  use arg newVal, fieldName

  signal on syntax
  .local~remove("BSF_ERROR_MESSAGE")   -- make sure to remove any old
  val=bsf.wrap(self~class~rBsf~call("setFieldValueStrict", JavaClassObject, fieldName, newVal))
  self~put:super(newVal, fieldName) -- save the value in the directory (= update cache)
  return newVal                     -- return the looked up value

syntax:
   /* if a Java error, then the Java error message/stack trace is stored in the
      current scope with a variable named "BSF_ERROR_MESSAGE"; in order to make
      this error message available to the caller it is stored in the .local environment */
  if var("BSF_ERROR_MESSAGE") then  -- was a variable by this name set?
     .local~bsf_error_message=BSF_ERROR_MESSAGE -- save Java error message
  raise propagate  -- raise the exception in caller/invoker on the Rexx side



/* ================================================================================================= */
   /* cheap man's "pretty print": encloses string value in square brackets */
::routine pp   public
  return "[" || arg(1)~string || "]"






/* ================================================================================================= */
/* ================================================================================================= */
/* name:    BSF_UTIL.CLS
   authors: Lee Peedin, Rony G. Flatscher
   date:    2006-03-11
   version: 0.90
   license: CPL 1.0, APL 2.0: (see below)

   defines class "BSF.DIALOG" for easy creating message, option and input dialogs

   usage:   send messages to class object .BSF.DIALOG and the component used will be ".nil",
            else create an instance of this class supplying the component to use for the dialogs
            as its only argument

--------

class method messageBox(...): shows dialog, returns always .nil

   arguments:
      message
      message, title
      message, [title], messageType

--------

class method dialogBox(...): shows dialog, returns the number of the push button pressed (0=first key, ...)
                       or -1, if user closed dialog with other means
   arguments:
      message
      message, title
      message, [title], messageType
      message, [title], [messageType], optionType
      message, [title], [messageType], [optionType], [icon], textOfButtons[, defaultButton]

--------

class method inputBox(...): shows dialog, returns the entered/chosen value or .nil if user closed dialog
                      with other means

   arguments:
      message
      message, defaultValue
      message, [title], messageType
      message, [title], messageType, [icon], textOfOptions[, defaultValue]

--------

where messageType is one of:

      error                   - includes X icon
      information             - includes i icon
      plain                   - no icon included
      question                - includes ? mark icon
      warning                 - includes ! icon

where optionType is one of:

      OkCancel                - includes OK/Cancel buttons
      YesNo                   - includes Yes/No buttons
      YesNoCancel             - includes Yes/No/Cancel buttons
      default                 - includes OK/Cancel, uses messageType "question"


*/

/*


showConfirmDialog
    Used to require confirmation from the user
    Icons vary based on the messageType parameter
    Buttons vary based on the optionType parameter
    Return a value associated with the button selected or the dialog CLOSE button
    Displays in the middle of the users screen

showInputDialog
    Allows the user to enter a single value OR select from a pre-defined array of values
    Icons vary based on the messageType parameter
    Has 2 buttons (OK & Cancel)
    Return the value entered or selected by the user (.nil if Cancel or CLOSE)
    Displays in the middle of the users screen


    Valid messageType parameter

      error                   - includes X icon
      information             - includes i icon
      warning                 - includes ! icon
      question                - includes ? mark icon
      plain                   - no icon included

    Valid optionType parameter
      default                 - includes OK/Cancel, questionType
      OkCancel                - includes OK/Cancel buttons
      YesNo                   - includes Yes/No buttons
      YesNoCancel             - includes Yes/No/Cancel buttons

Button Return Values    -1  Dialog CLOSE
                         0  Yes or OK depending on the optionType
                         1  No
                         2  Cancel
*/

::class bsf.dialog public

/* ------------------------------------------------------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------- */
/* class methods        */
/* ------------------------------------------------------------------------------------------------- */
::method init class      guarded
  expose dlgConstants javaDialogClass defaultInstance
   -- initialize attributes to .nil
  dlgConstants    = .nil
  javaDialogClass = .nil
  defaultInstance = .nil


/* ------------------------------------------------------------------------------------------------- */
::method getMessageType guarded class    -- determine full name of message type using first character
  parse arg messageType .

  idx=pos(left(messageType~translate,1), "EIPQW")
  if idx>0 then                  -- extract full spelling and return it
     return word("error information plain question warning", idx)

  return arg(1)                  -- return unchanged argument


::method setDefaultComponent guarded class
  expose defaultInstance
  defaultInstance=self~new(.nil) -- use .nil as default component


/* ------------------------------------------------------------------------------------------------- */
::method javaDialogClass guarded class   -- the Java class object proxy for creating dialogs
  expose javaDialogClass

  if .nil=javaDialogClass then   -- not yet defined, get reference to it
     javaDialogClass=.bsf~bsf.import('javax.swing.JOptionPane', .nil)

  return javaDialogClass


/* ------------------------------------------------------------------------------------------------- */
::method dlgConstants guarded class      -- defines all the dialog constants needed
  expose dlgConstants

  if .nil=dlgConstants then     -- not yet defined, create it
  do
       /* initialisation needed for this module */
          -- get easy access to dlgConstants
 -- 2016-08-15: TODO (check): with Java 1.8 "bsf.wrapStiticFields" seems to not work w.r.t. fetching integer return values
 --             like "error_message" and the like; using the imported class works though!
 --    o =  bsf.wrapStaticFields('javax.swing.JOptionPane')
     o =  self~javaDialogClass   -- get imported Java class
          -- message types
     dlgConstants = .directory~new
     dlgConstants~error                  = o~error_message
     dlgConstants~error.title            = "Error"

     dlgConstants~information            = o~information_message
     dlgConstants~information.title      = "Information"

     dlgConstants~warning                = o~warning_message
     dlgConstants~warning.title          = "Warning"

     dlgConstants~question               = o~question_message
     dlgConstants~question.title         = "Question"

     dlgConstants~plain                  = o~plain_message
     dlgConstants~plain.title            = ""

       -- dialog option types
     dlgConstants~default                = o~default_option
     dlgConstants~default.buttonText     = "OK Cancel"

     dlgConstants~OkCancel               = o~ok_cancel_option
     dlgConstants~OkCancel.buttonText    = "OK Cancel"

     dlgConstants~YesNo                  = o~yes_no_option
     dlgConstants~YesNo.buttonText       = "Yes No"

     dlgConstants~YesNoCancel            = o~yes_no_cancel_option
     dlgConstants~YesNoCancel.buttonText = "Yes No Cancel"
  end

  return dlgConstants


/* ------------------------------------------------------------------------------------------------- */
::method unknown guarded class     -- forward all unknown class messages to the defaultInstance
  expose defaultInstance
  use arg methodName, arrArgs
-- say "unknown::class: methodName:" pp(methodName) "defaultInstance:" pp(defaultInstance)
  forward message (methodName) to (defaultInstance) arguments (arrArgs)



/* ------------------------------------------------------------------------------------------------- */
/* ------------------------------------------------------------------------------------------------- */
/* instance methods     */
/* ------------------------------------------------------------------------------------------------- */
::method component guarded attribute     -- the Java component for which the dialog should execute; will be modal for it!
/* ------------------------------------------------------------------------------------------------- */
::method init            guarded
  expose component
  use arg component              -- get component and save it


/* ------------------------------------------------------------------------------------------------- */
   -- method to make it simple to invoke a dialog with push buttons as options
   -- implementation uses javax.swing.JOptionPane
::method dialogBox       guarded
  expose component
  use arg message, title, messageType, optionType, icon, textOfButtons, defaultButton

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

  call beep 500, 100    -- beep in an attention tone

  if arg()=1 then       -- only one argument given
  do
     return javaDialogClass~new~showConfirmDialog(component, message)
  end
  else
  do
     -- messageType
     messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
     swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
     if .nil=swingMessageType then
     do
         messageType="plain"
         swingMessageType=dlgConstants~plain   -- default messageType
     end

     -- title omitted, get default title
     if arg(2, 'Omitted') then   -- title not given, use a default one
     do
        title=dlgConstants~entry(messageType~strip".TITLE")
        if title="" then title="Message"  -- plain, but indicate that this is a message!
     end

     -- optionType
     swingOptionType=dlgConstants~entry(optionType)
     if .nil=swingOptionType then
     do
        optionType="default"
        swingOptionType=dlgConstants~default   -- default optionType
     end

     if arg()<5 then    -- no more arguments (strings for buttons, and setting default button)
     do
        return javaDialogClass~new~showConfirmDialog(component,message,title,swingOptionType,swingMessageType)
     end

     -- optional icon
     if arg(5, 'Omitted') then
        icon=.nil

     -- optional button text collection and optional default button
     if arg(6, 'Omitted') then
        textOfButtons=dlgConstants~entry(optionType~space".ButtonText")~makearray(' ')

     javaArr=bsf.createArray(.bsf4rexx~object.class, textOfButtons~items)  -- create Java array of needed size
     i=0
     do entry over textOfButtons -- this way any ooRexx collection object can be used !
        i=i+1
        javaArr[i]=entry
     end

     if arg(7, 'Omitted') then   -- no default button
     do
        defaultButton=.nil
     end

     return javaDialogClass~new~showOptionDialog(component,message,title,swingOptionType,swingMessageType, icon, javaArr, defaultButton)
  end
  return -99  -- return impossible value


/* ------------------------------------------------------------------------------------------------- */
/*
   -- method to make it simple to invoke a dialog with input field or drop-down list to choose from
   -- implementation uses javax.swing.JOptionPane

   test ".bsfdialog~inputBox(...):'
       (message                                                             )
       (message, defaultValue                                               )
       (message, [title], messageType                                       )
       (message, [title], messageType, [icon], textOfOptions[, defaultValue])

        ... method returns the entered/chosen value or .nil (user closed dialog)
 */
::method inputBox        guarded
  expose component
  nrArgs=arg()          -- get number of arguments

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

  if nrArgs=1 then       -- only one argument given
  do
     use arg message
     return javaDialogClass~new~showInputDialog(component, message)
  end
  else if nrArgs=2 then
  do
     use arg message, defaultValue
     return javaDialogClass~new~showInputDialog(component, message, defaultValue)
  end


  use arg message, title, messageType, icon=.nil, textOfOptions, defaultValue
  -- messageType
  messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
  swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
  if .nil=swingMessageType then
  do
      messageType="plain"
      swingMessageType=dlgConstants~plain   -- default messageType
  end

  -- title omitted, get default title
  if arg(2, 'Omitted') then   -- title not given, use a default one
  do
     title=dlgConstants~entry(messageType~strip".TITLE")
     if title="" then title="Message"  -- plain, but indicate that this is a message!
  end


  if nrArgs=3 then
  do
     return javaDialogClass~new~showInputDialog(component, message, title, swingMessageType)
  end

  -- optional icon
  if arg(4, 'Omitted') then
     icon=.nil

  -- option collection

  -- if textOptions~isA(.BSF_ARRAY_REFERENCE) | textOptions~isA(.UNO_ARRAY_PROXY) then
  if textOptions~isA(.BSF_ARRAY_REFERENCE) then
  do
     javaArr=textOptions      /* already a Java array in hands, just pass it through */
  end
     -- determine whether we have UNO (OpenOffice) support available and if so, test for it
  else if .uno_array_proxy~isA(.class), textOptions~isA(.uno_array_proxy) then
  do
     javaArr=textOptions      /* already a Java array in hands, just pass it through */
  end
  else
  do
     if arg(5)~hasmethod("PUT")=.false then -- not a collection ?
     do
        textOfOptions=space(textOfOptions)~makearray(' ')
     end

     -- optional button text collection and optional default button
     javaArr=bsf.createArray(.bsf4rexx~object.class, textOfOptions~items)  -- create Java array of needed size
     i=0
     do entry over textOfOptions -- this way any ooRexx collection object can be used !
        i=i+1
        javaArr[i]=entry
     end
  end

  if arg(6, 'Omitted') then   -- no default button
  do
     defaultValue=.nil
  end

  return javaDialogClass~new~showInputDialog(component,message,title,swingMessageType, icon, javaArr, defaultValue)


/* ------------------------------------------------------------------------------------------------- */
   -- class method to make it simple to invoke the desired functionality
   -- implementation uses javax.swing.JOptionPane
::method messageBox      guarded
  expose component
  use arg message, title, messageType

  selfClz=self~class
  dlgConstants    = selfClz~dlgConstants     -- get dlgConstants
  javaDialogClass = selfClz~javaDialogClass  -- get Java class object

  call beep 1500, 100            -- beep in an attention tone
  if arg()=1 then       -- only one argument given
  do
     javaDialogClass~new~showMessageDialog(component, message)
  end
  else
  do
     -- default to plain message box
     messageType=selfClz~getMessageType(messageType)  -- make sure we have correct spelling
     if dlgConstants~hasentry(messageType)=.false then
     do
        messageType="plain"
     end

     swingMessageType=dlgConstants~entry(messageType) -- get the Java encoding of message type
     -- title omitted, get default title
     if arg(2, 'Omitted') then   -- title not given, use a default one
     do
        title=dlgConstants~entry(messageType~strip".TITLE")
        if title="" then title="Message"  -- plain, but indicate that this is a message!
     end

     javaDialogClass~new~showMessageDialog(component, message, title, swingMessageType)
  end
  return .nil     -- message box returns always .nil




/* ================================================================================================= */
/* ================================================================================================= */
/** This routine will create a new Java class on the fly, which extends the supplied Java class
*   and implements the supplied methodNames. The returned Java class implements all constructors
*   of the base class and for each a version which accepts as its first parameter a
*   <code>RexxProxy</code> object which gets the Java method invocations forwarded as ooRexx
*   messages with all supplied Java arguments plus a trailing ooRexx <code>.Directory</code>
*   argument containing some useful invocation details.
*
*   <br/>
*   If the <code>RexxProxy</code> object needs to invoke a masked
*   method (a method the extended Java class implements and forwards to the <code>RexxProxy</code>)
*   then it merely forwards the invocation to <code>self</code>, appending to the method name the
*   string &quot;_forwardToSuper&quot; and supplying all parameters the method mandates.
*
*   <br/>The extended class possesses the static methods <code>getDefaultHandler()</code> and
*    <code>setDefaultHandler(RexxProxy)</code>. The <em>defaultHandler</em> is a <code>RexxProxy</code>
*    which receives ooRexx messages for every static Java method that gets invoked in the extended
*    class.
*
*   <br/>The extended class possesses the instance methods <code>getTargetRexxProxy()</code> and
*    <code>setTargetRexxProxy(RexxProxy)</code>. The <em>DefaultHandler</em> is a <code>RexxProxy</code>
*    which receives ooRexx messages for every static Java method that gets invoked in the extended
*    class. It is set with the extended constructors that expect a <code>RexxProxy</code> as the
*    first argument. If the class' <em>defaultHandler</em> is not set yet, then the extended constructor's
*    code will set it to the <em>targetRexxProxy</em> automatically.
*
* @param javaClzName the fully qualified Java class name to be extended, either a string or a Java class object.
         If the argument is a string, then it can be optionally followed by blank-delimited Java interface names
         that the new class would implement as well and get serviced by the <code>RexxProxy</code> handling
         the Java method invocations. Alternatively this can be an array where each array element is either
         a string fully qualifying the Java class or a Java class object directly representing the class.

* @param newName the optional name of the newly created Java class, if omitted, a synthetic name
*        will be created, containing the string &quot;<em>visitorName</em>_$RexxExtendClass$_<em>someHashcode</em>&quot;

* @param methName... optional, where this and all following arguments denote a list of single Java method names or
*        a single argument representing an empty string the asterisk (*) character or a Rexx array (or Java array of type
*        <code>java.lang.String</code>) where each element denotes the Java method name
*        the Rexx proxy needs to implement; optionally, each Java method name may be preceded by
*        the Java class name and a blank. Please note: all public/protected abstract methods and
*        constructors will always get proxied! <br/>

*          The following special cases apply for this argument:
*          <ul>
*          <li>if this argument is <code>.nil</code>, then only the public/protected abstract methods and
*              constructors get proxied.
*          </li>
*
*          <li>if this argument is a single empty string, then only the public/protected abstract methods
*              and constructors get proxied.
*          </li>
*
*          <li>if this argument has a single element with the value <code>&quote;*&quote;</code> (asterisk), then
*              all public/protected methods and constructors get proxied.
*          </li>
*          </ul>


*        Please note:
         <ul>
         <li>
        if this argument is omitted or <code>.nil</code> then only the public or protected

then all declared public and protected methods of the given class will be proxied; if single
*        empty string <code>&quot;&quot;</code> is given, then an empty (size of 0) Java String array
*        is created and supplied, causing only the public and protected constructors to be proxied

*        <br>Warning: if you proxy the methods <code>toString</code>, <code>equals</code> and <code>hashCode</code> be aware that a wrong
*        implementation on your side may break the program. For that reason these methods are never
*        automatically proxied (if you leave out this argument).
*
* @return Java class object that is able to delegate its method invocations to a <code>RexxProxy</code> object
*
* @since 2010-04-18, 2014-08-31, 2017-03-09
*/

-- class name can be followed with blank-delimited interface class names that also are regarded to be implemented
::routine bsf.createProxyClass public
   use strict arg javaClzToProxy, newName=.nil, methName="", ...

   signal on syntax

      -- get the Java class that controls the creation of the extended Java class
   clzName="org.rexxla.bsf.engines.rexx.ClassAdapter"
   jPJCT=.bsf4rexx~entry(clzName)
   if jPJCT=.nil then
   do
       jPJCT=bsf.loadClass(clzName)
       .bsf4rexx~setEntry(clzName, jPJCT)
   end

   arrInterfaceClasses=.nil   -- if interface classes are contained in first argument, this variable will be used for them
   if javaClzToProxy~isA(.string) then
   do
      -- make an array of classes, could be that optionally blank delimited interface classes follow
      allClasses=javaClzToProxy~makeArray(" ")
      items=allClasses~items
      javaClzToProxy=bsf.loadClass(allClasses[1])  -- load Java class
      if items>1 then   -- interface classes supplied too?
      do
         arrInterfaceClasses=bsf.createJavaArray("java.lang.Class", items-1)
         loop i=2 to allClasses~items
            tmpClz=bsf.loadClass(allClasses[i])    -- load the Java interface class
            if tmpClz~isInterface<>.true then      -- not a Java interface class!
            do
               raise syntax 40.900 array ("BSF.CLS/routine/bsf.createProxyClass(): error 1 in argument 1: the additional supplied interface class" pp(allClasses[i]) "is not a Java interface class")
            end
            arrInterfaceClasses[i-1]=tmpClz        -- save interface class object
         end
      end
   end
   else if javaClzToProxy~isA(.array) then      -- assuming an array of strings denoting the Java classes or Java class objects
   do
       ifItems=javaClzToProxy~items-1
       arrInterfaceClasses=bsf.createJavaArray("java.lang.Class", javaClzToProxy~items-1)
       loop i=2 to javaClzToProxy~items         -- create array of Java interface classes to proxy
          tmpClz=javaClzToProxy[i]              -- get entry
          if tmpClz~isA(.string) then           -- a string?
             tmpClz=bsf.loadClass(tmpClz)   -- load the Java interface class

          if tmpClz~isInterface<>.true then     -- not a Java interface class!
          do
             raise syntax 40.900 array ("BSF.CLS/routine/bsf.createProxyClass(): error 2 in argument 1: the additional supplied interface class" pp(allClasses[i]) "at index" pp(i) "is not a Java interface class")
          end
          arrInterfaceClasses[i-1]=tmpClz       -- save interface class object
       end

       javaClzToProxy=javaClzToProxy[1]         -- now replace the class to be extended with first entry in the supplied array
       if javaClzToProxy~isA(.string) then      -- make sure we have a Java object in hand
          javaClzToProxy=bsf.loadClass(javaClzToProxy)   -- load the Java class
   end

   if methName="", arg()<4 then     -- if only an empty method given, turn argument to empty Java String array
      methName=bsf.createJavaArray("java.lang.String", 0)

   if methName<>.nil then
   do
      if \methName~isA(.bsf_array_reference) then   -- not a Java array ?
      do
         if arg()=3, methName~isA(.array) then workArr=methName
                                          else workArr=arg(3,"array")
         javaArr=bsf.createJavaArray("java.lang.String", workArr~items)
         do i=1 to workArr~items
            javaArr[i]=workArr[i]
         end
         methName=javaArr           -- replace argument(s) with Java String array of methods
      end
   end

   newProxyClz=jPJCT~createProxiedJavaClass(javaClzToProxy, newName, methName, arrInterfaceClasses)

   -- now add ooRexx' new() and newStrict() class methods to it
   newProxyClz=.bsf~bsf.import(newProxyClz~getName, .nil, newProxyClz)
   return newProxyClz   -- return the new extended proxy Java class object

syntax: raise propagate



/* ================================================================================================= */
/* rgf, 2012-02-11: add, but do not document as of yet; originally developped for the
                    DBUS4ooRexx package which mandates UTF-8 strings (and checks them) */
   /* ---------------------------------------------------------------------------------------- */
   /* as of 2011-07-05 ooRexx from trunk does not support UTF-8, hence depending on BSF4ooRexx */
/** The received Rexx string is rendered to a Java String object using the 'file.encoding' codepage
*   and then translated to an UTF-8 encoded Rexx string.
*
* @param str the Rexx string encoded according to the 'file.encoding's codepage
* @return the equivalent UTF-8-encoded Rexx string
* @since 2012-02-11
*/
::routine bsf.stringToUtf8 public    -- needs BSF4ooRexx
  parse arg str
  return BsfRawBytes(.java.lang.String~new(str)~getBytes("UTF-8"))


/* ================================================================================================= */
   /* ---------------------------------------------------------------------------------------- */
/** The received Java object's string value is rendered to a Rexx string according to 'file.encoding's
*   codepage.
*
* @param obj the Java String object (JSO) or a Java object which string value is retrieved and worked upon,
*            or an UTF-8 encoded Rexx string
* @return a Rexx string encoded according to 'file.encoding's codepage
* @since 2012-02-11
*/
::routine bsf.utf8ToString public    -- needs BSF4ooRexx
  use arg str

  if str~isA(.bsf) then    -- any Java object, use its String value
     return str~toString   -- this will do the translation from UTF-8 to file.encoding codepage
  else   -- a Rexx string? -- turn UTF-8 encoded Rexx string into Java string, recode the Java string using 'file.encoding's codepage and return it as a Rexx string
     return BsfRawBytes(.java.lang.String~new(bsfRawBytes(str),"UTF-8")~getBytes)


/* ================================================================================================= */
   /* ---------------------------------------------------------------------------------------- */
/** The received Rexx string consists of UTF-8 characters for which a Java string object gets
*  created and returned, independent of the file.encoding codepage setting.
*
* @param str the Rexx string consisting of UTF-8 characters
* @return a reference to the Java String object constructed from the Rexx string
* @since 2014-10-07
*/
::routine bsf.utf8stringAsJSO public
   parse arg str
   return .java.lang.String~new(BSFRawBytes(str),"UTF-8")


/* ================================================================================================= */
   /* ---------------------------------------------------------------------------------------- */
/** The received Java String object (JSO) is rendered as an UTF-8 Rexx string, independent
*   of the file.encoding codepage setting.
* @param obj the Java String object reference or a Java object which String value is retrieved and worked upon
* @return a Rexx UTF-8 encoded string constructed from the Java String object
* @since 2014-10-07
*/
::routine bsf.JSOasUtf8string public
   use arg obj

   if obj~isA(.bsf) then      -- a Rexx proxy (to a Java object) in hand?
   do
      if obj~bsf.isA(.java.lang.String)=.false then   -- need to get String value from object?
         obj=obj~bsf.invokeJSO('toString')            -- fetch String value as a Java String object (JSO)

      return BSFRawBytes(obj~getBytes("UTF-8"))       -- render String value to a Rexx string and return it
   end

   raise syntax 40.900 array ("BSF.CLS/routine/bsf.JSOasUtf8string(): wrong argument, must be a Java String object (JSO) or a Java object which String value (result of sending it the 'toString' message) will be processed upon. Instead, received an ooRexx object.")

syntax:
   raise propagate


/* ================================================================================================= */
   /* ---------------------------------------------------------------------------------------- */
   -- as of ooRexx 4.1.1 the following scenario may occur: Rexx starts Java and Java calls
   -- back into ooRexx; this may take place even after the Rexx program ended and as a result the
   -- Rexx interpreter shuts down; a callback in that situation will cause a crash!
   -- Unfortunately, there is no way to tell currently whether the Rexx interpreter can be
   -- safely used from within native code.
   -- Hence this workaround: if Java was loaded by Rexx, then this routine will terminate
   -- the Java RexxEngine, such that the Java side will become able to realize that callbacks
   -- to Rexx are not safe anymore
::routine BSF.terminateRexxEngine public
  re=bsf.wrap(.routines~xBsf~call('getRexxEngine'))
  re~terminate                -- terminate Rexx interpreter instance to inhibit callbacks from Java

  signal on syntax            -- if terminated, we now get a syntax error by BSF()
  return re~isTerminated      -- indicate whether terminating the Rexx engine was successful
syntax:
  return .true                -- terminated successfully



/* ================================================================================================= */
/* rgf, 2014-07-06 */

/** Routine to allow redirecting ooRexx standard monitors (.input, .output, .error,
   since ooRexx 4.2 in addition: .debugInput, .traceOutput) to Java input and output
   streams and resetting to default ooRexx streams, if either first argument is not "J"ava
   (redirecting to the Java standard files) or if no arguments are given altogether.
   Each BSF.InputStream and BSF.OutputStream object will get their "prefix" attribute
   defined to prepend an informative string, such that the user is able to distinguish
   the Rexx output or Rexx input from Java.

   The prefix values for the redirected Java streams that are monitored by .input, .output,
   .error, .debugInput and .traceOutput are "REXXin?>", "REXXout>", "REXXerr>",
   "REXXdbgIn?>" and "REXXtrc>" respectively.
   To inhibit the prefix to be output, just fetch the monitored stream objects and set the
   prefix to .nil. To change the prefix values, just fetch the monitored stream objects and
   set the prefix to any other string.

   @param bRedirectTo  optional, "J"ava (default): redirect ooRexx monitors to
                          Java's standard (System) streams;
                          otherwise reset to standard ooRexx streams
   @param input        optional, stream to redirect .input monitor to
   @param output       optional, stream to redirect .output monitor to
   @param error        optional, stream to redirect .error monitor to
   @param debugInput   optional, stream to redirect .debugInput monitor to (defaults to .input~current)
   @param traceOutput  optional, stream to redirect .traceOutput monitor to (defaults to .error~current)
*/

::routine bsf.redirectTo public
  use strict arg redirectTo="J", input=.nil, output=.nil, error=.nil, debugInput=.nil, traceOutput=.nil

  bRedirectToJava=redirectTo~strip~left(1)~caselessEquals("J")

   ---------------------- .input ----------------------
  if Input <>.nil then           -- object for input monitor supplied
  do
     if bRedirectToJava then     -- make sure we wrap the Java object
     do
        if input~isA(.bsf.inputStream) then  -- already wrapped
           .input~destination(input)
        else                     -- wrap Java input object
           .input~destination(.bsf.inputStream~new(input,"REXXin?>"))
     end
     else   -- redirect to Rexx object, whatever it is
       .input~destination(input)
  end
  else   -- no input object supplied
  do
     prev=.input~current    -- get current monitored object
     if .input~destination=.nil then   -- remove current montitored object, if none left, add it back
        .input~destination(prev)

     if bRedirectToJava=.true then     -- if no input Java object supplied, default to System's in object
        .input~destination(.bsf.InputStream~new(.java.lang.System~in,"REXXin?>"))
     else
        .input~destination(.stdin)
  end

   ---------------------- .output ----------------------
  if Output <>.nil then
  do
     if bRedirectToJava then     -- make sure we wrap the Java object
     do
        if output~isA(.bsf.outputStream) then  -- already wrapped
           .output~destination(output)
        else                     -- wrap Java output object
           .output~destination(.bsf.outputStream~new(output,"REXXout>"))
     end
     else   -- redirect to Rexx object, whatever it is
       .output~destination(output)
  end
  else   -- no output object supplied
  do
     prev=.output~current    -- get current monitored object
     if .output~destination=.nil then  -- remove current montitored object, if none left, add it back
        .output~destination(prev)

     if bRedirectToJava=.true then     -- if no output Java object supplied, default to System's out object
        .Output~destination(.bsf.outputStream~new(.java.lang.System~out,"REXXout>"))
     else
        .Output~destination(.stdout)
  end

   ---------------------- .error ----------------------
  if Error <>.nil then
  do
     if bRedirectToJava then     -- make sure we wrap the Java object
     do
        if error~isA(.bsf.outputStream) then  -- already wrapped
        do
           .error~destination(error)
           setErrorPrefix=.false -- do not change prefix setting
        end
        else                     -- wrap Java output object
           .error~destination(.bsf.outputStream~new(error,"REXXerr>"))
     end
     else   -- redirect to Rexx object, whatever it is
       .error~destination(error)
  end
  else   -- no error object supplied
  do
    prev=.error~current    -- get current monitored object
    if .error~destination=.nil then    -- remove current montitored object, if none left, add it back
       .error~destination(prev)

    if bRedirectToJava=.true then      -- if no error Java object supplied, default to System's err object
       .Error~destination(.bsf.outputStream~new(.java.lang.System~err,"REXXerr>"))
    else
       .Error~destination(.stderr)
  end

   ---------------------- .debugInput ----------------------
  if .local~hasEntry("DebugInput") then   -- ooRexx<4.2 does not have this
  do
     If DebugInput <>.nil then
     do
        if bRedirectToJava then     -- make sure we wrap the Java object
        do
           if debugInput~isA(.bsf.inputStream) then  -- already wrapped
              .debugInput~destination(debugInput)
           else                     -- wrap Java input object
              .debugInput~destination(.bsf.inputStream~new(debugInput,"REXXdbgIn?>"))
        end
        else   -- redirect to Rexx object, whatever it is
          .debugInput~destination(debugInput)
     end
     else   -- no debugInput object supplied
     do
        prev=.debugInput~current    -- get current monitored object
        if .debugInput~destination=.nil then    -- remove current montitored object, if none left, add it back
           .debugInput~destination(prev)

        if bRedirectToJava then  -- create a new BSF.InputStream instance in order to distinguish both streams (e.g. for prefixing correctly)
        do
            -- create a new .BSF.InputStream working with the same Java stream object
            .debugInput~destination(.bsf.inputStream~new(.input~javaObject,"REXXdbgIn?>"))
        end
        else
           .DebugInput~destination(.input)    -- redirect to .input object, whatever it is
     end
  end

   ---------------------- .traceOutput ----------------------
  if .local~hasEntry("TraceOutput") then   -- ooRexx<4.2 does not have this
  do
     prev=.traceOutput~current    -- get current monitored object
     if .traceOutput~destination=.nil then    -- remove current montitored object, if none left, add it back
        .traceOutput~destination(prev)

     if TraceOutput <>.nil then
     do
        if bRedirectToJava then     -- make sure we wrap the Java object, if needed
        do
           if traceOutput~isA(.bsf.outputStream) then  -- already wrapped
              .traceOutput~destination(traceOutput)
           else                     -- wrap Java output object
              .traceOutput~destination(.bsf.outputStream~new(traceOutput,"REXXtrc>"))
        end
        else   -- redirect to Rexx object, whatever it is
          .traceOutput~destination(traceOutput)
     end
     else   -- no traceOutput object supplied
     do
        if bRedirectToJava then  -- create a new BSF.OutputStream instance in order to distinguish both streams (e.g. for prefixing correctly)
        do
           .traceOutput~destination(.bsf.outputStream~new(.error~javaObject,"REXXtrc>"))
        end
        else
           .TraceOutput~destination(.error)  -- redirect to .error object, whatever it is
     end
  end


/* ================================================================================================= */
/* ================================================================================================= */
/* Allows to use a java.io.InputStream or a java.io.Reader object as an ooRexx input stream. */
::class "BSF.InputStream"  subclass inputStream  public
/* ------------------------------------------------------------------------------------------------- */
::attribute javaObject  guarded        -- allow direct access to input stream object
/* ------------------------------------------------------------------------------------------------- */
::attribute isReader    guarded             -- allow direct access
/* ------------------------------------------------------------------------------------------------- */
::attribute prefix      guarded             -- prefix string to be output before executing a LINEIN, writes directly to System.err to bypass outputting any other prefixes

::method string          guarded
  expose javaObject isReader prefix
  return "BSF.InputStream[javaObject="javaObject~objectName         || -
                          ";isReader="isReader                      || -
                          ";prefix="prefix"]"


/* ------------------------------------------------------------------------------------------------- */
::method init            guarded            -- constructor that takes the Java InputStream object
  expose javaObject byteArray1 charArray1 isReader prefix
  use strict arg javaObject, prefix=.nil
  byteArray1=bsf.createJavaArray("byte.class",1)   -- Java byte array with capacity of one
  charArray1=bsf.createJavaArray("char.class",1)   -- Java char array with capacity of one

  javaClassName="java.io.Reader"
  if \.environment~hasEntry(javaClassName) then
     call bsf.loadClass javaClassName, javaClassName  -- load Java class and store it as "JAVA.IO.READER" in .local
  isReader=.java.io.Reader~isInstance(javaObject)


/* ------------------------------------------------------------------------------------------------- */
::method charIn          guarded
  expose javaObject byteArray1 charArray1 isReader
  signal on syntax name wrong_args
  use strict arg start=1, length=1
  signal off syntax

  if start>1 then
  do
     signal on syntax               -- if empty string, then InputStream raises an "invalid handle" exception
     javaObject~skip(start-1)
  end

  if isReader=.false then
  do
     if length=1 then
        barr=byteArray1
     else
        barr=bsf.createJavaArray("byte.class", length)

     n=javaObject~read(barr,0,length)
     if n=0 then return ""
     return BSFRawBytes(barr,n)
  end

  if length=1 then
     charr=charArray1
  else
     charr=bsf.createJavaArray("char.class", length)

  n=javaObject~read(charr, 0, length)

  if n<=0 then return ""
  return .java.lang.String~new(charr, 0, n)~toString

syntax:
   return ""

wrong_args:
   raise propagate


/* ------------------------------------------------------------------------------------------------- */
::method chars           guarded
  expose javaObject isReader
  if isReader then
     return javaObject~ready
  else   -- assume an InputStream (e.g. java.lang.System.in)
     return javaObject~available


/* ------------------------------------------------------------------------------------------------- */
::method lineIn          guarded
   expose prefix

   if prefix<>.nil then       -- write a prefix string to inform the user ?
      .output~charout(prefix,,.true)   -- third argument: if .true, then prefix will not be inserted in .output

   mb=.mutableBuffer~new
   do forever
      char=self~charin            -- read a byte

      if char="0d"x then          -- skip CR
         iterate

      if char="0a"x | char=="" then   -- received LF or nothing (end of input), return chars gathered so far
         return mb~string         -- return collected characters

      mb~append(char)
   end


/* ================================================================================================= */
/* ================================================================================================= */
/* Allows to use a java.io.PrintStream or a java.io.Writer object as an ooRexx output stream. */
::class "BSF.OutputStream" subclass outputStream public
/* ------------------------------------------------------------------------------------------------- */
::attribute javaObject   guarded      -- allow direct access to output stream/writer object
/* ------------------------------------------------------------------------------------------------- */
::attribute isWriter     guarded      -- allow direct access
/* ------------------------------------------------------------------------------------------------- */
::attribute prefix       guarded      -- default: .nil, prefix string to be output before executing a LINEOUT
/* ------------------------------------------------------------------------------------------------- */
::attribute endOfLine    guarded      -- allows setting explicitly end-of-line charactesrs, defaults to .endofline
/* ------------------------------------------------------------------------------------------------- */
::attribute doFlush      guarded      -- default: .true, allows setting whether we do the flush (a no go in JSPs as 'flush' commits the 'request'!

::method string          guarded
  expose javaObject isWriter prefix endOfLine doFlush
  return "BSF.OutputStream[javaObject="javaObject~objectName      || -
                          ";isWriter="isWriter                    || -
                          ";prefix="prefix                        || -
                          ";endOfLine=" || ('"'endOfLine~c2x'"x') || -
                          ";doFlush="doFlush"]"

/* ------------------------------------------------------------------------------------------------- */
/** Constructor.
*   @param javaObject a Java <code>Stream</code> or <code>Writer</code> object to use for output
*   @param prefix the string to print at the beginning of an output line, defaults to <code>.nil</code>
*   @param endofLine the string to use as end-of-line sequence, defaults to <code>.endOfLine</code>
*/
::method init            guarded
  expose javaObject isWriter prefix bPrefixPrinted endOfLine doFlush
  signal on syntax
  use strict arg javaObject, prefix=.nil, endOfLine=.endOfLine, doFlush=.true

  javaClassName="java.io.Writer"
  if \.environment~hasEntry(javaClassName) then
     call bsf.loadClass javaClassName, javaClassName  -- load Java class and store it as "JAVA.IO.WRITER" in environment

  isWriter=.java.io.Writer~isInstance(javaObject)
  bPrefixPrinted=.false             -- indicates whether the prefix was printed already (in charout-scenarios)
  return
syntax:
  raise propagate


/* ------------------------------------------------------------------------------------------------- */
::method charOut         guarded
  expose javaObject isWriter prefix bPrefixPrinted doFlush
  signal on syntax
  use strict arg chars, start=1, bSkipPrefix=.false

  if start=1 then
     mb=.MutableBuffer~new(chars)   -- get the string as a MutableBuffer
  else
     mb=.MutableBuffer~new(chars~subStr(start))

  if prefix<>.nil then              -- if a prefix present
  do
     if bPrefixPrinted=.false, bSkipPrefix=.false then  -- if not yet output
     do
        if isWriter then
           javaObject~write(prefix)
        else
           javaObject~print(prefix)

        bPrefixPrinted=.true              -- on subsequent charOuts do not write prefix anymore
     end

     mb~changeStr("0A"x, "0A"x||prefix)   -- add the prefix after the LF char
  end

   -- output string
  if isWriter then
     javaObject~write(mb~string)
  else
     javaObject~print(mb~string)

  if doFlush=.true then
     javaObject~flush         -- make sure buffer gets flushed (otherwise we may not see any output)
  return
syntax:
  raise propagate


/* ------------------------------------------------------------------------------------------------- */
::method lineOut     guarded -- a true synonym for SAY in this implementation (even if no data to display are supplied)
  expose javaObject isWriter prefix bPrefixPrinted endOfLine doFlush
  use strict arg chars=""

  mb=.MutableBuffer~new(chars)

  if prefix<>.nil then
  do
     if bPrefixPrinted=.false then
        mb~insert(prefix)     -- insert prefix at the beginning of the string

      -- add prefix after LF char
     mb~changeStr("0A"x, "0A"x||prefix)   -- add the prefix after the LF char
  end

  if isWriter then
  do
     javaObject~write(mb~string)
     javaObject~write(endofline)
  end
  else
  do
     javaObject~println(mb~string)
  end

  if doFlush=.true then
     javaObject~flush         -- make sure buffer gets flushed (otherwise we may not see any output)
  bPrefixPrinted=.false          -- a new line for which the prefix was not printed yet


::method say      guarded -- in ooRexx file streams a synonym for lineOut
  forward message "lineout"



/* ================================================================================================= */

/** A utility class to ease JSR-223 (javax.script) related functionality.
*/
::class JSR223 public

/* ------------------------------------------------------------------------------------------------- */
::constant ENGINE_SCOPE 100   -- numeric value for javax.script.ScriptContext.ENGINE_SCOPE
/* ------------------------------------------------------------------------------------------------- */
::constant GLOBAL_SCOPE 200   -- numeric value for javax.script.ScriptContext.GLOBAL_SCOPE

/* ------------------------------------------------------------------------------------------------- */
::attribute showWarnings guarded class   -- .true or .false
/* ------------------------------------------------------------------------------------------------- */
::attribute showDebugs   guarded class   -- .true or .false

/* ------------------------------------------------------------------------------------------------- */
::method init guarded class
  expose tableOut tableIn showWarnings
   -- define input and output table for TRANSLATE()-BIF
  tableOut   =".abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?_" -- valid (Rexx symbol) characters
  tableIn    =tableOut || xrange("00"x,"ff"x)
  showWarnings=.true


/* ------------------------------------------------------------------------------------------------- */
/** Class method that returns the requested Binding from the ScriptContext.
*
* @param scriptContext the javax.script.ScriptContext
* @param sc the scope number for the javax.script.Binding to be retrieved from scriptContext
* @return the javax.script.Binding or <code>.nil</code> if not found
*/
::method getBindings guarded class    -- return bindings from ScriptContext, if not found, .nil
  use strict arg scriptContext, sc
  signal on syntax         -- if a scope does not exist, then an IllegalArgumentException gets thrown

  if scriptContext~isA(.RexxContext) then
     scriptContext=scriptContext~args~lastItem~scriptContext

  return scriptContext~getBindings(sc)
syntax:
  return .nil



/* ------------------------------------------------------------------------------------------------- */
/* rgf, 2016-10-26: make sure the entry names can be used as Rexx variable names; if name starts with a dot,
   prepend name with a question mark, otherwise it would be an environment symbol in ooRexx */

/** Returns the normalized (mangled) name for the supplied name. The returned name consists of
*   characters that are valid for Rexx symbols (invalid characters get replaced by a question mark)
*   only and prepends a question mark of the new name would start with a dot or a digit (Rexx
*   variables must not start with a dot or a digit).
*
* @param name the name to be normalized such that it can be used as a Rexx variable name
* @return either the unchanged name, if it can be used as a Rexx variable name already, otherwise the
*         normalized (mangled) form
*/
::method normalizeName guarded class
  expose tableOut tableIn
  parse arg name
  newName=TRANSLATE(name, tableOut, tableIn, '?')~upper -- translate all invalid chars into a question mark

   -- make sure that the Rexx symbol can be used as a Rexx variable for BSFContextVariable(...)
  if pos(newName~left(1), ".0123456789")>0 then
     return "?"newName   -- prepend an underscore

  return newName


/* ------------------------------------------------------------------------------------------------- */
/** Class method that creates and returns a Rexx directory object off the supplied <code>ScriptContext</code>
*   which is meant to be used with the external Rexx function <code>BSFContextVariables()</code> to set
*   and drop context variables. Dots in the Java supplied variable names get replaced by underscores
*   to inhibit the creation of Rexx stem objects.
*
*  @param scriptContext    the Java <code>ScriptContext</code> object containing entries in different
*                          bindings or the caller's .context object which is expected to have
                           slotDir as its last argument that contains the ScriptContext to work with
                           (which is the case, if the caller was invoked by Java via the javax.script framework)

*  @param bMangleName      defaults to .true, which makes sure that each key in the Binding is a
                           a valid Rexx variable name, and if not will mangle it accordingly; if .false
                           no mangling will take place

*  @param scope...         optional, defaults to option <code>&quot;A[ll]&quot;</code> (all bindings from highest to lowest),
*                          <code>&quot;G[lobal]&quot;</code> (global scope, i.e. scope value <code>200</code>),
*                          <code>&quot;E[ngine]&quot;</code> (engine scope, i.e. scope value <code>100</code>),
*                          the scope number to process, optionally followed by additional scope numbers;
*                          the explicit given sequence of scope numbers determines the order in which the
*                          bindings get added to the directory (entries in bindings of latter scopes override
*                          entries in bindings of previous scopes)
*
*  @return                 a Rexx directory object containing the name/value mappings matching the ScriptContext; the
*                          name are mangled to valid Rexx variable names if bMangleName was .true
*/
::method ScriptContext2RexxDir guarded class

  use strict arg scriptContext, bMangleName=.true, scope="AllBindings", ...

   /* if a RexxContext supplied instead, then extract the ScriptContext from its last
      argument (expected: the slotDir Rexx directory supplied by the Java side) */
  signal on syntax

  if scriptContext~isA(.RexxContext) then
     scriptContext=scriptContext~args~lastItem~scriptContext

  dir=.directory~new       -- contains the entries from the bindings of the processed scopes

  if arg(3, 'omitted') then arr=.array~of(200,100)   -- process global and engine scope only
  else
  do
     arr=.array~new
     if scope~caselessAbbrev("A") then    -- all scopes from top to bottom value (lower scopes mask override higher scopes)
     do
        arr=scriptContext~getScopes~makearray   -- returns a list of defined scopes as a Rexx array
        -- sort descendingly
        arr=arr~sortWith(.InvertingComparator~new(.NumericComparator~new))
     end
     else if scope~caselessAbbrev("G"), arg()=3 then     -- global scope
        arr~append(200)
     else if scope~caselessAbbrev("E"), arg()=3 then     -- engine scope
        arr~append(100)
     else   -- a list of scopes is given, process it in the given (argument) order
     do
         do i=3 to arg()      -- process argument list
            val=arg(i)
            if val~caselessAbbrev("G") then arr~append(200)
            else if val~caselessAbbrev("E") then arr~append(100)
            else  -- a scope number!
            do
               if \datatype(val, "W") then
                  raise syntax 40.900 array("BSF.CLS/class JSR223/class method ScriptContext2RexxDir, error 1: argument #" i "has value" pp(val)", which is not a whole number, nor starts with the letters 'G'(lobal) or 'E'(ngine)")
               arr~append(val)
            end
         end
     end
  end

  do sc over arr
      bin=self~getBindings(scriptContext,sc)

      if bin=.nil then iterate   -- inexistent scope
      keys=bin~keySet         -- get kay values
      it=keys~iterator
      do while it~hasNext
         key=it~next          -- fetch key
         val=bin~get(key)     -- fetch its value
         -- dir~setEntry(key~changeStr(".","_"), val)  -- save into Rexx directory
         if bMangleName=.true then  -- mangle Java supplied key names to Rexx symbols for Rexx variables
            dir~setEntry(self~normalizeName(key), val)  -- save into Rexx directory
         else
            dir[key]=val
      end
  end
  return dir      -- return directory that contains the selected key-value pairs

syntax:           -- propagate the syntax condition to caller
  raise propagate



/* ------------------------------------------------------------------------------------------------- */
/** Class method that returns the requested attribute from the ScriptContext.
*
* @param scriptContext the javax.script.ScriptContext
* @param name the attribute's name
* @param scope the scope number for the javax.script.Binding to be retrieved from scriptContext
* @return the attribute's value or <code>.nil</code> if not found
*/
::method getAttribute guarded class
  use strict arg scriptContext, name, scope=.nil
  signal on syntax

   /* if a RexxContext supplied instead, then extract the ScriptContext from its last
      argument (expected: the slotDir Rexx directory supplied by the Java side) */
  signal on syntax name syntax1
  if scriptContext~isA(.RexxContext) then
     scriptContext=scriptContext~args~lastItem~scriptContext

  signal on syntax name syntax2
  if scope=.nil then
     return scriptContext~getAttribute(name)       -- search through all Bindings
  else
     return scriptContext~getAttribute(name, scope)-- search in specified Bindings

syntax1: raise propagate   -- neither a .RexxContext with a / nor a ScriptContext supplied as first argument

syntax2:       -- no entry by that name in ScriptContext
  return .nil


/** Fetches the supplied names from the ScriptContext Bindings and returns them as a Rexx directory where the
*   index names are mangled to allow it to be used as a Rexx variable (hence suitable for <code>BSFContextVariables()</code>).
*
*  @param scriptContext    the Java <code>ScriptContext</code> object containing entries in different
*                          bindings or the caller's .context object which is expected to have
*                          slotDir as its last argument that contains the ScriptContext to work with
*                          (which is the case, if the caller was invoked by Java via the javax.script framework)
*
*  @param name, ...        optional list of case-sensitive names serving as entries into the ScriptContext's Bindings. If omitted
*                          all found attributes will be queried (searching from lowest to highest Bindings) and
*                          returned with their mangled names; optionally the second argument may be an ordered
*                          Rexx collection of case-sensitive names to be looked up in the ScriptContext's Bindings.
*
* @return a Rexx directory where the index is the mangled name of the Bindings entry and its item that Bindings entry's value
*/

::method getAttributes guarded class
  use strict arg scriptContext, ...

  signal on syntax
  if scriptContext~isA(.RexxContext) then    -- extract scriptContext
     scriptContext=scriptContext~args~lastItem~scriptContext

  if arg()=1 then    -- if only ScriptContext or .context is given, then return all available attributes
     return self~scriptContext2RexxDir(scriptContext) -- return all entries from all Bindings with mangled names

   /* if a RexxContext supplied instead, then extract the ScriptContext from its last
      argument (expected: the slotDir Rexx directory supplied by the Java side) */

  dir=.directory~new       -- contains the entries from the bindings of the processed scopes

  if arg()=2, arg(2)~is(.orderedCollection) then  -- two arguments only, second one an ordered collection?
     args=arg(2)           -- use
  else
     args=arg(2, "Array")   -- collect all remaining arguments as an array

  do name over args
     dir~setEntry(self~normalizeName(name), self~getAttribute(scriptContext,name))
  end
  return dir

syntax: raise propagate    -- raise in caller




/* ------------------------------------------------------------------------------------------------- */
/** Class method that returns the requested Binding from the ScriptContext.
*
* @param scriptContext the javax.script.ScriptContext
* @param sc the scope number for the javax.script.Binding to be retrieved from scriptContext
* @return the javax.script.Binding or <code>.nil</code> if not found
*/
::method setAttribute guarded class
  use strict arg scriptContext, name, value, scope=100   -- "100" is "ENGINE_SCOPE", 200="GLOBAL_SCOPE"
  signal on syntax

   /* if a RexxContext supplied instead, then extract the ScriptContext from its last
      argument (expected: the slotDir Rexx directory supplied by the Java side) */
  signal on syntax
  if scriptContext~isA(.RexxContext) then
     scriptContext=scriptContext~args~lastItem~scriptContext

  if \datatype(scope,"whole") then
  do
      pos=pos(scope~left(1)~upper, "EG")
      if pos<>0 then
      do
         if pos=1 then scope=100    -- "E" ... engine scope
                  else scope=200    -- "G" ... global scope
      end
  end

  scriptContext~setAttribute(name, value, scope)
  return

syntax: raise propagate   -- neither a .RexxContext with a / nor a ScriptContext supplied as first argument




/* ------------------------------------------------------------------------------------------------- */
/** Utility method for the methods get() and set() to parse the attributes (optionally with scopes)
*   supplied from a Rexx script annotation. This method will start parsing that string from the position
*   indicated by <code>startPos</code>
*
* This method is able to work with quoted attribute names according to the Rexx rules, which may
* be given in a Rexx script annotation. Each attribute name may be immediately followed by a colon, which
* is immediately followed by a scope number to use for the attribute. If an attribute name is a number
* and that number is not used as a key into a Bindings, then it determines the scope to be used
* for all following attribute names without an explicitly given scope number.
*
* @param attrString the Rexx annotation script string to parse
* @param startPos   either the position (a number) where parseing should start or a Rexx array
*                   object as returned from a previous invocation (see @returns below)
*
* @return <code>.nil</code>, if no attribute names left, or an ooRexx .Array with index [1]
                  being the attribute name (unqouted and unescaped),
                  index [2] the position right after the returned attribute name (will
                  be used as the starting position if passed as the startPos argument in
                  a new invocation; index [3] the scopeNr, if any given
*/
   -- make sure no one else uses this, so we have no need to check for argument erros
::method getNextAttribute guarded class private
  use arg attrString, startPos=1

  len=attrString~length

   -- if the previously returned array is supplied as the second argument, extract the startPos from it
  if startPos~isA(.array) then startPos=startPos[2]
  if startPos>len then return .nil  -- beyond string

   -- next non-blank character (in ooRexx the TAB char is regarded equivalent to blank)
  pos=verify(attrString, " " || "09"x, "Nomatch", startPos)
  if pos=0 then return .nil      -- no more attribute names available

  startChar=attrString~substr(pos,1)   -- get first char
  if pos(startChar, '"''')>0 then      -- a quoted attribute name in hand!
  do
-- say "getNextAttribute(): quoted attribute name!"
     bQuoteOpen=.true      -- set to .false if matching closing quote was found
     mb=.MutableBuffer~new
     startPos=pos+1

     do endPos=startPos while endPos<=len
        currChar=attrString~substr(endPos,1)

        if bQuoteOpen, currChar=startChar then -- a quote in hand, that must be escaped! If not, it is regarded as the ending quote
        do
           if endPos<len then
           do
              nextChar=attrString~substr(endPos+1,1)
              if nextChar=currChar then   -- the same quote, then it is escaped!
              do
                 mb~append(currChar)      -- an escaped quote
                 endPos+=1                -- account for skipping the escape quote
                 iterate
              end
              else   -- stop character (next attribute name or ":scope" starts), current attribute name
              do
                 bQuoteOpen=.false

                 if nextChar=":" then  -- ok, must be followed by a scope number
                 do
                    endPos+=2    -- skip colon
                    scopeNr=""
                    do while endPos<=len, datatype(attrString~substr(endPos,1),"N")
                       scopeNr ||= attrString~substr(endPos,1)
                       endPos+=1
                    end
                    return .array~of(mb~string,endPos,scopeNr)  -- next start position
                 end

                 return .array~of(mb~string,endPos+1)    -- next start position
              end
           end

               -- attribute name ended, define next starting position (beyond end of string)
           return .array~of(mb~string,endPos+1)
        end
        else   -- either not a quote or not the one used as delimiter
        do
           mb~append(currChar)
        end
     end
     return .array~of(mb~string, endPos)
  end

   -- process an attribute name that is not quoted, but may have the scope information
  endPos=verify(attrString, " '"":" || "09"x, "Match", pos+1)
  if endPos=0 then   -- no scope information given
     return .array~of(attrString~substr(pos),len+1)

  if attrString~subStr(endPos,1)=":" then  -- ok, must be followed by a scope number
  do
     attrLength=endPos-pos    -- get length of attrName
     endPos+=1    -- skip colon
     scopeNr=""
     do while endPos<=len, datatype(attrString~substr(endPos,1),"N")
        scopeNr ||= attrString~substr(endPos,1)
        endPos+=1
     end
     return .array~of(attrString~substr(pos, attrLength),endPos,scopeNr)
  end

  return .array~of(attrString~substr(pos, endPos-pos),endPos)



/* ------------------------------------------------------------------------------------------------- */
/** This method will be used by the Rexx injection code in Rexx scripts if the Rexx script annotation
*   <code>@GET</code> or <code>@GET(quoted list of blank delimited attribute names)</code> is contained
*   in Rexx block comments.
*
*   @param scriptContext a Java <code>scriptContext</code> or a RexxContext <code>.context</code>
*                 object from which the last argument will be fetched (expecting a slotDir argument
*                 appended by BSF4ooRexx) which is expected to have the Java <code>scriptContext</code>
*                 stored with the index named <code>SCRIPTCONTEXT</code>.
*
*   @param attributeNames optional.
*
*                 <ul>
*                 <li> If omitted or blank string will cause all attributes from all Bindings in
*                      the <code>scriptContext</code> to be made available as local Rexx variables.
*
*                 <li> If the string contains (blank delimited) attribute names then each
*                      attribute will be looked up using the ScriptContext's Bindings sorted
*                      ascendingly by their scope number. The value of the first found entry
*                      will be made available for the matching Rexx variable. If attribute names
*                      contain blanks, then they must be enquoted in the Rexx script annotation.
*
*                 <li>If the attribute name is immediately followed by a colon (:) then immediately
*                     a scope number must be following. This will restrict the lookup to the ScriptContext's
*                     Bindings having that scope number.
*
*                 <li>If the attribute name is a number and that number is not used as an attribute
*                     name in any Bindings, then it is taken as the scope number to restrict lookups to
*                     for all following attribute names.
*                 </ul>
*
*  @return a Rexx directory object where the index is a valid Rexx variable name derived from
*          the attribute name [illegal Rexx variable characters get replaced by question marks (?),
*          cf. class method {@link normalizeName}] and the item is the attribute's value
*/
::method get guarded class
   expose showWarnings
   signal on syntax
   use strict arg scriptContext, attributeNames=""

   if scriptContext~isA(.RexxContext) then   -- fetch the scriptContext entry
      scriptContext=scriptContext~args~lastItem~scriptContext

   if attributeNames="" then      -- get all attributes from all Bindings of the current script context
      return self~scriptContext2RexxDir(scriptContext)

      -- get all scopes and their Bindings
   arrScopes =scriptContext~getScopes~makearray~sortWith(.NumericComparator~new)  -- sort ascendingly: solve attributes from lowest to highest
   dirBindings=.directory~new
   do scope over arrScopes
      dirBindings[scope]=scriptContext~getBindings(scope)   -- fetch and store Bindings
   end

   -- process each word in the attributeNames string
   resDir=.directory~new      -- directory that will hold the Rexx variable name and its value
   scopeNr=.nil               -- Bindings number in the ScriptContext
      -- allow reporting usage for other candidates
   createdRexxVars=.directory~new   -- index=Rexx variable name, item=attribute name and scope number

   -- - attributes may contain spaces, in that case the Rexx script annotation must quote the name;
   -- - if attribute is a number and no attribute exists for it, then the scope of the Bindings to work
   --   on is determined for all following attributes;
   -- - if an attribute name is followed by a colon and a number, the number determines the scope of
   --   the Bindings to work on
   -- DONE: - 2017-02-11, rgf: attributes with space, quoted by Rexx rules
   res=.array~of(attributeNames,1)
   do label outer while res<>.nil -- attributeNames<>""
         -- res: an array [1]->attrName or scopeNr to use from now on; [2] next start position; [3] if not .nil the scopeNr to use for the given attrName
      res=self~getNextAttribute(attributeNames,res)
      if res=.nil then leave
      attrName=res[1]
      currScopeNr =res[3]   -- .nil or a number (or empty string, if colon was there, but no number followed!)

      if currScopeNr="" then  -- attribute name was followed by a colon ':', but the scope number is missing!
      do
         if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */ contains an attribute with a trailing colon, but no scope number:" pp(attrName":")", skipping")
         iterate
      end

      if datatype(attrName,"W"), currScopeNr=.nil then -- maybe just a scope number given that is to be used from now on?
      do
          tmpScopeNr=scriptContext~getAttributesScope(attrName)   -- could be that the number is a key into a Bindings
          if tmpScopeNr<>-1 then    -- found entry (a number key) in a scope, use that scope
              currScopeNr=tmpScopeNr
          else    -- attrName is the scopeNr to use from now on!
          do
             if dirBindings~hasIndex(attrName) then -- does scope number exist?
                scopeNr=attrName    -- from now on look only in this scope!
             else -- scope does not exist, warn and ignore
                if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */ contains a non-existing scope" pp(attrName)", skipping")
             iterate
          end
      end

      if currScopeNr<>.nil then  -- explicit scope given for attribute name, check it out
      do
          if \dirBindings~hasIndex(currScopeNr) then -- does scope number exist?
          do
             if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */ contains a non-existing scope" pp(currScopeNr) "in" pp(attrName||":"||currScopeNr)", skipping")
             iterate
          end
      end

      -- now process
      if scopeNr=.nil, currScopeNr=.nil then -- no scope explicitly given, go through all available scopes
         arr=arrScopes
      else if currScopeNr<>.nil then   -- process explicitly defined scope only
         arr=.array~of(currScopeNr)
      else                             -- process explicitly defined scope only
         arr=.array~of(scopeNr)

      do scope over arr       -- iterate over bindings
         tmpBindings=dirBindings~at(scope)   -- fetch Bindings
         if tmpBindings<>.nil, tmpBindings~containsKey(attrName) then   -- check whether attribute is contained
         do
              -- fetch the attribute's value, make sure attrName is a valid Rexx variable name
            normalizedName=self~normalizeName(attrName)
            if createdRexxVars~hasEntry(normalizedName) then
            do
               if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */ in" pp(attrName||":"||currScopeNr) "yields the Rexx variable named" pp(normalizedName) "which was already used for" createdRexxVars~entry(normalizedName)", skipping")
            end
            else
            do
               resDir~put(tmpBindings~get(attrName),self~normalizeName(attrName))
               createdRexxVars~setEntry(normalizedName, pp(attrName)":"pp(scope))   -- remember where we created this Rexx variable name
            end
            iterate outer
         end
      end
      if currScopeNr<>.nil then
         if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */: cannot find attribute" pp(attrName) "in the explicitly supplied scopeNr" pp(currScopeNr)", cf. for some entry" pp(attrName)":"pp(currScopeNr) "in the Rexx script annotation")
      else
         if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @get("arg(2)") */: cannot find attribute" pp(attrName) "in any of the ScriptContext's scopes")
   end

   return resDir

syntax:
   raise propagate   -- neither a .RexxContext with a / nor a ScriptContext supplied as first argument



/* ------------------------------------------------------------------------------------------------- */
/** This method will be used by the Rexx injection code in Rexx scripts if the Rexx script annotation
*   <code>@SET(quoted list of blank delimited attribute names)</code> is contained in Rexx block comments.
*   <p>
*
*   If an attribute in the attribute list cannot be found in the ScriptContext's scope, a warning will
*   be given. The attribute name will be normalized and that value will be used to look up the current
*   Rexx value in the Rexx variable Rexx directory passed in as the second argument. If there is no
*   Rexx variable with the normalized name a warning will be issued.
*
*
*   @param scriptContext a Java <code>scriptContext</code> or a RexxContext <code>.context</code>
*                 object from which the last argument will be fetched (expecting a slotDir argument
*                 appended by BSF4ooRexx) which is expected to have the Java <code>scriptContext</code>
*                 stored with the index named <code>SCRIPTCONTEXT</code>.
*
*   @param attributeNames optional.
*
*                 <ul>
*                 <li> If omitted or blank string will cause all attributes from all Bindings in
*                      the <code>scriptContext</code> to be updated with the values from
*                      <code>RxVarDir</code>.
*
*                 <li> If the string contains (blank delimited) attribute names then each
*                      attribute will be looked up using the ScriptContext's Bindings sorted
*                      ascendingly by their scope number. The value of the first found entry
*                      with a normalized name matching a Rexx variable name from
*                      <code>RxVarDir</code> will cause that attribute to be updated with
*                      the Rexx value, if that is different.
*                      If attribute names contain blanks, then they must be enquoted in the
*                      Rexx script annotation.
*
*                 <li>If the attribute name is immediately followed by a colon (:) then immediately
*                     a scope number must be following. This will restrict the lookup to the
*                     ScriptContext's Bindings having that scope number. If the attribute's normalized
*                     name matches a Rexx variable name from <code>RxVarDir</code> then that
*                     attribute will be updated with the Rexx value, if that is different.
*
*                 <li>If the attribute name is a number and that number is not used as an attribute
*                     name in any Bindings, then it is taken as the scope number to restrict lookups to
*                     for all following attribute names.
*                 </ul>
*
*  @param rxVarDir   a Rexx directory containing variable names as index and their values as items.
*                    From each attribute name its (maybe mangled) Rexx variable name will be inferred,
*                    and the attribute's value in the Bindings updated to the Rexx value, if that
*                    value differs in its class or objectname value.
*/

::method set guarded class
   expose showWarnings

   signal on syntax
   use strict arg scriptContext, attributeNames="", rxVarDir

   if scriptContext~isA(.RexxContext) then   -- fetch the scriptContext entry
      scriptContext=scriptContext~args~lastItem~scriptContext

      -- get all scopes and their Bindings
   arrScopes =scriptContext~getScopes~makearray~sortWith(.NumericComparator~new)  -- sort ascendingly: solve attributes from lowest to highest
   dirBindings=.directory~new
   do scope over arrScopes
      dirBindings[scope]=scriptContext~getBindings(scope)   -- fetch and store Bindings
   end

   /*
       if no attribute names are given, then update the first occurrence, but check all scopes
       for possible candidates: if found, give a warning depicting the attribute name and scope
       that was not updated; there may be two different reasons for this,

       - one are attribute names that are identically spelled in different Bindings, the other

       - one is that the mapping of differently spelled attribute names still map to the same
         Rexx variable name (illegal characters will be replaced by the '?' character, a
         leading '.' too, to allow it to be a Rexx variable and not an environment symbol)
   */
   if attributeNames="" then
   do
      do rxVarName over RxVarDir~allIndexes~sort   -- iterate over all Rexx variables in alphabetical order
         RxVarApplied4=""           -- was this Rexx variable name already applied
         do tmpScope over arrScopes -- look for attribute names that match the Rexx variable name
             bin=scriptContext~getBindings(tmpScope)
             keys=bin~keySet           -- get Bindings' keys
             it=bin~keySet~makearray   -- get the keys as a Rexx array
             do key over it~sortWith(.CaselessComparator~new)  -- sort keys (attributes) caselessly
                normalizedName=self~normalizeName(key)         -- get symbol usable as Rexx variable name (illegal Rexx chars are replaced by '?')
                if rxVarName=normalizedName, RxVarDir~hasEntry(normalizedName) then   -- ha, attribute candidate found!
                do
                   if RxVarApplied4="" then  -- Rexx variable was already applied, give a warning!
                   do
                       rxVarApplied4=pp(key)":"pp(tmpScope) -- remember to which attribute in which scope
                       currVal=bin~get(key)                 -- get Bindings value
                       rexxVal=RxVarDir~entry(normalizedName)  -- get Rexx value
                        -- if of equal type, then do they have the same objectname ?
                       bEqual=(currVal~class=rexxVal~class) & (currVal~objectName=rexxVal~objectName)
                        -- only update attribute's value in the Bindings, if a new value
                       if bEqual=.false then
                          bin~put(key,rexxVal)     -- set attribute to Rexx variable's value
                   end
                   else -- oops: Rexx variable was already used, warn
                   do
                       if showWarnings=.true then .error~say("Warning: Rexx variable" pp(rxVarName) "could be applied to" pp(key)":"pp(tmpScope)", however it was already used for:" RxVarApplied4 "(to override specify scope number explicitly)")
                   end
                end
             end

         end
      end
      return      -- done with processing
   end

   -- process each word in the attributeNames string
   scopeNr=.nil               -- Bindings number in the ScriptContext

   res=.array~of(attributeNames,1)  -- parse first attribute name
   do label outer while res<>.nil -- attributeNames<>""
         -- res: an array [1]->attrName or scopeNr to use from now on; [2] next start position; [3] if not .nil the scopeNr to use for the given attrName
      res=self~getNextAttribute(attributeNames,res)
      if res=.nil then leave
      attrName=res[1]
      currScopeNr =res[3]   -- .nil or a number
      if currScopeNr="" then  -- attribute name was followed by a colon ':', but the scope number is missing!
      do
         if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @set("arg(2)") */ contains an attribute with a trailing colon, but no scope number:" pp(attrName":")", skipping")
         iterate
      end


      if datatype(attrName,"W"), currScopeNr=.nil then -- maybe just a scope number given that is to be used from now on?
      do
          tmpScopeNr=scriptContext~getAttributesScope(attrName)   -- could be that the number is a key into a Bindings
          if tmpScopeNr<>-1 then    -- found entry (a number key) in a scope, use that scope
              currScopeNr=tmpScopeNr
          else    -- attrName is the scopeNr to use from now on!
          do
             if dirBindings~hasIndex(attrName) then -- does scope number exist?
                scopeNr=attrName    -- from now on look only in this scope!
             else -- scope does not exist, warn and ignore
                if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @set("arg(2)") */ contains a non-existing scope" pp(attrName)", skipping")
             iterate
          end
      end

      if currScopeNr<>.nil then  -- explicit scope given for attribute name, check it out
      do
          if \dirBindings~hasIndex(currScopeNr) then -- does scope number exist?
          do
             if showWarnings=.true then .error~say("Warning: Rexx script annotation /* @set("arg(2)") */ contains a non-existing scope" pp(currScopeNr) "in" pp(attrName||":"||currScopeNr)", skipping")
             iterate
          end
      end

      -- now process
      if scopeNr=.nil, currScopeNr=.nil then -- no scope explicitly given, go through all available scopes
         arr=arrScopes
      else if currScopeNr<>.nil then   -- process explicitly defined scope only
         arr=.array~of(currScopeNr)
      else                             -- process explicitly defined scope only
         arr=.array~of(scopeNr)

      do label inner scope over arr       -- iterate over bindings
         tmpBindings=dirBindings~at(scope)   -- fetch Bindings
         if tmpBindings~containsKey(attrName) then
         do
   -- iterate over all Bindings and check which attribute name can be mapped to a Rexx variable name
            normalizedName=self~normalizeName(attrName)  -- get normalized name of the attribute
            if RxVarDir~hasEntry(normalizedName) then -- ha, attribute candidate found!
            do
               currVal=tmpBindings~get(attrName)      -- get Bindings value
               rexxVal=RxVarDir~entry(normalizedName) -- get Rexx value
                -- if of equal type, then do they have the same objectname ?
               bEqual=(currVal~class=rexxVal~class) & (currVal~string=rexxVal~string)
                -- only update attribute's value in the Bindings, if a new value
               if bEqual=.false then
                  tmpBindings~put(attrName,rexxVal)   -- set attribute to Rexx variable's value
            end
         end
      end
   end

   return

syntax:
   raise propagate   -- neither a .RexxContext with a / nor a ScriptContext supplied as first argument


/* ================================================================================================= */
/* ================================================================================================= */

/** This abstract class implements the protocol and common methods for the awt/swing and JavaFX
    utility classes that allow one to interact with the GUI objects on the awt/JavaFX thread.
    <br>

    If Rexx code is invoked by the GUI maintenance code in form of callbacks, that Rexx code
    will already execute on the GUI thread and can successfully
    interact with the GUI and the elements. However, if Rexx code on a non-GUI-thread wishes
    to interact with the GUI, this would hang the GUI (single threaded implementations are
    typical for GUIs). Rather, such an interaction must occur on the GUI thread.
    <p>The class methods <code>fxRunLater</code> and <code>fxRunLaterLatest</code> allow
    Rexx programmers to send Rexx messages later, when running in the GUI thread.

    @since 2017-08-14
*/
::class AbstractGUIThread private
::method init  guarded abstract    -- do not allow any instances to be created

::attribute JavaGuiUtilityClz get guarded class abstract   -- javafx.application.Platform or javax.swing.SwingUtilitities
::attribute msgQueue              guarded class abstract   -- force creation of attribute in subclass; queues GUIMessages
::attribute runnable              guarded class abstract   -- force creation of attribute in subclass; Java Proxy of Rexx class itself
::attribute waitingToRun          guarded class abstract   -- force creation of attribute in subclass; determines

::method    setup                 guarded class private abstract    -- setup class attributes
::method    isGuiThread           guarded class abstract   -- determines if current thread is GUI thread
::method    invokeOnGuiThread     guarded class private abstract    -- use the appropriate Java utility class method


/* ------------------------------------------------------------------------------------------------- */
/** Private static method that processes the supplied GUIMessage.
*/
::method process           guarded class private
  use strict arg guiMsg

  signal on syntax      -- intercept errors
  if guiMsg~hasArguments then -- create and send messages
     .message~new(guiMsg~target, guiMsg~messageName, "Array", guiMsg~arguments)~send
  else
     .message~new(guiMsg~target, guiMsg~messageName)~send

  if var("RESULT") then    -- was a result returned?
  do
     guiMsg~result=result   -- save result
     guiMsg~hasResult=.true -- indicate we have a result
  end
  guiMsg~completed=.true    -- communicate that processing has completed
  return

syntax:
  guiMsg~errorCondition=condition("o") -- supply error condition object
  guiMsg~hasError=.true    -- indicate we had an error
  guiMsg~completed=.true   -- communicate that processing has completed in this case as well

/*
-- TODO: remove the following debug statements
.stderr~say("---> SOD (start of debug), line #" .line)
.stderr~say(self":process, error" pp(condition("C")) "in" pp(guiMsg~target) "~" pp(guiMsg~messageName))
.stderr~say
.context~package~addPackage(.package~new("rgf_util2.rex"))  -- do a dynamic requires
.stderr~say(ppCondition2(guiMsg~errorCondition))   -- show condition formatted in full
.stderr~say("<--- EOD (end of debug), line #" .line)
*/

  return


/* ------------------------------------------------------------------------------------------------- */
/** Private static method that allows simple creation of a GUIMessage from the supplied array of arguments.
*   @param args the argument array where index 1 stores the target object, index 2 the message name; optonally
*               index 3 indicates whether the arguments are supplied individually or in index 4 as an array already
*
*   @return returns a <code>GUIMessage</code> instance
*/
::method createGUIMessage  guarded class private
  use strict arg args      -- an array

  signal on syntax
  target     =args[1]
  messageName=args[2]
  if args~items>2 then     -- message arguments supplied
  do
     argType=args[3]~left(1)~upper   -- must be "Individual" or "Array"
     select
        when argType="I" then msgArgs=args~section(4) -- turn individual arguments into an array

        when argType="A" then -- fourth argument is an array of arguments already
             do
                if args~items>4 then     -- too many arguments!
                   raise syntax 88.922 array ("4")

                if \args[4]~isA(.array) then    -- wrong type
                   raise syntax 88.923 array ("message", "4", args[4])

                msgArgs=args[4]
             end

        otherwise             -- we have a problem here!
           raise syntax 93.915 array ("IA", args[3])
     end
     guiMsg=.GuiMessage~new(target, messageName, msgArgs)
  end
  else
  do
     guiMsg=.GuiMessage~new(target, messageName)
  end
  return guiMsg

syntax:
  raise propagate



/* ------------------------------------------------------------------------------------------------- */
::method run               guarded class       -- this method will be invoked by Java at a later time
  -- expose fxQueue PlatformClz waitingToRun
-- say self": arrived at  :" pp(.dateTime~new)
  msgQueue=self~msgQueue      -- get message queue

  if self~isGuiThread then   -- only execute, if we are running on the GUI thread!
  do
      -- process queued GuiMessages
     do while msgQueue~items>0
        self~process(msgQueue~pull)    -- fetch item from head of the queue
     end
     self~waitingToRun=.false   -- indicate that no runLater is pending!
  end
-- say self": returning at:" pp(.dateTime~new)




/* ------------------------------------------------------------------------------------------------- */
/** This class method allows to define a Rexx message with its arguments that should
*   be processed on the GUI thread. Each invocation will create a new <code>GUIMessage</code>
*   from the supplied arguments and returns it for further inspection in addition to queuing
*   it for later execution on the GUI thread. This method remembers the order of invocations
*   and makes sure that each GUI message will be executed in that order.
*
*   @param target the target object to receive a message on the GUI thread
*   @param messageName the message name to send to the target on the GUI thread
*   @param indicator  optional; indicates with &quot;I&quot; (Indivdual) that the arguments
*                     are listed individually, &quot;A&quot; (Array) indicates that the fourth
*                     argument is an array containing the arguments to supply with the
*                     message on the GUI thread
*
*   @return GUIMessage a GUI message object (modelled after ooRexx' <code>Message</code> class
*                     that allows one to inspect the state of the message (process completed,
*                     fetching a possible result, determining whether an error occurred and
*                     inspecting it)
*/
::method runLater          guarded class
  use strict arg target, messageName, ...

  signal on syntax
  if self~JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized
  guiMsg=self~createGuiMessage(arg(1,"array"))
  self~msgQueue~queue(guiMsg)
  if self~waitingToRun=.false then -- "runLater" not invoked yet?
  do
     self~waitingToRun=.true
     self~invokeOnGuiThread   -- make sure Java will dispatch Rexx message on GUI thread
  end
  return guiMsg            -- allows caller to get a hold of a possible return value

syntax:
  raise propagate


/* ------------------------------------------------------------------------------------------------- */
/** This class method allows to define a Rexx message with its arguments that should
*   be processed on the GUI thread. Each invocation will create a new <code>GUIMessage</code>
*   from the supplied arguments and returns it for further inspection in addition to queuing
*   it for later execution on the GUI thread. This method will push the message into the queue, such
*   that it will run before any other messages that may have existed in the queue at that point in time.
*
*   @param target the target object to receive a message on the GUI thread
*   @param messageName the message name to send to the target on the GUI thread
*   @param indicator  optional; indicates with &quot;I&quot; (Indivdual) that the arguments
*                     are listed individually, &quot;A&quot; (Array) indicates that the fourth
*                     argument is an array containing the arguments to supply with the
*                     message on the GUI thread
*
*   @return GUIMessage a GUI message object (modelled after ooRexx' <code>Message</code> class
*                     that allows one to inspect the state of the message (process completed,
*                     fetching a possible result, determining whether an error occurred and
*                     inspecting it)
*/
::method runLaterPush      guarded class
  use strict arg target, messageName, ...

  signal on syntax
  if self~JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized
  guiMsg=self~createGuiMessage(arg(1,"array"))
  self~msgQueue~push(guiMsg)  -- will become the first message in the queue
  if self~waitingToRun=.false then -- "runLater" not invoked yet?
  do
     self~waitingToRun=.true
     self~invokeOnGuiThread   -- make sure Java will dispatch Rexx message on GUI thread
  end
  return guiMsg            -- allows caller to get a hold of a possible return value

syntax:
  raise propagate



/* ------------------------------------------------------------------------------------------------- */
   -- method replaces existing target: this way only the latest sent message will get executed!
/** This class method allows to define a Rexx message with its arguments that should
*   be processed on the GUI thread. Each invocation will create a new <code>GUIMessage</code>
*   from the supplied arguments and returns it for further inspection in addition to queueing
*   it for later execution on the GUI thread. Unlike <code>runLater</code> this method will
*   first remove any queued messages with the same target and the same message name, before
*   queueing this message.
*
*   @param target the target object to receive a message on the GUI thread
*   @param messageName the message name to send to the target on the GUI thread
*   @param indicator  optional; indicates with &quot;I&quot; (Indivdual) that the arguments
*                     are listed individually, &quot;A&quot; (Array) indicates that the fourth
*                     argument is an array containing the arguments to supply with the
*                     message on the GUI thread
*
*   @return GUIMessage a GUI message object (modelled after ooRexx' <code>Message</code> class
*                     that allows one to inspect the state of the message (process completed,
*                     fetching a possible result, determining whether an error occurred and
*                     inspecting it)
*/
::method runLaterLatest    guarded class
  use strict arg target, messageName, ...

  signal on syntax
  if self~JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized

  forward message "REMOVEMESSAGE" continue  -- remove all GUI messages of the same name targeted to the same object
  guiMsg=result               -- fetch returned GUIMessage object
  msgQueue=self~msgQueue      -- get message queue
  msgQueue~queue(guiMsg)      -- now enqueue current (latest) guiMsg

  if self~waitingToRun=.false then -- "runLater" not invoked yet?
  do
     self~waitingToRun=.true
     self~invokeOnGuiThread   -- make sure Java will dispatch Rexx message on GUI thread
  end
  return guiMsg            -- allows caller to get a hold of a possible return value

syntax:
  raise propagate


/* ------------------------------------------------------------------------------------------------- */
   -- method replaces existing target: this way only the latest sent message will get executed!
/** This class method allows to define a Rexx message with its arguments that should
*   be processed on the GUI thread. Each invocation will create a new <code>GUIMessage</code>
*   from the supplied arguments and returns it for further inspection in addition to pushing
*   it for later execution on the GUI thread. This method will
*   first remove any queued messages with the same target and the same message name, before
*   pushing this message. This may be useful if there is a need for interacting with a specific
*   GUI object before all the other ones for which GUIMessage objects may be present.
*
*   @param target the target object to receive a message on the GUI thread
*   @param messageName the message name to send to the target on the GUI thread
*   @param indicator  optional; indicates with &quot;I&quot; (Indivdual) that the arguments
*                     are listed individually, &quot;A&quot; (Array) indicates that the fourth
*                     argument is an array containing the arguments to supply with the
*                     message on the GUI thread
*
*   @return GUIMessage a GUI message object (modelled after ooRexx' <code>Message</code> class
*                     that allows one to inspect the state of the message (process completed,
*                     fetching a possible result, determining whether an error occurred and
*                     inspecting it)
*/
::method runLaterLatestPush   guarded class    -- make sure this message is the first in the message queue, such that it gets executed first
  use strict arg target, messageName, ...

  signal on syntax
  if self~JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized
  forward message "REMOVEMESSAGE" continue  -- remove all GUI messages of the same name targeted to the same object
  guiMsg=result               -- fetch returned GUIMessage object
  msgQueue=self~msgQueue      -- get message queue
  msgQueue~push(guiMsg)       -- now enqueue current (latest) guiMsg

  if self~waitingToRun=.false then -- "runLater" not invoked yet?
  do
     self~waitingToRun=.true
     self~invokeOnGuiThread   -- make sure Java will dispatch Rexx message on GUI thread
  end
  return guiMsg            -- allows caller to get a hold of a possible return value

syntax:
  raise propagate


/* ------------------------------------------------------------------------------------------------- */
   -- method removes all GUI message objects
/** This private class method removes all GUIMessage objects from the message queue that will be processed
*   later when Java call backs on the GUI thread, that have the supplied target and the supplied
*   message name.
*
*   @param target the target object to receive a message on the GUI thread
*   @param messageName the message name to send to the target on the GUI thread
*   @param indicator  optional; indicates with &quot;I&quot; (Indivdual) that the arguments
*                     are listed individually, &quot;A&quot; (Array) indicates that the fourth
*                     argument is an array containing the arguments to supply with the
*                     message on the GUI thread
*
*   @return GUIMessage a GUI message object (modelled after ooRexx' <code>Message</code> class
*                     that allows one to inspect the state of the message (process completed,
*                     fetching a possible result, determining whether an error occurred and
*                     inspecting it)
*/
::method removeMessage    guarded class private
  use strict arg target, messageName, ...

  signal on syntax
  if self~JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized

      -- remove all occurrences of messages with the same messagename and the same target
  msgQueue=self~msgQueue      -- get message queue
  idx=msgQueue~first          -- get index of the first item in the queue, if any
  do while idx<>.nil          -- a valid index in hand ?
     tmpItem=msgQueue~at(idx)  -- fetch item, a GuiMessage
         -- same target, same messagename?
     if tmpItem~target=target, tmpItem~messageName~caselessEquals(messagename) then
        msgQueue~delete(idx)   -- delete the guiMsg object

     idx=msgQueue~next(idx)    -- get index of next item in the queue
  end

  guiMsg=self~createGuiMessage(arg(1,"array"))  -- create and return GUI message object
  return guiMsg            -- allows caller to get a hold of a possible return value

syntax:
  raise propagate



/* ================================================================================================= */
/* ================================================================================================= */

-- rgf, 2017-08-04/05, create a possibility that eases interaction with
-- the JavaFX GUI objects from Rexx programs in non-GUI-threads, classes "FxGUIThread" and "GUIMessage"
-- rgf, 2018-07-14, refactored

/** This class is meant to help ooRexx programmers to simplify interactions with the JavaFX
    GUI. If Rexx code is invoked by the GUI maintenance code, that Rexx code will already
    execute on the GUI thread (called "JavaFX Applicaton Thread" on JavaFX) and can successfully
    interact with the GUI and the elements. However, if Rexx code on a non-GUI-thread wishes
    to interact with the GUI, this would hang the GUI (single threaded implementations are
    typical for GUIs). Rather, such an interaction must occur on the GUI thread.
    <p>The class methods <code>fxRunLater</code> and <code>fxRunLaterLatest</code> allow
    Rexx programmers to send Rexx messages later, when running in the GUI thread.

    @since 2017-08-05
*/
::class "FxGUIThread"  subclass AbstractGUIThread public

/* ------------------------------------------------------------------------------------------------- */
/** Constructor class method. */
::method init             guarded class
  expose JavaGuiUtilityClz          -- class attribute for this class object!
  JavaGuiUtilityClz=.nil


::attribute msgQueue          get guarded class
::attribute runnable          get guarded class
::attribute JavaGuiUtilityClz get guarded class
::attribute waitingToRun          guarded class


/* ------------------------------------------------------------------------------------------------- */
/** Private class method to set up and initilaize the infrastructure.
*/
::method setup             guarded class private
  expose msgQueue runnable JavaGuiUtilityClz waitingToRun
  msgQueue=.queue~new         -- msgQueue: all queued messages will be dispatched
  runnable=.routines["XBSFCREATEREXXPROXY"]~call(self, .nil, "java.lang.Runnable") -- allow this class object to be used as a Runnable
  JavaGuiUtilityClz=bsf.loadClass("javafx.application.Platform")
  waitingToRun=.false


/* ------------------------------------------------------------------------------------------------- */
/**
* @return returns <code>.true</code>, if running on the GUI thread ("JavaFX Application Thread"), <code>.false</code> else
*/
::method isGuiThread     guarded class
  expose JavaGuiUtilityClz
  if JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized
  return JavaGuiUtilityClz~isFxApplicationThread


/**
*   Will cause the <code>runnable</code> (this Rexx class wrapped as a java.lang.Runnable object) to execute later on the GUI thread.
*/
::method invokeOnGuiThread    guarded class       -- make sure Java will dispatch Rexx message on GUI thread
  expose JavaGuiUtilityClz runnable
  JavaGuiUtilityClz~runLater(runnable)    -- JavaFX version



/* ================================================================================================= */
/* ================================================================================================= */
-- rgf, 2018-07-14, refactored

/** This class is meant to help ooRexx programmers to simplify interactions with the awt/swing
    GUI. If Rexx code is invoked by the GUI maintenance code, that Rexx code will already
    execute on the GUI thread (called "event dispatch thread" a.k.a. "awt thread") and can successfully
    interact with the GUI and the elements. However, if Rexx code on a non-GUI-thread wishes
    to interact with the GUI, this would hang the GUI (single threaded implementations are
    typical for GUIs). Rather, such an interaction must occur on the GUI thread.
    <p>The class methods <code>awtRunLater</code> and <code>awtRunLaterLatest</code> allow
    Rexx programmers to send Rexx messages later, when running in the GUI thread.

    @since 2018-07-14
*/
::class "AwtGUIThread"  subclass AbstractGUIThread public

/* ------------------------------------------------------------------------------------------------- */
/** Constructor class method. */
::method init             guarded class
  expose JavaGuiUtilityClz          -- class attribute for this class object!
  JavaGuiUtilityClz=.nil


::attribute msgQueue          get guarded class
::attribute runnable          get guarded class
::attribute JavaGuiUtilityClz get guarded class
::attribute waitingToRun          guarded class

/* ------------------------------------------------------------------------------------------------- */
/** Private class method to set up and initilaize the infrastructure.
*/
::method setup             guarded class private
  expose msgQueue runnable JavaGuiUtilityClz waitingToRun
  msgQueue=.queue~new         -- msgQueue: all queued messages will be dispatched
  runnable=.routines["XBSFCREATEREXXPROXY"]~call(self, .nil, "java.lang.Runnable") -- allow this class object to be used as a Runnable
  JavaGuiUtilityClz=bsf.loadClass("javax.swing.SwingUtilities")
  waitingToRun=.false

/* ------------------------------------------------------------------------------------------------- */
/**
* @return returns <code>.true</code>, if running on the GUI thread ("event dispatch thread" a.k.a. "awt Thread"), <code>.false</code> else
*/
::method isGuiThread     guarded class
  expose JavaGuiUtilityClz
  if JavaGuiUtilityClz=.nil then self~setup  -- make sure attributes are initialized
  return JavaGuiUtilityClz~isEventDispatchThread   -- awt/swing version


/**
*   Will cause the <code>runnable</code> (this Rexx class wrapped as a java.lang.Runnable object) to execute later on the GUI thread.
*/
::method invokeOnGuiThread    guarded class       -- make sure Java will dispatch Rexx message on GUI thread
  expose JavaGuiUtilityClz runnable
  JavaGuiUtilityClz~invokeLater(runnable) -- awt/swing version



/* ================================================================================================= */
/* ================================================================================================= */

/** This is a private class modelled after ooRexx <code>Message</code> class and is used in the
    methods of the BSF4ooRexx class <code>FxGUIThread</code>. It allows for inspecting the
    message parts meant to be executed on the GUI thread on the next possibility.

    @since 2017-08-05
*/
::class "GUIMessage"    PRIVATE -- class to represent a message (including a future result) to be run on the GUI thread

/* ------------------------------------------------------------------------------------------------- */
/** Constructor method.
*
*  @param target the object to receive the message on the GUI thread
*  @param messageName the message name to be sent on the GUI thread
*  @param arguments optional array of arguments to be supplied when sending the message on the GUI thread
*/
::method init                     guarded
  expose completed hasResult target messageName arguments hasArguments hasError errorCondition
  use strict arg target, messageName, args=.nil

   -- make sure messageName is a string
  .ArgUtil~validateClass(pp("messageName")||"="pp(messageName), messageName, .string)   -- check for correct type

  hasArguments=(arg()=3)
  if hasArguments then
  do
     arguments=args
     .ArgUtil~validateClass(pp("arguments")||"="pp(args), args, .array)   -- check for correct type
  end

  completed=.false
  hasResult=.false
  result   =.nil
  hasError =.false
  errorCondition=.nil



/* ------------------------------------------------------------------------------------------------- */
/** If <code>.true</code> then <code>errorCondition</code> will hold the Rexx condition object.
 @return returns <code>.true</code>, if an error has occurred while running this message, <code>.false</code> else
*/
::attribute hasError              guarded


/* ------------------------------------------------------------------------------------------------- */
/** If an error occurred while running (executing) the message the error condition object will be made available
    via this attribute.
 @return returns the Rexx condition object or <code>.nil</code>, if none
*/
::attribute errorCondition        guarded

/* ------------------------------------------------------------------------------------------------- */
/** @return returns <code>.true</code>, if this message has a result, <code>.false</code> else
*/
::attribute hasResult             guarded


/* ------------------------------------------------------------------------------------------------- */
/** If the message has run and has completed and returns a result, this attrbitue makes it available.

 @return returns <code>.true</code>, if this message has completed without an error, <code>.false</code> else
*/
::attribute completed             guarded


/* ------------------------------------------------------------------------------------------------- */
/** @return returns the target object which receives the message */
::attribute target            get guarded

/* ------------------------------------------------------------------------------------------------- */
/** @return returns the message name that gets sent */
::attribute messageName       get guarded

/* ------------------------------------------------------------------------------------------------- */
/** @return returns <code>.true</code>, if this message has arguments, <code>.false</code> else */
::attribute hasArguments          guarded

/* ------------------------------------------------------------------------------------------------- */
/** @return returns an array of message arguments */
::attribute arguments         get guarded

/* ------------------------------------------------------------------------------------------------- */
/** Returns the result of running the message. This method will block (wait) until the message
   has been sent and completed (or having an error while executing), before returning the
   <code>result</code>. You can check the attribute <code>completed</code> to determine
   whether the message has completed (<code>.true</code>), such that invoking this method
   does not block. Make sure that <code>hasError</code> is <code>.false</code>, otherwise the
   result value is invalid.

   @return <code>result</code> object, when <code>completed</code> or <code>hasError</code> turns to <code>.true</code>
*/
::attribute result            get guarded
  expose completed result hasError
  guard on when completed=.true | hasError=.true
  return result

/* ------------------------------------------------------------------------------------------------- */
/** Setter method allowing setting result. */
::attribute result            set guarded
  expose result
  use strict arg result


/* ================================================================================================= */
/** This class gets instantiated either by native or Java code and contains additional, but optional,
*   entries when sending a message to a Rexx object (a callback from native/Java code to Rexx)
*/
::class "Slot.Argument" subclass Directory public




/* ================================================================================================= */
/* rgf, 2018-03-05, 2022-01-13 */

/** Routine that compiles a Java source file and returns the class object.

   @param className  the fully qualified class name (package plus class name in source)
   @param code       a mandatory string or array of strings representing the Java or NetRexx source code of a class
   @param language   optional, defaults to &quot;Java&quot;, alternatively can be &quot;NetRexx&quot;
   @param import     logical, if set to .true (default) will box newClass using bsf.import()
                     allowing ooRexx new to be sent), otherwise the Java class object gets returned as is
   @return           the compiled Java class object
*/

::routine bsf.compile   public
   use strict arg className, code, language="Java", bImport=.true

   if className="" | \className~isA(.string) then
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.compile(): argument 'className' must be a string and not empty")

   if code~isNil then
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.compile(): argument 'code' must not be .nil")

   lang=language~left(1)~upper
   if pos(lang,"JN")=0 then
      raise syntax 40.900 array ("BSF.CLS/routine/bsf.compile(): argument 'language' must be" -
         '"J[ava]" or "N[etRexx]", found:"' lang)

   signal on syntax
   if code~isA(.array) then code=code~toString  -- turn string array into a string

   if language~left(1)~upper="N" then     -- NetRexx compilation!
   do
      signal on syntax name nrxSyntax
      nrc=bsf.loadClass("org.netrexx.process.NetRexxC")
      al=.bsf~new("java.util.ArrayList")

      qualifiedClassName=className
      -- if package in name, extract unqualified class name
      p=qualifiedClassName~pos(.)
      if p>0 then
         parse var qualifiedClassName pkgName +(p) className

      -- compiler expects unqualified class name!
      res=nrc~main(className "-noverbose",code,.nil,al)
      if res<>0 then
      do
         errMsg = "BSF.CLS/routine/bsf.compile(), hint: NetRexx compiler indicates a problem, return value was:" res
         raise syntax 40.900 array (errMsg)
      end

      newClass=al~get(0)~findClass(qualifiedClassName)
      -- camouflage NetRexxJava class object as ooRexx class object, add class methods new and newStrict
      if bImport=.true then   -- wrap it up as an ooRexx class understanding the new and newStrict messages
         newClass=.bsf~bsf.import(qualifiedClassName, .nil, newClass)   -- do not place it into the environment
      return newClass
   end

   sc=.bsf~new("org.codehaus.janino.SimpleCompiler")
   clzLdr=.bsf~bsf.loadClass("java.lang.Thread")~currentThread~getContextClassLoader
   if clzLdr=.nil then
   do
      clzLdr=bsf.wrap(bsf("getRexxAndJava"))~getClass~getClassLoader
   end

   sc~setParentClassLoader(clzLdr)
   sc~cook(code)
   newClass=sc~getClassLoader~loadClass(className)
   if bImport=.true then   -- wrap it up as an ooRexx class understanding the new and newStrict messages
      newClass=.bsf~bsf.import(className, .nil, newClass)   -- do not place it into the environment
   return newClass

nrxSyntax:
   raise propagate

syntax:
   extractedName=extractJavaClassName(code)     -- extract Java class name from source code
   errMsg=""
   if extractedName="" then
      errMsg = "BSF.CLS/routine/bsf.compile(), hint: cannot extract className, maybe not given or the Java class is not ""public"" ?"
   else if className<>extractedName then
   do
      errMsg = "BSF.CLS/routine/bsf.compile(), hint: supplied class name" pp(className) "does not match the first found public class name" pp(extractedName) "in the source file"
   end

   if errMsg="" then raise propagate
   raise syntax 40.900 array (errMsg)


   -- extracts the fully qualified class name from the Java source code; cater for all sorts of formatting of the unqualified class name
extractJavaClassName: procedure
   parse arg code

   parse var code "package" pkgName ";"
   parse var code "public" . "class" clzName "{"
   -- replace contained CR-LF with blanks; remove leading/trailing blanks, get first word
   clzName=clzName~translate("  ", "0d0a"x)~strip~word(1)

   if pkgName="" then return clzName
   return pkgName~strip"."clzName



/* ================================================================================================= */
/* ================================================================================================= */

/* ------------------------------------------------------------------------ */
/** Routine that recodes a Rexx string in the given <em>fromCp</em> codepage into a Rexx string in the
    given <em>toCp</em> codepage. The names of the character sets used for encoding are documented e.g.
    in <https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html> or
    <https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html>.
    Examples for such
    names are &quot;Cp850&quot;, &quot;Cp1251&quot;, &quot;UTF-16&quot;.

   @param str        Rexx string encoded in the <em>fromCp</em> codepage
   @param fromCp     the character set encoding used for <em>str</em>
   @param toCp       the character set encoding to be used the recoded Rexx string that gets returned
   @return           the recoded Rexx string
*/
::routine bsf.iconv public    -- cf. <https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html>
  use strict arg str, fromCp, toCp
  signal on syntax
   -- create Java String using Rexx string encoded in the fromCP codepage
  jstr=.java.lang.String~new(bsfRawBytes(str),fromCp)
   -- get Java byte array in desired toCp codepage and return it as Rexx string (encoded in toCp)
  return bsfRawBytes(jstr~getBytes(toCp))
syntax:
  raise propagate


/* ================================================================================================= */
/* ================================================================================================= */

/* ------------------------------------------------------------------------ */
/** Routine that interfaces with the Thread's context class loader. If no arguments are given
    the current Thread's context class loader gets returned. Otherwise the argument may be
    a string denoting a directory or a file, a java.io.File object, a java.net.URL object or
    an array of these kinds of values, for which a new java.net.URLClassLoader gets created
    which will be used to set the Thread's context class loader using the old context class loader
    as its parent.

    If a context class loader does not exist yet, the method
    java.lang.ClassLoader.getSystemClassLoader will be used on Java 1.6/6, 1.7/7 and 1.8/8, otherwise
    (Java 9 or higher) java.lang.ClassLoader.getPlatformClassLoader which then gets used as the
    parent of the new java.net.URLClassLoader.

   @param urlArg     optional, either a list of strings denoting a directory or a file or a URL,
                     a java.net.URL object, alternatively a single array of these kinds of values

   @return           the Thread's current class loader
*/
::routine bsf.contextClassLoader public
  use arg urlArg=.nil

  currentThread=.java.lang.Thread~currentThread       -- get current Thread object
  currentCCL   =currentThread~getContextClassLoader   -- get this Thread's context class loader, if any

  if arg()=0 then    -- no arguments given, just return current context class loader
     return currentCCL

  if arg()>1 | \urlArg~isA(.array) then
     urlArg=arg(1,"Array") -- turn argument(s) into an array

  -- process arguments, turn them into java.net.URL array
  arrUrls=bsf.createJavaArray(.java.net.URL, urlArg~size)      -- create Java array to hold the URL objects
  signal on syntax
  do i=1 to urlArg~size
     if val~isA(.bsf) then arrUrls[i]=url[i] -- assume Java object is java.net.URL otherwise error gets reported
     else
     do
         -- support for URL-encoded string, expected to be composed as "protocol:data", cater for old Windows drive letters
         val=urlArg[i]
         parse var val protocol ":" data
         if data<>"", protocol~length>1 then  -- assume a string containing an URL
            arrUrls[i]=.java.net.URL~new(val)   -- create URL object and store it
         else  -- assume a directory or a file
            arrUrls[i]=.java.io.File~new(val)~toUri~toURL
     end
  end
  drop val     -- remove VAL such that exception handler will not attempt to show its value

  -- create URLClassLoader (possible enhancement: we could subclass it to allow access to e.g. addURL() and findClass())
  if currentCCL=.nil then  -- no context class loader as of yet, get the system or platform class loader instead
  do
     if .bsf4rexx~java.major.version<9 then -- use the Java 8 system (application) class loader as parent
        currentCCL=jclzClassLoader~getSystemClassLoader     -- up to Java 1.8/8: use this as parent class loader
     else
        currentCCL=jclzClassLoader~getPlatformClassLoader   -- Java 9 or later: use this as parent class loader
  end
   -- create new URL class loader, use it to set the current Thread's context class loader
  newURLClassLoader=.java.net.URLClassLoader~new(arrUrls, currentCCL)
  currentThread~setContextClassLoader(newURLClassLoader)
  return newURLClassLoader


syntax:
  if var("VAL") then .error~say("BSF.CLS - routine BSF.contextClassLoader(): argument #" i "("val") caused syntax error")

  raise propagate



/** Encodes the currently employed versions of ooRexx, BSF.CLS and Java in a single string to ease debugging.

    @return a string containing the version information of ooRexx, BSF.CLS and Java in use
*/
::routine create_display.version_entry
    parse version "-" name "_" ver "(" . . date
    orxVersion=name ver || getRevision() "("date")"

    javaVersion    =.java.lang.System~getProperty("java.version")
    javaVersionDate=.java.lang.System~getProperty("java.version.date")  -- since Java 10 (spring 2018)
    if javaVersionDate<>.nil then
       javaVersion=javaVersion "(released:" javaVersionDate")"

    javaVersion="Java" javaVersion"," .java.lang.System~getProperty("sun.arch.data.model")"-bit" "(".java.lang.System~getProperty("os.arch")")"

    .bsf4rexx~display.version=orxVersion "/ BSF" .bsf4rexx~version "/" javaVersion "/" sysVersion()
    return

getRevision: procedure  -- starting with svn rev. 11639 (Dec 22, 2018) the source-code revision number is available
    signal on syntax

    return " r".rexxInfo~revision   -- get and return (e.g. svn) revision value
syntax:
    return ""


/** This routine takes a Rexx condition object, extracts the Java exception object and
    creates the chain of the Java exceptions such that Rexx programmers can easily
    learn about it. Each Java exception's object name gets shown together with the
    its string exception. If the optional second argument is <code>.true</code> then
    the stack trace of the source of the Java exception chain gets returned as well.

    @param co Rexx condition object (cf. <code>condition("object")</code> in the reference manual)
    @param bStackTrace optional, defaults to <code>.false</code>; if set to <code>.true</code>
              then the stack trace of the source of the Java exception chain gets returned
    @return string containing the Java exception chain or an empty string, if there cannot
                   be a a Java exception object found
    @since  2020-08-12
    @author Rony G. Flatscher
*/
::routine ppJavaExceptionChain public  -- "pretty print" the list of the Java exception chain, return it as a string
  use strict arg co, bStackTrace=.false

  jexc=co~Additional~at(2)    -- get Java Exception object
  if \jexc~isA(.bsf) then return ""    -- not a Java object

  mb=.MutableBuffer~new

  mb~append("/"~copies(39), " Environment: [", .bsf4rexx~display.version, "]", .endOfLine)
  mb~append(.endOfLine, "showing Java exception chain (trigger exception at bottom) ...", .endOfLine, .endOfLine)
  i=0
  eyecatcher="-"~copies(7)
  do while jexc~isA(.bsf)     -- loop over Java exception chain
     i+=1
     mb~append(eyecatcher, "> (Java exception # ", i, ")")
     mb~append(" caused by: [", jexc~objectName, "]:", .endOfLine)
     mb~append(jexc~toString, .endOfLine, "<", eyecatcher, " (Java exception # ", i, ")", .endofline)
     oldJexc=jexc             -- save Java exception object
     jexc=jexc~getCause       -- get cause if any left
     if jexc~isA(.bsf) then mb~append(.endOfLine)
  end

  if bStackTrace=.true then   -- show stack trace for the source exception?
  do
     barrOutputStream=.bsf~new("java.io.ByteArrayOutputStream", 8192)
     barrPrintWriter=.bsf~new("java.io.PrintWriter", barrOutputStream)
     oldJexc~printStackTrace(barrPrintWriter)   -- show stack trace
     barrPrintWriter~flush
     mb~append(.endOfLine, eyecatcher, "- stack trace from [", oldJexc~objectName, "]:", .endOfLine, .endOfLine, barrOutputStream~toString) -- get string and append
     barrPrintWriter~close
  end
  mb~append("\"~copies(39), " ... end of Java exception chain." , .endOfLine)
  return mb~string

