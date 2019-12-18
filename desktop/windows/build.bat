cd /d %~dp0

powershell -ExecutionPolicy Bypass -command .\download.ps1 || exit /b 1

cd ..
set buidl_root=%cd%
cd ..
set app_root=%cd%
cd %buidl_root%

cd %buidl_root%\windows

set app_name=manta
set /p version=<%app_root%\version.txt
set release_name=%app_name%-%version%-windows-x64
set release_dir=%cd%\releases
set release_path=%release_dir%\%release_name%
set resource_dir=%cd%\resources
set PATH=%resource_dir%\jdk8\bin;%PATH%
set JAVA_HOME=%resource_dir%\jdk8
set PATH=%resource_dir%\ant\bin;%PATH%
set PATH=%resource_dir%\sqlite-tools;%PATH%
set PATH=%resource_dir%\exewrap\x64;%PATH%
java -version

cd %app_root%
call ant clean
call ant

cd %app_root%\war
jar -cf manta.war *

cd %app_root%
if not exist %release_dir% mkdir %release_dir%
if exist %release_path% rmdir /s/q %release_path%
mkdir %release_path%
mkdir %release_path%\lib

copy /y war\manta.war %release_path%\manta.war
xcopy /s/e/i/y/q %resource_dir%\tomcat-embed %release_path%\tomcat-embed
copy /y %resource_dir%\commons-lang3-3.9.jar %release_path%\lib\commons-lang3-3.9.jar

sqlite3 %release_path%\gutflora.db < documents\create_tables_sqlite.sql

cd %release_path%
javac -classpath ".;.\lib\*;.\tomcat-embed\*" -d . %buidl_root%\MantaLauncher.java
jar cfm .\MantaLauncher.jar %buidl_root%\manifest.txt .\MantaLauncher.class
exewrap -t 1.8 -L .;.\lib\*;.\tomcat-embed\* -e SHARE -i %buidl_root%\icons\manta.ico .\MantaLauncher.jar
del .\MantaLauncher.class
xcopy /s/e/i/y/q %resource_dir%\jdk8\jre %release_path%\jre

cd %release_dir%
powershell compress-archive %release_name% %release_name%.zip -Force
rmdir /s/q %release_name%

REM javapackager -deploy ^
REM   -native exe ^
REM   -outdir ..\%release_name% ^
REM   -outfile %release_name% ^
REM   -srcdir . ^
REM   -srcfiles MantaLauncher.jar ^
REM   -srcfiles manta.war ^
REM   -srcfiles gutflora.db ^
REM   -srcfiles tomcat-embed ^
REM   -srcfiles lib ^
REM   -appclass MantaLauncher ^
REM   -name "Manta" -title "Manta" ^
REM   -BmainJar=MantaLauncher.jar ^
REM   -BappVersion=%version% ^
REM   -BjvmOptions=-showversion ^
REM   -v ^
REM   -Bruntime="%resource_dir%\jre8"

exit /b 0
