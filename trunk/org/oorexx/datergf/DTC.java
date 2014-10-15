//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

package org.oorexx.datergf;

/** This interface serves as a container for constants used in the classes of
  * the datergf package in order to minimize the needed space of compiled classes.
  * It also allows for referring to these constants in a much shorter way by using
  * the string &quot;DTC&quot;: &quot;<strong>D</strong>ate/<strong>T</strong>ime
  * <strong>C</strong>onstants&quot;.
 *
 * <p>
  *
  * <p>This file contains <em>final</em> primitive datatype values (constants) only!
  *
  *
  * <p><hr>
  * <p>Examples:
  *
  * <pre>
  *       DateRGF d=new DateRGF().update();
  *
  *       int day=d.get(DTC.DAY_FIELD);         // retrieves the day field
  * </pre>
 *
 * <p>
 * <hr>
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2006 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -----------------------------------------------------------------------------
  *
  * Temporary dev-infos:
  *
  *    version  date            remark
  *
  *    0.9.2    2001-04-05      - created this interface
  *             2005-12-28      - added Apache license 2.0, put sources with BSF4Rexx
  * </pre>

  * <hr>
  * @author Rony G. Flatscher
  * @version 0.92,  date: 2001-02-08 through 2001-04-02, 2006-01-01
  */
