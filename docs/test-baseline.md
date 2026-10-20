# BSF4ooRexx 850: test baseline

*20/10/2026. Build: `main` (SVN `branches/850` r1125) with
[`scripts/build.sh`](../scripts/build.sh); ooRexx 5.3.0 r13278; OpenJDK
21.0.12.1 (classes compiled with `javac --release 8`); Ubuntu 24.04 x86_64.
Runner: [`scripts/run-tests.sh`](../scripts/run-tests.sh).*

## Summary

The 67 test groups of `bsf4oorexx.dev/testUnits/bsf4rexx/` (ooRexxUnit),
run one by one:

| Pass | Groups ok | Tests | Failures | Errors | Did not run |
|---|---:|---:|---:|---:|---|
| Virtual display (Xvfb) | 65 / 67 | 992 | 0 | 0 | 2 (see below) |
| No display at all (`DISPLAY` unset) | 65 / 67 | 992 | 0 | 0 | the same 2 |
| Virtual display, library also offered under its pre-850 name | **67 / 67** | **1013** | 0 | 0 | none |
| The same on **Java 8** (OpenJDK 1.8.0_504), UTF-8 locale (`LANG=C.UTF-8`) | **67 / 67** | 1013 | 0 | 0 | none |
| The same on Java 8, POSIX locale (no `LANG`) | 63 / 67 | 1013 | 0 | 16 | none |

273,246 assertions in the third pass. No group needs a display, and none
needs Windows: every group that runs passes on Linux with no display.
The jar and the test helpers are compiled with `javac --release 8`, and the
whole suite also passes on a Java 8 runtime.

**The locale matters on Java 8, not on Java 21.** With no `LANG` (POSIX
locale), Java 8's default charset is ASCII, and four groups that send a
`char` `'ß'` through a Rexx proxy and back get it back changed (4 errors
each): `rgf/C_interfaceClasses/01_rexx_testInterface`,
`rgf/D_abstractClasses/01_rexx_testAbstract`,
`rgf/E_extendNormalClasses/01_rexx_testExtendNormal`,
`rgf/F_testClassNames/01_rexx_ClassNameMusings` (the check is in, e.g.,
`rgf/C_interfaceClasses/Test_ITest.java` line 127). With `LANG=C.UTF-8`
they pass. Java 18 and later default to UTF-8 whatever the locale (JEP
400), which is why the Java 21 passes, also run with no `LANG`, are clean.
The cause: strings crossing between Rexx and Java are mostly converted
with the JVM's default charset (`String(byte[])`, `getBytes()`), so under
ASCII a `'ß'` does not survive; only a string that a Rexx method returns to
a Java caller goes through `NewStringUTF` (`BSF4ooRexx.cc` line 2984),
whatever that charset is.

## What it takes to run them

- **The test framework.** `testUnits/ooRexxUnit/` is an old copy of the
  ooRexx test framework (ooTest 4.0.0; files dated 2007 to 2015). Several groups call assertions
  it does not have (for example `assertSameList` in
  `java6/plain/Do_over.testGroup` and `java8/plain/Do_over.testGroup`:
  SYNTAX 97.1, 6 errors each with that framework), and `setEnv4bsf4rexx.sh` says that the
  bundled copy was dropped from the path in 2017. The runner therefore uses
  the current framework from ooRexx's SVN (`test/trunk`: `testOORexx.rex`,
  `worker.rex`, `ooTest.frm`, `framework/*.cls`) at revision 13278, the
  same as the interpreter; `--framework bundled` uses the old copy.
- **The Java helper classes** of the tests (`rgf.*`, `java6.plain`,
  `java8.plain`, `java8.defaultMethods`, `rgf_multithreading`; `UseTest2` in
  the unnamed package) are compiled in place in a copy of the tree, as
  their `compile*.cmd` do. `java9/` has no test groups and is not compiled.
- **The pre-850 library name.** Two groups,
  `lex/procedural/Bean.testGroup` (line 50) and
  `lex/procedural/201811_strictArg.testGroup`, load the native functions
  the classic way, `call rxFuncAdd "BsfLoadFuncs", "BSF4ooRexx",
  "BsfLoadFuncs"`: the library's name before 850 (now
  `BSF4ooRexx850`). Without a library by that name their `setUp` fails
  (`Error 43.1:  Could not find routine "BSFLOADFUNCS".`) and the framework
  then waits until the time limit (180 s) instead of reporting an error.
  With a symbolic link `libBSF4ooRexx.so` → `libBSF4ooRexx850.so`
  (`--legacy-lib-name`) both pass (7 and 14 tests). The other 65 groups use
  `::requires BSF.CLS` or the library's 850 name.
