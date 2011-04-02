//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

package org.oorexx.datergf; // "DateTime_RGF"-package

/**
  * {@link org.oorexx.datergf.DateTimeRGF} implements a class consisting of a
  * {@link org.oorexx.datergf.DateRGF} and
  * {@link org.oorexx.datergf.TimeRGF}.
 *
 * <p>

  * <p>This class has a Waba and a Java version, due to the following
  * methods, which are implemented depending on the runtime environment:
  *
  * <p>
        <table border="1" cellpadding="3" cellspacing="0">
        <tr class="TableHeadingColor">
        <td colspan="2" align="center"> Implemented?
        <td> &nbsp;
        <td> &nbsp;

        <tr class="TableHeadingColor">
        <td align="center">     Java
        <td align="center">     Waba
        <td>                    Method name
        <td>                    Short Description

        <tr class="TableRowColor"><!-- table row -->
        <td align="center">     yes
        <td align="center">     yes
        <td align="left">       public static void update(Object&nbsp;obj)
        <td align="left">       Updates a {@link org.oorexx.datergf.DateTimeRGF} object,
                                a {@link org.oorexx.datergf.DateRGF} object or
                                a {@link org.oorexx.datergf.TimeRGF} object to the
                                local time of the machine.
        </table>


    <hr>
    <p>
    This version was created with Waba
  * (e.g. <a href="http://www.superwaba.org">http://www.SuperWaba.org</a>)
  * family of Java-compatible mobile systems (PDAs, Handies, etc.)
  * in mind. Hence this package does not use threads or exceptions.

  * The Waba version does not use the datatypes double and long.
  *
  * <br>(Though there are two methods in this class employing long,
  * meant for running under virtual machines offering that datatype:
  * {@link #toLong(int epoch)} and
  * {@link #valueOf(int epoch, long millis)}.)
  * The original Waba as well as many other Java VMs for small devices
  * do not support datatype long.
  *
  *
    <p>
    Some of the available operations are:

    <ul>
    <p><li>Calculating the difference in days between two DateTimeRGFs
        ({@link #subtract(DateTimeRGF other)
                 subtract(DateTimeRGF other)}),

    <p><li>creating a DateTimeRGF object from <code><em>unsigned</em></code>
        <code>int</code> (yep!) on Waba (as well as creating them) which
        allows one e.g. to use/create the Palm <code>unsigned int</code> values directly
        from Waba.
    </ul>


  * <p><hr>
    <p>
    Examples:

  * <pre>
  *      DateTimeRGF dt1, dt2;
  *      dt1=new DateTimeRGF(2001,1,1,0,0,0);      // yields: 2001-01-01 00:00:00
  *      dt2=new DateTimeRGF(2005,12,31,11,59,59); // yields: 2005-12-31 11:59:59
  *
  *      float  diff1=dt2.subtract(dt1);           // yields: &quot;1825.5&quot; (1825 and 1/2 days)
  *      double diff2=dt2.subtractDouble(dt1);     // yields: &quot;1825.499988436699&quot;

  *                     // if running under Waba yields actual date and time:
  *      dt2.update();
  *      diff1=dt2.subtract(dt1);       // yields: number of days and time since the
  *                                     //         beginning of the new millenium
  * </pre>
 *
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
  *    0.9.2    2001-03-21      - catering for changes in TimeRGF
  *                             - deleted float/double subtract(), and their add()-counterparts
  *                             - new subtract (and add()) working on a two-element int-array) under
  *                               Waba exact to the millisecond (now using int only)!
  *                             - corrected a bug with toLong()
  *
  *                             - added the Serializable interface; DateRGF(), TimeRGF() have
  *                               in addition readObject() and writeObject() implemented
  *
  *             2001-04-02      - introduced variant "JAVA" and "WABA"
  *             2005-12-28      - added Apache license 2.0, put sources with BSF4Rexx
  * </pre>
  *

    <hr>
  * <p>
  *
  * @author Rony G. Flatscher
  * @version 0.92,  date: 2001-02-08 through 2001-04-02, 2006-01-01
  *
  *
  */


