; Name: BSF4ooRexx_x64_reinstall.nsi
; Purpose: NSIS reinstall script for BSF4ooRexx (64 bit)
; Author: Juergen Hesse, Manuel Paar
; Version: 1.0
;
; ------------------- Apache Version 2.0 license --------------------
;    Copyright (C) 2011 Juergen Hesse, Manuel Paar
;
;    Licensed under the Apache License, Version 2.0 (the "License");
;    you may not use this file except in compliance with the License.
;    You may obtain a copy of the License at
;
;          http://www.apache.org/licenses/LICENSE-2.0
;
;    Unless required by applicable law or agreed to in writing, software
;    distributed under the License is distributed on an "AS IS" BASIS,
;    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
;    implied.
;    See the License for the specific language governing permissions and
;    limitations under the License.
; -------------------------------------------------------------------

; load more functions
!include "MUI.nsh"
!include "EnvVarUpdate.nsh"

; set product name and uninstall registry-key
!define PRODUCT_NAME "BSF4ooRexx"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; TO CUSTOMIZE
; Source-Folder
!define BSF_ROOT ".\BSF4ooRexx_install\bsf4oorexx"
; Script Location (getBSFVersion.rex, ...)
!define SCRIPT_PATH "."


SetCompressor lzma

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${BSF_ROOT}\install\images\windows\bsf4rexx-install.ico"
!define MUI_UNICON "${BSF_ROOT}\install\images\windows\bsf4rexx-uninstall.ico"
; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES
; Language files
!insertmacro MUI_LANGUAGE "English"
; Reserve files
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

Var JAVA_INSTALLATION_MSG
Var REXX_HOME
Var REXX_VER
Var REXX_INSTALLATION_MSG
Var OO_HOME
Var LO_HOME
Var WINDOWS_ARCHITECTURE
Var SYSDRIVE
Var PRODUCT_VERSION

Name "${PRODUCT_NAME} $PRODUCT_VERSION"
OutFile "${PRODUCT_NAME}_Setup_x64_reinstall.exe"
InstallDir "$PROGRAMFILES64\${PRODUCT_NAME}"
XPStyle on       ; wenn XP, dann XP-Style verwenden
ShowInstDetails show
ShowUnInstDetails show

