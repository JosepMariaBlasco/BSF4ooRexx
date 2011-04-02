public class Test
{
    public static void main (String args[])
    {
        String one= "a- ä"+
                   "-e- é"+
                   "-o- ö"+
                   "-u- ü"+
                   "-sz-ß"+
                   "-A- Ä"+
                   "-A- Å"+
                   "-O- Ö"+
                   "-U- Ü.";
        System.out.println("one=["+one+"]");

        // é=\u00e9
        // Å=\u00c5

        String two= "a- \u00e4"+
                   "-e- \u00e9"+
                   "-o- \u00f6"+
                   "-u- \u00fc"+
                   "-sz-\u00df"+
                   "-A- \u00c4"+
                   "-A- \u00c5"+
                   "-O- \u00d6"+
                   "-U- \u00dc.";
        System.out.println("two=["+two+"]");

    }
}

