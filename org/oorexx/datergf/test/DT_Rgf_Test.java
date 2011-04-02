//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

import org.oorexx.datergf.*;       // import DateTimeRGF package

/**
 * Testing the DateTimeRGF-classes, using the documented examples.
 *
 * <p>
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
 * ----------------------------------------------------------------------------- </pre>
  *
 * <p>
 * 2005-12-28: make code compilable (adjust for constants moved to DTC class)
 *
 * @author Rony G. Flatscher
 * @version 0.92
 */

class DT_Rgf_Test{

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";


  public static void main (String args[])
  {
     System.out.println("Testing the DateTimeRGF-classes:\n");
     test_DateRGF       ();
     test_TimeRGF       ();
     test_DateTimeRGF   ();
     test_DateFormatRGF ();
     test_NewDT();
  }




  private static void test_DateRGF()
  {
      System.out.print("Testing DateRGF-class:\n\n");

      System.out.println("   Global settings:\n");
      System.out.println("\tDateRGF.startGregCal  ="+DateRGF.getGregorianChange());
      // System.out.println("\tDateRGF.weekStart    ="+(new DateRGF()).get(DateRGF.WEEK_START));
      System.out.println("\tDateRGF.weekStart    ="+(new DateRGF()).getStatic(DTC.WEEK_START));
      System.out.println("\tDateRGF.dateSeparator=\""+DateRGF.dateSeparator+"\"" );
      // System.out.println("\tDateRGF.dateOrder    ="+DateRGF.dateOrder     );
      System.out.println("\tDateRGF.dateOrder    ="+DateRGF.getStatic(DTC.DATE_ORDER));

      System.out.println("\t\t\t\t\tYMD="+DTC.YMD); // DateRGF.YMD);
      System.out.println("\t\t\t\t\tMDY="+DTC.MDY); // DateRGF.MDY);
      System.out.println("\t\t\t\t\tDMY="+DTC.DMY); // DateRGF.DMY);

      System.out.print("\n\t\t\t---\n");

      // -----------------------------------------------------------------------------------
      {
          DateRGF firstDate=new DateRGF(   1, 1, 1), // yields: '0001-01-01'
                  lastDate =new DateRGF(9999,12,31); // yields: '9999-12-31'
       System.out.println("\tfirstDate="+firstDate+", lastDate="+lastDate);

          int     days=lastDate.subtract(firstDate); // yields: 3652060
       System.out.println("\tdays="+days);


          DateRGF tmpDate=(DateRGF)firstDate.clone();// yields: '0001-01-01'
       System.out.println("\ttmpDate="+tmpDate);
          tmpDate.add(days);                         // yields: '9999-12-31'
       System.out.println("\ttmpDate="+tmpDate+"\t(after adding days)");

                         // get Labor Monday in 2001
          tmpDate=new DateRGF(2001,8,31);            // yields: '2001-08-31', a Friday
       System.out.println("\ttmpDate="+tmpDate);
          tmpDate.set(DTC.WEEKDAY, 1);                   // yields: '2001-09-03'
       System.out.println("\ttmpDate="+tmpDate+ "\t(Labor Day)");

          tmpDate=new DateRGF(2001,8,31).set(DTC.WEEKDAY, 1);  // yields: '2001-09-03'
       System.out.println("\ttmpDate="+tmpDate+ "\t(Labor Day, version # 2)");

          DateRGF d=DateRGF.valueOf(DTC.ENCODED_AS_INTEGER, 20190521);       // yields: '2019-05-21'
       System.out.println("\td="+d);

          d.set(DTC.WB, 0);              // yields: '2019-05-19', if weekStart=7 (Sunday)
       System.out.println("\td="+d+"\tafter setting with 'WB'");
          int iso_day=d.get(DTC.DOW);    // yields: 7 (ISO-number for Sunday)
       System.out.println("\tiso_day="+iso_day);

          d.set(DTC.WB, 3);              // yields: '2019-05-22', if weekStart=7 (Sunday)
       System.out.println("\td="+d+"\tafter setting with 'WB', 3");
          d.set(DTC.WE, 0);              // yields: '2019-05-25', if weekStart=7 (Sunday)
       System.out.println("\td="+d+"\tafter setting with 'WE', 0");

          DateRGF easter=DateRGF.easter(d);        // yields: '2019-04-21'
       System.out.println("\teaster="+easter);

          int palm_days=d.get(DTC.EPOCH_PALM);           // yields: 35430
       System.out.println("\tpalm_days="+palm_days);
          int java_days=d.get(DTC.EPOCH_JAVA);           // yields: 18041
       System.out.println("\tjava_days="+java_days);

          String day_name  =DateRGF.getString(DTC.DN, d.get(DTC.DOW));           // yields: 'Saturday'
       System.out.println("\tday_name  ="+day_name);
          String month_name=DateRGF.getString(DTC.MN, d.get(DTC.MONTH_FIELD));   // yields: 'May'
       System.out.println("\tmonth_name="+month_name);
      }
      System.out.print("\n\t\t---\n");

      // -----------------------------------------------------------------------------------
      {
           DateRGF e=DateRGF.valueOf(DTC.EPOCH_PALM, 0),         // yields: '1904-01-01'
                   m=new DateRGF(2001, 1, 1);                        // yields: '2001-01-01', the
                                                                     // new millenium
       System.out.println("\te="+e);
       System.out.println("\tm="+m);

           int palm_days=m.subtract(e);                             // yields: 35430
       System.out.println("\tpalm_days="+palm_days);

           DateRGF d=DateRGF.valueOf(DTC.EPOCH_PALM, palm_days); // yields: '2001-01-01'
       System.out.println("\td="+d+"\tafter adding palm_days");

           e.add(palm_days);                                         // yields: '2001-01-01'
       System.out.println("\te="+e+"\tadding palm_days directly to e");
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

           DateRGF a=DateRGF.valueOf("1989-05-21") ,  // yields: '1989-05-21'
                   b=DateRGF.valueOf("19930922"  ) ,  // yields: '1993-09-22'
                   c=DateRGF.valueOf("1782.10.15") ,  // yields: '1782-10-15'
                   d=DateRGF.valueOf("2010 02 28") ,  // yields: '2010-02-28'
                   e=DateRGF.valueOf("030201"    ) ,  // yields: '2003-02-01'
                   f=DateRGF.valueOf("123"       ) ;  // yields: null

       System.out.println("\ta="+a);
       System.out.println("\tb="+b);
       System.out.println("\tc="+c);
       System.out.println("\td="+d);
       System.out.println("\te="+e);
       System.out.println("\tf="+f);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

           DateRGF newMillenium=new DateRGF(2001,01,01); // yields: '2001-01-01'
       System.out.println("\tnewMillenium="+newMillenium);
           newMillenium.add(100000);   // add 100.000 days, yields: '2274-10-17'
       System.out.println("\tnewMillenium="+newMillenium+"\tafter adding 100.000");
      }
      System.out.print("\n\t\t---\n");

      // -----------------------------------------------------------------------------------
      {

           DateRGF firstDate=new DateRGF(   1, 1, 1), // yields: '0001-01-01'
                   lastDate =new DateRGF(9999,12,31); // yields: '9999-12-31'

       System.out.println("\tfirstDate="+firstDate+"\tlastDate="+lastDate);

           int     days=lastDate.subtract(firstDate); // yields: 3652060
       System.out.println("\tdays="+days);
      }
      System.out.print("\n\t\t---\n");



      // -----------------------------------------------------------------------------------
      {

          DateRGF d=new DateRGF(2059, 5, 20);            // yields: '2059-05-20'
       System.out.println("\td="+d);
           int     this_date  =d.get(DTC.ENCODED_AS_INTEGER), // yields: 20590520
                   quarter    =d.get(DTC.Q),                  // yields: 2
                   semester   =d.get(DTC.HY),                 // yields: 1
                   dow        =d.get(DTC.DOW),                // yields: 2
                   ws         =d.get(DTC.WEEK_START),         // yields: 7
                   dow_ord    =d.get(DTC.DOW_ORDINAL),        // yields: 3
                   dim        =d.get(DTC.DAYS_IN_MONTH),      // yields: 31
                   palm_days  =d.get(DTC.EPOCH_PALM),         // yields: 56753
                   java_days  =d.get(DTC.EPOCH_JAVA);         // yields: 32646

       System.out.println("\tthis_date="+this_date);
       System.out.println("\tquarter  ="+quarter);
       System.out.println("\tsemester ="+semester);
       System.out.println("\tdow      ="+dow);
       System.out.println("\tws       ="+ws);
       System.out.println("\tdow_ord  ="+dow_ord);
       System.out.println("\tdim      ="+dim);
       System.out.println("\tpalm_days="+palm_days);
       System.out.println("\tjava_days="+java_days);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

           DateRGF d=DateRGF.valueOf(20010101, 0);    // yields: '2001-01-01'
       System.out.println("\td="+d);

           d.set(DTC.YEAR_FIELD, 2345);             // yields: '2345-01-01'
       System.out.println("\td="+d+"\tYEAR_FIELD, 2345");
           d.set(DTC.MONTH_FIELD, 12);              // yields: '2345-12-01'
       System.out.println("\td="+d+"\tMONTH_FIELD, 12");
           d.set(DTC.DAY_FIELD, 15);                // yields: '2345-12-15', a Saturday
       System.out.println("\td="+d+"\tDAY_FIELD, 15");

           d.set(DTC.WEEKDAY, 1);                   // yields: '2345-12-17', the next Monday
       System.out.println("\td="+d+"\tWEEKDAY, 1");

           d.set(DTC.ENCODED_AS_INTEGER, 17280228); // yields: '1728-02-28'
       System.out.println("\td="+d+"\tENCODED_AS_INTEGER, 17280228");
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
           DateRGF d=new DateRGF();

           d.set(DTC.ENCODED_AS_INTEGER, 17280228);  // yields: '1728-02-28'
       System.out.println("\td="+d);
           d.set(DTC.WB, 0);              // yields: '1728-02-22', if weekStart=7
       System.out.println("\td="+d+"\tWB, 0");
           d.set(DTC.WB, 3);              // yields: '1728-02-25', if weekStart=7
       System.out.println("\td="+d+"\tWB, 3");
           d.set(DTC.WE, 0);              // yields: '1728-02-28', if weekStart=7
       System.out.println("\td="+d+"\tWE, 0");

           d.set(DTC.QB, 0);              // yields: '1728-01-01'
       System.out.println("\td="+d+"\tQB, 0");
           d.set(DTC.QE, 0);              // yields: '1728-03-31'
       System.out.println("\td="+d+"\tQE, 0");
           d.set(DTC.QE, -21);            // yields: '1728-03-10
       System.out.println("\td="+d+"\tQE, -21");
                                      // i.e. three weeks before the end of the quarter
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

                       // get Labor Day of 2001 (first Monday in September)
           DateRGF d=new DateRGF(2001, 8, 31),e; // yields: '2001-08-31'
       System.out.println("\td="+d);
           DateRGF.setGivenWeekday(1, d);              // yields: '2001-09-03'
       System.out.println("\td="+d+"\tsGW 1");

                       // get the date the summer time ends in 2006 (last Sunday in October)
           d=new DateRGF(2006, 11, 01);        // yields: '2006-11-01'

       System.out.println("\td="+d);
           e=(DateRGF)d.clone();
           DateRGF.setGivenWeekday(-7, d);             // yields: '2006-10-29'
       System.out.println("\td="+d+"\tsGW -7");
      System.out.println("\te="+e);
           e.set(DTC.WEEKDAY, -7);
       System.out.println("\te="+d+"\tsGW -7 --- WEEKDAY");

                       // get last working day of this quarter
           d.set(DTC.QE, 1);                       // yields: '2007-01-01', first day of next quarter
       System.out.println("\td="+d);
           DateRGF.setGivenWeekday(-12345, d);         // yields: '2006-12-29', a Friday
       System.out.println("\td="+d+"\tsGW -12345");

      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
           DateRGF d  =new DateRGF(2001,1,1);         // yields: '2001-01-01'
       System.out.println("\td="+d);
           String  mon=DateRGF.getString(DTC.DN, d.get(DTC.DOW));     // yields: "Monday"
       System.out.println("\tmon="+mon);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

    String tagesnamen[]={"Montag", "Dienstag", "Mittwoch", "Donnerstag",
                                "Freitag", "Samstag", "Sonntag"};

           DateRGF.setString(DTC.DN, tagesnamen);

           DateRGF d  =new DateRGF(2001,1,1);         // yields: '2001-01-01'
       System.out.println("\td="+d);
           String  mon=DateRGF.getString(DTC.DN, d.get(DTC.DOW));     // yields: "Montag"
       System.out.println("\tmon="+mon);


       // reset global changes!
       tagesnamen=new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
       DateRGF.setString(DTC.DN, tagesnamen);

      }
      System.out.print("\n\t\t---\n");

      System.out.print("\n==================================================\n\n");
  }

