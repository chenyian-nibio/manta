### Building manta desktop version for windows
1. Run `download.ps1` to download & extract jdk and other libraries.
    - If you couldn't run PS script because of Security Exception, run as below
    ```
    powershell -ExecutionPolicy Bypass -command .\download.ps1
    ```
2. Run `build.bat` to generate application zip file for download.
