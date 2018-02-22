setlocal

@rem ok: using installed BSF4ooRexx jar!
@rem ok: set BSF4Rexx_JavaStartupOptions=--module-path F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out --add-modules mod_A,mod_B,mod_C


@rem ok: using current BSF4ooRexx from development directory !!
set BSF4Rexx_JavaStartupOptions=-cp "%CLASSPATH%" --module-path F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out --add-modules mod_A,mod_B,mod_C

@rem almost ok, mod_C missing: set BSF4Rexx_JavaStartupOptions=--module-path F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out\test-modules-abc.jar --add-modules mod_A,mod_B,mod_C

@rem set BSF4Rexx_JavaStartupOptions=--module-path F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out

@rem set BSF4Rexx_JavaStartupOptions=-cp %CLASSPATH%;F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out\test-modules-abc.jar

@rem set BSF4Rexx_JavaStartupOptions=--module-path F:\work\svn\bsf4oorexx\trunk\bsf4oorexx.dev\testUnits\bsf4rexx\java9modules\out\test-modules-abc.jar
echo BSF4Rexx_JavaStartupOptions=%BSF4Rexx_JavaStartupOptions%

rexxj "%1" %*

endlocal
