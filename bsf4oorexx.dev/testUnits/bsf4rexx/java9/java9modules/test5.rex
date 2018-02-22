signal on syntax
-- o=.bsf~new("mtest3.Class03B")             -- create Java object
o=.bsf~new("mtest3.Class03A")             -- create Java object
say "o:" o "o~getMyClassName:" o~getMyClassName -- get (static) field value
say "o:" o "o~myClassName   :" o~myClassName -- get (static) field value
exit

syntax:
  co=condition('o')
  say "rgf:" ppCondition2(co)
   say "--"
   jexc=co~additional[2]
   say "jexc="pp(jexc) "jexc~class="pp(jexc~class)
   if jexc~isA(.bsf) then jexc~printStackTrace
   raise propagate

::requires BSF.CLS   -- direct interpreter to load Java bridge

::requires "rgf_util2.rex"

