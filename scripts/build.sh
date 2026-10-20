#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# scripts/build.sh -- build BSF4ooRexx 850 on Linux (x86_64) from this tree.
#
# The upstream build is a set of Windows batch files (compile_all.cmd,
# bsf4oorexx.dev/cmpj.cmd, bsf4oorexx.dev/source_cc/Makefile-64-linux,
# createDistributionForBSF4ooRexx8.cmd). This script does the same steps on
# Linux, without changing any file of the imported tree: everything goes
# to build/ (ignored by git).
#
# Usage:  scripts/build.sh [all|java|native|dist|clean]     (default: all)
#
# Needs:  a JDK >= 9 (javac --release 8), g++, make-free; curl and sha1sum
#         for the dependencies from Maven Central; ooRexx 5.x installed
#         (headers and librexx/librexxapi; default prefix /usr/local, or
#         set OOREXX_HOME).
#
# Output:
#   build/classes/                     compiled classes
#   build/bsf4ooRexx-v850-dev-bin.jar  the jar (same layout as the
#                                      published bsf4ooRexx-v850-*-bin.jar)
#   build/lib/libBSF4ooRexx850.so      the JNI library
#   build/dist/                        a runnable layout: Rexx packages,
#                                      jar, library, and env.sh to source
#
# What is compiled from source, and what is not:
#   - from source: bsf4oorexx.dev/source_java/*.java (org.rexxla.bsf.*,
#     org.oorexx.handlers.jdor), org/apache/bsf (Rony's patched BSF 2.4,
#     with the Java, JavaClass, JavaScript and XSLT engines), org/oorexx
#     (datergf, misc, uno).
#   - copied as committed .class files (third-party code): org/codehaus
#     (Janino), and org/apache/bsf/engines/{jacl,jython,netrexx} and
#     org/apache/taglibs, whose dependencies are not on Maven Central in a
#     usable version.
#   - not built: the OpenOffice/LibreOffice script provider (com/sun/star/...,
#     needs ScriptFramework.jar), the .NET/CLR support, the installers.
# -----------------------------------------------------------------------------
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEV="$ROOT/bsf4oorexx.dev"
OUT="${BUILD_DIR:-$ROOT/build}"
DEPS="$OUT/deps"
CLASSES="$OUT/classes"
JAR="$OUT/bsf4ooRexx-v850-dev-bin.jar"

: "${JAVA_HOME:=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")}"
export JAVA_HOME
OOREXX_HOME="${OOREXX_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v rexx)")")")}"

JAVAC=("$JAVA_HOME/bin/javac" --release 8 -encoding ISO-8859-1 -nowarn -Xlint:none -XDsuppressNotes)

log() { printf '\n== %s\n' "$*"; }

# ---------------------------------------------------------------- deps
# Maven Central artifacts used only at compile time (none goes into the jar).
MAVEN=https://repo1.maven.org/maven2
DEPS_LIST=(
  "org/libreoffice/libreoffice/7.6.7/libreoffice-7.6.7.jar"   # UNO (org.oorexx.uno, org.oorexx.misc)
  "org/mozilla/rhino/1.7.15/rhino-1.7.15.jar"                  # BSF JavaScript engine
)

fetch_deps() {
  log "dependencies (Maven Central) -> $DEPS"
  mkdir -p "$DEPS"
  local p f
  for p in "${DEPS_LIST[@]}"; do
    f="$DEPS/$(basename "$p")"
    if [ ! -s "$f" ]; then
      curl -fsSL -o "$f.tmp" "$MAVEN/$p"
      echo "$(curl -fsSL "$MAVEN/$p.sha1" | cut -c1-40)  $f.tmp" | sha1sum -c --quiet -
      mv "$f.tmp" "$f"
    fi
    echo "   $(basename "$f")"
  done
}

cp_of() { local IFS=:; echo "$*"; }

