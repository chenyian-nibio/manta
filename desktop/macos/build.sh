#!/bin/bash
cd "$(dirname "$0")"

BUILD_ROOT=$PWD
APP_NAME="manta"
APP_ROOT=`echo $(cd ../.. && pwd)`
VERSION=`echo $(cat $APP_ROOT/version.txt)`
RELEASE_NAME="$APP_NAME-$VERSION"
BUNDLE_NAME="$RELEASE_NAME-macos"
RELEASE_DIR=$PWD/releases/$VERSION
RESOURCE_DIR=$PWD/resources
PATH=$RESOURCE_DIR/jdk8/bin:$PATH
PATH=$RESOURCE_DIR/ant/bin:$PATH
PATH=$RESOURCE_DIR:$PATH
JAVA_HOME=$RESOURCE_DIR/jdk8
export JAVA_HOME
java -version


PATH=$RESOURCE_DIR/sqlite-tools:$PATH

cd $APP_ROOT

if [ -e war/manta.war ]; then
  rm war/manta.war
fi
ant clean
ant

cd war
jar -cf manta.war *
cd ..

if [ ! -d $RELEASE_DIR/$RELEASE_NAME ]; then
  mkdir -p $RELEASE_DIR/$RELEASE_NAME
else
  rm -rf $RELEASE_DIR/$RELEASE_NAME/*
fi

if [ -d $RELEASE_DIR/$BUNDLE_NAME ]; then
  rm -rf $RELEASE_DIR/$BUNDLE_NAME
fi

cp war/manta.war $RELEASE_DIR/$RELEASE_NAME/
cp -r $RESOURCE_DIR/tomcat-embed $RELEASE_DIR/$RELEASE_NAME/
mkdir $RELEASE_DIR/$RELEASE_NAME/lib
cp $RESOURCE_DIR/commons-lang3-3.9.jar $RELEASE_DIR/$RELEASE_NAME/lib/
sqlite3 $RELEASE_DIR/$RELEASE_NAME/gutflora.db < documents/create_tables_sqlite.sql

cd $RELEASE_DIR/$RELEASE_NAME
javac -classpath ".:./lib/*:./tomcat-embed/*" -d . $APP_ROOT/desktop/MantaLauncher.java
jar cfmv MantaLauncher.jar $APP_ROOT/desktop/manifest.txt MantaLauncher.class
rm MantaLauncher.class

javapackager -deploy \
  -native pkg \
  -outdir ../$BUNDLE_NAME \
  -outfile $BUNDLE_NAME \
  -srcdir . \
  -srcfiles MantaLauncher.jar \
  -srcfiles manta.war \
  -srcfiles gutflora.db \
  -srcfiles tomcat-embed \
  -srcfiles lib \
  -appclass MantaLauncher \
  -name "Manta" -title "Manta" \
  -BmainJar=MantaLauncher.jar \
  -BappVersion=$VERSION \
  -BjvmOptions=-showversion \
  -v \
  -Bruntime="$RESOURCE_DIR/amazon-corretto-8.jdk"
#   # -Bruntime=

cd $BUILD_ROOT

