#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2020-06-22
   changes: --

   purpose: demonstrate a controller with a public routine that can be invoked directly from
            an ooRexx FXML event handler script; it will handle the interactions with the
            Stylesheet "id_menu_stylesheets" menu items

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx tpgm_demoListView.rex
         rexxj.{cmd|sh} tpgm_demoListView.rex

   needs: ooRexx 5.0 or higher, and BSF4ooRexx 6.00 or highter
*/
   /* @showsource - ending comment on different line, RexxScript annotation does not take effect
   */
parse source s
say .line":" .dateTime~new pp(s)  "->" pp("demoListViewController.rex") "..."

/** This routine will fetch the <code>event</code> JavaFX object from the global
    <em>ScriptContext</em> using the Rexx script annotation &quot;/* @get(event) */&quot;
    using <code>slotDir</code> argument.

    @param slotDir a Rexx directory supplied by the event handler script in the FXML file
*/
::routine setStylingType public
   use arg slotdir
   /* RexxScript annotation: @get(event) (will be fetched from slotDir's scriptContext) */
   if .my.app~showDebugOutput=.true then
   do
      say .line":" .context~name":" "arg()="arg() "event="pp(event) "slotDir="pp(slotDir)
      say .line":" .context~name":" "event="pp(event~toString)
      say .line":" .context~name":" "event~source="pp(event~source) pp(event~source~toString)
      say .line":" .context~name":" "event~target="pp(event~target) pp(event~target~toString)
      say .line":" .context~name":" "event~eventType="pp(event~eventType) pp(event~eventType~toString)
      say .line":" .context~name":" "event~source~selected="pp(event~source~selected)
      say "<---------------------------------------"
      say
   end

      -- fetch the id_listview object
   id_vbox=.my.app~demoListView.fxml~id_vbox

   currStyleId=event~source~id      -- fetch id value from menu item
   id_listView=.my.app~demoListView.fxml~id_listView  -- get access to ListView
   select case currStyleId
      when "id_menu_style_none" then
           do
              .my.app~listview_stylesheet_kind=0
              id_vbox~getStyleSheets~clear
           end

      when "id_menu_style_1", "id_menu_style_2" then
           do
              n=currStyleId~right(1)
              .my.app~listview_stylesheet_kind=n
              cssFileName="file:styles-"n".css"
              id_vbox~getStyleSheets~~clear~~add(cssFileName)
           end

      when "id_menu_style_cell" then
              .my.app~listview_stylesheet_kind=3

      otherwise nop
   end
   id_listView~refresh     -- make sure the ListView refreshes

/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2020 Rony G. Flatscher

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
