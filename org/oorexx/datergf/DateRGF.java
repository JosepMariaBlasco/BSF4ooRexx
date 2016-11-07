//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

package org.oorexx.datergf; // "DateTime_RGF"-package

/**
  * {@link #DateRGF} is an implementation of Date which allows for date
  * manipulations and date arithmetics.
  *
  *
 * <p>
 * <p>
  *
  * The philosophy of this class follows the <a href="http://hobbes.nmsu.edu/cgi-bin/h-search?key=DateRGF">&quot;DateRGF.cmd&quot;</a>,
  * a date arithmetic function
  * written for the scripting language <a href="http://www.RexxLA.org">Rexx</a>,
  * which is available as commercial and (opensourced) free interpreters
    for practically every operating system in the world.

    <p>
    {@link #DateRGF} allows dates in the range of <kbd>0001-01-01</kbd>
    and <kbd>9999-12-31</kbd>. By default
    (can be changed with {@link #setGregorianChange(int year, int month, int day)})
    it switches from the Julian calendar to the Gregorian calendar in October of 1582 (October 4th of
    1582 is followed by October 15th of 1582). It uses the ISO-values for indicating
    the days and the ISO-rules to determine which week is the first week for a year.

    <p>
    Some of the available operations are:

    <ul>
    <li>Calculating the difference in days between two DateRGFs,

    <li>adding days to a DateRGF (using negative days one subtracts
        them from a DateRGF),

    <li>creating various DateRGF-values, like beginning (end) of a week
        (month, quarter, half-year = semester, year) a DateRGF falls into,
        optionally adding (subtracting) days from it,

    <li>getting information about DateRGFs like year, month, day,
        day-of-week, number of days in a month etc.

    </ul>

    <p><hr>
    <p>
    This Java-version was created for the
  * Waba
  * (e.g. <a href="http://www.superwaba.org">http://www.SuperWaba.org</a>)
  * family of Java-compatible mobile systems (PDAs, Handies, etc.) (e.g.:
  * not employing threading, exceptions, and the datatypes long and double).

  * <br>This class attempts to use as few resources as possible, yet
  * allow for comprehensive and fast date manipulations.


  <p>
  {@link #DateRGF DateRGF } is <strong>not</strong> modelled
  * after Java's classes Date, Calendar!
    <em>Therefore</em>, in order for getting fast to the point
    of this class, most method descriptions contain short examples.

    <p>
  * <hr>
  * <p>Examples:
  *
  * <pre>
  *      DateRGF firstDate=new DateRGF(   1, 1, 1), // yields: '0001-01-01'
  *              lastDate =new DateRGF(9999,12,31); // yields: '9999-12-31'
  *
  *      int     days=lastDate.subtract(firstDate); // yields: 3652060
  *
  *      DateRGF tmpDate=(DateRGF)firstDate.clone();// yields: '0001-01-01'
  *      tmpDate.add(days);                         // yields: '9999-12-31'
  *
  *                     // get Labor Monday in 2001
  *      tmpDate=new DateRGF(2001,8,31);            // yields: '2001-08-31', a Friday
  *      tmpDate.set(WEEKDAY, 1);                   // yields: '2001-09-03'
  *
  *                     // get Labor Monday in 2001 in <em>one</em> line
  *      tmpDate=(new DateRGF(2001,8,31)).set(WEEKDAY,1); // yields: '2001-08-31', a Friday
  *
  *      DateRGF d=valueOf(ENCODED_AS_INTEGER, 20190521);       // yields: '2019-05-21'
  *
  *      d.set(WB, 0);              // yields: '2019-05-19', if weekStart=7 (Sunday)
  *      int iso_day=d.get(DOW);    // yields: 7 (ISO-number for Sunday)
  *
  *      d.set(WB, 3);              // yields: '2019-05-22', if weekStart=7 (Sunday)
  *      d.set(WE, 0);              // yields: '2019-05-25', if weekStart=7 (Sunday)
  *
  *      DateRGF easter=DateRGF.easter(d);          // yields: '2019-04-21'
  *
  *      int palm_days=d.get(EPOCH_PALM);           // yields: 35430
  *      int java_days=d.get(EPOCH_JAVA);           // yields: 18041
  *
  *
  *           // if run on "2010-09-22 17:49:01" under Waba, then
  *      d.update();                // yields: '2010-09-22'
  *
  *      String day_name  =getString(DN, d.get(DOW));           // yields: 'Saturday'
  *      String month_name=getString(MN, d.get(MONTH_FIELD));   // yields: 'May'
  * </pre>

  *
 * <hr>
  *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2016 Rony G. Flatscher
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
  *    0.9.2    2001-03-20      - removed all deprecated stuff from the 0.9.1 version
  *                             - changed:
  *                                  is24hours          -> is24Hour
  *                                  date_delimiter     -> dateSeparator
  *                                  week_start         -> weekStart
  *                                  date_order         -> dateOrder
  *
  *             2001-03-21      - added Serializable (needs only one int to store the DateRGF)
  *                               taking care with supplied readObject(), writeObject()
  *
  *             2001-04-02      - introduced variant "JAVA" and "WABA"
  *             2001-04-05      - included update() here
  *
  *             2005-12-28      - added Apache license 2.0, put sources with BSF4Rexx
 *              2016-11-07      - added main method to give a brief overview
  * </pre>
  *
  * <hr>
  *
  * @author Rony G. Flatscher
  * @version 0.92,  date: 2001-02-08 through 2001-04-05, 2006-01-01
  *
  */

// rgf, 2016-11-07: this package could be overhauled by employing java.lang.Enum


