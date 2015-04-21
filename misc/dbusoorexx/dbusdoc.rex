#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-07-29/31, 2011-08-01/05, 2011-08-14
      changed:    2014-08-07, rgf - make sure that connection gets closed, otherwise message loop thread lingers
                  2014-11-02, rgf - made debug output parametrized, added option to open generated html fiel
                  2014-11-04, rgf - removed dependency on "rgf_util2.rex"
                  2015-04-21, rgf - adjusting to version 2.00, renamed dbusrexx.css to dbusdoc.css, which is more intuitive
                                  - added routine makeSureCssFileExists() which copies css file into
                                    current directory, if not there already

      needs:      ooRexx ("open object Rexx") 4.2.0 or higher, cf. <http://www.ooRexx.org>
                  ooRexx language bindings for ooRexx, cf. <https://sourceforge.net/projects/bsf4oorexx/files/GA/sandbox/dbusoorexx/>
                  ---
                  DBus for MacOSX: e.g. install via <https://www.macports.org/>
                  DBus for Windows: e.g. <http://wi.wu.ac.at/rgf/rexx/orx22/work/>
                  ---
                  DBus home/source: <http://www.freedesktop.org/wiki/Software/dbus/#index5h1>

      version:    103.20150421
      name:       dbusdoc.rex
      purpose:    documents given bus name in HTML form to allow the creation of API listings,
                  if no argument is given the available session bus names get listed on stdout
      usage:      rexx dbusdoc.rex [ [session|system] [service.bus.name] [-a|r] [/open] ]
      license:    AL 2.0
            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011-2015 Rony G. Flatscher

               Licensed under the Apache License, Version 2.0 (the "License");
               you may not use this file except in compliance with the License.
               You may obtain a copy of the License at

                   http://www.apache.org/licenses/LICENSE-2.0

               Unless required by applicable law or agreed to in writing, software
               distributed under the License is distributed on an "AS IS" BASIS,
               WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
               See the License for the specific language governing permissions and
               limitations under the License.
            -----------------------------------------------------------------------------
*/


.local~dbus.doc.debug   = .false
.local~dbus.doc.css     = "dbusdoc.css"   -- file name of css-file

parse arg val           -- get argument string
switch=""               -- make sure empty string
open  =""               -- make sure empty string
if pos("-",val)>0 then     -- extract "-" switch ?
do
   parse var val before "-" switch after
   val=before after
end

if pos("/",val)>0 then     -- extract "/" switch?
do
   parse var val before "/" open after
   val=before after
end

-- parse arg connection needle . 1 "-" switch . 1 "/" open .
parse var val connection needle .

bAllBuses=.false         -- by default do not inspect all buses, but those that have explicit service (bus) names
bAllReservedBuses=.false -- by default do not process reserved buses only
if switch<>"" then
do
    sw=switch~left(1)~translate
    bAllBuses=(sw='A')    -- allow for all buses
    bAllReservedBuses=(sw='R')
end

bOpen=.false             -- by default do not open generated html-file with default app (browser)
if open<>"" then
   bOpen=(open~left(1)~upper = 'O')

if .dbus.doc.debug=.true then
do
   say "settings in effect:"
   say "   connection:                " pp(connection)
   say "   needle:                    " pp(needle)
   say "   process all buses:         " pp(bAllBuses)
   say "   process all reserved buses:" pp(bAllReservedBuses)
   say "   open generated html-file:  " pp(bOpen)
   say
end

if needle="" then
do
    if connection<>"",pos(connection, "session system")=0 then
    do
       needle    =connection
       connection="session"      -- default to session
    end
    else    -- list all session busnames in sorted order and show usage at the end
    do
       if connection<>"" then
          conn=.dbus~connect(connection)
       else
       do
          connection="session"
          conn=.dbus~session     -- default to "session" connection
       end

       o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
       allBusNames=.set~new ~union(o~listNames) ~union(o~listActivatableNames) -- get all bus names on this connection

       say pp(connection)"-connection, the following bus names are currently available:"

       do busName over allBusNames~allIndexes~sort
          if bAllReservedBuses=.true then       -- skip all non reserved bus names
          do
              if busName~left(1)<>':' then
              do
                 allBusNames~remove(busName)
                 iterate
              end
          end
          else
          if bAllBuses=.false, busName~left(1)=':' then            -- skip all reserved bus names (those that start with ':')
          do
              allBusNames~remove(busName)
              iterate
          end

          say "   " busName
       end
       say pp(connection)"-connection:" pp(allBusNames~items) "bus name(s) currently available."
       say

       conn~close
       signal usage     -- show usage
    end
end


signal on syntax
conn=.dbus~new(connection)
o=conn~getObject(.dbus.dir~ServiceDBus, .dbus.dir~PathDBus) -- get the DBus object
allBusNames=.set~new ~union(o~listNames) ~union(o~listActivatableNames) -- get all bus names on this connection

filtered=.array~new

