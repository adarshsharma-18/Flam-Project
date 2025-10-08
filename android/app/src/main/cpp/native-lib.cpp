#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_cameraapp_MainActivity_processFrameJNI(JNIEnv* env, jobject /*thiz*/, jlong addrInput, jlong addrOutput) {
    Mat& input = *(Mat*)addrInput;
    Mat& output = *(Mat*)addrOutput;

    // Convert to grayscale
    cvtColor(input, output, COLOR_RGBA2GRAY);

    // Apply Canny Edge Detection
    Canny(output, output, 80, 150);
}

