//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

package org.oorexx.datergf; // "DateTime_RGF"-package

/**
  * Class to implement the time of the day, consisting of the fields {@link #hour},
  * {@link #minute}, {@link #second} and {@link #millis millis(econds)}.
 *
 * <p>
  *
  * <p>
  * This class was created with the
  * Waba family
  * (e.g. <a href="http://www.superwaba.org">http://www.SuperWaba.org</a>)
  * of Java-compatible mobile systems (PDAs, Handies, etc.) in mind (e.g.: does not employ
  * threads, exceptions, long and double).
  *
  * <p><hr>
  * <p>Examples:
  *
  * <pre>
  *      TimeRGF time1, time2, time3, time4;
  *
  *      time =new TimeRGF(23,59,59,999);       // yields: &quot;23:59:59.999&quot;
  *
  *      time1=new TimeRGF(19,29,39);           // yields: &quot;19:29:39&quot;
  *      time2=new TimeRGF( 8, 8, 8);           // yields: &quot;08:08:08&quot;
  *      float diff=time1.subtract(time2);      // yields: &quot;0.47327545&quot;
  *
  *      time3=TimeRGF.valueOf(diff);           // yields: &quot;11:21:31&quot;
  *      time4=TimeRGF.valueOf(-diff);          // yields: &quot;12:38:29&quot;
  *
  *           // if run on "2010-09-22 17:49:01.987" under Waba, then
  *      time4.update();                // yields: '17:49:01.987'
  * </pre>
 *
 * <p>
 * <hr>
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
  *    0.9.2    2001-03-20      - added a millisecond field, changed code appropriately
  *                               (added: MILLIS_FIELD, millis, MILLIS_PER_DAY, MILLIS_PER_HOUR,
  *                                       MILLIS_PER_MINUTE, MILLIS_PER_SECOND,
  *                                       RAW_MILLIS_FIELD, raw_millis
  *                                removed: RAW_SECS_FIELD, raw_secs
  *                             - changed static "time_delimiter" to "timeSeparator"
  *             2001-03-21      - corrected a bug in set()
  *                             - added Serializable (needs only one int to store the TimeRGF)
  *                               taking care with supplied readObject(), writeObject()
  *
  *             2001-04-02      - introduced variant "JAVA" and "WABA"
  *             2001-04-05      - included update() here
  *             2005-12-28      - added Apache license 2.0, put sources with BSF4Rexx
  * </pre>

  * <hr>
  * @author Rony G. Flatscher
  * @version 0.92,  date: 2001-02-08 through 2001-04-02, 2006-01-01
  */


