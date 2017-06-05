package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.zeroturnaround.exec.ProcessExecutor;
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
    public void executeCommand() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Path execDir = generateRandomPath();
        createDummyFile(execDir, uuid);

        String output = new Cmd(new ProcessExecutor("ls", ".")
                .readOutput(true)
                .directory(execDir.toFile()))
                .execute()
                .outputUTF8();
        assertEquals(uuid + "\n", output);
    }

    @Test
    public void createExecDir() throws Exception {
        Path path = generateRandomPath();
        assertFalse(path.toFile().exists());

        new Cmd(new ProcessExecutor("echo", "hello world")
                .directory(path.toFile()))
                .execute();

        assertTrue(path.toFile().exists());
    }

    @Test
    public void deleteExecDir() throws Exception {
        Path execDir = generateRandomPath();
        createDummyFile(execDir, UUID.randomUUID().toString());

        new Cmd(new ProcessExecutor("echo", "hello world")
                .directory(execDir.toFile()))
                .deleteExecDir(true)
                .execute();
        assertFalse(execDir.toFile().exists());
    }

    @Test
    public void outputFile() throws Exception {
        Path execDir = generateRandomPath();
        String outputFileName = "test.output";
        new Cmd(new ProcessExecutor("echo", "hello world")
                .directory(execDir.toFile()))
                .outputFileName(outputFileName)
                .execute();
        assertTrue(Paths.get(execDir.toString(), outputFileName).toFile().exists());
    }

    private void createDummyFile(Path path, String fileName) throws IOException {
        path.toFile().mkdir();
        File dummyFile = Paths.get(path.toString(), fileName).toFile();
        dummyFile.createNewFile();
        assertTrue(dummyFile.exists());
    }

    @Test
    public void readOutput() throws IOException, InterruptedException, TimeoutException {
        final String str = "Hello";
        String result =
                new Cmd(new ProcessExecutor("echo", str)
                        .readOutput(true))
                        .execute()
                        .outputUTF8();

        assertEquals(str + "\n", result);
    }

    @Test
    public void beforeStartListener() throws IOException, InterruptedException, TimeoutException {
        ArrayList<String> lines = Lists.newArrayList();
        final String arg = "line1";
        new Cmd("echo", arg)
                .beforeStart(e -> e.redirectOutputAlsoTo(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        lines.add(line);
                    }
                }))
                .execute();

        assertEquals(1, lines.size());
        assertEquals(arg, lines.get(0));
    }

    @Test
    public void runScript() throws InterruptedException, TimeoutException, IOException {
        Assert.assertEquals("Hello\n",
                new Cmd(new ProcessExecutor("s='Hello'; echo $s;")
                        .readOutput(true))
                        .script(true)
                        .execute()
                        .outputUTF8());
    }

    private Path generateRandomPath() {
        return Paths.get("./target/", UUID.randomUUID().toString());
    }
}
