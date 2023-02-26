# CMakeLists.txt (according to <https://cedanet.com.au/ceda/downloads/java/jni-example.php>, 2023-02-25)
# step # 1: create BSF4ooRexx850 library (2023-02-25, rgf)

cmake_minimum_required(VERSION 3.5)

project(BSF4ooRexx850 VERSION 0.2 LANGUAGES CXX)

## --- ooRexx libraries ---
link_libraries(rexx rexxapi ${CMAKE_REQUIRED_LIBRARIES})


## --- Java section ---
# testing finding Java (eventually we can add compiling Java sources, doing "javac -h" etc.)
find_package(Java 1.8 REQUIRED) # BSF4ooRexx850 has Java "8" a.k.a. "1.8" as minimum

message("JAVA_HOME               = ${JAVA_HOME}")
message("Java_JAVA_EXECUTABLE    = ${Java_JAVA_EXECUTABLE}")
message("Java_JAVAC_EXECUTABLE   = ${Java_JAVAC_EXECUTABLE}")
message("Java_JAVAH_EXECUTABLE   = ${Java_JAVAH_EXECUTABLE}")
message("Java_JAVADOC_EXECUTABLE = ${Java_JAVADOC_EXECUTABLE}")
message("Java_VERSION_STRING     = ${Java_VERSION_STRING}")
message("Java_VERSION            = ${Java_VERSION}")

# --- Java native interface (JNI) section ---
find_package(JNI REQUIRED)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS = ${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES    = ${JNI_LIBRARIES}")
    message (STATUS "JNI_VERSION      = ${JNI_VERSION}")
    message (STATUS "JNI_VERSION_PATCH= ${JNI_VERSION_PATCH}")
endif()
include_directories(BSF4ooRexx850 PRIVATE ${JNI_INCLUDE_DIRS} )


## --- compile options for BSF4ooRexx.cc ---
# define WINDOWS flag if on Windows
if (WIN32)
    set (DEF_WINDOWS_FLAG "-DWINDOWS")
    set (WIN_REXX_HOME "$ENV{REXX_HOME}")       # get value from environment
    include_directories(${WIN_REXX_HOME}/api)
    link_directories(${WIN_REXX_HOME}/api)
    message(STATUS "... DEF_WINDOWS_FLAG=${DEF_WINDOWS_FLAG}")
endif ()

# define BSF4Rexx bitness flag
if (CMAKE_SIZEOF_VOID_P MATCHES 8)
    set(BSF4REXX_BITNESS "64")
else ()
    set(BSF4REXX_BITNESS "32")
endif ()
set(BSF4REXX_BITNESS_FLAG "BSF4REXX_${BSF4REXX_BITNESS}_BIT")
message(STATUS "... BSF4REXX_BITNESS_FLAG =${BSF4REXX_BITNESS_FLAG}")

add_compile_options(/std:c++latest ${DEF_WINDOWS_FLAG} -D${BSF4REXX_BITNESS_FLAG} -D_CRT_SECURE_NO_WARNINGS) # MSC turn off sscanf related warnings

## needs to be placed after the compile options
add_library(BSF4ooRexx850 SHARED BSF4ooRexx.cc)

# done.
