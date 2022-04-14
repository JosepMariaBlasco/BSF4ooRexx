"stopWatchFX"
=============

A JavaFX implementation of "ooRexx\samples\oodialog\examples\stopWatch.rex"
(Windows only) that runs on Windows, but also on Linux and MacOS.

Files
-----

        readme.txt       ... this file

        main.rex         ... main Rexx program which loads and runs stopWatchFX.fxml

        stopWatchFX.fxml ... GUI definition, defines event scripts for MenuItems and
                             Buttons, implemented in stopWatchFXController.rex which
                             needs to be invoked therefore such that its public routines
                             become accessible

        stopWatchFX.css  ... JavaFX CSS definition for formatting the elapsed time
                             Label with the id attribute set to "idElapsedTime"

---rgf, 2022-04-14

