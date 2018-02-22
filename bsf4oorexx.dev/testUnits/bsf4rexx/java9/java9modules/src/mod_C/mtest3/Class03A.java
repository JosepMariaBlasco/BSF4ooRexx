package mtest3;

public class Class03A extends mtest2.Class02A
{
//    public static String myClassName = "class-mtest3.Class03A";

    public static void main (String args[]) {
        System.err.println(Class03A.class+".main() ...");

        System.err.println("       getMyClassNameStatic()=["+  getMyClassNameStatic()+"]");
        System.err.println("       myClassName           =["+  myClassName           +"]");
        System.err.println("       getMyName1()          =["+  getMyName1()          +"]");
        Class03A o=new Class03A();
        System.err.println("     o.getMyName2()          =["+o.getMyName2()          +"]");
        System.err.println("     o.myClassName           =["+o.myClassName           +"]");
    }

}
