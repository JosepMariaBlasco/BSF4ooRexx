parse source s
say "(Rexx) parse source:" s
say
say "(Rexx) received" arg() "arguments:"
do i=1 to arg()
   say "(Rexx)   arg("i")="pp(arg(i)) pp(arg(i)~class)
   if i=arg() then      -- last argument is a directory containing "SCRIPTCONTEXT"
   do
      say "(Rexx) the last argument is a Rexx directory containing the ScriptContext of this invocation:"
      sc=arg(i)~scriptContext      -- fetch script context
      jclz=bsf.importClass("javax.script.ScriptEngine")
      fn=jclz~FILENAME
      say "(Rexx)           FILENAME ->" pp(sc~getAttributesScope(fn))  pp(fn)    pp(sc~getAttribute(fn))
      argv=jclz~ARGV
      say "(Rexx)           ARGV     ->" pp(sc~getAttributesScope(argv)) pp(argv) pp(sc~getAttribute(argv))
   end
end

::requires bsf.cls   -- get Java support
