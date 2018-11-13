#include <stdio.h>
#include "bindings_Sleuthkit__.h"

/*
    Helpers
*/
jint throwOutOfMemoryError( JNIEnv* env, char* message )
{
    char *className = "java/lang/OutOfMemoryError" ;
    jclass exClass = (*env)->FindClass( env, className );
    return (*env)->ThrowNew( env, exClass, message );
}

jint throwFileNotFound( JNIEnv* env, char* message ) {
    char *className = "java/io/FileNotFoundException" ;
    jclass exClass = (*env)->FindClass( env, className );
    return (*env)->ThrowNew( env, exClass, message );
}

jint throwIOException( JNIEnv* env, char* message ) {
    char *className = "java/io/FileNotFoundException" ;
    jclass exClass = (*env)->FindClass( env, className );
    return (*env)->ThrowNew( env, exClass, message );
}

/*
 * Class:     bindings_Sleuthkit__
 * Method:    openImg
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_openImgNat(JNIEnv * env, jobject obj, jstring path) {
    const char *image_path = (*env)->GetStringUTFChars(env, path, 0);;
    const char* const images[] = {image_path};
    const int images_count = 1;
    const int sector_size = 0; // Auto detect
    const TSK_IMG_INFO* img_info = tsk_img_open_utf8(images_count, images, TSK_IMG_TYPE_DETECT, sector_size);

    if( img_info == NULL ) {
        throwFileNotFound(env, "Specified image not found.");
    }

    return (jlong) img_info;
}
/*
 * Class:     bindings_Sleuthkit__
 * Method:    getImgSize
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_getImgSize(JNIEnv * env, jclass obj, jlong image) {

return (jlong)  (((TSK_IMG_INFO*) image ) -> size);
}


/*
 * Class:     bindings_Sleuthkit__
 * Method:    openFsNat
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_openFsNat(JNIEnv * env, jclass obj, jlong image) {

    TSK_OFF_T offset = 0; // I guess
    TSK_FS_INFO* filesystem_info = tsk_fs_open_img( (TSK_IMG_INFO*) image, 0, TSK_FS_TYPE_DETECT);
    if( filesystem_info == NULL ) {
        throwIOException(env, "Unable to open the file system.");
    }

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
                                    (long) file->meta->crtime_nano,
                                    (long) file->fs_info
                                    );

    if( list->used >= list->size ) {
        const int bytes = sizeof(jobject) * list->size;
        jobject* files = malloc(bytes*2); // Double the length
        memcpy(files, list->files, bytes);
        free(list->files);
        list->files = files;
        list->size *= 2;
    }

    list->files[list->used] = obj;
    list->used++;

    return TSK_WALK_CONT;
}
/*
 * Class:     bindings_Sleuthkit__
 * Method:    getDirFilesNat
 * Signature: (JLjava/lang/String;)[Lpl/dyskobol/model/File;
 */