do busName over allBusNames~allIndexes~sort
   if bAllReservedBuses=.true then       -- skip all non reserved bus names
   do
       if busName~left(1)<>':' then
       do
          allBusNames~remove(busName)
          iterate
       end
   end
   else
   if bAllBuses=.false, busName~left(1)=':' then            -- skip all reserved bus names (those that start with ':')
   do
       allBusNames~remove(busName)
       iterate
   end

   if busName~caselesspos(needle)>0 then
      filtered~append(busName)   -- we need to process this bus name
end

if filtered~items=0 then
do
   say 'no bus name found containing "'||needle'"'
   conn~close
   exit
end

items=filtered~items
w=items~length
serviceDir=.directory~new    -- contains arrays, where [1]=serviceName and [2]=analyzeObject

do i=1 to filtered~size
   serviceName=filtered[i]
   say i~right(w)"/"items":" serviceName
   o=.analyzedService~new(conn,serviceName)  -- try to get objects
   serviceDir[serviceName]=o                -- save analyze object with service name
end

m=.MutableBuffer~new

call htmlProlog m, needle
if serviceDir~items>1 then             -- if more than one service, create an overview with links to the definitions
   call htmlJumpList m, serviceDir, conn

call htmlDetails m, serviceDir, conn
call htmlEpilog m



newFileName=conn~busType"_"needle"_".DateTime~new~string~translate("_-",":.")".html"
say "creating HTML output, writing to" pp(newFileName) "..."
call stream newFileName, "C", "open replace"
call charout newFileName, m~string
call stream newFileName, "C", "close"

call makeSureCssFileExists -- make sure css exists in current directory

call dumpStatistics conn
conn~close

if bOpen=.true then                 -- if "/o[pen]" given then open generated html-file with default pp
do
      -- open generated HTML file
   parse upper source s +1
   say "attempt to open" pp(newFileName) "in browser..."
   select
      when s="W" then newFileName            -- Windows
      when s="M" then "open" newFileName     -- MacOSX
      otherwise
         "xdg-open" newFileName              -- freedesktop.org-utility (e.g. Linux)
   end
end

exit

usage:

   parse source . . fullName
   thisName=filespec("name",fullName)
   say "usage:" thisName "[ [session|system] [service.bus.name] ]"
   say
   say "    ... session|system - optional, defaults to 'session' message bus "
   say "    ... service.bus.name - optional, if omitted lists all known service.bus.names on the message bus"
   say
   say "... queries all objects on the given ""service.bus.name"" and creates a HTML rendering of the introspect data"
   say "    (supplying a fragment of a bus name will document all bus names containing that fragment)"
   exit 0

syntax:
  co=condition('o')
  say ppCondition(co)        -- "pretty prints" the condition object's information
  say
  conn~close
  raise propagate


::requires "dbus.cls"         -- get support for DBus


::class analyzedService

::method init
  expose  conn busname objectPathList objectPathDefs objectPathIntrospectData stdIntroData2objectPath
  use arg conn, busname

  objectPathList=.array~new
  objectPathDefs=.directory~new
  objectPathIntrospectData=.directory~new
  stdIntroData2objectPath=.relation~new
  self~getObjectPathList

::attribute busname
::attribute objectPathDefs
::attribute objectPathList
::attribute objectPathIntrospectData
::attribute stdIntroData2objectPath


::method getObjectPathList
  expose busname objectPathList
  self~getAllObjectPaths("/")

  if objectPathList~items=0, pos(":",busname)=0 then  -- no object paths found and not a system bus name
  do
      testObjPath="/" || busname~changeStr('.','/')   -- create object path from busname
      say self":getObjectPathList: no object path(s) found, testing busname-derived object path" pp(testObjPath)
      self~getAllObjectPaths(testObjPath)
  end

  return


::method getAllObjectPaths private
   expose conn busname objectPathList objectPathDefs objectPathIntrospectData stdIntroData2objectPath
   use arg objPath

   signal on syntax
   o=conn~getObject(busName, objPath)  -- try to get the DBus object
   introData=o~proxy.introspectData          -- get introspect data
   rootNode=o~proxy.introspectRootNode
   content=rootNode~content~sort       -- process root's subordinate nodes
   do n over content
      if n~isA(.IDBusNode) then     -- process next received fragment for building object path
      do
            -- an Introspection node for the object path in hand
         name=n~name
         if name="" | left(name,1)="/" then
         do
            if n~contains("interface") then     -- make sure the subtree has at least one 'interface' node
            do
               objectPathList~append(objPath)      -- add object path to array
               objectPathDefs[objPath]=rootNode    -- save object's introspect data
               objectPathIntrospectData[objPath]=introData -- save introspection data for later use
               str=stdIntroData(n)                 -- create a canonized version of this node's introspect data

               stdIntroData2objectPath[str]=objPath   -- save canonized version with its object path
            end
            iterate
         end

            -- o.k. add fragment to object path, retry
         if objPath="/" then self~getAllObjectPaths(       "/"name) -- ,busName,objectPathList)
                        else self~getAllObjectPaths(objPath"/"name) -- ,busName,objectPathList)
      end
   end

   return

