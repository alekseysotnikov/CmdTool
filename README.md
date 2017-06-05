# CmdTool [![Build Status](https://travis-ci.org/alekseysotnikov/CmdTool.svg?branch=master)](https://travis-ci.org/alekseysotnikov/CmdTool) [![codecov](https://codecov.io/gh/alekseysotnikov/CmdTool/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/CmdTool)
Tiny, pure object-oriented and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around process execution. All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are still available for usage along with CmdTool.

Java 8+ required.

**Note**: it is a very early alpha version, the API may change.

### Motivation
When we call external programs from Java, we certainly need to harvest the output files and/or output stream. It is ok, but what if we have thousands of calls? Some of them produces files we need, but another one not. So, we have to make a cleanup.

This library solves this small problem and intended to process each command call inside a separate directory. It performs particular activities with the directory, such as creating or deleting on appropriate stages of execution (before/after start, after finish and after stop process). 

### Features
- Execute command and script
- Save output stream of the process into a file
- Create or delete execution directory before or after execution

### Examples
> Execute command
````java
String output = new Cmd(new ProcessExecutor("s='Hello'; echo $s;")
                     .readOutput(true))
                     .script(true)
                     .execute()
                     .outputUTF8()
System.out.println(output);
````
```
output> Hello
```
> Execute script in Shell
````java
String output = new Cmd(new ProcessExecutor("s='Hello'; echo $s;")
                     .readOutput(true))
                     .script(true)
                     .execute()
                     .outputUTF8();
System.out.println(output);
````
```
output> Hello
```
> Save output stream of a process into a file, even if the process stopped unexpectedly
```java
new Cmd(new ProcessExecutor("echo", "Hello")
                .directory(new File("./")))
                .outputFileName("output.txt")
                .execute();

        File file = new File("./", "output.txt");
        System.out.println(Files.readFirstLine(file, Charset.defaultCharset())); 
```
```
output> Hello
```