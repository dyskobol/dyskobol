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

/*
 * Class:     simple_Library__
 * Method:    openImg
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_simple_Library_00024_openImgNat(JNIEnv * env, jobject obj, jstring path) {
    const char *image_path = (*env)->GetStringUTFChars(env, path, 0);;
    const char* const images[] = {image_path};
    const int images_count = 1;
    const int sector_size = 0; // Auto detect
    const TSK_IMG_INFO* img_info = tsk_img_open_utf8(images_count, images, TSK_IMG_TYPE_DETECT, sector_size);

    return (jlong) img_info;
}

/*
 * Class:     simple_Library__
 * Method:    openFsNat
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_simple_Library_00024_openFsNat(JNIEnv * env, jclass obj, jlong image) {

    TSK_OFF_T offset = 0; // I guess
    TSK_FS_INFO* filesystem_info = tsk_fs_open_img( (TSK_IMG_INFO*) image, 0, TSK_FS_TYPE_DETECT);
    assert(filesystem_info != NULL);

    return (jlong) filesystem_info;
}

typedef struct FilesList {
    int size;
    int used;
    jobject* files;
    jclass* cls;
    jmethodID* constructor;
    JNIEnv* env;
} FilesList;


TSK_WALK_RET_ENUM create_list(TSK_FS_FILE *file, const char *a_path, void *a_ptr) {
    FilesList* list = (FilesList*) a_ptr;
    JNIEnv* env = list->env;
    jstring name = (*env)->NewStringUTF(env, file->name->name);
    jstring path = (*env)->NewStringUTF(env, a_path);
    jobject obj = (*env)->NewObject(env,
                                    *(list->cls),
                                    *(list->constructor),
                                    name,
                                    path,
                                    (int) file->name->type,
                                    (long) file->meta->addr,
                                    (int) file->meta->mode,
                                    (long) file->meta->size,
                                    (int) file->meta->uid,
                                    (int) file->meta->gid,
                                    (long) file->meta->mtime,
                                    (long) file->meta->mtime_nano,
                                    (long) file->meta->atime,
                                    (long) file->meta->atime_nano,
                                    (long) file->meta->ctime,
                                    (long) file->meta->ctime_nano,
                                    (long) file->meta->crtime,
                                    (long) file->meta->crtime_nano
                                    );

    if( list->used >= list->size ) {
        jobject* files = malloc(sizeof(jobject*) * list->size * 2); // Double the length
        memcpy(files, list->files, sizeof(list->files));
        free(list->files);
        list->files = files;
        list->size *= 2;

    }

    list->files[list->used] = obj;
    list->used++;
}
/*
 * Class:     simple_Library__
 * Method:    getDirFilesNat
 * Signature: (JLjava/lang/String;)[Lpl/dyskobol/model/File;
 */
JNIEXPORT jobjectArray JNICALL
Java_simple_Library_00024_getDirFilesNat(JNIEnv * env, jclass obj, jlong fileSystem, jstring path) {
	const char* path_ = (*env)->GetStringUTFChars(env, path, 0);


    TSK_FS_INFO* filesystem = (TSK_FS_INFO*) fileSystem;
    TSK_FS_FILE* file = tsk_fs_file_open(filesystem, NULL, path_);
    TSK_INUM_T inode = file->meta->addr;
    FilesList list;
    list.size = 20;
    list.used = 0;
    jclass cls = (*env)->FindClass(env, "pl/dyskobol/model/File");
    list.cls = &cls;
    // String, String, int, long, int, long, int, int, long x 8
    const char* methodParams = "(Ljava/lang/String;Ljava/lang/String;IJIJIIJJJJJJJJ)V";
    jmethodID constructor = (*env)->GetMethodID(env, *list.cls, "<init>", methodParams);
    list.constructor = &constructor;
    list.env = env;
    list.files = malloc(sizeof(jobject)*list.size);

    tsk_fs_dir_walk(filesystem, inode, TSK_FS_DIR_WALK_FLAG_ALLOC, create_list, &list);

    tsk_fs_file_close(file);

    // Create array
    jobjectArray ret = (jobjectArray) (*env)->NewObjectArray(
        env,
        list.used,
        *(list.cls),
        NULL
    );
    // Fill it
    for(int i=0; i<list.used; i++) {
        (*env)->SetObjectArrayElement(
            env,
            ret,
            i,
            list.files[i]
        );
    }

    free(list.files);

    return ret;
}