public  class   DateRGF
        implements      java.lang.Cloneable,
                        java.lang.Comparable,
                        java.io.Serializable
{

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "100.20161107";




    /**
      * Stores the day which starts the week.
      * In Europe the week usually starts
      * with Monday, in the US with Sunday. Can be one of
      * {@link     DTC#MONDAY},
      * {@link     DTC#TUESDAY},
      * {@link     DTC#WEDNESDAY},
      * {@link     DTC#THURSDAY},
      * {@link     DTC#FRIDAY},
      * {@link     DTC#SATURDAY},
      * {@link     DTC#SUNDAY}.
      */
    private static int weekStart=DTC.MONDAY;

    /**
      * Stores the desired string to be used to delimit date fields in
      * {@link #toString()}.
      *
      * Will get set on class load from preferences.
      * The default value is: <code>'-'</code>.
      */
    public static char   dateSeparator= '-';   // changed to char, 2001-04-10

    /**
      * Stores the desired date ordering
      ( {@link DTC#YMD},
        {@link DTC#MDY},
        {@link DTC#DMY}).
      * Ordering will be used to delimit date fields in
      * {@link #toString()}.
      * Will get set on class load from preferences.
      * The default value is: {@link DTC#YMD}.
      */
    private static byte dateOrder = DTC.YMD;




    /**
      * Stores the names of the months in an array of strings.
      * Retrieval is possible via

      * {@link #getString(int flag, int value)}, where <code>flag</code>

        is set to {@link DTC#MN} and <code>value</code> is either set

        to the result of

        {@link #get(int flag)} with <code>flag</code> set to
        {@link DTC#M}, or to the {@link #month} of a

        <code>DateRGF</code> or one of the following
        constants:

        to the month of a <code>DateRGF</code> or one of the following constants:

      * {@link     DTC#JANUARY},
      * {@link     DTC#FEBRUARY},
      * {@link     DTC#MARCH},
      * {@link     DTC#APRIL},
      * {@link     DTC#MAY},
      * {@link     DTC#JUNE},
      * {@link     DTC#JULY},
      * {@link     DTC#AUGUST},
      * {@link     DTC#SEPTEMBER},
      * {@link     DTC#SEPTEMBER},
      * {@link     DTC#OCTOBER},
      * {@link     DTC#NOVEMBER},
      * {@link     DTC#DECEMBER}.
      *
      * One may change this string e.g. for localization.
      */
    static private String[] monthNames = {
                        "January", "February", "March",
                        "April",   "May",      "June",
                        "July",    "August",   "September",
                        "October", "November", "December"
                         };

    /**
      * Stores the names of the days in an array of strings.
      * One may change this string e.g. for localization.
      *
      * Retrieval is possible via

      * {@link #getString(int flag, int value)} with <code>flag</code>

        set to {@link DTC#DN} and <code>value</code> is either set
        to the result of

        {@link #get(int flag)} with <code>flag</code> set to

        {@link DTC#DOW}, or to the

        result of calling the static method
        {@link #dow(DateRGF tmpDate)} or
        one of the following constants:

      *
      *
      * {@link     DTC#MONDAY},
      * {@link     DTC#TUESDAY},
      * {@link     DTC#WEDNESDAY},
      * {@link     DTC#THURSDAY},
      * {@link     DTC#FRIDAY},
      * {@link     DTC#SATURDAY},
      * {@link     DTC#SUNDAY}.
      */
    static private String[] dayNames = {
                        "Monday",   "Tuesday", "Wednesday",
                        "Thursday", "Friday",  "Saturday",
                        "Sunday"
                         };




    /** Stores the DateRGF to serve as the default epoch date.
      * (Uses 1858-11-17, which is the 'native' epoch for the date calculations of this implementation.)
        The epoch DateRGF can be retrieved with {@link #getDefaultEpochDate()}
      */
    private static DateRGF defaultEpochDate=null;


    /** Stores one of the epoch flags:
    {@link DTC#EPOCH_MJD},
    {@link DTC#EPOCH_MACINTOSH},
    {@link DTC#EPOCH_PALM},
    {@link DTC#EPOCH_JAVA},
    {@link DTC#EPOCH_DOS},
    {@link DTC#EPOCH_WIN32},
    {@link DTC#EPOCH_USER}.
     */
    private static int defaultEpochFlag    = 0;

    /** Stores the DateRGFs of the epochs. Needed by {@link #gED(int flag)}.
      */
    private static final DateRGF [] epochDates= {null, null, null, null, null };


    /** <em>&quot;Julian day number&quot;</em> for the first date in the Gregorian calendar.
      * Defaults to the value representing Friday, '1582-10-15', i.e.: 2299161.
      */

    /** Stores the date of the first day in the Gregorian calender. Defaults to
      * '1582-10-15'. All dates smaller (before) this date are treated as Julian calendar
      * dates, all others as Gregorian calendar dates. This way it is possible to localize
      * the switch from Julian to Gregorian calendars. E.g. England and her
      * colonies (including most of the United States) started to use the Gregorian
      * calendar with Thursday, '1752-09-14', (the last Julian date in those countries
      * was '1752-09-02', a Wednesday).
      */
    private static DateRGF stGC = new DateRGF();


    /**
      * Stores the year, a value between 1 and 9999.

      * If changing this value directly, set {@link #jdn} to {@link DTC#NAD}
      * and call {@link #cD()} which checks the resulting DateRGF and sets
      * {@link #jdn} to its appropriate value.
      */
    protected transient int year;

    /**
      * Stores the month, a value between 1 and 12.

      * If changing this value directly, set {@link #jdn} to {@link DTC#NAD}
      * and call {@link #cD()} which checks the resulting DateRGF and sets
      * {@link #jdn} to its appropriate value.

      */
    protected transient int month;

    /**
      * Stores the day, a value between 1 and 28, 29, 30, 31, depending on the month and year.

      * If changing this value directly, set {@link #jdn} to {@link DTC#NAD}
      * and call {@link #cD()} which checks the resulting DateRGF and sets
      * {@link #jdn} to its appropriate value.
      */
    protected transient int day;



    /**
      * Stores the &quot;jdn&quot; (&quot;Julian day number&quot;) value. This
      * number <em>uniquely numbers</em> each day in the &quot;Julian period&quot; and
      * may be represented by different <em>calendar dates</em> depending on the calendar
      * one uses: the <em>Gregorian calendar</em> (used for civil purposes throughout
      * the world!) or the <em>Julian calendar</em> (used sometimes for religious purposes,
      * e.g. in the Orthodox Christian world for determining Christmas and Easter).
      *
      * <p>
      * This is a unique value
      * assigned to each day <em>irrespectible</em> of the calendar system one uses.
      * Cf. <a href="http://www.tondering.dk/claus/calendar.html"><em>&quot;Frequently
      * Asked Questions about Calendars&quot;</em></a> and read about 'Julian period',
      * 'Julian day (number)', 'Julian calendar' and 'Gregorian calendar'.
      *
      * <p>Use the {@link #get(int flag)}
      * method, to access this field, using the flag {@link DTC#JDN}.
      * (If this field has a value of {@link DTC#NAD}
      * then the actual {@link #jdn} value will get calculated and returned.)
      *
      */
    protected transient int jdn;



    /**
      * Creates a DateRGF object with all fields set to 1.
      */
    public DateRGF()
    {
       year =1;
       month=1;
       day  =1;
       jdn  =1721424;   // julian day number for the date "0001-01-01" in the Julian calendar
     }



    /**
      * Creates a DateRGF from the three integers, representing year, month
      * and day. The valid range is '0001-01-01' through '9999-12-31'.

      * <p>Please note: due to the underlying algorithms, it is <em>not</em>
      * an error to use '0' or a value larger than the maximum value
      * for {@link #month} or {@link #day}. In such a case the
      * date will be used which comes 'closest' to the indicated values. E.g.
      * '2004-03-00' will be translated to '2004-02-29', '2005-01-00' to '2004-12-31',
      * '2005-00-00' to '2004-11-30', '2005-12-40' to '2006-01-09',
      * '2005-13-40' to '2006-02-09', etc.
      *
      */
    public DateRGF(int year, int month, int day)
    {
        this.set(year, month, day);
     }

    /** Main method to give a few hints on the command line.
     *
     * @author Rony G. Flatscher
     * @since 2016-11-07
     */
    public static void main (String args[])
    {
        System.out.println(
             "The class \"org.oorexx.datergf.DateRGF\" is an implementation of Date which allows     \n" +
             "for date manipulations and date arithmetics.                                           \n" +
             "                                                                                       \n" +
             "The philosophy of this class follows the Rexx date arithmetic and manipulation         \n" +
             "package \"datergf.cmd|rex\" of the autho, (originally written on OS/2).                \n" +
             "                                                                                       \n" +
             "                                                                                       \n" +
             "\"org.oorexx.datergf.DateRGF\" allows dates in the range of \"0001-01-01\"             \n" +
             "and \"9999-12-31\". By default (can be changed with the method                         \n" +
             "\"setGregorianChange(int year, int month, int day)\") it switches from                 \n" +
             "the Julian calendar to the Gregorian calendar in October of 1582 (October 4th of       \n" +
             "1582 is followed by October 15th of 1582).                                             \n" +
             "                                                                                       \n" +
             "It uses the ISO-values for indicating the days and the ISO-rules to determine          \n" +
             "which week is the first week for a year.                                               \n" +
             "                                                                                       \n" +
             "Some of the available operations are:                                                  \n" +
             "                                                                                       \n" +
             "-   Calculating the difference in days between two DateRGFs,                           \n" +
             "                                                                                       \n" +
             "-   adding days to a DateRGF (using negative days one subtracts                        \n" +
             "    them from a DateRGF),                                                              \n" +
             "                                                                                       \n" +
             "-   creating various DateRGF-values, like beginning (end) of a week                    \n" +
             "    (month, quarter, half-year = semester, year) a DateRGF falls into,                 \n" +
             "    optionally adding (subtracting) days from it,                                      \n" +
             "                                                                                       \n" +
             "-   getting information about DateRGFs like year, month, day,                          \n" +
             "    day-of-week, number of days in a month etc.                                        \n" +
             "                                                                                       \n" +
             "This Java-version was originally created for the Waba family                           \n" +
             "(link used to be \"http://www.SuperWaba.org\")family of Java-compatible                \n" +
             "mobile systems (PDAs, Handies, etc.) (e.g.: not employing threading,                   \n" +
             "exceptions, and the datatypes long and double).                                        \n" +
             "                                                                                       \n" +
             "This class attempts to use as few resources as possible, yet                           \n" +
             "allow for comprehensive and fast date manipulations and can run                        \n" +
             "on any current Java platform.                                                          \n" +
             "                                                                                       \n" +
             "\"org.oorexx.datergf.DateRGF } is *not* modelled after Java's classes                  \n" +
             "Date or Calendar! Therefore, in order for getting fast to the point                    \n" +
             "of this class, most method descriptions contain short examples.                        \n" +
             "                                                                                       \n" +
             "Examples:                                                                              \n" +
             "                                                                                       \n" +
             "     DateRGF firstDate=new DateRGF(   1, 1, 1), // yields: '0001-01-01'                \n" +
             "             lastDate =new DateRGF(9999,12,31); // yields: '9999-12-31'                \n" +
             "                                                                                       \n" +
             "     int     days=lastDate.subtract(firstDate); // yields: 3652060                     \n" +
             "                                                                                       \n" +
             "     DateRGF tmpDate=(DateRGF)firstDate.clone();// yields: '0001-01-01'                \n" +
             "     tmpDate.add(days);                         // yields: '9999-12-31'                \n" +
             "                                                                                       \n" +
             "                    // get Labor Monday in 2001                                        \n" +
             "     tmpDate=new DateRGF(2001,8,31);            // yields: '2001-08-31', a Friday      \n" +
             "     tmpDate.set(WEEKDAY, 1);                   // yields: '2001-09-03'                \n" +
             "                                                                                       \n" +
             "                    // get Labor Monday in 2001 in <em>one</em> line                   \n" +
             "     tmpDate=(new DateRGF(2001,8,31)).set(WEEKDAY,1); // yields: '2001-08-31', a Friday\n" +
             "                                                                                       \n" +
             "     DateRGF d=valueOf(ENCODED_AS_INTEGER, 20190521);       // yields: '2019-05-21'    \n" +
             "                                                                                       \n" +
             "     d.set(WB, 0);              // yields: '2019-05-19', if weekStart=7 (Sunday)       \n" +
             "     int iso_day=d.get(DOW);    // yields: 7 (ISO-number for Sunday)                   \n" +
             "                                                                                       \n" +
             "     d.set(WB, 3);              // yields: '2019-05-22', if weekStart=7 (Sunday)       \n" +
             "     d.set(WE, 0);              // yields: '2019-05-25', if weekStart=7 (Sunday)       \n" +
             "                                                                                       \n" +
             "     DateRGF easter=DateRGF.easter(d);          // yields: '2019-04-21'                \n" +
             "                                                                                       \n" +
             "     int palm_days=d.get(EPOCH_PALM);           // yields: 35430                       \n" +
             "     int java_days=d.get(EPOCH_JAVA);           // yields: 18041                       \n" +
             "                                                                                       \n" +
             "                                                                                       \n" +
             "          // if run on \"2010-09-22 17:49:01\",  then                                  \n" +
             "     d.update();                // yields: '2010-09-22'                                \n" +
             "                                                                                       \n" +
             "     String day_name  =getString(DN, d.get(DOW));           // yields: 'Saturday'      \n" +
             "     String month_name=getString(MN, d.get(MONTH_FIELD));   // yields: 'May'           \n" +
             "                                                                                       \n" +
             "                                                                                       \n" +
             "See the Javadoc-documentation in the BSF4ooRexx \"information\" folder (menu           \n" +
             "\"BSF4ooRexx -> Information\") for the full documentation of this package consisting   \n" +
             "of the classes \"org.oorexx.datergf.DateRGF\", \"org.oorexx.datergf.TimeRGF\",         \n" +
             "\"org.oorexx.datergf.DateTimeRGF\", \"org.oorexx.datergf.DateFormatRGF\", and          \n" +
             "\"org.oorexx.datergf.DTC\" (date time constants).                                      \n" +
             "                                                                                       \n" +
             "It is very easy to take advantage of this class from Rexx. Just check out the          \n" +
             "Rexx samples \"samples/1-050_DateRgf.rxj\" and \"samples/1-060_DateRgfSample.rxj\"     \n" +
             "in the BSF4ooRexx \"samples\" folder (menu \"BSF4ooRexx -> Samples\") as of 2016-11-07.\n"
            );

    }




    /** Creates a DateRGF object by adding days to the given DateRGF (which
      * may be an epoch date indicated by the appropriate constant).
      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF e=DateRGF.valueOf(DateRGF.EPOCH_PALM, 0),         // yields: '1904-01-01'
      *              m=new DateRGF(2001, 1, 1);                        // yields: '2001-01-01', the
      *                                                                // new millenium
      *
      *      int palm_days=m.subtract(e);                              // yields: 35430
      *
      *      DateRGF d=DateRGF.valueOf(DateRGF.EPOCH_PALM, palm_days); // yields: '2001-01-01'
      *
      *      e.add(palm_days);                                         // yields: '2001-01-01'
      *
      * </pre>
      *
      *
      *  <p>
      *  @param flag one of the constants of epoch
      * ({@link DTC#EPOCH_MACINTOSH},
      *  {@link DTC#EPOCH_PALM},
      *  {@link DTC#EPOCH_JAVA},
      *  {@link DTC#EPOCH_DOS},
      *  {@link DTC#EPOCH_WIN32},
      *  {@link DTC#EPOCH_MJD},
      *  {@link DTC#EPOCH_DEFAULT}),
      *
      * or an explicit date serving as an epoch, formed according to the
      * {@link DTC#ENCODED_AS_INTEGER} rule.
      *
      * @param daysToAdd days to add/subtract to/from epoch or integer encoded date,
      * indicated by the second parameteras.
      * If the second parameter <code>flag</code> is {@link DTC#ENCODED_AS_INTEGER}
      * than this parameter is interpreted as a date encoded as an integer.
      *
      * @return a DateRGF object.
      *
      */
    public static DateRGF valueOf(int flag, int daysToAdd)
    {
        DateRGF newDate=null;

        if (flag>0 || flag==DTC.ENCODED_AS_INTEGER)  // flag is a date ENCODED_AS_INTEGER
        {
            if (flag==DTC.ENCODED_AS_INTEGER)
            {
                flag=daysToAdd;
                daysToAdd=0;
             }

            newDate         = new DateRGF();
            newDate.year    = flag/10000;
            int tmp         = flag%10000;
            newDate.month   = tmp    /100;
            newDate.day     = tmp    %100;
            newDate.jdn=DTC.NAD;
            newDate.cD();
         }

        else if (flag==defaultEpochFlag) {newDate=getDefaultEpochDate(); }

        else newDate=(DateRGF)gED(flag).clone();

        if (daysToAdd!=0) newDate.add(daysToAdd);
        return newDate;
     }


        // the compiler will substitute all references to these fields

    /** Create a DateRGF object from a string containing a DateRGF
      * encoded according to the present setting of
      * {@link #dateOrder}.
      *
      * <p>
      * Extraction of digits is <em>very</em>
      * lenient, i.e. everything but a digit is skipped, all digits (up to eight)
      * are concatenated and then turned into a DateRGF date.

      * <p>If the length of the extracted string of digits is six characters,
      * then it is assumed that the year is two digits long and '2000' is
      * added to the year.
      *
      * <p>
      * If the number of digits is not
      * exactly eight (the year consists of four digits) or not
      * exactly six (the year consists of two digits), <code>null</code>
      * is returned. Therefore the day and the month must be given with
      * two digits (i.e. leading 0, if necessary).
      *
      * <p>Examples (assuming
      * {@link #dateOrder}: {@link DTC#YMD}):
      *
      * <pre>
      *      DateRGF a=valueOf("1989-05-21") ,  // yields: '1989-05-21'
      *              b=valueOf("19930922"  ) ,  // yields: '1993-09-22'
      *              c=valueOf("1782.10.15") ,  // yields: '1782-10-15'
      *              d=valueOf("2010 02 28") ,  // yields: '2010-02-28'
      *              e=valueOf("030201"    ) ,  // yields: '2003-02-01'
      *              f=valueOf("123"       ) ;  // yields: null
      * </pre>
      *
      * @param value a string containing a string representation of DateRGF.
      *
      * @return the appropriate DateRGF object, or <code>null</code>, if
      *         there are not exactly eight or six digits in the parsed
      *         (after removing delimiters) string.
      */
    public static DateRGF valueOf(String value)
    {
        int    i, i1, i2, i3, k;
        char   tmp;
        String res="";
        DateRGF tmpD=new DateRGF();

        for (i=0; i<value.length() && res.length()<8; i++)
        {
           tmp=value.charAt(i);
           if (tmp>=(char)'0' && tmp<=(char)'9') res=res+tmp;

//                // end of string?
//           if (tmp==(char)' ' && dateSeparator=="") break;
         }

        if (res.length()!=6 && res.length()!=8) return null;

// <!-- JAVA versions - B E G I N -  -->
        k=Integer.parseInt(res);
// <!-- JAVA versions - E N D     -  -->



        if (res.length()==6)    // year of two digits only?
        {
           i1=k/10000;
           k =k%10000;
           i2=k/  100;
           i3=k%  100;
           switch (dateOrder)
           {
               case DTC.DMY:
                         return tmpD.set(i3+2000, i2, i1);

               case DTC.MDY:
                         return tmpD.set(i3+2000, i1, i2);

               case DTC.YMD:
               default:
                         return tmpD.set(i1+2000, i2, i3);
            }
         }

        switch (dateOrder)
        {
           case DTC.DMY: i3=k/1000000;      // day
                     k =k%1000000;
                     i2=k/  10000;      // month
                     i1=k%  10000;      // year
                     break;

           case DTC.MDY: i2=k/1000000;      // month
                     k =k%1000000;
                     i3=k/  10000;      // day
                     i1=k%  10000;      // year
                     break;

           case DTC.YMD:
           default : return valueOf(DTC.ENCODED_AS_INTEGER, k);
         }
        return tmpD.set(i1, i2, i3);
     }



    /** Adds the number of days to this DateRGF. If that number is negative,
      * the DateRGF gets the number of days subtracted.
      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF newMillenium=new DateRGF(2001,01,01); // yields: '2001-01-01'
      *      newMillenium.add(100000);   // add 100.000 days, yields: '2274-10-17'
      * </pre>
      *
      *
      @param numOfDays number of days to add to this <code>DateRGF</code>.
      *
      * @return the DateRGF object (set to the new values).
      */
    public DateRGF add(int numOfDays)
    {
        if (numOfDays==0) return this;
        return jdn2date(jdn+numOfDays, this);
     }


    /**
      * Calculates the differences (in number of days) between two DateRGFs.
      * This subtracts <kbd>otherDateRGF</kbd> from this DateRGF.
      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF firstDate=new DateRGF(   1, 1, 1), // yields: '0001-01-01'
      *              lastDate =new DateRGF(9999,12,31); // yields: '9999-12-31'
      *
      *      int     days=lastDate.subtract(firstDate); // yields: 3652060
      * </pre>
      *
      *
      * @param other DateRGF object to subtract from this DateRGF.
      *
      * @return number of days between this DateRGF and <kbd>otherDateRGF</kbd>.
      *
      */
    public int subtract(DateRGF other)
    {
        return this.get(DTC.JDN)-other.get(DTC.JDN);
     }



     /** Assigns all DateRGF fields of <kbd>otherDateRGF</kbd> to this <kbd>DateRGF</kbd>.
       * After this operation <kbd>this.equals(otherDateRGF)</kbd> will be true.
       *
       * @param otherDateRGF date to assign from.
       *
      * @return the DateRGF object (set to the new values).
       */
     public DateRGF assign(DateRGF otherDateRGF)
     {
         this.year =otherDateRGF.year;
         this.month=otherDateRGF.month;
         this.day  =otherDateRGF.day;
         this.jdn  =otherDateRGF.jdn;
         return this;
      }




    /**
      * Checks the date. The DateRGF may get adjusted to the 'closest' date, if
        the values of one of the DateRGF fields {@link #month} or
        {@link #day} are beyond their appropriate limits.
        (E.g. '2005-12-32' will be turned into '2006-01-01', or '2717-01-00'
        into '2716-12-31'!)

        <p>This method is executed only, if {@link #jdn} is set to {@link DTC#NAD}.
        At the end of the method {@link #jdn} will be set to the value fully
        representing the given DateRGF.
      */
    protected void cD()
    {
       if (jdn!=DTC.NAD) return;    // if mjd is set, a check has been done already

       if (year<0)      {year = -year; }
       if (month<0)     {month= -month; }
       if (day<0)       {day  = -day; }

       jdn2date(this.get(DTC.JDN), this);        // use get(), this makes sure mjd is set
     }



    /**
      * Implements the &quot;Comparable&quot; interface.
      * Returns <kbd>-1</kbd>, if this time is earlier (smaller) than the argument,
      * returns  <kbd>0</kbd>, if both are equal,
      * returns <kbd>+1</kbd>, if this time is later (greater) than the argument.
      */
    public int compareTo( Object otherDateRGF)
    {
        int other_jdn=((DateRGF)otherDateRGF).get(DTC.JDN), // using get() makes sure jdn is set
            this_jdn =this.get(DTC.JDN);                    // using get() makes sure jdn is set

        if (this_jdn < other_jdn) {return -1; }
        if (this_jdn== other_jdn) {return  0; }
        return +1;
     }



    /**
      * Implements the  &quot;Clonable&quot; interface.
      * Returns a newly created DateRGF object with all fields set to this DateRGF object.
      */
    public Object clone()
    {
       DateRGF tmp=new DateRGF();
       tmp.assign(this);
       return tmp;
     }




    /**
      * Returns true if DateRGFs are equal (both have the same value in
      * the fields
      * {@link #year}
      * {@link #month}
      * {@link #day}).
      */
    public boolean equals(DateRGF otherDateRGF)
    {
        return ( (this.year ==otherDateRGF.year) &&
                 (this.month==otherDateRGF.month) &&
                 (this.day  ==otherDateRGF.day)
               );
     }


    /**
      * Allows to retrieve information about this DateRGF.
      *
      <p>
      <table border="1" cellpadding="3" cellspacing="0">
      <tr class="TableHeadingColor">
      <td> flag
      <td> returns

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#YEAR_FIELD}
      <td>      {@link #year}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#MONTH_FIELD}
      <td>      {@link #month}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DAY_FIELD}
      <td>      {@link #day}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEK_START}
      <td>      {@link DTC#WEEK_START}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#JDN}
      <td>      {@link #jdn}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DOW}
      <td>      day of week (according to ISO)

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DOW_ORDINAL}
      <td>      the number of day in the week, relative to
                {@link #weekStart}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#JULIAN}
      <td>      Julian date, e.g. the date 19590202 (February, 2nd) will
                return the Julian date 1959033 (33rd day in the year)

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEK}
      <td>      the week number according to ISO

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DAYS_IN_MONTH}
      <td>      number of days in this date's month

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#HY}
      <td>      <kbd>1</kbd>, if date is in first half (semester) of the year,
                <kbd>2</kbd> else.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#Q}
      <td>      the quarter of the date (<kbd>1</kbd> through <kbd>4</kbd>).

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#EPOCH_MACINTOSH},
                {@link DTC#EPOCH_PALM},
                {@link DTC#EPOCH_JAVA},
                {@link DTC#EPOCH_DOS},
                {@link DTC#EPOCH_WIN32},
                {@link DTC#EPOCH_MJD},
                {@link DTC#EPOCH_DEFAULT}
      <td>      the number of days between this DateRGF and the given epoch.
                If using <code>EPOCH_DEFAULT</code>, then the default
                epoch is used. The default epoch may be set with
                {@link #setDefaultEpochDate(int value)}
                and queried/gotten with
                {@link #getDefaultEpochDate()}.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#ENCODED_AS_INTEGER}
      <td>      returns this DateRGF encoded as an integer:
                <code>year*10000+month*100+day</code>.


      </table>
      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF d=new DateRGF(2059, 5, 20);            // yields: '2059-05-20'
      *      int     this_date  =d.get(DTC.ENCODED_AS_INTEGER), // yields: 20590520
      *              quarter    =d.get(DTC.Q),                  // yields: 2
      *              semester   =d.get(DTC.HY),                 // yields: 1
      *              dow        =d.get(DTC.DOW),                // yields: 2
      *              ws         =d.get(DTC.WEEK_START),         // yields: 7
      *              dow_ord    =d.get(DTC.DOW_ORDINAL),        // yields: 3
      *              dim        =d.get(DTC.DAYS_IN_MONTH),      // yields: 31
      *              palm_days  =d.get(DTC.EPOCH_PALM),         // yields: 56753
      *              java_days  =d.get(DTC.EPOCH_JAVA);         // yields: 32646
      * </pre>
      *
      *
      * @param flag indicates the desired value.
      * @return the value according to the given <code>flag, -1</code>,
      *         if unknown flag is used.
      */
    public int get(int flag)
    {
       switch (flag)
       {
           case DTC.JDN:            if (jdn==DTC.NAD) {jdn=date2jdn(this); } return jdn;

           case DTC.DOW:            return dow(this);

           case DTC.DOW_ORDINAL:    return dow_ord(this);

           case DTC.YEAR_FIELD:     return year;

           case DTC.MONTH_FIELD:    return month;

           case DTC.DAY_FIELD:      return day;

/* this is actually a *static* field ! */
           case DTC.WEEK_START :    return weekStart;

           case DTC.JULIAN:         return toJulianDate(this);

           case DTC.WEEK:           return isoWeek(this);

           case DTC.DAYS_IN_MONTH:  return daysInMonth(this);

           case DTC.HY:             return (this.month<7?1:2);

           case DTC.Q:              return (this.month<4?1: (this.month<7?2: (this.month<10?3:4) ) );

           case DTC.EPOCH_PALM:
           case DTC.EPOCH_JAVA:
           case DTC.EPOCH_DOS:
           case DTC.EPOCH_WIN32:
           case DTC.EPOCH_MJD:
           case DTC.EPOCH_DEFAULT:
                                return this.subtract(gED(flag));


           case DTC.ENCODED_AS_INTEGER:
                                return year*10000+month*100+day;

           default:             return -1;
         }
     }





    /**
      * Allows to set this DateRGF object to a specific date.
      *
      <p>
      <table border="1" cellpadding="3" cellspacing="0">
      <tr class="TableHeadingColor">
      <td> flag
      <td> comment

      <tr>
      <td colspan="2">The following flags use the parameter <code>number</code>
                for setting the desired values.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#YEAR_FIELD}
      <td>      set {@link #year} to the value of parameter <kbd>number</kbd>.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#MONTH_FIELD}
      <td>      set {@link #month} to the value of parameter <kbd>number</kbd>.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DAY_FIELD}
      <td>      set {@link #day} to the value of parameter <kbd>number</kbd>.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#ENCODED_AS_INTEGER}
      <td>      set {@link #year}, {@link #month} and
                {@link #day} to the Date value encoded as an
                <code>int</code> in
                parameter <kbd>number</kbd>.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEKDAY}
      <td>      set date to one of the weekdays encoded in parameter
                parameter <kbd>number</kbd>. This uses the
                {@link #setGivenWeekday(int weekdays, DateRGF date)} to carry
                 out the operation.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEK_START}
      <td>      set {@link #weekStart  } to the day as indicated by the
                parameter <kbd>number</kbd>. If an invalid value is passed, the
                present setting is not changed.
      </table>

      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF d=DateRGF.valueOf(20010101, 0);    // yields: '2001-01-01'
      *
      *      d.set(YEAR_FIELD, 2345);             // yields: '2345-01-01'
      *      d.set(MONTH_FIELD, 12);              // yields: '2345-12-01'
      *      d.set(DAY_FIELD, 15);                // yields: '2345-12-15', a Saturday
      *
      *      d.set(WEEKDAY, 1);                   // yields: '2345-12-17', the next Monday
      *
      *      d.set(ENCODED_AS_INTEGER, 17280228); // yields: '1728-02-28'
      * </pre>
      *


      <p>
      <table border="1" cellpadding="3" cellspacing="0">

      <tr class="TableHeadingColor">
      <td> flag
      <td> comment

      <tr>
      <td colspan="2">The following flags cause the DateRGF object to be firstly
                set to the desired date, then <code>number</code> of days are
                added to (subtracted from, if negative) to it.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#HYB}
      <td>      set the date to the beginning of its semester and
                add the value of parameter <kbd>number</kbd> to it.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#HYE}
      <td>      set the date to the end of its semester and
                add the value of parameter <kbd>number</kbd> to it.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#MB}
      <td>      set the date to the beginning of its month and
                add the value of parameter <kbd>number</kbd> to it.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#ME}
      <td>      set the date to the end of its month and
                add the value of parameter <kbd>number</kbd> to it.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#QB}
      <td>      set the date to the beginning of its quarter and
                add the value of parameter <kbd>number</kbd> to it.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#QE}
      <td>      set the date to the end of its quarter and
                add the value of parameter <kbd>number</kbd> to it.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WB}
      <td>      set the date to the beginning of its week and
                add the value of parameter <kbd>number</kbd> to it.
                The beginning of a week is determined by
                {@link #weekStart}.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WE}
      <td>      set the date to the end of its week and
                add the value of parameter <kbd>number</kbd> to it.
                The end of a week is always six days after the day of
                {@link #weekStart}.


      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#YB}
      <td>      set the date to the beginning of its year and
                add the value of parameter <kbd>number</kbd> to it.

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#YE}
      <td>      set the date to the end of its year and
                add the value of parameter <kbd>number</kbd> to it.


      <tr class="TableRowColor"><!-- table row -->
      <td>
      <td>

      </table>

      *
      * <p>Examples:
      *
      * <pre>
      *      d.set(DTC.ENCODED_AS_INTEGER, 17280128);  // yields: '1728-02-28'
      *      d.set(DTC.WB, 0);              // yields: '1728-02-22', if weekStart=7
      *      d.set(DTC.WB, 3);              // yields: '1728-02-25', if weekStart=7
      *      d.set(DTC.WE, 0);              // yields: '1728-02-28', if weekStart=7
      *
      *      d.set(DTC.QB, 0);              // yields: '1728-01-01'
      *      d.set(DTC.QE, 0);              // yields: '1728-03-31'
      *      d.set(DTC.QE, -21);            // yields: '1728-03-10
      *                                 // i.e. three weeks before the end of the quarter
      * </pre>
      *
      *
      * @param flag indicates type of set operation
      * @param number either a value for a DateRGF-field
               ({@link #year},
                {@link #month},
                {@link #day})
               or may be a number indicating how many days to add/subtract
               from the desired date.
      *
      * @return this DateRGF object (set to the new values).
      */
    public DateRGF set(int flag, int number)
    {
       switch (flag)
       {
           case DTC.YEAR_FIELD:
           case DTC.MONTH_FIELD:
           case DTC.DAY_FIELD:
           case DTC.ENCODED_AS_INTEGER:
                if (number<0) {number=-number; }

               switch(flag)
               {
                   case DTC.YEAR_FIELD:   year    = number; break;
                   case DTC.MONTH_FIELD:  month   = number; break;
                   case DTC.DAY_FIELD:    day     = number; break;
                   case DTC.ENCODED_AS_INTEGER:
                                      year    = number/10000;
                                      int tmp = number%10000;
                                      month   = tmp   /100;
                                      day     = tmp   %100;
                                      break;

                }
               jdn=DTC.NAD;
               cD();
               return this;

/* this is actually a *static* field ! */
           case DTC.WEEK_START :          if (number>0 && number<8) weekStart=number;
                                      return this;

           case DTC.WEEKDAY:
                        return setGivenWeekday(number, this);


           case DTC.WB:
           case DTC.WE:
           case DTC.MB:
           case DTC.ME:
           case DTC.HYB:
           case DTC.HYE:
           case DTC.QB:
           case DTC.QE:
           case DTC.YB:
           case DTC.YE:

               DateRGF tmpDate=null;
               int     tmp;
               switch (flag)
               {
                  case DTC.WB:
                  case DTC.WE:
                       tmp=weekStart-dow(this);
                       if (tmp>0) tmp-=7;       // go back, hence deduct
                       number+=tmp;
                       if (flag==DTC.WE) {number+=6; }  /* add six days to get to end of week   */

                       if (number!=0) this.add(number);
                       return this;

                  case DTC.MB:
                       tmpDate = new DateRGF(year, month, 1);
                       break;

                  case DTC.HYB:
                       tmpDate = new DateRGF(year, this.get(DTC.HY)==1?1:7, 1);
                       break;

                  case DTC.HYE:
                       tmpDate = new DateRGF(year, this.get(DTC.HY)==1?6:12, 1);
                       break;

                  case DTC.QB:
                       tmp = this.get(DTC.Q);
                       tmpDate = new DateRGF(year, (tmp==1?1:(tmp==2?4:(tmp==3?7:10))), 1);
                       break;

                  case DTC.QE:
                       tmp = this.get(DTC.Q);
                       tmpDate = new DateRGF(year, (tmp==1?3:(tmp==2?6:(tmp==3?9:12))), 1);
                       break;

                  case DTC.YB:
                       tmpDate = new DateRGF(year, 1, 1);
                       break;

                  case DTC.YE:
                       tmpDate = new DateRGF(year, 12, 31);
                       break;

                  default: break;
                }

               /* set last day of month for the following end dates    */
               switch(flag)
               {
                  case DTC.ME:
                  case DTC.HYE:
                  case DTC.QE:
                           tmpDate.day=tmpDate.get(DTC.DAYS_IN_MONTH);
                  default: break;
                }

               tmpDate.jdn=DTC.NAD;
               tmpDate.cD();
               if (number != 0)
                  tmpDate.add(number);

               this.assign(tmpDate);
               return this;


           default:     break;
        }

       return this;
     }




    /** Allows to query the static fields
      * {@link #weekStart} and {@link #dateOrder}.
      *
      <p>
      <table border="1" cellpadding="3" cellspacing="0">
      <tr class="TableHeadingColor">
      <td> flag
      <td> returns

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DATE_ORDER}
      <td>      {@link #dateOrder}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEK_START}
      <td>      {@link #weekStart}

      </table>
      *
      *
      * @param flag indicates the desired value.
      * @return the value according to the given <code>flag, -1</code>,
      *         if unknown flag is used.
      */
    public static int getStatic(int flag)
    {
       switch (flag)
       {
           case DTC.WEEK_START :    return weekStart;

           case DTC.DATE_ORDER :    return dateOrder;

           default:             return -1;
         }
     }



    /** Allows to set the static fields
      * {@link #weekStart} and {@link #dateOrder}.
      *
      <p>
      <table border="1" cellpadding="3" cellspacing="0">
      <tr class="TableHeadingColor">
      <td> flag
      <td> sets and returns

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#DATE_ORDER}
      <td>      {@link #dateOrder}

      <tr class="TableRowColor"><!-- table row -->
      <td>      {@link DTC#WEEK_START}
      <td>      {@link #weekStart}

      </table>
      *
      *
      * @param flag indicates the desired value.
      * @return the value according to the given <code>flag, -1</code>,
      *         if unknown flag is used. Illegal values are ignored, instead
      *         the present set value for the indicated field is returned.
      */
    public static int setStatic(int flag, int number)
    {
       switch (flag)
       {
           case DTC.WEEK_START :    if (number<0 || number>7)       // beyond limits, don't change,
                                   return weekStart;            // but return
                                weekStart=number;
                                return number;

           case DTC.DATE_ORDER :
                                switch (number)                 // change only, if valid
                                {
                                   case DTC.YMD: dateOrder=DTC.YMD; break;
                                   case DTC.DMY: dateOrder=DTC.DMY; break;
                                   case DTC.MDY: dateOrder=DTC.MDY; break;
                                 }
                                return dateOrder;

           default:             return -1;
         }
     }



    /** Set all three DateRGF fields at once.
      *
      * @param year  the DateRGF field {@link #year}.
      * @param month the DateRGF field {@link #month}.
      * @param day   the DateRGF field {@link #day}.
      *
      * @return this DateRGF object (set to the new values).
      */
    public DateRGF set(int year, int month, int day)
    {
                // just make sure to use positive numbers
       if (year<0)      {this.year  = -year; }
       else             {this.year  = year; }

       if (month<0)     {this.month = -month; }
       else             {this.month = month; }

       if (day<0)       {this.day   = -day; }
       else             {this.day   = day; }

       this.jdn=date2jdn(this);         // calculate and assign appropriate Julian day number
       jdn2date(this.jdn, this);        // adjust year, month, day, if necessary

       return this;
     }



    /**
      * Returns time formatted into a string according to the fields
      * {@link #dateSeparator},
      * {@link #dateOrder}.
      */
    public String toString()
    {
       switch (dateOrder)
       {
           case DTC.DMY: return ( ri(day, 2)  +dateSeparator
                             +ri(month,2) +dateSeparator
                             +ri(year,4)
                             );

           case DTC.MDY: return ( ri(month,2) +dateSeparator
                             +ri(day,2)   +dateSeparator
                             +ri(year,4)
                             );

           case DTC.YMD:
           default:  return (ri(year,4)  +dateSeparator
                            +ri(month,2) +dateSeparator
                            +ri(day,2)
                            );
        }
     }


    /**
      * Convert the primitive Java int <kbd>tmp</kbd> to a String and right
      * adjust value with a leading
      * <kbd>0</kbd> to two places, if necessary.
      */
    protected static String ri(int value, int len)
    {
        String zeros   ="000000";
        String strValue=""+value;
        len=len-strValue.length();
        if (len>0) return zeros.substring(0, len)+strValue;
        return strValue;
     }




   /**
     * Calculates the day of week (DOW).
     *
     * @param tmpDate DateRGF to work on.
     *
     * @return a value between 1 (Monday as per ISO) and 7 (Sunday as per ISO).
     */
   public static int dow(DateRGF tmpDate)
   {
       return (tmpDate.get(DTC.JDN)%7+1);
    }


   /**
     * Calculates the ordinal value of the day of week (DOW), which
     * is relative to {@link #weekStart}. Hence, if weeks
     * start on Sundays, then Mondays will be the second day relative
     * to the start of week. If weeks start on Monday, then Monday
     * will be the first day of the week.
     *
     * @param tmpDate DateRGF to work on.
     *
     * @return a value between 1 and 7, indicating the ordinal value
     *         relative to the beginning of the week as defined in
     *         {@link #weekStart}.
     *
     */
   public static int dow_ord(DateRGF tmpDate)
   {
       int aha=dow(tmpDate)-weekStart+1;
       if (aha < 1) aha+=7;
       return aha;
    }


   /** From a given DateRGF calculate the previous or next date, which falls on
     * one of the given weekdays. This algorithm moves the date between one and
     * seven days depending on the argument <code>weekdays</code>.
     *
     * <p>Each decimal character represents a weekday, starting
     * with {@link DTC#MONDAY} (=1) and ending with
     * {@link DTC#SUNDAY} (=7). One needs to encode them into an integer, e.g.
     * '12345' represents the next weekdays from Monday to Friday,
     * whereas '67' represents the next weekdays Saturday and Sunday. Therefore to find
     * the next Monday or Friday, use '15'. If seeking
     * a previous date being one of the indicated weekdays, use negative numbers
     * (e.g. '-12345', '-67', '-15' etc.).
     *
     * <p>Examples:
     *
     * <pre>
     *                  // get Labor Day of 2001 (first Monday in September)
     *      DateRGF d=new DateRGF(2001, 8, 31); // yields: '2001-08-31'
     *      setGivenWeekday(1, d);              // yields: '2001-09-03'
     *
     *                  // get the date the summer time ends in 2006 (last Sunday in October)
     *      d=new DateRGF(2006, 11, 01);        // yields: '2006-11-01'
     *      setGivenWeekday(-7, d);             // yields: '2006-10-29'
     *
     *                  // get last working day of this quarter
     *      d.set(DTC.QE, 1);                   // yields: '2007-01-01', first day of next quarter
     *      setGivenWeekday(-12345, d);         // yields: '2006-12-29', a Friday
     * </pre>
     *
     *
     * @param weekdays an integer number indicating which days to match with.
     *                 If this number is negative, then the closest <em>previous</em>
     *                 weekday is searched, else the closest next one.
     *
     * @param date to be used.
     *
     * @return DateRGF set to the given weekday.
     */
     public static DateRGF setGivenWeekday(int weekdays, DateRGF date)
     {
        boolean goUp=true;
        String  sdow=""+date.get(DTC.DOW);
        char    cdow=sdow.charAt(0);

        int     i, j=0, k=0,
                min=7;                  // default to go back/forth an entire week

        if (weekdays<0)                 // go back?
        {
           weekdays=-weekdays;
           goUp    = false;
         }
        String wd =""+weekdays;         // convert to String
        char   c;

        for (i=0; i<wd.length();i++)    // get smallest diff to date
        {
           c=wd.charAt(i);
           if (c<'1' && c>'7') continue;// ignore, iterate
           if (c==cdow) continue;       // ignore, iterate

           j=c-cdow;                    // difference in days

           if (goUp)
           {
              if (j<0) j=j+7;           // difference to add
            }
           else
           {
              if (j>0) j=j-7;           // difference to subtract
              if (j<0) j=-j;            // get absolute value
            }

           if (j<min) min=j;            // new minimum difference
         }

        if (!goUp)  min=-min;           // go back
        date.add(min);                  // change date accordingly

        return date;
      }





    /** Determines into which calendar a date falls into and calls the appropriate
      * method to calculate the respective <em>Julian day number</em> of the
      * <em>Julian period</em>.
      *
      * This method decides which calendar (Gregorian or Julian) to use by taking
      * {@link #getGregorianChange()} into account.
     *
     * <p>Examples:
     *
     * <pre>
     *                  // assuming getGregorianChange(): '1582-10-15'
     *     DateRGF d1=new DateRGF(1582, 10,  4),  // yields: '1582-10-04' (using Julian calendar)
     *             d2=new DateRGF(1582, 10,  15), // yields: '1582-10-15' (using Gregorian calendar)
     *             d3=new DateRGF(),
     *             d4=new DateRGF();
     *
     *     int   jdn1=DateRGF.date2jdn(d1),       // yields: 2299160
     *           jdn2=DateRGF.date2jdn(d2);       // yields: 2299161
     *
     *     DateRGF.jdn2date(jdn1, d3);            // yields: '1582-10-04' (using Julian calendar)
     *     DateRGF.jdn2date(jdn2, d4);            // yields: '1582-10-15' (using Gregorian calendar)
     * </pre>
     *
      *
      * @see #date2jdn(int year, int month, int day, int flag) date2jdn(int year, int month, int day, int flag)
      *
      * @param  aDate the DateRGF for which to calculate the <em>Julian day number</em>.
      *
      * @return the <em>Julian day number</em> of the <em>Julian period</em>.
      */
    public static int date2jdn(DateRGF aDate)
    {
                // date in hand smaller than (before) first Gregorian date?
                // intentionally *not* using "jdn", hence date fields determine
                // into which calendar the date falls into, using the appropriate date2jdn().
       if ( aDate.year< stGC.year  ||
           (aDate.year==stGC.year &&
            ((aDate.month< stGC.month) ||
             (aDate.month==stGC.month && aDate.day<stGC.day)
            )
           )
          )
       {
                // a date, which falls into the Julian calendar
          return date2jdn(aDate.year, aDate.month, aDate.day, DTC.JULIAN);
        }
                // a date, which falls into the Gregorian calendar
       return date2jdn(aDate.year, aDate.month, aDate.day, DTC.GREGORIAN);
     }


    /** Calculates the <em>Julian day number</em> of the <em>Julian period</em> from a date
      * which belongs to the <em>Julian</em> or <em>Gregorian</em> calendar, depending on
      * the <code>flag</code> argument.
      *
      * <p>Refer to the <em>excellent</em> source on Calendars at
      * <a href="http://www.tondering.dk/claus/calendar.html">Frequently Asked Questions about
      * Calendars</a> (did find it the first time on 2001-03-06!, ---rgf).
      *
     *
     * <p>Example (&quot;translating a Julian calendar date to a Gregorian calendar date&quot;):
     *
     * <pre>
     *          // get Julian day numer for '2010-01-01' in the <em>Julian</em> calendar
     *    int     jdn=date2jdn(2010,1,1,JULIAN);        // yields: 2455211
     *    DateRGF   d=new DateRGF();
     *
     *    DateRGF.jdn2date(jdn, d, DateRGF.JULIAN);     // yields: '2010-01-01' (JULIAN)
     *    DateRGF.jdn2date(jdn, d, DateRGF.GREGORIAN);  // yields: '2010-01-14' (GREGORIAN)
     * </pre>
     *
      * @param flag one of {@link DTC#JULIAN} (default) or {@link DTC#GREGORIAN}
      *             indicating which calendar is targeted.
      *
      * @return the <em>Julian day number</em> of the <em>Julian period</em> with the
      *         given date belonging either to the Julian calendar or Gregorian calendar.
      *         (This number is unique for each day and independent of the calendar one uses.)
      */
    public static int date2jdn(int year, int month, int day, int flag)
    {
       int a, b=0, y, m;

       a=(14-month)/12;
       y=year+4800-a;
       m=month+12*a-3;

       return (day + (153*m+2)/5 + 365*y + (flag==DTC.GREGORIAN?
                                             (y/4 - y/100 + y/400 - 32045)      // GREGORIAN
                                             :
                                             (y/4                 - 32083)      // JULIAN
                                            )
                                           );
     }





    /**
      * Determines into which calendar a <em>Julian day number</em> of the
      * <em>Julian period</em> falls into (Julian calendar or Gregorian calendar)
      * and calls the appropriate method to set the date accordingly.
      * This method decides which of these calendar to use by taking
      * {@link #getGregorianChange()} into account.
      *
      * @see #jdn2date(int jdn, DateRGF aDate, int flag) jdn2date(int jdn, DateRGF aDate, int flag)
      *
      * @param jdn the <em>Julian day number</em> of the <em>Julian period</em>.
      * @param aDate the DateRGF to be set accordingly.
      * @return the DateRGF object (set to the new values).
      */
    public static DateRGF jdn2date(int jdn, DateRGF aDate)
    {
                // does jdn fall into Julian calendar?
       if (jdn<stGC.jdn)
          return jdn2date(jdn, aDate, DTC.JULIAN);         // create a date in the Julian calendar
       else
          return jdn2date(jdn, aDate, DTC.GREGORIAN);      // create a date in the Gregorian calendar
     }

    /** Sets the date belonging to the <em>Julian calendar</em> or <em>Graegorian calendar</em>
      * according to the given
      * <em>Julian day number</em> of the <em>Julian period</em>.
      *
      * <p>Refer to the <em>excellent</em> source on Calendars at
      * <a href="http://www.tondering.dk/claus/calendar.html">Frequently Asked Questions about
      * Calendars</a> (did find it the first time on 2001-03-06!).
      *
     *
     * <p>Example (&quot;translating a Gregorian calendar date to a Julian calendar date&quot;):
     *
     * <pre>
     *          // get Julian day numer for '2010-01-01' in the <em>Gregorian</em> calendar
     *    int     jdn=date2jdn(2010,1,1,DTC.GREGORIAN);     // yields: 2455198
     *    DateRGF   d=new DateRGF();
     *
     *    DateRGF.jdn2date(jdn, d, DTC.GREGORIAN);          // yields: '2010-01-01' (GREGORIAN)
     *    DateRGF.jdn2date(jdn, d, DTC.JULIAN);             // yields: '2009-12-19' (JULIAN)
     * </pre>
     *
      * @param jdn the <em>Julian day number</em> of the <em>Julian period</em>.
      * @param aDate the DateRGF to be set accordingly.
      * @param flag one of {@link DTC#JULIAN} or {@link DTC#GREGORIAN}
      *             indicating which calendar is targeted.
      *
      * @return the DateRGF object (set to the new values).
      */
    public static DateRGF jdn2date(int jdn, DateRGF aDate, int flag)
    {

       int b=0, c, d, e, m, m10, day, month, year;

       if (flag==DTC.GREGORIAN)
       {
          // Gregorian
          int a=jdn+32044;
          b=(4*a+3)/146097;
          c=a-(146097*b)/4;
        }
       else     // use Julian calendar (default)
       {
          // Julian
          c  =jdn+32082;
        }

       d=(4*c+3)/1461;
       e=c-(1461*d)/4;
       m=(5*e+2)/153;
       m10=m/10;                // save one division below

       if (b==0)                // Julian calendar
          aDate.year =d-4800+m10;
       else
          aDate.year =100*b+d-4800+m10;

       aDate.month=m+3-12*m10;
       aDate.day  =e-(153*m+2)/5+1;
       aDate.jdn  =jdn;

       return aDate;
     }




    /** Determines whether the given DateRGF is a leap year.
      * A leap year will have 29 days in February instead of 28.
      *
      * @param date the date to be checked on.
     *
      * @return true, if the date falls into a leap year.
     *
      */
    public static boolean isLeapYear(DateRGF date)
    {
         return ((new DateRGF(date.year, 3, 0)).day==29);
/*
        if (date.year%4!=0)        return false;
        if (date.year < stGC.year) return true;
        if (date.year%100!=0)      return true;
        return (date.year%400==0);
*/
     }




    /** Determines the number of days for the date's month.
      *
      * @param  date the date to be used.
     *
      * @return number of days.
     *
      */
    public static int daysInMonth(DateRGF date)
    {
        // this should also work for months in which the switch to the Gregorian calendar took place
       return (
                (new DateRGF(date.year, date.month+1,0)).jdn
               -(new DateRGF(date.year, date.month,  0)).jdn
              );
     }



    /** Renders the DateRGF into a Julian date.
      * A Julian date is calculated as: <kbd>year*1000+day_of_year</kbd>.
      * E.g. the date &quot;20191231&quot; yields the Julian date &quot;2019365&quot;.
      *
      * @param date the date to be rendered.
      * @return the appropriate Julian date.
      */
    public static int toJulianDate(DateRGF date)
    {
        return date.year*1000+(date.jdn-(new DateRGF(date.year-1, 12, 31)).jdn);
     }


    /** Renders a Julian date into a DateRGF.
      *
      * @param julianDate a date in the form: YYYYddd, where ddd indicates the day of the year.
      * @return a DateRGF
      */
    public static DateRGF fromJulianDate(int julianDate)
    {
        DateRGF tmp=new DateRGF(julianDate/1000-1,12,31);
        tmp.add(julianDate%1000);
        return tmp;
     }


    /** Determines into which week of the year the given DateRGF falls into using ISO rules.
      * The first ISO week of a year is the one which contains the first Thursday of January.
      * Therefore there may be years with 53 (!) weeks, e.g. 20041231 through 20050102.
      *
      * @param date the date for which the week of the year has to be determined.
      * @return a value between 1 and 53 indicating the ISO week of the year.
      */
    public static int isoWeek(DateRGF date)
    {
       DateRGF jan1=new DateRGF(date.year,1,1);
       int     diff, week, dow=dow(jan1);

       diff = (dow(jan1)>4?dow-9:dow-2);    /* second or first week?*/
       week=(date.get(DTC.JDN)-jan1.get(DTC.JDN)+1+diff)/7+1;

       if (week>51)     /* last week or within first week of next year? */
       {
           dow=dow(new DateRGF(date.year,12,31));
           if (dow<4 && (date.day>(31-dow))) week=1;
        }
       else if (week==1)
       {
           if ((date.day+diff)<0)
           {
               jan1=new DateRGF(date.year-1,1,1);
               diff = (dow(jan1)>4?dow-9:dow-2);    /* second or first week?*/
               week=(date2jdn(new DateRGF(date.year-1,12,31))-jan1.get(DTC.JDN)+1+diff)/7+1;
            }
        }
       return week;
     }



    /** Returns the DateRGF of the desired epoch. If necessary,
      * the DateRGF for the epoch is created.
      *
      * <p><em>Caution:</em> returns the original epoch DateRGF object itself,
      * which is stored in a static array for performance reasons (therefore
      * this method is defined to be private);
      * do not change the value of that epoch object!
      *
      * @param  epoch one of
                {@link DTC#EPOCH_MJD},
                {@link DTC#EPOCH_MACINTOSH},
                {@link DTC#EPOCH_PALM},
                {@link DTC#EPOCH_JAVA},
                {@link DTC#EPOCH_DOS},
                {@link DTC#EPOCH_WIN32}. Any other value
                for flag will return the default epoch DateRGF as set
                by {@link #setDefaultEpochDate(int value)}.

      * @return the appropriate epoch as a DateRGF object.
      *
      */
   private static DateRGF gED(int epoch)
   {
      if (epoch==defaultEpochFlag) {return defaultEpochDate; }

      switch (epoch)
      {
          case DTC.EPOCH_JAVA:     if (epochDates[0]==null)
                                      epochDates[0]=new DateRGF(1970,01,01);
                               return (DateRGF)epochDates[0];

          case DTC.EPOCH_DOS:      if (epochDates[1]==null)
                                      epochDates[1]=new DateRGF(1980,01,01);
                               return (DateRGF)epochDates[1];

          case DTC.EPOCH_WIN32:    if (epochDates[2]==null)
                                      epochDates[2]=new DateRGF(1601,01,01);
                               return (DateRGF)epochDates[2];

          case DTC.EPOCH_PALM:     if (epochDates[3]==null)
                                      epochDates[3]=new DateRGF(1904,01,01);
                               return (DateRGF)epochDates[3];

          case DTC.EPOCH_MJD:      if (epochDates[4]==null)
                                      epochDates[4]=new DateRGF(1858,11,17);
                               return (DateRGF)epochDates[4];

          case DTC.EPOCH_DEFAULT:
          default:             return getDefaultEpochDate(); // already a clone!
        }
     }



    /** Returns a clone (copy) of the presently defined epoch DateRGF.
        If a default epoch has not been defined yet,
        {@link DTC#EPOCH_PALM}
        is used.
    */
    public static DateRGF getDefaultEpochDate()
    {
        if (defaultEpochDate==null)
        {
           setDefaultEpochDate(DTC.EPOCH_PALM);
         }
        return (DateRGF)defaultEpochDate.clone();
     }


    /** Sets the DateRGF which presently serves as the epoch.
      *
      * @param value must be
        either an integer encoded as defined in {@link DTC#ENCODED_AS_INTEGER} or one of
        {@link DTC#EPOCH_MACINTOSH},
        {@link DTC#EPOCH_PALM} (default),
        {@link DTC#EPOCH_JAVA},
        {@link DTC#EPOCH_DOS},
        {@link DTC#EPOCH_WIN32},
        {@link DTC#EPOCH_MJD}}.

      * @return a clone of the set DateRGF object.
      */
    public static DateRGF setDefaultEpochDate(int value)
    {
        if (defaultEpochFlag!=value || value==0)// do we have to set a new epoch?
        {
           if (value<0)
           {
               switch (value)
               {
                   case DTC.EPOCH_PALM:
                   case DTC.EPOCH_JAVA:
                   case DTC.EPOCH_DOS:
                   case DTC.EPOCH_WIN32:
                   case DTC.EPOCH_MJD:
                                       break;

                   default:            value=DTC.EPOCH_PALM;
                }

               defaultEpochDate=gED(value);
               defaultEpochFlag=value;
            }
           else
           {
               defaultEpochDate=valueOf(DTC.ENCODED_AS_INTEGER, value);
               defaultEpochFlag=DTC.EPOCH_USER;
            }
         }

        return (DateRGF) defaultEpochDate.clone();
     }





    /** Returns the name of the day or of the month according to <code>flag</code>.
      *
      * <p>Examples:
      *
      * <pre>
      *      DateRGF d  =new DateRGF(2001,1,1);         // yields: '2001-01-01'
      *      String  mon=getString(DTC.DN, d.get(DTC.DOW));     // yields: "Monday"
      * </pre>
      *
      *
      * @param flag {@link DTC#DN} or {@link DTC#MN}
      * @param value 1-based <code>day</code> or <code>month</code>, if value is smaller
      *              than 1 then it defaults to 1
      * @return name of the day or month
      */

    public static String getString(int flag, int value)
    {
        if (value<1) {value=1; }        // default to 1

        switch (flag)
        {
            case DTC.DN: return dayNames[value%8-1];

            case DTC.MN: return monthNames[value%13-1];

            default: return "<DateRGF.getString(): unknown flag '"+flag+"'>";
         }
     }



    /** Allows to set the names of the days or months in either the field {@link #dayNames}
     *  or {@link #monthNames}
        according to <code>flag</code>.
      *
      * <p>Examples:
      *
      * <pre>
      *      String tagesnamen[]={"Montag", "Dienstag", "Mittwoch", "Donnerstag",
      *                           "Freitag", "Samstag", "Sonntag" };
      *
      *      DateRGF.setString(DTC..DN, tagesnamen);
      *
      *      DateRGF d  =new DateRGF(2001,1,1);         // yields: '2001-01-01'
      *      String  mon=getString(DTC.DN, d.get(DOW));     // yields: "Montag"
      * </pre>
      *
      *
      * @param flag {@link DTC#DN} or {@link DTC#MN}
      * @param value String[] of names, exactly 7 for <code>DN</code> and
                                        exactly 12 for <code>MN</code>

      */

    public static void setString(int flag, String [] value)
    {
        switch (flag)
        {
            case DTC.DN: if (value.length==7) dayNames  =value;
                     return;

            case DTC.MN: if (value.length==12) monthNames=value;
                     return;

            default: return;
         }
     }









    /** Calculate Easter Sunday for the year of the date. Uses
      * {@link #getGregorianChange()} in order to determine
      * whether Easter Sunday is to be created for the Julian calendar (Orthodox Easter Sunday)
      * or for the Gregorian calendar.
      *
      *        <p><em>Note:</em> In the very rare case of using a date which falls
      *        into the year where the
      *        change to the Gregorian calendar occurred, than be very careful which
      *        easter is calculated! In such a case you may want to explicitly
      *        determine which easter is to be calculated by invoking
      *        {@link #easter(int year, int flag)} with
      *        <code>flag</code> set to either {@link DTC#JULIAN} (i.e. orthodox easter) or
      *        {@link DTC#GREGORIAN}.
      *
      *  <p>In case you are interested in those Christian (e.g. Roman Catholic)
      *  holidays
      *  depending on Easter Sunday, these are the most important ones
      *  (giving the German name too, as well as the number of days before
      *  or after Easter Sunday, to be directly used in
      *  {@link #add(int numOfDays)}):
      *
      <p>
      <table border="1" cellpadding="3" cellspacing="0">
      <tr class="TableHeadingColor">
      <td> Day dependent on Easter Sunday
      <td> Number of days to add/subtract to/from Easter Sunday


      <tr class="TableRowColor"><!-- table row -->
      <td>      Shrove Tuesday (Mardi Gras)<br>(Faschingsdienstag)
      <td>      -47

      <tr class="TableRowColor"><!-- table row -->
      <td>      Ash Wednesday<br>(Aschermittwoch)
      <td>      -46 (six weeks and four days before Easter Sunday)

      <tr class="TableRowColor"><!-- table row -->
      <td>      Saturday of Lazarus
      <td>      -8 (one week before Easter Sunday)

      <tr class="TableRowColor"><!-- table row -->
      <td>      Palm Sunday<br>(Palmsonntag)
      <td>      -7 (one week before Easter Sunday)

      <tr class="TableRowColor"><!-- table row -->
      <td>      Maundy Thursday<br>(Gr&uuml;ndonnerstag)
      <td>      -3

      <tr class="TableRowColor"><!-- table row -->
      <td>      Good Friday<br>(Karfreitag)
      <td>      -2

      <tr class="TableHeadingColor"><!-- table row -->
      <td>      Easter Sunday <br>(Ostersonntag)
      <td>      0

      <tr class="TableRowColor"><!-- table row -->
      <td>      Easter Monday <br>(Ostermontag)
      <td>      +1

      <tr class="TableRowColor"><!-- table row -->
      <td>      Ascension Day <br>(Christi Himmelfahrt)
      <td>      +39 (five weeks and four days after Easter Sunday)


      <tr class="TableRowColor"><!-- table row -->
      <td>      Whitsunday <br>(Pfingstsonntag)
      <td>      +49 (seven weeks after Easter Sunday)


      <tr class="TableRowColor"><!-- table row -->
      <td>      Whitmonday <br>(Pfingstmontag)
      <td>      +50 (seven weeks and one day after Easter Sunday)

      <tr class="TableRowColor"><!-- table row -->
      <td>      Corpus Christi <br>(Fronleichnam)
      <td>      +60 (eight weeks and four days after Easter Sunday)

      </table>
      <p>


     * <p>Examples:
     *
     * <pre>
     *          // assuming Gregorian change of most of the British Empire and
     *          // and USA, i.e. first Gregorian date is '<strong>1752-09-14</strong>':
     *    setGregorianChange(1752, 9, 14);      // set Gregorian change date
     *
     *    DateRGF d1=new DateRGF(1659, 11, 17), // yields: '1659-11-17' (a <em>Julian calendar</em> date)
     *            d2=new DateRGF(2016,  2,  1), // yields: '2016-02-01' (a <em>Gregorian calendar</em> date)
     *
     *    DateRGF d3=easter(d1);        // yields: '1659-04-03' (a <em>Julian calendar</em> date)
     *            d4=easter(d2);        // yields: '2016-03-27' (a <em>Gregorian calendar</em> date)
     * </pre>
     *

      * @see #easter(int year, int flag) easter(int year, int flag)
      *
      * @param date determines the year for which Easter Sunday is to be calculated.
      *        If it is smaller (earlier) than {@link #getGregorianChange()}
      *        than the Julian calendar is used, the Gregorian calendar else.
      *
      * @return a new DateRGF object set to Easter Sunday.
      */
    public static DateRGF easter(DateRGF date)
    {
       if (date.get(DTC.JDN)<stGC.get(DTC.JDN)) // Julian calendar?
          return  easter(date.year, DTC.JULIAN);
       else
          return  easter(date.year, DTC.GREGORIAN);
     }


    /** Calculate the Julian (Orthodox) or Gregorian (Western) Easter Sunday for
      * the given year.
      *
      * <p>The returned date
      *         represents the respective calendar, i.e. if <code>flag</code>
      *         is {@link DTC#GREGORIAN}, then the date returned belongs to
      *         to the <em>Gregorian calendar</em>, otherwise to the <em>Julian
      *         calendar</em>. Therefore you may directly use the {@link #jdn}
      *         to produce the according date in the other calendar system.
      *
      * <p>Refer to the <em>excellent</em> source on Calendars at
      * <a href="http://www.tondering.dk/claus/calendar.html">Frequently Asked Questions about
      * Calendars</a> (did find it the first time on 2001-03-06!, ---rgf).
     *
     *
     * <p>Example:
     *
     * <pre>
     *    DateRGF d1=new DateRGF(2016, 9, 1);           // yields: '2016-09-01'
     *
     *    DateRGF d2=easter(d1.get(DTC.YEAR_FIELD), DTC.JULIAN);// yields: '2016-04-18'
     *                                                  // (a <em>Julian calendar</em> date)
     *
     *    d2.jdn2date(d2.get(JDN), d2, DTC.GREGORIAN);      // yields: '2016-05-01'
     *                                                  // (a <em>Gregorian calendar</em> date)
     * </pre>

      *
      * @param year for which Easter Sunday is to be calculated.
      *
      * @param flag one of {@link DTC#JULIAN} or {@link DTC#GREGORIAN}
      *             indicating which calendar is targeted.
      *
      * @return a DateRGF set to Easter Sunday of the given year.
      */
    public static DateRGF easter(int year, int flag)
    {
       int G,   // Golden Number - 1
           I,   // number of days from 21 March to the Paschal full moon
           J,   // weekday for the Paschal full moon (0=Sunday, 1=Monday, ...)
           L;   // number of days from 21 March to the Sunday on or before the Paschal full
                // moon (a number between -6 and 28)

       DateRGF tmpD=new DateRGF();

       G = year % 19;

       if (flag==DTC.GREGORIAN)     // Gregorian calendar
       {
          int C  = year/100;
          int c4 = C/4;         // save one division later
          int H  = (C - c4 - (8*C+13)/25 + 19*G + 15) % 30;     // 23-epact (modulo 30)
          int h28= H/28;        // save one division later
          I      = H - h28*(1 - h28*(29/(H+1))*((21-G)/11) );
          J      = (year + year/4 + I + 2 - C + c4) % 7;
        }
       else                     // Julian calendar (default)
       {
          I      = (19*G + 15) % 30;
          J      = (year + year/4 + I) % 7;
        }

       L= I - J;

       int m = 3 + (L+40)/44,           // Easter Month
           d = (L + 28 - 31*(m/4));     // Easter day


       tmpD.year =year;
       tmpD.month=m;
       tmpD.day  =d;
       tmpD.jdn  =date2jdn(year, m, d, flag);

       return tmpD;
     }



    /**
      * Returns a clone of the DateRGF object which is set to the first date
      * of the Gregorian calendar.
      * All days before this date are in the Julian calendar.
      */
    public static DateRGF getGregorianChange()
    {
       return (DateRGF)stGC.clone();    // return a clone, such that changes to it do
                                        // not affect the original Gregorian change date
     }


    /**
      * Allows to set the date on which the usage of the Gregorian calendar starts.
      * All days before this date are in the Julian calendar.
      *
      * @param year the year of the first Gregorian date.
      * @param month the month of the first Gregorian date.
      * @param day the day of the first Gregorian date.
      * @return a clone of the set DateRGF object.
      */
    public static DateRGF setGregorianChange(int year, int month, int day)
    {
       stGC.year =year;
       stGC.month=month;
       stGC.day  =day;
       stGC.jdn  =date2jdn(year, month, day, DTC.GREGORIAN); // get jdn for this date
       return (DateRGF)stGC.clone();
     }




    /**
      * Overrides default java.io.Serializable by merely storing {@link #jdn}.
      */
 private void writeObject(java.io.ObjectOutputStream out)
     throws java.io.IOException
    {
       out.writeInt(this.jdn);
     }


    /**
      * Overrides default java.io.Serializable by merely retrieving {@link #jdn}.
      * Uses {@link #jdn2date(int jdn, DateRGF aDate)} to re-create the appropriate
      * DateRGF object.
      *
      * <p><em>Note:</em> the locally Gregorian change date will be used
      * to create a Julian or a Gregorian calendar date.
      *
      */
 private void readObject(java.io.ObjectInputStream in)
     throws java.io.IOException, ClassNotFoundException
    {
       jdn2date(in.readInt(), this);    // create the DateRGF from jdn
     }





    /** Anonymous block to initialize settings at package/class load time.
      */
    static {
//System.out.println("DateRGF().cinit()...");
                // default values
       setGregorianChange(1582, 10, 15);// set begin of Gregorian calendar to 1582-10-15
       setDefaultEpochDate(DTC.EPOCH_PALM); // make sure that an epoch date is set
       weekStart=7;                    // weeks start on Sundays
     }





// <!-- JAVA versions - B E G I N -  -->
  /** This method sets the DateRGF object to
    * the actual local date of the system.
    * For this purpose class <em>java.util.GregorianCalendar</em>
    * is instantiated.
    *
    * <p>Examples:
    * <pre>
    *     DateRGF     d =new DateRGF(); // yields: '0001-01-01'
    *
    *           // if run on "2010-09-22", then
    *     d.update();                   // yields: '2010-09-22'
    * </pre>
    */
  public DateRGF update()
  {
                // Java runtime, get local date and time
      java.util.GregorianCalendar nowGC=new java.util.GregorianCalendar();
      return this.set(nowGC.get(java.util.Calendar.YEAR ),
                      nowGC.get(java.util.Calendar.MONTH)+1,
                      nowGC.get(java.util.Calendar.DAY_OF_MONTH) );
   }
// <!-- JAVA versions - E N D     -  -->


}




