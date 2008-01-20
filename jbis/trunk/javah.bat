@echo generazione file .h

p:
cd \Dev\jbis	
c:\Programmi\java\jdk1.6.0\bin\javah.exe -classpath bin -o JBisKyoUnit.h it.ascia.bentel.JBisKyoUnit

@pause