; uninstall old version
; copy all files to the destination folder
; create start menue entries
; create file associations
Section "BSF4ooRexx" SEC01
  SetShellVarContext all
  SetAutoClose true
  SetOverwrite ifnewer
  
  ; uninstall old version
  ExecWait '"$INSTDIR\uninst.exe" /S _?=$INSTDIR'
  
  ; copy all files to the destination folder
  SetOutPath "$INSTDIR"
  File "${BSF_ROOT}\addLinks2OOoSnippets.rxo"
  File "${BSF_ROOT}\ApacheLicense2.0.txt"
  File "${BSF_ROOT}\asm_LICENSE.TXT"
  File "${BSF_ROOT}\bsf-rexx-engine.jar"
  File "${BSF_ROOT}\bsf-v400-20090910.jar"
  File "${BSF_ROOT}\BSF.CLS"
  File "${BSF_ROOT}\install\64\BSF4ooRexx.dll"
  File "${BSF_ROOT}\BSF_OnTheFly.cls"
  File "${BSF_ROOT}\credits.txt"
  File "${BSF_ROOT}\janino_LICENSE.TXT"
  File "${BSF_ROOT}\refcardBSF4ooRexx.pdf"
  File "${BSF_ROOT}\refcardOOo.pdf"
  File "/oname=rexxj.cmd" "${BSF_ROOT}\rexxj2.cmd"
  File "${BSF_ROOT}\rexxj2.cmd"
  File "${BSF_ROOT}\rexxj2-32.sh"
  File "${BSF_ROOT}\rexxj2-64.sh"
  File "${BSF_ROOT}\rexxj2.sh"
  File "${BSF_ROOT}\rgf_util2.rex"
  File "${BSF_ROOT}\UNO.CLS"
  File "${BSF_ROOT}\UNO_API_info.rxo"
  File "${BSF_ROOT}\UNO_CREATE_INTERFACE_LIST.RXO"
  File "${BSF_ROOT}\UNO_ReflectionUtil.cls"
  File "${BSF_ROOT}\UNO_XINTERFACES.RXO"
  CopyFiles "$SYSDRIVE\${PRODUCT_NAME}_Setup_x64_reinstall.exe" "$INSTDIR"
  Delete "$SYSDRIVE\${PRODUCT_NAME}_Setup_x64_reinstall.exe"

  File /r "${BSF_ROOT}\information"
  File /r "${BSF_ROOT}\install"
  File /r "${BSF_ROOT}\samples"
  File /r "${BSF_ROOT}\Templates"
  File /r "${BSF_ROOT}\utilities"

  ; create start menue entries
  CreateDirectory "$SMPROGRAMS\${PRODUCT_NAME}"
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\GUI RexxTry Program (ooRexxTry.rxj).lnk" "$INSTDIR\utilities\ooRexxTry.rxj" "" "$INSTDIR\install\images\windows\oorexxtry.ico"
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Information (ReleaseNotes, introductions to ooRexx, ${PRODUCT_NAME} overview).lnk" "$INSTDIR\information"

  CreateDirectory "$SMPROGRAMS\${PRODUCT_NAME}\Installation"
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Installation\${PRODUCT_NAME} Download Page.lnk" "http://sourceforge.net/projects/bsf4oorexx/"
  
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Samples (simple to comprehensive ${PRODUCT_NAME} examples).lnk" "$INSTDIR\samples"
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Utilities (some platform-independent utility scripts).lnk" "$INSTDIR\utilities"
  
  ; create file associations
  WriteRegStr HKCR ".JREXX" "" "RexxJavaScript"
  WriteRegStr HKCR ".JREXX" "Content Type" "text/x-rexx-java"
  WriteRegStr HKCR ".JREXX" "PerceivedType" "text"
  
  WriteRegStr HKCR ".ORX" "" "REXXScript"
  WriteRegStr HKCR ".ORX" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".ORX" "PerceivedType" "text"

  WriteRegStr HKCR ".REXX" "" "REXXScript"
  WriteRegStr HKCR ".REXX" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".REXX" "PerceivedType" "text"

  WriteRegStr HKCR ".REXXJ" "" "RexxJavaScript"
  WriteRegStr HKCR ".REXXJ" "Content Type" "text/x-rexx-java"
  WriteRegStr HKCR ".REXXJ" "PerceivedType" "text"
  
  WriteRegStr HKCR ".rxj" "" "RexxJavaScript"
  WriteRegStr HKCR ".rxj" "Content Type" "text/x-rexx-java"
  WriteRegStr HKCR ".rxj" "PerceivedType" "text"
  WriteRegStr HKCR ".rxj\ShellNew" "FileName" "$INSTDIR\Templates\RexxJavaScript (Rexx script using Java) executed by ooRexx.rxj"

  WriteRegStr HKCR ".TESTGROUP" "" "REXXScript"
  WriteRegStr HKCR ".TESTGROUP" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".TESTGROUP" "PerceivedType" "text"

  WriteRegStr HKCR ".TESTUNIT" "" "REXXScript"
  WriteRegStr HKCR ".TESTUNIT" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".TESTUNIT" "PerceivedType" "text"

  WriteRegStr HKCR "RexxJavaScript" "" "RexxJavaScript (Rexx script using Java) executed by ooRexx"
  WriteRegStr HKCR "RexxJavaScript\DefaultIcon" "" "$INSTDIR\install\images\windows\bsf4oorexx.ico,0"
  WriteRegStr HKCR "RexxJavaScript\shell" "" "open"
  WriteRegStr HKCR "RexxJavaScript\shell\edit" "" "Edit"
  WriteRegStr HKCR "RexxJavaScript\shell\edit\command" "" 'notepad.exe "%1"'
  WriteRegStr HKCR "RexxJavaScript\shell\open" "" "Run"
  WriteRegStr HKCR "RexxJavaScript\shell\open\command" "" '"$REXX_HOME\rexx.exe" "%1" %*'
  
  WriteRegStr HKCR ".CLS" "" "REXXScript"
  WriteRegStr HKCR ".CLS" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".CLS" "PerceivedType" "text"
  
  DeleteRegKey HKCR ".rex"
  WriteRegStr HKCR ".rex" "" "REXXScript"
  WriteRegStr HKCR ".rex" "Content Type" "text/x-rexx"
  WriteRegStr HKCR ".rex" "PerceivedType" "text"
  WriteRegStr HKCR ".rex\ShellNew" "FileName" "$INSTDIR\Templates\RexxScript (Rexx script) executed by ooRexx.rex"
  
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.rxj"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.rxj\OpenWithList" "a" "rexx.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.rxj\OpenWithList" "MRUList" "a"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.rxj\OpenWithProgids" "RexxJavaScript" ""

  WriteRegStr HKCR "Mime\Database\Content Type\text/x-rexx" "Extension" ".rex"
  WriteRegStr HKCR "Mime\Database\Content Type\text/x-rexx-java" "Extension" ".rxj"
  
  WriteRegStr HKCR "REXXScript" "" "RexxScript (Rexx script) executed by ooRexx"
  WriteRegStr HKCR "REXXScript\DefaultIcon" "" "$INSTDIR\install\images\windows\oorexx.ico,0"
  WriteRegStr HKCR "REXXScript\shell" "" "open"
  WriteRegStr HKCR "REXXScript\shell\edit" "" "Edit"
  WriteRegStr HKCR "REXXScript\shell\edit\command" "" 'notepad.exe "%1"'
  WriteRegStr HKCR "REXXScript\shell\open" "" "Run"
  WriteRegStr HKCR "REXXScript\shell\open\command" "" '"$REXX_HOME\rexx.exe" "%1" %*'

  ; set CLASSPATH and PATH
  ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$INSTDIR\bsf-rexx-engine.jar"
  ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$INSTDIR\bsf-v400-20090910.jar"
  
  ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$INSTDIR"

  SetRegView 64
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\1.6" "RuntimeLib"
  ${StrRep} $2 "$1" "\jvm.dll" ""
  ${StrRep} $2 "$2" "client" "server"
  ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$2"

  ; is OpenOffice installed?
  StrCmp "" $OO_HOME SearchLibreOffice OOFound

  ; yes, so copy necessary files
  OOFound:
     KillProcDLL::KillProc "soffice.exe"
     KillProcDLL::KillProc "soffice.bin"
     SetOutPath "$OO_HOMEprogram"
     File "${BSF_ROOT}\install\64\BSF4ooRexx.dll"
     File "${BSF_ROOT}\BSF.CLS"
     File "${BSF_ROOT}\UNO.CLS"
     File "${BSF_ROOT}\UNO_API_info.rxo"
     File "${BSF_ROOT}\UNO_CREATE_INTERFACE_LIST.RXO"
     File "${BSF_ROOT}\UNO_ReflectionUtil.cls"
     File "${BSF_ROOT}\UNO_XINTERFACES.RXO"

     SetOutPath "$OO_HOMEBasis\share\Scripts\javascript"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\javascript\HowtoCreateApiInfo"

     SetOutPath "$OO_HOMEBasis\share\Scripts"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\oorexx"

     SetOutPath "$OO_HOMEBasis\share\Scripts\python"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\python\pythonSamples"

     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$OO_HOMEprogram\"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$OO_HOMEBasis\program\classes\unoil.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$OO_HOMEURE\java\ridl.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$OO_HOMEURE\java\jurt.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$OO_HOMEURE\java\juh.jar"

     ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$OO_HOMEprogram"

     SetOutPath "$INSTDIR\install"
     ExecWait '"$OO_HOMEprogram\unopkg.exe" add --shared ScriptProviderForooRexx.oxt'
     StrCmp "" $LO_HOME AddOfficeReg LOFound

  SearchLibreOffice:
     ; is LibreOffice installed?
     StrCmp "" $LO_HOME Done LOFound

  ; yes, so copy necessary files
  LOFound:
     KillProcDLL::KillProc "soffice.exe"
     KillProcDLL::KillProc "soffice.bin"
     SetOutPath $LO_HOME"program"
     File "${BSF_ROOT}\install\64\BSF4ooRexx.dll"
     File "${BSF_ROOT}\BSF.CLS"
     File "${BSF_ROOT}\UNO.CLS"
     File "${BSF_ROOT}\UNO_API_info.rxo"
     File "${BSF_ROOT}\UNO_CREATE_INTERFACE_LIST.RXO"
     File "${BSF_ROOT}\UNO_ReflectionUtil.cls"
     File "${BSF_ROOT}\UNO_XINTERFACES.RXO"

     SetOutPath "$LO_HOMEBasis\share\Scripts\javascript"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\javascript\HowtoCreateApiInfo"

     SetOutPath "$LO_HOMEBasis\share\Scripts"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\oorexx"

     SetOutPath "$LO_HOMEBasis\share\Scripts\python"
     File /r "${BSF_ROOT}\install\OOo_ScriptLibraries\python\pythonSamples"

     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$LO_HOMEprogram\"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$LO_HOMEBasis\program\classes\unoil.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$LO_HOMEURE\java\ridl.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$LO_HOMEURE\java\jurt.jar"
     ${EnvVarUpdate} $0 "CLASSPATH" "A" "HKLM" "$LO_HOMEURE\java\juh.jar"

     ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$LO_HOMEprogram"

     SetOutPath "$INSTDIR\install"
     ExecWait '"$LO_HOMEprogram\unopkg.exe" add --shared ScriptProviderForooRexx.oxt'
     Goto AddOfficeReg

  AddOfficeReg:
     WriteRegStr HKCR ".RXO" "" "RexxOOoScript"
     WriteRegStr HKCR ".RXO" "Content Type" "text/x-rexx-java-ooo"
     WriteRegStr HKCR ".RXO" "PerceivedType" "text"
     WriteRegStr HKCR ".RXO\ShellNew" "FileName" "$INSTDIR\Templates\RexxOOoScript (Rexx script using OpenOffice.org) executed by ooRexx.rxo"

     WriteRegStr HKCR "RexxOOoScript" "" "RexxOOoScript (Rexx script using OpenOffice.org/LibreOffice) executed by ooRexx"
     WriteRegStr HKCR "RexxOOoScript\DefaultIcon" "" "$INSTDIR\install\images\windows\oorexx4ooo.ico,0"
     WriteRegStr HKCR "RexxOOoScript\shell" "" "open"
     WriteRegStr HKCR "RexxOOoScript\shell\edit" "" "Edit"
     WriteRegStr HKCR "RexxOOoScript\shell\edit\command" "" 'notepad.exe "%1"'
     WriteRegStr HKCR "RexxOOoScript\shell\open" "" "Run"
     WriteRegStr HKCR "RexxOOoScript\shell\open\command" "" '"$REXX_HOME\rexx.exe" "%1" %*'
     
     WriteRegStr HKCR "Mime\Database\Content Type\text/x-rexx-java-ooo" "Extension" ".rxo"
     Goto Done

  Done:
     ; reload file associations
     ExecWait "$INSTDIR\install\associationChanged.exe"
     MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully reinstalled."
