#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2020-06-23
   changes: 2025-05-29, ---rgf   - add prefix to reveal interpreter, thread and invocation

   purpose: demonstrate a controller with a public routine that can be invoked from
            an ooRexx FXML event handler script


   license: Apache License 2.0 (see at bottom)

   invoke:  called from FXML form

   needs: ooRexx 5.0 or higher, and BSF4ooRexx 6.00 or highter
*/
   /* @showsource - ending comment on different line, RexxScript annotation does not take effect
   */
parse source s
prefix=.mutableBuffer~new~append("[R", .context~interpreter, "] [T", .context~thread, "] [", .context~invocation, "]")~string ""

if .my.app~bDebug=1 then
   say prefix .line":" .dateTime~new pp(s)  "->" pp("demoTableViewController.rex") "..."


::routine setStylingType public
   use arg slotdir
   /* RexxScript annotation: @get(event) (will be fetched from slotDir's scriptContext) */
   if .my.app~showDebugOutput=.false then
   do
      prefix=""
      if .my.app~bDebug=1 then
         prefix=.mutableBuffer~new~append("[R", .context~interpreter, "] [T", .context~thread, "] [", .context~invocation, "]")~string ""

      say prefix || .line":" .context~name":" "arg()="arg() "event="pp(event) "slotDir="pp(slotDir)
      say prefix || .line":" .context~name":" "event="pp(event~toString)
      say prefix || .line":" .context~name":" "event~source="pp(event~source) pp(event~source~toString)
      say prefix || .line":" .context~name":" "event~target="pp(event~target) pp(event~target~toString)
      say prefix || .line":" .context~name":" "event~eventType="pp(event~eventType) pp(event~eventType~toString)
      say prefix || .line":" .context~name":" "event~source~selected="pp(event~source~selected)
      say prefix || "<---------------------------------------"
      say
   end

      -- fetch the id_tableview object
   id_vbox=.my.app~demoTableView.fxml~id_vbox

   currStyleId=event~source~id      -- fetch id value from menu item
   id_tableview=.my.app~demoTableView.fxml~id_tableview  -- get access to TableView
   select case currStyleId
      when "id_menu_style_none" then
           do
              .my.app~tableview_stylesheet_kind=0
              id_vbox~getStyleSheets~clear
           end

      when "id_menu_style_1", "id_menu_style_2" then
           do
              n=currStyleId~right(1)
              .my.app~tableview_stylesheet_kind=n
              cssFileName="file:styles-"n".css"
              id_vbox~getStyleSheets~~clear~~add(cssFileName)
           end

      when "id_menu_style_cell" then
              .my.app~tableview_stylesheet_kind=3

      otherwise nop
   end
   id_tableview~refresh     -- make sure the TableView refreshes

/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2020-2025 Rony G. Flatscher

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

