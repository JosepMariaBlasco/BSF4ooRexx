o=.bsf~new("Test")

m=o~getClass~getMethods
tab="09"x
tab2="0909"x

say m~items "methods:"

do i=1 to m~items
   mo=m[i]
   say i":" tab mo~toString
   p=mo~getParameterTypes
   if p~items>0 then
      say tab p~items "parameter(s):"
   do k=1 to p~items
      say tab2 k")" p[k]~toString
      say tab2 k")" p[k]~bsf.invokeStrict("toString") "// bsf.invokeStrict(p[k]) \\"
      -- aha=p[k]
      -- say aha~toString
      -- say tab2 p[k]~toString
   end
end
say "---"
m=o~getClass~getDeclaredMethod("aha", .nil)
say "m:         " m
say "m~toString:" m~toString
say "now attempting to invoke method:"
m~invoke(o, .nil)

say "---"
clz=bsf.createArray("java.lang.Class", 1);
clz[1]=.bsf4rexx~integer.class
m=o~getClass~getDeclaredMethod("aha", clz)
say "m:         " m
say "m~toString:" m~toString

args=bsf.createArray("java.lang.Object", 1)
args[1]=box("int",1)
say "now attempting to invoke method:"
m~invoke(o, args)


::requires bsf.cls
