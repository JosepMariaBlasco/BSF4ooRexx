//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

package org.oorexx.datergf; // "DateTime_RGF"-package

/** This class helps to format DateRGF objects in strings, using
  * various renderings.<p>
 *
  *
  *
  * {@link org.oorexx.datergf.DateFormatRGF} is an implementation of
  * formatting dates and times using either the Java-style or the Unix-style keys (strfmt,
  * escaping formatting keys with a percent sign &quot;%&quot;).
  * The formatting keys consist of letters,
  * which may be repeated one or more times in order to indicate different renderings.
  *
  * <p>
 *
 *


  * This class has a Waba and a Java version, due to the following
  * methods, which are implemented depending on the runtime environment:
  *
        <table border="1" cellpadding="3" cellspacing="0">
        <tr class="TableHeadingColor">
        <td colspan="2" align="center"> Implemented?
        <td> &nbsp;
        <td> &nbsp;

        <tr class="TableHeadingColor">
        <td align="center">     Waba
        <td align="center">     Java
        <td>                    Method name
        <td>                    Short Description

        <tr class="TableRowColor"><!-- table row -->
        <td align="center">     yes
        <td align="center">     yes
        <td align="left">       public static void localize()
        <td align="left">       Sets formatting options of
                                {@link org.oorexx.datergf.DateRGF} and
                                {@link org.oorexx.datergf.TimeRGF} to the local/default Locale
                                settings.

        <tr class="TableRowColor"><!-- table row -->
        <td align="center">     no
        <td align="center">     yes
        <td align="left">       public static void localize(java.util.Locale loc)
        <td align="left">       Sets formatting options of
                                {@link org.oorexx.datergf.DateRGF} and
                                {@link org.oorexx.datergf.TimeRGF} according to the settings
                                of the Locale available to the Java runtime
                                environment.

        <tr class="TableRowColor"><!-- table row -->
        <td align="center">     no
        <td align="center">     yes
        <td align="left">       protected static int findFirstDigit(String str)
        <td align="left">       Helper method for the Java runtime environment version.

        <tr class="TableRowColor"><!-- table row -->
        <td align="center">     no
        <td align="center">     yes
        <td align="left">       protected static int toInt(String str, int start, int length)
        <td align="left">       Helper method for the Java runtime environment version.

        </table>

  *
  *

    <hr>
    <p>
    This version was created with Waba
  * (e.g. <a href="http://www.superwaba.org">http://www.SuperWaba.org</a>)
  * family of Java-compatible mobile systems (PDAs, Handies, etc.)
  * in mind. Hence this package does not use threads or exceptions.
  * The Waba version does not employ the datatypes
  * long and double, which may not be available on other Java Virtual machine
  * compatible implementations for small devices like PDAs.

  * <p>This particular class was derived from
  * <a href="http://www.wildfire.dircon.co.uk/">Toby Thurston's</a>
  * <a href="http://www.RexxLA.org">Rexx</a> program named &quot;makedate.cmd&quot;,
  * which is part of his great cal-package (a calendar
    package for the freely available
    <a href="http://www.tangbu.com/x2main.shtml">x2-editors
    <!-- http://www.interlog.com/~bwt">x2-editors -->
    (by Blair W. Thompson)</a> available
    for practically all operating systems (even with Java language support)
    and having Rexx as its macro
    language.


  * <p><hr>
  * <p>Examples:
  *
  * <pre>
  *   DateRGF d=new DateRGF(2033, 9, 22);             // yields: '2033-09-22'
  *
  *   String  info1=format("EEEE, yy-MMM-dd!",   d);  // yields: "Thursday, 33-Sep-22!"
  *   String  info2=format("%A, %y-%b-%d!",      d);  // yields: "Thursday, 33-Sep-22!"
  *
  *   String  info3=format("'days so far: 'DDD", d);  // yields: "days so far: 265"
  *   String  info4=format("'days to go:  'ggg", d);  // yields: "days to go:  100"
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

  * Temporary dev-infos:
  *
  *    version  date            remark
  *
  *    0.92     2001-03-20      - adding option "S" for displaying milliseconds
  *
  *             2001-04-02      - introduced variant "JAVA" and "WABA"
  *
  *             2005-12-28      - added Apache license 2.0, put sources with BSF4Rexx
  * </pre>
  *
  *
 * <hr>
  * @author Rony G. Flatscher
  * @version 0.92,  date: 2001-02-08 through 2001-04-02, 2006-01-01
  *
  *
  */


