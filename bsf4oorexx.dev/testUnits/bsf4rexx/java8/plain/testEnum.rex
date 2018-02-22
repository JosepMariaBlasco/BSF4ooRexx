clz=bsf.loadClass("java8.plain.TestEnum")
say "clz         ="pp(clz)
say "clz~toString="pp(clz~toString)
say
do e over clz  -- iterate over Enum class
   say "   " "e="pp(e) "e~toString="pp(e~toString)
end
say
s=clz~supplier
do with index idx item val over clz
   say "   " "idx="pp(idx) "val="pp(val) "val~toString="pp(val~toString)
end


::requires "BSF.CLS"
