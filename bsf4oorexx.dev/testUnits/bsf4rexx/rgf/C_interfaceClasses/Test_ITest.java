// rgf, 2009-08-26
// define a class that uses an ITest object, which now can also be a RexxProxy
// that declares to implement the "ITest" interface, i.e. all its methods;
// the RexxProxy implementation may take advantage of the ooRexx UNKNOWN mechanism
// - rgf, 2011-02: added "bShowInfos" to control additional new debug output

package rgf.C_interfaceClasses;

import org.rexxla.bsf.engines.rexx.RexxProxy;

public class Test_ITest
{
    final boolean bShowInfos=true;

    public void test_ITest (ITest itest) throws Exception
    {
if (bShowInfos) System.err.println(this+".test_ITEST: "+"itest="+itest);
        RexxProxy rp=itest.getTestRunner();
if (bShowInfos) System.err.println("rp=["+rp+"]");

            // inverse value
        String hintText="itest.boolean_Method(true)";
        Object argArray[]=null;
        if (itest.boolean_Method(true)!=false)
        {
if (bShowInfos) System.err.println(this+".test_ITEST: "+"1a");

            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception (hintText);
        }
        else
        {
if (bShowInfos) System.err.println(this+".test_ITEST: "+"1b");

            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }
if (bShowInfos) System.err.println(this+".test_ITEST: "+"1-done.");


        //-------
        hintText="itest.Boolean_Method(Boolean.TRUE).booleanValue()";
        if (itest.Boolean_Method(Boolean.TRUE).booleanValue()!=false)
        {
if (bShowInfos) System.err.println(this+".test_ITEST: "+"2a");

            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Boolean_Method(Boolean.TRUE).booleanValue()");
        }
        else
        {
if (bShowInfos) System.err.println(this+".test_ITEST: "+"2b");

            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }
if (bShowInfos) System.err.println(this+".test_ITEST: "+"2-done.");


if (bShowInfos) System.err.println(this+".test_ITEST: "+"3");
            // half value, Rexx: value%2
            // boolean/Boolean
        //-------
        hintText="";
        if (itest.byte_Method((byte)-128)!= -64)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.byte_Method(-128)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.byte_Method((byte)127)!=63)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.byte_Method(127)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Byte_Method(new Byte(Byte.MIN_VALUE)).byteValue()!= -64)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Byte_Method(new Byte(Byte.MIN_VALUE)).byteValue()");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
            if (itest.Byte_Method(new Byte(Byte.MAX_VALUE)).byteValue()!=63)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Byte_Method(new Byte(Byte.MAX_VALUE)).byteValue()");
        }
            else
            {
                rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
            }

if (bShowInfos) System.err.println(this+".test_ITEST: "+"char/Character, rp=["+rp+"]");

            // char/Character: return value unchanged
            //-------
            hintText="";
        if (itest.char_Method('A')!=('A'))
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.char_Method('A')");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

