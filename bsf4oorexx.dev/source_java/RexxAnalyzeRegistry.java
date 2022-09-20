package org.rexxla.bsf.engines.rexx;

/* rexxtry-session:

    rexxtry.rex
    call bsf.cls
    clz=bsf.loadclass("org.rexxla.bsf.engines.rexx.RexxAnalyzeRegistry")
    say clz~getAnalyzedDataAsString

    a="1234567890" -- ten characters
    s=.bsf~new("java.lang.String",a);say "a:" pp(a) "length:" pp(a~length); say s
    say
    say clz~getAnalyzedDataAsString

    arr1=bsf.createJavaArrayOf("java.lang.String", a, a, a)
    say "arr1:" pp(arr1) "arr1~items:" pp(arr1~items)
    say
    say clz~getAnalyzedDataAsString

    arr2=bsf.createJavaArray("java.lang.String", 10, 5) -- two dimensions
    arr2[1,1]=a;arr2[10,5]=a;say "arr2:" pp(arr2) "arr2~items:" pp(arr2~items)
    say
    say clz~getAnalyzedDataAsString

    ---

    a=clz~getAnalyzedData
    say "a:" pp(a)
    say "BsfRexxProxy(a):" pp(BsfRexxProxy(a))
    say "refCount:" pp(BsfRexxProxy(a,'refCount'))
    say a~toString

    ---

    a=xrange("00"x,"ff"x)~copies(1000)
    do i=1 to 1000;t1=BsfRawBytes(a);t2=bsfRawBytes(t1,a~length);end
    say clz~getAnalyzedData~toString
*/

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.IntSummaryStatistics;  // needs Java 8
import java.util.LongSummaryStatistics; // needs Java 8

/**
 *  This class allows to analyze the registry maintained by RexxAndJava.java, it
 *  needs at least Java/OpenJDK version 8.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2022 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
  * @version 1.0.0, 2022-08-02
  * @author Rony G. Flatscher
  */

/*  changes:    2022-07-06: check array element to be of type string before calculating size
 *              2022-07-07: also show individual java.lang.Class object entries and their references
*/

/* ============================================================================= */
public class RexxAnalyzeRegistry
{
    static final String constBeanName="!{beanname}!";  // starting with exclamation mark to sort this category to top

    /** Version information on this class. */
    static final public String version="100.20220802";

    /* Private constructor to not allow instances to be created. */
    private RexxAnalyzeRegistry() {}


    private static AnalyzedCategory analyzedData = null;

    /** Returns the analyzed data, runs {@link #updateAnalyzeData()}
     *  to analyze the latest version of the BSF registry.
     *
     * @return the AnalyzedCategory object
    */
    public static AnalyzedCategory getAnalyzedData()
    {
        updateAnalyzeData();
        return analyzedData;
    }

    /** Returns the analyzed data as a string, runs {@link #updateAnalyzeData()}
     *  to analyze the latest version of the BSF registry.
     *
     * @return the analyzed data as a string
    */
    public static String getAnalyzedDataAsString()
    {
        updateAnalyzeData();
        return analyzedData.toString();
    }

    /** Forces the analysis of a new copy of the BSF registry, use @link{getAnalyzedData()} to
     *  fetch the {@link AnalyzedCategory} object.
     */
    public static void updateAnalyzeData()
    {
        // currentRajRegistry=RexxAndJava.getRajRegistry();    // get copy
        Map<String,RexxAndJava.RAJBean> currentRajRegistry=RexxAndJava.getRajRegistry();    // get copy
        Map <String,ArrayList<RexxAndJava.RAJBean>> categorizedData=categorizeData(currentRajRegistry);
        analyzedData=new AnalyzedCategory(categorizedData, currentRajRegistry.size());
    }