public  class   TimeRGF
        implements      java.lang.Cloneable,
                        java.lang.Comparable,
                        java.io.Serializable
{

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";


    /**
      * Character to be used to delimit time fields in
      * {@link #toString() toString()}.
      * Will get set on class load from preferences.
      * This field can be queried/set directly.
      * The default value is: <code>':'</code>.
      */
    public static char timeSeparator= ':';


    /** An array of two strings representing &quot;am&quot; and &quot;pm&quot;.
      * These two Strings are referred to in {@link #toString() toString()},
      * if {@link #is24Hour is24Hour} is set to <code>false</code>.
      *
      * The default value is (note the leading single blank):
      * {&quot;am&quot;, &quot;pm&quot;}.
      *
      */
    public static String[] am_pm_string={"am", "pm"};


    /**
      * Indicates whether 24 hour clock (military time) or
      * am/pm style is in effect in {@link #toString() toString()}.
      * Will get set on class load from preferences.
      * If am/pm style is in effect, then <kbd>12:00:00 am</kbd> is midnight and
      * <kbd>12:00:00 pm</kbd> is noon.
      * This field can be queried/set directly.
      * The default value is: <code>true</code>.
      */
    public static boolean is24Hour=true;


    /**
      * Indicates whether second portion should be shown in
      * {@link #toString() toString()}.
      * This field can be queried/set directly.
      * The default value is: <code>true</code>.
      */
    public static boolean showSecs=true;




    /**
      * Stores the hour, a value between 0 and 23.
      */
    protected transient int hour;

    /**
      * Stores the minute, a value between 0 and 59.
      */
    protected transient int minute;

    /**
      * Stores the second, a value between 0 and 59.
      */
    protected transient int second;

    /**
      * Stores the milliseconds, a value between 0 and 999.
      */
    protected transient int millis;

    /**
      * Stores the &quot;raw milliseconds&quot;.
      * This is the total of milliseconds for this
      * time, i.e. <kbd>0 &gt;= raw_millis &lt;
      * 86400000 (cf. {@link DTC#MILLIS_PER_DAY})</kbd>.
      * Value is calculated as: <kbd>raw_millis=hour*3600000+minute*60000+second*1000+millis</kbd>.
      *
      * <p>
      * <p>Examples:
      *
      * <kbd>
      * <ul>
      * <li>raw_millis=0<br>
      *     TimeRGF: "00:00:00"
      * </li>
      *
      * <li>raw_millis=86399999<br>
      *     TimeRGF: "23:59:59.999" (or result of: 23*3600000+59*60000+59*1000+999)
      * </li>
      *
      * </ul>
      * </kbd>
      *
      */
    protected transient int raw_millis;



    /**
      * Creates a TimeRGF object with all fields set to 0.
      */
    public TimeRGF()
    {
        hour=minute=second=millis=raw_millis=0;
    }





    /**
      * Creates a TimeRGF from the three integers, representing hour,
      * minute and second. This constructor uses the absolute values
      * of its arguments. If a field has a value, larger than its
      * upper limit (e.g. '25' for hour), than the modulo (using
      * the upper limit) result is used (e.g. '27' for hour yields '3',
      * the result of '27&nbsp;%&nbsp;24').
      *
      * @param hour  an int value between 0 and 23
      * @param minute an int value between 0 and 59
      * @param second an int value between 0 and 59
      */
    public TimeRGF(int hour, int minute, int second)
    {
        this.hour=cI(hour,23);    /* set hour             */
        this.minute=cI(minute,59);/* set minute           */
        this.second=cI(second,59);/* set second           */
        this.millis=0;
                                /* calc value for hour  */
        this.raw_millis= hour*  DTC.MILLIS_PER_HOUR
                        +minute*DTC.MILLIS_PER_MINUTE
                        +second*DTC.MILLIS_PER_SECOND
                        +millis;
    }

    /**
      * Creates a TimeRGF from the three integers, representing hour,
      * minute and second. This constructor uses the absolute values
      * of its arguments. If a field has a value, larger than its
      * upper limit (e.g. '25' for hour), than the modulo (using
      * the upper limit) result is used (e.g. '27' for hour yields '3',
      * the result of '27&nbsp;%&nbsp;24').
      *
      * @param hour  an int value between 0 and 23
      * @param minute an int value between 0 and 59
      * @param second an int value between 0 and 59
      * @param millis an int value between 0 and 999 representing milliseconds
      */
    public TimeRGF(int hour, int minute, int second, int millis)
    {
        this.hour=cI(hour,23);    /* set hour             */
        this.minute=cI(minute,59);/* set minute           */
        this.second=cI(second,59);/* set second           */
        this.millis=cI(millis,999);     /* set millis     */
                                /* calc value for hour  */
        this.raw_millis= hour*  DTC.MILLIS_PER_HOUR
                        +minute*DTC.MILLIS_PER_MINUTE
                        +second*DTC.MILLIS_PER_SECOND
                        +millis;
    }





    /**
      * Creates a TimeRGF object from a fraction of a day.
      * <p>
      * <em>Note:</em>this value is assumed to <em>not</em> contain milliseconds, i.e.
      * the resulting TimeRGF object will have {@link #millis millis} set to 0.
      *
      * <p>
      * To produce the time the fractional part only is used. These are the rules
      * to build the time:
      *
      * <ul>
      * <li>
      * If the fraction is positive, the time is built as the number
      * of hours, minutes and seconds increasing from midnight. E.g. '0.25'
      * (1/4th of a day, i.e. 6 hours) yields the time: '06:00:00'.
      *
      * <li>
      * If the fraction is negative, the time is built as the number
      * of hours, minutes and seconds decreasing from midnight. E.g. '-0.25'
      * (1/4th of a day, i.e. 6 hours) yields the time: '18:00:00'.
      *
      * </ul>
      *
      * <p>Examples:
      * <pre>
      *     TimeRGF t1=TimeRGF.valueOf( 0.47399306f);   // yields: &quot;11:22:33&quot;
      *     TimeRGF t2=TimeRGF.valueOf(-0.47399306f);   // yields: &quot;12:37:27&quot;
      * </pre>
      *
      * @param  fraction represents the time as a fraction of a day.
      * One second is represented as: <kbd>1f/86400</kbd>.
      *
      */
    public static TimeRGF valueOf(float fraction)
    {
        int tmp;
        TimeRGF time=new TimeRGF();
        boolean negative=false;
        if (fraction<0f) {
                negative=true;
                fraction=-fraction;
                };
           /* get second representation    */
        time.raw_millis= (int) (fraction*DTC.SECONDS_PER_DAY+.5f) % DTC.SECONDS_PER_DAY;

        if (negative) time.raw_millis=DTC.SECONDS_PER_DAY-time.raw_millis;  // deduct from midnight
        time.hour    = time.raw_millis/DTC.SECONDS_PER_HOUR;
        tmp          = time.raw_millis%DTC.SECONDS_PER_HOUR;
        time.minute  = tmp/DTC.SECONDS_PER_MINUTE;
        time.second  = tmp%DTC.SECONDS_PER_MINUTE;
        time.millis  = 0;
        time.raw_millis *= DTC.MILLIS_PER_SECOND;
        return time;
    }



    /**
      * Creates a TimeRGF object from an integer, representing the time.
      *
      * <p>Examples:
      *
      * <pre>
      * TimeRGF t1=TimeRGF.valueOf(DTC.ENCODED_AS_INTEGER, 112233);   // yields: &quot;11:22:33&quot;
      * TimeRGF t2=TimeRGF.valueOf(DTC.ENCODED_AS_INTEGER,-112233);   // yields: &quot;12:37:27&quot;
      *
      * TimeRGF t3=TimeRGF.valueOf(DTC.ENCODED_AS_SECONDS,  40953);   // yields: &quot;11:22:33&quot;
      * TimeRGF t4=TimeRGF.valueOf(DTC.ENCODED_AS_SECONDS, -40953);   // yields: &quot;12:37:27&quot;
      *
      * TimeRGF t5=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS ,  40953123);   // yields: &quot;11:22:33.123&quot;
      * TimeRGF t6=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS , -40953123);   // yields: &quot;12:37:26.877&quot;
      *
      * TimeRGF t7=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS ,  86399999);   // yields: &quot;23:59:59.999&quot;
      * </pre>
      *
      * @param intTimeRGF an integer representation of the time.
      *        If negative, the time is constructed by deducting the resulting
      *        time from midnight.
      *
      * @param flag indicates how the time is encoded in <code>intTimeRGF</code>,
      *             can be one of:
      *        <ul>
      *        <li>
      *             {@link DTC#ENCODED_AS_INTEGER}, an integer number in the
      *             form of: <code>{@link #hour}*10000+{@link #minute}*100+{@link #second}</code>
      *        <li>
      *             {@link DTC#ENCODED_AS_MILLIS}, an integer number representing the number of
      *             <em>millis</em>econds,
      *        <li>
      *             {@link DTC#ENCODED_AS_SECONDS} (default),
      *             an integer number representing the number of seconds.
      *
      * @return the appropriate set <code>TimeRGF</code>.
      */
    public static TimeRGF valueOf(int flag, int intTimeRGF)
    {
        int tmp;
        boolean negative=false;
        TimeRGF time=new TimeRGF();

        if (intTimeRGF<0) {
                negative=true;
                intTimeRGF=-intTimeRGF;
                };

        if (flag==DTC.ENCODED_AS_INTEGER)
        {
           time.hour    = intTimeRGF/10000;
           tmp          = intTimeRGF%10000;
           time.minute  = (tmp    /100);
           time.second  = (tmp    %100);
           time.millis  = 0;
           time.raw_millis= ( time.hour*   DTC.SECONDS_PER_HOUR
                             +time.minute* DTC.SECONDS_PER_MINUTE
                             +time.second)*DTC.MILLIS_PER_SECOND;

           if (negative)   // deduct from midnight
           {
               time.raw_millis=DTC.MILLIS_PER_DAY-time.raw_millis;
               time.hour    = time.raw_millis/DTC.MILLIS_PER_HOUR;
               tmp          = time.raw_millis%DTC.MILLIS_PER_HOUR;
               time.minute  =  tmp/DTC.MILLIS_PER_MINUTE;
               time.second  = (tmp%DTC.MILLIS_PER_MINUTE)/DTC.MILLIS_PER_SECOND;
           }
        }

        else    // encoded as milliseconds or seconds (default)
        {

            if (flag == DTC.ENCODED_AS_SECONDS) intTimeRGF *= DTC.MILLIS_PER_SECOND;

            if (intTimeRGF>=DTC.MILLIS_PER_DAY) intTimeRGF %= DTC.MILLIS_PER_DAY;

            if (negative) intTimeRGF=DTC.MILLIS_PER_DAY-intTimeRGF; // deduct from midnight

            time.raw_millis= intTimeRGF;
            time.millis  = intTimeRGF%DTC.MILLIS_PER_SECOND;
            tmp          = intTimeRGF/DTC.MILLIS_PER_SECOND;

            time.hour    = tmp/DTC.SECONDS_PER_HOUR;
            tmp          = tmp%DTC.SECONDS_PER_HOUR;

            time.minute  = tmp/DTC.SECONDS_PER_MINUTE;
            time.second  = tmp%DTC.SECONDS_PER_MINUTE;
        }

        return time;
    }





    /** Create a TimeRGF object from a string containing a TimeRGF
      * encoded <em>exactly</em> according to the present setting of
      * the fields:
      * {@link #timeSeparator timeSeparator},
      * {@link #is24Hour is24Hour} and
      * {@link #am_pm_string am_pm_string}. Only the first seven characters are inspected.
      * If they do not contain at least three digits, then <code>null</code> is returned.
      * <p>
      *
      * <em>Note:</em>this value is assumed to <em>not</em> contain milliseconds, i.e.
      * the resulting TimeRGF object will have {@link #millis millis} set to 0.
      *
      * <p>
      *
      * <p>Examples assuming
      * {@link #timeSeparator timeSeparator} with a value of <code>':'</code>
      * and {@link #is24Hour is24Hour} with a value of <code>true</code>:
      *
      * <pre>
      *      TimeRGF t1=TimeRGF.valueOf("00:01:02");    // yields: '00:01:02'
      *      TimeRGF t2=TimeRGF.valueOf("11:59:59");    // yields: '11:59:59'
      *      TimeRGF t3=TimeRGF.valueOf("12:55:44");    // yields: '12:55:44'
      *      TimeRGF t4=TimeRGF.valueOf("17:01:59");    // yields: '17:01:59'
      *      TimeRGF t5=TimeRGF.valueOf("23:11:22");    // yields: '23:11:22'
      * </pre>
      *
      * <p>Examples assuming
      * {@link #timeSeparator timeSeparator} with a value of <code>':'</code>,
      * {@link #is24Hour is24Hour} with a value of
      * <strong><code>false</code></strong>  and
      * {@link #am_pm_string am_pm_string} with a value of
      * <code>{&quot;am&quot;, &quot;pm&quot;}</code>:
      *
      * <pre>
      *      TimeRGF t1=TimeRGF.valueOf("12:01:02 am"); // yields: '00:01:02'
      *      TimeRGF t2=TimeRGF.valueOf("11:59:59 am"); // yields: '11:59:59'
      *      TimeRGF t3=TimeRGF.valueOf("12:55:44 pm"); // yields: '12:55:44'
      *      TimeRGF t4=TimeRGF.valueOf("05:01:59 pm"); // yields: '17:01:59'
      *      TimeRGF t5=TimeRGF.valueOf("11:11:22 pm"); // yields: '23:11:22'
      * </pre>
      *
      * @param value a string containing a string representation of TimeRGF.
      *
      * @return the appropriate TimeRGF object, or <code>null</code>, if
      *         there are not at least {@link #hour hour} and
      *         {@link #minute minute} available.
      *
      */
    public static TimeRGF valueOf(String value)
    {
        int     i, hour, min, sec, j, k;
        char    tmp;
        String  res="", chunk="";

        for (i=0; i<value.length() && res.length()<7; i++)
        {
           tmp=value.charAt(i);
           if (tmp>=(char)'0' && tmp<=(char)'9')
           {
              chunk=chunk+tmp;
              continue;
           }

                // make sure that there are two digits
           if (chunk.length()==1) res=res+'0';  // add leading 0
           res=res+chunk;
           chunk="";

                // end of string?
           if (tmp!=timeSeparator) break;
        }

        if (chunk.length()==1) res=res+'0';     // add leading 0
        res=res+chunk;                          // add left-over chunk


        if (res.length()<3) return null;        // at least two parts of a time needed

        if (res.length()<5) res=res+"00";       // add seconds (assume hours & minutes in hand)

// <!-- JAVA versions - B E G I N -  -->
        k   =Integer.parseInt(res);             // Java version
// <!-- JAVA versions - E N D     -  -->



        hour=k/10000;
        k   =k%10000;
        min =k/100;
        sec =k%100;


        if (!is24Hour) // we have to look for am/pm string
        {
            boolean bAmPmFound=false;
                // find next non-blank char
            while (value.charAt(i) == ' ' && i < value.length())
            { i++; }

            int len=value.length()-i+1;
            for (j=0; j<2; j++)
            {
               String apm=am_pm_string[j];
               if (apm.length()>len) continue;


                        // extract appropriate length of remaining chars
               chunk=value.substring(i, i+apm.length());

               bAmPmFound=true;
               for (k=0; k<apm.length(); k++)
               {
                  if (apm.charAt(k)!=chunk.charAt(k))
                  {
                     bAmPmFound=false;
                     break;
                  }
               }
               if (bAmPmFound) break;
            }

            if (j==0)           // "am" string found
            {
               if (hour==12) hour=0;    // 12am == 0
            }
            else if (j==1)      // "pm" string found
            {
               if (hour<12)  hour+=12;  // 12pm == 12, 1..11 == 13...23
            }
        }
        return new TimeRGF(hour, min, sec);
    }




    /** Assigns other TimeRGF to this TimeRGF.
      */
      public TimeRGF assign(TimeRGF other)
      {
         this.hour      =other.hour;
         this.minute    =other.minute;
         this.second    =other.second;
         this.millis    =other.millis;
         this.raw_millis=other.raw_millis;
         return this;
      }




    /**
      * Implements the &quot;Comparable&quot; interface.
      *
      * @param otherTimeRGF a TimeRGF
      * @return <kbd>-1</kbd> if this TimeRGF is earlier (smaller) than <kbd>otherTimeRGF</kbd>,
      *         <kbd>0</kbd> if both times are equal,
      *         <kbd>+1</kbd> if this TimeRGF is later (greater) than <kbd>otherTimeRGF</kbd>
      *
      */
    public int compareTo(Object otherTimeRGF)
    {
        if (this.raw_millis <  ((TimeRGF) otherTimeRGF).raw_millis)  {return -1;}
        if (this.raw_millis == ((TimeRGF) otherTimeRGF).raw_millis)  {return  0;}
        return +1;
    }



    /**
      * Implements the  &quot;Clonable&quot; interface.
      *
      * @return A newly created TimeRGF object with all fields set to this TimeRGF object.
      */
    public Object clone()
    {
        TimeRGF tmp=new TimeRGF();
        tmp.assign(this);
        return tmp;
    }



    /**
      * Implements the &quot;Comparator.equals(Object obj)&quot; interface.
      *
      * @param otherTimeRGF a TimeRGF
      *
      * @return <kbd>true</kbd> if Times are equal (have both the same values
      * in their appropriate time fields), <kbd>false</kbd> else.
      */
    public boolean equals(TimeRGF otherTimeRGF)
    {
        return (this.raw_millis==otherTimeRGF.raw_millis);
    }


    /**
      * Allows to retrieve time fields and a rendering of TimeRGF to integer.
      *
      * <p>Examples:
      *
      * <pre>
      *     TimeRGF t1=new TimeRGF(11,22,33);           // yields: &quot;11:22:33&quot;
      *
      *     int     hour  =t1.get(DTC.HOUR_FIELD),          // yields: &quot;11&quot;
      *             minute=t1.get(MINUTE_FIELD),        // yields: &quot;22&quot;
      *             sec   =t1.get(SECOND_FIELD);        // yields: &quot;33&quot;
      *
      *     int     a1=t1.get(ENCODED_AS_INTEGER);      // yields: &quot;112233&quot;
      *
      *     int     a2=t1.get(ENCODED_AS_SECONDS);      // yields: &quot;40953&quot;
      *     int     a3=t1.get(RAW_MILLIS_FIELD);        // yields: &quot;40953000&quot;
      * </pre>

      * @param flag one of {@link DTC#HOUR_FIELD},
      * {@link DTC#MINUTE_FIELD},
      * {@link DTC#SECOND_FIELD},
      * {@link DTC#MILLIS_FIELD},
      * {@link DTC#RAW_MILLIS_FIELD}, same as:
      * {@link DTC#ENCODED_AS_MILLIS},
      * {@link DTC#ENCODED_AS_SECONDS},
      * {@link DTC#ENCODED_AS_INTEGER}
      * (<code>hour*10000+minute*100+second</code>).
      *
      * @return the appropriate value, or <kbd>-1</kbd>, if an unknown flag
      *         was received.
      */
    public int get(int flag)
    {
       switch (flag)
       {
           case DTC.HOUR_FIELD:           return hour;
           case DTC.MINUTE_FIELD:         return minute;
           case DTC.SECOND_FIELD:         return second;
           case DTC.MILLIS_FIELD:         return millis;

           case DTC.ENCODED_AS_SECONDS:   return raw_millis/DTC.MILLIS_PER_SECOND;

           case DTC.ENCODED_AS_MILLIS:
           case DTC.RAW_MILLIS_FIELD:     return raw_millis;

           case DTC.ENCODED_AS_INTEGER:   return hour*10000+minute*100+second;

           default:             return -1;
       }
    }


    /**
      * Allows to set the indicated time portion.
      *
      * <p>Examples:
      *
      * <pre>
      *     TimeRGF t1=new TimeRGF(23,59,59);   // yields: &quot;23:59:59&quot;
      *
      *     t1.set(DTC.HOUR_FIELD, 11);             // yields: &quot;11:59:59&quot;
      *     t1.set(MINUTE_FIELD, 22);           // yields: &quot;11:22:59&quot;
      *     t1.set(SECOND_FIELD, 33);           // yields: &quot;11:22:33&quot;
      *
      *     t1.set(ENCODED_AS_INTEGER, 10001);  // yields: &quot;01:00:01&quot;
      *
      *     t1.set(ENCODED_AS_SECONDS, 40953);  // yields: &quot;11:22:33&quot;
      *     t1.set(RAW_MILLIS_FIELD, 40954000); // yields: &quot;11:22:34&quot;
      * </pre>
      *
      *
      * @param flag one of {@link DTC#HOUR_FIELD},
      * {@link DTC#MINUTE_FIELD},
      * {@link DTC#SECOND_FIELD},
      * {@link DTC#MILLIS_FIELD},
      * {@link DTC#RAW_MILLIS_FIELD}, same as:
      * {@link DTC#ENCODED_AS_MILLIS},
      * {@link DTC#ENCODED_AS_SECONDS},
      * {@link DTC#ENCODED_AS_INTEGER}.
      *
      * @param new_value a value appropriate for the fields to be set
      * (cf. {@link #hour hour},
      *      {@link #minute minute},
      *      {@link #second second},
      *      {@link #millis millis},
      *      {@link #raw_millis raw_millis}).
      *
      */
    public TimeRGF set(int flag, int new_value)
    {
       boolean negative=(new_value<0);
       if (negative) new_value= -new_value;

       switch (flag)
       {
           case DTC.HOUR_FIELD:           hour    = cI(new_value,23);
                                break;

           case DTC.MINUTE_FIELD:         minute  = cI(new_value,59);
                                break;

           case DTC.SECOND_FIELD:         second  = cI(new_value,59);
                                break;

           case DTC.MILLIS_FIELD:         millis  = cI(new_value,999);
                                break;

           case DTC.ENCODED_AS_INTEGER:
                                {
                                    hour     = cI(new_value/10000, 23);
                                    int tmp  = new_value%10000;
                                    minute   = cI(tmp/100,59);
                                    second   = cI(tmp%100,59);
                                    millis   = 0;
                                }
                                break;


           case DTC.RAW_MILLIS_FIELD:
           case DTC.ENCODED_AS_MILLIS:
                               raw_millis = cI(new_value, (DTC.MILLIS_PER_DAY-1) );

           case DTC.ENCODED_AS_SECONDS:
                               if (flag==DTC.ENCODED_AS_SECONDS)
                               {
                                   raw_millis=cI(new_value,(DTC.SECONDS_PER_DAY-1))*DTC.MILLIS_PER_SECOND;
                               }

                               if (negative) { raw_millis = DTC.MILLIS_PER_DAY-raw_millis;}   // deduct from midnight

                               {
                                    millis   =       raw_millis % DTC.MILLIS_PER_SECOND;
                                    int tmp_secs   = raw_millis / DTC.MILLIS_PER_SECOND;

                                    hour     = tmp_secs / DTC.SECONDS_PER_HOUR;
                                    tmp_secs = tmp_secs % DTC.SECONDS_PER_HOUR;

                                    minute   = tmp_secs / DTC.SECONDS_PER_MINUTE;
                                    second   = tmp_secs % DTC.SECONDS_PER_MINUTE;
                                }
                                return this;

           default:             return this;
       }
       raw_millis= hour*  DTC.MILLIS_PER_HOUR
                  +minute*DTC.MILLIS_PER_MINUTE
                  +second*DTC.MILLIS_PER_SECOND
                  +millis;
       return this;
    }


    /**
      * Subtracts otherTimeRGF and returns the difference as a fraction.
      * <p>
      * <em>Note:</em>the calculation is carried out <em>without</em>
      * using the {@link #millis millis} field (as if the {@link #millis millis} field
      * is set to <code>0</code>. Reason: to cater for
      * the limited precision of digits available with a <code>float</code>.
      *
      */
      public float subtract(TimeRGF otherTimeRGF)
      {
          return (float) ((this.raw_millis        -this.millis) -
                          (otherTimeRGF.raw_millis-otherTimeRGF.millis))
                         /DTC.MILLIS_PER_DAY;
      }



    /**
      *
      * Renders TimeRGF as a fraction of a day.
      *
      * <p>
      * <em>Note:</em>the calculation is carried out <em>without</em>
      * using the {@link #millis millis} field (as if the {@link #millis millis} field
      * is set to <code>0</code>. Reason: to cater for
      * the limited precision of digits available with a <code>float</code>.
      *
      * This is the result of:
      * <kbd>({@link #raw_millis}-{@link #millis}) / {@link DTC#MILLIS_PER_DAY}</kbd>.
      *
      * <p>Example:
      *
      * <pre>
      *     TimeRGF t1=new TimeRGF(11,22,33);           // yields: &quot;11:22:33&quot;
      *     float   f1=t1.toFloat();                    // yields: &quot;0.47399306&quot;
      * </pre>
      *
      * @return a float representing the time as a fraction of a day.
      *
      */
    public float toFloat()
    {
        return (float) (raw_millis-millis) / DTC.MILLIS_PER_DAY;
    }



    /**
      * Renders TimeRGF as a String.
      * The formatting is done according to the values of the fields:
      * {@link #timeSeparator timeSeparator},
      * {@link #showSecs showSecs},
      * {@link #is24Hour is24Hour} and
      * {@link #am_pm_string am_pm_string} (with a preceeding blank).
      *
      * <p>
      * <em>Hint:</em> The {@link #millis} field is never used by this method.
      *
      *
      * <p>Examples:
      *
      * <pre>
      *     TimeRGF t1=new TimeRGF(14,23,56);   // yields: &quot;14:23:56&quot;
      *
      *     TimeRGF.is24Hour=true;             // changing global setting
      *     TimeRGF.showSecs =true;             // changing global setting
      *     String  s1=t1.toString();           // yields: &quot;<strong>14:23:56</strong>&quot;
      *
      *     TimeRGF.is24Hour=false;            // changing global setting
      *     TimeRGF.showSecs =false;            // changing global setting
      *     s1=t1.toString();                   // yields: &quot;<strong>2:23 pm</strong>&quot;
      * </pre>
      *
      * @return a string representing the time of a day.
      *
      * @see DateFormatRGF#format(java.lang.String fmt, java.lang.Object obj) for
      *      formatting a TimeRGF object freely (using <em>all</em> fields)
      */
    public String toString()
    {
       if (!is24Hour) {int idx=(hour>11?1:0);     /* determine "am" or "pm" string */

                         return ( ri((hour==0?12:(hour>12?hour-12:hour)))
                                +timeSeparator
                                + ri(minute)
                                + (showSecs?(timeSeparator+ri(second)):"")
                                +" "            // insert space
                                +am_pm_string[idx])
                                ;}

       else {            return   ri(hour)
                                +timeSeparator
                                + ri(minute)
                                + (showSecs?(timeSeparator+ri(second)):"")
                                ;}
    }


    /**
      * Turn an integer into a two character String, right-adjusted with a
      * leading <kbd>0</kbd> if necessary.
      *
      * @param tmp an integer to be right adjusted in a two character String.
      *
      * @return a String of two characters.
      *
      */
    private static String ri(int tmp)
    {
       if (tmp<10) { return "0"+tmp;}
       else        { return "" +tmp;}
    }


    /**
      * Makes sure that an integer value falls into the range
      * <kbd>0</kbd> and <kbd>upperLimit</kbd>. If the value is negative
      * it is turned positive. If the value is beyond the <kbd>upperLimit</kbd>
      * then <kbd>value%(upperLimit+1)</kbd> is returned.
      *
      * @param value the integer value to be worked on.
      * @param upperLimit the upper bound of the integer.
      *
      * @return an integer within the range of
      *         <kbd>0</kbd> and <kbd>upperLimit</kbd>.
      *
      */
    private static int cI(int value, int upperLimit)
    {
        if (value<0)            {value=-value;}
        if (value>upperLimit)   {return (value%(upperLimit+1));}
        return value;
    }


    /**
      * Overrides default java.io.Serializable by merely storing {@link #raw_millis}.
      */
 private void writeObject(java.io.ObjectOutputStream out)
     throws java.io.IOException
    {
       out.writeInt(this.raw_millis);
    }


    /**
      * Overrides default java.io.Serializable by merely retrieving {@link #raw_millis}.
      * Uses {@link #set(int flag, int new_value) TimeRGF.set(DTC.ENCODED_AS_MILLIS, raw_millis)} to re-create the appropriate
      * TimeRGF object.
      */
 private void readObject(java.io.ObjectInputStream in)
     throws java.io.IOException, ClassNotFoundException
    {
       this.set(DTC.ENCODED_AS_MILLIS, in.readInt());    // create the TimeRGF object from raw_millis
    }




// <!-- JAVA versions - B E G I N -  -->
  /** This method sets the TimeRGF object to
    * to the actual local time of the system.
    * For this purpose class <em>java.util.GregorianCalendar</em>
    * is instantiated.
    *
    * <p>Examples:
    * <pre>
    *     TimeRGF t =new TimeRGF(); // yields: '00:00:00'
    *
    *           // if run at "17:49:01.987", then
    *     t.update();               // yields: '17:49:01.987'
    * </pre>
    */
  public TimeRGF update()
  {
                // Java runtime, get local date and time
      java.util.GregorianCalendar nowGC=new java.util.GregorianCalendar();

      this.hour      = nowGC.get(java.util.Calendar.HOUR_OF_DAY);
      this.minute    = nowGC.get(java.util.Calendar.MINUTE);
      this.second    = nowGC.get(java.util.Calendar.SECOND);
      this.millis    = nowGC.get(java.util.Calendar.MILLISECOND);
      this.raw_millis= this.hour  *DTC.MILLIS_PER_HOUR
                      +this.minute*DTC.MILLIS_PER_MINUTE
                      +this.second*DTC.MILLIS_PER_SECOND
                      +this.millis
                      ;
      return this;
  }

// <!-- JAVA versions - E N D     -  -->



}




