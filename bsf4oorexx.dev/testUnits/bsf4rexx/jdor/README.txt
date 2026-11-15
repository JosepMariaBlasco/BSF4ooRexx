JDOR: an ooRexxUnit test suite
==============================

Test groups for JDOR, the drawing command handler of BSF4ooRexx
(org.oorexx.handlers.jdor.JavaDrawingHandler, 100.20230718), written for
the ooTest framework (ooRexx test/trunk). Version 0.2.0, 14/10/2026.

The same groups are meant to run against the Java handler and against
the JDOR handler of the browser build of ooRexx/WASM. Therefore:
- a handler is only set up with .bsf~new(...) and
  BsfCommandHandler("add", ...);
- assertions are made on numbers, on strings returned as RC and on the
  getters of returned Java objects, never on their string forms;
- pixels are read with  'image'; rc~getRGB(x, y)  after
  'render aliasOff', with a margin of 2 px from the edges of a shape.

No test opens a window: the suite also runs headless.


Files
-----

testUnits/jdor/
  jdorTestSupport.cls          support routines (fresh handler, pixels,
                               ARGB values, error messages)
  01_basics.testGroup          images, position, comments, reset, sleep,
                               assignRC, rounding, RC/.RS conventions
  02_color_paint.testGroup     color, background, clearRect, paint,
                               gradientPaint, composite
  03_stroke_font_render.testGroup
                               stroke, font, fontSize, fontStyle, render,
                               stringBounds, drawString
  04_draw_fill.testGroup       draw*/fill* (lines, rectangles, ovals, arcs,
                               round and 3D rectangles, polygons)
  05_transform.testGroup       translate, scale, shear, rotate, transform
  06_stacks_images_clip.testGroup
                               pushGC/popGC, image stack and named images,
                               copyArea, clip, saveImage/loadImage/drawImage
  07_shapes_area.testGroup     shape (all types), drawShape/fillShape/
                               clipShape, shapeBounds, area*
  08_path2d.testGroup          path*, pathIterator
  09_redirection.testGroup     canonical output, errors, replaying macros
  10_findings.testGroup        regression tests for the findings of
                               13/10/2026, including 4 and 5 as decided
                               on 14/10
  11_findings_new.testGroup    regression tests for the findings of
                               14/10/2026, as decided (fixed by the patch
                               of 14/10)

run-java.sh                    the Linux runner we use (headless); see below


Running it
----------

With BSF4ooRexx installed, from the directory of the ooTest framework
(ooRexx test/trunk, the one that holds testOORexx.rex):

    rexx testOORexx.rex -R <path>/testUnits/jdor
    rexx testOORexx.rex -R <path>/testUnits/jdor -f 07_shapes_area

REXX_PATH must reach ooTest.frm (test/trunk and test/trunk/framework) and
BSF.CLS. jdorTestSupport.cls is found next to the groups.

On a machine without a display, add -Djava.awt.headless=true to
BSF4Rexx_JavaStartupOptions.

To test a modified handler, compile it and put its classes before the
BSF4ooRexx jar in CLASSPATH.

run-java.sh does all this on Linux. It uses environment variables for the
locations, and prints which handler it uses:

    BSF_DIST      unpacked BSF4ooRexx distribution (holds BSF.CLS, lib/)
    BSF_LIB       directory with libBSF4ooRexx850.so
    OOTEST        ooRexx test/trunk checkout
    JDOR_PATCHED  compiled classes of a modified handler (default);
                  run-java.sh -u  uses the handler in the jar instead

    BSF_DIST=... BSF_LIB=... OOTEST=... JDOR_PATCHED=... ./run-java.sh
    ./run-java.sh -u -f 08_path2d


Running it in the browser
-------------------------

In the browser build of ooRexx/WASM (github.com/JosepMariaBlasco/oorexxwasm,
patch 0009), BSF.CLS is provided by the build. Copy the groups into
ooRexx/jdor/ of an ooTest checkout (test/trunk), and run, from an
oorexxwasm checkout with the browser build made (see its INSTALL.md):

    SUITE=<that test/trunk> python3 tests/suite-browser/run.py chromium build-wasm/bin-web-test OUT

(chromium, firefox or webkit, through Playwright).


Expected results (14/10/2026)
-----------------------------

ooRexx 5.3.0, BSF4ooRexx 850 (refresh 20260326), OpenJDK, Linux, headless:

  handler                                   tests  failures
  r1125 + the patch of 14/10/2026            245       0
  r1125 + the patches of 13/10/2026          245      11   (fixed by the patch of 14/10)
  r1125 (as in the BSF4ooRexx jar)           245      22

The browser port (oorexxwasm) is being brought to the decisions of
14/10; with the previous version of this suite (242 tests) it gave the
same results as Java in Chromium, Firefox and WebKit.

