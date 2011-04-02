public class TestFile extends java.io.File
{

    public TestFile(java.lang.String s)
    {
        super(s);
    }

    public static void main (String args[]) {
        System.out.println("in TestFile.main(...)");
    }

    public int compareTo (java.io.File f)
    {
        return super.compareTo(f);
    }

    public int compareTo (java.lang.Object o)
    {
        return super.compareTo(o);
    }


}
