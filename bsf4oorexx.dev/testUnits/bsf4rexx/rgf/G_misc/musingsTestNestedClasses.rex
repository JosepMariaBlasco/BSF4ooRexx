#!/usr/bin/env rexx
-- 2021-02-06/07, rgf: testing nested classes
say .bsf4rexx~display.version
say

contClz  ="rgf.G_misc.TestNestedClasses", "rgf.G_misc.TestNestedClassesSub"
   -- nested classes
letters  ="A", "B", "C", "D"
modifiers="public", "protected", "private", "package_private"

tab="09"x
tab2="0909"x
do clz over contClz  -- iterate over containing classes
   say pp(clz)":"
   jcClz=bsf.loadClass(clz) -- load Java class

   do counter idx l over letters -- build nested class names
      n1Name=l'_static_'modifiers[idx]    -- nested name of nested class (level 1)
      jn1Clz=getNestedClass(jcClz,n1Name) -- try to access/load nested class
      say tab (pp(n1Name)":")~left(39,'.') pp(getNestedClassName(jn1Clz))

      if \jn1Clz~isNil then   -- try to access/load nested-nested class
      do
         do m2 over modifiers
            n2Name=l'_static_'m2'_Sub'
            jn2Clz=getNestedClass(jn1Clz,n2Name)
            say tab2 (pp(n2Name)": ")~left(31,'.') pp(getNestedClassName(jn2Clz))
         end
         say
      end
      say
   end
end

::requires "BSF.CLS"    -- get Java bridge

::routine getNestedClassName
  use arg clz
  if clz~isNil then return "n/a"
  return clz~getName

::routine getNestedClass   -- load class, return .nil if it could not be loaded
  use arg clz,name
  signal on syntax         -- intercept condition
  return clz~send(name)    -- get nested class and return it
syntax:
  return .nil
