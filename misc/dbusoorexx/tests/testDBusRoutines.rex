#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-07-16
      name:       testDBusRoutines.rex
      purpose:    test the DBusDataType() routine (and the external function DBusValidateSignature())
      license:    AL 2.0
*/

busNames      =.array~of("a", ".a", "1.a", "a.bcd", "a..c", "a.b/c.d", "a.bcd.123_efgh", "a-BCD-123.efgh", "1", "1:123", ":123", ":1.234")
interfaceNames=.array~of("a", ".a", "1.a", "a.bcd", "a..c", "a.b/c.d", "a.bcd", "a.bcd.123_efgh", "a-BCD-123.efgh", "1", "1:123", ":123", ":1.234")
memberNames   =.array~of("a", "", , "1a", "a_bcd", "a_bcd.123_efgh", "a-BCD-123.efgh", "1", "1:123", ":123", ":1.234")
objectPaths   =.array~of("/", "//", "/a/", "/a//b", "/a/b#c/d", "/a", "/a/bcd", "/a/bcd/123_efgh", "/a-BCD-123.efgh", "/1", "1:123", ":123", ":1.234")
signatures    =.array~of("saay", "", "a[as]", "sa{ss}", "sa(tsk)as", "aSab", "a", "v", "vv", -
                         "as(", "as)", "{ss}", "a{", "as}", "ss{ss}ss", -
                         "s(((((((((((((((((((((((((((((((((((((ss)))))))))))))))))))))))))))))))))))))s",-
                         "saaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaasss" -
                         )

headings=.array~of("Bus Names", "Interface Names", "Member Names", "Object Paths", "Signatures")
tests   =.array~of(busNames,    interfaceNames,    memberNames, objectPaths,       signatures)

do i=1 to headings~size
   h=headings[i]
   say center("" h "",79, "-")

   t=tests[i]
   say "t:" pp2(t)

   do k=1 to t~size
      val=t[k]
      say "  " "val="pp2(val) "DbusDataType(val)="pp2(DbusDataType(val))
   end
   say "-"~copies(79)
end
say


do i=1 to headings~size
   h=headings[i]
   say center("" h "",79, "-")

   t=tests[i]
   say "t:" pp2(t)

   do k=1 to t~size
      val=t[k]
      say "  " "val="pp2(val) "DbusDataType(val,h)="pp2(DbusDataType(val,h))
   end
   say "-"~copies(79)
end
say "="~copies(79)
say


------------------------------
/* TODO: maybe we remove DbusValidateSignature() ? Maybe its bloating the binding?
 */
say "--"
do s over .list~of("s", "nixi,noxi", "gob", "aaaya(a(say))")
   say "-  DbusValidateSignature:" pp2(s) pp2(DbusValidate(s))
   say "   DbusValidateSignature:" pp2(s) pp2(DbusValidate(s,"S"))
   say "   DbusValidateSignature:" pp2(s) pp2(DbusValidate(s,"signature"))
   say "   DbusValidateSignature:" pp2(s) pp2(DbusValidate(s,))
end
say "-- "~copies(10)

do s over signatures
   say "   DbusValidateSignature:" pp2(s) pp2(DbusValidate(s,"s"))
end


::requires "dbus.cls"      -- get access to the DBUS functionality from Rexx
::requires "rgf_util2.rex" -- get utilities