syntax:  -- mostlikely: org.freedesktop.DBus.Error.Reply or org.freedesktop.DBus.Error.ServiceUnknown
   co=condition('o')
   say "---> error:" co~additional~at(2)  -- contains the substitution error message
   say "     (hint: you could retry immediately, maybe the service was not listening yet)"
   return


stdIntroData: procedure    -- create and return a canonized version of the Introspect interface data
   use arg node

   root=.IntrospectHelper~new    -- create a node for the received node
   do iFace over node~content     -- iterate over the node's content
      if iFace~isA(.IDBusInterface) then
      do
         currentIFace=root~addInterface(iFace~name) -- add the interface by its name
         do o over iFace~content    -- iterate over the interface's content
            select
            when o~isA(.IDBusCallMethod) then
                 currentIFace~addMethod(o~name, o~argSignature, o~replySignature)
            when o~isA(.IDBusPropertyMethod) then
                 currentIFace~addProperty(o~name, o~type, o~access)
            when o~isA(.IDBusSignalMethod) then
                 currentIFace~addSignal(o~name, o~argSignature)
            otherwise nop
            end
         end
      end
   end
   return root~makeString




/* ------------------------------- class definition ------------------------------- */
::class hrefmgr         -- HREF-manager:
::method init class
  expose obj2id name2counter objClz2name
  obj2id      =.table~new
  objClz2name =.table~new
  name2counter=.directory~new

::method getid class                -- returns a unique name for the supplied object
  expose obj2id objClz2name name2counter
  use strict arg o, clzName=(o~class~id)

  if obj2id~hasIndex(o) then        -- already known, return unique name
     return obj2id[o]

  clz=o~class

  if objClz2Name~hasitem(clzName)=.false then  -- new class
  do
     objClz2Name[clz]=clzName
     name2counter[clzName]=0                 -- save counter
  end

  counter=name2counter[clzName]+1   -- fetch and create new counter value
  name2counter[clzName]=counter     -- save new counter value
  newName=clzName"_"counter         -- determine unique name
  obj2id[o]=newName                 -- save unique name
  return newName

/* ------------------------------- routine definitions ---------------------------- */

::routine esc                       -- escape text to SGML entities for <,>,&
  parse arg text
  return text~changeStr("&","&amp;") ~changeStr("<","&lt;") ~changeStr(">","&gt;") ~changeStr('"',"&quot;")



::routine htmlProlog
  use arg m, needle

  crlf="0d0a"x

  m~~append('<html>                                                                  ') ~~append(crlf)
  m~~append('<head>                                                                  ') ~~append(crlf)
  m~~append('   <title>D-Bus Interface On-the-Fly Documentation for' esc('"'needle'"') '</title>') ~~append(crlf)
  m~~append('   <link href="'.dbus.doc.css'" rel="stylesheet" type="text/css"/>'      ) ~~append(crlf)
  m~~append('</head>                                                                 ') ~~append(crlf)
  m~~append('<body>                                                                  ') ~~append(crlf)


::routine htmlEpilog
  use arg m

  crlf="0d0a"x
  m~~append('<p class="epilog">')
  parse version v
  text="Program 'dbusdoc.rex', run by user '"value('USERNAME',,"ENVIRONMENT")"' at '".DateTime~new"', using <a href=""http://www.ooRexx.org"">'"v"'</a> on '"SysVersion()"'"
  m~~append(text)~~append(crlf)
  text=", using Rexx package <a href=""http://sourceforge.net/projects/bsf4oorexx/"">'dbus.cls', version '".dbus.dir~version"' which employs '"DbusVersion()"'</a>."

  m~~append(text)
  m~~append('</p>')     ~~append(crlf)
  m~~append('</body>')  ~~append(crlf)
  m~~append('</html>')  ~~append(crlf)


::routine htmlJumpList       -- create an overview with links to the interface descriptions
  use arg m, serviceDir, conn      -- serviceDir: idx 1: serviceName, idx 2: analyzedServiceObject

  crLf="0d0a"x
  tab1="    "
  tab2=tab1~copies(2)
  tab3=tab1~copies(3)

  -- m~~append('<h1>Overview of Analyzed Service/Bus Name(s)</h1>') ~~append(crlf)
  m~~append('<h1>Overview of Analyzed Service/Bus Name(s) on the ')~~append(pp(conn~busType))~~append('-Bus </h1>') ~~append(crlf)

  m~~append('<ol class="jumplist">')~~append(crlf)

  comparator=.caselessComparator~new     -- to sort caselessly
  do serviceName over serviceDir~allIndexes~sortWith(comparator) -- iterate over array
     o=serviceDir[serviceName]           -- get analyzed service object
     m~~append(tab1)~~append("<li>")~~append(crlf)

     m~~append(tab2)             -- indent a little bit further
     if o~objectPathDefs~items=0 then     -- do not create link, as nothing to show
        m~~append(serviceName) ~~append(" (no interface definitions found)")
     else
     do
        m~~append('<a href="#') ~~append(.hrefmgr~getId(serviceName,'serviceName')) ~~append('">') ~~append(serviceName) ~~append("</a>") ~~append(crlf)
        m~~append(tab2) ~~append('<ul class="jumplist">') ~~append(crlf)

        do p over o~objectPathDefs~allIndexes~sortWith(comparator)
           m~~append(tab3) ~~append('<li>') ~~append(p) ~~append('</li>')  ~~append(crlf)
        end
        m~~append(tab2) ~~append('</ul>')                 ~~append(crlf)
     end

     m~~append(tab1)~~append("</li>") ~~append(crlf)
  end

  m~~append("</ol>")~~append(crlf)
  m~~append('<hr/>')~~append(crlf) ~~append(crlf)


