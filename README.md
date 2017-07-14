# CmdTool 
### Continuous Integration
[![Build Status](https://travis-ci.org/alekseysotnikov/CmdTool.svg?branch=master)](https://travis-ci.org/alekseysotnikov/CmdTool) [![codecov](https://codecov.io/gh/alekseysotnikov/CmdTool/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/CmdTool)

### Quick Overview
Tiny, pure object-oriented, declarative and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around a process execution. All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are still available for usage along with CmdTool.

Java 8+ required.

**Note**: it is a very early alpha version, the API may change.

### Motivation
When we call external programs from Java, we certainly need to harvest the output files and output stream. It is ok, but what if we have thousands of calls? They will pollute a disk space if some of them produce files we don't need. 
So, we have to do a clean up of disk space if files don't need anymore, just like Java GC frees RAM automatically.

This library solves this small problem and intended to call each command inside a separate directory. It performs particular activities with the directory, such as creating or deleting on appropriate stages of execution (before/after start, after the finish and after stop process). 

### Features
- Execute command or script
- Save output stream of the process into a file
- Create work directory automatically
- Clean up disk space from a produced data automatically

### Download
1. Get the [latest version here](https://github.com/alekseysotnikov/CmdTool/releases) with or without dependencies
2. Dependencies
````xml
<dependency>
  <groupId>org.zeroturnaround</groupId>
  <artifactId>zt-exec</artifactId>
  <version>1.9</version>
</dependency>

<dependency>
  <groupId>org.cactoos</groupId>
  <artifactId>cactoos</artifactId>
  <version>0.11</version>
</dependency>
````
### Examples
> Execute command
````java
new Cmd().command("echo", "Hello").execute();
````
> Execute script in a Shell
````java
new Cmd().script("s='Hello'; echo $s;").execute();
````
> Execute script and read output
````java
String output = new Cmd()
                     .configuring(e -> e.readOutput(true))
                     .script("s='Hello'; echo $s;").execute()
                     .outputUTF8();
System.out.println(output);

// output> Hello
````
> Save output stream into a file, even if the process stopped unexpectedly
```java
new Cmd()
      .outputFileName("output.txt")
      .command("echo", "Hello").execute();
````
> Create work directory before start and delete after finish
````java
new Cmd()
        .configuring(e -> e.directory(new File("./", "foo"))) // specify work directory ./foo
        .cleanUp(true)
        .listening().afterStop(process -> {
            //work directory ./foo will be exists here
        }).back()
        .command("echo", "hello world").execute(); // work directory ./foo will be created automatically
//work directory ./foo was deleted after execution
````
> Run in a background
````java
StartedProcess startedProcess = new Cmd().command("echo", "Hello").start();
startedProcess.getFuture().get(); //wait result
````
