This package contains the BSF4Rexx 4.x test units.

You may want to check for the following:

        - all Java classes need to be compiled for Java 1.4; you can achieve this by doing:

                javac -source 1.4 -target 1.4 *.java

        - the CLASSPATH environment variable must point to the directory named "bsf4rexx";
          reason: the testUnit Java names are defined to belong to a package that starts with
          "rgf." that resides in "bsf4rexx".

        - You need the ooRexx testing framework from the ooRexx development site; you can
          arrive there going through <http://www.ooRexx.org> (contains a Wiki about creating
          test units with the ooRexx test unit framework), and look for "Sourceforge" links
          to follow to the ooRexx homepage there.

          - A version of the ooRexx test unit framework of 2009-10-20 is included in the
            subdirectory "ooRexxUnit". You can add temporarily a path (and classpath) to it
            issuing:

                setEnv4bsf4rexx.cmd             (Windows)
                . ./setEnv4bsf4rexx.sh          (Unix)

        - Run testUnit tests:

             - all tests:

                        run_allTests.cmd
                        ./run_allTests.sh

             - only the ooRexx tests ("bsf4rexx/rgf"):
                        run_BSF4ooRexx_Tests.cmd
                        ./run_BSF4ooRexx_Tests.sh

             - only the Rexx tests ("bsf4rexx/lex"):
                        run_BSF4Rexx_Tests.cmd
                        ./run_BSF4Rexx_Tests.sh


If there are any questions, please post them to <news:comp.lang.rexx> or become a member of
the non-profit Rexx Language Association (RexxLA), cf. <http://www.RexxLA.org> and use the
internal mailing list there.

Rony G. Flatscher
2009-12-08

