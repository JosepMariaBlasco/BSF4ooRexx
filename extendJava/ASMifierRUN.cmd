@echo off
@echo %0: processing class "%1", all args: "%*"
java org.objectweb.asm.util.ASMifierClassVisitor %*
