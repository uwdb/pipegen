#!/usr/bin/env bash
cd build
/usr/lib/jvm/java-8-openjdk-amd64/bin/java -classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/java-atk-wrapper.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar:/home/bhaynes/research/pipegen/target/production/pipegen:/home/bhaynes/research/pipegen/lib/jackson-databind-2.7.2.jar:/home/bhaynes/research/pipegen/lib/jackson-annotations-2.7.0.jar:/home/bhaynes/research/pipegen/lib/jackson-core-2.7.2.jar:/home/bhaynes/research/pipegen/lib/javassist-3.20.0-GA.jar:/home/bhaynes/research/pipegen/lib/guava-19.0.jar:/home/bhaynes/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.7.2/jackson-core-2.7.2.jar:/home/bhaynes/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.7.2/jackson-databind-2.7.2.jar:/home/bhaynes/research/pipegen/lib/btrace-agent-1.3.4.jar:/home/bhaynes/research/pipegen/lib/vector-0.1-SNAPSHOT.jar:/home/bhaynes/research/pipegen/lib/commons-lang3-3.4.jar:/home/bhaynes/research/pipegen/lib/javassist-3.12.1.GA.jar:/home/bhaynes/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.7.0/jackson-annotations-2.7.0.jar:/home/bhaynes/research/pipegen/lib/btrace-client-1.3.4.jar:/home/bhaynes/research/pipegen/lib/btrace-boot-1.3.4.jar:/home/bhaynes/research/pipegen/lib/soot-trunk.jar:/home/bhaynes/research/pipegen/lib/hadoop-core-1.2.1.jar:/home/bhaynes/research/pipegen/lib/commons-cli-1.2.jar:/home/bhaynes/research/pipegen/lib/xmlenc-0.52.jar:/home/bhaynes/research/pipegen/lib/jersey-core-1.8.jar:/home/bhaynes/research/pipegen/lib/jersey-json-1.8.jar:/home/bhaynes/research/pipegen/lib/jettison-1.1.jar:/home/bhaynes/research/pipegen/lib/stax-api-1.0.1.jar:/home/bhaynes/research/pipegen/lib/jaxb-impl-2.2.3-1.jar:/home/bhaynes/research/pipegen/lib/jaxb-api-2.2.2.jar:/home/bhaynes/research/pipegen/lib/stax-api-1.0-2.jar:/home/bhaynes/research/pipegen/lib/activation-1.1.jar:/home/bhaynes/research/pipegen/lib/jackson-core-asl-1.7.1.jar:/home/bhaynes/research/pipegen/lib/jackson-mapper-asl-1.8.8.jar:/home/bhaynes/research/pipegen/lib/jackson-jaxrs-1.7.1.jar:/home/bhaynes/research/pipegen/lib/jackson-xc-1.7.1.jar:/home/bhaynes/research/pipegen/lib/jersey-server-1.8.jar:/home/bhaynes/research/pipegen/lib/asm-3.1.jar:/home/bhaynes/research/pipegen/lib/commons-io-2.1.jar:/home/bhaynes/research/pipegen/lib/commons-httpclient-3.0.1.jar:/home/bhaynes/research/pipegen/lib/junit-3.8.1.jar:/home/bhaynes/research/pipegen/lib/commons-logging-1.0.3.jar:/home/bhaynes/research/pipegen/lib/commons-codec-1.4.jar:/home/bhaynes/research/pipegen/lib/commons-math-2.1.jar:/home/bhaynes/research/pipegen/lib/commons-configuration-1.6.jar:/home/bhaynes/research/pipegen/lib/commons-collections-3.2.1.jar:/home/bhaynes/research/pipegen/lib/commons-lang-2.4.jar:/home/bhaynes/research/pipegen/lib/commons-digester-1.8.jar:/home/bhaynes/research/pipegen/lib/commons-beanutils-1.7.0.jar:/home/bhaynes/research/pipegen/lib/commons-beanutils-core-1.8.0.jar:/home/bhaynes/research/pipegen/lib/commons-net-1.4.1.jar:/home/bhaynes/research/pipegen/lib/oro-2.0.8.jar:/home/bhaynes/research/pipegen/lib/jetty-6.1.26.jar:/home/bhaynes/research/pipegen/lib/jetty-util-6.1.26.jar:/home/bhaynes/research/pipegen/lib/servlet-api-2.5-20081211.jar:/home/bhaynes/research/pipegen/lib/jasper-runtime-5.5.12.jar:/home/bhaynes/research/pipegen/lib/jasper-compiler-5.5.12.jar:/home/bhaynes/research/pipegen/lib/jsp-api-2.1-6.1.14.jar:/home/bhaynes/research/pipegen/lib/servlet-api-2.5-6.1.14.jar:/home/bhaynes/research/pipegen/lib/jsp-2.1-6.1.14.jar:/home/bhaynes/research/pipegen/lib/core-3.1.1.jar:/home/bhaynes/research/pipegen/lib/ant-1.6.5.jar:/home/bhaynes/research/pipegen/lib/commons-el-1.0.jar:/home/bhaynes/research/pipegen/lib/jets3t-0.6.1.jar:/home/bhaynes/research/pipegen/lib/hsqldb-1.8.0.10.jar:/home/bhaynes/research/pipegen/lib/arrow-memory-0.1-SNAPSHOT.jar:/home/bhaynes/research/pipegen/lib/guava-18.0.jar:/usr/local/idea-IC-145.258.11/lib/idea_rt.jar org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryServer verification