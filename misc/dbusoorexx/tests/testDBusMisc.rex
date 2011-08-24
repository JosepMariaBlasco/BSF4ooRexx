#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-07-16
      name:       testDBusMisc.rex
      purpose:    test miscellaneous things
      license:    AL 2.0
*/

conn=.dbus~session      -- get the session connection

say "conn~query('typeCodes')="pp2(conn~query("typeCodes"))
xrange=xrange("00"x,"ff"x)
say "conn~query('TypeCodes', xrange)="pp2(conn~query("TypeCodes", xrange))

xrange=xrange("a","z")
do i=1 to xrange~length
   char=xrange~subchar(i)
   say "conn~query('typeCode', "char"'):" pp2(conn~query("typeCode", char))
end

say "--"
say "conn~query('typeCode', 'h') (UNIX_FD):" pp2(conn~query("TypeCode", "h"))
say "conn~query('t', 'h') (UNIX_FD)       :" pp2(conn~query("t", "h"))
say "--"
say "conn~query('Authenticated')  :" pp2(conn~query("Authenticated"))
say "conn~query('A')              :" pp2(conn~query("A"))
say "conn~query('isAuthenticated'):" pp2(conn~query("isAuthenticated"))
say "conn~query('isA')            :" pp2(conn~query("isA"))
say "--"
say "conn~query('Open')           :" pp2(conn~query("Open"))
say "conn~query('O')              :" pp2(conn~query("O"))
say "conn~query('isOpen')         :" pp2(conn~query("isOpen"))
say "conn~query('isO')            :" pp2(conn~query("isO"))
say
say "conn~query('Connected')      :" pp2(conn~query("Connected"))
say "conn~query('C')              :" pp2(conn~query("C"))
say "conn~query('isConnected')    :" pp2(conn~query("isConnected"))
say "conn~query('isC')            :" pp2(conn~query("isC"))
say "--"
say "DBusVersion():" pp2(DBusVersion())


::requires "dbus.cls"      -- get access to the DBUS functionality from Rexx
::requires "rgf_util2.rex" -- get utilities
