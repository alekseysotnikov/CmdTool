# CmdTool [![Build Status](https://travis-ci.org/alekseysotnikov/CmdTool.svg?branch=master)](https://travis-ci.org/alekseysotnikov/CmdTool) [![codecov](https://codecov.io/gh/alekseysotnikov/CmdTool/branch/master/graph/badge.svg)](https://codecov.io/gh/alekseysotnikov/CmdTool)
Tiny, pure object-oriented and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around process execution. All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are still available for usage along with CmdTool.

Java 8+ required.

### Motivation
When we call external programs from Java, we certainly need to harvest the output files and/or output stream. It is ok, but what if we have thousands of calls? Some of them produces files we need, but another one not. So, we have to make a cleanup.

This library solves this small problem and intended to process each program call inside a separate directory. It performs particular activities with the directory, such as creating or deleting on appropriate stages of execution (before/after start, after finish and after stop process). 

### Features
- Create or delete execution directory before or after process execution
- Save text output of the process into a file
- Execute script or command in Shell

### Examples
> Execute command
````java
String output = new Exec("echo", "Hello")
                .beforeStart(e -> e.readOutput(true))
                .executor()
                .execute()
                .outputUTF8();
//> Hello
````
> Execute script in Shell
````java
String output = new Script("s='Hello'; echo $s;")
                    .toExec()
                    .beforeStart(e -> e.readOutput(true))
                    .executor()
                    .execute()
                    .outputUTF8(); 
//> Hello
````
> Save output stream of the process into a file, even if process stopped unexpectedly
```java
new Cmd(Paths.get("./"), "echo", "Hello")
        .outputFileName("output.txt")
        .execute();

File file = new File("./", "output.txt");
System.out.println(Files.readFirstLine(file, Charset.defaultCharset())); // Hello
```
> Delete empty execution directory after process finished 
````java
Path execPath = Paths.get("./", UUID.randomUUID().toString());
new Cmd(execPath, "echo", "Hello")
     .deleteEmptyExecDir(true)
     .execute();
````

### TODO
- parsing one line command with arguments
