# BSF4ooRexx 850: inventory of the public surface

*20/10/2026. What a program can use from BSF4ooRexx 850, where each
piece is defined, whether it is documented and whether the tests touch
it. The item-by-item tables are generated:
[`inventory-tables.md`](inventory-tables.md), by
[`scripts/inventory.py`](../scripts/inventory.py) (run it after
`scripts/build.sh java`). This page is the overview, the method and what
was found on the way.*

Source: `main`, i.e. SVN `branches/850` r1125 (17/01/2026). The test
results referred to are in [`test-baseline.md`](test-baseline.md).

## 1. The layers

```
 Rexx program                                     Java program
   │  ::requires BSF.CLS                             │  BSFManager / javax.script / RexxDispatcher
   ▼                                                 ▼
 BSF.CLS ── classes BSF, BSF4REXX, ...,          org.rexxla.bsf.engines.rexx
   │        routines bsf.*, box, unbox, ...        RexxEngine, RexxScriptEngine(Factory),
   │  ::requires "BSF4ooRexx850" library           RexxProxy, RexxAndJava, handlers, ...
   ▼                                                 ▲
 libBSF4ooRexx850.so ── Rexx routines BSF(),         │ JNI (Java_..._jni* functions)
   │   BsfCreateRexxProxy(), BsfGetTID(), ...        │
   │ JNI: RexxAndJava.javaCallBSF(String[])  ─────────┘
   ▼
 RexxAndJava: 56 subfunctions (invoke, getFieldValue, createArrayOf, ...)
   ▼
 RexxReflectJava7 (Java >= 8): finds and calls the Java member by reflection
```

The surface a Rexx programmer sees is mostly **dynamic**: a `BSF` object
forwards any unknown message to Java (`BSF~unknown`), so every public
member of every Java class is reachable, and what matters for
compatibility there is the conversion of arguments and results, studied in
`oorexx-bridges` (`topics/conversion-rules/java.md`), not a list of names.
The static surface listed here is what the bridge itself adds.

## 2. How big it is

