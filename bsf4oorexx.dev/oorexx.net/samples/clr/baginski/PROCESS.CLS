/* File:        PROCESS.CLS
 * Class:       Process
 * Description: An ooRexx package with a Rexx class named "Process" to wrap the functionality
                of the CLR/.Net class "System.Diagnostics.Process". It can be used to
                configure and start Windows processes based on information about application,
                file or using an instance of "System.Diagnostics.ProcessStartInfo".
 *
 *  license:
 *
 *  ------------------------ Apache Version 2.0 license -------------------------
 *     Copyright (C) 2016 Adrian Baginski
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *  -----------------------------------------------------------------------------
 */


::CLASS Process PUBLIC
  ::ATTRIBUTE Application
  ::ATTRIBUTE Filepath
  ::ATTRIBUTE ProcessStartInfo

  /* Method:      init
   * Description: Constructor which saves the passed arguments in the class and defines variable "Process" as .NET class "System.Diagnostics.Process"
   * Argument:   - Application: the application which will be started
   */
  ::METHOD init
    EXPOSE Application Process

    if arg(1,"Exists") then   -- if an argument was supplied, assign it to attribute "application"
       application=arg(1)

    Process = .clr~new("System.Diagnostics.Process") -- get access to public class System.Diagnostics.Process

  /* Method:      Start
   * Description: Starts the process based on already submitted information
   * Argument:   - ResetValues: calls class method "Reset" after starting the process
   */
  ::METHOD Start
    EXPOSE ProcessStartInfo Application Filepath Process ResetValues
    USE ARG ResetValues = .false

    IF var("ProcessStartInfo") THEN
      Process~clr.dispatch("Start", ProcessStartInfo) -- start process based on ProcessStartInfo
    ELSE IF var("Application") THEN
      Process~clr.dispatch("Start", Application, Filepath) -- start process based on Application and optionally Filepath
    ELSE IF var("Filepath") Then
      Process~clr.dispatch("Start", Filepath) -- start process based solely on Filepath (uses default application)
    ELSE
      RETURN "ERROR: No values specified"

    IF ResetValues <> .false THEN -- if the call to this method includes an argument, all values will be reset after launching the process
      self~Reset -- call to class method Reset

    RETURN .true

  /* Method:      Reset
   * Description: Resets all class attributes to their default values to launch a different process in the same class instance
   */
    ::METHOD Reset
      EXPOSE Application Filepath ProcessStartInfo
      DROP application filepath processStartInfo   -- drop the attributes

::REQUIRES CLR.CLS   -- get ooRexx.NET (CLR, common language runtime) support
