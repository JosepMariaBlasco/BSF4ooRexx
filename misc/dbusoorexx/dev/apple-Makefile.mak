# usage: make -f apple-Makefile.mak [all \ i386 | x86_64 | ppc | universal \ clean ]
# rgf, 2011-08-15
# rgf, 2014-08-05: adjust paths


# Define a list of pkg-config packages we want to use
pkg_packages = dbus-1

PKG_CFLAGS  = $(shell pkg-config --cflags $(pkg_packages))
PKG_LDFLAGS = $(shell pkg-config --libs $(pkg_packages))

INC_PATH = -I. -I/opt/local/include/dbus-1.0
INC_PATH_ORX = -I/opt/ooRexx/include $(INC_PATH)

# port libraries likde dbus-1
LIB_PATH = -L/opt/local/lib


# ADD_CFLAGS := -g -Wall -DUNIX  -D__cplusplus
# Clang does not like -D__cplusplus
ADD_CFLAGS := -g -Wall -DUNIX

# LDFLAGS = -shared -rdynamic -lpthread -lc  -lm
ADD_LFLAGS = -shared -rdynamic 

CFLAGS = $(PKG_CFLAGS) $(ADD_CFLAGS) -fPIC
LFLAGS = $(PKG_LDFLAGS) $(ADD_LFLAGS)

OOREXX_HOME_32=/Users/rony/dev/oorexx_allura/main/releases/4.2.0/trunk/tmp_i386/usr
OOREXX_HOME_64=/Users/rony/dev/oorexx_allura/main/releases/4.2.0/trunk/tmp_x86_64/usr


# --------------------------------------------------------
all: i386 x86_64 ppc universal

# --------------------------------------------------------
i386: dbusoorexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-i386/include -m32 -arch i386 -DDBUSOOREXX_32 -DUSE_OREXX -DUNIX -odbusoorexx-mac-i386.o dbusoorexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I$(OOREXX_HOME_32)/include -m32 -arch i386 -DDBUSOOREXX_32 -DUSE_OREXX -DUNIX -odbusoorexx-mac-i386.o dbusoorexx.cc


#	g++ -dynamiclib $(LFLAGS) -o libdbusoorexx-i386.dylib dbusoorexx-mac-i386.o /usr/lib/librexx.dylib /usr/lib/librexxapi.dylib $(LIB_PATH) -framework JavaVM -arch i386
	g++ -dynamiclib $(LFLAGS) -o libdbusoorexx-i386.dylib dbusoorexx-mac-i386.o $(OOREXX_HOME_32)/lib/librexx.dylib $(OOREXX_HOME_32)/lib/librexxapi.dylib $(LIB_PATH) -arch i386

#	cp -p libdbusoorexx-i386.dylib 32/libdbusoorexx.dylib

# ---------------------------------------------------
x86_64: dbusoorexx.cc
#	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -m64 -arch x86_64 -DDBUSOOREXX_64 -DUSE_OREXX -DUNIX -odbusoorexx-mac-x86_64.o dbusoorexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I$(OOREXX_HOME_64)/include -m64 -arch x86_64 -DDBUSOOREXX_64 -DUSE_OREXX -DUNIX -odbusoorexx-mac-x86_64.o dbusoorexx.cc


#	g++ -dynamiclib $(LFLAGS) -o libdbusoorexx-x86_64.dylib dbusoorexx-mac-x86_64.o /opt/ooRexx-x86_64/lib/ooRexx/librexx.dylib /opt/ooRexx-x86_64/lib/ooRexx/librexxapi.dylib $(LIB_PATH) -framework JavaVM -arch x86_64
	g++ -dynamiclib $(LFLAGS) -o libdbusoorexx-x86_64.dylib dbusoorexx-mac-x86_64.o $(OOREXX_HOME_64)/lib/librexx.dylib $(OOREXX_HOME_64)/lib/librexxapi.dylib $(LIB_PATH) -arch x86_64

#	cp -p libdbusoorexx-x86_64.dylib 64/libdbusoorexx.dylib

# ---------------------------------------------------
ppc: dbusoorexx.cc
	g++ -c $(CFLAGS) $(INC_PATH) -I/opt/ooRexx-x86_64/include -I/opt/ooRexx-ppc/include -m32 -arch ppc -DDBUSOOREXX_32 -DUSE_OREXX -DUNIX -odbusoorexx-mac-ppc.o dbusoorexx.cc

	g++ -dynamiclib $(LFLAGS) -o libdbusoorexx-ppc.dylib dbusoorexx-mac-ppc.o /opt/ooRexx-ppc/lib/ooRexx/librexx.dylib /opt/ooRexx-ppc/lib/ooRexx/librexxapi.dylib $(LIB_PATH) -framework JavaVM -arch ppc

# ---------------------------------------------------
universal: dbusoorexx.cc i386 x86_64
	lipo -create -output libdbusoorexx.dylib -arch x86_64 libdbusoorexx-x86_64.dylib -arch i386 libdbusoorexx-i386.dylib 

#	lipo -create -output libdbusoorexx.dylib -arch x86_64 libdbusoorexx-x86_64.dylib -arch i386 libdbusoorexx-i386.dylib -arch ppc libdbusoorexx-ppc.dylib


# ---------------------------------------------------


.PHONY: clean
clean:
	rm -f *.dylib
	rm -f *.o
