@echo off
echo setup gui wallet

java -version >nul 2>&1 && (
    goto java_found
) || (
    goto java_not_found
)
   
:java_not_found
	echo JAVA NOT FOUND
	
:download_java
	reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set OS=32BIT || set OS=64BIT
	
	echo It is recommended to use Java 14 to run GUI Wallet.
	echo The direct install link is here:
	
	if %OS%==32BIT echo https://github.com/AdoptOpenJDK/openjdk14-binaries/releases/download/jdk-14.0.2%%2B12/OpenJDK14U-jdk_x86-32_windows_hotspot_14.0.2_12.msi
	if %OS%==64BIT echo https://github.com/AdoptOpenJDK/openjdk14-binaries/releases/download/jdk-14.0.2%%2B12/OpenJDK14U-jre_x64_windows_hotspot_14.0.2_12.msi
	
	echo Please first install Java and then re-run `start.bat` 
	
	pause
	exit /B 1
	
:java_found
	echo FOUND JAVA
	java -version
	echo Continue...
	for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "jver=%%j%%k%%l%%m"
	
    if "%jver%" NEQ "1402+12" (
      goto download_java
    )

	rem Kick off GUI Wallet in separate window
	cd bin/
	start veriblock-wallet-ui.bat
	exit /B 1