// This does NOT compile under javac from Sun's JDK6
// Reason: overriding the bridge method "compareTo(Object o)"

public class Dora extends java.io.File
{
    public Dora(java.lang.String a1)
    {
            super(a1);
    }

    public int compareTo(java.io.File o)
    {
        return super.compareTo(o);
    }


// in Java 6: overriding a bridged method from superclass causes a compiler error "name clash", so
// uncommenting the following synthetic, bridged method in Java 6 causes the following error:
//
//     Dora.java:1: name clash: compareTo(java.lang.Object) in Dora and compareTo(T) in
//     java.lang.Comparable<java.io.File> have the same erasure, yet neither overrides the other
    public int compareTo(Object o)      // in "generics Java" a synthetic bridge method in java.io.File
    {
        return super.compareTo((java.io.File) o);
    }
}

