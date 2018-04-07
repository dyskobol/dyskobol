/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <libtsk.h>
#include <assert.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>


/* Header for class simple_Library__ */

#ifndef _Included_simple_Library__
#define _Included_simple_Library__
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     simple_Library__
 * Method:    say
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_simple_Library_00024_say
  (JNIEnv *, jobject, jstring);

/*
 * Class:     simple_Library__
 * Method:    openImgNat
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jlong JNICALL Java_simple_Library_00024_openImgNat
  (JNIEnv *, jobject, jstring);

/*
 * Class:     simple_Library__
 * Method:    openFsNat
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_simple_Library_00024_openFsNat(JNIEnv *, jobject, jlong);

/*
 * Class:     simple_Library__
 * Method:    getDirFilesNat
 * Signature: (JLjava/lang/String;)[Lpl/dyskobol/model/File;
 */
JNIEXPORT jobjectArray JNICALL
Java_simple_Library_00024_getDirFilesNat(JNIEnv*, jobject, jlong, jstring);

/*
 * Class:     simple_Library__
 * Method:    openFileNat
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_simple_Library_00024_openFileNat(JNIEnv*, jclass, jlong, jlong);

/*
 * Class:     simple_Library__
 * Method:    closeFileNat
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_simple_Library_00024_closeFileNat(JNIEnv*, jclass, jlong);
/*
 * Class:     simple_Library__
 * Method:    readNat
 * Signature: (JJJ)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_simple_Library_00024_readNat(JNIEnv*, jclass, jlong, jlong, jlong);

/*
 * Class:     simple_Library__
 * Method:    readToBufferNat
 * Signature: (JJJ[BJ)J
 */
JNIEXPORT jlong JNICALL
Java_simple_Library_00024_readToBufferNat(JNIEnv*, jclass, jlong, jlong, jlong, jbyteArray, jlong);

#ifdef __cplusplus
}
#endif
#endif