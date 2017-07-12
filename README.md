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

### Examples
> Execute command
````java
new Cmd().executing().execute("echo", "Hello");
````
> Execute script in a Shell
````java
new Cmd().executing().executeInShell("s='Hello'; echo $s;");
````
> Execute script and read output
````java
String output = new Cmd(e -> e.readOutput(true))
                     .executing().executeInShell("s='Hello'; echo $s;")
                     .outputUTF8();
System.out.println(output);

// output> Hello
````
> Save output stream into a file, even if the process stopped unexpectedly
```java
new Cmd()
      .outputFileName("output.txt")
      .executing().execute("echo", "Hello");
````
> Create work directory before start and delete after finish
````java
new Cmd()
        .listening()
        .beforeStart(e -> e.directory(new File("./", "foo"))) // specify work directory ./foo
        .afterStop(process -> {
            //work directory ./foo will be exists here
        })
        .back()
        .cleanUp(true)
        .executing().execute("echo", "hello world"); // work directory ./foo will be created automatically
//work directory ./foo was deleted after execution
````
> Run in a background
````java
StartedProcess startedProcess = new Cmd().executing().start("echo", "Hello");
startedProcess.getFuture().get(); //wait result
````
