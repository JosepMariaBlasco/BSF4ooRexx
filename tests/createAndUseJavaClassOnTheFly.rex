/*    author:  Rony G. Flatscher
      date:    2011-05-31
      purpose: demonstrate the (as of yet) undocumented possibility in BSF4ooRexx to create
               Java classes on the fly, but also to create proxy classes from any Java class
               in which method invocations are forwarded to ooRexx

      needs:   BSF4ooRexx, at least version 406.20110531
      nint:    BSF4ooRexx comes with janino and can use its SimpleCompiler to easily compile
               Java code; the very first invocation may take time for janino to initialize
               fully; consecutive invocations are fast (enough)

      changes: ---
      license: Apache License 2.0 (AL 2.0) or Common Public License 1.0/1.1 (CPL 1.0, CPL 1.1)
*/

nl="0a"x
javaCode=       -
   'package rony.rgf;                                                                           '  nl -
   '                                                                                            '  nl -
   'public class A {                                                                            '  nl -
   '  public static int statField = 123;                                                        '  nl -
   '                                                                                            '  nl -
   '  public static void main (String args[])                                                   '  nl -
   '  {                                                                                         '  nl -
   '      System.out.println("statField=["+statField+"]");                                      '  nl -
   '      System.out.println("statMethod()=["+statMethod()+"]");                                '  nl -
   '      System.out.println("statMethod(\"hey\")=["+statMethod("hey")+"]");                    '  nl -
   '                                                                                            '  nl -
   '      A obj=new A();                                                                        '  nl -
   '      System.out.println("obj=["+obj+"]");                                                  '  nl -
   '      System.out.println("obj.instMethod()=["+obj.instMethod()+"]");                        '  nl -
   '      System.out.println("obj.instMethod(\"heya\")=["+obj.instMethod("heya")+"]");          '  nl -
   '  }                                                                                         '  nl -
   '                                                                                            '  nl -
   '  public static String statMethod ()                                                        '  nl -
   '  {                                                                                         '  nl -
   '      return "statMethod()";                                                                '  nl -
   '  }                                                                                         '  nl -
   '                                                                                            '  nl -
   '  public static String statMethod (String str)                                              '  nl -
   '  {                                                                                         '  nl -
   '      return "statMethod(String str), received: ["+str+"]";                                 '  nl -
   '  }                                                                                         '  nl -
   '                                                                                            '  nl -
   '                                                                                            '  nl -
   '  public String instMethod ()                                                               '  nl -
   '  {                                                                                         '  nl -
   '      return "instMethod(), this=["+this+"]";                                               '  nl -
   '  }                                                                                         '  nl -
   '                                                                                            '  nl -
   '  public String instMethod (String str)                                                     '  nl -
   '  {                                                                                         '  nl -
   '      return "instMethod(String str), this=["+this+"] str=["+str+"]";                       '  nl -
   '  }                                                                                         '  nl -
   '                                                                                            '  nl -
   ' };                                                                                         '

say "="~copies(79)
say "JavaCode:"
say pp(javaCode)
say

say "--- now compiling Java code"
sc=.bsf~new("org.codehaus.janino.SimpleCompiler")
call time "R"
sc~cook(javaCode)       -- compile Java code
say "cooking needed:" time("R")
clzLoader=sc~getClassLoader();
say "loading ClassLoader needed:" time("R")

-- clzName="A"
clzName="rony.rgf.A"
clz=clzLoader~loadClass(clzName)
say "loading class needed:" time("R")

say "clz=         "pp(clz)
say "clz~toString="pp(clz~toString)
say
say "--- now invoking main() method:"
clz~main(bsf.createJavaArray("java.lang.String",0))
say

say "--- now interfacing from ooRexx:"
say "clz~statField:               " pp(clz~statField)
say "clz~statMethod:              " pp(clz~statMethod)
say "clz~statMethod('eia,popeia'):" clz~statMethod('eia,popeia')
say

-- create a new BSF proxy class that attaches the new and newStrict methdos to ease creation of instances
clzImported=.bsf~bsf.importClass(clzName, .nil, clz)  -- second argument is .nil, hence do not store class object in .local
obj=clzImported~new
say "created obj:" pp(obj) "obj~toString:" pp(obj~toString)
say
say "obj~statField:               " pp(obj~statField)
say "obj~statMethod:              " pp(obj~statMethod)
say "obj~statMethod('eia,popeia'):" pp(obj~statMethod('eia,popeia'))
say
say "obj~instMethod:              " pp(obj~instMethod)
say "obj~instMethod('oopsla'):    " pp(obj~instMethod('oopsla'))
say "---"
say

