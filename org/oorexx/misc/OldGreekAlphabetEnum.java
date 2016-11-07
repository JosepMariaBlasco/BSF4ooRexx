/*
    Author:     Rony G. Flatscher
    Date:       2016-11-07
    Purpose:    Java class that defines the old Greek alphabet as a public java.lang.Enum;
                in order to access it CLASSPATH needs to point to the directory where
                this class is stored; one could add the "current directory" represented
                as a dot (.) to the CLASSPATH variable, if running programs needing this
                class off that directory

    Changed:    -

    Needs:      Java 1.5 or later

    License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2016 Rony G. Flatscher

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

package org.oorexx.misc;        // this way we are sure that the class can always be found by Rexx sample program

public enum OldGreekAlphabetEnum {
    ALPHA, BETA, GAMMA, DELTA, EPSILON,
    ZETA, ETA, THETA, IOTA, KAPPA, LAMBDA,
    MY, NY, XI, OMIKRON, PI, RHO, SIGMA,
    TAU, YPSILON, PHI, CHI, PSI, OMEGA
}
