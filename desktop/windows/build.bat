echo %cd%
cd /d %~dp0
echo %cd%
set version=0.1.0
set app_name=manta
set release_name=%app_name%-%version%-windows-x64
set release_dir=desktop\windows\releases
set release_path=%release_dir%\%release_name%
set resource_dir=%cd%\resources
set PATH=%resource_dir%\jdk\bin;%PATH%
set JAVA_HOME=%resource_dir%\jdk
set PATH=%resource_dir%\ant\bin;%PATH%
set PATH=%resource_dir%\sqlite-tools;%PATH%
set PATH=%resource_dir%\exewrap\x64;%PATH%

cd ..\..
call ant clean
call ant

cd war
jar -cf manta.war *

cd ..
if exist %release_path% rmdir /s/q %release_path%
mkdir %release_path%

copy /y war\manta.war %release_path%\manta.war
xcopy /s/e/i/y/q %resource_dir%\tomcat-embed %release_path%\tomcat-embed

sqlite3 %release_path%\gutflora.db < documents\create_tables.sql

cd %release_path%
javac -classpath ".\tomcat-embed\*" -d . ..\..\MantaLauncher.java
jar -c -v -f .\MantaLauncher.jar -e MantaLauncher .\MantaLauncher.class
exewrap -t 1.11 -L .;.\tomcat-embed\* -e SHARE .\MantaLauncher.jar
del .\MantaLauncher.class

cd ..\..\..\..
for /f "tokens=* USEBACKQ" %%f in (`jdeps --print-module-deps %release_path%\MantaLauncher.jar war\WEB-INF\classes\* war\WEB-INF\lib\*`) do ( set MODULE_DEPS=%%f)
set MODULE_DEPS=%MODULE_DEPS%,jdk.localedata,java.instrument
echo Module dependancies: %MODULE_DEPS%
jlink --compress=2 --module-path %resource_dir%\jdk\jmods --add-modules %MODULE_DEPS% --output %release_path%\jre

cd %release_dir%
powershell compress-archive %release_name% %release_name%.zip -Force
rmdir /s/q %release_path%

exit 1