::routine htmlDetails
  use strict arg m, serviceDir, conn

  crLf="0d0a"x
  tab1="    "
  tab2=tab1~copies(2)
  tab3=tab1~copies(3)
  tab4=tab1~copies(4)
  tab5=tab1~copies(5)
  tab6=tab1~copies(6)
  tab7=tab1~copies(7)

  comparator=.caselessComparator~new   -- to sort caselessly
  complexSignatures=.set~new           -- collect complex types from signatures

  exposeVars='crlf tab1 tab2 tab3 tab4 tab5 tab6 tab7 comparator complexSignatures exposeVars'   -- list of variables to be exposed to procedures

  -- m~~append('<h1>Details of Analyzed Service/Bus Name(s)</h1>') ~~append(crlf)
  m~~append('<h1>Details of Analyzed Service/Bus Name(s) on the ')~~append(pp(conn~busType))~~append('-Bus </h1>') ~~append(crlf)


  m~~append('<ol class="servicename">')~~append(crlf)
  do serviceName over serviceDir~allIndexes~sortWith(comparator) -- iterate over array
     o=serviceDir[serviceName]         -- get analyzed service object for current serviceName

      -- create the service name header
     m~~append(tab1)
     m~~append('<li class="servicename">') ~~append("Bus Type: ") ~~append('<em class="bustype">')~~append(pp(conn~busType)) ~~append("</em>")
     m~~append(", Service (Bus) Name: ")
     if o~objectPathDefs~items = 0 then
     do
        m~~append('<em class="servicename">')~~append(pp(serviceName)) ~~append("</em>")
        m~~append("<br/> (no interface definitions found)")
     end
     else
     do
        -- define anchor name
        m~~append('<a name="') ~~append(.hrefmgr~getId(serviceName,'serviceName')) ~~append('">')
        m~~append('<em class="servicename">')~~append(pp(serviceName))
        m~~append('</a>') ~~append("</em><br/>")
        m~~append(crlf)~~append(crlf)

         -- create the object path headers
say tab1 "servicename:" pp(servicename)
        call createObjectPathDocs m, o
say
     end
     m~~append(tab1)~~append('<br/>')~~append(crlf)
     m~~append(tab1)~~append('</li>') ~~append('<br/>') ~~append(crlf) ~~append(crlf)
  end
  m~~append('</ol>')~~append(crlf) ~~append(crlf)

  if complexSignatures~items>0 then             -- decipher found complex signatures
     call htmlDumpComplextTypes m, complexSignatures

  call htmlShowDatatypes m    -- list DBus Type characters

  return complexSignatures


createObjectPathDocs: procedure expose (exposeVars)  -- create a list of object paths and its interface documentation
   use arg m, o

   allPaths=.set~new~union(o~objectPathIntrospectData~allIndexes)

   rel=o~stdIntroData2objectPath -- ~copy

   do while allPaths~items>0         -- as long as object paths to process keep on going
      oPath=allPaths~allIndexes~sortWith(comparator)[1]

      m~~append(tab2)~~append('<br/>Object Path: <br/>')~~append(crlf)
      m~~append(tab2)~~append('<ul class="objectpath">')~~append(crlf)

      introData=rel~index(oPath) -- get introspection Data for object path

      -- list all object paths that share the identical (canonized) introspect data (they returned the same introspect data upon "Introspect()")
      do tmpPath over rel~allAt(introData)~sortWith(comparator)
         m~~append(tab3)~~append('<li>') ~~append(pp(tmpPath)) ~~append('</li>') ~~append(crlf)
         allPaths~remove(tmpPath)      -- remove object path from set
      end
      m~~append(tab2)~~append('</ul><br/>') ~~append(crlf)

      -- now process the interface definitions
      tmpRootNode=o~objectPathDefs[oPath]

      call createInterfaceDocs m, tmpRootNode
   end
   return


createInterfaceDocs: procedure expose (exposeVars)
  use arg m, rootNode

  dt=.dbus.dir~dataTypes         -- get datatype dir, "r" -> struct, "e" -> dict(entry)

