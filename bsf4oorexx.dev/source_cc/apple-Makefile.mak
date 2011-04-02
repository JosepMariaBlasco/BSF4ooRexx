# usage: make -f apple-Makefile.mak [all \ i386 | x86_64 | ppc | universal \ clean ]
# rgf, 2011-01-02, 2011-01-12, 2011-02-20

INC_PATH = -I. -I/System/Library/Frameworks/JavaVM.framework/headers

INC_PATH_ORX = -I/opt/ooRexx/include $(INC_PATH)

CFLAGS = -fPIC
LFLAGS = -shared

# --------------------------------------------------------
all: i386 x86_64 ppc universal

# --------------------------------------------------------
i386: BSF4ooRexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH_ORX) -m32 -arch i386 -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-i386.o BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-i386/include -m32 -arch i386 -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-i386.o BSF4ooRexx.cc

	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o /usr/lib/librexx.dylib /usr/lib/librexxapi.dylib -framework JavaVM -arch i386
#	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-i386.dylib BSF4ooRexx-mac-i386.o -L/usr/lib -llibrexx.dylib -llibrexxapi.dylib -framework JavaVM -arch i386

	cp -p libBSF4ooRexx-i386.dylib 32/libBSF4ooRexx.dylib

# ---------------------------------------------------
x86_64: BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -m64 -arch x86_64 -DUSE_OREXX -DUNIX -DBSF4REXX_64_BIT -oBSF4ooRexx-mac-x86_64.o BSF4ooRexx.cc

	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-x86_64.dylib BSF4ooRexx-mac-x86_64.o /opt/ooRexx-x86_64/lib/ooRexx/librexx.dylib /opt/ooRexx-x86_64/lib/ooRexx/librexxapi.dylib -framework JavaVM -arch x86_64
	cp -p libBSF4ooRexx-x86_64.dylib 64/libBSF4ooRexx.dylib

# ---------------------------------------------------
ppc: BSF4ooRexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -I/opt/ooRexx-ppc/include -m32 -arch ppc -DUSE_OREXX -DUNIX -DBSF4REXX_32_BIT -oBSF4ooRexx-mac-ppc.o BSF4ooRexx.cc

	g++ -dynamiclib $(LFLAGS) -o libBSF4ooRexx-ppc.dylib BSF4ooRexx-mac-ppc.o /opt/ooRexx-ppc/lib/ooRexx/librexx.dylib /opt/ooRexx-ppc/lib/ooRexx/librexxapi.dylib -framework JavaVM -arch ppc

# ---------------------------------------------------
universal: BSF4ooRexx.cc i386 x86_64
	lipo -create -output libBSF4ooRexx.dylib -arch x86_64 libBSF4ooRexx-x86_64.dylib -arch i386 libBSF4ooRexx-i386.dylib -arch ppc libBSF4ooRexx-ppc.dylib

# ---------------------------------------------------


.PHONY: clean
clean:
	rm -f *.dylib
	rm -f *.jnilib
	rm -f *.o
