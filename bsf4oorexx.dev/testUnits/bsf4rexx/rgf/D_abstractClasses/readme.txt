Needs:  ooRexx 4.0.0 or higher
Needs:  BSF4Rexx 4.0.0 or higher

These tests assert that Java PUBLIC abstract methods can be implemented in ooRexx and
that they can be invoked and return the expected value correctly.

The test unit has variants in which the return value gets boxed to the
appropriate type, but also tests that autoboxing works as well.

Files:

        AbstractTest.java       ... interface class
        AbstractTest.class      ... compiled for Java 1.4

        Test_AbstractTest.java  ... invokes all ITest methods
        Test_AbstractTest.class ... compiled for Java 1.4

        rexx_testAbstract1.testGroup    ... Rexx unit test, Rexx classes
                                            implement ITest interface methods


2009-08-28, Rony G. Flatscher

