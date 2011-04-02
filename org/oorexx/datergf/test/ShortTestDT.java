//   last change: $Revision: 136 $ $Author: rony $ $Date: 2006-01-20 01:05:12 +0100 (Fri, 20 Jan 2006) $

import org.oorexx.datergf.*;       // import DateTimeRGF package

/**
 * Demonstrating the DateTimeRGF-classes, using the documented examples.
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
 *
 * Works with both versions, Java and waba.
 *
 * to run: 1) compile ("javac ShortTestDT.java")
 *         2) start   ("java ShortTestDT")
 *
 * @author Rony G. Flatscher, 2001-04-07, 2005-12-28, 2006-01-01
 * @version 0.92
 */

class ShortTestDT{

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "92.20060101";


  public static void main (String args[])
  {

     DateRGF       d=null;
     TimeRGF       t=null;
     DateTimeRGF dt1=new DateTimeRGF(),
                 dt2=(DateTimeRGF)dt1.clone();

     System.out.println(DateFormatRGF.format("'dt1='yyyy-MM-dd HH:mm:ss.SSS", dt1) +"\t(using DateFormatRGF.format() to show msecs)");
     System.out.println("dt1="+dt1+"\t\t(using DateTimeRGF.toString() formatting)\n");

     dt1.date.update();
     System.out.println(DateFormatRGF.format("'dt1='yyyy-MM-dd HH:mm:ss.SSS, 'after updating date-portion'", dt1));
     dt1.time.update();
     System.out.println(DateFormatRGF.format("'dt1='yyyy-MM-dd HH:mm:ss.SSS, 'after updating time-portion'", dt1));
     System.out.println("dt1="+dt1+"\t\t(using DateTimeRGF.toString() formatting)\n");

     System.out.println("(now doing something consuming a little bit of time...)");
        // do something time-consuming
     {
        for (int i=0 ; i < 100; i++)
        {
            DateTimeRGF dtrgf=new DateTimeRGF(new DateRGF().update(),
                                              new TimeRGF().update()).update();
        }

     }
     dt2.update();
     System.out.println(DateFormatRGF.format("'dt2='yyyy-MM-dd HH:mm:ss.SSS, 'after updating date- and time-portions at once'\n", dt2));

     d=dt2.date;        // get DateRGF object

     System.out.println("day="+(d.getString(DTC.DN, d.get(DTC.DOW)))+"\n");
     System.out.println("1)  "+DateFormatRGF.format("E, yyyy-MMM-dd", dt2)+"\t\t(day and month names: first 3 letters)");
     System.out.println("2)  "+DateFormatRGF.format("EEEE, yyyy-MMMM-dd", dt2)+"\t(day and month names: full name)");

     System.out.print("\n\t\t---The End---\n\n");
  }

}


