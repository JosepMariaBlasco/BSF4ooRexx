Needs:  ooRexx 4.0.0 or higher
Needs:  BSF4Rexx 4.0.0 or higher
Needs:  CLASSPATH pointing to this directory

These tests assert that Java interface methods can be implemented in ooRexx and
that they can be invoked and return the expected value correctly.

The test unit has variants in which the return value gets boxed to the
appropriate type, but also tests that autoboxing works as well.

Files:

        ITest.java              ... interface class
        ITest.class             ... compiled for Java 1.4

        Test_ITest.java         ... invokes all ITest methods
        Test_ITest.class        ... compiled for Java 1.4

        rexx_testInterface1.testGroup   ... Rexx unit test, Rexx classes
                                            implement ITest interface methods


2009-08-28, Rony G. Flatscher