| Part | Where | Count | Doc next to it | In the tests |
|---|---|---:|---|---|
| Rexx: `BSF.CLS` | `bsf4oorexx.dev/bin/BSF.CLS` | 194 public items (10 public classes; methods, attributes, routines), 85 non-public; 7 environment entries set literally | 51 ooRexxDoc, 126 another comment, 17 none | 74 of the 164 non-generic names appear |
| Rexx: `rgf_util2.rex` | `bin/` | 75 public | 1 ooRexxDoc, 43 comment, 31 none | 7 of 67 |
| Rexx: `UNO.CLS`, `UNO_ReflectionUtil.cls`, `UNO_*.rxo` | `bin/` | 64 + 75 + 2 public | no ooRexxDoc comments; another comment on 45 + 18 + 2 | 10 names appear (no OpenOffice/LibreOffice in the tests) |
| Rexx: `BSF_OnTheFly.cls`, `rexxcUtil.rex`, `rexxb4r.rex`, `addLinks2OOoSnippets.rxo` | `bin/` | no public items (helpers and programs) | | |
| Subfunctions of `BSF()` | `source_java/InvocationType.java`, `RexxAndJava.java` | 56 (+5 synonyms) | 33 on the reference card | 39 appear, 15 do not (`exit` and `version` too generic to search) |
| Native Rexx routines | `source_cc/BSF4ooRexx.cc`, table `bsf_external_functions[]` | 26 (one only `#ifdef HOT_FIX_CRASH`, not defined) | 16 on the reference card | 16 appear |
| JNI functions | `BSF4ooRexx.cc` | 50 | | (indirectly) |
| Java: `org.rexxla.*`, `org.oorexx.*` | `source_java/`, `org/oorexx/` | 37 public classes, 648 public/protected members | 499 with Javadoc | 133 of the 547 non-generic names appear |
| Java: Apache BSF 2.4 (Rony's copy) | `org/apache/bsf/` | class level only | | |

"Appears in the tests" is a textual search over `testUnits/bsf4rexx/`, an
indicator and not a coverage measure (§6).

The 15 subfunctions whose names appear in no test file: `wrapArray`,
`increaseRefCounter`, `get_RII_id`, `invokeStrictJSO`, `wrapEnumeration`,
`addEventListener`, `carryOutSystemExit`, `getEnv`, `setEnv`,
`getPropertyValue`, `kudos`, `haltThread`, `setPropertyValue`,
`setPropertyValueStrict`, `testPing`. Native routines likewise:
`BsfDoUnregisterRexxObject`, `BsfDropFuncs`, `BsfGetRIID`, `BsfGetRTC`,
`BsfJavaException`, `BsfUnloadJava`, `BsfGetRegistryObjects`,
`BsfApplyHotFixCrash`, `BsfUninit4JavaBean`, `BsfTestPing`.

## 3. Entry points and command-line tools

| Entry point | What it is | Where |
|---|---|---|
| `::requires BSF.CLS` | Rexx-first: `rexx` loads `BSF.CLS`, which loads `libBSF4ooRexx850.so`, which loads the JVM (`JAVA_HOME`, then the system; options from `BSF4Rexx_JavaStartupOptions` and `JDK_JAVA_OPTIONS`, `BSF.CLS` `parseJavaOptions` line 1532) | `bin/BSF.CLS` |
| `call rxFuncAdd "BsfLoadFuncs", "BSF4ooRexx850", "BsfLoadFuncs"; call BsfLoadFuncs` | the classic (procedural) way to get `BSF()` and the `Bsf*()` functions. `BsfLoadFuncs` and `BsfDropFuncs` are stubs kept for backward compatibility (they return `"0"`; `.cc` lines 6254, 6277); `BsfLoadJava` and `BsfUnloadJava` (5488, 6145), despite the same comment, start and destroy the JVM (`#define ALLOW_LOADING_JVM`, line 1078), and `BSF.CLS` loads Java through `BsfLoadJava` (line 973) | `source_cc/BSF4ooRexx.cc` |
| `org.rexxla.bsf.RexxDispatcher` | Java-first: `java org.rexxla.bsf.RexxDispatcher program.rex args`; the command-line arguments are also in the BSF registry as `allCommandLineArguments` | `source_java/RexxDispatcher.java:104` |
| `rexxj.sh` / `rexxj.cmd`, `rexxj2.sh` / `.cmd`, `rexxjh.sh` / `.cmd` | shell launchers for `RexxDispatcher` (`rexxj.sh` in `bsf4oorexx.dev/` is a 2011 template with a Mac path; the installer writes the real ones; `rexxj2`, `rexxjh` in `bin/`) | `bsf4oorexx.dev/`, `bin/` |
| BSF language `rexx` | `BSFManager` engine `org.rexxla.bsf.engines.rexx.RexxEngine`, extensions `rex rexx cls rxj rxs jrexx` | `org/apache/bsf/Languages.properties:32` |
| JSR-223 `ooRexx` | `javax.script` engine, factory `RexxScriptEngineFactory` (names `rexx`, `Rexx`, `REXX`, `oorexx`, `ooRexx`, `OOREXX`, ...), registered in `META-INF/services/javax.script.ScriptEngineFactory` | `source_java/RexxScriptEngineFactory.java` |
| `org.apache.bsf.Main` | Apache BSF's own command line (`-lang`, `-in`, ...) | `org/apache/bsf/Main.java` |
| Command handlers, exit handlers | Java classes a Java host implements to receive Rexx commands and exits (`AbstractDirectCommandHandler`, `AbstractRedirectingCommandHandler`, `AbstractExitHandler`, ...), and `BsfCommandHandler()` on the Rexx side | `source_java/`, §4 of the tables |
| JDOR | `org.oorexx.handlers.jdor.JavaDrawingHandler`, a Rexx command handler for drawing (Java2D) | `source_java/JavaDrawingHandler.java`, `samples/jdor.cls` |
| Installation scripts | `install/linux/install.sh` etc., `setupBSF.rex`, `setupJava.rex`, `setupOOo.rex`, ... | `bsf4oorexx.dev/install/` |
| GUI tools | `ooRexxTry.rxj` (Swing), `ooRexxTryFX` | `bsf4oorexx.dev/utilities/` |

Other `main()` methods (test drivers inside library classes) are listed in
§4.3 of the tables.

## 4. Documentation that exists

- **ooRexxDoc** generated from the Rexx packages:
  `bsf4oorexx.dev/information/oorexxdoc/{BSF,UNO,CLR,rgf_util2.rex}/` (HTML).
  It lists every directive; the prose in it comes from the `/** */`
  comments, which is what the **Src** column of the tables measures.
- **Reference cards**: `bsf4oorexx.dev/ReferenceCards/BSF4ooRexx/refcardBSF4ooRexx.pdf`
  (and `.odt`), `.../OOo/refcardOOo.pdf`, `.../rgf_util/`. The **Refcard**
  column searches the BSF4ooRexx card's text.
- **Javadoc** in the Java sources; the published distribution carries it
  generated (`information/docs.bsf4oorexx/`, `docs.apache.bsf/`), not in SVN.
- **Articles and readmes**: `bsf4oorexx.dev/information/*.pdf`,
  `readme*.txt`, `information/jdor/jdor_doc.html`,
  `information/installation/*.html`; `changesBSF4ooRexx.txt` is the change
  log.
- In-code history comments at the top of `BSF.CLS`, `BSF4ooRexx.cc` and
  each Java class (dated entries since 2001).

## 5. Found on the way

Technical facts, each with its evidence; none of them is a judgement about
the code, and none has been reported anywhere.

1. **The published 850 is not exactly SVN r1125.** The latest published
   distribution, `BSF4ooRexx_install_v850-20260326-refresh.zip`, has a
   `BSF.CLS` with changes dated 04/02 to 23/02/2026 that SVN does not have
   (adding `-XX:+IgnoreUnrecognizedVMOptions` and
   `--enable-native-access=ALL-UNNAMED` to the JVM options,
   "BSF_Medication"; version string `850.20260326` instead of
   `850.20260117`); the launchers `rexxj2.sh`, `rexxj2.cmd`, `rexxjh.sh` and
   `rexxjh.cmd` define `BSF_Medication` with the same options (2026-02-11);
   and `rexxb4r.rex` has a new comment block. `UNO.CLS` and
   `UNO_ReflectionUtil.cls` differ only in expanded `$Revision$` keywords.
   The Java side matches: the 748 `.class` files committed to the tree that
   the jar carries are byte-identical to the ones in
   `bsf4ooRexx-v850-20260326-bin.jar` (the tree also commits 25 classes of
   `org/apache/commons/logging`, without sources, which the jar does not
   carry), and the jar built here has the same classes and member
   signatures (2,286 named members by `javap -p` outside anonymous classes,
   compiler-generated ones apart). The published
   `libBSF4ooRexx850.so-64-amd64` and the one built here export the same
   120 global functions (they differ in weak, inline symbols: 244 against
   11). So "compatible with 850" needs a reference version: the
   SVN tree, or a given published refresh.
2. **Three version strings.** On this build, `.bsf4rexx~version` (BSF.CLS)
   is `850.20260117`, `BsfVersion()` (native) `850.20240510`, and
   `BSF("version")` (Java) `850.20250827`: each layer carries its own date.
3. **A JNI export without its Java declaration.** `BSF4ooRexx.cc` still
   defines `Java_org_rexxla_bsf_engines_rexx_RexxCleanupRef_jniRexxHaltInterpreterInstance`
   (line 10782), and the committed header
   `source_cc/org_rexxla_bsf_engines_rexx_RexxCleanupRef.h` declares it,
   but `RexxCleanupRef.java` no longer has that `native` method; `javac -h`
   gives a header without it (and with `bDebug` 0 instead of 1).
   `RexxAndJava.jniRexxHaltInterpreterInstance` exists and is declared.
4. **Conditional native routines.** `BsfApplyHotFixCrash` is in the routine
   table only `#ifdef HOT_FIX_CRASH`, which is commented out (`.cc` line
   924), so it is not in the library; `BsfGetRegistryObjects` depends on
   `CONFIG_MAKE_OREXX_REGISTRY_ACCESSIBLE`, which is defined (line 1009,
   "meant for debugging").
5. **The Java master copies.** `bsf4oorexx.dev/source_java/` holds the 31
   sources of `org.rexxla.bsf*` (and `JavaDrawingHandler`); `cmpj.cmd`
   compiles them and copies them into `org/rexxla/...`. The tree's
   `org/rexxla/` has 15 of those sources (identical) and 25 class files;
   the other classes are build products not committed.
6. **What the published jar has that SVN does not**: `NOTICE.txt`,
   `org/apache/bsf/listJavaFiles.rex`, and four small resource files in
   `org/oorexx/datergf/`. Two compiler-generated classes,
   `EventAdapterGenerator$1` and `RexxCleanupRef$2`, are in the published
   jar and committed in SVN, but `javac` 21 does not produce them from these
   sources, so the jar built here lacks them. The jar also has
   `org/oorexx/handlers/jdor/` (built from
   `source_java/JavaDrawingHandler.java`).
7. **Tests that use the pre-850 library name**, and a bundled test
   framework older than the tests: see [`test-baseline.md`](test-baseline.md).
8. **`org/apache/bsf/util/event/rgf/EventAdapterRegistry.java`** declares
   `org.apache.bsf.util.event.EventAdapterRegistry`, the same class as
   `org/apache/bsf/util/event/EventAdapterRegistry.java`; the build leaves
   it out, as the published jar does.

## 6. Method, and its limits

- **Rexx**: every directive of the packages in `bin/`, after removing
  comments (nested `/* */` and `--`); public = a public class's non-private
  methods, attributes and constants, and public routines. Environment
  entries = assignments to `.local~x` / `.environment~x` and their
  `put`/`setEntry`. Names created at run time (for instance the many
  entries `BSF.CLS` puts into `.bsf4rexx`) are only found when they are
  literal.
- **Subfunctions**: the enum `InvocationType` and the `case` labels in
  `RexxAndJava.javaCallBSF`.
- **Native**: the routine table and `RexxRoutineN(...)` definitions; the
  `JNIEXPORT` functions, matched with the `native` declarations in the Java
  sources.
- **Java**: `javap -protected` on the build, then the declaration located
  in the source by name (overloads in declaration order).
- **Documentation**: `/** */` before a Rexx directive or a Java member (an
  ordinary comment is counted apart for Rexx), and the name in the text of
  the BSF4ooRexx reference card.
- **Tests**: the number of files under `testUnits/bsf4rexx/` (groups, Rexx
  and Java helpers) where the name appears as a word, caselessly. A name
  that appears is not necessarily tested; generic names (`init`, `get`,
  `run`, ...) are marked `n/a`. Coverage proper would need instrumenting
  the Rexx side (ooRexx has no coverage tool) and JaCoCo on the Java side:
  a later step, if wanted.
