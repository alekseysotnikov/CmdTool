# CmdTool 
### Continuous Integration
[![Build Status](https://travis-ci.org/alekseysotnikov/CmdTool.svg?branch=master)](https://travis-ci.org/alekseysotnikov/CmdTool) [![codecov](https://codecov.io/gh/alekseysotnikov/CmdTool/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/CmdTool)

### Quick Overview
Tiny, pure object-oriented, declarative and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around a process execution.

Java 8+ required.

### Motivation
When we call external programs from Java, we certainly need to harvest the output files and output stream. It is ok, but what if we have thousands of calls? They will pollute a disk space if some of them produce files we don't need. 
So, we have to do a clean up of disk space if the files don't need anymore, just like Java GC frees a RAM automatically.

### Features
All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are supported plus the following:
- Execute command or script
- Save output stream of the process into a file
- Create and clean up work directory automatically

### Download
1. Get the [latest version here](https://github.com/alekseysotnikov/CmdTool/releases) with or without dependencies
2. Dependencies
````xml
<dependency>
  <groupId>org.zeroturnaround</groupId>
  <artifactId>zt-exec</artifactId>
  <version>1.10</version>
</dependency>

<dependency>
  <groupId>org.cactoos</groupId>
  <artifactId>cactoos</artifactId>
  <version>0.11.10</version>
</dependency>
````
### Examples
> Execute command
````java
new Cmd().command("echo", "Hello").execute();
````
> Execute script in a Shell (Unix-like, Mac OS)
````java
new Cmd()
       .interpreter("sh") // specify command interpreter
       .command("-c", "s='Hello'; echo $s;").execute();
````
or even shorter
````java
new Cmd().command("sh", "-c", "s='Hello'; echo $s;").execute();
````
> ... and read output 
````java
String output = new Cmd()
                     .configuring(c -> c.readOutput(true)) // configure zt-exec's executor
                     .interpreter("sh")
                     .command("-c", "s='Hello'; echo $s;").execute()
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
>  Execute command inside a separate work directory. It creates work directory before start and delete after finish
````java
new Cmd()
        .configuring(c -> c.directory(new File("./", "foo"))) // specify work directory ./foo
        .cleanUp(true) // delete work directory after process stopped, only if the directory will be created during the execution
        .listening().afterStop(process -> {
            //work directory ./foo will be exist here
        }).back()
        .command("echo", "hello world").execute(); // work directory ./foo will be created automatically
//work directory ./foo was deleted after execution
````
> Run command in a background
````java
StartedProcess startedProcess = new Cmd().command("echo", "Hello").start();
startedProcess.getFuture().get(); //wait result
````
