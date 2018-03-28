
#include "tsk_bindings.h"


TSK_WALK_RET_ENUM walk_dir(TskFsFile *a_fs_file, const char *a_path, void *a_ptr) {
    TskFsName* name = a_fs_file->getName();

    std::cout << name->getName() << std::endl;
}

JNIEXPORT jlong JNICALL
Java_org_sleuthkit_datamodel_SleuthkitJNI_openImgNat(JNIEnv * env, jclass obj, jobjectArray paths, jint num_imgs) {
    TSK_IMG_INFO *img_info;
    jboolean isCopy;

    const char *image_path = (char *) env->
                            GetStringUTFChars((jstring) env->GetObjectArrayElement(paths, 0), &isCopy);
    const char* const images[] = {image_path};

    img_info = tsk_img_open_utf8(1, images, TSK_IMG_TYPE_DETECT, 0);
    env-> ReleaseStringUTFChars((jstring) env->GetObjectArrayElement(paths, 0), images[0]);
    return (jlong) img_info;


}



void open(){

// Opening image
    char const* image = "/home/przemek/Dokumenty/agh/out.img";
    static const char* const images[] = {image};
    const int images_count = 1;
    const int sector_size = 0; // Auto detect
    TSK_IMG_INFO* img_info = tsk_img_open_utf8(images_count, images, TSK_IMG_TYPE_DETECT, sector_size);
    assert(img_info != NULL);

// Opening filesystem
    TSK_OFF_T offset = 0; // I guess
    TSK_FS_INFO* filesystem_info = tsk_fs_open_img(img_info, 0, TSK_FS_TYPE_DETECT);
    assert(filesystem_info != NULL);

    TskFsInfo* filesystem = new TskFsInfo(filesystem_info); // Uff, finally xD
    assert(filesystem_info != NULL);

    TSK_INUM_T rootNode = filesystem->getRootINum();
    filesystem->dirWalk(rootNode, TSK_FS_DIR_WALK_FLAG_ALLOC, walk_dir, NULL);

// Close the fucking file
    delete filesystem; // Closes the filesystem
    tsk_img_close(img_info);
}


