#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# scripts/run-tests.sh -- run the ooRexxUnit test groups of
# bsf4oorexx.dev/testUnits against the build of scripts/build.sh.
#
# Usage:  scripts/run-tests.sh [--display xvfb|none] [--framework svn|bundled]
#                               [GROUP-PATTERN]
#
#   --display xvfb  run every group under xvfb-run (a virtual X display);
#                   this is the default when xvfb-run is installed
#   --display none  run with no display at all (DISPLAY unset; AWT headless
#                   not forced), to see which groups need one
#   --framework svn      the ooRexx test framework (testOORexx.rex, ooTest.frm,
#                   worker.rex, framework/*.cls) from ooRexx's SVN at revision
#                   $OOTEST_REV (default 13299, the family's pin for test/trunk);
#                   the default. The tests use assertions (assertSameList, ...)
#                   that the bundled copy lacks.
#   --framework bundled  testUnits/ooRexxUnit/ (an old copy, ooTest 4.0.0)
#   --legacy-lib-name    also offer the library as libBSF4ooRexx.so (a symlink
#                   in build/tests/): two groups still load it by its pre-850
#                   name (rxFuncAdd "BsfLoadFuncs", "BSF4ooRexx", ...)
#   GROUP-PATTERN   an extended regex; only the groups whose path matches run
#
# Environment: TEST_TIMEOUT (seconds per group, default 180), OOTEST_REV;
#   TEST_JAVA_HOME: run the tests on another JVM than the one of the build
#   (for instance a Java 8 JRE); its libjvm.so directory is put first on
#   LD_LIBRARY_PATH, instead of the build JDK's (on Java 8 that is
#   jre/lib/amd64/server, which the library's own JAVA_HOME lookup does not
#   try: it tries lib/server and jre/lib/server).
#
# The test tree is copied to build/tests/<display>-<framework>/ (the imported tree is not
# touched); the Java helper classes of the tests are recompiled there with
# javac --release 8. Each group runs on its own (testOORexx -f), with a
# time limit, so that one hanging or crashing group does not hide the rest.
#
# Output:
#   build/tests/<display>-<framework>/logs/<group>.log   the full output of each group
#   build/tests/<display>-<framework>/results.tsv        one line per group:
#       group  status  tests  assertions  failures  errors  seconds
#   Exit status: 0 if every group is ok, 1 otherwise.
#     status: ok | failures | errors | crash (no summary; exit code noted)
#             | timeout (no summary) | hang-at-exit (summary printed, then the
#             process did not end before the time limit; the counts are valid)
# -----------------------------------------------------------------------------
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${BUILD_DIR:-$ROOT/build}"
MODE=xvfb; command -v xvfb-run >/dev/null || MODE=none
PATTERN='.'
TIMEOUT="${TEST_TIMEOUT:-180}"
FRAMEWORK=svn
LEGACY=0
OOTEST_REV="${OOTEST_REV:-13299}"

while [ $# -gt 0 ]; do
  case "$1" in
    --display) MODE="$2"; shift 2 ;;
    --framework) FRAMEWORK="$2"; shift 2 ;;
    --legacy-lib-name) LEGACY=1; shift ;;
    -h|--help) sed -n 2,36p "$0"; exit 0 ;;
    *) PATTERN="$1"; shift ;;
  esac
done

[ -f "$OUT/dist/env.sh" ] || { echo "run scripts/build.sh first"; exit 1; }
# shellcheck disable=SC1091
. "$OUT/dist/env.sh"
unset JAVA_TOOL_OPTIONS   # the tests need no network; keeps the logs clean
JAVAC="$JAVA_HOME/bin/javac"   # the build's JDK compiles the test helpers (--release 8)
if [ -n "${TEST_JAVA_HOME:-}" ]; then
  export JAVA_HOME="$TEST_JAVA_HOME"
  # not the build JDK's directories: a mixed path makes the JVM fail to start
  export LD_LIBRARY_PATH="$OUT/dist:$OOREXX_HOME/lib:$JAVA_HOME/lib/server:$JAVA_HOME/jre/lib/amd64/server"
fi
JVERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -1)

T="$OUT/tests/$MODE-$FRAMEWORK$([ "$LEGACY" = 1 ] && echo -legacylib)$([ -n "${TEST_JAVA_HOME:-}" ] && echo "-$(basename "$TEST_JAVA_HOME")")"
rm -rf "$T"; mkdir -p "$T/logs"
if [ "$LEGACY" = 1 ]; then
  mkdir -p "$T/lib" && ln -s "$OUT/dist/libBSF4ooRexx850.so" "$T/lib/libBSF4ooRexx.so"
  export LD_LIBRARY_PATH="$T/lib:$LD_LIBRARY_PATH"
fi
cp -r "$ROOT/bsf4oorexx.dev/testUnits" "$T/tu"
TU="$T/tu"

