



::requires bsf.cls

::routine bsf.createProxyClass public   -- create an ooRexx class that extends a Java class, rgf, 2010-11-27
  use strict arg clz, methodsToProxy=.nil

  signal on syntax

  .ArgUtil~validateClass("javaClassNameToProxy", javaClassNameToProxy, .String)

  if methodsToProxy<>.nil then
     .ArgUtil~validateClass("methodsToProxy", methodsToProxy, .Collection)

  if methodsToProxy<>.nil then
  do
     if methodsToProxy~isA(.mapCollection) then -- mapping of index->item
        arr=methodsToProxy~allIndexes     -- use indices as method names
     else
        arr=methodsToProxy~allItems       -- use items as method names

     nrItems=arr~items
     methodNames2proxy=bsf.createJavaArray("java.lang.String", nrItems) -- define a Java array of correct capacity
     do i=1 to nrItems
        methodNames2proxy[i]=arr[i]       -- create entries in Java String array
     end
  end
  else
     methodNames2proxy=.nil               -- default to proxy all methods

      -- get Utility class, determine name of dynamically created class
  recl=.bsf~new("org.rexxla.bsf.engines.rexx.RexxExtendingClassLoader")
  newClzName=(clz"_$RexxProxyClass$_"recl~identityHash"_".object~new~identityhash)~changestr(".", "_")

      -- create extended class with the ability to forward all proxied method invocations to a RexxProxy object
  newClzObj=recl~generateAndLoadExtendedClass(clz, newClzName, 0, methodNames2proxy)

      -- add "NEW" and "NEWSTRICT" method to class object!
  parse value newClzObj with newClzName "@"
  return .bsf~bsf.importClass(newClzName, .nil, newClzObj)  -- do not save in .local

syntax:
  raise propagate          -- raise in caller

