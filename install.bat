@echo off
setlocal

echo ==================================================
echo       Restaurant POS - Application Installer     
echo ==================================================
echo.

:: 1. Check Java
echo [1/4] Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java is not installed or not found in PATH.
    echo Please install Java 21 or higher from https://adoptium.net/
    pause
    exit /b 1
)
echo Java runtime detected: OK

:: 2. Check or build JAR
echo [2/4] Verifying executable package...
set SCRIPT_DIR=%~dp0
set JAR_PATH=%SCRIPT_DIR%target\app.jar

if not exist "%JAR_PATH%" (
    echo Building application JAR using Maven...
    call mvn clean package -DskipTests
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Maven build failed. Please ensure Maven is installed.
        pause
        exit /b 1
    )
)

:: 3. Create install directory
echo [3/4] Installing application files...
set INSTALL_DIR=%LOCALAPPDATA%\RestaurantPOS
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

copy /Y "%JAR_PATH%" "%INSTALL_DIR%\app.jar" >nul
if exist "%SCRIPT_DIR%sample-images" (
    xcopy /E /I /Y "%SCRIPT_DIR%sample-images" "%INSTALL_DIR%\sample-images" >nul
)

:: Create run batch file
(
echo @echo off
echo cd /d "%%~dp0"
echo start javaw -jar "%%~dp0app.jar"
) > "%INSTALL_DIR%\restaurant-pos.bat"

:: 4. Create desktop shortcut via PowerShell
echo [4/4] Creating desktop shortcuts...
powershell -Command "$ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut([System.Environment]::GetFolderPath('Desktop') + '\Restaurant POS.lnk'); $s.TargetPath = '%INSTALL_DIR%\restaurant-pos.bat'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Save()" >nul 2>&1

echo.
echo ==================================================
echo         Installation Completed Successfully!      
echo ==================================================
echo.
echo You can launch the application from the 'Restaurant POS' icon on your Desktop.
echo.
pause