public class DateFormatRGF  {
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";



    /** Array of lowercase roman number strings (1 through 12).
      */
    final public static String romanL[] ={"i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii"};
    /** Array of uppercase roman number strings (1 through 12). */
    final public static String romanU[] ={"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};


    /** Supported format character keys in argument <code>fmt</code> of
      * method
      * {@link #format(String fmt, Object obj)}.
      * Characters: <code>&quot;yMdhHmsSEeoRrDgwakK&quot;</code>.
      */
    static protected char []options={
                                 'y', // year
                                 'M', // Month
                                 'd', // day (in month)
                                 'h', // hour in day 1-12
                                 'H', // hour in day 0-23
                                 'm', // minute
                                 's', // second
                                 'S', // milliSecond

                                 'E', // day of week (textual)
                                 'e', // day of week (Monday=1, ..., Sunday=7)
                                 'o', // dow_ord
                                 'R', // month in (uppercase) Roman
                                 'r', // month in (lowercase) Roman

                                 'D', // day in year (Julian day)
                                 'g', // days left to end of year
                                 'w', // ISO week in year
                                 'a', // am/pm marker
                                 'k', // hour in day 1-24
                                 'K'  // hour in day 0-11
                                 };



    /** Creates a string according to a format pattern, using objects of type
      * <a href="DateTimeRGF.html">DateTimeRGF</a>,
      * <a href="DateRGF.html">DateRGF</a> or
      * <a href="TimeRGF.html">TimeRGF</a>.
      *
      * <p>Text enclosed within single or double quotes is unchanged
      * (the enclosing quotes are removed). If one needs to use
      * the quotes themselves, either use the other quote to enclose
      * them or escape the quote by doubling (i.e. use another quote to
      * immediately follow the first one) it.
      * (It is also possible to escape the percentage sign outside
      * of quotes by doubling it.)
      *
      * <p>The following table lists the available keys and explains
      * the formatting induced by it. The symbols where picked from Java's SimpleDateFormat().
      *
      * <p>
        <table border="1" cellpadding="3" cellspacing="0">
        <tr class="TableHeadingColor">
        <td colspan="2" align="center"> Keys
        <td> &nbsp;

        <tr class="TableHeadingColor">
        <td align="right">      Java
        <td align="center">     Unix
        <td>                    Formatting

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      d
        <td align="center">     %e
        <td>                    day of month

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      dd
        <td align="center">     %d
        <td>                    day of month with leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      e
        <td align="center">     %w
        <td>                    day of week (Monday=1 ... Sunday=7)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      o
        <td align="center">     &nbsp;---
        <td>                    ordinal number of day of week, relative to
                                {@link DateRGF#weekStart DateRGF.weekStart};
                                e.g. if weeks start with Sunday, then Sunday=1 and
                                Saturday=7; if weeks start with Monday, then
                                Monday=1 and Sunday=7.
                                <br><em>Note: extension of this package! </em>

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      E
        <td align="center">     %a
        <td>                    first three letters of the dayname (abbreviation)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      EEEE
        <td align="center">     %A
        <td>                    full name of the day


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      M
        <td align="center">     &nbsp;---
        <td>                    number of the month

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      MM
        <td align="center">     %m
        <td>                    number of the month with leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      MMM
        <td align="center">     %b
        <td>                    first three letters of the monthname

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      MMMM
        <td align="center">     %B
        <td>                    full name of the month

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      r
        <td align="center">     &nbsp;---
        <td>                    number of the month as a lowercase Roman number
                                <br><em>Note: extension of this package! </em>

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      R
        <td align="center">     &nbsp;---
        <td>                    number of the month as an uppercase Roman number
                                <br><em>Note: extension of this package! </em>

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      w
        <td align="center">     %W
        <td>                    week of the year (according to ISO rules)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      ww
        <td align="center">     &nbsp;---
        <td>                    week of the year (according to ISO rules) with leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      y
        <td align="center">     &nbsp;---
        <td>                    year

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      yy
        <td align="center">     %y
        <td>                    the last two digits in year, leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      yyy
        <td align="center">     &nbsp;---
        <td>                    year with as many digits as needed

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      yyyy
        <td align="center">     %Y
        <td>                    year with four digits, leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      D
        <td align="center">     &nbsp;---
        <td>                    day of the year (Julian date), no leading 0

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      DD
        <td align="center">     &nbsp;---
        <td>                    day of the year (Julian date), at least two digits (leading 0)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      DDD
        <td align="center">     %j
        <td>                    day of the year (Julian date), exactly three digits (leading 0)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      g
        <td align="center">     &nbsp;---
        <td>                    days to end of year, no leading 0
                                <br><em>Note: extension of this package! </em>

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      gg
        <td align="center">     &nbsp;---
        <td>                    days to end of year, at least two digits (leading 0)
                                <br><em>Note: extension of this package! </em>

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      ggg
        <td align="center">     &nbsp;---
        <td>                    days to end of year, exactly three digits (leading 0)
                                <br><em>Note: extension of this package! </em>


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      H
        <td align="center">     &nbsp;---
        <td>                    hour (range: 0-23)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      HH
        <td align="center">     &nbsp;---
        <td>                    hour, two digits (leading 0, range: 0-23)


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      k
        <td align="center">     &nbsp;---
        <td>                    hour (range: 1-24)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      kk
        <td align="center">     &nbsp;---
        <td>                    hour, two digits (leading 0, range: 1-24)



        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      K
        <td align="center">     &nbsp;---
        <td>                    hour in am/pm (range: 0-11)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      KK
        <td align="center">     &nbsp;---
        <td>                    hour in am/pm, two digits (leading 0, range: 0-11)



        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      h
        <td align="center">     &nbsp;---
        <td>                    hour in am/pm (range: 1-12)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      hh
        <td align="center">     &nbsp;---
        <td>                    hour in am/pm, two digits (leading 0, range: 1-12)


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      m
        <td align="center">     &nbsp;---
        <td>                    minute

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      mm
        <td align="center">     &nbsp;---
        <td>                    minute, two digits (leading 0)



        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      s
        <td align="center">     &nbsp;---
        <td>                    second

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      ss
        <td align="center">     &nbsp;---
        <td>                    second, two digits (leading 0)


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      S
        <td align="center">     &nbsp;---
        <td>                    millisecond

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      SS
        <td align="center">     &nbsp;---
        <td>                    millisecond, at least two digits (leading 0)

        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      SSS
        <td align="center">     &nbsp;---
        <td>                    millisecond, exactly three digits (leading 0)


        <tr class="TableRowColor"><!-- table row -->
        <td align="right">      a
        <td align="center">     &nbsp;---
        <td>                    am/pm marker


        </table>

      *<p>
      *
      * <p>Examples:
      *
      * <pre>
      *    DateTimeRGF dt=new DateTimeRGF(2033, 9, 22, 12, 6, 59); // yields: '2033-09-22 12:06:59'
      *
      *    String  info1=format("EEEE, d. MMMM yyyy, hh:mm a 'o''clock'", dt);
      *                           // info1: "Thursday, 22. September 2033, 12:06 pm o'clock"
      *
      *    String  info2=format("EEEE, yy-MMM-dd!",   dt); // yields: "Thursday, 33-Sep-22!"
      *    String  info3=format("%A, %y-%b-%d!",      dt); // yields: "Thursday, 33-Sep-22!"
      *
      *    String  info4=format("D/ggg", dt);              // yields: "265/100"
      *
      *    String  info5=format("'week #' w", dt);         // yields: "week # 38"
      *
      *    String  info6=format("'day of week:' e", dt);   // yields: "day of week: 4"
      *    String  info7=format("'dayname:    ' EEEE", dt);// yields: "dayname: Thursday"
      *    String  info8=format("'ordinal day:' o", dt);   // yields: "ordinal day: 5", if
      *                                                    //          DateRGF.weekStart=7
      *
      *    String  info9=format("d. R. yyyy", dt);         // yields: "22. IX. 2033"
      * </pre>
      *
      *
      * @param fmt  the string containing formatting codes.
      * @param obj an instance of <a href="DateTimeRGF.html">DateTimeRGF</a>,
      *                           <a href="DateRGF.html">DateRGF</a> or
      *                           <a href="TimeRGF.html">TimeRGF</a>.
      * @return     a String containing formatted DateRGF data.
      *
      */
    public static String format(String fmt, Object obj)
    {
       if (obj==null) return fmt;      // no DateRGF, hence no formatting
       DateTimeRGF dt=null;

       if       (obj instanceof DateTimeRGF) dt=(DateTimeRGF)obj;
       else  if (obj instanceof DateRGF)
       {
          dt=new DateTimeRGF( (DateRGF)obj, new TimeRGF() );
       }
       else  if (obj instanceof TimeRGF)
       {
          dt=new DateTimeRGF( new DateRGF(), (TimeRGF)obj );
       }
       else return fmt;                 // not a valid class



       int    len=fmt.length(),
              i,
              i1, i2, i3
              ;
       char   c;
       String out="",                   // String to be returned
              key="";                   // key to be used for formatting

           // parse the string
       for (i=0; i<len; i++)
       {
          c=fmt.charAt(i);              // get character

                // quoted text --------------------------------------------------------------
          if (c=='\'' || c=='"')     // quoted string?
          {
             if (key.length()>0)               // pending key?
             {
                out=out+dateval(key, dt);
                key="";
             }

             if ( c==( (i+1)<len ? fmt.charAt(i+1) : (char)'?' ) )        // escaped?
             {
                out=out+c;
                i++;
                continue;               // iterate
             }

             for (; i<len; i++)                 // loop until closing quote is found
             {
                 i1=pos(c, fmt, i+1);           // find closing quote

                 if (i1==-1)                    // oops, closing quote not found!
                 {
                     return out+fmt.substring(i);   // append remainder and return
                 }

                 out=out+fmt.substring(i+1, i1); // extract quoted text
                 i=i1;

                 if ( c==( (i1+1)<len ? fmt.charAt(i1+1) : (char)'?' ) )        // escaped?
                 {
                    out=out+c;
                    continue;               // iterate
                 }
                 break;
             }
             continue;  // iterate
          }

                // Unix strfmt --------------------------------------------------------------
          if (c==(char)'%')             // Unix strfmt-escaped?
          {
             if (key.length()>0)               // pending key?
             {
                out=out+dateval(key, dt);
                key="";
             }
             i++;
             if (i>len)                 // o.k. at end, nothing left anymore
                return out+c;

             c=fmt.charAt(i);
             switch (c)
             {
                case '%': out=out+c;
                          continue;     // iterate

                case 'e': key="d";      // day of month (DOM)
                          break;

                case 'd': key="dd";     // DOM with leading 0
                          break;

                case 'a': key="E";      // first three letters of day of week name
                          break;

                case 'A': key="EEEE";   // full name of day of week name
                          break;

                case 'm': key="MM";     // month with leading 0
                          break;

                case 'b': key="MMM";    // first three letters of month name
                          break;

                case 'B': key="MMMM";   // full name of month name
                          break;

                case 'w': key="e";      // day of week
                          break;

                case 'W': key="ww";     // week of the year (according to ISO rules)
                          break;

                case 'y': key="y";      // last two digits of the year (leading 0s)
                          break;

                case 'Y': key="yyy";    // year, with all digits used
                          break;

                case 'j': key="DDD";    // Julian date, three digits (leading 0s)
                   break;

                default:  out=out+"%"+c;
                          continue;     // iterate
             }
             out=out+dateval(key, dt);
             key="";
             continue;
          }

                // check 'c'   --------------------------------------------------------------
          if (verify(c, options) == -1)  // 'c' not found
          {
             if (key.length()>0)               // pending key?
             {
                out=out+dateval(key, dt);
                key="";
             }
             out=out+c;
             continue;  // iterate
          }

          if (key.length()==0 || c==key.charAt(0)) key=key+c;   // build key
          else          // new character
          {
              out=out+dateval(key, dt);
              key=""+c;
          }
       }

       if (key.length()>0) return out+dateval(key, dt);
       return out;
    }




    /** Returns a string, formatted according to the received key.
      *
      * @param  key String indicating the desired format.
      *
      * @param  dt the DateTimeRGF object to use.
      *
      * @return the formatted String for the key, or if the key is
      *         unknown, the key itself.
      */
    private static String dateval(String key, DateTimeRGF dt)
    {
       int len=key.length(), temp=-1;

/*
            'h', // hour in day 1-12
            'H', // hour in day 0-23
            'm', // minute
            's', // second

            'k', // hour in day 1-24
            'K'  // hour in day 0-11
*/
       char c = key.charAt(0);
       switch (c)
       {
          case 'd':     // day number
                    if      (len==1) return ""+dt.date.day;
                    else             return DateRGF.ri(dt.date.day, 2);

          case 'E':     // day name
                    {
                       String tmpS=DateRGF.getString(DTC.DN, DateRGF.dow(dt.date));
                       if (len<4 && tmpS.length()>2)       // use abbreviation
                          return tmpS.substring(0,3);

                       return tmpS;     // return full string
                    }

          case 'M':     // month
                    {
                       if      (len==1) return ""+dt.date.month;
                       else if (len==2) return DateRGF.ri(dt.date.month, 2);

                           // text
                       String tmpS=DateRGF.getString(DTC.MN, dt.date.month);

                       if (len==3 && tmpS.length()>2)       // use abbreviation
                             return tmpS.substring(0,3);

                       return tmpS;
                    }

          case 'e':     // day of week
                    return ""+DateRGF.dow(dt.date);       // ISO index of day of week

          case 'w':     // ISO week number
                    temp=DateRGF.isoWeek(dt.date);
                    if (len==1) return (""+temp);
                    return DateRGF.ri(temp, 2);

          case 'o':     // ordinal value of day of week, relative to *DateRGF.weekStart*
                    return ""+DateRGF.dow_ord(dt.date);

          case 'y':     // year value
                    if      (len==1) return ""+(dt.date.year%100);                 // use last two digits
                    else if (len==2) return DateRGF.ri(dt.date.year%100, 2);       // use last two digits force leading 0
                    else if (len==3) return ""+dt.date.year;                       // use as many digits as needed
                    else             return DateRGF.ri(dt.date.year, 4);           // use four digits with leading 0s

          case 'r':     // month as lowercase Roman
                    return romanL[dt.date.month-1];

          case 'R':     // month as uppercase Roman
                    return romanU[dt.date.month-1];

          case 'D':     // Julian date
          case 'g':     // days to go until end of year
                    int days=0;

                    if (c=='D')         // Julian days
                    {
                        days =  dt.date.get(DTC.JDN)
                               -new DateRGF(dt.date.year, 1, 1).get(DTC.JDN)
                               +1;
                    }
                    else                // days left in year
                    {
                        days =  new DateRGF(dt.date.year, 12, 31).get(DTC.JDN)
                               -dt.date.get(DTC.JDN);
                    }

                    if      (len==1) return ""+days;
                    else if (len==2) return DateRGF.ri(days, 2);
                    else             return DateRGF.ri(days, 3);

          case 'm':     // minute
          case 's':     // second

          case 'K':     // hour, range 0-11
          case 'H':     // hour, range 0-23

          case 'h':     // hour, range 1-12
          case 'k':     // hour, range 1-24


                    if      (c=='m') temp=dt.time.minute;

                    else if (c=='s') temp=dt.time.second;

                    else if (c=='H') temp=dt.time.hour;

                    else if (c=='k')
                    {
                       temp=dt.time.hour;
                       if (temp==0) temp=24;
                    }

                    else if (c=='K' || c=='h')
                    {
                       temp=dt.time.hour%12;

                       if (c=='h' && temp==0) temp=12;
                    }

                    if (len==1) return (""+temp);
                    return DateRGF.ri(temp, 2);


          case 'S':     // milliSecond
                    {
                       if      (len==1) return ""+dt.time.millis;
                       else if (len==2) return DateRGF.ri(dt.time.millis, 2);
                       return DateRGF.ri(dt.time.millis, 3);
                    }


          case 'a':     // am/pm marker
                    return TimeRGF.am_pm_string[ (dt.time.hour<12 ? 0 : 1) ];


          default:
                    return key;         // unknown, return unaltered
       }
    }



    /** Verifies that <code>needle</code> is in character array of <code>reference</code>.
      *
      * Modelled after Rexx' VERIFY() function.
      *
      * @param  needle character to check.
      * @param  reference array of characters to verify with.
      * @return 0-based index into <code>reference</code> having the same value as needle,
      *         or <code>-1</code> if not found.
      */
    protected static int verify(char needle, char [] reference)
    {
       int i;
       for (i=0; i<reference.length;i++)
       {
          if (needle==reference[i]) return i;
       }

       return -1;
    }

    /** Returns the 0-based position of <code>needle</code> in <code>string</code>,
      * starting the search at the indicated 0-based <code>start</code> position,
      * or <code>-1</code>, if not found.
      *
      * This is modelled after Rexx' POS() function.
      */
    protected static int pos(char needle, String string, int start)
    {
       int i;
       if (start>string.length()) return -1;    // start beyond String

       for (i=start; i<string.length(); i++)    // seek character
       {
          if (needle==string.charAt(i)) return i;
       }
       return -1;
    }








// <!-- JAVA versions - B E G I N -  -->
   /** Sets the national language characteristics of the Date/Time/RGF package. This
     * affects the Strings of the daynames, monthnames and am/pm Strings as well as
     * the date and time separators, the default order of the date fields,
     * the day which starts a week, the date the Gregorian calendar took effect.
     *
     * <ul>
     * <li>
     * This method uses Java's default <code>java.util.Locale()</code>
     * to determine the specific NLS settings of the machine this code
     * runs at. The work itself is carried out in method
     * {@link #localize(java.util.Locale loc)}.
     *
     * <li>
     * If run under SuperWaba (i.e. Waba with a version > 1.0), then
     * <code>waba.sys.Settings()</code> is used. <em>Not implemented as of yet!</em>
     * </ul>
     *
     */
    public static void localize()
    {
       localize(java.util.Locale.getDefault()); // Java
       return;
    }




    /** Sets the national language characteristics of the Date/Time/RGF package using
     *  the Java runtime information. This affects the global settings of the Date/Time/RGF
     *  settings, such that the <code>toString()</code> methods reflect the new formats.
      *
  *
  * <p>There is <em>no</em> Waba dependent version of this method!
  *
    *

     * <p>The following settings are affected by this method:
     *
     * <ul>
     * <li>{@link DateRGF#dateOrder},
     *
     * <li>{@link DateRGF#dayNames} <em>(Java runtime only)</em>,
     *
     * <li>{@link DateRGF#dateSeparator},
     *
     * <li>{@link DateRGF#monthNames} <em>(Java runtime only)</em>,
     *
     * <li>{@link DateRGF#stGC} <em>(Java runtime only)</em>,
     *
     * <li>{@link DateRGF#weekStart},
     *
     * <li>{@link TimeRGF#am_pm_string},
     *
     * <li>{@link TimeRGF#is24Hour},
     *
     * <li>{@link TimeRGF#timeSeparator}.

     *
     * </ul>


      * <p>{@link DateFormatRGF#format(String fmt, Object obj)} will use
      * the localized versions of the daynames, monthnames and the am/pm string.
      *
      * <p>Examples:
      *
      * <pre>
      *   DateTimeRGF dt = new DateTimeRGF(2033, 9, 22, 12, 6, 59);
      *   String    form = "EEEE, yyyy-mmm-dd HH:mm/hh:mm a";
      *   String s1, s2, s3, s4, s5, s6, s7, s8;
      *
      *   s1=DateFormatRGF.format(form, dt); // yields: "Thursday, 2033-06-22 12:06/12:06 pm"
      *   s2=dt.toString();                  // yields: "2033-09-22 12:06:59"
      *
      *   localize(java.util.Locale.GERMAN);
      *   s3=DateFormatRGF.format(form, dt); // yields: "Donnerstag, 2033-06-22 12:06/12:06 PM"
      *   s4=dt.toString();                  // yields: "22.09.2033 12:06:59"
      *
      *   localize(java.util.Locale.ITALIAN);
      *   s5=DateFormatRGF.format(form, dt); // yields: "gioved&igrave;, 2033-06-22 12:06/12:06 PM"
      *   s6=dt.toString();                  // yields: "22/09/2033 12.06.59"
      *
      *   localize(java.util.Locale.US);
      *   s7=DateFormatRGF.format(form, dt); // yields: "Thursday, 2033-06-22 12:06/12:06 PM"
      *   s8=dt.toString();                  // yields: "09/22/2033 12:06:59 PM"
      * </pre>
      *
     * @see #localize() localize()
     *
     * @param loc the Locale to be used.
     *
     */
    public static void localize(java.util.Locale loc)
    {
       if (loc==null) loc=java.util.Locale.getDefault(); // make sure a Locale is available
       else           java.util.Locale.setDefault(loc);  // make the passed in Locale the default


       java.util.TimeZone tz_ori= java.util.TimeZone.getDefault(),
                          tz_gmt= java.util.TimeZone.getTimeZone("GMT-0:00");

                // get a localized GregorianDate
       java.util.GregorianCalendar aGregCal = new java.util.GregorianCalendar(loc);

                // get date/time formatters
       java.text.DateFormat df_d =java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, loc);
       java.text.DateFormat df_t =java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, loc);

