cd /d %~dp0

set resource_dir=%cd%\resources
set PATH=%resource_dir%\jdk8\bin;%PATH%
set JAVA_HOME=%resource_dir%\jdk8
set PATH=%resource_dir%\ant\bin;%PATH%
set PATH=%resource_dir%\sqlite-tools;%PATH%

cd ..\..
if not exist .\gutflora.db sqlite3 .\gutflora.db < documents\create_tables_sqlite.sql

call ant devmode

exit 1
