JDK = /cygdrive/c/Programmi/Java/jdk1.6.0

INCLUDES = -I$(JDK)/include -I$(JDK)/include/win32

GCCFLAGS = -mno-cygwin -Wl,--add-stdcall-alias -shared

SRCS = JBisKyoUnit.c

LIBS = -LBisKyoUnit.dll

TARGET = JBisKyoUnit.dll

GCC = gcc  

$(TARGET):	$(SRCS)
	$(GCC) $(GCCFLAGS) $(INCLUDES) -o $(TARGET) $(LIBS) $(SRCS)

all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)