# Java helpers of the tests: as their compile*.cmd do, javac *.java in each
# directory, classes next to the sources (package rgf.*, java8.plain, ...,
# rooted at bsf4rexx/; UseTest2 is in the unnamed package). java9/ has no
# test groups and needs module tooling: not compiled.
( cd "$TU/bsf4rexx"
  : > "$T/javac.log"
  for d in $(find . -name '*.java' ! -path './java9/*' -exec dirname {} \; | sort -u); do
    ( cd "$d" && "$JAVAC" --release 8 -nowarn -encoding ISO-8859-1 \
          -cp "$TU/bsf4rexx:.:$CLASSPATH" ./*.java ) >> "$T/javac.log" 2>&1 \
      || { echo "compiling the Java helpers in $d failed:"; cat "$T/javac.log"; exit 1; }
  done ) || exit 1

# "." because each group runs in its own directory (UseTest2, unnamed package)
export CLASSPATH="$CLASSPATH:$TU/bsf4rexx:."

if [ "$FRAMEWORK" = svn ]; then
  FW="$OUT/deps/ootest-r$OOTEST_REV"
  if [ ! -f "$FW/framework/OOREXXUNIT.CLS" ]; then
    mkdir -p "$FW/framework"
    base="https://svn.code.sf.net/p/oorexx/code-0/!svn/bc/$OOTEST_REV/test/trunk"
    for f in testOORexx.rex worker.rex ooTest.frm framework/OOREXXUNIT.CLS \
             framework/FileUtils.cls framework/WinUtils.cls; do
      curl -fsSL -o "$FW/$f" "$base/$f" || { echo "cannot fetch $base/$f"; exit 1; }
    done
  fi
  export PATH="$PATH:$FW:$FW/framework"
else
  FW="$TU/ooRexxUnit"
  export PATH="$PATH:$FW"
fi
echo "test framework: $FRAMEWORK ($FW); display: $MODE; legacy lib name: $LEGACY; time limit: ${TIMEOUT}s per group"
echo "JVM: $JVERSION ($JAVA_HOME)"

run_group() {   # $1 = group path relative to $TU
  if [ "$MODE" = xvfb ]; then
    timeout -k 10 "$TIMEOUT" xvfb-run -a rexx "$FW/testOORexx.rex" -f "$TU/$1"
  else
    env -u DISPLAY timeout -k 10 "$TIMEOUT" rexx "$FW/testOORexx.rex" -f "$TU/$1"
  fi
}

num() { grep -m1 "^$1" "$2" | awk -F: '{gsub(/ /,"",$2); print $2}'; }

printf 'group\tstatus\ttests\tassertions\tfailures\terrors\tseconds\n' > "$T/results.tsv"
cd "$TU"
find bsf4rexx -iname '*.testGroup' | sort | grep -E "$PATTERN" | while read -r g; do
  log="$T/logs/$(echo "$g" | sed 's|^bsf4rexx/||; s|/|__|g').log"
  start=$(date +%s)
  ( cd "$(dirname "$g")" && run_group "$g" ) > "$log" 2>&1 < /dev/null
  rc=$?
  secs=$(( $(date +%s) - start ))
  tests=$(num 'Tests ran' "$log"); asserts=$(num 'Assertions' "$log")
  fails=$(num 'Failures' "$log");  errs=$(num 'Errors' "$log")
  if { [ "$rc" = 124 ] || [ "$rc" = 137 ]; } && [ -n "$tests" ]; then status=hang-at-exit
  elif [ "$rc" = 124 ] || [ "$rc" = 137 ]; then status=timeout
  elif [ -z "$tests" ]; then status="crash(rc=$rc)"
  elif [ "${errs:-0}" != 0 ]; then status=errors
  elif [ "${fails:-0}" != 0 ]; then status=failures
  else status=ok; fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "${g#bsf4rexx/}" "$status" "${tests:--}" \
         "${asserts:--}" "${fails:--}" "${errs:--}" "$secs" >> "$T/results.tsv"
  printf '%-60s %-10s tests=%s failures=%s errors=%s (%ss)\n' "${g#bsf4rexx/}" "$status" \
         "${tests:--}" "${fails:--}" "${errs:--}" "$secs"
done

echo
echo "display=$MODE framework=$FRAMEWORK  results: $T/results.tsv"
awk -F'\t' 'NR>1{n[$2 ~ /^crash/ ? "crash" : $2]++; t+=$3; f+=$5; e+=$6}
  END{printf "groups:"; for (k in n) printf " %s=%d", k, n[k]; printf "\ntests=%d failures=%d errors=%d\n", t, f, e}' "$T/results.tsv"

# exit status: 0 only if every group that ran is ok
awk -F'\t' 'NR>1 && $2!="ok"{bad=1} END{exit bad}' "$T/results.tsv"