say tab2 "   node:" pp(rootNode~name)
  do n over rootNode~content~sort     -- sortWith(comparator)
     if n~isA(.IDBusNode) then
     do
if .dbus.doc.debug=.true then say tab4 "**debug node:" pp(n~name) -- "content~items:" pp(n~content~items)

        if n~contains("interfaces")=.false then -- a fragment node attached to introspect data?
           iterate

        m~~append(tab2) ~~append("Node name: ")~~append('<em class="node">')~~append(pp(n~name)) ~~append("</em><br/>") ~~append(crlf)
        m~~append(tab2) ~~append('<ul class="node">') ~~append(crlf)
     end

     do i over n~content~sort   -- interfaces etc.
        if i~isA(.IDBusInterface) then
        do
           m~~append(tab3) ~~append('<br/><li>') ~~append('Interface: ') ~~append('<em class="interface">')
           m~~append(pp(i~name))~~append('</em>')  ~~append(crlf)

           m~~append(tab4) ~~append('<table class="interface">')~~append(crlf)
           bEven=.false

if .dbus.doc.debug=.true then say tab4 "*** debug interface:" pp(i~name)

           oldType=""
           nr=0
           do meth over i~content~sort                -- iterate over interface content

              if      meth~isA(.IDBusCallMethod)     then kind="method"
              else if meth~isA(.IDBusSignalMethod)   then kind="signal"
              else if meth~isA(.IDBusPropertyMethod) then kind="property"
              else iterate    -- ignore any other kinds

              if oldType<>kind then
              do
                 if oldType<>"" then
                    bBorderAbove=.true

                 oldType=kind
                 nr=0            -- reset counter
              end
              nr+=1              -- increase counter

              m~~append(tab5)
              if bEven then
                 m~~append('<tr class="even ')
              else
                 m~~append('<tr class="odd ')

              m~~append(kind)
              bEven=\bEven

              if bBorderAbove=.true then      -- new kind, after an existing one, make it visible
              do
                  m~~append(' borderabove')
                  bBorderAbove=.false
              end

              m~~append('">')~~append(crlf)


               -- number of this group's entry
              m~~append(tab6)~~append('<td class="nr"><em class="nr">') ~~append(nr) ~~append('</em></td>')~~append(crlf)   -- TD # 1 - nr

              if kind="property" then  -- a property: if a "readwrite" property show both signatures
              do
                 m~~append(tab6)~~append('<td class="result argtype">&nbsp;</td>') ~~append(crlf)   -- TD # 2 - result
                 m~~append(tab6)~~append('<td class="kind"><em class="kind">') ~~append(kind)    ~~append('</em></td>')~~append(crlf)   -- TD # 3 - kind

                 m~~append(tab6)~~append('<td class="propertyname">') ~~append('<span class="property name">') ~~append(meth~name)~~append('</span>')
                 m~~append('</td>')~~append(crlf)   -- TD # 4 - name

                 m~~append(tab6)~~append('<td><span class="access">') ~~append(meth~access) ~~append("</span></td>")~~append(crlf)
                 m~~append(tab6)~~append('<td><span class="signature property">')
                 t=meth~type     -- get type (signature) of property
                 if dt~hasIndex(t) then m~~append(dt[t]) -- translated type
                                   else
                                   do
                                      m~~append(t)       -- DBus type
                                      if pos(t~left(1),"a(")=0 then   -- not yet shown as structure or array?
                                         t='('t')'
                                      complexSignatures~put(t) -- remember complex type
                                   end

                 m~~append('</span>') ~~append(crlf)   -- TD # 6 - kind
                 m~~append(tab6)
                 m~~append('<span class="arrow">') ~~append(' &rarr;&nbsp;') ~~append("</span>")
                 m~~append('<span class="signature">') ~~append(pp(t)) ~~append("</span>")
                 m~~append('</td>') ~~append(crlf)   -- TD # 7 - show untranslated argSignature
              end

              else      -- a method_call or signal
              do

                 tmpStr=""    -- create partly substituted argument string

                 mm=.MutableBuffer~new
                 bFirstRun=.true
                 do a over meth~content   -- iterate over input attributes
                    if a~isA(.IDbusArg), a~direction="in" then
                    do
                       if bFirstRun=.false then mm~append('<span class="comma">, </span>')
                       mm~~append(crlf)~~append(tab7)

                       t=a~type
                       mm~~append('<span class="argtype">')
                       if dt~hasIndex(t) then mm~~append(dt[t])
                                         else
                                         do
                                            mm~~append(t)
                                            complexSignatures~put(t) -- remember complex type
                                         end
                       mm~~append('</span>')

                       if a~name<>"" then
                          mm~~append(' <span class="argname">')~~append(a~name)~~append('</span>')

                       bFirstRun=.false
                    end
                 end
                 tmpStr=mm~string

                 m~~append(tab6)~~append('<td class="result signature">')

                 replyS=meth~replySignature
                 if dt~hasIndex(replyS) then replyS=dt[replyS] -- simple type? then substitute!
                                        else
                                        do
                                            if pos(replyS~left(1),'a(')=0 then -- not explicitly shown as structure or array?
                                               replyS='('replyS')'      -- show as structure
                                            complexSignatures~put(replyS)
                                        end

                 m~~append('<span class="result argtype">')
                 if kind="signal" then    -- on signals: don't show 'void' as result type, as it is clear for DBus that they do not return anything
                    m~~append('&nbsp;')
                 else
                    m~~append(replyS)
                 m~~append('</span>')  -- TD # 2 - result

                 m~~append('</td>')~~append(crlf)

                 m~~append(tab6)~~append('<td class="kind"><em class="kind">') ~~append(kind) ~~append('</em></td>')~~append(crlf)   -- TD # 3 - kind
                 m~~append(tab6)~~append('<td colspan="3" class="methodname">') ~~append('<span class="method name">')~~append(meth~name) ~~append('</span>')

                 m~~append('<span class="arguments">') ~~append('<span class="paren">(</span>') ~~append(tmpStr~strip)
                 m~~append(crlf)~~append(tab7)~~append('<span class="paren">)</span></span>') ~~append(crlf)
                 if meth~argSignature<>"" then
                 do
                    m~~append(tab6) ~~append('<span class="arrow">') ~~append(' &rarr;&nbsp;') ~~append("</span>")
                    m~~append('<span class="signature">')~~append(pp(meth~argSignature))
                    m~~append('</span>') -- ~~append(crlf)   -- TD # 4 - name
                 end
                 m~~append(tab6)~~append('</td>') ~~append(crlf)   -- TD # 7 - show untranslated argSignature
              end

              m~~append(tab5)~~append('</tr>')~~append(crlf)
           end

           -- end of interface description
           m~~append(tab4) ~~append('</table>')~~append(crlf)
           m~~append(tab3) ~~append('</li>') ~~append(crlf)~~append(crlf)
        end

     end
     m~~append(tab2) ~~append('</ul>') ~~append(crlf) ~~append(crlf)
  end

  m~~append(tab2)~~append('<hr/>')~~append(crlf) ~~append(crlf)
  return






