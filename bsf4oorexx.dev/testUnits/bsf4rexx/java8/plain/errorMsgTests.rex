-- signal on syntax

dim=.bsf~new("java.awt.Dimension", 100, 300)
say "dim:" pp(dim) "dim~toString:" pp(dim~toString)
say "---"
strictDim=bsf.import("java.awt.Dimension")
dim=strictDim~new("200", "300")


say "dim:" pp(dim) "dim~toString:" pp(dim~toString)
say dim~"toString"( .nil, 1, .bsf~new("java.lang.Object"))
say dim~bsf.invokeStrict("toString", .nil, 1, .bsf~new("java.lang.Object"))


say dim~bsf.invokeStrict("toString", "object", .nil)

dim=strictDim~newStrict("int", "400", "int", "500")
say "dim:" pp(dim) "dim~toString:" pp(dim~toString)

dim=strictDim~new("int", "400", "int", "500", "int", 600)

dim=strictDim~newStrict("int", "400", "int", "500", "int", 600)

exit

dim=.bsf~new("java.awt.Dimension", 100, box("int",300))
dim=.bsf~new("java.awt.Dimension", 100, box("double",300))


dim=.bsf~new("java.awt.Dimension", 100, 300, 400)


dim~width(99)
say "dim:" pp(dim) "dim~toString:" pp(dim~toString) "(after change)"

say "width:" bsf("getFieldValueStrict", dim, "width")


call  bsf "setFieldValueStrict", dim, "width", 333
say "width:" bsf("getFieldValueStrict", dim, "width")

call  bsf "setFieldValueStrict", dim, "width", 'double', 444

say "width:" bsf("getFieldValueStrict", dim, "widtH")



say dim~bsf.invokeStrict("toString")

say dim~bsf.invokeStrict("toString", nixi, .nil)
say dim~bsf.invokeStrict("toString", "object", .nil)
say dim~bsf.invokeStrict("ToStrinG")

say dim~bsf.invokeStrict("toString".nil, 1, .bsf~new("java.lang.Object"))

say dim~toString(.nil, 1, .bsf~new("java.lang.Object"))

return

syntax:
  co=condition("object")
  say ppCondition2(co)
  say "---"
  jexc=co~additional[2]
  say "jexc:" pp(jexc) "jexc~isA(.bsf):" pp(jexc~isA(.bsf))
  say "-"~copies(100)
  if jexc~isA(.bsf) then
  do
     jexc~printStackTrace
  end
  say "--- after 'jexc~printStackTrace'"


::requires BSF.CLS
