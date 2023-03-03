# CMakeLists.txt (according to <https://cedanet.com.au/ceda/downloads/java/jni-example.php>, 2023-02-25)
# purpose: allow creating library
#
# - RPATH related information for Linux & Apple, cf. (2023-03-02, rgf)
#   - <https://stackoverflow.com/questions/43551483/how-to-set-rpath-in-cmake>
#   - <https://dev.my-gate.net/2021/08/04/understanding-rpath-with-cmake/>
#   - <https://gitlab.kitware.com/cmake/community/-/wikis/doc/cmake/RPATH-handling/diff?version_id=592082d367c069c348427dafdea61696fd9eafa0>

# step # 1: create BSF4ooRexx850 library (2023-02-25, rgf)

cmake_minimum_required(VERSION 3.5)
# cmake_minimum_required(VERSION 3.16)

set(CMAKE_VERBOSE_MAKEFILE on)  # show make's commands for debugging

project(BSF4ooRexx850 VERSION 0.2 LANGUAGES CXX)

## --- ooRexx libraries ---
link_libraries(rexx rexxapi ${CMAKE_REQUIRED_LIBRARIES})


## --- Java section ---
# testing finding Java (eventually we can add compiling Java sources, doing "javac -h" etc.)
find_package(Java 1.8 REQUIRED) # BSF4ooRexx850 has Java "8" a.k.a. "1.8" as minimum

message(STATUS "    JAVA_HOME               = ${JAVA_HOME}")
message(STATUS "    Java_JAVA_EXECUTABLE    = ${Java_JAVA_EXECUTABLE}")
message(STATUS "    Java_JAVAC_EXECUTABLE   = ${Java_JAVAC_EXECUTABLE}")
message(STATUS "    Java_JAVAH_EXECUTABLE   = ${Java_JAVAH_EXECUTABLE}")
message(STATUS "    Java_JAVADOC_EXECUTABLE = ${Java_JAVADOC_EXECUTABLE}")
message(STATUS "    Java_VERSION_STRING     = ${Java_VERSION_STRING}")
message(STATUS "    Java_VERSION            = ${Java_VERSION}")

# --- Java native interface (JNI) section ---
find_package(JNI REQUIRED)

if (JNI_FOUND)
    message (STATUS "    JNI_INCLUDE_DIRS = ${JNI_INCLUDE_DIRS}")
    message (STATUS "    JNI_LIBRARIES    = ${JNI_LIBRARIES}")
    message (STATUS "    JNI_VERSION      = ${JNI_VERSION}")
    message (STATUS "    JNI_VERSION_PATCH= ${JNI_VERSION_PATCH}")
endif()
include_directories(BSF4ooRexx850 PRIVATE ${JNI_INCLUDE_DIRS} )


## --- compile options for BSF4ooRexx.cc ---

# define BSF4Rexx bitness flag
if (CMAKE_SIZEOF_VOID_P MATCHES 8)
    set(BSF4REXX_BITNESS "64")
else ()
    set(BSF4REXX_BITNESS "32")
endif ()
set(BSF4REXX_BITNESS_FLAG "BSF4REXX_${BSF4REXX_BITNESS}_BIT")

# define WINDOWS flag if on Windows
if (WIN32)
    set (DEF_B4R_OPSYS_FLAG "-DWINDOWS")
    # MSC turn off sscanf related warnings:
    set (DEF_B4R_CRT_SECURE_NO_WARNINGS "-D_CRT_SECURE_NO_WARNINGS")
    set (WIN_REXX_HOME "$ENV{REXX_HOME}")       # get value from environment
    include_directories(${WIN_REXX_HOME}/api)
    link_directories(${WIN_REXX_HOME}/api)

else () # UNIX

    set (DEF_B4R_OPSYS_FLAG "-DUNIX")
    include_directories(/usr/local/include)
    link_directories(/usr/local/lib)

    # ad RPATH, cf. <https://gitlab.kitware.com/cmake/community/-/wikis/doc/cmake/RPATH-handling>
    if (APPLE)
        # to remove warning introduced in 3.9, cf. https://cmake.org/cmake/help/latest/policy/CMP0068.html
        cmake_policy(SET CMP0068 NEW)
        SET (CMAKE_MACOSX_RPATH TRUE)

        set( CMAKE_OSX_DEPLOYMENT_TARGET 10.13.6 CACHE STRING  "" FORCE)    # this version has M1 support
        if( BUILD_OSX_UNIVERSAL_BINARIES )  # build universal (fat) library?
          set( CMAKE_OSX_ARCHITECTURES arm64 x86_64 )
        endif()
    endif ()

    # use, i.e. don't skip the full RPATH for the build tree
    set(CMAKE_SKIP_BUILD_RPATH FALSE)

    # when building, don't use the install RPATH already
    # (but later on when installing)
    set(CMAKE_BUILD_WITH_INSTALL_RPATH TRUE)     # Linux: will set RUNPATH, installed library to RPATH instead

    if (APPLE)
        # check rpath: otool l libBSF4ooRexx850.dylib | grep -3 PATH
        SET (CMAKE_INSTALL_RPATH "@loader_path;@loader_path/../lib;/usr/local/lib;/opt/ooRexx/lib;/Library/Frameworks/ooRexx.framework/Libraries")
    else () # not all Unixes may define $ORIGIN
        # check rpath: objdmp -p libBSF4ooRexx850.dylib | grep PATH
        SET (CMAKE_INSTALL_RPATH "$ORIGIN;$ORIGIN/../lib;/usr/local/lib64;/usr/lib64;/usr/local/lib;/usr/lib;/opt/ooRexx/lib")
    endif ()

    # add the automatically determined parts of the RPATH
    # which point to directories outside the build tree to the install RPATH
    # set (CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)

    set (INSTALL_LIB_DIR ${CMAKE_INSTALL_LIBDIR})

endif ()


add_compile_options(${DEF_B4R_OPSYS_FLAG} -D${BSF4REXX_BITNESS_FLAG} ${DEF_B4R_CRT_SECURE_NO_WARNINGS}) # MSC turn off sscanf related warnings
message(STATUS "... add_compile_options: [${DEF_B4R_OPSYS_FLAG} -D${BSF4REXX_BITNESS_FLAG} ${DEF_B4R_CRT_SECURE_NO_WARNINGS}]")

## needs to be placed after the compile options
add_library(BSF4ooRexx850 SHARED BSF4ooRexx.cc)


## install-rule just for testing purposes, to test installing in build dir use:
##         make DESTDIR=. install -> ./usr/local/libBSF4ooRexx850.dylib

## NOTE: - Linux: the build version sets RUNPATH, installing changes it to RPATH !
install (TARGETS BSF4ooRexx850
#         ARCHIVE DESTINATION ./rgflib
#         LIBRARY DESTINATION ./rgflib
#         RUNTIME DESTINATION ./rgfbin)
        )
# done.
