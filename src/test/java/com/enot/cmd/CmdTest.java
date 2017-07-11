package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import org.junit.Assert;
import org.junit.Test;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class CmdTest {
    @Test
    public void execute() throws Exception {
        Assert.assertEquals("Hello\n",
                new Cmd(e -> e.readOutput(true))
                        .executing()
                        .execute("echo", "Hello")
                        .outputUTF8());
    }

    @Test
    public void executeNoTimeout() throws Exception {
        Assert.assertEquals("Hello\n",
                new Cmd(e -> e.readOutput(true))
                        .executing()
                        .executeNoTimeout("echo", "Hello")
                        .outputUTF8());
    }

    @Test
    public void start() throws Exception {
        Assert.assertEquals("Hello\n",
                new Cmd(e -> e.readOutput(true))
                        .executing()
                        .start("echo", "Hello")
                        .getFuture()
                        .get()
                        .outputUTF8());
    }

    @Test
    public void executeScript() throws Exception {
        Assert.assertEquals("Hello\n",
                new Cmd(e -> e.readOutput(true))
                        .executing()
                        .executeInShell("s='Hello'; echo $s;")
                        .outputUTF8());
    }

    @Test
    public void createWorkDir() throws Exception {
        File workDir = generateRandomPath().toFile();

        assertFalse(workDir.exists());

        new Cmd(e -> e.directory(workDir))
                .executing()
                .execute("echo", "hello world");

        assertTrue(workDir.exists());
    }

    @Test
    public void cleanUp() throws Exception {
        File workDir = generateRandomPath().toFile();

        assertFalse(workDir.exists());

        new Cmd(e -> e.directory(workDir))
                .cleanUp(true)
                .executing()
                .execute("echo", "hello world");

        assertFalse("Directory should be deleted", workDir.exists());
    }

    @Test
    public void outputFile() throws Exception {
        Path execDir = generateRandomPath();
        String outputFileName = "test.output";
        new Cmd(e -> e.directory(execDir.toFile()))
                .outputFileName(outputFileName)
                .executing()
                .execute("echo", "hello world");
        assertTrue("Output file should be exists", Paths.get(execDir.toString(), outputFileName).toFile().exists());
    }

    @Test
    public void beforeStartListener() throws IOException, InterruptedException, TimeoutException {
        ArrayList<String> lines = new ArrayList<>();
        final String arg = "line1";
        new Cmd()
                .listening()
                .beforeStart(e -> e.redirectOutputAlsoTo(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        lines.add(line);
                    }
                }))
                .back()
                .executing()
                .execute("echo", arg);

        assertEquals(1, lines.size());
        assertEquals(arg, lines.get(0));
    }

    private Path generateRandomPath() {
        return Paths.get("./target/", UUID.randomUUID().toString());
    }
}
