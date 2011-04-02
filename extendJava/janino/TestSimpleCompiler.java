// 2010-04-12, rgf, wu vienna

import org.codehaus.janino.SimpleCompiler;
import java.io.File;

public class TestSimpleCompiler
{
    public static void main (String args[]) {
        File f=null;
        String fullClzName=null;

        if (args.length==0)
        {
            f=new File("Aloha.java");
            fullClzName="org.rexxla.bsf.language.onTheFly.Aloha";
        }
        else
        {
            f=new File(args[0]);
            int lpos=args[0].lastIndexOf(".");
            if (lpos>=0)
            {
                fullClzName=args[0].substring(0, lpos);
            }
            else
            {
                fullClzName=args[0];
            }

        }

        SimpleCompiler sc=new SimpleCompiler();

        try {
            sc.cookFile (f);
            System.out.println("After having cooked ["+f+"], trying to loadClass(): ["+fullClzName+"]");
            Class clz=sc.getClassLoader().loadClass(fullClzName);
            System.out.println("clz="+clz);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

