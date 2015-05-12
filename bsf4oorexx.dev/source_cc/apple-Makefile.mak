# usage: make -f apple-Makefile.mak [all \ i386 | x86_64 | ppc | universal \ clean ]
# rgf, 2011-01-02, 2011-01-12, 2011-02-20

# rgf, 2013-08-12: to support an earlier version of MacOSX use e.g. "export MACOSX_DEPLOYMENT_TARGET=10.6" before compiling (cf. "man g++")

# INC_PATH = -I. -I/System/Library/Frameworks/JavaVM.framework/headers


# allow earlier versions of MacOSX to use the lib
# export MACOSX_DEPLOYMENT_TARGET=10.6
# echo $MACOSX_DEPLOYMENT_TARGET



# rgf, 2014-03-30, 2014-03-31
# INC_PATH = -I. -I/Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers
INC_PATH = -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -I .
INC_PATH_ALL = $(INC_PATH) -I/System/Library/Frameworks/ooRexx.framework/Versions/Current/Headers

LIB_2_REXX = /System/Library/Frameworks/ooRexx.framework/Libraries
LIB_2_REXX_32 = /opt/oldrexx/ooRexx-i386/lib

# INC_PATH_ORX = -I/opt/ooRexx/include $(INC_PATH)

CFLAGS = -fPIC
LFLAGS = -shared

# --------------------------------------------------------
#all: i386 x86_64 ppc universal
all: i386 x86_64 universal

# -------------------------------------------------------- needs 32-bit ooRexx installed currently
i386: BSF4ooRexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH_ORX) -m32 -arch i386 -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-i386.o BSF4ooRexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-i386/include -m32 -arch i386 -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-i386.o BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH_ALL) -m32 -arch i386 -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-i386.o BSF4ooRexx.cc


	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o $(LIB_2_REXX_32)/librexx.dylib $(LIB_2_REXX_32)/librexxapi.dylib -framework JavaVM -arch i386

#	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o $(LIB_2_REXX)/librexx.dylib $(LIB_2_REXX)/librexxapi.dylib -framework JavaVM -arch i386
#	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o /usr/lib/librexx.dylib /usr/lib/librexxapi.dylib -framework JavaVM -arch i386
#	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o -L/usr/lib -llibrexx.dylib -llibrexxapi.dylib -framework JavaVM -arch i386

	cp -p libBSF4ooRexx-i386.dylib 32/libBSF4ooRexx.dylib

# --------------------------------------------------- needs 64-bit ooRexx installed currently
x86_64: BSF4ooRexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -m64 -arch x86_64 -DUSE_OREXX -DUNIX -DBSF4REXX_64_BIT -oBSF4ooRexx-mac-x86_64.o BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) $(INC_PATH_ALL) -m64 -arch x86_64 -DUSE_OREXX -DUNIX -DBSF4REXX_64_BIT -oBSF4ooRexx-mac-x86_64.o BSF4ooRexx.cc

#	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-x86_64.dylib BSF4ooRexx-mac-x86_64.o /opt/ooRexx-x86_64/lib/librexx.dylib /opt/ooRexx-x86_64/lib/librexxapi.dylib -framework JavaVM -arch x86_64
	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-x86_64.dylib BSF4ooRexx-mac-x86_64.o $(LIB_2_REXX)/librexx.dylib $(LIB_2_REXX)/librexxapi.dylib -framework JavaVM -arch x86_64

	cp -p libBSF4ooRexx-x86_64.dylib 64/libBSF4ooRexx.dylib

# ---------------------------------------------------
ppc: BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -I/opt/ooRexx-ppc/include -m32 -arch ppc -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-ppc.o BSF4ooRexx.cc

	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-ppc.dylib BSF4ooRexx-mac-ppc.o /opt/ooRexx-ppc/lib/ooRexx/librexx.dylib /opt/ooRexx-ppc/lib/ooRexx/librexxapi.dylib -framework JavaVM -arch ppc

# ---------------------------------------------------
universal: BSF4ooRexx.cc i386 x86_64
	lipo -create -output libBSF4ooRexx.dylib -arch x86_64 libBSF4ooRexx-x86_64.dylib -arch i386 libBSF4ooRexx-i386.dylib 

#	lipo -create -output libBSF4ooRexx.dylib -arch x86_64 libBSF4ooRexx-x86_64.dylib -arch i386 libBSF4ooRexx-i386.dylib -arch ppc libBSF4ooRexx-ppc.dylib

# ---------------------------------------------------


.PHONY: clean
clean:
	rm -f *.dylib
	rm -f *.jnilib
	rm -f *.o