  private static void test_TimeRGF()
  {
      System.out.print("Testing TimeRGF-class:\n\n");

      System.out.println("   Global settings:\n");
      System.out.println("\tTimeRGF.am_pm_string[0]=\""+TimeRGF.am_pm_string[0]+"\"" );
      System.out.println("\tTimeRGF.am_pm_string[1]=\""+TimeRGF.am_pm_string [1]+"\"" );
      System.out.println("\tTimeRGF.timeSeparator =\""+TimeRGF.timeSeparator+"\"" );
TimeRGF.showSecs=true;
      System.out.println("\tTimeRGF.showSecs       ="+TimeRGF.showSecs       );

      System.out.print("\n\t\t\t---\n");

      // -----------------------------------------------------------------------------------
      {

           TimeRGF time1, time2, time3, time4;

           time1=new TimeRGF(19,29,39);           // yields: "19:29:39"
       System.out.println("\ttime1="+time1);
           time2=new TimeRGF( 8, 8, 8);           // yields: "08:08:08"
       System.out.println("\ttime2="+time2);
           float diff=time1.subtract(time2);      // yields: "0.47327545"
       System.out.println("\tdiff="+diff);
           time3=TimeRGF.valueOf(diff);           // yields: "11:21:31"
       System.out.println("\ttime3="+time3);
           time4=TimeRGF.valueOf(-diff);          // yields: "12:38:29"
       System.out.println("\ttime4="+time4);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

          TimeRGF t1=TimeRGF.valueOf( 0.47399306f);   // yields: "11:22:33"
       System.out.println("\tt1="+t1);
          TimeRGF t2=TimeRGF.valueOf(-0.47399306f);   // yields: "12:37:27"
       System.out.println("\tt2="+t2);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
          TimeRGF t1=TimeRGF.valueOf(DTC.ENCODED_AS_INTEGER, 112233);   // yields: "11:22:33"
       System.out.println("\tt1="+t1);
          TimeRGF t2=TimeRGF.valueOf(DTC.ENCODED_AS_INTEGER,-112233);   // yields: "12:37:27"
       System.out.println("\tt2="+t2);

          TimeRGF t3=TimeRGF.valueOf(DTC.ENCODED_AS_SECONDS,  40953);   // yields: "11:22:33"
       System.out.println("\tt3="+t3);
          TimeRGF t4=TimeRGF.valueOf(DTC.ENCODED_AS_SECONDS, -40953);   // yields: "12:37:27"
       System.out.println("\tt4="+t4);

          TimeRGF t5=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS ,  40953123);   // yields: "11:22:33.123"
       String tst=DateFormatRGF.format("HH:mm:ss.SSS", t5);
       System.out.println("\tt5="+t5+" ==> "+tst);

          TimeRGF t6=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS , -40953123);   // yields: "12:37:26.877"
              tst=DateFormatRGF.format("HH:mm:ss.SSS", t6);
       System.out.println("\tt6="+t6+" ==> "+tst);