SectionEnd

; Add Uninstall and Reinstall Entry to the start menue
Section -AdditionalIcons
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Installation\Uninstall ${PRODUCT_NAME}.lnk" "$INSTDIR\uninst.exe" "" "$INSTDIR\install\images\windows\bsf4rexx-uninstall.ico"
  CreateShortCut "$SMPROGRAMS\${PRODUCT_NAME}\Installation\Reinstall ${PRODUCT_NAME}.lnk" "$INSTDIR\${PRODUCT_NAME}_Setup_x64_reinstall.exe" "" "$INSTDIR\install\images\windows\bsf4rexx-reinstall.ico"
SectionEnd

; create Uninstaller and Uninstall registry entries
Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\install\images\windows\bsf4oorexx.ico"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "$PRODUCT_VERSION"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "NoRepair" 0
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "NoModify" 0
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "ModifyPath" "$INSTDIR\${PRODUCT_NAME}_Setup_x64_reinstall.exe"
SectionEnd

; if no silent mode, show success message
Function un.onUninstSuccess
  IfSilent jump_if_silent jump_if_not_silent

  jump_if_not_silent:
     HideWindow
     MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully uninstalled." IDOK

  jump_if_silent:

FunctionEnd

; if no silent mode, ask user if he want to uninstall
Function un.onInit
  Call un.getBSFVersion
  IfSilent jump_if_silent jump_if_not_silent

  jump_if_not_silent:
     MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Do you want to uninstall $(^Name) and all the components?" IDYES +2
     Abort

  jump_if_silent:

