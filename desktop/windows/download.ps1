[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$scriptpath = $MyInvocation.MyCommand.Path
$dir = Split-Path $scriptpath
Set-Location $dir

if(!(Test-Path resources )){
    mkdir resources
}

if(!(Test-Path ..\resources )){
    mkdir ..\resources
}

Set-Location resources

# if(!(Test-Path gwt )){
#     Invoke-WebRequest -Uri https://goo.gl/pZZPXS -OutFile .\gwt.zip 
#     Expand-Archive -Path gwt.zip -DestinationPath .
#     Move-Item gwt-2.8.2 gwt
# }
if(!(Test-Path ..\..\resources\gwt )){
    Write-Output "Downloading gwt"
    Invoke-WebRequest -Uri http://goo.gl/t7FQSn -OutFile .\gwt.zip
    Expand-Archive -Path gwt.zip -DestinationPath .
    Move-Item gwt-2.7.0 ..\..\resources\gwt
}
if(Test-Path .\gwt\samples){
	Remove-Item .\gwt\samples -Recurse -Force -Confirm:$false
}
if(Test-Path .\gwt\doc){
	Remove-Item .\gwt\doc -Recurse -Force -Confirm:$false
}
# if(!(Test-Path jdk11 )){
#     Invoke-WebRequest -Uri https://d3pxv6yz143wms.cloudfront.net/11.0.3.7.1/amazon-corretto-11.0.3.7.1-windows-x64.zip -OutFile .\corretto.zip
#     Expand-Archive -Path corretto.zip -DestinationPath .
#     Move-Item jdk11.0.3_7 jdk11
# }
if(!(Test-Path jdk8 )){
    Write-Output "Downloading jdk"
    Invoke-WebRequest -Uri https://d3pxv6yz143wms.cloudfront.net/8.232.09.1/amazon-corretto-8.232.09.1-windows-x64-jdk.zip -OutFile .\corretto.zip
    Expand-Archive -Path corretto.zip -DestinationPath .
    Move-Item jdk1.8.0_232 jdk8
}
if(!(Test-Path jre8 )){
    Write-Output "Downloading jre"
    Invoke-WebRequest -Uri https://d3pxv6yz143wms.cloudfront.net/8.232.09.1/amazon-corretto-8.232.09.1-windows-x64-jre.zip -OutFile .\corretto-jre.zip
    Expand-Archive -Path corretto-jre.zip -DestinationPath .
}
if(!(Test-Path ant )){
    Write-Output "Downloading ant"
    Invoke-WebRequest -Uri http://ftp.riken.jp/net/apache//ant/binaries/apache-ant-1.10.7-bin.zip -OutFile .\ant.zip
    Expand-Archive -Path ant.zip -DestinationPath .
    Move-Item apache-ant-1.10.7 ant
}
if(!(Test-Path tomcat-embed )){
    Write-Output "Downloading tomcat"
    Invoke-WebRequest -Uri http://ftp.riken.jp/net/apache/tomcat/tomcat-8/v8.5.47/bin/embed/apache-tomcat-8.5.47-embed.zip -OutFile .\tomcat-embed.zip
    Expand-Archive -Path tomcat-embed.zip -DestinationPath .\tomcat-embed
}
if(!(Test-Path sqlite-tools )){
    Write-Output "Downloading sqlite"
    Invoke-WebRequest -Uri https://www.sqlite.org/2019/sqlite-tools-win32-x86-3300100.zip -OutFile .\sqlite-tools.zip
    Expand-Archive -Path sqlite-tools.zip -DestinationPath .
    Move-Item sqlite-tools-win32-x86-3300100 sqlite-tools
}
if(!(Test-Path exewrap )){
    Write-Output "Downloading exewrap"
    Invoke-WebRequest -Uri "https://ja.osdn.net/frs/redir.php?m=ymu&f=exewrap%2F71580%2Fexewrap1.4.2.zip" -OutFile .\exewrap.zip
    Expand-Archive -Path exewrap.zip -DestinationPath .
    Move-Item exewrap1.4.2 exewrap
}

Set-Location ..\..\..

if(!(Test-Path .\war\WEB-INF\lib\sqlite-jdbc-3.27.2.1.jar )){
    Write-Output "Downloading sqlite-jdbc"
    Invoke-WebRequest -Uri https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.27.2.1.jar -OutFile .\war\WEB-INF\lib\sqlite-jdbc-3.27.2.1.jar
}
