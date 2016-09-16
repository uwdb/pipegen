# PipeGen: Data Pipe Generation for Hybrid Analytics

![PipeGen Build Status](https://travis-ci.org/uwdb/pipegen.svg?branch=master "Build Status")

PipeGen allows users to automatically create an efficient connection between pairs of database systems. PipeGen targets data analytics workloads on shared-nothing engines, and supports scenarios where users seek to perform different parts of an analysis in different DBMSs or want to combine and analyze data stored in different systems. The systems may be colocated in the same cluster or may be in different clusters.

This is the core repository for the PipeGen tool, which may to create an optimized data transfer connector between a pair of Java database systems.  For a higher-level overview, visit the [project website](http://db.cs.washington.edu/projects/pipegen) or [read the paper](https://arxiv.org/pdf/1605.01664v2.pdf).

## Creating a Data Pipe

To add a data pipe to a new system, first create a configuration that describes the system.  For example, to add a data pipe to the [Myria DBMS](http://myria.cs.washington.edu/) we use the following configuration:

```YAML
name: myria 									          # A name used when transferring data to this DBMS
version: 51										          # Java version to use during instrumentation
path: $HOME/myria 								          # Location of the DBMS being modified
instrumentation:
  classPaths:									          # Location of the Java classes and JARs being instrumented
    - build/libs/*
  commands: ^(?!org.brandonhaynes.pipegen).*              # Expression that identifies JVM command lines to consider for instrumentation
  classes: .*GradleWorkerMain					          # Expression that identifies JVM classes to consider for instrumentation
optimization:
  classPaths:									          # Java classes and JARs to consider during optimization
    - build/libs/myria-0.1.jar
    - build/libs/commons-csv-1.1.jar
datapipe:
  import:
    - ./gradlew -Dtest.single=FileScanTest cleanTest test # Script to execute during import data pipe creation
  export:
    - ./gradlew -Dtest.single=DataSinkTest cleanTest test # Script to execute during export data pipe creation
  verify:												  # Script to verify data pipe creation
    - unzip -o build/libs/myria-0.1.jar -d build/main
    - ./gradlew -Dtest.single=FileScanTest -x compileJava cleanTest test
    - ./gradlew -Dtest.single=DataSinkTest -x compileJava cleanTest test
```

The configuration file also supports the following optional parameters:

```YAML
backupPath: $TMP 								          # Location for temporary files during instrumentation and optimization
instrumentation:
  port: 7780									          # Instrumentation listener port
  timeout: 60									          # Maximum time for instrumentation to complete
  trace: $DIR/templates/Instrumentation.java 	          # Instrumentation harness file
  agent: $DIR/lib/btrace-agent.jar 				          # Trace agent JAR
  logPath: $TMP 								          # Log output location
  debug: false									          # When set, emits additional debugging information at runtime
datapipe:
  debug: false									          # When set, emits additional debugging information at runtime
```