htmlShowDatatypes: procedure expose (exposeVars)
  use strict arg m
  dt=.dbus.dir~dataTypes


  m~~append('<h1>DBus Datatype Character List</h1>') ~~append(crlf)
  m~~append('<table class="datatype">') ~~append(crlf)

  sortedIndex=dt~allindexes~sort
  sortedItems=dt~allItems~sort

  bEven=.false
  do i=1 to sortedIndex~size
     char=sortedIndex[i]
     if bEven then
        m~~append(tab1) ~~append('<tr class="even">') ~~append(crlf)
     else
        m~~append(tab1) ~~append('<tr class="odd">') ~~append(crlf)

     m~~append(tab2) ~~append('<td class="argtype">') ~~append(pp(char)) ~~append('</td>') ~~append(crlf)
     m~~append(tab2) ~~append('<td class="datatype">') ~~append(" &hellip; ") ~~append(pp(dt[char])) ~~append('</span></td>') ~~append(crlf)

     m~~append(tab2) ~~append('<td class="emptycell">&nbsp;</td>') ~~append(crlf)

     datatype=sortedItems[i]
     char    =dt~index(datatype)
     m~~append(tab2) ~~append('<td class="datatype">') ~~append(pp(datatype)) ~~append('</td>') ~~append(crlf)
     m~~append(tab2) ~~append('<td class="argtype">') ~~append(" &hellip; ") ~~append(pp(char)) ~~append('<td>') ~~append(crlf)

     m~~append(tab1) ~~append('</tr>')~~append(crlf)
     bEven=\bEven    -- toggle
  end

  m~~append('</table>') ~~append(crlf)
  m~~append('<hr/>')~~append(crlf) ~~append(crlf)
  return



htmlDumpComplextTypes: procedure expose (exposeVars)  -- show complex types and a more human rendering of it
  use strict arg m, set

  m~~append('<h1>Complex Types Rendered in a More Human Readable Form </h1>') ~~append(crlf)
  m~~append('<dl class="complex">') ~~append(crlf)

  bEven=.false
  do ct over set~allindexes~sort
     if bEven then
        m~~append(tab1) ~~append('<dt class="even argtype">')
     else
        m~~append(tab1) ~~append('<dt class="odd argtype">')

     m ~~append(ct) ~~append('</dt>') ~~append(crlf)
     m ~~append(tab2)

     if bEven then
        m~~append('<dd class="argtype even">')
     else
        m~~append('<dd class="argtype odd">')

     m~~append('<span class="arrow">') ~~append('&rarr; ') ~~append('</span>')
     m~~append(decipher(ct)) ~~append('</dd>') ~~append(crlf)

     bEven=\bEven    -- toggle
  end

  m~~append('</dl>') ~~append(crlf)
  m~~append('<hr/>')~~append(crlf) ~~append(crlf)
  return