    /** Categorizes BSF registry data.
     *
     * @return a Map of categories with their ArrayList
    */
    static Map <String,ArrayList<RexxAndJava.RAJBean>> categorizeData(Map<String,RexxAndJava.RAJBean>  currentRajRegistry)
    {
        // create a Map: string/type ->
        HashMap <String,ArrayList<RexxAndJava.RAJBean>> myMap = new HashMap ();

        // Set <String> setKeys=currentRajRegistry.keySet();
        Set <String> setKeys=currentRajRegistry.keySet();

        try
        {
            String arrKeys[] = setKeys.toArray(new String[setKeys.size()]);   // or: ...toArray(new String[0]);

            // step 1: categorizing by "<beanname>", java-class-name -> RAJBeans to ArrayLists
            for (String key : arrKeys)
            {
                String tmpKey=key;
                if (key==null)
                {
                    continue;
                }
                RexxAndJava.RAJBean tmpValue=currentRajRegistry.get(key);

                int    pos=key.indexOf("@");        // a Java object?
                if (pos>0)
                {
                    tmpKey=key.substring(0,pos);    // extract class name
                }
                else
                {
                    tmpKey=constBeanName;           //  key (beanname) got explicitly set by BSF4ooRexx or programmer
                }

                ArrayList al = null;
                if (myMap.containsKey(tmpKey))      // already an entry for this category?
                {
                    al=myMap.get(tmpKey);           // get appropriate ArrayList
                }
                else                                // create a new category entry with an ArrayList
                {
                    al=new ArrayList<String>();
                    myMap.put(tmpKey,al);
                }

                al.add(tmpValue);                   // add current value to ArrayList
            }

        } catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(-1);
        }
        return myMap;
    }