FunctionEnd

; undo all changes
Section Uninstall
  SetShellVarContext all
  
  ; is OpenOffice/LibreOffice installed?
  Call un.FindOpenOffice
  Call un.FindLibreOffice
  StrCmp "" $OO_HOME SearchLibreOffice OOFound

  ; OpenOffice found: so delete files
  OOFound:
     KillProcDLL::KillProc "soffice.exe"
     KillProcDLL::KillProc "soffice.bin"

     RmDir /r "$OO_HOMEBasis\share\Scripts\javascript\HowtoCreateApiInfo"
     RmDir /r "$OO_HOMEBasis\share\Scripts\oorexx"
     RmDir /r "$OO_HOMEBasis\share\Scripts\python\pythonSamples"

     ExecWait "$OO_HOMEprogram\unopkg.exe remove --shared ScriptProviderForooRexx.oxt"

     Delete "$OO_HOMEprogram\BSF4ooRexx.dll"
     Delete "$OO_HOMEprogram\BSF.CLS"
     Delete "$OO_HOMEprogram\UNO.CLS"
     Delete "$OO_HOMEprogram\UNO_API_info.rxo"
     Delete "$OO_HOMEprogram\UNO_CREATE_INTERFACE_LIST.RXO"
     Delete "$OO_HOMEprogram\UNO_ReflectionUtil.cls"
     Delete "$OO_HOMEprogram\UNO_XINTERFACES.RXO"

     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$OO_HOMEprogram\"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$OO_HOMEBasis\program\classes\unoil.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$OO_HOMEURE\java\ridl.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$OO_HOMEURE\java\jurt.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$OO_HOMEURE\java\juh.jar"

     ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$OO_HOMEprogram"
     StrCmp "" $LO_HOME RemOfficeReg LOFound

  SearchLibreOffice:
     ; is LibreOffice installed?
     StrCmp "" $LO_HOME Done LOFound

  ; LibreOffice found: so delete files
  LOFound:
     KillProcDLL::KillProc "soffice.exe"
     KillProcDLL::KillProc "soffice.bin"

     RmDir /r "$LO_HOMEBasis\share\Scripts\javascript\HowtoCreateApiInfo"
     RmDir /r "$LO_HOMEBasis\share\Scripts\oorexx"
     RmDir /r "$LO_HOMEBasis\share\Scripts\python\pythonSamples"

     ExecWait "$LO_HOMEprogram\unopkg.exe remove --shared ScriptProviderForooRexx.oxt"

     Delete "$LO_HOMEprogram\BSF4ooRexx.dll"
     Delete "$LO_HOMEprogram\BSF.CLS"
     Delete "$LO_HOMEprogram\UNO.CLS"
     Delete "$LO_HOMEprogram\UNO_API_info.rxo"
     Delete "$LO_HOMEprogram\UNO_CREATE_INTERFACE_LIST.RXO"
     Delete "$LO_HOMEprogram\UNO_ReflectionUtil.cls"
     Delete "$LO_HOMEprogram\UNO_XINTERFACES.RXO"

     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$LO_HOMEprogram\"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$LO_HOMEBasis\program\classes\unoil.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$LO_HOMEURE\java\ridl.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$LO_HOMEURE\java\jurt.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$LO_HOMEURE\java\juh.jar"

     ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$LO_HOMEprogram"
     Goto RemOfficeReg

  RemOfficeReg:
     DeleteRegKey HKCR ".RXO"
     DeleteRegKey HKCR "RexxOOoScript"
     DeleteRegKey HKCR "Mime\Database\Content Type\text/x-rexx-java-ooo"
     Goto Done

  Done:
     SetRegView 64 ; change registry mode to 64 bit
     DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
     SetRegView 32 ; change registry mode to 32 bit
     DeleteRegKey HKCR ".JREXX"
     DeleteRegKey HKCR ".ORX"
     DeleteRegKey HKCR ".REXX"
     DeleteRegKey HKCR ".REXXJ"
     DeleteRegKey HKCR ".RXJ"
     DeleteRegKey HKCR ".TESTGROUP"
     DeleteRegKey HKCR ".TESTUNIT"
     DeleteRegKey HKCR "RexxJavaScript"
     DeleteRegKey HKCR ".CLS"
     DeleteRegKey HKCR ".rex"
     DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.rxj"
     DeleteRegKey HKCR "Mime\Database\Content Type\text/x-rexx"
     DeleteRegKey HKCR "Mime\Database\Content Type\text/x-rexx-java"

     WriteRegStr HKCR "REXXScript" "" "ooRexx Rexx Progam"
     WriteRegStr HKCR "REXXScript\DefaultIcon" "" "$REXX_HOME\rexx.exe,0"
     
     ; reload file associations
     ExecWait "$INSTDIR\install\associationChanged.exe"
     
     RMDir /r "$INSTDIR"
     RMDir /r "$SMPROGRAMS\${PRODUCT_NAME}"
     RMDir "$INSTDIR"
     RMDir "$SMPROGRAMS\${PRODUCT_NAME}"

     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$INSTDIR\bsf-rexx-engine.jar"
     ${un.EnvVarUpdate} $0 "CLASSPATH" "R" "HKLM" "$INSTDIR\bsf-v400-20090910.jar"

     ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$INSTDIR"
     
     SetRegView 64
     ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\1.6" "RuntimeLib"
     ${un.StrRep} $2 "$1" "\jvm.dll" ""
     ${un.StrRep} $2 "$2" "client" "server"
     ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$2"
     
     SetAutoClose true
