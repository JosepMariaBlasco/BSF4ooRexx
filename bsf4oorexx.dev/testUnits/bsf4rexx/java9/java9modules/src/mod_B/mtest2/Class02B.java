package mtest2;

public class Class02B extends mtest1.Class01B implements I02B
{
    // public static String myClassName = "class-mtest2.Class02B";

    public static void main (String args[]) {
        System.err.println(Class02B.class+".main() ...");

        System.err.println("       getMyClassNameStatic()=["+  getMyClassNameStatic()+"]");
        System.err.println("       getMyName1()          =["+  getMyName1()          +"]");
        Class02B o=new Class02B();
        System.err.println("     o.getMyName2()          =["+o.getMyName2()          +"]");
        System.err.println("     o.getMyClassName()      =["+o.getMyClassName()      +"]");
    }

}
