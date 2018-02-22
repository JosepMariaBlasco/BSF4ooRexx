20180113, ---rgf; setting up three modules for testing correct Java 9 module refeflection

        e.g. cf. <http://www.logicbig.com/tutorials/core-java-tutorial/modules/quick-start/>,
                 <https://blog.codefx.org/java/java-module-system-tutorial/>


Three modules:

    src\

         mod_A\   ... open to all
                    package "mtest1" with public:    abstract class "Class01A implements interface I01A"
                                          protected: class "Class01B implements interface I01B"

        mod_B\   ... open to mod_C only
                    package "mtest2" with public:    class "Class02A" extends "Class01A" implements interface "I02A" which extends interface "I01A"
                                          protected: class "Class02B extends Class01B", interface "I02B extends I01B"

        mod_C\   ... open to all, requires mod_B
                    package "mtest3" with public:    class "Class03A extends Class02A", "Class03B extends Class02b"

Using classes from "mod3":

        - members of "mod2" can be reflected, but not accessed
        - Java 9 : a warning will be given at runtime if accessing non-open members
        - Java 10: an error will be given at runtime if accessing non-open members



