package org.rexxla.bsf.engines.rexx;

import  java.lang.reflect.*;

/**
 * This class serves as a wrapper for Java arrays. Its methods allow to fully interact
 * with arrays of any type from Rexx (and other non-Java-based languages). Names of
 * methods are called after their Object Rexx counterparts, if any.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2009 Rony G. Flatscher
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
 *
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 * @version 1.3, 2006-01-01
 */

/*
   2009-09-10, rgf, account for Thread context class loader not set
   2006-01-01, rgf, released version 1.3
   2005 ---rgf, fixed a bug when receiving an array-object with a capacity of 0
*/

public class ArrayWrapper {

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "104.20090910";


    /** Stores the array object which this instance wraps. */
    private Object arrayObject=null;


    /** Stores the total number of dimensions. */
    public int dimensions=0;

    /** Returns the size of the given dimension.
     *
     * @param i indicates the dimension of interest (Java index, i.e. first dimension to be
     *        indicated/indexed with the value <code>0</code>).
     *
     * @return the size of the given dimension.
     */
    public int dimension (int i) {
        return sizeOfDimension[i];
    }

    /** Array of <code>int</code> which stores the size (maximum number of entries) of the appropriate dimension. */
    public int [] sizeOfDimension=null;


    /** Stores the total size (maximum number of entries) of the entire array. */
    public int items=1;     // must start with one (will be multiplied with the size of the dimensions


    /* * Stores the class object used for the values stored in the array. */
    public Class componentType=null;


    /** Creates and returns an instance of this class, allowing easier access to Java arrays.
     *  The methods {@link #get(int[] idxArr)}, {@link #put(Object valueObj, int[] idxArr)},
     *  {@link #supplier()}, {@link #supplier(boolean bRexxStyle)} and
     *  {@link #makearray ( boolean bSupplier, boolean bRexxStyle ) }
     *  are modelled after <a href="http://www.ooRexx.org">ooRexx</a> (eg. cf. ooRexx class
     *  <code>Array</code>).
     *
     *  @param objArray the array object for which an ArrayWrapper instance is sought
     */
    public ArrayWrapper (Object objArray) throws java.lang.ClassNotFoundException
    {
        arrayObject=objArray;   // save the array with this instance of ArrayWrapper

        int    i=0, len=0;

            // determine total number of dimensions
        String className=objArray.getClass().getName();   // get name of class
        len=className.length();

        for (; i<len; i++)
        {
            if ('['==className.charAt(i))
            {
                dimensions++;
            }
            else break;      // no more leadin square brackets left
        }

        char elementType=className.charAt(i);   // get the element type letter

/*
 Lclassname;  class or interface

     I            int
     C            char
     Z            boolean
     B            byte
     D            double
     F            float
     S            short
     J            long
*/

        switch (elementType)
        {       // primitive component types
            case 'I':   componentType=int.class;       break;
            case 'C':   componentType=char.class;      break;
            case 'Z':   componentType=boolean.class;   break;
            case 'B':   componentType=byte.class;      break;
            case 'D':   componentType=double.class;    break;
            case 'F':   componentType=float.class;     break;
            case 'S':   componentType=short.class;     break;
            case 'J':   componentType=long.class;      break;

                // Object component type
            case 'L':   {       // extract name of class
                            String tmp="";
                            int    end=len-1;
                            for (i++; i<end; i++)
                            {
                                tmp=tmp+className.charAt(i);
                            }


                            // componentType=Class.forName(tmp);   // get appropriate class object

                            // componentType=Thread.currentThread().getContextClassLoader().loadClass(tmp);
                            // rgf, 2009-09-10
                            ClassLoader tccl=Thread.currentThread().getContextClassLoader();
                            if (tccl!=null)
                            {
                                try
                                {
                                    componentType=tccl.loadClass(tmp);
                                }
                                catch (ClassNotFoundException e1) // not found by contextClassLoader
                                {}
                            }

                            if (componentType==null)        // did not find the class
                            {
                                ArrayWrapper.class.getClassLoader().loadClass(tmp);
                            }

                        }
                        break;
        }




            // determine the size of the individual dimensions and total entries/items
       sizeOfDimension = new int [dimensions];    // sizeOfDimension array

       Object tmpArray = objArray;  // assign passed in array to tmpArr variable
// System.err.println("ArrayWrapper: tmpArray=<"+tmpArray+">, dimensions="+dimensions+", sizeOfDimension.length="+sizeOfDimension.length+", items="+items);

        // calculate the total number of elements needed
       for (i=0; i<dimensions; i++)    // loop over all dimensions/arrays
       {
           sizeOfDimension[i] = Array.getLength(tmpArray);// save the length of this dimension
           items *= sizeOfDimension[i];                 // total number of possible elements
// System.err.println("              tmpArray=<"+tmpArray+">, i="+i+", sizeOfDimension[i]="+sizeOfDimension[i]+", items="+items);

           if (sizeOfDimension[i]==0)        // no array possibly left
           {
               break;
           }
           tmpArray=Array.get(tmpArray, 0);   // get next array, if any
       }
    }




    /** Get a value (item, element) from the array at the given position.
     *
     * @param idxArr a one-dimensional array of <code>int</code>s determining the location in the array.
     *
     * @return the Object stored at the specified location.
    */
    public Object get(int[] idxArr)
    {
        int i;
        Object o=null;

        o=arrayObject;
        for (i=0; i<dimensions && i<idxArr.length; i++)
        {
            o=Array.get( o, idxArr[i] );
        }
        return o;
    }