say "="~copies(79)
say "----------------------------"
say "--- now creating a Java proxy class which forwards all method invocations to a Rexx proxy:"
say


   -- load the Java class that does the magic for us
pjctClz   =bsf.loadClass("org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool")    -- janino-based
   -- proxy constructor and all method invocations
methods2proxy=bsf.createArrayOf("java.lang.String", "<init>", "statMethod", "instMethod")

say "clz=         "pp(clz)
say "clz~toString="pp(clz~toString)

call time "R"
proxyClass=pjctClz~createProxiedJavaClass(clz, .nil, methods2proxy)
say "cooking needed:" time("R")


   -- creates a Java class that forwards all Java method invocations to a Rexx proxy:
importedProxyClass=.bsf~bsf.importclass(proxyClass~getName, .nil, proxyClass)

say "creating a RexxProxy ..."
   -- returns a Java object forwarding Java method invocations to the Rexx object:
rexxProxy=BsfCreateRexxProxy(.MyProxyClass~new)

say "creating an instance of the proxy class, supplying the RexxProxy object as argument..."
proxy=importedProxyClass~new(rexxProxy)   -- create an instance of the proxy class supplying the Rexx proxy
say

say "created proxy:" pp(proxy) "proxy~toString:" pp(proxy~toString)
say
say "proxy~statField:               " pp(proxy~statField)
say "proxy~statMethod:              " pp(proxy~statMethod)
say "proxy~statMethod('eia,popeia'):" pp(proxy~statMethod('eia,popeia'))
say
say "proxy~instMethod:              " pp(proxy~instMethod)
say "proxy~instMethod('oopsla'):    " pp(proxy~instMethod('oopsla'))
say "---"


::requires BSF.CLS            -- get Java support
::requires "rgf_util2.rex"    -- get access to public routine dump2()

   /* the Rexx class that proxies the Java class; its instances can be used in lieu of
      the respective Java object instance */
::class MyProxyClass

::method unknown              /* fetch all messages that have no corresponding method implemented */
  use arg methName, methArgs
  say "."~copies(40)
  say "...ooRexx/unknown" "args="pp(methArgs~items-1)

  slotDir=methArgs[methArgs~items]    -- fetch slotDir, which is always the last argument
  call dump2 slotDir, "...ooRexx/unknown" "slotDir's content:"   -- show slotDir's content

  methName=slotDir~methodName || "_forwardToSuper"    -- create method name to use

  say "...ooRexx/unknown" "determined and about to execute method:" pp(methName) "..."
  res=slotDir~javaObject~sendwith(methName, methArgs~section(1,methArgs~items-1))~reverse

  say "---"
  return res


      /* the proxied Java class has two "instMethod" methods, one without an argument,
         one with a String argument; so we need to differentiate */
::method "<init>"    /* "<init>" is the special (internal) Java name for constructors */
  say "."~copies(40)
  say "...ooRexx/<init>" "(proxying a Java constructor method)" "arg():" arg()
  use arg slotDir

  call dump2 slotDir, "...ooRexx/<init>" "slotDir's content:"   -- show slotDir's content
  say "---"


      /* the proxied Java class has two "instMethod" methods, one without an argument,
         one with a String argument; so we need to differentiate */
::method instMethod
  say "."~copies(40)
  say "...ooRexx/instMethod" "arg():" arg()

  str=.nil
  if arg()=1 then    -- only one argument, must be slotDir-argument
  do
     use arg slotDir
  end
  else
  do
     use arg str, slotDir
     say "...ooRexx/instMethod" "received a 'str' argument with a value of:" pp(str)
  end
  call dump2 slotDir, "...ooRexx/instMethod" "slotDir's content:"   -- show slotDir's content

  say "...ooRexx/instMethod" "forwarding invocation to super (and returning its result) ..."
  res=.nil
  if str<>.nil then
     res=slotDir~javaObject~instMethod_forwardToSuper(str)~reverse
  else
     res=slotDir~javaObject~instMethod_forwardToSuper~reverse

  say "---"
  return res

