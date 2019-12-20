#!/bin/bash
# Build script for Mac OS
cd "$(dirname "$0")"

bash ./download.sh

APP_NAME="manta"
BUILD_ROOT=`echo $(cd .. && pwd)`
APP_ROOT=`echo $(cd ../.. && pwd)`
VERSION=`echo $(cat $APP_ROOT/version.txt)`
RELEASE_NAME="$APP_NAME-$VERSION"
BUNDLE_NAME="$RELEASE_NAME-macos"
RELEASE_DIR=$PWD/releases/$VERSION
MAC_RESOURCE_DIR=$PWD/resources
PATH=$MAC_RESOURCE_DIR/jdk/Contents/Home/bin:$PATH
JAVA_HOME=$MAC_RESOURCE_DIR/jdk/Contents/Home/
export JAVA_HOME
RESOURCE_DIR=$BUILD_ROOT/resources
PATH=$RESOURCE_DIR/ant/bin:$PATH
PATH=$RESOURCE_DIR:$PATH
java -version


PATH=$MAC_RESOURCE_DIR/sqlite-tools:$PATH

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
cp $BUILD_ROOT/icons/manta.icns $RELEASE_DIR/$RELEASE_NAME/
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
  -Bicon=manta.icns \
  -BmainJar=MantaLauncher.jar \
  -BappVersion=$VERSION \
  -BjvmOptions=-showversion \
  -v \
  -Bruntime="$MAC_RESOURCE_DIR/jdk"

cd $BUILD_ROOT/macos
