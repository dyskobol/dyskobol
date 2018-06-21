//
// Created by przemek on 28.03.18.
//

#include <libtsk.h>
#include <assert.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif


/*
 * Class:     org_sleuthkit_datamodel_SleuthkitJNI
 * Method:    openImgNat
 * Signature: ([Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_org_sleuthkit_datamodel_SleuthkitJNI_openImgNat
        (JNIEnv *, jclass, jobjectArray, jint);

}


void open();