public interface DTC
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";

    /* --------------------------------------------------------------------------- */
    /* ===>   ===>   ===> T I M E R G F  -  C O N S T A N T S <===    <===    <=== */
    /* --------------------------------------------------------------------------- */

    /**
      * Seconds in a full day (86400).
      */
    public final static int SECONDS_PER_DAY = 86400;

    /**
      * Seconds in a full hour (3600).
      */
    public final static int SECONDS_PER_HOUR = 3600;

    /**
      * Seconds in a full minute (60).
      */
    public final static int SECONDS_PER_MINUTE = 60;


    /**
      * Millis in a full day (86.400.000).
      */
    public final static int MILLIS_PER_DAY = 86400000;

    /**
      * Millis in a full hour (3.600.000).
      */
    public final static int MILLIS_PER_HOUR = 3600000;

    /**
      * Millis in a full minute (60.000).
      */
    public final static int MILLIS_PER_MINUTE = 60000;

    /**
      * Millis in a full second (1.000).
      */
    public final static int MILLIS_PER_SECOND = 1000;


    /*
      * Constants to be used in set() and get();
      */

    /**
      * Constant to refer to the
      * {@link TimeRGF#hour} field in
      * {@link TimeRGF#get(int flag)} and
      * {@link TimeRGF#set(int flag, int new_value)}.
      */
    public final static int HOUR_FIELD        = -100;

    /**
      * Constant to refer to the
      * {@link TimeRGF#minute} field in
      * {@link TimeRGF#get(int flag)} and
      * {@link TimeRGF#set(int flag, int new_value)}.
      */
    public final static int MINUTE_FIELD      = -101;

    /**
      * Constant to refer to the
      * {@link TimeRGF#second} field in
      * {@link TimeRGF#get(int flag)} and
      * {@link TimeRGF#set(int flag, int new_value)}.
      */
    public final static int SECOND_FIELD      = -102;


    /**
      * Constant to refer to the
      * {@link TimeRGF#millis} field in
      * {@link TimeRGF#get(int flag)} and
      * {@link TimeRGF#set(int flag, int new_value)}.
      */
    public final static int MILLIS_FIELD      = -103;


    /**
      * Constant to refer to the
      * {@link TimeRGF#raw_millis} field in
      * {@link TimeRGF#get(int flag)} and
      * {@link TimeRGF#set(int flag, int new_value)}.
      */
    public final static int RAW_MILLIS_FIELD = -110;


    /**
      * Constant to indicate that DateRGF is encoded as an integer in the
      * form of: <code>{@link DateRGF#year year}*10000+ {@link DateRGF#month month}*100+
                 {@link DateRGF#day day}</code>.

        <p>Or:<P

        Constant to indicate that TimeRGF is encoded as an integer in the
       form of: <code>{@link TimeRGF#hour}*10000+
                 {@link TimeRGF#minute}*100+
                 {@link TimeRGF#second}</code>.
      */
    public final static int ENCODED_AS_INTEGER = -1;


    /** Constant to indicate that the time is encoded as an integer
        representing the time as seconds relative to midnight.
      */
    public final static int ENCODED_AS_SECONDS = -3;


    /** Constant to indicate that the time is encoded as an integer
        representing the time as <em>millis</em>econds relative to midnight.
      */
    public final static int ENCODED_AS_MILLIS = -4;

    /* --------------------------------------------------------------------------- */
    /* ===>   ===>   ===> D A T E R G F  -  C O N S T A N T S <===    <===    <=== */
    /* --------------------------------------------------------------------------- */



        // refer to days

    /** Monday, has value 1 (ISO).
      */
    public final static int MONDAY      =     1 ;

    /** Tuesday, has value 2 (ISO).
      */
    public final static int TUESDAY     =     2 ;

    /** Wednesday, has value 3 (ISO).
      */
    public final static int WEDNESDAY   =     3 ;

    /** Thursday, has value 4 (ISO).
      */
    public final static int THURSDAY    =     4 ;

    /** Friday, has value 5 (ISO).
      */
    public final static int FRIDAY      =     5 ;

    /** Saturday, has value 6 (ISO).
      */
    public final static int SATURDAY    =     6 ;

    /** Sunday, has value 7 (ISO).
      */
    public final static int SUNDAY      =     7 ;



        // refer to months

    /** January, has value 1 (ISO).   */
    public final static int JANUARY     = 1;

    /** February, has value (ISO).    */
    public final static int FEBRUARY    = 2;

    /** March, has value 3 (ISO).     */
    public final static int MARCH       = 3;

    /** April, has value 4 (ISO).    */
    public final static int APRIL       = 4;

    /** May, has value 5 (ISO).       */
    public final static int MAY         = 5;

    /** June, has value 6 (ISO).      */
    public final static int JUNE        = 6;

    /** July, has value 7 (ISO).      */
    public final static int JULY        = 7;

    /** August, has value 8 (ISO).    */
    public final static int AUGUST      = 8;

    /** September, has value 9 (ISO). */
    public final static int SEPTEMBER   = 9;

    /** October, has value 10 (ISO).  */
    public final static int OCTOBER     = 10;

    /** November, has value 11 (ISO). */
    public final static int NOVEMBER    = 11;

    /** December, has value 12 (ISO). */
    public final static int DECEMBER    = 12;



        // refer to ordering of date fields
    /**
      * Constant to indicate the order Year/Month/Day.
      */
    public final static byte YMD = -1;

    /**
      * Constant to indicate the order Month/Day/Year.
      */
    public final static byte MDY = -2;

    /**
      * Constant to indicate the order Day/Month/Year.
      */
    public final static byte DMY = -3;

    /** Constant to refer to the
      * {@link DateRGF#dateOrder} field in
      * {@link DateRGF#getStatic(int flag)} and
      * {@link DateRGF#setStatic(int flag, int new_value)}.
      */
    public final static int DATE_ORDER=-5;

    /**
      * Constant to refer to the
      * {@link DateRGF#year DateRGF.year} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      */
    public final static int YEAR_FIELD        = -200;

    /**
      * Constant to refer to the
      * {@link DateRGF#year DateRGF.year} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      * This is an alias for {@link #YEAR_FIELD YEAR_FIELD}.
      */
    public final static int Y           = -200;

    /**
      * Flag to refer to the
      * {@link DateRGF#month DateRGF.month} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      */
    public final static int MONTH_FIELD       = -201;

    /**
      * Constant to refer to the
      * {@link DateRGF#month DateRGF.month} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      * This is an alias for {@link #MONTH_FIELD MONTH_FIELD}.
      */
    public final static int M           = -201;

    /**
      * Constant to refer to the
      * {@link DateRGF#day DateRGF.day} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      */
    public final static int DAY_FIELD         = -202;

    /**
      * Constant to refer to the
      * {@link DateRGF#day DateRGF.day} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      * This is an alias for {@link #DAY_FIELD DAY_FIELD}.
      */
    public final static int D           = -202;

    /** Constant to refer to the
      * {@link DateRGF#jdn} field in
      * {@link DateRGF#get(int flag)} and
      * {@link DateRGF#set(int flag, int new_value)}.
      *
      * <p>&quot;JDN&quot; stands for &quot;Julian day number&quot; and is a unique value
      * assigned to each day of a Julian or Gregorian date.
      */
    public final static int JDN         = -203;



    /**  Constant, epoch starts with 1858-11-17 and denotes the epoch of the
         &quot;modified julian day (MJD)&quot; sometimes used in calendar algorithms.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_MJD, 0)}.
    */
    public final static int EPOCH_MJD           =         -210;



    /** Constant, epoch starts with 1904-01-01.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_MACINTOSH, 0)}.
    */
    public final static int EPOCH_MACINTOSH    =         -211;

    /** Constant, epoch starts with 1904-01-01.
        Same as {@link #EPOCH_MACINTOSH EPOCH_MACINTOSH}.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_PALM, 0)}.
        */
    public final static int EPOCH_PALM          =         -211;

    /** Constant, epoch starts with 1970-01-01.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_JAVA, 0)}.
    */
    public final static int EPOCH_JAVA          =         -212;

    /** Constant, epoch starts with 1980-01-01.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_DOS, 0)}.
    */
    public final static int EPOCH_DOS           =         -213;

    /** Constant, epoch starts with 1601-01-01.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_WIN32, 0)}.
    */
    public final static int EPOCH_WIN32         =         -214;

    /** Constant, epoch defined by the user
      *({@link DateRGF#setDefaultEpochDate(int value)}.
      */

    public final static int EPOCH_USER          =         -215;

    /** Constant, to indicate to use the default epoch.
         To get a DateRGF object of it use {@link DateRGF#valueOf(int flag, int daysToAdd)
                                                  valueOf(#valueOf(int flag, int daysToAdd)
                                                  valueOf(DTC.EPOCH_DEFAULT, 0)}.
    */
    public final static int EPOCH_DEFAULT       =         -219;







    /** Constant 'day name'.
      */
    public final static int DN    = -280;

    /** Constant 'day of week' (DOW). Values 1 (Monday) through 7 (Sunday).
      */
    public final static int DOW   = -281;

    /** Constant 'day of week relative to day which begins the week
        ({@link DateRGF#weekStart}. If week starts with Sundays,
        then a Sunday is 1 ('first day of the week'), Monday is 2
        ('second day of the week') through
        Saturday, which is 7 ('seventh day of the week').
         */
    public final static int DOW_ORDINAL = -282;


    /** Constant 'ISO week number'.
      * The first ISO week of a year is the one which contains the first Thursday of January.
      * Therefore there may be years with 53 (!) weeks, e.g. 20041231 through 20050102.
      */
    public final static int WEEK  = -290;

    /** Constant 'ISO week number'.
      * This is an alias for {@link #WEEK WEEK}.
      */
    public final static int W     = -290;


    /** Constant 'week begin'. This will start with the day given in
           {@link DateRGF#weekStart}.
      */
    public final static int WB    = -291;

    /** Constant 'week end'.   This is relative to
           {@link DateRGF#weekStart}.
      */
    public final static int WE    = -292;

    /** Constant 'weekday' inidicating that the date is to be set to one
      * of the given weekdays using the {@link DateRGF#set(int flag, int number)}
      * method.
      */
    public final static int WEEKDAY = -293;



    /** Constant 'month begin'.
      */
    public final static int MB    = -300;

    /** Constant 'month end'.
      */
    public final static int ME    = -301;

    /** Constant 'month name'.
      */
    public final static int MN    = -302;

    /** Constant 'total of days in month'.
      */
    public final static int DAYS_IN_MONTH = -303;


    /** Constant 'quarter'.
      */
    public final static int Q     = -310;

    /** Constant 'quarter begin'.
      */
    public final static int QB    = -311;

    /** Constant 'quarter end'.
      */
    public final static int QE    = -312;


    /** Constant 'half year' (semester).
      */
    public final static int HY    = -320;

    /** Constant 'half year begin' (semester begin).
      */
    public final static int HYB   = -321;

    /** Constant 'half year end' (semester end).
      */
    public final static int HYE   = -322;

    /** Constant 'year begin'.
      */
    public final static int YB    = -331;

    /** Constant 'year end'.
      */
    public final static int YE    = -332;


    /** Constant to indicate the usage of the Gregorian calendar in
     *  the methods {@link DateRGF#jdn2date(int jdn, DateRGF aDate, int flag)}
     * and {@link DateRGF#date2jdn(int year, int month, int day, int flag)}.
     */
    public final static int GREGORIAN   = -335;

    /** Constant to indicate the usage of the Julian calendar in
      * the methods {@link DateRGF#jdn2date(int jdn, DateRGF aDate, int flag)}
      * and {@link DateRGF#date2jdn(int year, int month, int day, int flag)},
      * or to indicate that a 'Julian date' (in the form 'YYYYddd')
      * is desired in the method {@link DateRGF#get(int flag)}.
      */
    public final static int JULIAN      = -336;


    /**
      * Constant to indicate an impossible value for
      {@link DateRGF#jdn}, &quot;Not A Date&quot;
      */
    public final static int NAD         = -5000005;


    /**
     * Constant to refer to {@link DateRGF#weekStart}.
     */
    public final static int WEEK_START = -340;



}