# ---------------------------------------------------------------- java
build_java() {
  fetch_deps
  log "Java -> $CLASSES (javac --release 8, $("$JAVA_HOME/bin/javac" -version 2>&1 | grep ^javac))"
  rm -rf "$CLASSES"; mkdir -p "$CLASSES"
  local lists="$OUT/javac-lists"; rm -rf "$lists"; mkdir -p "$lists"

  local CP
  CP=$(cp_of "$CLASSES" "$ROOT/jars/Xalan/xalan.jar" "$ROOT/jars/Xalan/xercesImpl.jar" \
             "$ROOT/jars/Xalan/junit.jar" "$DEPS"/*.jar)

  # 1. Precompiled classes committed to the tree (byte-identical to the ones
  #    in the published bsf4ooRexx-v850-20260326-bin.jar), for third-party
  #    code: Janino (vendored; its sources need Apache Ant and Java 9 to
  #    compile), and the parts whose dependencies (Jacl, Jython 2.2, NetRexx,
  #    the JSP/servlet API) are not available here.
  ( cd "$ROOT"
    find org/codehaus org/apache/bsf/engines/jacl org/apache/bsf/engines/jython \
         org/apache/bsf/engines/netrexx org/apache/taglibs/bsf -name '*.class' \
      -exec cp --parents {} "$CLASSES/" \; )
  echo "   copied precompiled: org/codehaus (Janino), bsf engines/{jacl,jython,netrexx}, taglibs/bsf"

  # 2. Apache BSF (Rony's patched copy of BSF 2.4), from the repository root.
  #    The engines whose dependencies are not available are left out here,
  #    and so is util/event/rgf/ (a second EventAdapterRegistry in the same
  #    package, not in the published jar).
  ( cd "$ROOT"
    find org/apache/bsf -name '*.java' \
      | grep -v -E '^org/apache/bsf/engines/(jacl|jython|netrexx)/' \
      | grep -v -E '^org/apache/bsf/util/event/rgf/' \
      | sort > "$lists/apache-bsf.txt" )
  ( cd "$ROOT" && "${JAVAC[@]}" -cp "$CP" -d "$CLASSES" @"$lists/apache-bsf.txt" )
  echo "   org/apache/bsf: $(wc -l < "$lists/apache-bsf.txt") files"

  # 3. The BSF4ooRexx Java side. The master copies live in
  #    bsf4oorexx.dev/source_java/ (cmpj.cmd compiles them there and copies
  #    them into org/...; the copies under org/rexxla in this tree are partial
  #    and older). RexxDispatcher is package org.rexxla.bsf; JavaDrawingHandler
  #    is org.oorexx.handlers.jdor; everything else org.rexxla.bsf.engines.rexx.
  ls "$DEV"/source_java/*.java | sort > "$lists/source_java.txt"
  "${JAVAC[@]}" -cp "$CP" -d "$CLASSES" @"$lists/source_java.txt"
  echo "   bsf4oorexx.dev/source_java: $(wc -l < "$lists/source_java.txt") files"

  # 4. org/oorexx (datergf, misc, uno), against LibreOffice's UNO jar.
  #    org/oorexx/datergf/test/*.java are in the unnamed package; the
  #    published jar carries their classes in that directory, so they are
  #    compiled apart and put there.
  ( cd "$ROOT" && find org/oorexx -name '*.java' ! -path '*/datergf/test/*' | sort > "$lists/oorexx.txt" )
  ( cd "$ROOT" && "${JAVAC[@]}" -cp "$CP" -d "$CLASSES" @"$lists/oorexx.txt" )
  mkdir -p "$CLASSES/org/oorexx/datergf/test"
  "${JAVAC[@]}" -cp "$CP" -d "$CLASSES/org/oorexx/datergf/test" "$ROOT"/org/oorexx/datergf/test/*.java
  echo "   org/oorexx: $(( $(wc -l < "$lists/oorexx.txt") + $(ls "$ROOT"/org/oorexx/datergf/test/*.java | wc -l) )) files"

  # 5. Resources: as createDistributionForBSF4ooRexx8.cmd does (jar the
  #    directories, then delete *.java, *.txt, *.cmd, backups), every other
  #    file under those directories goes into the jar too.
  mkdir -p "$CLASSES/META-INF"
  cp -r "$ROOT/META-INF/services" "$CLASSES/META-INF/"
  ( cd "$ROOT" && find org/apache/bsf org/apache/taglibs org/codehaus org/rexxla org/oorexx -type f \
      ! -name '*.java' ! -name '*.class' ! -name '*.txt' ! -name '*.cmd' ! -name '*~' \
      ! -path '*bkp*' ! -path '*tmp*' ! -path '*.idea*' ! -name 'bin.iml' \
      -exec cp --parents {} "$CLASSES/" \; )
  cp "$ROOT"/Apache_LICENSE_2.0.txt "$ROOT"/janino_LICENSE.TXT \
     "$ROOT"/jni4net-MIT-LICENSE.txt "$ROOT"/xdg-utils_freedesktop_org_LICENSE.txt "$CLASSES/"
  # NOTICE.txt, which the published jar has, is not in the SVN repository.

  log "jar -> $JAR"
  rm -f "$JAR"
  ( cd "$CLASSES" && "$JAVA_HOME/bin/jar" --create --file "$JAR" --no-manifest . )
  echo "   $(unzip -l "$JAR" | tail -1 | awk '{print $2}') entries"
}

# ---------------------------------------------------------------- native
build_native() {
  log "JNI library -> $OUT/lib/libBSF4ooRexx850.so (ooRexx at $OOREXX_HOME)"
  mkdir -p "$OUT/lib" "$OUT/obj"
  local SRC="$DEV/source_cc"
  # Flags as in source_cc/Makefile-64-linux (gcc compiling a .cc with
  # -D__cplusplus; here g++, which defines __cplusplus itself).
  g++ -fPIC -c -O3 -DUNIX -m64 -DBSF4REXX_64_BIT -DUSE_OREXX \
      -I"$SRC" -I"$OOREXX_HOME/include" \
      -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" \
      -o "$OUT/obj/BSF4ooRexx850-64-amd64.o" "$SRC/BSF4ooRexx.cc" \
      2> "$OUT/obj/compile-warnings.txt" || { cat "$OUT/obj/compile-warnings.txt"; exit 1; }
  echo "   compiler warnings: $(grep -c 'warning:' "$OUT/obj/compile-warnings.txt" || true) (build/obj/compile-warnings.txt)"
  # RPATH as in the Makefile ($ORIGIN first; RPATH, not RUNPATH).
  g++ -shared -rdynamic \
      -Wl,--disable-new-dtags,-soname,libBSF4ooRexx850.so \
      -Wl,-rpath,'$ORIGIN':'$ORIGIN/../lib':/usr/local/lib64:/usr/lib64:/usr/local/lib:/usr/lib:/opt/ooRexx/lib \
      -o "$OUT/lib/libBSF4ooRexx850.so" "$OUT/obj/BSF4ooRexx850-64-amd64.o" \
      -L"$OOREXX_HOME/lib" -lrexx -lrexxapi -lpthread -lm

  # The JNI headers in source_cc/ are committed; regenerate them from the
  # Java classes and report any difference (javac -h).
  if [ -d "$CLASSES" ]; then
    local H="$OUT/jni-headers"; rm -rf "$H"; mkdir -p "$H"
    "${JAVAC[@]}" -cp "$CLASSES" -h "$H" -d "$OUT/obj/h-classes" \
       "$DEV/source_java/RexxAndJava.java" "$DEV/source_java/RexxCleanupRef.java" 2>/dev/null || true
    for h in "$H"/*.h; do
      if diff -q <(tr -d '\r' < "$SRC/$(basename "$h")") "$h" >/dev/null 2>&1; then
        echo "   $(basename "$h"): same as committed"
      else
        echo "   $(basename "$h"): DIFFERS from committed (see build/jni-headers/)"
      fi
    done
  fi
}

# ---------------------------------------------------------------- dist
build_dist() {
  log "runnable layout -> $OUT/dist"
  local D="$OUT/dist"
  rm -rf "$D"; mkdir -p "$D"
  cp "$DEV"/bin/*.CLS "$DEV"/bin/*.cls "$DEV"/bin/*.rex "$DEV"/bin/*.rxo "$DEV"/bin/*.RXO "$D/" 2>/dev/null || true
  cp "$JAR" "$D/"
  cp "$OUT/lib/libBSF4ooRexx850.so" "$D/"
  cat > "$D/env.sh" <<EOF
# source this file: . build/dist/env.sh
export JAVA_HOME="$JAVA_HOME"
export BSF4OOREXX_DIST="$D"
export PATH="$D:\$PATH"
export LD_LIBRARY_PATH="$D:$JAVA_HOME/lib/server:$JAVA_HOME/lib\${LD_LIBRARY_PATH:+:\$LD_LIBRARY_PATH}"
export CLASSPATH="$D/$(basename "$JAR")\${CLASSPATH:+:\$CLASSPATH}"
EOF
  cat > "$D/rexxj.sh" <<'EOF'
#!/bin/sh
# Java-first start: java runs org.rexxla.bsf.RexxDispatcher, which starts Rexx.
. "$(dirname "$0")/env.sh"
exec "$JAVA_HOME/bin/java" --enable-native-access=ALL-UNNAMED \
     -Djava.library.path="$LD_LIBRARY_PATH" -cp "$CLASSPATH" org.rexxla.bsf.RexxDispatcher "$@"
EOF
  chmod +x "$D/rexxj.sh"
  echo "   . $D/env.sh ; rexx yourProgram.rex   (Rexx-first)"
  echo "   $D/rexxj.sh yourProgram.rex          (Java-first)"
}

smoke() {
  log "smoke test"
  # shellcheck disable=SC1091
  . "$OUT/dist/env.sh"
  local t="$OUT/smoke.rex"
  cat > "$t" <<'EOF'
s = .bsf~new("java.lang.String", "Hello from Java")
say s~toUpperCase
say "java.version:" .bsf4rexx~system.class~getProperty("java.version")
say "BSF4ooRexx:" .bsf4rexx~version
::requires "BSF.CLS"
EOF
  echo "   Rexx-first (rexx loads libBSF4ooRexx850.so, which loads the JVM):"
  ( cd "$OUT/dist" && rexx "$t" )
  echo "   Java-first (java runs org.rexxla.bsf.RexxDispatcher):"
  "$OUT/dist/rexxj.sh" "$t" 2>&1 | grep -v '^Picked up JAVA_TOOL_OPTIONS'
}

case "${1:-all}" in
  all)    build_java; build_native; build_dist; smoke ;;
  java)   build_java ;;
  native) build_native ;;
  dist)   build_dist ;;
  smoke)  smoke ;;
  clean)  rm -rf "$OUT" ;;
  *) echo "usage: $0 [all|java|native|dist|smoke|clean]"; exit 2 ;;
esac