SectionEnd

; check if Java and ooRexx is installed
; find OpenOffice/LibreOffice folder if is installed
Function .onInit
    ReadEnvStr $SYSDRIVE SYSTEMDRIVE
    InitPluginsDir

    Call getBSFVersion
    GetVersion::WindowsPlatformArchitecture
    Pop $WINDOWS_ARCHITECTURE ;32 or 64
    StrCmp $WINDOWS_ARCHITECTURE "64" x64 x32
    
    x64:
        Call FindooRexx
        StrCmp "" $REXX_INSTALLATION_MSG RexxSuccess RexxNoSuccess
        Goto Done
    x32:
        MessageBox MB_OK|MB_ICONEXCLAMATION "You need a 64 bit Windows to start this installer."
        Abort

    RexxSuccess:
        Call FindJava
        StrCmp "" $JAVA_INSTALLATION_MSG JavaSuccess JavaNoSuccess

    JavaSuccess:
        ; save reinstaller
        CopyFiles "$INSTDIR\${PRODUCT_NAME}_Setup_x64_reinstall.exe" "$SYSDRIVE\"
        Call FindOpenOffice
        Call FindLibreOffice
        Goto Done

    JavaNoSuccess:
        MessageBox MB_OK|MB_ICONEXCLAMATION $JAVA_INSTALLATION_MSG
        Goto Error

    RexxNoSuccess:
        MessageBox MB_OK|MB_ICONEXCLAMATION $REXX_INSTALLATION_MSG
        Goto Error

    Error:
        RMDir /r "$INSTDIR"
        Abort

    Done:
