@echo off
echo Copying frames from Android device...

REM Create the adb command to pull the file
adb pull /sdcard/Pictures/EdgeDetection/processed_frame.jpg processed_frame.jpg

if %ERRORLEVEL% EQU 0 (
    echo ✅ Frame copied successfully!
    echo 🔄 Refreshing web viewer...
) else (
    echo ❌ Failed to copy frame. Make sure:
    echo   1. Android device is connected via USB
    echo   2. USB debugging is enabled
    echo   3. You have saved a frame in the app
    echo   4. ADB is installed and in PATH
)

pause
