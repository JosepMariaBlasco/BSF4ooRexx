2023-02-25, rgf: initial CMake for BSF4ooRexx850 according to:

    <https://cedanet.com.au/ceda/downloads/java/jni-example.php>

    <https://cmake.org/cmake/help/latest/module/FindJava.html>
    <https://cmake.org/cmake/help/latest/module/FindJNI.html>

To create the BSF4ooRexx850 library

        set JAVA_HOME
        run cmake
        run make or nmake (Windows)


---
Winodws with MS' cl

nmake clean
cmake: default: cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=DEBUG           F:\work\svn\oorexx\main\trunk
cmake:          cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RELEASE         F:\work\svn\oorexx\main\trunk
cmake:          cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RELWITHDEBINFO  F:\work\svn\oorexx\main\trunk
cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RELEASE        -DDOC_SOURCE_DIR=G:\oorexx.tmp\oorexxDocs\docs.5.1.0beta -DOS_DIST=windows F:\work\svn\oorexx\main\trunk
cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RELWITHDEBINFO -DDOC_SOURCE_DIR=G:\oorexx.tmp\oorexxDocs\docs.5.1.0beta -DOS_DIST=windows F:\work\svn\oorexx\main\trunk
cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=DEBUG          -DDOC_SOURCE_DIR=G:\oorexx.tmp\oorexxDocs\docs.5.1.0beta -DOS_DIST=windows F:\work\svn\oorexx\main\trunk
nmake


