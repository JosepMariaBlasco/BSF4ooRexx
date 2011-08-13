#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testMultipleConnections.rex
      date:    2011-07-20
      license: LGPL 3.0, AL 2.0
      purpose: - retrieving system and session bus in three different ways,
                 allows checking that the identical connections are returned
                 and used

      usage:   rexx testMultipleConnections

      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)
*/

conns=.array~new
do i=1 to 2
   say "#" i "(session)"
   conns~append(.DBus~connect('session'))
   conns~append(.DBus~new('session'))
   conns~append(.DBus~session)
end
say "---"

do i=1 to 2
   say "#" i "(system)"
   conns~append(.DBus~connect('system'))
   conns~append(.DBus~new('system'))
   conns~append(.DBus~system)
end
say "---"

do i=1 to conns~size
   c=conns[i]
   say "#" right(i,2)":" pp2(c) "c~busType="left(pp2(c~busType),9) "c~busName('unique')="pp2(c~busName('unique')) "c~uniqueBusId="pp2(c~uniqueBusId)
end

say "---"

say "DbusVersion()  ="pp2(DbusVersion())


::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities (gets installed by BSF4ooRexx)


