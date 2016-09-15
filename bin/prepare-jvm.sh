MAVIN_BIN=/usr/bin/mvn
MAVEN_REPOSITORY=$HOME/.m2/repository
BUILD_DIR=.
TARGET_DIR=./stage

pwd
mkdir $TARGET_DIR
$MAVI_BIN dependency:resolve

$JAVA_HOME/bin/javac -classpath "$MAVEN_REPOSITORY/org/javassist/javassist/3.20.0-GA/javassist-3.20.0-GA.jar:$MAVEN_REPOSITORY/com/google/guava/guava/18.0/guava-18.0.jar" \
           -d $TARGET_DIR \
           $BUILD_DIR/src/main/java/org/brandonhaynes/pipegen/utilities/JvmUtilities.java \
           $BUILD_DIR/src/main/java/org/brandonhaynes/pipegen/mutation/ClassModifierReplacer.java \
           $BUILD_DIR/src/main/java/org/brandonhaynes/pipegen/utilities/JarUtilities.java \
           $BUILD_DIR/src/main/java/org/brandonhaynes/pipegen/configuration/Version.java \
           $BUILD_DIR/src/main/java/org/brandonhaynes/pipegen/utilities/JarClassPath.java
sudo $JAVA_HOME/bin/java -classpath "$TARGET_DIR:$MAVEN_REPOSITORY/org/javassist/javassist/3.20.0-GA/javassist-3.20.0-GA.jar:$MAVEN_REPOSITORY/com/google/guava/guava/18.0/guava-18.0.jar" \
          org.brandonhaynes.pipegen.utilities.JvmUtilities RemoveFinalFlagFromString java.lang.String,java.lang.StringBuilder,java.lang.StringBuffer
