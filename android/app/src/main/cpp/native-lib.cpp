#include <jni.h>
#include <opencv2/opencv.hpp>
using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_cameraapp_MainActivity_processFrameJNI(JNIEnv *env, jobject thiz, jlong addrInput, jlong addrOutput) {
    Mat &input = *(Mat*)addrInput;   // expected RGBA (CV_8UC4)
    Mat &output = *(Mat*)addrOutput; // empty Mat; we will assign output

    if (input.empty()) return;

    Mat gray;
    cvtColor(input, gray, COLOR_RGBA2GRAY);     // RGBA2GRAY
    Mat edges;
    Canny(gray, edges, 80, 150);                // tweak thresholds as needed

    // Convert edges (single channel) to RGBA so Android can display it
    Mat edgesRGBA;
    cvtColor(edges, edgesRGBA, COLOR_GRAY2RGBA);

    edgesRGBA.copyTo(output);
}