::routine decipher
  use arg signature, dt=(.dbus.dir~dataTypes)

  tmpStr=""
  do while signature<>""
     parse var signature char +1 signature

     if tmpStr<>"" then tmpStr=tmpStr", "

     if char="a" then
     do
        str=""
        typePos=verify(signature,"a","NoMatch")
        typeChar=signature~subChar(typePos)
        dim=typePos  -- dimensionality of array

        if typeChar="{" then  -- a dict
           dim-=1       -- reduce dimensionality by one

        if str<>"" then str=str", "
        str=str || "array of ["~copies(dim)

        if pos(typeChar,"({")>0 then
        do
           if typeChar="{" then        -- a dict
           do
              endPos=findMatchingEnd(signature,typePos+1, "{", "}")
              -- parse local signature without encompassing brackets, skip closing bracket for next signature
              parse var signature =(typePos+1) localSignature +(endPos-typePos-1) . +1 signature
              str=str || "dict of {"decipher(localSignature)"}"
           end

           else if typeChar="(" then   -- a struct
           do
              endPos=findMatchingEnd(signature,typePos+1, "(", ")")
               -- parse local signature without encompassing brackets, skip closing bracket for next signature
              parse var signature =(typePos+1) localSignature +(endPos-typePos-1) . +1 signature
              str=str || "struct of ("decipher(localSignature)")"
           end
        end
        else      -- simple type
        do
           str=str || dt[typeChar]  -- get human readable representation
           signature=substr(signature,typePos+1)
        end
        tmpStr=tmpStr || str || "]"~copies(dim)

     end
     else if char="(" then    -- a struct
     do
        endPos=findMatchingEnd(signature,1, "(", ")")
         -- parse local signature without encompassing brackets, skip closing bracket for next signature
        parse var signature localSignature +(endPos-1) . +1 signature
        tmpStr=tmpStr || "struct of (" || decipher(localSignature) || ")"
     end

     else   -- a simple type
     do
         tmpStr=tmpStr || dt[char]
     end
  end
  return tmpStr



findMatchingEnd: procedure       -- returns pos of matching end parenthesis; start points after opening char
  use arg signature, start=2, open="(", end=")"

  count=1                        -- number of open parenthesis seen so far
  do until pos=0
     pos=verify(signature, open||end, "Match", start)
     if signature~subChar(pos)=open then count+=1
                                    else count-=1
     if count=0 then return pos  -- return position of matching end char
     start=pos+1     -- next round
  end
  return -1


::routine dumpStatistics      -- dump connection's statistics
  use arg conn
  statistics=conn~statistics
  say "conn~collectStatistics:" pp(conn~collectStatistics)
  call dump statistics, "statistics"
  do e over .array~of("lastSentError","lastSentMessage", "lastSentSignal", "lastReceivedError", "lastReceivedMessage", "lastReceivedSignal")
      d=statistics~entry(e)
      if d=.nil then iterate
      say "   " pp(e)":"
      call dump d, e"'s slotDir:"
  end

  started=statistics~started
  now=.dateTime~new
  say "started: " pp(started)
  say "now:     " pp(now)
  say "duration:" pp(now-started)


/* from "rgf_util2.rex", using pp() instead of pp2():
   create and return a string rendering of the supplied condition object rgf, 2011-06-08
*/
::routine ppCondition
  use strict arg co, indent1="09"x, lf=.endOfLine

  indent2=indent1~copies(2)
  indent3=indent1~copies(3)

  maxWidth=0            -- determine length of widest index
  do idx over co
     if idx~isA(.string) then maxWidth=max(maxWidth,idx~length)
  end
  maxWidth+=2           -- add square brackets

  mb=.MutableBuffer~new

  do idx over co~allindexes~sort
     entry=co[idx]
     mb~~append(indent1) ~~append(pp(idx)~left(maxWidth)) ~~append("=") ~~append(pp(entry)) ~~append(lf)
     if entry~isA(.collection) then
     do val over entry
        mb ~~append(indent2) ~~append(pp(val)) ~~append(lf)
     end
  end

  return mb~string


/* ======================================================================= */
/* copied from "rgf_util2.rex", removed non-available features, hence simplified version, ---rgf, 20141104
*/
::routine dump
  use arg coll, title=("type: The" coll~class~id "class"), comparator=.nil

  if .nil=comparator, title~isA(.comparator) then
  do
     comparator=title
     title=("type: The" coll~class~id "class")
  end


  if coll~isA(.supplier) then
  do
     s=coll
     len=5  -- define an arbitrary high width
     say title
  end
  else if \coll~isA(.Collection) then   -- make sure we have a Collection else
  do
     if arg(2,"E") then    -- title omitted !
        say title

     say "DUMP(): ---> argument to dump is *NOT* a *COLLECTION/SUPPLIER* ! <--- "
     say "                       type:" pp(coll~class)
     say "       default string value:" pp(coll)
     -- .ArgUtil~validateClass("collection", coll, .Collection) -- must be of type Collection
     return
  end
  else      -- a collection in hand
  do
     say title": ("coll~items "items)"
     len=length(coll~items)
  end

  say
  count=0


  if coll~isA(.Collection) then
  do
     s=makeSortedSupplier(coll, comparator)
  end

   -- determine maximum length of "pretty printed" index-value
  maxWidth=0
  s2=s~copy
  do while s2~available
     maxWidth=max(maxWidth,length(s2~index)+2)
     s2~next
  end

  count=0
  do while s~available
     count=count+1
     say "   " "#" right(count,len)":" "index="pp(s~index)~left(maxWidth) "-> item="pp(s~item)
     s~next
  end
  say "-"~copies(50)
  return


