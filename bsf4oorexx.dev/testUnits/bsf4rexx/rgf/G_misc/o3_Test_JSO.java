// ---rgf, 2015-06-29: Java class to allow testing the JSO functionality

package rgf.G_misc;

public class o3_Test_JSO     // "03_Test_JSO" not allowed before Java 1.7
{
    public static String staticString="This is a static Java string.";
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

