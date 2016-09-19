# PipeGen: Data Pipe Generation for Hybrid Analytics

![PipeGen Build Status](https://travis-ci.org/uwdb/pipegen.svg?branch=master "Build Status")

PipeGen allows users to automatically create an efficient connection between pairs of database systems. PipeGen targets data analytics workloads on shared-nothing engines, and supports scenarios where users seek to perform different parts of an analysis in different DBMSs or want to combine and analyze data stored in different systems. The systems may be colocated in the same cluster or may be in different clusters.

This is the core repository for the PipeGen tool, which may to create an optimized data transfer connector between a pair of Java database systems.  For a higher-level overview, visit the [project website](http://db.cs.washington.edu/projects/pipegen) or [read the paper](https://arxiv.org/pdf/1605.01664v2.pdf).

## Configuring Data Pipe Creation

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

## Creating a Data Pipe

To create a data pipe in a new database system, execute PipeGen as follows:

```sh
$ java -jar target/pipegen-0.1.jar [configuration YAML]
```

PipeGen will create an optimized data pipe using the following phases:

### 1. IO Redirection Phase (IORedirect)

First, PipeGen executes the unit tests provided in the verification section of the configuration file and identifies file IO operations.  It uses the result of this instrumentation to modify the bytecode to support transfer to and from a remote DBMS when a _reserved filename_ is specified.  See Runtime Configuration for details regarding the format of this filename.

### 2. Verification Phase (Existing Functionality)

Once PipeGen has modified the DBMS to support an initial data pipe, it executes the verification script to ensure that the associated unit tests continue to pass after the bytecode modifications.

### 3. Verification Phase (New Functionality)

Next, PipeGen tests the new functionality introduced into the DBMS during the IORedirect phase.  It does this by first activating a _debugging proxy_.  This proxy acts like a remote, data pipe-enabled DBMS, but reads and writes directly to and from the underlying file system.  PipeGen then activates a special mode that transmits _all_ import and export data across the new data pipe.  Finally, PipeGen executes the verification script and ensures that the unit tests pass.

### 4. Optimization Phase (FormOpt)

In this phase, PipeGen optimizes the new data pipe.  It begins by instrumenting the bytecode of the data pipes to locate import and export IO operations.  It then performs data flow analysis to identify the sources and uses of primitive values that are (eventually) converted to and from string form during the import and export process.  It then applies decorates the strings (and string-handling classes) with a special augmented type that avoids conversion and concatenation overhead.  PipeGen also examines the import and export operations for use of common IO libraries and replaces each with version optimized for transmission to a remote system.

### 5. FormOpt Verification Phase (Optimized Functionality)

Finally, PipeGen executes the verification script using the optimized data pipe and debugging proxy to ensure that unit tests continue to pass.

## Launching the Worker Directory

Data pipes rely on a common _worker directory_ to identify peers and connect individual workers or partitions.  This directory must be active and accessible by each participating DBMS prior to transmitting or receiving data.  To launch the worker directory, execute the following command:

```sh
bin/directory-server.sh
```

The worker directory listens on the host and port defined in the runtime configuration described below.


## Using a Data Pipe

Once PipeGen has added a data pipe to a database system, a user may import and export data from and to a remote system by specifying a filename that matches the _reserved filename_ format.  By default, this filename is of the form `__dbms__[name]`, where `[name]` is the name of the remote DBMS producing or consuming data.  The exact filename format may be specified in the PipeGen runtime configuration file located at `/etc/pipegen/pipegen.yaml`.  This configuration file must be readable by each DBMS that uses the data pipe.  

For example, in Spark we would transmit a RDD to a remote DBMS named `foo` by executing the following query:

```scala
rdd.saveAsTextFile("__dbms__foo")
```

The PipeGen runtime configuration supports the following options:


```YAML
filenames:
  import: __dbms__(?<name>.+)    # Reserved filename format for import; name identifies the exporting DBMS
  export: __dbms__(?<name>.+)    # Reserved filename format for export; name identifies the importing DBMS
directory: http://localhost:8888 # Host and port for the worker directory
optimization:
  varchar-size: 1024             # Maximum size of varchar elements transmitted over a data pipe
  vector-size:  4096             # Size of vector transmitted over a data pipe
  allocation:   1024             # Initial vector allocation 
timeout: 50                      # Time to wait for IO activity before disconnecting a data pipe (in seconds)
```