public  class   DateTimeRGF
        implements      java.lang.Cloneable,
                        java.lang.Comparable,
                        java.io.Serializable
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";


        /**
          * DateRGF field to store the date portion.
          */
          public DateRGF date = null;

        /**
          * TimeRGF field to store the time portion.
          */
          public TimeRGF time = null;


   /** Creates a DateTimeRGF object set to "0001-01-01 00:00:00.000".
     */
  public DateTimeRGF()
   {
      date=new DateRGF();
      time=new TimeRGF();
   }


   /** Creates a DateTimeRGF object set to the arguments <code>date</code> and
     * <code>time</code>.
     */
  public DateTimeRGF(DateRGF date, TimeRGF time)
   {
      this.date=date;
      this.time=time;
   }


  /** Creates a DateTimeRGF object set to the values from the arguments.
    *
    * @param year a value between 1 and 9999.
    * @param month a value between 1 and 12.
    * @param day a value between 1 and the maximum number of days of the appropriate month.
    * @param hour a value between 0 and 23.
    * @param minute a value between 0 and 59.
    * @param second a value between 0 and 59.
    */
  public DateTimeRGF(int year, int month, int day, int hour, int minute, int second)
  {
     date=new DateRGF(year, month, day);
     time=new TimeRGF(hour, minute, second);
  }


  /** Creates a DateTimeRGF object set to the values from the arguments.
    *
    * @param year a value between 1 and 9999.
    * @param month a value between 1 and 12.
    * @param day a value between 1 and the maximum number of days of the appropriate month.
    * @param hour a value between 0 and 23.
    * @param minute a value between 0 and 59.
    * @param second a value between 0 and 59.
    * @param millis a value between 0 and 999.
    */
  public DateTimeRGF(int year, int month, int day, int hour, int minute, int second, int millis)
  {
     date=new DateRGF(year, month, day);
     time=new TimeRGF(hour, minute, second, millis);
  }



  /**
    * Adds the number of days to the date field and the
    * number of milliseconds to the time field of this DateTimeRGF object.
    *
    * <p>Works on any Java-VM, even ones
    * with no implementations of the <code>long</code> or <code>double</code>
    * datatype, like some for
    * for PDAs (like the classic Waba); therefore <em>there may be
    * rounding errors</em>, if the integer part (representing whole days)
    * is too large.
    *
    *
  * <pre>
  *                             // create a DateTimeRGF object with a
  *                             // value of: "2010-01-12 14:05:47.123"
  *      DateTimeRGF dt=new DateTimeRGF(2010,01,12,14,05,47,123);
  *
  *      dt.add(1, 64800000);    // add 1 3/4 days == 1 day, 18 hours (= 64800000 millis)
  *                             // "dt" now: "2010-01-14 08:05:47.123"
  * </pre>
    *
    * @param days number of days to add to DateTimeRGF.
    *
    * @param millis number of milliseconds to add to DateTimeRGF.
    */
  public DateTimeRGF add(int days, int millis)
  {
       millis = this.time.raw_millis+millis;
       days   = days + (millis/DTC.MILLIS_PER_DAY); // more than a day?

       this.date.add(days);

       this.time.set(DTC.ENCODED_AS_MILLIS, millis);
       return this;
  }




  /**
    * Subtracts a DateTimeRGF object from this one.
    *
    * <p>
    * Works on any Java-VM, even ones
    * with no implementations of the <code>double</code> datatype, like some for
    * for PDAs (like the classic Waba); therefore <em>there may be
    * rounding errors</em>, if the integer part (representing whole days)
    * is too large.
    *
    *
    *<p>Example:
  * <pre>
  *                             // create a DateTimeRGF object with a
  *                             // value of: "2010-01-12 14:05:47"
  *      DateTimeRGF dt1=new DateTimeRGF(2010,01,12,14,05,47);
  *
  *                             // create a DateTimeRGF object with a
  *                             // value of: "2010-01-14 08:05:47"
  *      DateTimeRGF dt2=new DateTimeRGF(2010,01,14,8,05,47);
  *
  *      int []      diff=dt2.subtract(dt1);    // yields: {1, 64800000};
  *
  * </pre>
    * @param other the DateTimeRGF object to subtract.
    *
    * @return an <code>int</code> array with two elements
    *         <ul>
    *         <li>at index '0': the difference in number of days,
    *         <li>at index '1': the difference in milliseconds.
    *         </ul>
    */
   public int [] subtract(DateTimeRGF other)
   {
      int diff1 = this.date.subtract(other.date),
          diff2 = this.time.raw_millis-other.time.raw_millis;


      if (diff1 > 0 && diff2 < 0 )      // make diff2 positive
      {
         diff1--;                               // whole days
         diff2 += DTC.MILLIS_PER_DAY;       // part of day
      }
      else if (diff1 < 0 && diff2 > 0)  // make diff2 negative
      {
         diff2 -= DTC.MILLIS_PER_DAY;       // part of day
      }

      return new int[] {diff1, diff2};
   }




  /** Assigns otherDateTimeRGF to this DateTimeRGF.
    *
    */
  public DateTimeRGF assign(DateTimeRGF otherDateTimeRGF)
  {
      this.date.assign(otherDateTimeRGF.date);
      this.time.assign(otherDateTimeRGF.time);
      return this;
  }



  /**
    * Implements the &quot;Comparable&quot; interface.
    *
    * @param otherDateTimeRGF a DateTimeRGF
    * @return <kbd>-1</kbd> if this DateTimeRGF is earlier (smaller) than <kbd>otherDateTimeRGF</kbd>,
    *         <kbd>0</kbd> if both times are equal,
    *         <kbd>+1</kbd> if this DateTimeRGF is later (greater) than <kbd>otherDateTimeRGF</kbd>
    *
    */
  public int compareTo(Object otherDateTimeRGF)
  {
      int ret=this.date.compareTo(((DateTimeRGF)otherDateTimeRGF).date);
      if (ret==0) ret=this.time.compareTo(((DateTimeRGF) otherDateTimeRGF).time);
      return ret;
  }




    /**
      * Implements the  &quot;Clonable&quot; interface.
      *
      * @return a newly created DateTimeRGF object with all fields set to this DateTimeRGF object.
      */
    public Object clone()
    {
        DateTimeRGF tmp = new DateTimeRGF();
        tmp.date=(DateRGF)this.date.clone();
        tmp.time=(TimeRGF)this.time.clone();
        return tmp;
    }



    /**
      * Implements the &quot;Comparator.equals(Object obj)&quot; interface.
      *
      * @param otherDateTimeRGF a DateTimeRGF
      *
      * @return <kbd>true</kbd> if DateTimes are equal (have both the
        same value in the fields {@link #date date} and {@link #time time}),
        <kbd>false</kbd> else.
      */
    public boolean equals(DateTimeRGF otherDateTimeRGF)
    {
        return (this.date.equals(otherDateTimeRGF.date) &&
                this.time.equals(otherDateTimeRGF.time));
    }





   /** Render the DateTimeRGF object to a string.
     */
  public String toString()
  {
      return date.toString()+" "+time.toString();
  }



  /** Renders the DateTimeRGF object into a long, encoded as <em>milli</em>seconds since/until
    * the given epoch. The 'epoch' is the basedate (at midnight) used to calculate the number of
    * seconds from/to this DateTimeRGF object.
    *
    * <p>Does <em>not</em> Work on
    * Java-VMs with no implementation of the <code>long</code> datatype
    * (e.g. early versions of Waba for PDAs).
    *
    * <p><em>Remark:</em> If your system is using seconds, then you need to
    * mulitply the second value by <code>1000</code> in order to arrive at the
    * needed millisecond value.
    *
  * <pre>
  *      DateTimeRGF dt=new DateTimeRGF(2010,1,12, 14,5,47);
  *                     // "dt" is now: "2010-01-12 14:05:47"
  *
  *      long secs_long=dt.toLong(DTC.EPOCH_PALM);
  *                     // "secs_long" is now: "3.346.149.947" seconds
  *                     //                     since DateRGF.EPOCH_PALM
  * </pre>
    *
    * @param epoch one of:
        {@link DTC#EPOCH_MACINTOSH},
        {@link DTC#EPOCH_PALM},
        {@link DTC#EPOCH_JAVA},
        {@link DTC#EPOCH_DOS},
        {@link DTC#EPOCH_WIN32},
        {@link DTC#EPOCH_MJD}.

        <p>
        It is possible to use an integer encoded date (a positive number in
        the form of: <code>year*10000+month*100+day</code>) as the
        <code>epoch</code>. In such a case that date (starting at midnight)
        is used as the epoch.

        If an illegal epoch is given, the default epoch of
           {@link DateRGF#getDefaultEpochDate()} is used.
    *
    */
  public long toLong(int epoch)
  {
     DateRGF base=null;

     if (epoch>0) base=DateRGF.valueOf(DTC.ENCODED_AS_INTEGER, epoch);
     else         base=DateRGF.valueOf(epoch, 0);

     int days   =this.date.subtract(base),
         millis =this.time.raw_millis;

     if (days<0 && millis>0) days--;

     return ( (long) days*DTC.MILLIS_PER_DAY + millis);
  }





  /** Creates a DateTimeRGF object from the number of <em>milli</em>seconds since/to the given epoch.
    *
    * <p>Does <em>not</em> Work on
    * Java-VMs with no implementation of the <code>long</code> datatype
    * (e.g. early versions of Waba for PDAs).
    *
    * <p><em>Remark:</em> If your system is using seconds, then you need to
    * mulitply the second value by <code>1000</code> in order to arrive at the
    * needed millisecond value.
    *
  * <pre>
  *                     // 3.346.149.947 seconds since DateRGF.EPOCH_PALM,
  *                     // which is exactly: "2010-01-12 14:05:47"
  *      long secs_long=3346149947l;
  *
  *      DateTimeRGF dt=valueOf(DTC.EPOCH_PALM, secs_long);
  *                     // "dt" is now: "2010-01-12 14:05:47"

  * </pre>
    *
    * @param epoch one of:
        {@link DTC#EPOCH_MACINTOSH},
        {@link DTC#EPOCH_PALM},
        {@link DTC#EPOCH_JAVA},
        {@link DTC#EPOCH_DOS},
        {@link DTC#EPOCH_WIN32},
        {@link DTC#EPOCH_MJD}.
        If an illegal epoch is given, the default epoch of
           {@link DateRGF#getDefaultEpochDate()} is used.
    *
    * @param millis number of <em>milli</em>seconds since/to the epoch date (midnight).
    *
    */
  public static DateTimeRGF valueOf(int epoch, long millis)
  {
     DateTimeRGF tmp=new DateTimeRGF (DateRGF.valueOf(epoch, 0),
                                      new TimeRGF()
                                     );

     tmp.add((int)(millis/DTC.MILLIS_PER_DAY),      // days
             (int)(millis%DTC.MILLIS_PER_DAY));     // millis

     return tmp;
  }






  /** Renders the DateTimeRGF object into an
    * <em><code><b<unsigned</b> int</code></em>, encoded as seconds since/until
    * the given epoch. The 'epoch' is the basedate used to calculate the number of
    * seconds from/to this DateTimeRGF object.
    *
    * <p>Use this returned <em><code>unsigned int</code></em> as a parameter for
    * {@link #valueOf(int unsigned_int, int epoch) valueOf(int unsigned_int, int epoch)}.
    *
    * <p><em>Remark:</em> An int - if regarded as an unsigned int - can
    * store at most 49710 days and 23295 secs (or a value range between 0 and
    * 4.294.967.295).
    * Therefore, one can only
    * cover the date/time-range between the epoch date (midnight) and
    * a little bit more than 135 years after it. E.g. for the
    * {@link DTC#EPOCH_PALM} the maximum
    * date/time value storable is exactly: '2040-02-06 06:28:15' (an unsigned integer
    * with all bits set is represented as <code>-1</code> if interpreted as a
    * signed integer).
    *
    * <p>This was written to support the Palm operating system which
    *    uses an <em>unsigned int</em> representing the seconds elapsed since
    *    its epoch (1904-01-01, midnight), cf.
    *    {@link DTC#EPOCH_PALM}.
    *
    * <pre>
    *           // create "dt" and set it to: "2010-01-12 14:05:47"
    *      DateTimeRGF dt=new DateTimeRGF(2010,1,12, 14,5,47);
    *
    *      int secs_int=dt.toInt(DTC.EPOCH_PALM);
    *           // "secs_int" is now: "-948.817.349", which is equivelant to
    *           // an <em>unsigned</em> int with a value of '3.346.149.947'
    *           // (seconds elapsed since DateRGF.EPOCH_PALM)
    * </pre>
    *
    * @param epoch one of:
    *   {@link DTC#EPOCH_MACINTOSH},
    *   {@link DTC#EPOCH_PALM},
    *   {@link DTC#EPOCH_JAVA},
    *   {@link DTC#EPOCH_DOS},
    *   {@link DTC#EPOCH_WIN32},
    *   {@link DTC#EPOCH_MJD}.
    *
    *   <p>
    *   It is possible to use an integer encoded date (a positive number in
    *   the form of: <code>year*10000+month*100+day</code>) as the
    *   <code>epoch</code>. In such a case that date (starting at midnight)
    *   is used as the epoch.
    *
    *   If an illegal epoch is given, the default epoch of
    *      {@link DateRGF#getDefaultEpochDate()} is used.
    *
    * @return an (unsigned!) <code>int</code> representing the seconds to add
    *         to the epoch in order
    *         to arrive at this DateTimeRGF; if this DateTimeRGF is before
    *         the epoch or after <code>epoch+49710days+23295secs</code> then
    *         0 is returned.
    *
    */
  public int toInt(int epoch)
  {
     DateRGF base=DateRGF.valueOf(epoch, 0);

     int days   =this.date.subtract(base);
     if (days<0) return 0;     // date must be after epoch

//     int seconds=this.time.get(TimeRGF.RAW_SECS_FIELD);
//     int seconds=this.time.raw_secs;
     int seconds=this.time.raw_millis/DTC.MILLIS_PER_SECOND;
     if (days>49710 || (days==49710 && seconds>23295)) return 0; // beyond 2**32 bit range

     return (   (((days*5400) + (seconds>>4))<<4) | (seconds&0x0f) );
  }



  /** Creates a DateTimeRGF from the unsigned int relative to
    * the given epoch.
    *
    * <p>This was written to support the Palm operating system which
    *    uses an <em>unsigned int</em> representing the seconds elapsed since
    *    its epoch (1904-01-01, midnight), cf.
    *    {@link DTC#EPOCH_PALM}.
    *
    * <p>The algorithm uses (signed) <code>int</code> only, such that this method can be used
    *    on Java-bytecode systems, which do not support long (or double),
    *    e.g. PDA's.
    *
    * <pre>
    *                     // The date and time "2010-01-12 14:05:47" lies
    *                     // 3.346.149.947 seconds after DateRGF.EPOCH_PALM;
    *                     // if stored as an 'unsigned int' but referred to as
    *                     // a 'signed int', one needs to use the value
    *                     // '-948.817.349' in order to set the appropriate
    *                     // bits.
    *      int secs_int=-948817349;
    *
    *      DateTimeRGF dt=valueOf(DTC.EPOCH_PALM, secs_int);
    *                     // "dt" is now: "2010-01-12 14:05:47"
    * </pre>
    *
    *
    * @param epoch one of:
    *   {@link DTC#EPOCH_MACINTOSH},
    *   {@link DTC#EPOCH_PALM},
    *   {@link DTC#EPOCH_JAVA},
    *   {@link DTC#EPOCH_DOS},
    *   {@link DTC#EPOCH_WIN32},
    *   {@link DTC#EPOCH_MJD}.
    *
    * @param unsigned_seconds an <code>int</code> which gets interpreted as an
    *        <code><em>unsigned int</em></code> (cf.
    *        {@link #toInt(int epoch) toInt(int epoch)}).
    *
    *   <p>
    *   It is possible to use an integer encoded date (a positive number in
    *   the form of: <code>year*10000+month*100+day</code>) as the
    *   <code>epoch</code>. In such a case that date (starting at midnight)
    *   is used as the epoch.
    *
    *   If an illegal epoch is given, the default epoch of
    *      {@link DateRGF#getDefaultEpochDate()} is used.
    *
    * @return a DateTimeRGF object set according to the given epoch.
    *
    */
  public static DateTimeRGF valueOf(int epoch, int unsigned_seconds)
  {
     int days, seconds;

     if (unsigned_seconds<0)        // larger values than positive numbers!
     {
         int x, y;
         x      = unsigned_seconds    & 0x0000ffff;
         y      =(unsigned_seconds>>4)& 0x0fffffff;
         days   =   (y|x>>4)/5400;
         seconds=( ((y|x>>4)%5400)<<4|(unsigned_seconds&0x0000000f) );
     }
     else
     {
         seconds=unsigned_seconds%86400;
         days   =((unsigned_seconds>>7)&0x1fffffff)/675;
     }

     DateRGF d=DateRGF.valueOf(epoch, 0);
     d.add(days);

     return new DateTimeRGF(d, TimeRGF.valueOf(DTC.ENCODED_AS_SECONDS, seconds));
  }





