JDK = /cygdrive/c/Programmi/Java/jdk1.6.0_04

INCLUDES = -I$(JDK)/include -I$(JDK)/include/win32

GCCFLAGS = -mno-cygwin -Wl,--add-stdcall-alias -shared -Wall

SRCS = JBisKyoUnit.c JBisKyoUnit.h

LIBS = -LBisKyoUnit.dll

TARGET = JBisKyoUnit.dll

GCC = gcc  

JAVAH = ${JDK}/bin/javah

all:	$(TARGET)

JBisKyoUnit.h: src/it/ascia/bentel/JBisKyoUnit.java
	${JAVAH} -classpath bin -o JBisKyoUnit.h it.ascia.bentel.JBisKyoUnit

$(TARGET):	$(SRCS)
	$(GCC) $(GCCFLAGS) $(INCLUDES) -o $(TARGET) $(LIBS) $(SRCS)

clean:
	rm -f $(OBJS) $(TARGET)
