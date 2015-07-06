// ---rgf, 2015-06-29: Java class to allow testing the JSO functionality

package rgf.G_misc;

public class o3_Test_JSO     // "03_Test_JSO" not allowed before Java 1.7
{
    public static String staticString="This is a static Java string.";

    public static String staticStringWithUTF8umlauts=
                    "auml-\u00e4"  +
                   "-ouml-\u00f6"  +
                   "-uuml-\u00fc"  +
                   "-szlig-\u00df" +
                   "-Auml-\u00c4"  +
                   "-Ouml-\u00d6"  +
                   "-Uuml-\u00dc"  +
                   "-"
                   ;

    public static String staticStringWithUTF8icons=
                   "\ue29980 " +   //  female           = "e2 99 80"x
                   "\ue29982 " +   //  male             = "e2 99 82"x
                   "\ue29890 " +   //  ballot           = "e2 98 90"x
                   "\ue29892 " +   //  ballot_x         = "e2 98 92"x
                   "\ue29891 " +   //  ballot_check     = "e2 98 91"x
                   "\ue298a2 " +   //  radioactive      = "e2 98 a2"x
                   "\ue298a0 " +   //  skullAndBones    = "e2 98 a0"x
                   "\ue29894 " +   //  umbrellaRain     = "e2 98 94"x

                   "\ue298ba " +   //  smiley           = "e2 98 ba"x
                   "\ue298b9 " +   //  frowney          = "e2 98 b9"x
                   "\ue298bb " +   //  smileyBlack      = "e2 98 bb"x

                   "\ue298ae " +   //  peace            = "e2 98 ae"x
                   "\ue298af " ;   //  yingYang         = "e2 98 af"x

    public String instanceString="This is an instance Java string.";

    public static String getStaticString()
    {
        return staticString;
    }

    public        String getInstanceString()
    {
        return instanceString;
    }
}

