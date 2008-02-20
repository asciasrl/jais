#include <jni.h>
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>

#include "JBisKyoUnit.h"

typedef int(*funzione)();
/**
 * Funzione Bentel. Documentami.
 */
funzione PanelConnection;

// JBisKyUnit DLL helper - CopyRight (C) 2007,2008 Ascia S.r.l.

/**
 * Istanza della libreria DLL Bentel.
 */
HINSTANCE libreria;

JNIEXPORT jboolean JNICALL Java_it_ascia_bentel_JBisKyoUnit_openLibrary
  (JNIEnv *env, jobject object)
{
	libreria = LoadLibrary("BisKyoUnit.dll");
	if(libreria == NULL) {
		fprintf(stderr, "Caricamento BisKyoUnit.dll non riuscito\n");
		return JNI_FALSE;
	}
	PanelConnection = (funzione)GetProcAddress(libreria, "PanelConnection");
	if (PanelConnection == NULL) {
		fprintf(stderr, "Risoluzione funzione PanelConnection non riuscito\n");
		FreeLibrary(libreria);
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

JNIEXPORT jbyte JNICALL Java_it_ascia_bentel_JBisKyoUnit_PanelConnection
  (JNIEnv *env, jclass obj, jint comando, jbyte seriale, jbyte tentativi, 
		  jstring jPIN, jbyteArray jdata)
{		
	int risultato;
	jsize dataSize, pinLength;
	char data[1024];
	jboolean iscopy = JNI_FALSE;
	const char *PIN = (*env)->GetStringUTFChars(env, jPIN, &iscopy);
	pinLength = (*env)->GetStringLength(env, jPIN);
	if (PIN == NULL) {
		fprintf(stderr, "Memoria esaurita!\n");
		return -1;
	}
	// Preparazione dei dati da inviare
	dataSize = (*env)->GetArrayLength(env, jdata);
	(*env)->GetByteArrayRegion(env, jdata, 0, dataSize, data);
	
	printf("\r\nComando=%d PIN=%s (%d)\r\n", (int)comando, PIN, pinLength);
	
	risultato = PanelConnection(comando, seriale, tentativi, PIN, (char)pinLength, 
			data);
	
	//API Ref :SetByteArrayRegion(JNIEnv *env, jbyteArray array, 
	//			jsize startelement, jsize length, jbyte *buffer)	
	(*env)->SetByteArrayRegion(env, jdata, 0, dataSize, data);
    (*env)->ReleaseStringUTFChars(env, jPIN, PIN);

	/* int i;
	printf("C side result:");
	for (i=0; i < 5; i++) {
	  // printf(" 0x%02x",data[i]);
		printf(" %ud",data[i]);
	}
	printf("\r\n"); */
   	return risultato;
}

JNIEXPORT void JNICALL Java_it_ascia_bentel_JBisKyoUnit_close
  (JNIEnv *env, jobject object) {
	FreeLibrary(libreria);
}
