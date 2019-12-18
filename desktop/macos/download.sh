#!/bin/bash
cd "$(dirname "$0")"

if [ ! -d ./resources ]; then
  mkdir resources
fi

if [ ! -d ../resources ]; then
  mkdir ../resources
fi

cd resources
if [ ! -d jdk8 ]; then
  curl https://d3pxv6yz143wms.cloudfront.net/8.232.09.1/amazon-corretto-8.232.09.1-macosx-x64.tar.gz -o jdk8.tar.gz
  tar -xvf jdk8.tar.gz
  cp -r amazon-corretto-8.jdk/Contents/Home jdk8
fi

if [ ! -d ../../resources/gwt ]; then
  GWT="gwt-2.7.0"
  curl http://storage.googleapis.com/gwt-releases/$GWT.zip -o gwt.zip
  unzip gwt.zip -x "$GWT/doc/*" "$GWT/samples/*"
  mv $GWT ../../resources/gwt
fi

if [ ! -d ant ]; then
  ANT="apache-ant-1.10.7"
  curl https://archive.apache.org/dist/ant/binaries/$ANT-bin.tar.gz -o ant.tar.gz
  tar -xvf ant.tar.gz
  mv $ANT ant
fi

if [ ! -d tomcat-embed ]; then
  TOM="apache-tomcat-8.5.49-embed"
  curl https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.49/bin/embed/$TOM.tar.gz -o tomcat-embed.tar.gz
  tar -xvf tomcat-embed.tar.gz
  mv $TOM tomcat-embed
fi

if [ ! -d sqlite-tools ]; then
  SQL="sqlite-tools-osx-x86-3300100"
  curl https://www.sqlite.org/2019/$SQL.zip -o sqlite-tools.zip
  unzip sqlite-tools.zip
  mv $SQL sqlite-tools
fi

if [ ! -e commons-lang3-3.9.jar ]; then
  curl -O https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.9/commons-lang3-3.9.jar
fi

cd ..
