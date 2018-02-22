This is targeted at Java 1.8/8 or higher, reason: Java 8 introduced static and default
interface methods, which get among other things tested here.

Windows:

        set    CLASSPATH=%CLASSPATH%;..;.

or absolute:

        set    CLASSPATH=%CLASSPATH%;%cd%\..;%cd%

---

Unix:

        export CLASSPATH=$CLASSPATH;..;.

or absolute:

        export CLASSPATH=$CLASSPATH;`pwd`/..;`pwd`

---rgf, 2018-02-10
