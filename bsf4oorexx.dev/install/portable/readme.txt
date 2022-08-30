2022-02-07, 2022-08-30, rgf

The following scripts need to reside in the same directory:

        createPortableB4R.rex ... creates portable versions of BSF4ooRexx850
        setupbsf4oorexx.rex   ... will create the rexxj.{cmd|sh} and setenv2bsf4oorexx.{cmd|sh}
        testbsf4oorexx.rex    ... test script for portable BSF4ooRexx850
        testuno.rex           ... test script for portable BSF4ooRexx850 OOo/AO and LO support

To create portable BSF4ooRexx850 versions:

        - unzip the BSF4ooRexx850 installation package in your working directory, a subdirectory
          named "bsf4ooRexx" will get created

        - run "path-to\createPortableB4R op_sys bitness machine" in your working directory

                op_sys  ... L(inux) W(indows) D(arwin)
                bitness ... 32 or 64
                machine ... "x" (Intel) "amd" (Intel/AMD) "arm" (ARM) "s390x"

          a subdirectory _portable_ gets created and according to the arguments the
          appropriate zip archives get created in the workding directory, e.g. for
          BSF.CLS with the version set to "641.20220131" for 32-bit Windows:

          07.02.2022  14:11         1 530 940 bsf4oorexx_v641.20220131-Windows-x86-portable-UNO-runtime.zip
          07.02.2022  14:11        10 872 843 bsf4oorexx_v641.20220131-Windows-x86-portable-UNO.zip

          ... "runtime" does not contain the documentation and the samples
          ... "UNO" indicates that UNO support is included such that users with
              Apache OpenOffice or LibreOffice installed can run BSF4-ooRexx
              scripts against them (e.g. cf. samples/OOo)


