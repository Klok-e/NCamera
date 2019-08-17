#include <jni.h>
#include <string>
#include <android/bitmap.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_neuralcamera_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_neuralcamera_MainActivity_processImage(JNIEnv *env, jobject /* this */, jbyteArray byte_array, int width,
                                                int height) {
    jbyte *bufferPtr = env->GetByteArrayElements(byte_array, nullptr);

}
