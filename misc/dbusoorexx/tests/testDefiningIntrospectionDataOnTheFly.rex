#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (C) 2011
      date:       2011-07-24
      name:       testDefiningIntrospectionDataOnTheFly.rex
      purpose:    if no introspection data can be retrieved, but one knows the
                  interfaces, demonstrate how to define them on the fly
      needs:      dbus.cls

      cf.:        dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)

      license:    Apache License 2.0

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2011 Rony G. Flatscher

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

   /* define introspect data */

node=.IntrospectHelper~new       -- create root node (could supply a name)
say "empty node:" node~makeString
say "---"
n1=node~addNode("nixiNoxi")
n2=node~addNode("abracadabra")
say "node after adding two nodes:" node~makeString
say "---"

if=node~addInterface("org.freedesktop.RealtimeKit1")
if~addMethod("MakeThreadRealtime","tu")
if~addMethod("MakeThreadRealtimeWithPID","ttu")

if=n1~addInterface("org.freedesktop.DBus.Introspectable")
if~addMethod("Introspect",,"s")  -- name, in-signature, out-signature


if=n2~addInterface("org.rexxla.rgf.tests")
if~addMethod("Test1","s(abi)a{si}aaaaxs","a(a{sa(isx)}i)")

if~addProperty("Prop1","s","write")
if~addProperty("Prop2","s","readwrite")
if~addProperty("Prop3","s","read")

if~addSignal("Signal1", "aaia{ss}x(itv)s")

say "node after adding interfaces to each:" node~makeString
say "---"

exit


node=.IntrospectHelper~new       -- create root node (could supply a name)
if=node~addInterface("org.freedesktop.DBus.Properties")
if~addMethod("Get","ss","v")
if~addMethod("Set","ssv","")

/*
if=node~addInterface("org.freedesktop.RealtimeKit1")
if~addMethod("MakeThreadRealtime","tu")
if~addMethod("MakeThreadRealtimeWithPID","ttu")
if~addMethod("MakeHighPriority","ti")
if~addMethod("MakeHighPriorityWithPID","tti")
if~addMethod("ResetKnown")
if~addMethod("ResetAll")
if~addMethod("Exit")
if~addProperty("RTTimeNSecMax","x","read")
if~addProperty("MaxRealtimePriority","i","read")
if~addProperty("MinNiceLevel","i","read")

if=node~addInterface("org.freedesktop.DBus.Introspectable")
if~addMethod("Introspect",,"s")  -- name, in-signature, out-signature

if=node~addInterface("org.rexxla.rgf.tests")
if~addMethod("Test1","s(abi)a{si}aaaaxs","a(a{sa(isx)}i)")

if~addProperty("Prop1","s","write")
if~addProperty("Prop2","s","readwrite")
if~addProperty("Prop3","s","read")

if~addSignal("Signal1", "aaia{ss}x(itv)s")
*/

chars=node~makeString
say "introspect data generated from input:"
say chars
say "-"~copies(50)

rootNode=.IDBus~newIntrospection(chars)
call IDBus.dumpIDBus rootNode
say "="~copies(50)


::requires "dbus.cls"