          TimeRGF t7=TimeRGF.valueOf(DTC.ENCODED_AS_MILLIS , 86399999);   // yields: "23:59:59.999"
              tst=DateFormatRGF.format("HH:mm:ss.SSS", t7);
       System.out.println("\tt7="+t7+" ==> "+tst);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

           TimeRGF t1=TimeRGF.valueOf("00:01:02");    // yields: '00:01:02'
       System.out.println("\tt1="+t1);
           TimeRGF t2=TimeRGF.valueOf("11:59:59");    // yields: '11:59:59'
       System.out.println("\tt2="+t2);
           TimeRGF t3=TimeRGF.valueOf("12:55:44");    // yields: '12:55:44'
       System.out.println("\tt3="+t3);
           TimeRGF t4=TimeRGF.valueOf("17:01:59");    // yields: '17:01:59'
       System.out.println("\tt4="+t4);
           TimeRGF t5=TimeRGF.valueOf("23:11:22");    // yields: '23:11:22'
       System.out.println("\tt5="+t5);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
         boolean old=TimeRGF.is24Hour;

         TimeRGF.is24Hour=false;

           TimeRGF t1=TimeRGF.valueOf("12:01:02 am"); // yields: '00:01:02'
           TimeRGF t2=TimeRGF.valueOf("11:59:59 am"); // yields: '11:59:59'
           TimeRGF t3=TimeRGF.valueOf("12:55:44 pm"); // yields: '12:55:44'
           TimeRGF t4=TimeRGF.valueOf("05:01:59 pm"); // yields: '17:01:59'
           TimeRGF t5=TimeRGF.valueOf("11:11:22 pm"); // yields: '23:11:22'

        TimeRGF.is24Hour=true;         // now show in 24 hour-mode

       System.out.println("\tt1="+t1);
       System.out.println("\tt2="+t2);
       System.out.println("\tt3="+t3);
       System.out.println("\tt4="+t4);
       System.out.println("\tt5="+t5);

