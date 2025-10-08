@echo off
"C:\\Users\\Adarsh Sharma\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HC:\\Users\\Adarsh Sharma\\code\\project\\FLAM-project\\android\\app\\src\\main\\cpp" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=x86" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86" ^
  "-DANDROID_NDK=C:\\Users\\Adarsh Sharma\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\Adarsh Sharma\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\Adarsh Sharma\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\Adarsh Sharma\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=C:\\Users\\Adarsh Sharma\\code\\project\\FLAM-project\\android\\app\\build\\intermediates\\cxx\\Debug\\646f3p3w\\obj\\x86" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=C:\\Users\\Adarsh Sharma\\code\\project\\FLAM-project\\android\\app\\build\\intermediates\\cxx\\Debug\\646f3p3w\\obj\\x86" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-DCMAKE_FIND_ROOT_PATH=C:\\Users\\Adarsh Sharma\\code\\project\\FLAM-project\\android\\app\\.cxx\\Debug\\646f3p3w\\prefab\\x86\\prefab" ^
  "-BC:\\Users\\Adarsh Sharma\\code\\project\\FLAM-project\\android\\app\\.cxx\\Debug\\646f3p3w\\x86" ^
  -GNinja
