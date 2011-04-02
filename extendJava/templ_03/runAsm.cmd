rem @echo off

if '%1'=='' goto default
set ver=%1
echo supplied version number, ver=%ver%
goto continue

:default
set ver=4
echo default, ver=%ver%

:continue
echo continue, using ver=%ver%

del *%ver%*-asm*.java
del java_1_%ver%\*%ver*-asm*.java


java org.objectweb.asm.util.ASMifierClassVisitor        TestTemplate > TT-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestTemplate > TT-%ver%-asm-debug.java

rexx addNL.rex TT-%ver%-asm.java > TT-%ver%-asm-nl.java
rexx addNL.rex TT-%ver%-asm-debug.java > TT-%ver%-asm-debug-nl.java

java org.objectweb.asm.util.ASMifierClassVisitor        TestTemplateShort > TTS-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestTemplateShort > TTS-%ver%-asm-debug.java

rexx addNL.rex TTS-%ver%-asm.java > TTS-%ver%-asm-nl.java
rexx addNL.rex TTS-%ver%-asm-debug.java > TTS-%ver%-asm-debug-nl.java


java org.objectweb.asm.util.ASMifierClassVisitor        TestConstr > TC-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestConstr > TC-%ver%-asm-debug.java

java org.objectweb.asm.util.ASMifierClassVisitor        TestConstrFromSubclass > TCFS-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestConstrFromSubclass > TCFS-%ver%-asm-debug.java

java org.objectweb.asm.util.ASMifierClassVisitor        TestConstrFromSubclass2 > TCFS2-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestConstrFromSubclass2 > TCFS2-%ver%-asm-debug.java

java org.objectweb.asm.util.ASMifierClassVisitor        TestFromCFS > TFCFS-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TestFromCFS > TFCFS-%ver%-asm-debug.java


java org.objectweb.asm.util.ASMifierClassVisitor        ABC_Interface > ABC_I-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug ABC_Interface > ABC_I-%ver%-asm-debug.java

rexx addNL.rex ABC_I-%ver%-asm.java > ABC_I-%ver%-asm-nl.java
rexx addNL.rex ABC_I-%ver%-asm-debug.java > ABC_I-%ver%-asm-debug-nl.java


java org.objectweb.asm.util.ASMifierClassVisitor        TTS_Interface > TTS_I-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug TTS_Interface > TTS_I-%ver%-asm-debug.java

rexx addNL.rex TTS_I-%ver%-asm.java > TTS_I-%ver%-asm-nl.java
rexx addNL.rex TTS_I-%ver%-asm-debug.java > TTS_I-%ver%-asm-debug-nl.java


java org.objectweb.asm.util.ASMifierClassVisitor        XYZ_Interface > XYZ_I-%ver%-asm.java
java org.objectweb.asm.util.ASMifierClassVisitor -debug XYZ_Interface > XYZ_I-%ver%-asm-debug.java

rexx addNL.rex XYZ_I-%ver%-asm.java > XYZ_I-%ver%-asm-nl.java
rexx addNL.rex XYZ_I-%ver%-asm-debug.java > XYZ_I-%ver%-asm-debug-nl.java



move *%ver%*-asm*.java java_1_%ver%

dir java_1_%ver%
