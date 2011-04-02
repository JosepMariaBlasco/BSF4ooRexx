import java.io.File;
import java.net.URI;

public class TestFile extends java.io.File
{

    public static void main (String args[]) throws java.net.URISyntaxException
    {
        TestFile f=new TestFile(new URI("oha"));

        // TestFile s=new TestFile("aha");     // in superclass defined, but not available (not inherited)
    }


    TestFile(URI aha)
    {
        super(aha);
        // super();
    }

/*
    TestFile()          // compiler does not find it, probably because of inseritng super();
    {
        int a=1;
    }
*/

    TestFile(int a, int b) // throws java.net.URISyntaxException
    {
        super(new URI("oha"));      // this works, no super() injected by compiler: defines throwing java.net.URISyntaxException
        // super();                    // no File() constructor available, hence compiler bombs!
        // super(a,b);                 // not defined in super
        int c=a+b;

    }


    public void rony ()
    {
        System.err.println("This is the error message from method rony()...");
    }

}
