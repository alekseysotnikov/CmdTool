package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import org.junit.Test;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class CmdTest {
    @Test
    public void interpreterCommandLine() {
        assertThat(
                new Cmd()
                        .interpreter("sh")
                        .command("-c", "echo Hello;")
                        .commandLine(),
                is(Arrays.asList("sh", "-c", "echo Hello;")));
    }

    @Test
    public void execute() throws Exception {
        assertThat(
                new Cmd()
                        .configuring(e -> e.readOutput(true))
                        .command("echo", "Hello")
                        .execute()
                        .outputUTF8(),
                is("Hello\n"));
    }

    @Test
    public void executeNoTimeout() throws Exception {
        assertThat(
                new Cmd()
                        .configuring(e -> e.readOutput(true))
                        .command("echo", "Hello")
                        .executeNoTimeout()
                        .outputUTF8(),
                is("Hello\n"));
    }

    @Test
    public void start() throws Exception {
        assertThat(
                new Cmd()
                        .configuring(e -> e.readOutput(true))
                        .command("echo", "Hello")
                        .start()
                        .getFuture()
                        .get()
                        .outputUTF8(),
                is("Hello\n"));
    }

    @Test
    public void executeScript() throws Exception {
        assertThat(
                new Cmd()
                        .configuring(e -> e.readOutput(true))
                        .interpreter("sh")
                        .command("-c", "s='Hello'; echo $s;")
                        .execute()
                        .outputUTF8(),
                is("Hello\n"));
    }

    @Test
    public void createWorkDir() throws Exception {
        final File workDir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(workDir.exists())),
                is(0 == new Cmd()
                        .configuring(e -> e.directory(workDir))
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(workDir.exists())
        ));
    }

    @Test
    public void cleanUp() throws Exception {
        final File workDir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(workDir.exists())),
                is(0 == new Cmd()
                        .configuring(e -> e.directory(workDir))
                        .cleanUp(true)
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(not(workDir.exists()))
        ));
    }

    @Test
    public void outputFile() throws Exception {
        final String outputFileName = "test.output";
        final File workDir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(workDir.exists())),
                is(0 == new Cmd()
                        .configuring(e -> e.directory(workDir))
                        .outputFileName(outputFileName)
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(new File(workDir, outputFileName).exists())
        ));
    }

    @Test
    public void beforeStartListener() throws IOException, InterruptedException, TimeoutException {
        final ArrayList<String> lines = new ArrayList<>();
        final String message = "line1";
        assertThat(true, allOf(
                is(0 == new Cmd()
                        .listening()
                        .beforeStart(e -> e.redirectOutputAlsoTo(new LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                lines.add(line);
                            }
                        }))
                        .back()
                        .command("echo", message)
                        .execute()
                        .getExitValue()),
                is(1 == lines.size()),
                is(message.equals(lines.get(0)))
        ));
    }

    private Path generateRandomPath() {
        return Paths.get("./target/", UUID.randomUUID().toString());
    }
}