if (bShowInfos) System.err.println(this+".test_ITEST: "+"'ß', # 1, rp=["+rp+"]");
        //-------
	char testChar='ß';
        hintText=this+": testChar=["+testChar+"]==[ß], itest.char_Method(testChar) == (testChar),  itest.char_Method('testChar')=["+itest.char_Method(testChar)+"]";

        if (itest.char_Method(testChar)!= (testChar))
        {
if (bShowInfos) System.err.println("\t'ß'!='ß': char=["+itest.char_Method('ß')+"]");
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.char_Method('ß')");
        }
        else
        {
if (bShowInfos) System.err.println("\telse branch: \\('ß'!='ß'): char=["+itest.char_Method('ß')+"]");

            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

/* // these only would work, if Rexx was fully Unicode aware
        if (itest.Character_Method(new Character(Character.MIN_VALUE))!= Character.MIN_VALUE)
        {
            throw new Exception ("itest.Character_Method(new Character(Character.MIN_VALUE))");
        }

        if (itest.Character_Method(new Character(Character.MAX_VALUE))!= Character.MAX_VALUE)
        {
            throw new Exception ("itest.Character_Method(new Character(Character.MAX_VALUE))");
        }
*/

if (bShowInfos) System.err.println(this+".test_ITEST: "+"'A'");
        //-------
        hintText="";
        if (itest.Character_Method(new Character('A')).charValue()!= 'A')
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Character_Method(new Character('A'))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

if (bShowInfos) System.err.println(this+".test_ITEST: "+"'ß', # 2");
        //-------
        hintText="";
        if (itest.Character_Method(new Character('ß')).charValue()!= 'ß')
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Character_Method(new Character('ß'))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"short/Short");
            // short/Short
        //-------
        hintText="";
        if (itest.short_Method((short)-32768)!= -16384)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.short_Method(-32768)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.short_Method((short)32767)!= 16383)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.short_Method(32767)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Short_Method(new Short(Short.MIN_VALUE)).shortValue()!= -16384)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Short_Method(new Short(Short.MIN_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.Short_Method(new Short(Short.MAX_VALUE)).shortValue()!= 16383)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Short_Method(new Short(Short.MAX_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"int/Integer");

            // int/Integer
        //-------
        hintText="";
        if (itest.int_Method(-2147483648)!= -1073741824)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.int_Method(-2147483648)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.int_Method(2147483647)!= 1073741823)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.int_Method(2147483647)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Integer_Method(new Integer(Integer.MIN_VALUE)).intValue()!= -1073741824)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Integer_Method(new Integer(Integer.MIN_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.Integer_Method(new Integer(Integer.MAX_VALUE)).intValue()!= 1073741823)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Integer_Method(new Integer(Integer.MAX_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"long/Long");
            // long/Long
        //-------
        hintText="";
        if (itest.long_Method(-9223372036854775808L)!= -4611686018427387904L)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.long_Method(-2147483648)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.long_Method(9223372036854775807L)!= 4611686018427387903L)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.long_Method(2147483647)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Long_Method(new Long(Long.MIN_VALUE)).longValue()!= -4611686018427387904L)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Long_Method(new Long(Long.MIN_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.Long_Method(new Long(Long.MAX_VALUE)).longValue()!= 4611686018427387903L)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Long_Method(new Long(Long.MAX_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"float/Float");
            // float/Float: return same value
        //-------
        hintText="";
        if (itest.float_Method(Float.MIN_VALUE)!= Float.MIN_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.float_Method(Float.MIN_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.float_Method(Float.MAX_VALUE)!= Float.MAX_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.float_Method(Float.MAX_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Float_Method(new Float(Float.MIN_VALUE)).floatValue()!= Float.MIN_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Float_Method(new Float(Float.MIN_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.Float_Method(new Float(Float.MAX_VALUE)).floatValue()!= Float.MAX_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Float_Method(new Float(Float.MAX_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"double/Double");
            // double/Double: return same value
        //-------
        hintText="";
        if (itest.double_Method(Double.MIN_VALUE)!= Double.MIN_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.double_Method(Double.MIN_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.double_Method(Double.MAX_VALUE)!= Double.MAX_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.double_Method(Double.MAX_VALUE)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="";
        if (itest.Double_Method(new Double(Double.MIN_VALUE)).doubleValue()!= Double.MIN_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Double_Method(new Double(Double.MIN_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }

        //-------
        hintText="";
        if (itest.Double_Method(new Double(Double.MAX_VALUE)).doubleValue()!= Double.MAX_VALUE)
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Double_Method(new Double(Double.MAX_VALUE))");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


if (bShowInfos) System.err.println(this+".test_ITEST: "+"String");
            // String: returns last three chars
        //-------
        hintText="";
        if (! (itest.String_Method("One, two, three").equals("ree")))
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.String_Method(\"One, two, three\")");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


           // String: returns supplied object
        //-------
        hintText="";
        if (itest.Object_Method(this).hashCode()!=this.hashCode())
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.Object_Method(o).hashCode()");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


        //-------
        hintText="itest.void_Method()";
           // just sendMessage the void test method
        itest.void_Method();        // just sendMessage that method
        rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});


            // test all arguments, Rexx will return them as a concatenated string
        String strExpected=//"0 1 -3 3 A B -4 4 -5 5 -6 6 -7 7 -8 8";
                             "0 1 -3 3 A B -4 4 -5 5 -6 6 -7.0 7.0 -8.0 8.0";

        //-------
        hintText="!(itest.all_args_Method(...)";
        if ( !(itest.all_args_Method(
                     false          , Boolean.TRUE,
                     (byte) -3      , new Byte((byte)3),
                     'A'            , new Character('B'),
                     (short) -4     , new Short((short)4),
                     (int)   -5     , new Integer(5),
                             -6L    , new Long(6),
                             -7f    , new Float(7),
                             -8d    , new Double(8)

                                  ).equals(strExpected))
           )
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.FALSE, hintText});
            // throw new Exception ("itest.all_args_Method(...)");
        }
        else
        {
            rp.sendMessage("assertTrue", new Object[] {Boolean.TRUE, hintText});
        }


    }
}


