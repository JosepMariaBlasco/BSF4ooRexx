// 2010-04-12, rgf, wu vienna

import org.codehaus.janino.SimpleCompiler;
import java.io.File;

public class TestSimpleCompiler
{
    public static void main (String args[]) {
        File f=new File("Aloha.java");
        SimpleCompiler sc=new SimpleCompiler();

        try {
            sc.cookFile (f);
            Class clz=sc.getClassLoader().loadClass("org.rexxla.bsf.language.onTheFly.Aloha");
            System.out.println("clz="+clz);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

