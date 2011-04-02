call bsf.cls
clz=bsf.loadClass("org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool")
say "clz="pp(clz)

clz~bss =.true
clz~bTiming=.true

clzName   ="java.io.File"
-- methodNames2proxy=bsf.createArrayOf("java.lang.String", "getAbsoluteFile",  "isFile")
methodNames2proxy=bsf.createArrayOf("java.lang.String", "getAbsoluteFile",  "isFile",  "deleteOnExit", -
                                                        "listFiles", "createTempFile", "<init>")
--                                                        "listFiles", "createTempFile")
--                                                        "listFiles", "compareTo", "createTempFile")


/* works
clzName   ="java.awt.Frame"
methodNames2proxy=bsf.createArrayOf("java.lang.String", "getTitle", "setTitle")
-- */

javaClassToProxy=bsf.loadClass(clzName)

newClzName="Dora"


newClz=clz~createProxiedJavaClass(javaClassToProxy, newClzName, methodNames2proxy)

say "worked, newClz="pp(newClz)
meths=newClz~getDeclaredMethods
say meths~items "methods:"
do m over meths
   say " " m~toString
end



::requires bsf.cls

call bsf.cls
clz=bsf.loadClass("org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool")
-- clz~bss =.true
clz~bTiming=.true
clzName   ="java.io.File"
javaClassToProxy=bsf.loadClass(clzName)
methodNames2proxy=bsf.createArrayOf("java.lang.String", "getAbsoluteFile",  "isFile")
newClzName="Dora"
newClz=clz~createProxiedJavaClass(javaClassToProxy, newClzName, methodNames2proxy)
say newClz newClz~toString


--
call bsf.cls
clzPJCT=bsf.loadClass("org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool")

clzPJCT~bss =.true
clzPJCT~bTiming=.true

clz3Name="javax.swing.filechooser.FileFilter"
clz3=bsf.loadClass(clz3name)

newClz3=clzPJCT~createProxiedJavaClass(clz3, "Marion", .nil)
say newClz3
say newClz3~toString

newClz3=clzPJCT~createProxiedJavaClass(clz3, "org.java.io.Marion", .nil)
say newClz3
say newClz3~toString

newClz3=clzPJCT~createProxiedJavaClass(clz3, .nil, .nil)
say newClz3
say newClz3~toString

-- ====
call bsf.cls
clz=bsf.createProxyClass("javax.swing.filechooser.FileFilter")
say bsf.lookupBean(clz~string); say clz
a=clz~new; say a


clz=bsf.createProxyClass("java.io.File", "Maxi", "getAbsoluteFile", "isFile")
