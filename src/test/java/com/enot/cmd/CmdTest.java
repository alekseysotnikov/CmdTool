package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import com.enot.cmd.core.Exec;
import com.enot.cmd.ext.Listeners;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

public class CmdTest {

    @Test
    public void executeCommand() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Path execDir = createWorkDir();
        createDummyFile(execDir, uuid);

        String output = new Cmd(execDir, new Exec("ls", ".")
                .beforeStart(Listeners.readOutputs))
                .execute()
                .outputUTF8();
        assertEquals(uuid + "\n", output);
    }

    @Test
    public void createExecDir() throws Exception {
        Path path = createWorkDir();
        assertFalse(path.toFile().exists());
        new Cmd(path, "echo", "hello world")
                .execute();
        assertTrue(path.toFile().exists());
        Path execPath = Paths.get("./", UUID.randomUUID().toString());
        new Cmd(execPath, "echo", "Hello")
                .deleteEmptyExecDir(true)
                .execute();
    }

    @Test
    public void deleteExecDir() throws Exception {
        Path execDir = createWorkDir();
        createDummyFile(execDir, UUID.randomUUID().toString());

        new Cmd(execDir, "echo", "hello world")
                .deleteExecDir(true)
                .execute();
        assertFalse(execDir.toFile().exists());
    }

    @Test
    public void deleteEmptyExecDir() throws Exception {
        Path execDir = createWorkDir();
        new Cmd(execDir, "echo", "Hello")
                .deleteEmptyExecDir(true)
                .execute();
        assertFalse(execDir.toFile().exists());
    }

    @Test
    public void outputFile() throws Exception {
        Path execDir = createWorkDir();
        String outputFileName = "test.output";
        new Cmd(execDir, "echo", "hello world")
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

    private Path createWorkDir() {
        return Paths.get("./target/", UUID.randomUUID().toString());
    }
}
