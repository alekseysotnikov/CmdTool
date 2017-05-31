# CmdTool
Tiny, pure object-oriented and immutable wrapper of [zt-exec](https://github.com/zeroturnaround/zt-exec) with additional features around process execution. All features of [zt-exec](https://github.com/zeroturnaround/zt-exec) are still available for usage along with CmdTool.

Java 8+ required.

### Features
- Save text output of the process into a file
- Create or delete execution directory before or after process execution

### Examples
> echo Hello
```java
String output = new Cmd("echo", "Hello")
                        .execute()
                        .outputUTF8();
System.out.print(output); //Hello
```
> Save text output of the process into a file, even if the process stopped during the execution
```java
new Cmd("./", "echo", "Hello")
        .outputFileName("output.txt")
        .execute();

File file = new File("./", "output.txt");
System.out.println(Files.readFirstLine(file, Charset.defaultCharset())); // Hello
```

### TODO
- add test coverage metrics
- make reasonable percentage code coverage by unit tests 
- configure maven build phase 
- configure Travis CI
