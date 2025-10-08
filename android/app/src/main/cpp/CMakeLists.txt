cmake_minimum_required(VERSION 3.10.2)
project("edge_detection")

# Use local OpenCV Android SDK via OpenCV_DIR
# Expected local SDK path: app/src/main/sdk/sdk/native/jni
set(_LOCAL_OPENCV_DIR "${CMAKE_CURRENT_LIST_DIR}/../sdk/sdk/native/jni")
if(EXISTS "${_LOCAL_OPENCV_DIR}/OpenCVConfig.cmake")
    set(OpenCV_DIR "${_LOCAL_OPENCV_DIR}")
endif()
find_package(OpenCV REQUIRED)

add_library(native-lib SHARED native-lib.cpp)

target_include_directories(native-lib PRIVATE ${OpenCV_INCLUDE_DIRS})
target_link_libraries(native-lib ${OpenCV_LIBS})