FunctionEnd

; read the BSF4ooRexx version by using getBSFVersion.rex
Function getBSFVersion
    Push $0

    SetOutPath "$INSTDIR"
    File "${SCRIPT_PATH}\getBSFVersion.rex"
    File "${BSF_ROOT}\BSF.CLS"
    ExecWait "rexx getBSFVersion.rex"

    FileOpen $0 "$INSTDIR\BSFVersion.txt" r
    FileRead $0 $PRODUCT_VERSION
    FileClose $0

    Delete "$INSTDIR\getBSFVersion.rex"
    Delete "$INSTDIR\BSF.CLS"
    Delete "$INSTDIR\BSFVersion.txt"

    Pop $0
FunctionEnd

; read the BSF4ooRexx version by using getBSFVersion.rex
; uninstall function
Function un.getBSFVersion
    Push $0

    SetOutPath "$INSTDIR"
    File "${SCRIPT_PATH}\getBSFVersion.rex"
    File "${BSF_ROOT}\BSF.CLS"
    ExecWait "rexx getBSFVersion.rex"

    FileOpen $0 "$INSTDIR\BSFVersion.txt" r
    FileRead $0 $PRODUCT_VERSION
    FileClose $0

    Delete "$INSTDIR\getBSFVersion.rex"
    Delete "$INSTDIR\BSF.CLS"
    Delete "$INSTDIR\BSFVersion.txt"

    Pop $0
FunctionEnd

; read Java version by using java_version.rex
Function FindJava
    Push $0
    Push $1

    SetOutPath "$INSTDIR"
    File "${SCRIPT_PATH}\java_version.rex"
    ExecWait "rexx java_version.rex bitness" $0
    ExecWait "rexx java_version.rex version" $1
    Delete "$INSTDIR\java_version.rex"
    StrCmp $0 "64" CheckVersion JavaVerNotCorrect

    CheckVersion:
        StrCmp $1 "1" Done JavaVerNotCorrect

    JavaVerNotCorrect:
        StrCpy $JAVA_INSTALLATION_MSG "You need Java version 1.4 (64 bit) or newer to run this program."

    Done:
        Pop $1
        Pop $0
FunctionEnd

