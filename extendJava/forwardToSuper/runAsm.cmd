@echo off

if '%1'=='' goto default
set ver=%1
echo supplied version number, ver=%ver%
goto continue

:default
set ver=4
echo default, ver=%ver%

:continue
echo continue, ver=%ver%

java org.objectweb.asm.util.ASMifierClassVisitor        Test > T-%ver%-asm.java
rexx addNL T-%ver%-asm.java > T-%ver%-asm-nl.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug Test > T-%ver%-asm-debug.java
rexx addNL T-%ver%-asm-debug.java > T-%ver%-asm-debug-nl.java

java org.objectweb.asm.util.ASMifierClassVisitor        TestSub > TS-%ver%-asm.java
rexx addNL TS-%ver%-asm.java > TS-%ver%-asm-nl.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestSub > TS-%ver%-asm-debug.java
rexx addNL TS-%ver%-asm-debug.java > TS-%ver%-asm-debug-nl.java

java org.objectweb.asm.util.ASMifierClassVisitor        UseTest > UT-%ver%-asm.java
rexx addNL UT-%ver%-asm.java > UT-%ver%-asm-nl.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug UseTest > UT-%ver%-asm-debug.java
rexx addNL UT-%ver%-asm-debug.java > UT-%ver%-asm-debug-nl.java

del /q java_1_%ver%
copy *-%ver%-* java_1_%ver%
del *-%ver%-*

