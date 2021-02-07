package org.rexxla.bsf.engines.rexx;

import java.util.*;
import org.apache.bsf.BSFException;

/** Class modelled after the <a href="http://www.ooRexx.org">ooRexx</a> class &quot;Supplier&quot;. Needs the {@link ArrayWrapper} class.
 *  Allows iterating over an array (regardless of its dimension). In the case an entry of the array is
 *  <code>null</code>, that entry is not reflected in the supplier object.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2021 Rony G. Flatscher
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
  * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 * @version 1.06, 20210204
 */

 /*
    2021-02-04, ---rgf: fix bug in nextElement(): skipping first index-element pair and causing
                        a runtime exception when a Java array gets used in DO WITH ... OVER
    2017-06-17, ---rgf: added BSFException to ArrayWrapper, need to declare it
    2009-09-10, ---rgf: adjust for change in ArrayWrappter, which now throws a ClassNotFound exception
    2007-07-07, ---rgf: added allIndexes() and allItems() method to match ooRexx 3.2.0.
 */
public class Supplier implements Enumeration
{
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "106.20210204";

static final boolean bDebug=false;  // rgf, 2021-02-04, leave until ooRexx DO..WITH got debugged

    /** Private array storing the values of the array. */
    private Object values[]=null;

    /** Private array storing the indices of the array in form of Strings. */
    private Object indices[]=null;

    /** Stores the index of the index/value pair pointing at. */
    private int index=0;

    /** Stores the index value at the time of invoking allIndexes() or allItems() for
     *  the first time, which in effect &quot;exhausts&quot; the supplier.
     */
    private int cutOffIndex=-1;

    /** Stores the length of the arrays. */
    private int length=0;


    /** Returns single dimensioned (Java) array object containing the remaining indexes. Like in
     *  ooRexx 3.2.0 the array will only contain all elements starting from the current
     *  pointed to element on (i.e., index/item pairs already processed will not show
     *  up in the returned array object). The supplier object itself is considered to be
     *  exhausted from this time on, such that {@link #available()} will return <pre>false</pre>.
     *
     * @return single dimensioned array object containing the remaining indexes.
     */
    public Object [] allIndexes()
    {
        // create a new array object of correct size
        if (cutOffIndex<0)      // neither allIndexes() nor allItmes() invoked so far
        {
            cutOffIndex=index;  // save current index
            index=length;       // make sure available() will return false from now on
        }

        Object [] newArr = new Object [length-cutOffIndex];   // create array with needed number of elements

        int idx=cutOffIndex;
        for (int i=0; idx<length; i++, idx++)
        {
            newArr[i]=indices[idx];
        }
        return newArr;
    }


    /** Returns single dimensioned (Java) array object containing the remaining items. Like in
     *  ooRexx 3.2.0 the array will only contain all elements starting from the current
     *  pointed to element on (i.e., index/item pairs already processed will not show
     *  up in the returned array object). The supplier object itself is considered to be
     *  exhausted from this time on, such that {@link #available()} will return <pre>false</pre>.
     *
     * @return single dimensioned array object containing the remaining items.
     */
    public Object [] allItems()
    {
        // create a new array object of correct size
        if (cutOffIndex<0)      // neither allIndexes() nor allItmes() invoked so far
        {
            cutOffIndex=index;  // save current index
            index=length;       // make sure available() will return false from now on
        }

        Object [] newArr = new Object [length-cutOffIndex];   // create array with needed number of elements

        int idx=cutOffIndex;
        for (int i=0; idx<length; i++, idx++)
        {
            newArr[i]=values[idx];
        }
        return newArr;
    }


    /** Tests whether an index/value pair is available.
     *
     * @return <code>true</code>, if an index/value pair is available, <code>false</code> else.
     */
    public boolean available()
    {
        return (length>0 && index<length);  //
    }

    /** Move to the next item/value pair, if any.
     */
    public void next()
    {
if (bDebug) System.err.println("Supplier.next(): just increasing index by 1 resulting in: "+(index+1)+"], length="+length);
        index++;            // position on next item/value pair
    }

    /** Returns the item (value) part of the supplier object.
     *
     * @return the item (value) part of the supplier object
     */
    public Object item()
    {
if (bDebug) System.err.println("Supplier.item(): returning values["+index+"]=["+values[index]+"]"
                   +" | indices[index] would be: "+indices[index]);
        return values[index];   // return the item (value)
    }

    /** Returns the index part of the supplier object.
     *
     * @return the index part of the supplier object
     */
    public Object index()
    {
if (bDebug) System.err.println("Supplier.index(): returning indices["+index+"]=["+indices[index]+"]");
        return indices[index];  // return the index
    }


    /** Creates an instance of the class using the {@link ArrayWrapper} class.
     *
     * @param arrObject an array (of any dimension)
     * @param bRexxStyle <code>true</code> format index-part in Object Rexx style (indexing starts with
     *         <code>1</code> and multiple indices are delimited with commas), <code>false</code> format index-part in
     *         Java style (indexing starts with <code>0</code> and multiple indices are delimited by
     *         enclosing each index in square brackets)
     */
    public Supplier (Object arrObject, boolean bRexxStyle) throws java.lang.ClassNotFoundException, BSFException
    {

if (bDebug) System.err.println(this+": Supplier(Object[] arrObject, boolean bRexxStyle="+bRexxStyle+") ...");

        // ArrayWrapper aw = ArrayWrapper.getArrayWrapper(arrObject);  // wrap up the array
        ArrayWrapper aw = new ArrayWrapper(arrObject);  // wrap up the array
        Object objArr [] = aw.makearray(true, bRexxStyle); // create the two arrays
        values  = (Object []) objArr[0];
        indices = (Object []) objArr[1];
        length=indices.length;
    }

    /** Creates an instance of the class using the {@link ArrayWrapper} class. Uses the Java style
     *  of indexing for the index part of the supplier object.
     *
     * @param arrObject an array (of any dimension)
     */
    public Supplier (Object arrObject) throws java.lang.ClassNotFoundException, BSFException
    {
        this(arrObject, false);             // default: use Java style indexing (starts with 0)
    }


    /** Creates an instance of the class.
     *
     * @param values a single-dimensioned array containing the values (items)
     * @param indices a single-dimensioned array containing the indices of <code>v</code>
     */
    public Supplier (Object [] values, Object [] indices)
    {
if (bDebug) System.err.println(this+": Supplier(Object[] values="+values+", Object[] indices="+indices+") ...");
if (bDebug) System.err.println(this+": Supplier(Object[] values.length="+values.length+", Object[] indices.length="+indices.length+") ...");
        this.values  = values;              // save values (items) array
        this.indices = indices;             // save indices array
        length  = this.indices.length;      // save length (number of entries) of array
/*
--- */
    }



    // Enumeration interface implementation
    /** Implements the interface {@link java.util.Enumeration#hasMoreElements() java.util.Enumeration.hasMoreElements()}.
     */
    public boolean hasMoreElements()
    {
        return this.available();
    }

    /** Implements the interface {@link java.util.Enumeration#nextElement() java.util.Enumeration.nextElement()}.
     *
     * @exception NoSuchElementException raised, if using the Enumerator interface and moving
     *                                   beyond the end of the supplier object.
     */
    public Object nextElement() throws NoSuchElementException
    {
        if (index>=length)
        {
            throw new NoSuchElementException( this+": exceeding total number of ["+length+"] elements!");
        }
        Object result=this.item();
        this.next();            // move to next index/element pair

        return result;
    }
}

