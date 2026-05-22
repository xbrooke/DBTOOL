@echo off
echo Building DBTOOL project...
call gradlew.bat build --no-daemon
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build successful!
    echo APK location: app\build\outputs\apk\release\
) else (
    echo.
    echo ❌ Build failed!
    echo Check the error messages above.
)
pause