    /** Put a value (item, element) into the array object at the given position.
     *
     * @param idxArr a one-dimensional array of <code>int</code>s determining the location in the array.
     *
     * @param valueObj the Object to store; in the case of primitives the appropriate conversions
     *                 takes place, curtesy of {@link java.lang.reflect.Array}.
     */
    public void put(Object valueObj, int [] idxArr)
    {
        int i, upperLimit=dimensions-1;

        Object o=null;

        o=arrayObject;
        for (i=0; i<upperLimit && i<idxArr.length; i++)     // find appropriate array object first
        {
            o=Array.get( o, idxArr[i] );
        }
            // now assign the value
       Array.set(o, idxArr[i], valueObj);
   }



    /** Creates a supplier object for allowing to enumerate the array together with
     *  the appropriate indices (Java style: index starts with <code>0</code> and is
     *  enclosed in square brackets).
     *
     *  @return an instance of class {@link Supplier}, allowing for enumerating the contents of the array.
     */
    public Supplier supplier ()
    {
        Object [] res=makearray(true, false);
        return new Supplier( (Object []) res[0], (Object []) res[1]);  // return the appropriate supplier object
    }


    /** Creates a supplier object for allowing to enumerate the array together with
     *  the appropriate indices.
     *
     *  @param bRexxStyle determines whether the index value is [Object] Rexx
     *         style (index starts with <code>1</code>, multiple indices are separated by a comma)
     *         or Java style (index starts with <code>0</code>, indices are enclosed in square brackets)
     *
     *  @return an instance of class {@link Supplier}, allowing for enumerating the contents of the array.
     */
    public Supplier supplier (boolean bRexxStyle)
    {
        Object [] res=makearray(true, bRexxStyle);
        return new Supplier( (Object []) res[0], (Object []) res[1]);  // return the appropriate supplier object
    }




    /** Modelled after Object Rexx <code>MAKEARRAY</code> method of the array class. This
     *  method is executed <em>synchronized</em>.
     *
     * @param bSupplier boolean value to indicate whether a {@link Supplier supplier} array (containing the appropriate indices as Strings) is to be prepared and returned (if set to <code>true</code>).
     *
     * @param bRexxStyle boolean value to indicate whether using Rexx (index starts at: <code>1</code>) or Java (index starts at: <code>0</code>) style for creating the indices for a {@link Supplier supplier}.
     *
     * @return a two-dimensional array of Objects, where the first element contains the single dimensioned array and
     *         the second element the optional supplier array
     */
    synchronized public Object [] makearray ( boolean bSupplier, boolean bRexxStyle )
    {
         // initialize ...
        int i=0, k=0, m=0,
            idx=0,
            totalCount=items;       // totalCount already calculated

        int    run[] = new int [dimensions];    // run array

        String info="", infoTail="";     // info String containing the index/indices
        Object o=null, tmpArr = arrayObject;  // assign passed in array to tmpArr variable

        Object [] resObj= {null, null},  // single-dimensioned arrays; result Object
                  newArr=null,           // new single-dimensioned array
                  newSupplier=null;      // new single-dimensioned array for supplier

//        if (totalCount==0) System.err.println("shit, now it has happened! (totalCount=0, array has no capacity, why? think about it...)");

        newArr = new Object [totalCount];   // create array with needed number of elements
        if (bSupplier)                      // supplier really requested, create the array
        {
            newSupplier = (Object[]) new Object [totalCount];
        }


             // loop and walk the array
        while ( run[0]<sizeOfDimension[0] )
        {
            tmpArr=arrayObject;       // (re-)assign master array
            k=dimensions-1;             // get array with values
            for (i=0; i<k; tmpArr=Array.get(tmpArr, run[i++]));

            info="";             // create info-string used as value for the supplier-array
            for (int o2=0; o2<(dimensions-1); o2++)
            {
                if (bRexxStyle)          // Object Rexx type array, index starts with '1'
                    info=info+(run[o2]+1)+",";
                else                     // Java type array, index starts with '0'
                    info=info+"["+run[o2]+"]";
            }

            for (i=0; i<sizeOfDimension[k]; i++)  // loop over value-array and assign newArr, if element not null
            {
                o=Array.get(tmpArr, i);    // get value object
                if (o==null) continue;   // empty element, ignore (only possible if an array of Object

                if (bRexxStyle)          // Object Rexx type array, index starts with '1'
                    infoTail=""+(i+1);
                else                     // Java type array, index starts with '0'
                    infoTail="["+i+"]";

                if (bSupplier) newSupplier [idx] = info+infoTail;
                newArr[idx++] = o;           // save element in new array
            }

             // increment index (last dimension, carry over to the left)
            for (i=dimensions-2; i>=0; i--)         // increment the next index from right to left
            {
                run[i]=run[i]+1;             // increment present dimension

                if (run[i]<sizeOfDimension[i]) break; // o.k. not yet at peak, incrementation went o.k.

                 // arrived at limit, increase previous (left-hand sided) dimension by 1
                for (int z=i-1; z>=0; z--)
                {
                    run[z]=run[z]+1;         // increase
                    if (run[z]<sizeOfDimension[z])    // o.k. within limits, reset everything right to 0
                    {
                        for (m=z+1; m<dimensions; m++)  // loop over all of the right dimensions
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


        if (totalCount != idx)                   // only create a new array, if dimensions differ
        {
            Object [] resArr = new Object [idx], // create an array with the right dimensions
                      resSupplier = null;        // create an array variable
            if (bSupplier)
                resSupplier = new Object [idx];  // create the array for the supplier now

            for (i=0; i<idx; i++)                // loop over all elements
            {
                resArr[i]=newArr[i];             // assign values to new array
                if (bSupplier)
                    resSupplier[i]=newSupplier[i];
            }
            newArr=resArr;
            newSupplier=resSupplier;
        }

        resObj[0]=newArr;                    // use the working array as result
        resObj[1]=newSupplier;               // return the supplier array object

        return resObj;
    }

}

