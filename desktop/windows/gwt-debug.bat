cd /d %~dp0

set version=0.1.0
set app_name=manta
set release_name=%app_name%-%version%-windows-x64
set release_dir=desktop\windows\releases
set release_path=%release_dir%\%release_name%
set resource_dir=%cd%\resources
set PATH=%resource_dir%\jdk8\bin;%PATH%
set JAVA_HOME=%resource_dir%\jdk8
set PATH=%resource_dir%\ant\bin;%PATH%
set PATH=%resource_dir%\sqlite-tools;%PATH%
set PATH=%resource_dir%\exewrap\x64;%PATH%

cd ..\..
sqlite3 .\gutflora.db < documents\create_tables_sqlite.sql

call ant devmode

exit 1