                // first day of the week
       int    fdow=df_d.getCalendar().getFirstDayOfWeek();
       int    weekStart=fdow-1;        // Java is 1=Sunday, 2=Monday ... 6=Saturday
                                        // normalize on ISO Monday (i.e. Mon=1 ... Sun=7)
       if (weekStart==0) weekStart=7; // set to ISO Sunday


       char    dateSeparator=0, timeSeparator=0;
       int     dateOrder=0;
       boolean is24Hour=true,
               bDigitsFound
               ;


                        // create a particular date which allows to infer the order of the fields
       int              year=2001, month=01, day=8, hour=18, minute=22, second=33; // a Monday in January
       long                   ld =978978153000l,        // '2001-01-08 18:22:33'
                              ld2=978934953000l;        // '2001-01-08 06:22:33' (before noon!)
       java.util.Date       nowD =new java.util.Date( ld );


       String          shortDate =java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, loc).format(nowD);
       String          shortTime =java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, loc).format(nowD);


       int  i, j, n1=-1;    // negative value indicates no number found
       char c;
       String str="";

       i=findFirstDigit(shortDate);     // determine position of first digit

       if (i!=-1)
       {
          str="";
          for (j=0 ; i<shortDate.length(); i++)  // extract digits, delimiter and determine date order
          {
             c=shortDate.charAt(i);      // get char
             if (c>='0' && c<='9') str=str+c;
             else
             {
                 dateSeparator=c;        // get delimiter
                 n1=toInt(str, 0, 99);
                 break;                  // enough infos
             }
          }
       }

       if (n1==-1)       // not enough infos available, use defaults
       {
          dateSeparator='-';             // default to dash
          dateOrder    =DTC.YMD;     // default to Year-Month-Day order
          weekStart    =DTC.MONDAY;  // default to Monday (most European countries)
       }
       else              // o.k., enough date-infos
       {                 // determine date order:
          if (n1==month)    {dateOrder=DTC.MDY; }
          else if (n1==day) {dateOrder=DTC.DMY; }
          else              {dateOrder=DTC.YMD; }

       }
                     // set DateRGF formatting options
       DateRGF.setStatic(DTC.WEEK_START, weekStart);
       DateRGF.setStatic(DTC.DATE_ORDER, (byte) dateOrder);
       DateRGF.dateSeparator=dateSeparator;



                 // now figure out time formats
       n1=-1;
       i=findFirstDigit(shortTime);     // determine position of first digit

       if (i!=-1)
       {
          str="";
          for (j=0 ; i<shortTime.length(); i++)  // extract digits, delimiter and determine date order
          {
             c=shortTime.charAt(i);      // get char
             if (c>=(char)'0' && c<=(char)'9') str=str+c;
             else
             {
                 timeSeparator=c;        // get delimiter
                 n1=toInt(str, 0, 99);
                 break;                  // enough infos
             }
          }
       }

       if (n1==-1)       // not enough infos available, use defaults
       {
          timeSeparator=':';
          is24Hour    =true;
       }
       else
       {
                         // if hour was changed (reduced by 12), then inferring am/pm style
          is24Hour    = (n1>12);
       }

                // set TimeRGF
       TimeRGF.timeSeparator=timeSeparator;
       TimeRGF.is24Hour     =is24Hour;



                // determine and set Gregorian change date in DateRGF
      java.text.SimpleDateFormat sdf_ymd  = new  java.text.SimpleDateFormat("yyyyMMdd"),// YYYYMMDD
                                 sdf_EEEE = new  java.text.SimpleDateFormat("EEEE"),    // dayname
                                 sdf_MMMM = new  java.text.SimpleDateFormat("MMMM"),    // monthname
                                 sdf_ampm = new  java.text.SimpleDateFormat("a");       // am/pm String

      String tsk= sdf_ymd.format(aGregCal.getGregorianChange(),
                                 new StringBuffer(),                // some dummy StringBuffer
                                 new java.text.FieldPosition(1)     // some dummy field
                                ).toString();

      DateRGF aGC = new DateRGF(
                                  toInt(tsk, 0, 4),     // year
                                  toInt(tsk, 4, 2),     // month
                                  toInt(tsk, 6, 2)      // day
                               );

      DateRGF.setGregorianChange(aGC.year, aGC.month, aGC.day);

      java.util.TimeZone.setDefault(tz_gmt);  // set default time-zone to GMT, such that date and
                                              // time does not shift, if run someplace else on this
                                              // world (besides Austria/Europe where this was developed)
                // now get localized strings
                // get and set daynames
      {
          String dn[] = new String [7];

          for (i=0; i<7; i++)
          {
             nowD.setTime(ld + i*DTC.SECONDS_PER_DAY*1000l);
             dn[i]= sdf_EEEE.format(nowD,
                                    new StringBuffer(),                // some dummy StringBuffer
                                    new java.text.FieldPosition(1)     // some dummy field
                                   ).toString();
          }
          DateRGF.setString(DTC.DN, dn);    // set localized day names
      }

                // get and set monthnames
      {
          String mn[] = new String [12];
          long   incr = 31*DTC.SECONDS_PER_DAY*1000l;       // add milliseconds for skipping 31 days

          for (i=0; i<12; i++)
          {
             nowD.setTime(ld + i*incr);
             mn[i]= sdf_MMMM.format(nowD,
                                    new StringBuffer(),                // some dummy StringBuffer
                                    new java.text.FieldPosition(1)     // some dummy field
                                   ).toString();
          }
          DateRGF.setString(DTC.MN, mn);    // set localized month names
      }

                // get and set am/pm strings
      {


          String ap[] = new String [2];
          nowD.setTime(ld2);            // '2001-01-01 08:22:33' to get am-string
          ap[0]=sdf_ampm.format(nowD, new StringBuffer(),                // some dummy StringBuffer
                                   new java.text.FieldPosition(1)     // some dummy field
                                   ).toString();

          nowD.setTime(ld);             // '2001-01-01 18:22:33' to get pm-string
          ap[1]=sdf_ampm.format(nowD, new StringBuffer(),                // some dummy StringBuffer
                                   new java.text.FieldPosition(1)     // some dummy field
                                   ).toString();

          TimeRGF.am_pm_string=ap;              // set localized am/pm strings

      }

      java.util.TimeZone.setDefault(tz_ori);  // reset default time-zone of this machine, so
                                              // everything else runs o.k.

    }





    /**
     * Searches a String and returns the 0-based index of the first digit character, -1 else.
     */
    protected static int findFirstDigit(String str)
    {
        char    c;
        for (int i=0; i<str.length(); i++)      // position on first digit
        {
           c=str.charAt(i);
           if (c>=(char)'0' && c<=(char)'9') return i;
        }

        return -1;      // no digit found
    }


    /**
     * Calculates the int value from a string built of digit characters only.
     *
     * <p>Remark: As Waba does not have <code>String.parseInt()</code> a portable
     * simple method had to be written.
     */
    protected static int toInt(String str, int start, int length)
    {
       int x=0, i,
           top=(start+length),          // get last index position in string
           str_len=str.length();        // get length of string

       top= ( top < str_len ? top : str_len);

       for (i=start; i<top; i++)
       {
          x=x*10;
          switch (str.charAt(i))
          {
             case '1': x++ ; break;
             case '2': x+=2; break;
             case '3': x+=3; break;
             case '4': x+=4; break;
             case '5': x+=5; break;
             case '6': x+=6; break;
             case '7': x+=7; break;
             case '8': x+=8; break;
             case '9': x+=9; break;

             case '0':
             default :
                       break;
          }
       }
       return x;
    }

// <!-- JAVA versions - E N D     -  -->


}