- The published `BSF4ooRexx_testUnits.zip` (SourceForge, `GA/`) has the
  same 67 groups; it also carries a few scratch scripts that are not in SVN.

## Per group

| Group | Tests | Assertions | Virtual display | No display | Legacy lib name |
|---|---:|---:|---|---|---|
| `java6/plain/Do_over.testGroup` | 6 | 150 | ok | ok | ok |
| `java6/plain/FieldTestUnit.testGroup` | 13 | 575 | ok | ok | ok |
| `java6/plain/InvokeTestUnit.testGroup` | 6 | 540 | ok | ok | ok |
| `java8/defaultMethods/defaultMethods.testGroup` | 13 | 25 | ok | ok | ok |
| `java8/plain/Do_over.testGroup` | 6 | 150 | ok | ok | ok |
| `java8/plain/FieldTestUnit.testGroup` | 13 | 575 | ok | ok | ok |
| `java8/plain/InvokeTestUnit.testGroup` | 7 | 750 | ok | ok | ok |
| `lex/oo/Array.testGroup` | 174 | 174 | ok | ok | ok |
| `lex/oo/Bean.testGroup` | 15 | 17 | ok | ok | ok |
| `lex/oo/FieldValue.testGroup` | 20 | 20 | ok | ok | ok |
| `lex/oo/eventText.testGroup` | 15 | 15 | ok | ok | ok |
| `lex/oo/getConstant.testGroup` | 4 | 4 | ok | ok | ok |
| `lex/oo/getStaticValue.testGroup` | 8 | 8 | ok | ok | ok |
| `lex/oo/invoke.testGroup` | 19 | 19 | ok | ok | ok |
| `lex/oo/objects/Boolean.testGroup` | 13 | 12 | ok | ok | ok |
| `lex/oo/objects/Integer.testGroup` | 11 | 8 | ok | ok | ok |
| `lex/oo/objects/String.testGroup` | 6 | 6 | ok | ok | ok |
| `lex/oo/setRexxNullString.testGroup` | 3 | 3 | ok | ok | ok |
| `lex/oo/sleep.testGroup` | 6 | 6 | ok | ok | ok |
| `lex/oo/version.testGroup` | 17 | 17 | ok | ok | ok |
| `lex/procedural/201811_strictArg.testGroup` | 7 | 67 | timeout | timeout | ok |
| `lex/procedural/Array.testGroup` | 317 | 322 | ok | ok | ok |
| `lex/procedural/BSFVersion.testGroup` | 2 | 2 | ok | ok | ok |
| `lex/procedural/Bean.testGroup` | 14 | 66 | timeout | timeout | ok |
| `lex/procedural/BsfInvokedBy.testGroup` | 2 | 2 | ok | ok | ok |
| `lex/procedural/EventListener.testGroup` | 3 | 3 | ok | ok | ok |
| `lex/procedural/FieldValue.testGroup` | 20 | 20 | ok | ok | ok |
| `lex/procedural/bsfPrefixReturnValue.testGroup` | 31 | 31 | ok | ok | ok |
| `lex/procedural/eventText.testGroup` | 12 | 12 | ok | ok | ok |
| `lex/procedural/functions.testGroup` | 4 | 4 | ok | ok | ok |
| `lex/procedural/getBSFManager.testGroup` | 2 | 2 | ok | ok | ok |
| `lex/procedural/getStaticValue.testGroup` | 8 | 8 | ok | ok | ok |
| `lex/procedural/invoke.testGroup` | 11 | 11 | ok | ok | ok |
| `lex/procedural/setRexxNullString.testGroup` | 3 | 3 | ok | ok | ok |
| `lex/procedural/sleep.testGroup` | 7 | 7 | ok | ok | ok |
| `lex/procedural/version.testGroup` | 17 | 17 | ok | ok | ok |
| `rgf/A_BsfRexxProxy/01_rexx_testBsfProxyObject.testGroup` | 6 | 13 | ok | ok | ok |
| `rgf/B_callback/01_rexx_testCallback.testGroup` | 6 | 6 | ok | ok | ok |
| `rgf/C_interfaceClasses/01_rexx_testInterface.testGroup` | 4 | 136 | ok | ok | ok |
| `rgf/C_interfaceClasses/02_rexx_testInterfaceCallback.testGroup` | 6 | 6 | ok | ok | ok |
| `rgf/D_abstractClasses/01_rexx_testAbstract.testGroup` | 5 | 139 | ok | ok | ok |
| `rgf/D_abstractClasses/02_rexx_testAbstractCallback.testGroup` | 6 | 6 | ok | ok | ok |
| `rgf/D_abstractClasses/03_rexx_testAbstractCallback.testGroup` | 12 | 89 | ok | ok | ok |
| `rgf/E_extendNormalClasses/01_rexx_testExtendNormal.testGroup` | 5 | 143 | ok | ok | ok |
| `rgf/E_extendNormalClasses/02_rexx_testExtendNormalCallback.testGroup` | 6 | 12 | ok | ok | ok |
| `rgf/E_extendNormalClasses/03_rexx_testExtendNormalCallback.testGroup` | 13 | 121 | ok | ok | ok |
| `rgf/F_testClassNames/01_rexx_ClassNameMusings.testGroup` | 5 | 143 | ok | ok | ok |
| `rgf/G_misc/01_rexx_testBsfRawBytes.testGroup` | 8 | 1799 | ok | ok | ok |
| `rgf/G_misc/02_rexx_test_bsf_isA.testGroup` | 4 | 12 | ok | ok | ok |
| `rgf/G_misc/03_rexx_testInvokeJSO.testGroup` | 7 | 122 | ok | ok | ok |
| `rgf/G_misc/04_rexx_BsfContextVariables.testGroup` | 1 | 28 | ok | ok | ok |
| `rgf/G_misc/05_rexx_BSF_Input_Output_Stream.testGroup` | 4 | 172 | ok | ok | ok |
| `rgf/G_misc/06_rexx_BSF_Redirect_To.testGroup` | 1 | 70 | ok | ok | ok |
| `rgf/G_misc/07_rexx_JSR_223.testGroup` | 3 | 106 | ok | ok | ok |
| `rgf/G_misc/08_do_over.testGroup` | 4 | 9 | ok | ok | ok |
| `rgf/G_misc/09_bsf_compile.testGroup` | 4 | 17 | ok | ok | ok |
| `rgf/G_misc/10_box_boxStrictArg.testGroup` | 10 | 137 | ok | ok | ok |
| `rgf/G_misc/11_box_boxStrictArg_with_Java_class.testGroup` | 21 | 196 | ok | ok | ok |
| `rgf/G_misc/12_declareBean.testGroup` | 1 | 11 | ok | ok | ok |
| `rgf/G_misc/15_rexx_NestedClasses.testGroup` | 17 | 19 | ok | ok | ok |
| `rgf/H_850/01_sendMailScoped.testGroup` | 11 | 47 | ok | ok | ok |
| `rgf/H_850/05_redirectingHandler.testGroup` | 1 | 132241 | ok | ok | ok |
| `rgf/H_850/06_redirectingHandler_preconfigured.testGroup` | 1 | 132238 | ok | ok | ok |
| `rgf_multithreading/01_test_MultiThreading_Rexx.testGroup` | 3 | 66 | ok | ok | ok |
| `rgf_multithreading/80_test_MultiThreading_via_Java_threads.testGroup` | 1 | 1381 | ok | ok | ok |
| `rgf_multithreading/81_test_MultiThreading_via_Java_threads.testGroup` | 1 | 61 | ok | ok | ok |
| `rgf_multithreading/90_test_MultiThreading_via_Java_threads.testGroup` | 1 | 49 | ok | ok | ok |

"Tests" and "Assertions" are from the last pass. The full logs are not
kept in the repository; `scripts/run-tests.sh` writes them to
`build/tests/<display>-<framework>/logs/`.

## To reproduce

The CI (`.github/workflows/ci.yml`) runs the build and the third pass on
every push, on Java 21 and on Java 8 (with `LANG=C.UTF-8`), Linux only.
Locally:

```sh
scripts/setup-oorexx.sh deb                        # ooRexx at the CI's revision
scripts/build.sh                                   # needs ooRexx 5.x and a JDK
scripts/run-tests.sh                               # Xvfb if available
scripts/run-tests.sh --display none
scripts/run-tests.sh --legacy-lib-name
LANG=C.UTF-8 TEST_JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 scripts/run-tests.sh --legacy-lib-name
```

On Java 8 the runner puts `jre/lib/amd64/server` on `LD_LIBRARY_PATH`:
the library's own lookup through `JAVA_HOME` tries `lib/server` and
`jre/lib/server` (`BSF4ooRexx.cc`, around line 5635), which is where
Java 9 and later keep `libjvm.so`, not Java 8 on Linux; without it,
Rexx-first start fails with *Java dynamic link/shared library
"/opt/BSF4ooRexx/libjvm.so" not found*.
