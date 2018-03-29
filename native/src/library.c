#include <stdio.h>
#include "simple_Library__.h"

/*
 * Class:     simple_Library__
 * Method:    say
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_simple_Library_00024_say
(JNIEnv *env, jobject clazz, jstring message) {
	const char* msg = (*env)->GetStringUTFChars(env, message, 0);
	fprintf(stdout, "Printing from native lisadasdbrary: %s\n", msg);
	fflush(stdout);
	(*env)->ReleaseStringUTFChars(env, message, msg);
	return 42;
}

JNIEXPORT jlong JNICALL
Java_simple_Library_00024_openImgNat(JNIEnv * env, jclass obj, jstring path, jint num_imgs) {
    TSK_IMG_INFO *img_info;
    jboolean isCopy;

    const char *image_path = path;
    const char* const images[] = {image_path};
    fprintf(stdout, "Printing from native library: %s\n", path);
    img_info = tsk_img_open_utf8(1, images, TSK_IMG_TYPE_DETECT, 0);

    return (jlong) img_info;


}