JNIEXPORT jobjectArray JNICALL
Java_bindings_Sleuthkit_00024_getDirFilesNat(JNIEnv * env, jclass obj, jlong fileSystem, jstring path) {
	const char* path_ = (*env)->GetStringUTFChars(env, path, 0);


    TSK_FS_INFO* filesystem = (TSK_FS_INFO*) fileSystem;
    TSK_FS_FILE* file = tsk_fs_file_open(filesystem, NULL, path_);
    TSK_INUM_T inode = file->meta->addr;
    FilesList list;
    list.size = 20;
    list.used = 0;
    jclass cls = (*env)->FindClass(env, "pl/dyskobol/model/File");
    list.cls = &cls;
    // String, String, int, long, int, long, int, int, long x 9
    const char* methodParams = "(Ljava/lang/String;Ljava/lang/String;IJIJIIJJJJJJJJJ)V";
    jmethodID constructor = (*env)->GetMethodID(env, *list.cls, "<init>", methodParams);
    list.constructor = &constructor;
    list.env = env;
    list.files = malloc(sizeof(jobject)*list.size);

    if( tsk_fs_dir_walk(filesystem, inode, TSK_FS_DIR_WALK_FLAG_ALLOC | TSK_FS_DIR_WALK_FLAG_NOORPHAN, create_list, &list) ) {
        throwIOException(env, "Unable to walk directory");
    }

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

/*
 * Class:     bindings_Sleuthkit__
 * Method:    getDirFilesByInodeNat
 * Signature: (JJ)[Lpl/dyskobol/model/File;
 */
JNIEXPORT jobjectArray JNICALL
Java_bindings_Sleuthkit_00024_getDirFilesByInodeNat(JNIEnv * env, jclass obj, jlong fileSystem, jlong addr) {
    TSK_FS_INFO* filesystem = (TSK_FS_INFO*) fileSystem;
    TSK_INUM_T inode = (TSK_INUM_T) addr;
    FilesList list;
    list.size = 20;
    list.used = 0;
    jclass cls = (*env)->FindClass(env, "pl/dyskobol/model/File");
    list.cls = &cls;
    // String, String, int, long, int, long, int, int, long x 9
    const char* methodParams = "(Ljava/lang/String;Ljava/lang/String;IJIJIIJJJJJJJJJ)V";
    jmethodID constructor = (*env)->GetMethodID(env, *list.cls, "<init>", methodParams);
    list.constructor = &constructor;
    list.env = env;
    list.files = malloc(sizeof(jobject)*list.size);

    tsk_fs_dir_walk(filesystem, inode, TSK_FS_DIR_WALK_FLAG_ALLOC | TSK_FS_DIR_WALK_FLAG_NOORPHAN, create_list, &list);


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

/*
 * Class:     bindings_Sleuthkit__
 * Method:    getFileInode
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_getFileInode(JNIEnv * env, jclass obj, jlong fileSystem, jstring path) {
    const char* path_ = (*env)->GetStringUTFChars(env, path, 0);

    TSK_FS_INFO* filesystem = (TSK_FS_INFO*) fileSystem;
    TSK_FS_FILE* file = tsk_fs_file_open(filesystem, NULL, path_);
    TSK_INUM_T inode = file->meta->addr;
    tsk_fs_file_close(file);
    return (jlong) inode;
}

/*
 * Class:     bindings_Sleuthkit__
 * Method:    openFileNat
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_openFileNat(JNIEnv * env, jclass obj, jlong fileSystem, jlong inode) {
    TSK_FS_INFO* filesystem = (TSK_FS_INFO*) fileSystem;
    TSK_INUM_T address = (TSK_INUM_T) inode;
    TSK_FS_FILE* file = tsk_fs_file_open_meta(filesystem, NULL, address);
    return (jlong) file;
}

/*
 * Class:     bindings_Sleuthkit__
 * Method:    closeFileNat
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_bindings_Sleuthkit_00024_closeFileNat(JNIEnv * env, jclass obj, jlong file) {
    TSK_FS_FILE* file_ = (TSK_FS_FILE*) file;
    tsk_fs_file_close(file_);
    return;
}

/*
 * Class:     bindings_Sleuthkit__
 * Method:    readNat
 * Signature: (JJJ)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_bindings_Sleuthkit_00024_readNat(JNIEnv * env, jclass obj, jlong file, jlong offset, jlong count) {
    TSK_FS_FILE* file_ = (TSK_FS_FILE*) file;

    long bytesLeft = file_->meta->size - offset;
    if( bytesLeft < 0 ) {
        bytesLeft = 0;
    }

    count = bytesLeft < count ? bytesLeft : count;

    jbyteArray data = (*env)->NewByteArray(env, count);
    if (data == NULL) {
        throwOutOfMemoryError(env, "Not enough space for the ByteArray.");
    }

    jbyte *bytes = (jbyte*) (malloc(sizeof(char) * count));

    ssize_t read = tsk_fs_file_read(file_, offset, bytes, count, TSK_FS_FILE_READ_FLAG_NONE);

    (*env)->SetByteArrayRegion(env, data, 0, count, bytes);

    free(bytes);

    return data;
}

/*
 * Class:     bindings_Sleuthkit__
 * Method:    readToBufferNat
 * Signature: (JJJ[BJ)J
 */
JNIEXPORT jlong JNICALL
Java_bindings_Sleuthkit_00024_readToBufferNat(JNIEnv * env, jclass obj, jlong file, jlong fileOffset, jlong count, jbyteArray buffer, jlong bufferOffset) {
    TSK_FS_FILE* file_ = (TSK_FS_FILE*) file;

    long bytesLeft = file_->meta->size - fileOffset;
    count = bytesLeft < count ? bytesLeft : count;

    if( count == 0 ) {
        return -1; // File ended
    }

    jbyte *bytes = (jbyte*) (malloc(sizeof(char) * count));

    ssize_t read = tsk_fs_file_read(file_, fileOffset, bytes, count, TSK_FS_FILE_READ_FLAG_NONE);

    if( read < 0 ) {
        return read;
    }

    (*env)->SetByteArrayRegion(env, buffer, bufferOffset, read, bytes);

    free(bytes);

    return (jlong) read;
}