/* ============================================================================= */
// ---------- class to analyze and maintain categorized data
    // step 2: process sorted Map:
    //         -> if primitive or String (array) then calculate and store memory size (memSize)
    // step 3: supply count, min, max, average refCount per Category
    // step 4: supply count, min, max, average memSize per Category
    //
    protected static class AnalyzedCategory
    {
        static final String strFormatTimeHeader     = "RexxAnalyzeRegistry [%1$tY-%1$tm-%1$td %1$tT.%1$tN]%n";
        static final String strFormatCategoryHeader = "RexxAnalyzeRegistry category=[%s]%n";
        static final String strFormatRefCount       = "                    - RefCount: entries=[%,7d] | references: min=[%,9d] max=[%,9d] avg=[%,9.1f] sum=[%,9d]%n";
        static final String strFormatMemSize        = "                    - MemSize:  entries=[%,7d] | references: min=[%,9d] max=[%,9d] avg=[%,9.1f] sum=[%,9d]%n";
        // BSF4ooRexx' pre-registerd beanNames are 17 characters long
        static final String strFormatBeanName       = "%-17s: refCount=[%,3d] memSize=[%,5d] getClass()=[%15s] toString()=[%s]";
        static final String strFormatClassObjectName= "%-45s: refCount=[%,5d] getName()=[%s]";

        static final int hintLength=40; // maximum length of hint string value

        String strNrRegistryEntries=null;
        public String getStrNrRegistryEntries() { return strNrRegistryEntries; }

        String strTimeHeader;
        String [] rawBeanNames=null;    // beannames defined by the programmers with refCount
        String strBeanNameWithInfos;    // ordered String rendering of rawBeanNames

        String [] strClassObjectsWithInfos=null; // for debugging java.lang.Class objects

        public String    getStrTimeHeader()        { return strTimeHeader; }
        public String [] getRawBeanNames ()        { return rawBeanNames; }
        public String    getStrBeanNameWithInfos() { return strBeanNameWithInfos; }

        String [] category;
        int    [] refCount;         // accumulated refCounts per category
        long   [] refMemSize;       // accumulated memSize per category

        public String [] getCategory()    {  return category; }
        public int    [] getRefCount()    {  return refCount; }
        public long   [] getRefMemSize()  {  return refMemSize; }

        String [] strRefCountSummary;
        String [] strMemSizeSummary;

        public String [] getStrRefCountSummary() { return strRefCountSummary; }
        public String [] getStrMemSizeSummary()  { return strMemSizeSummary; }

        private AnalyzedCategory()  // inhibit use of default constructor
        {}

        // AnalyzedCategory(Map <String,ArrayList<RexxAndJava.RAJBean>> categorizedData)
        AnalyzedCategory(Map <String,ArrayList<RexxAndJava.RAJBean>> categorizedData, int currentRajRegistrySize)
        {
            strTimeHeader=String.format(strFormatTimeHeader, Calendar.getInstance());
            // strNrRegistryEntries=String.format("RexxAnalyzeRegistry: number of entries in registry: %d\n", currentRajRegistry.size() );
            strNrRegistryEntries=String.format("RexxAnalyzeRegistry: number of entries in registry: %d\n", currentRajRegistrySize );

                // determine size of arrays
            int size=categorizedData.size();
            category           = new String [size];
            refCount           = new int    [size];
            strRefCountSummary = new String [size];
            refMemSize         = new long   [size];
            strMemSizeSummary  = new String [size];


            Set <String> catSet=categorizedData.keySet();
            String catKeys[] = catSet.toArray(new String[catSet.size()]);   // or: ...toArray(new String[0]);
            Arrays.sort(catKeys);   // now sort the array of keys

            boolean bBeanCategory=false;        // true if a bean (no '@' in name)
            boolean bClassObjectCategory=false; // true if not bBeanCategory and javaObject instanceof Class

            ArrayList alClassObject=new ArrayList();    // collect formatted infos
            int mIdx=0;
            for (String key: catKeys)   // iterate by category
            {
                bClassObjectCategory=false;
                bBeanCategory=key.equals(constBeanName);    // if so, show keys with their refCount after stats

                String strCategoryHeader=String.format(strFormatCategoryHeader, key);
                category[mIdx]=strCategoryHeader;

                ArrayList<RexxAndJava.RAJBean> al=categorizedData.get(key);
                int alSize = al.size();
                if (bBeanCategory)
                {
                    rawBeanNames=new String [alSize];
                }

                    // create array to store bean information (to allow count/min/avg/max analysis per category
                int    [] rawRefCount = new int  [alSize];
                long   [] rawMemSize  = new long [alSize];


                // sort by key, fill and calc
                for (int al_i=0;al_i<alSize;al_i++)
                {
                    RexxAndJava.RAJBean rajb=al.get(al_i);
                    int       tRefCount=rajb.refCount;
                    refCount[mIdx]   +=tRefCount;            // accumulate refCounts
                    rawRefCount[al_i] =tRefCount;

                        // if a primitive type (array) or String (array) calc memSize and store it
                    Object jo=rajb.javaObject;
                    long ms=calcSize(jo);                    // memsize of current bean

                    if (bBeanCategory)
                    {
                        String strJo=jo.toString();
                        if (strJo.length()>hintLength)
                        {
                            strJo=strJo.substring(0,hintLength)+"...";
                        }
                        rawBeanNames[al_i] =String.format(strFormatBeanName,'"'+rajb.name+'"',
                                                  tRefCount,ms,jo.getClass().getName(),
                                                  strJo);
                    }
                    else if (jo instanceof Class)
                    {
                        bClassObjectCategory=true;
                        String strJo=((Class)jo).getName();
                        String strJoHint=strJo;
                        if (strJo.length()>hintLength)
                        {
                            strJoHint=strJo.substring(0,hintLength)+"...";
                        }
                        String strTmp=String.format(strFormatClassObjectName,
                                          '"'+strJoHint+'"',
                                          tRefCount,
                                          strJo);
                        alClassObject.add(strTmp);
                    }

                    refMemSize[mIdx]+=ms;   // accumulate refCounts
                    rawMemSize[al_i] =ms;   // save
                }

                // do the analysis, save results
                // get summary statistics as int
                IntSummaryStatistics iss = Arrays.stream(rawRefCount).summaryStatistics();
                String strRefCount=String.format(strFormatRefCount, iss.getCount(), iss.getMin(),
                                                                    iss.getMax(), iss.getAverage(), iss.getSum() );
                strRefCountSummary[mIdx] = strRefCount;
                    // get summary statistics as long
                LongSummaryStatistics lss = Arrays.stream(rawMemSize).summaryStatistics();
                if (lss.getSum()==0L)
                {
                    strMemSizeSummary[mIdx]  = "";
                }
                else
                {
                    String strMemSize=String.format(strFormatMemSize, lss.getCount(), lss.getMin(),
                                                                      lss.getMax(), lss.getAverage(), lss.getSum() );
                    strMemSizeSummary[mIdx]  = strMemSize;
                }

                if (bBeanCategory)  // if programmer supplied bean names, allow to show them with their refCounts
                {
                    Arrays.sort(rawBeanNames, (String s1,String s2)-> s1.compareToIgnoreCase(s2));
                    strBeanNameWithInfos=Arrays.toString(rawBeanNames);
                }
                else if (bClassObjectCategory)
                {
                    // - turn ArrayList into String array and sort it
                    strClassObjectsWithInfos = (String[]) alClassObject.toArray(new String[alClassObject.size()]);
                    Arrays.sort(strClassObjectsWithInfos, (String s1,String s2)-> s1.compareToIgnoreCase(s2));
                }

                mIdx++;
            }
        }

        /** Creates a string rendering of the analyzed BSF registry data.
         *
         * @return string rendering of the anlyzed BSF registry data
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            char nl='\n';
            sb.append(strNrRegistryEntries) .append(nl)
              .append(strTimeHeader)        .append(nl);

            for (int i=0;i<category.length;i++)
            {
                sb.append(category[i])
                  .append(strRefCountSummary[i])
                  .append(strMemSizeSummary[i]);

                if (category[i].indexOf(constBeanName)>0)
                {
                    int l1=rawBeanNames.length;
                    int l2=(""+l1).length();
                    String fmt="\t... %"+l2+"d/%"+l2+"d: %s %n";
                    sb.append("\n\t... ["+l1+"] bean names defined explicitly by programmers:\n")
                      .append(nl);

                    int k=0;
                    for (String info: rawBeanNames)
                    {
                        sb.append(String.format(fmt,++k,l1,info));
                    }
                }
                else if (category[i].indexOf("[java.lang.Class]")>0)
                {
                    int l1=strClassObjectsWithInfos.length;
                    int l2=(""+l1).length();
                    String fmt="\t... %"+l2+"d/%"+l2+"d: %s %n";
                    sb.append("\n\t... ["+l1+"] \"java.lang.Class\" objects:\n")
                      .append(nl);

                    int k=0;
                    for (String info: strClassObjectsWithInfos)
                    {
                        sb.append(String.format(fmt,++k,l1,info));
                    }
                }
                sb.append(nl);
            }

            // get and add the information of all currently running threads
            sb.append(getAllCurrentThreadsAsStrings());
            sb.append(nl);

            return sb.toString();
        }
    }

    /** Calculates and returns the size of the supplied category data. Sizes can only
     *  be calculated for primitive types and strings.
     *
     * @param  jo category data
     * @return size of the supplied category data
    */
    static long calcSize(Object jo)
    {
        long ms=0;
        // ----------------------------- calculate approximate memory size
        if (jo!=null)
        {
            Class clz = jo.getClass();
            Class componentType = clz;      // if component type, componentType will be different to clz
            int dimensions  = 0;       // if an array, then dimensionality >=1

            if (clz.isArray())   // if an array, get the component type
            {
                dimensions=1;
                componentType=clz.getComponentType();
                while (componentType.isArray())    // the last to last component type is the type to test
                {
                    dimensions++;
                    componentType=componentType.getComponentType();
                }
            }

            if (componentType.isPrimitive())
            {
                    if (componentType==boolean.class || componentType==byte.class  ) ms=1;
               else if (componentType==char.class    || componentType==short.class ) ms=2;
               else if (componentType==int.class     || componentType==float.class ) ms=4;
               else if (componentType==long.class    || componentType==double.class) ms=8;
            }
            else
                 if (componentType==Boolean.class   || componentType==Byte.class  ) ms=1;
            else if (componentType==Character.class || componentType==Short.class ) ms=2;
            else if (componentType==Integer.class   || componentType==Float.class ) ms=4;
            else if (componentType==Long.class      || componentType==Double.class) ms=8;

                // a primitive type or java.lang.String class
            if (ms>0 || componentType==String.class || dimensions>0)   // a String (array) ?
            {
                if (dimensions>0)  // an array in hand?
                {
                        // determine the size of the individual dimensions and total entries/items
                   int [] sizeOfDimension = new int [dimensions];    // sizeOfDimension array
                   long items = 0;

                   Object tmpArray = jo;  // assign passed in array to tmpArr variable

                    // calculate the total number of elements needed
                   for (int i=0; i<dimensions; i++)     // loop over all dimensions/arrays
                   {
                       sizeOfDimension[i] = Array.getLength(tmpArray);// save the length of this dimension
                       items=(i==0 ? sizeOfDimension[0] :  items*sizeOfDimension[i]);

                       if (sizeOfDimension[i]==0)        // no array possibly left
                       {
                           break;
                       }
                       tmpArray=Array.get(tmpArray, 0);   // get next array, if any
                   }

                    if (ms>0)   // a primitive type, calc cartesian product
                    {
                        ms = ms*items;      // calc number of bytes
                    }
                    else    // iterate over all String elements and accumulate size
                    {
                        int    run[] = new int [dimensions];    // run array

                             // loop and walk the (multimensional) array(s)
                        while ( run[0]<sizeOfDimension[0] )
                        {
                            Object tmpArr=jo;                // (re-)assign master array
                            int k=dimensions-1;
                                // get array with values:
                            for (int i=0; i<k; tmpArr=Array.get(tmpArr, run[i++]));

                                // iterate over String elements
                            for (int i=0; i<sizeOfDimension[k]; i++)
                            {
                                Object o=Array.get(tmpArr, i);
                                if (o==null) continue; // empty element, ignore (only possible if an array of Object
                                if (! (o instanceof String)) continue;  // not a String value, ignore
                                ms +=((String)o).getBytes().length;  // add to memSize
                            }

                             // increment index (last dimension, carry over to the left)
                            for (int i=dimensions-2; i>=0; i--)         // increment the next index from right to left
                            {
                                run[i]=run[i]+1;             // increment present dimension

                                if (run[i]<sizeOfDimension[i]) break; // o.k. not yet at peak, incrementation went o.k.

                                 // arrived at limit, increase previous (left-hand sided) dimension by 1
                                for (int z=i-1; z>=0; z--)
                                {
                                    run[z]=run[z]+1;         // increase
                                    if (run[z]<sizeOfDimension[z])    // o.k. within limits, reset everything right to 0
                                    {
                                        for (int m=z+1; m<dimensions; m++)  // loop over all of the right dimensions
                                        {
                                            run[m]=0;        // reset the dimension
                                        }
                                        break; // o.k. this dimension could get increased within limits, leave array
                                    }
                                }
                                break;
                            }

                            if (dimensions==1) break;       // o.k., just one dimension, leave loop
                        }
                    }
                }
                else if (componentType==String.class)       // calc bytes needed
                {
                    ms=((String)jo).getBytes().length;
                }
            }
        }
        return ms;
    }


    /** Creates and returns a string of currently running threads.
     *
     * @return string listing all currently running threads
     */
    static String getAllCurrentThreadsAsStrings()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Currently running Java threads:\n\n");

        // cf. <https://www.baeldung.com/java-get-all-threads> (2022-07-21)
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        sb.append(String.format("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon"));
        for (Thread t : threads)
        {
            sb.append(String.format("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon()));
        }
        return sb.toString();
    }
}
