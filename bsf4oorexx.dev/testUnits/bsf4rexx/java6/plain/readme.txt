This is aimed against the new baseline, Java 1.6/6.0.

Windows:

        set    CLASSPATH=%CLASSPATH%;..;.

or absolute:

        set    CLASSPATH=%CLASSPATH%;%cd%\..;%cd%

---

Unix:

        export CLASSPATH=$CLASSPATH;..;.

or absolute:

        export CLASSPATH=$CLASSPATH;`pwd`/..;`pwd`

---rgf, 2018-02-17