/* Sort a collection considering its type and return a sorted supplier object. */
makeSortedSupplier: procedure
  use arg coll, comparator=.nil

  if coll~isA(.OrderedCollection) then    -- don't sort, just return the supplier
     return coll~supplier

  if coll~isA(.SetCollection) then        -- use items part, sort it and return it as a supplier
  do
     arr=coll~allItems                    -- get array representation
     call sortArray arr, comparator       -- sort elements
     return .supplier~new(arr, arr)       -- return supplier with sorted elements
  end

  if coll~hasMethod('allAt') then         -- handle collections with idx -> coll
  do
     arr=.set~new~union(coll~allIndexes)~makeArray -- remove duplicate indexes, if any
     call sortArray arr, comparator       -- sort elements

     arr2=.array~new

     do i=1 to arr~items                  -- iterate over all indexes
        tmp=coll~allAt(arr[i])            -- get all items associated with index
        if tmp~items=1 then
           arr2[i]=tmp~at(1)              -- save single item to show
        else
           arr2[i]=coll~allAt(arr[i])     -- save collection of associated items

     end

     return .supplier~new(arr2, arr)
  end

   -- o.k. only MapCollection/Collection left, assuming 1:1 mapping between index and item
  arr=coll~allIndexes                  -- remove duplicate indexes, if any
  call sortArray arr, comparator       -- sort elements

  arr2=.array~new
  do i=1 to arr~items                  -- iterate over all indexes
     arr2[i]=coll[arr[i]]              -- retrieve item part
  end
  return .supplier~new(arr2, arr)


   -- just sort the passed in array, depending on whether a comparator is needed or not
sortArray: procedure
  use arg arr, comparator=.nil

  if comparator<>.nil then
     arr~sortWith(comparator)
  else
  do
     comparator=.CaselessComparator~new
     do i=1 to arr~items
        arr[i]=arr[i]~string
     end
     arr~sortWith(.CaselessComparator~new)
  end

  return


/* Make sure that the css file is available in current directory, if not, copy it from dbusdoc.rex' directory. */
::routine makeSureCssFileExists
  if \sysFileExists(.dbus.doc.css) then   -- does not exist, copy it into current directory!
  do
     parse source 1 opsys +1 . . s     -- get path to dbusdoc.rex
     sourceCSS=get_css(opsys,s)        -- get fully qualified path to css fle

     if sysFileExists(sourceCSS)=0 then      -- source CSS does not exist!
     do
        .error~say("error:" pp(sourceCSS) "is not available" )
        return
     end

     if sysFileCopy(sourceCSS,.dbus.doc.css)<>0 then  -- copy css file to current directory
        .error~say("error: could not copy" pp(sourceCSS) "to current directory" )
  end
if .dbus.doc.debug=.true then say "makeSureCssFileExists(): 1, returning" pp(s)

  return

get_css: procedure
  parse arg opsys, s
  src=filespec("Location",s) || .dbus.doc.css


  if sysFileExists(src) then
  do
if .dbus.doc.debug=.true then say "   get_css(): 1, returning existing" pp(src)
     return src   -- already found, return fully qualified path to css file
  end

  src="/opt/".dbus.doc.css          -- default for Unix (Linux, MacOSX)
  if opsys='W' then  -- in windows look for "%programfiles[(86)]%\dbusoorexx"
  do
         -- %ProgramFiles%, %ProgramFiles(x86)%, %ProgramW6432%
      homedir=value("programfiles",,"environment") -- ok for 32-on-32 or 64-on-64 (ooRexx-on-Windows)
      parse version ")_" bitness "-bit"
      if bitness=32 then   -- 32-bit ooRexx: on 32-bit or 64-bit Windows ?
      do
         tmp=value("ProgramFiles(x86)",,"environment")   -- 32-bit ooRexx on 64-bit Windows?
         if tmp<>"" then
            homedir=tmp
      end
      src=homedir || .file~separator || .dbus.doc.css -- build fully qualified css-file-name
  end
  if sysFileExists(src) then
  do
if .dbus.doc.debug=.true then say "   get_css(): 2, returning existing" pp(src)
     return src   -- found, return fully qualified path to css file
  end
if .dbus.doc.debug=.true then say "   get_css(): 3," pp(.dbus.doc.css) "not found, returning empty string"
  return ""