; read ooRexx version from registry
; and bitness by using rexx_version.rex
Function FindooRexx
    Push $0
    Push $1
    Push $2
    Push $3

    ReadRegStr $REXX_VER HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\ooRexx" "DisplayVersion"
    StrCmp "" "$REXX_VER" RexxNotPresent
    ReadRegStr $0 HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment" "REXX_HOME"
    StrCmp "" $0 RexxNotPresent CheckRexxVer

    RexxNotPresent:
        StrCpy $REXX_INSTALLATION_MSG "ooRexx is not installed on your computer. You need version 4.1.0 (64 bit) or newer to run this program."
        Goto Done

    CheckRexxVer:
        GetFullPathName /SHORT $REXX_HOME "$0"
        StrCpy $0 $REXX_VER 1 0
        StrCpy $1 $REXX_VER 1 2
        StrCpy $2 $REXX_VER 1 4
        StrCpy $REXX_VER "$0$1$2"
        IntCmp 410 $REXX_VER FoundCorrectRexxVer FoundCorrectRexxVer RexxVerNotCorrect

    FoundCorrectRexxVer:
        IfFileExists "$REXX_HOME\rexx.exe" CheckRexxBitness RexxNotPresent

    CheckRexxBitness:
        SetOutPath "$INSTDIR"
        File "${SCRIPT_PATH}\rexx_version.rex"
        ExecWait "rexx rexx_version.rex" $3
        Delete "$INSTDIR\rexx_version.rex"
        StrCmp $3 "64" Done RexxVerNotCorrect

    RexxVerNotCorrect:
        StrCpy $REXX_INSTALLATION_MSG "The version of ooRexx installed on your computer is $0.$1.$2 ($3 bit). Version 4.1.0 (64 bit) or newer is required to run this program."

    Done:
        Pop $3
        Pop $2
        Pop $1
        Pop $0
FunctionEnd

; find OpenOffice folder
Function FindOpenOffice
    Push $0

    ReadRegStr $0 HKLM "SOFTWARE\WoW6432Node\OpenOffice.org\Layers\OpenOffice.org\3" "OFFICEINSTALLLOCATION"
    StrCmp "" $0 OO_2 Done

    OO_2:
        ReadRegStr $0 HKLM "SOFTWARE\OpenOffice.org\Layers\OpenOffice.org\3" "OFFICEINSTALLLOCATION"
        StrCmp "" $0 Done OOFound

    OOFound:
        GetFullPathName $OO_HOME "$0"
        Goto Done

    Done:
        Pop $0
FunctionEnd

; find OpenOffice folder (uninstall function)
Function un.FindOpenOffice
    Push $0

    ReadRegStr $0 HKLM "SOFTWARE\WoW6432Node\OpenOffice.org\Layers\OpenOffice.org\3" "OFFICEINSTALLLOCATION"
    StrCmp "" $0 OO_2 Done

    OO_2:
        ReadRegStr $0 HKLM "SOFTWARE\OpenOffice.org\Layers\OpenOffice.org\3" "OFFICEINSTALLLOCATION"
        StrCmp "" $0 Done OOFound

    OOFound:
        GetFullPathName $OO_HOME "$0"
        Goto Done

    Done:
        Pop $0
FunctionEnd

; find LibreOffice folder
Function FindLibreOffice
    Push $0

    ReadRegStr $0 HKLM "SOFTWARE\WoW6432Node\LibreOffice\Layers_\LibreOffice\3" "OFFICEINSTALLLOCATION"
    StrCmp "" $0 LO_2 Done

    LO_2:
        ReadRegStr $0 HKLM "SOFTWARE\LibreOffice\Layers_\LibreOffice\3" "OFFICEINSTALLLOCATION"
        StrCmp "" $0 Done LOFound

    LOFound:
        GetFullPathName $LO_HOME "$0"
        Goto Done

    Done:
        Pop $0
FunctionEnd

; find LibreOffice folder (uninstall function)
Function un.FindLibreOffice
    Push $0

    ReadRegStr $0 HKLM "SOFTWARE\WoW6432Node\LibreOffice\Layers_\LibreOffice\3" "OFFICEINSTALLLOCATION"
    StrCmp "" $0 LO_2 Done

    LO_2:
        ReadRegStr $0 HKLM "SOFTWARE\LibreOffice\Layers_\LibreOffice\3" "OFFICEINSTALLLOCATION"
        StrCmp "" $0 Done LOFound

    LOFound:
        GetFullPathName $LO_HOME "$0"
        Goto Done

    Done:
        Pop $0
FunctionEnd