// <!-- JAVA versions - B E G I N -  -->
  /** Updates the DateTimeRGF object to the actual local
    * date and time of the machine.
    * For this purpose class <em>java.util.GregorianCalendar</em>
    * is instantiated.
    *
    * <p>This method guarantees, that for both, the DateRGF and
    * the TimeRGF object, the <code>same</code> timestamp is used. Otherwise it is thinkable,
    * that while the TimeRGF object is updated the day rolls over and the update of the DateRGF
    * object reflects a different day (and vice versa).
    *
    * <p>Examples:
    * <pre>
    *     DateTimeRGF dt=new DateTimeRGF();     // yields: '0001-01-01 00:00:00.000'
    *
    *           // if run on "2010-09-22" at "17:49:01.987", then
    *     dt.update();  // yields: '2010-09-22 17:49:01.987'
    * </pre>
    */
  public DateTimeRGF update()
  {
                // Java runtime, get local date and time
      java.util.GregorianCalendar nowGC=new java.util.GregorianCalendar();

                /* protected access to save method calls        */
      this.date.year =nowGC.get(java.util.Calendar.YEAR );
      this.date.month=nowGC.get(java.util.Calendar.MONTH)+1;
      this.date.day  =nowGC.get(java.util.Calendar.DAY_OF_MONTH);
      this.date.jdn  =DateRGF.date2jdn(this.date);      // now fully set

                /* protected access to save method calls        */
      this.time.hour      = nowGC.get(java.util.Calendar.HOUR_OF_DAY);
      this.time.minute    = nowGC.get(java.util.Calendar.MINUTE);
      this.time.second    = nowGC.get(java.util.Calendar.SECOND);
      this.time.millis    = nowGC.get(java.util.Calendar.MILLISECOND);
      this.time.raw_millis= this.time.hour  *DTC.MILLIS_PER_HOUR
                           +this.time.minute*DTC.MILLIS_PER_MINUTE
                           +this.time.second*DTC.MILLIS_PER_SECOND
                           +this.time.millis
                           ;
      return this;
  }

// <!-- JAVA versions - E N D     -  -->


};



