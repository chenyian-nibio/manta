#!/bin/bash
cd "$(dirname "$0")"

JDK="amazon-corretto-8.232.09.1-macosx-x64"
GWT="gwt-2.7.0"
ANT="apache-ant-1.10.7"
TOM="apache-tomcat-8.5.49-embed"
SERV="apache-tomcat-8.5.49-deployer"
SQL="sqlite-tools-osx-x86-3300100"

if [ ! -d ./resources ]; then
  mkdir resources
fi

if [ ! -d ../resources ]; then
  mkdir ../resources
fi

cd resources
if [ ! -d jdk8 ]; then
  wget https://d3pxv6yz143wms.cloudfront.net/8.232.09.1/$JDK.tar.gz
  tar -xvf $JDK.tar.gz
  cp -r amazon-corretto-8.jdk/Contents/Home jdk8
fi

if [ ! -d ../../resources/gwt ]; then
  wget http://storage.googleapis.com/gwt-releases/$GWT.zip -O gwt.zip
  unzip gwt.zip -x "$GWT/doc/*" "$GWT/samples/*"
  mv $GWT ../../resources/gwt
fi

if [ ! -d ant ]; then
  wget http://ftp.riken.jp/net/apache//ant/binaries/$ANT-bin.tar.gz
  tar -xvf $ANT-bin.tar.gz
  mv $ANT ant
fi

if [ ! -d tomcat-embed ]; then
  wget http://ftp.riken.jp/net/apache/tomcat/tomcat-8/v8.5.49/bin/embed/$TOM.tar.gz
  tar -xvf $TOM.tar.gz
  mv $TOM tomcat-embed
fi

if [ ! -d sqlite-tools ]; then
  wget https://www.sqlite.org/2019/$SQL.zip
  unzip $SQL.zip
  mv $SQL sqlite-tools
fi

if [ ! -e commons-lang3-3.9.jar ]; then
  wget https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.jar
fi

cd ..
