package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import com.enot.cmd.core.Listening;
import com.enot.cmd.listeners.CleanUp;
import com.enot.cmd.listeners.RedirectToFile;
import com.enot.cmd.listeners.RedirectTo;
import com.enot.cmd.listeners.WorkDir;
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
import static org.junit.Assert.assertTrue;

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
        final File dir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(dir.exists())),
                is(0 == new Cmd()
                        .configuring(new WorkDir(dir))
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(dir.exists())
        ));
    }

    @Test
    public void cleanUp() throws Exception {
        final File workDir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(workDir.exists())),
                is(0 == new Cmd()
                        .configuring(
                                new WorkDir(workDir),
                                new CleanUp()
                        ).listening((Listening.AfterStop) process -> {
                            assertTrue("Work directory has to be exist for listeners", workDir.exists());
                        })
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(not(workDir.exists()))
        ));
    }

    @Test
    public void outputFile() throws Exception {
        final String outputPath = "./test.output";
        final File workDir = generateRandomPath().toFile();
        assertThat(true, allOf(
                is(not(workDir.exists())),
                is(0 == new Cmd()
                        .configuring(
                                new WorkDir(workDir),
                                new RedirectToFile(outputPath)
                        )
                        .command("echo", "hello world")
                        .execute()
                        .getExitValue()),
                is(new File(workDir, outputPath).exists())
        ));
    }

    @Test
    public void beforeStartListener() throws IOException, InterruptedException, TimeoutException {
        final ArrayList<String> lines = new ArrayList<>();
        final String message = "line1";
        assertThat(true, allOf(
                is(0 == new Cmd()
                        .listening(new RedirectTo(new LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                lines.add(line);
                            }
                        }))
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
