#include <jni.h>
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

#include "JBisKyoUnit.h"

typedef int(*funzione)();

// JBisKyUnit DLL helper - CopyRight (C) 2007,2008 Ascia S.r.l.

JNIEXPORT jbyte JNICALL Java_it_ascia_bentel_JBisKyoUnit_PanelConnection
  (JNIEnv *env, jclass obj, jint comando, jbyte seriale, jbyte tentativi, jstring jPIN, jbyteArray jdata)
{		
	funzione PanelConnection;
	int risultato;
	
	HINSTANCE libreria = LoadLibrary("BisKyoUnit.dll");
	if(libreria == NULL) {
		printf("Caricamento BisKyoUnit.dll non riuscito\n");
		return -1;
	}
	
	PanelConnection = (funzione)GetProcAddress(libreria, "PanelConnection");
	if (PanelConnection == NULL) {
		printf("Risoluzione funzione PanelConnection non riuscito\n");
		FreeLibrary(libreria);
		return -1;
	}
	
	jboolean iscopy = JNI_FALSE;
	const char *PIN = (*env)->GetStringUTFChars(env, jPIN, &iscopy);
	//printf("Comando=%d PIN=%s\r\n", comando, PIN);
	
	// //API Ref :SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize startelement, jsize length, jbyte *buffer)
	const char *data[512];
	(*env)->SetByteArrayRegion(env,jdata,0,512,*data);
	
	risultato = PanelConnection(comando,seriale,tentativi,PIN,4,data);	

    (*env)->ReleaseStringUTFChars(env, jPIN, PIN);

	int i;
	printf("C side result:");
	for (i=0; i < 5; i++) {
	  printf(" 0x%0x",data[i]);
	}
	printf("\r\n");
    
	FreeLibrary(libreria);
	
	return risultato;
}
