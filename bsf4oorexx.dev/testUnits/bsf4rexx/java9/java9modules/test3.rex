#!/usr/bin/env rexx
signal on syntax

tab="09"x               -- define tab-character
   -- define array of class names to use
classes="mtest3.Class03A", "mtest3.Class03B",  "mtest1.Class01B", "mtest2.Class02A", "mtest2.Class02B"
loop cn over classes    -- iterate over each class name
   say "about to load class" pp(cn)

   clz=bsf.importClass(cn) -- load Java class
   say tab "loaded, clz~toString:" pp(clz~toString)
   pkg=clz~getPackage      -- get the package
   mod=clz~getModule       -- get the module
      -- show information
   say tab pp(clz) "package:" pp(clz~getPackage~toString) "module:" pp(clz~getmodule~toString)
   o=clz~new               -- create an instance of the class
      -- show the Java object (its object name) and the value of the field "myClassName"
   say tab "o="pp(o) "o~getMyClassName:" pp(o~getMyClassName ) "(via method)"
   say tab "o="pp(o) "o~myClassName:" pp(o~myClassName ) "(via getField)"
   say "-"~copies(99)      -- draw 99 dashes
end
exit 0                     -- all o.k.

syntax:     -- syntax condition handler
   co=condition('o')    -- get condition object
   say ppCondition2(co) -- show condition object in a "pretty print" form
   say "--"
   jexc=co~additional[2]
   say "jexc="pp(jexc) "jexc~class="pp(jexc~class)
   if jexc~isA(.bsf) then jexc~printStackTrace
   raise propagate



::requires "BSF.CLS"       -- get and instantiate the ooRexx-Java bridge
::requires "rgf_util2.rex" -- get access to the public routine ppCondition2()