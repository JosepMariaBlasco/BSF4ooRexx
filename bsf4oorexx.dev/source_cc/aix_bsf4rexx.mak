# make -f aix_bsf4rexx.mak rebuild
#
# ------------------------ Apache Version 2.0 license -------------------------
#    Copyright (C) 2001-2013 Rony G. Flatscher
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
# -----------------------------------------------------------------------------

#-------------------------------------------------------------------------
# aix_bsf4rexx.mak: makefile to build libBSF4ooRexx.so
#
# ooRexx 4.x, 2005-06-07, 2009-09-08
# 2013-07-30: adapted for AIX by Martin Pichler, Austria
#
# make -f aix_bsf4rexx [orexx | clean | clean_dll | rebuild ]
#-------------------------------------------------------------------------

# -----------------------  Path Definitions  ----------------------------
BLD_PATH = .

# JDK_BASE = /usr/java/j2sdk1.4.2_08
# JDK_BASE = /home/rony/dev/jdk1.6.0_06
# JDK_BASE = /usr/lib/jvm/java-6-sun
# JDK_BASE = /usr/lib/jvm/java-6-openjdk
# JDK_BASE = /usr/lib/jvm/java-6-sun-1.6.0.24
# JDK_BASE = /usr/lib/jvm/java-6-sun
#JDK_BASE = /usr/lib/jvm/java-6-openjdk-amd64
JDK_BASE = /usr/java6_64

INC_PATH = -I. -I/usr/local/include -I$(JDK_BASE)/include -I$(JDK_BASE)/include/aix

# INC_PATH_ORX = -I/opt/orexx $(INC_PATH)

INC_PATH_ORX = -I/opt/ooRexx/include

LIB_PATH = -L. -L/usr/local/lib -L/usr/lib -L/opt/ooRexx/lib/ooRexx


# --------------------------------------------------------
del_cmd  = rm -f
# copy_cmd = cp -p
copy_cmd = cp

ORX     = OREXX
BSF_SRC	= BSF4ooRexx.cc


#
# Compiler macros
#
# cc = g++ -fPIC
cc = gcc -c -fPIC
#link = gcc
link = gcc
#export OBJECT_MODE=64

#
# CFLAGS_ori     = -c -ggdb -D__cplusplus
#CFLAGS		= -c -O3 -D__cplusplus -DUNIX -m64 -DBSF4REXX_64_BIT
#CFLAGS= -D_AIX61 -D_ALL_SOURCE -DFUNCPROTO=15 -O -DBSF4REXX_64_BIT
CFLAGS= -maix64 -DAIX61 -DUNIX -D_ALL_SOURCE -DFUNCPROTO=15 -O -DBSF4REXX_64_BIT -DOBJECT_MODE=64

CFLAGS_ORX      = -DUSE_$(ORX)


# link_lib_flags_lib_bsf4rexx_ori = -shared -rdynamic -lpthreads -lrexxapi -lc  -lm
# 2003-04-05, ---rgf, added "-ljvm"
# LINK_FLAGS      = -shared -rdynamic -lpthread -ljvm -lc  -lm
# LINK_FLAGS      = -shared -rdynamic -lpthread /usr/java/j2sdk1.4.1_02/jre/lib/i386/client/libjvm.so -lc  -lm
# LINK_FLAGS      = -shared -rdynamic -lpthread $(JDK_BASE)/jre/lib/i386/client/libjvm.so -lc  -lm
#LINK_FLAGS      = -shared -rdynamic -lpthread -lc  -lm
#LINK_FLAGS      = --export-dynamic -shared -rdynamic -lpthread -lc  -lm
#LINK_FLAGS      =  -shared -lpthread -lc -lm
LINK_FLAGS = -shared -lpthread -lc -lm

LINK_ORX_FLAGS  = -lrexx -lrexxapi -lrexxutil

BSF_OBJ_ORX     = $(BLD_PATH)/BSF4ooRexx-64.o
BSF_ORX_TARGET  = $(BLD_PATH)/libBSF4ooRexx-64.so
BSF4REXX	= $(BLD_PATH)/libBSF4ooRexx.so

# *************************************************************************** #
# Rules									      #
# *************************************************************************** #
# .cc.o:
#	  $(cc) $(CFLAGS) -o$*.o $(INC_PATH) $(LIB_PATH) $*.cc

#---------------------------------------------------------- Generate library

# Object Rexx ----------------------------------------------
$(BSF_ORX_TARGET) : $(BSF_SRC)
	echo
	echo Building $@ ...
	echo
	$(cc) $(CFLAGS) $(CFLAGS_ORX) -o$(BSF_OBJ_ORX) $(INC_PATH) $(INC_PATH_ORX) $(LIB_PATH) $(BSF_SRC)
	$(link) -maix64 -DAIX61 -DUNIX -Wl,-b64,-brtl -g -o $(BLD_PATH)/$@ $(BSF_OBJ_ORX) $(LIB_PATH) $(LINK_FLAGS) $(LINK_ORX_FLAGS) -lstdc++
	$(copy_cmd) $(BSF_ORX_TARGET) $(BSF4REXX)
#	$(copy_cmd) $(BSF4REXX) 64
	$(copy_cmd) $(BSF4REXX) 64/AIX$(BSF4REXX)
#	$(copy_cmd) $(BSF4REXX) ../bsf4rexx


#---------------------------------------------------------- END section
orexx :		$(BSF_ORX_TARGET)

#---------------------------------------------------------- END section
# -------------------------------------------------------------------
# builds all
rebuild: clean clean_dll orexx

#---------------------------------------------------------- END section
# just clean up the mess, just leave the dll
clean:
	- $(del_cmd) $(BLD_PATH)/*.o

#---------------------------------------------------------- END section
clean_dll:
	- $(del_cmd) $(BLD_PATH)/*.so


# -------------------------------------------------------------------
# builds all
all: clean orexx

# -------------------------------------------------------------------
i:
	$(cc) $(CFLAGS) $(CFLAGS_ORX) -oBSF4ooRexx.i $(INC_PATH) $(INC_PATH_ORX) -E $(LIB_PATH) $(BSF_SRC)