        TimeRGF.is24Hour=old;
      }
      System.out.print("\n\t\t---\n");




      // -----------------------------------------------------------------------------------
      {
          TimeRGF t1=new TimeRGF(11,22,33);           // yields: "11:22:33"

          int     hour  =t1.get(DTC.HOUR_FIELD),          // yields: "11"
                  minute=t1.get(DTC.MINUTE_FIELD),        // yields: "22"
                  sec   =t1.get(DTC.SECOND_FIELD);        // yields: "33"

          int     a1=t1.get(DTC.ENCODED_AS_INTEGER);      // yields: "112233"

          int     a2=t1.get(DTC.ENCODED_AS_SECONDS);      // yields: "40953"
          int     a3=t1.get(DTC.RAW_MILLIS_FIELD);          // yields: "40953"

       System.out.println("\tt1    ="+t1);
       System.out.println("\thour  ="+hour);
       System.out.println("\tminute="+minute);
       System.out.println("\tsec   ="+sec);
       System.out.println("\ta1    ="+a1);
       System.out.println("\ta2    ="+a2);
       System.out.println("\ta3    ="+a3);

      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
          TimeRGF t1=new TimeRGF(23,59,59);   // yields: "23:59:59"
       System.out.println("\tt1="+t1);

          t1.set(DTC.HOUR_FIELD, 11);             // yields: "11:59:59"
       System.out.println("\tt1="+t1+"\tHOUR_FIELD, 11");
          t1.set(DTC.MINUTE_FIELD, 22);           // yields: "11:22:59"
       System.out.println("\tt1="+t1+"\tMINUTE_FIELD, 22");
          t1.set(DTC.SECOND_FIELD, 33);           // yields: "11:22:33"
       System.out.println("\tt1="+t1+"\tSECOND_FIELD, 33");

          t1.set(DTC.ENCODED_AS_INTEGER, 10001);  // yields: "01:00:01"
       System.out.println("\tt1="+t1+"\tE_A_I, 10001");

          t1.set(DTC.ENCODED_AS_SECONDS, 40953);  // yields: "11:22:33"
       System.out.println("\tt1="+t1+"\tE_A_S, 40953");
          t1.set(DTC.RAW_MILLIS_FIELD, 40954);      // yields: "11:22:34"
       System.out.println("\tt1="+t1+"\tRAW_S_F, 40954");
      }
      System.out.print("\n\t\t---\n");



      // -----------------------------------------------------------------------------------
      {
          TimeRGF t1=new TimeRGF(11,22,33);           // yields: "11:22:33"
       System.out.println("\tt1="+t1);
          float   f1=t1.toFloat();                    // yields: "0.47399306"
       System.out.println("\tf1="+f1);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
       System.out.println("\t");

          TimeRGF t1=new TimeRGF(14,23,56);   // yields: "14:23:56"
       System.out.println("\tt1="+t1);

          TimeRGF.is24Hour=true;             // changing global setting
          TimeRGF.showSecs =true;             // changing global setting
          String  s1=t1.toString();           // yields: "14:23:56"
       System.out.println("\ts1="+s1);

          TimeRGF.is24Hour=false;            // changing global setting
          TimeRGF.showSecs =false;            // changing global setting
          s1=t1.toString();                   // yields: "2:23 pm"
       System.out.println("\ts1="+s1);
                // reset
          TimeRGF.is24Hour=true;             // changing global setting
          TimeRGF.showSecs =true;             // changing global setting
      }
      System.out.print("\n\t\t---\n");


      System.out.print("\n==================================================\n\n");
  }

  private static void test_DateTimeRGF()
  {
      System.out.print("Testing DateTimeRGF-class:\n\n");

      // -----------------------------------------------------------------------------------
      {

           DateTimeRGF dt1, dt2;
           dt1=new DateTimeRGF(2001,1,1,0,0,0);      // yields: 2001-01-01 00:00:00
           dt2=new DateTimeRGF(2005,12,31,11,59,59); // yields: 2005-12-31 11:59:59
           int diff[];

//       System.out.println("\tdt1   ="+dt1);
       System.out.println(DateFormatRGF.format("\t'dt1='yyyy-MM-dd HH:mm:ss.SSS", dt1));
//       System.out.println("\tdt2   ="+dt2);
       System.out.println(DateFormatRGF.format("\t'dt2='yyyy-MM-dd HH:mm:ss.SSS", dt2));


       System.out.println("\tdt2.subtract(dt1):");
           diff=dt2.subtract(dt1);

       System.out.println("\tdiff[0]="+diff[0]);
       System.out.println("\tdiff[1]="+diff[1]+"\n");


       System.out.println("\tdt1.subtract(dt2):");
           diff=dt1.subtract(dt2);
       System.out.println("\tdiff[0]="+diff[0]);
       System.out.println("\tdiff[1]="+diff[1]+"\n");

//           dt2.update();                  // yields: actual date and time
           //MiscRGF_java.update(dt2);                  // yields: actual date and time
        dt2.update();
//       System.out.println("\tdt2   ="+dt2+"\t after update()");
       System.out.println(DateFormatRGF.format("\t'dt1='yyyy-MM-dd HH:mm:ss.SSS", dt1));
       System.out.println(DateFormatRGF.format("\t'dt2='yyyy-MM-dd HH:mm:ss.SSS", dt2)+" after update()");
       System.out.println("\tdt2.subtract(dt1):");
           diff=dt2.subtract(dt1);

       System.out.println("\tdiff[0]="+diff[0]);
       System.out.println("\tdiff[1]="+diff[1]+"\n");


       System.out.println("\tdt1.subtract(dt2):");
           diff=dt1.subtract(dt2);
       System.out.println("\tdiff[0]="+diff[0]);
       System.out.println("\tdiff[1]="+diff[1]+"\n");


      }
      System.out.print("\n\t\t---\n");


/* ---------->
      // -----------------------------------------------------------------------------------
      {
                                  // create a DateTimeRGF object with a
                                  // value of: "2010-01-12 14:05:47"
           DateTimeRGF dt=new DateTimeRGF(2010,01,12,14,05,47);
       System.out.println("\tdt="+dt);

           dt.add(1.75f);         // add 1 3/4 days (1 day, 18 hours)
                                  // "dt" now: "2010-01-14 08:05:47
       System.out.println("\tdt="+dt+"\tafter adding 1.75f");

      }
      System.out.print("\n\t\t---\n");

      // -----------------------------------------------------------------------------------
      {

                                  // create a DateTimeRGF object with a
                                  // value of: "2010-01-12 14:05:47"
           DateTimeRGF dt1=new DateTimeRGF(2010,01,12,14,05,47);
       System.out.println("\tdt1="+dt1);

                                  // create a DateTimeRGF object with a
                                  // value of: "2010-01-14 08:05:47"
           DateTimeRGF dt2=new DateTimeRGF(2010,01,14,8,05,47);
       System.out.println("\tdt2="+dt2);

           float       diff=dt2.subtract(dt1);     // yields: 1.75

       System.out.println("\tdiff="+diff);
      }
      System.out.print("\n\t\t---\n");

<-------------- */

      // -----------------------------------------------------------------------------------
      {

                                // create a DateTimeRGF object with a
                                // value of: "2010-01-12 14:05:47.123"
         DateTimeRGF dt=new DateTimeRGF(2010,01,12,14,05,47,123);

//         System.out.println("\tdt="+dt);
       System.out.println(DateFormatRGF.format("\t'dt='yyyy-MM-dd HH:mm:ss.SSS", dt));

         dt.add(1, 64800000);    // add 1 3/4 days == 1 day, 18 hours (= 64800000 millis)
                                 // "dt" now: "2010-01-14 08:05:47.123"

       System.out.println("\tdt="+dt+"\tafter adding (1, 64800000)");

       System.out.println("\t");
      }
      System.out.print("\n\t\t---\n");



/* ------------------>
      // -----------------------------------------------------------------------------------
      {


                                  // create a DateTimeRGF object with a
                                  // value of: "2010-01-12 14:05:47"
           DateTimeRGF dt1=new DateTimeRGF(2010,01,12,14,05,47);
//       System.out.println("\tdt1="+dt1);
       System.out.println(DateFormatRGF.format("\t'dt1='yyyy-MM-dd HH:mm:ss.SSS", dt1));

                                  // create a DateTimeRGF object with a
                                  // value of: "2010-01-14 08:05:47"
           DateTimeRGF dt2=new DateTimeRGF(2010,01,14,8,05,47);
//       System.out.println("\tdt2="+dt2);
       System.out.println(DateFormatRGF.format("\t'dt2='yyyy-MM-dd HH:mm:ss.SSS", dt2));

           double      diff=dt2.subtractDouble(dt1);      // yields: 1.75
       System.out.println("\tdiff="+diff);

       System.out.println("\t");
      }
      System.out.print("\n\t\t---\n");

<------------------ */

      // -----------------------------------------------------------------------------------
      {


           DateTimeRGF dt=new DateTimeRGF(2010,1,12, 14,5,47,123);
                          // "dt" is now: "2010-01-12 14:05:47"
//       System.out.println("\tdt="+dt);
       System.out.println(DateFormatRGF.format("\t'dt='yyyy-MM-dd HH:mm:ss.SSS", dt));

           long secs_long=dt.toLong(DTC.EPOCH_PALM);
                          // "secs_long" is now: "3.346.149.947" seconds
                          //                     since DateRGF.EPOCH_PALM
       System.out.println("\tsecs_long="+secs_long);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {

                          // 3.346.149.947 seconds since DateRGF.EPOCH_PALM,
                          // which is exactly: "2010-01-12 14:05:47"
           long secs_long=3346149947123l;
       System.out.println("\tsecs_long="+secs_long);

           DateTimeRGF dt=DateTimeRGF.valueOf(DTC.EPOCH_PALM, secs_long);
                          // "dt" is now: "2010-01-12 14:05:47"
//       System.out.println("\tdt="+dt);
       System.out.println(DateFormatRGF.format("\t'dt='yyyy-MM-dd HH:mm:ss.SSS", dt));


       System.out.println("\t");
      }
      System.out.print("\n\t\t---\n");



      // -----------------------------------------------------------------------------------
      {
                // create "dt" and set it to: "2010-01-12 14:05:47"
           DateTimeRGF dt=new DateTimeRGF(2010,1,12, 14,5,47);
//       System.out.println("\tdt="+dt);
       System.out.println(DateFormatRGF.format("\t'dt='yyyy-MM-dd HH:mm:ss.SSS", dt));

           int secs_int=dt.toInt(DTC.EPOCH_PALM);
                // "secs_int" is now: "-948.817.349", which is equivelant to
                // an unsigned int with a value of '3.346.149.947'
                // (seconds elapsed since DateRGF.EPOCH_PALM)
       System.out.println("\tsecs_int="+secs_int);
      }
      System.out.print("\n\t\t---\n");


      // -----------------------------------------------------------------------------------
      {
                          // The date and time "2010-01-12 14:05:47" lies
                          // 3.346.149.947 seconds after DateRGF.EPOCH_PALM;
                          // if stored as an 'unsigned int' but referred to as
                          // a 'signed int', one needs to use the value
                          // '-948.817.349' in order to set the appropriate
                          // bits.
           int secs_int=-948817349;
       System.out.println("\tsecs_int="+secs_int);

           DateTimeRGF dt=DateTimeRGF.valueOf(DTC.EPOCH_PALM, secs_int);
                          // "dt" is now: "2010-01-12 14:05:47"
//       System.out.println("\tdt="+dt);
       System.out.println(DateFormatRGF.format("\t'dt='yyyy-MM-dd HH:mm:ss.SSS", dt));

       System.out.println("\t");
      }
      System.out.print("\n\t\t---\n");



      System.out.print("\n==================================================\n\n");
  }

  private static void test_DateFormatRGF()
  {
      System.out.print("Testing DateFormatRGF-class:\n\n");
      // -----------------------------------------------------------------------------------
      {
           DateRGF d=new DateRGF(2029, 5, 21);            // yields: '2029-05-21'

           String  info1=DateFormatRGF.format("EEEE, yy-mmm-dd!",  d);  // yields: "Monday, 29-May-21!"         ???NEIN!!!
           String  info2=DateFormatRGF.format("%A, %y-%b-%d!",     d);  // yields: "Monday, 29-May-21!"

           String  info3=DateFormatRGF.format("'days so far: 'jjj", d); // yields: "days so far: 141"
           String  info4=DateFormatRGF.format("'days to go:  'ggg", d); // yields: "days to go:  224"

       System.out.println("\tinfo1="+info1);
       System.out.println("\tinfo2="+info2);
       System.out.println("\tinfo3="+info3);
       System.out.println("\tinfo4="+info4);
      }
      System.out.print("\n\t\t---\n");




      // -----------------------------------------------------------------------------------
      {
           DateRGF d=new DateRGF(2029, 5, 21);           // yields: '2029-05-21'

           String  info1=DateFormatRGF.format("EEEE, yy-mmm-dd!", d);   // yields: "Monday, 29-May-21!" ???NEIN!!!
           String  info2=DateFormatRGF.format("%A, %y-%b-%d!", d);      // yields: "Monday, 29-May-21!"

           String  info3=DateFormatRGF.format("'days so far: 'jjj", d); // yields: "days so far: 141"
           String  info4=DateFormatRGF.format("'days to go:  'ggg", d); // yields: "days to go:  224"

           String  info5=DateFormatRGF.format("'week #' ww", d);        // yields: "week # 21"

           String  info6=DateFormatRGF.format("'day of week:' w", d);   // yields: "day of week: 1"
           String  info7=DateFormatRGF.format("'ordinal day:' o", d);   // yields: "ordinal day: 2", if
                                                                        //          DateRGF.weekStart=7

           String  info8=DateFormatRGF.format("d. R. yyyy", d);         // yields: "21. V. 2029"

           String  info9=DateFormatRGF.format("%e. %B (yyyy), a EEEE.", d); // yields: "21. May (2029), a Monday." ???NEIN!!!

       System.out.println("\tinfo1="+info1);
       System.out.println("\tinfo2="+info2);
       System.out.println("\tinfo3="+info3);
       System.out.println("\tinfo4="+info4);
       System.out.println("\tinfo5="+info5);
       System.out.println("\tinfo6="+info6);
       System.out.println("\tinfo7="+info7);
       System.out.println("\tinfo8="+info8);
       System.out.println("\tinfo9="+info9);
      System.out.print("\n==================================================\n\n");
      }
      System.out.print("\n\t\t---\n");



      // -----------------------------------------------------------------------------------
      System.out.print("Now the additions since 2001-03-06...\n\n");
      {
           System.out.print("Examples 'DateRGF.date2jdn(DateRGF d)':\n");

           DateRGF d1=new DateRGF(1582, 10,  4),        // Julian calendar
                   d2=new DateRGF(1582, 10,  15),       // Gregorian calendar
                   d3=new DateRGF(),
                   d4=new DateRGF();
           System.out.println("\td1="+d1+", d2="+d2);

           int   jdn1=DateRGF.date2jdn(d1),             // yields:
                 jdn2=DateRGF.date2jdn(d2);             // yields:

           System.out.println("\tjd1="+jdn1+", jd2="+jdn2);

           DateRGF.jdn2date(jdn1, d3);
           DateRGF.jdn2date(jdn2, d4);

           System.out.println("\td3="+d3+", d4="+d4);
           System.out.print("\n\t\t---\n\n");
      }



      {
          System.out.print("Examples 'DateRGF.date2jdn(year, month, day, calendar2use)':\n");

          int     jdn=DateRGF.date2jdn(2010,1,1,DTC.GREGORIAN); // yields: 2455198 (Gregorian calendar)
          System.out.println("\tjdn="+jdn);

          DateRGF   d=new DateRGF();

          DateRGF.jdn2date(jdn, d, DTC.GREGORIAN);           // yields: '2010-01-01' (GREGORIAN)
          System.out.println("\t  d="+d+" (GREGORIAN) ");

          DateRGF.jdn2date(jdn, d, DTC.JULIAN);              // yields: '2009-12-19' (JULIAN)
          System.out.println("\t  d="+d+" (JULIAN) ");

          System.out.print("\n\t\t---\n\n");
      }



      {
          System.out.print("Examples 'DateRGF.calendar(DateRGF d)':\n");

          DateRGF bkpD = DateRGF.getGregorianChange();

          DateRGF.setGregorianChange(1752, 9, 14);      // set first Gregorian calendar date
          System.out.println("\tGCD=" +DateRGF.getGregorianChange()+"\n");

          DateRGF d1=new DateRGF(1659, 11, 17), // falls into the Julian calendar
                  d2=new DateRGF(2016,  2,  1), // falls into the Gregorian calendar
                  d3, d4;

          System.out.print("\td1="+d1+" ");
          d3=DateRGF.easter(d1);                   // yields: '
          System.out.print("\teaster(d3)="+d3+" \n");


          System.out.print("\td2="+d2+" ");
          d4=DateRGF.easter(d2);                   // yields: '
          System.out.print("\teaster(d4)="+d4+" \n\n");


          System.out.println("\tre-set Gregorian Change Date: "+
                DateRGF.setGregorianChange(bkpD.get(DTC.YEAR_FIELD       ),
                                           bkpD.get(DTC.MONTH_FIELD      ),
                                           bkpD.get(DTC.DAY_FIELD        ))
                            );

          System.out.print("\n\t\t---\n\n");
      }




      {
          System.out.print("Examples 'DateRGF.calendar(DateRGF d, flag calendar type)':\n");

          DateRGF d1=new DateRGF(2016, 9, 1);   // yields:'2010-02-01'
          System.out.print("\td1="+d1+" \n");

          DateRGF d2=DateRGF.easter(d1.get(DTC.YEAR_FIELD), DTC.JULIAN);
          System.out.print("\td2="+d2+" (Orthodox Easter)\n");

          d2.jdn2date(d2.get(DTC.JDN), d2, DTC.GREGORIAN);  // yields: '...'
          System.out.print("\td2="+d2+" Gregorian calendar date for Orthodox Easter\n");


          System.out.print("\n\t\t---\n\n");
      }



      {
          System.out.print("Examples 'DateTimeRGF.update(obj)':\n");
          DateRGF     d =new DateRGF();        // yields: '0001-01-01'
          TimeRGF     t =new TimeRGF();        // yields: '00:00:00'
System.out.println("\td="+d+", t="+t);

          // MiscRGF_waba.update(d);               // e.g. yields:
          d.update();
          // MiscRGF_waba.update(t);               // e.g. yields:
          t.update();
System.out.println("\td="+d+", t="+t+" ... after MiscRGF_waba.update(obj)");

          DateTimeRGF dt=new DateTimeRGF(d, t);// yields:
System.out.println("\tdt="+dt);

          // MiscRGF_java.update(d);               // e.g. yields:
          // MiscRGF_java.update(t);               // e.g. yields:
          d.update();
          t.update();

System.out.println("\td="+d+", t="+t+" ... after MiscRGF_java.update(obj)");

          dt=new DateTimeRGF(d, t);// yields:
System.out.println("\tdt="+dt);

          System.out.print("\n\t\t---\n\n");
      }


      System.out.print("\n==================================================\n\n");
      {
         System.out.println("Auáer Konkurrenz!\n");



      DateRGF d=new DateRGF(2033, 9, 22);             // yields: '2033-09-22'

      String  info1=DateFormatRGF.format("EEEE, yy-MMM-dd!",   d);  // yields: "Thursday, 33-Sep-22!"           ???NEIN!!!
      String  info2=DateFormatRGF.format("%A, %y-%b-%d!",      d);  // yields: "Thursday, 33-Sep-22!"

      String  info3=DateFormatRGF.format("'days so far: 'DDD", d);  // yields: "days so far: 265"
      String  info4=DateFormatRGF.format("'days to go:  'ggg", d);  // yields: "days to go:  100"


      System.out.println("\tinfo1="+info1);
      System.out.println("\tinfo2="+info2);
      System.out.println("\tinfo3="+info3);
      System.out.println("\tinfo4="+info4);

          System.out.print("\n\t\t---\n\n");
      }



      {
           DateTimeRGF dt=new DateTimeRGF(2033, 9, 22, 12, 6, 59);             // yields: '2033-09-22 12:06:59'

           String info1=DateFormatRGF.format("EEEE, d. MMMM yyyy, hh:mm a 'o''clock'", dt); // yields: "Thursday, 22. September 2033, 12:06 pm o'clock"

           String  info2=DateFormatRGF.format("EEEE, yy-MMM-dd!",   dt);  // yields: "Thursday, 33-Sep-22!"
           String  info3=DateFormatRGF.format("%A, %y-%b-%d!",      dt);  // yields: "Thursday, 33-Sep-22!"

           String  info4=DateFormatRGF.format("D/ggg", dt);              // yields: "265/100"

           String  info5=DateFormatRGF.format("'week #' w", dt);         // yields: "week # 38"

           String  info6=DateFormatRGF.format("'day of week:' e", dt);   // yields: "day of week: 4"
           String  info7=DateFormatRGF.format("'dayname:    ' EEEE", dt);// yields: "dayname: Thursday"
           String  info8=DateFormatRGF.format("'ordinal day:' o", dt);   // yields: "ordinal day: 5", if
                                                                         //          DateRGF.weekStart=7

           String  info9=DateFormatRGF.format("d. R. yyyy", dt);         // yields: "22. IX. 2033"


      System.out.println("\tinfo1 ="+info1);
      System.out.println("\tinfo2 ="+info2);
      System.out.println("\tinfo3 ="+info3);
      System.out.println("\tinfo4 ="+info4);
      System.out.println("\tinfo5 ="+info5);
      System.out.println("\tinfo6 ="+info6);
      System.out.println("\tinfo7 ="+info7);
      System.out.println("\tinfo8 ="+info8);
      System.out.println("\tinfo9 ="+info9);

          System.out.print("\n\t\t---\n\n");

      }




        // Localizable stuff
      {
          DateTimeRGF dt = new DateTimeRGF(2033, 9, 22, 12, 6, 59);
          String    form = "EEEE, yyyy-mmm-dd HH:mm/hh:mm a";
          String s1, s2, s3, s4, s5, s6, s7, s8;

          s1=DateFormatRGF.format(form, dt);
          s2=dt.toString();     // use Date/TimeRGFs format settings

          // MiscRGF_java.localize(java.util.Locale.GERMAN);
          DateFormatRGF.localize(java.util.Locale.GERMAN);

          s3=DateFormatRGF.format(form, dt);
          s4=dt.toString();     // use Date/TimeRGFs format settings

          // MiscRGF_java.localize(java.util.Locale.ITALIAN);
          DateFormatRGF.localize(java.util.Locale.ITALIAN);
          s5=DateFormatRGF.format(form, dt);
          s6=dt.toString();     // use Date/TimeRGFs format settings

          // MiscRGF_java.localize(java.util.Locale.US);
          DateFormatRGF.localize(java.util.Locale.US);
          s7=DateFormatRGF.format(form, dt);
          s8=dt.toString();     // use Date/TimeRGFs format settings

/* -------->

    s 1=Thursday, 2033-06-22 12:06/12:06 pm
    s 2=2033-09-22 12:06:59
    s 3=Donnerstag, 2033-06-22 12:06/12:06 PM
    s 4=22.09.2033 12:06:59
    s 5=giovedi, 2033-06-22 12:06/12:06 PM
    s 6=22/09/2033 12.06.59
    s 7=Thursday, 2033-06-22 12:06/12:06 PM
    s 8=09/22/2033 12:06:59 PM

<---------- */

          System.out.println("\ts 1="+s1);
          System.out.println("\ts 2="+s2);
          System.out.println("\ts 3="+s3);
          System.out.println("\ts 4="+s4);
          System.out.println("\ts 5="+s5);
          System.out.println("\ts 6="+s6);
          System.out.println("\ts 7="+s7);
          System.out.println("\ts 8="+s8);

          System.out.print("\n\t\t---\n\n");
      }




/* ---> no need to test more!
      {
         DateRGF      d=new DateRGF(2001, 5, 20);
         TimeRGF     t1=new TimeRGF(11, 59, 50);
         TimeRGF     t2=new TimeRGF(1, 01, 02);
         TimeRGF     t3=new TimeRGF(23, 26, 57);
         TimeRGF     t4=new TimeRGF(23, 26, 57, 987);                   // test millis
         DateTimeRGF dt=new DateTimeRGF(2001, 5, 20, 12, 6, 7);
         DateTimeRGF dt2=new DateTimeRGF(2001, 5, 20, 12, 6, 7, 654);   // test millis

         dumpThings(d);
         dumpThings(t1);
         dumpThings(t2);
         dumpThings(t3);
         dumpThings(dt);
         dumpThings(dt2);
         dumpThings(dt2.update());
         dumpThings(t4);
         dumpThings(dt2.update());

      }

<--- */


      System.out.print("\n==================================================\n\n");
          System.out.print("\n\t\t---The End---\n\n");
  }



  static void dumpThings(Object obj)
  {
     String [] formats={
                         "yyyy-M-d",
                         "yyyy-MM-dd",
                         "yyy-MMM-ddd",
                         "yyy-MMM-E",
                         "yy-MMMM-EEEE",
                         "EEEE, d. MMMM yyyy, HH:mm:ss",
                         "EEEE, d. MMMM yyyy, HH:mm:ss.SSS",
                         "/ r / R / e / w / ww /",
                         "/ o / D|g / DD|gg / DDD|ggg /",
                         "H:m:s / HH:mm:ss/ KK:mm:ss a / h:m / kk:mma/",
                         "a\"ist das so?\" 'ja, ''so'' ist das!'a",
                         "'''das ist so'''",
                         "''das ist so''",
                         "''''das ist so''''",
                         "'''''das ist so'''''"
                       };

     int i, top=formats.length;

     System.out.println("dumpThings(), obj="+obj+"\n");

     for (i=0; i<top; i++)
     {
        System.out.println("\t["+formats[i]+"]");
        System.out.println("\t"+DateFormatRGF.format(formats[i], obj)+"\n");
     }
     System.out.print("\n\t\t---\n\n");
  }


  static void test_NewDT()
  {
     testNewDT_Epoch(DTC.EPOCH_PALM);
     testNewDT_Epoch(DTC.EPOCH_JAVA);


     aha(new DateTimeRGF(1959, 5, 20, 0, 0, 0, 0),
         new DateTimeRGF(1959, 5, 22, 5, 5, 5, 5));

     aha(new DateTimeRGF(1959, 5, 20, 5, 5, 5, 5),
         new DateTimeRGF(1959, 5, 22, 0, 0, 0, 0));

     aha(new DateTimeRGF(1959, 5, 15, 1, 1, 1, 1),
         new DateTimeRGF(1959, 5, 20, 5, 5, 5, 5));

     aha(new DateTimeRGF(1959, 5, 15, 5, 5, 5, 5),
         new DateTimeRGF(1959, 5, 20, 1, 1, 1, 1));

     DateTimeRGF dt=new DateTimeRGF();
     // MiscRGF_java.update(dt);
     dt.update();

     aha(dt,
         new DateTimeRGF(1959, 5, 20, 1, 1, 1, 1));

// waba.sys.Vm.sleep(250);
     try
     {
         java.lang.Thread.sleep(250);
     }
     catch (Exception e)
     {
         System.err.println("java.lang.Thread.sleep(250): threw up on us!! \n\texception: "+e);
     }

     DateTimeRGF dt2=new DateTimeRGF();
     // MiscRGF_waba.update(dt2);
     dt2.update();
     aha(dt, dt2);


  }

  static void testNewDT_Epoch(int epoch)
  {
     DateRGF     ed = DateRGF.setDefaultEpochDate(epoch);

System.out.println("... epoch: "+ed);

     DateTimeRGF dt = new DateTimeRGF(1959, 5, 20, 18, 59, 12, 987);
     System.out.println(DateFormatRGF.format("\t'dt ='yyyy-MM-dd HH:mm:ss.SSS", dt));

     long millis     = dt.toLong(epoch);
     DateTimeRGF dt1 = DateTimeRGF.valueOf(epoch, millis);

     System.out.println("\t\t\tmillis (long)="+millis);
     System.out.println(DateFormatRGF.format("\t'dt1='yyyy-MM-dd HH:mm:ss.SSS", dt1));


     int secs        = dt.toInt(epoch);
     DateTimeRGF dt2 = DateTimeRGF.valueOf(epoch, secs  );

     System.out.println("\t\t\tsecs   (int) ="+secs);
     System.out.println(DateFormatRGF.format("\t'dt2='yyyy-MM-dd HH:mm:ss.SSS", dt2));

     System.out.println("\n\t---   ---   ---\n\n");

  }

  static  void aha (DateTimeRGF dt1, DateTimeRGF dt2)
  {
     DateTimeRGF dt3 = (DateTimeRGF) dt1.clone(),
                 dt4 = (DateTimeRGF) dt2.clone();
     int [] diff;

     System.out.println("\n\naha()...\n\n");
     System.out.println(DateFormatRGF.format("\t'dt3='yyyy-MM-dd HH:mm:ss.SSS", dt3));
     System.out.println(DateFormatRGF.format("\t'dt4='yyyy-MM-dd HH:mm:ss.SSS", dt4));

     diff=dt4.subtract(dt3);
     System.out.println("\t\tdt4.subtract(dt3): diff[0]="+diff[0]+"\tdiff[1]="+diff[1]);
     dt3.add(diff[0], diff[1]);
     System.out.println(DateFormatRGF.format("\t'dt3='yyyy-MM-dd HH:mm:ss.SSS", dt3)+" after add(dt3.add(diff))");
     System.out.println("\t\t---");


     dt3=(DateTimeRGF) dt2.clone();
     dt4=(DateTimeRGF) dt1.clone();

     System.out.println(DateFormatRGF.format("\t'dt3='yyyy-MM-dd HH:mm:ss.SSS", dt3));
     System.out.println(DateFormatRGF.format("\t'dt4='yyyy-MM-dd HH:mm:ss.SSS", dt4));

     diff=dt4.subtract(dt3);
     System.out.println("\t\tdt4.subtract(dt3): diff[0]="+diff[0]+"\tdiff[1]="+diff[1]);
     dt3.add(diff[0], diff[1]);
     System.out.println(DateFormatRGF.format("\t'dt3='yyyy-MM-dd HH:mm:ss.SSS", dt3)+" after add(dt3.add(diff))");
     System.out.println("\n\t\t---\n");
  }






  static String ri(DateRGF aha, int len)
  {
     if (aha==null) return "<null>";
     String blank="                                    ";
     String strDate=aha.toString();
     len=len-strDate.length();
     if (len>0) return blank.substring(0,len)+strDate;
     return strDate;

  }

}


