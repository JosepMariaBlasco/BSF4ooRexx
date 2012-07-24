Needs:  ooRexx 4.0.0 or higher
Needs:  BSF4Rexx 4.0.0 or higher

These tests assert that Java PUBLIC abstract methods can be implemented in ooRexx and
that they can be invoked and return the expected value correctly.

The test unit has variants in which the return value gets boxed to the
appropriate type, but also tests that autoboxing works as well.

Files:

        ExtendNormalTest.java       ... base class
        ExtendNormalTest.class      ... compiled for Java 1.4

        Test_ExtendNormalTest.java  ... invokes all ExtendNormalTest methods
        Test_ExtendNormalTest.class ... compiled for Java 1.4

        01_rexx_testExtendNormal1.testGroup    ... Rexx unit test, Rexx classes
                                                   implement ExtendNormalTest interface methods


2009-08-28, Rony G. Flatscher

