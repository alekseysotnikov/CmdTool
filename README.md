# CmdTool 
### Continuous Integration
[![Build Status](https://travis-ci.org/alekseysotnikov/CmdTool.svg?branch=master)](https://travis-ci.org/alekseysotnikov/CmdTool) [![codecov](https://codecov.io/gh/alekseysotnikov/CmdTool/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/CmdTool)

### Quick Overview
Tiny, pure object-oriented and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around a process execution. All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are still available for usage along with CmdTool.

Java 8+ required.

**Note**: it is a very early alpha version, the API may change.

### Motivation
When we call external programs from Java, we certainly need to harvest the output files and output stream. It is ok, but what if we have thousands of calls? They will pollute a disk space if some of them produce files we don't need. 
So, we have to do a cleanup of disk space if files don't need anymore, just like Java GC frees RAM automatically.

This library solves this small problem and intended to call each command inside a separate directory. It performs particular activities with the directory, such as creating or deleting on appropriate stages of execution (before/after start, after finish and after stop process). 

### Features
- Execute command or script
- Save output stream of the process into a file
- Create or delete execution directory before or after execution

### Examples
> Execute command
````java
new Cmd().execute("echo", "Hello");
````
> Execute script in a Shell
````java
new Cmd().executeInShell("s='Hello'; echo $s;");
````
> Execute script and read output
````java
String output = new Cmd()
                     .executeInShell(new ProcessExecutor("s='Hello'; echo $s;")
                                          .readOutput(true))
                     .outputUTF8();
System.out.println(output);

// output> Hello
````
> Save output stream into a file, even if the process stopped unexpectedly
```java
new Cmd()
      .outputFileName("output.txt")
      .execute("echo", "Hello");
````
> Create execution directory before start and delete after finish
````java
File execDir = new File("./", "foo");
String outputFileName = "output.txt";
new Cmd()
        .cleanUp(true)
        .outputFileName(outputFileName)
        .afterStop(process -> {
            //execution directory and process result is ready for usage here, and not deleted yet
            File outputFile = new File(execDir, outputFileName);
            System.out.println(outputFile.exists()); //true
        })
        .execute(new ProcessExecutor("echo", "hello world")
                         .readOutput(true)
                         .directory(execDir));
//execution directory doesn't exists here
System.out.println(execDir.exists()); //false

//output> true
//output> false
````
