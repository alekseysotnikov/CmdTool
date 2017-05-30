package com.enot.cmd;


import com.enot.cmd.core.Cmd;
import com.enot.cmd.core.Exec;
import com.enot.cmd.ext.Listeners;
import com.enot.cmd.ext.listeners.ReadOutputs;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CmdTest {

    @Test
    public void executeCommand() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Path execDir = generateWorkDirPath();
        createDummyFile(execDir, uuid);

        Exec ls =
                new Exec("ls", ".")
                        .beforeStart(Listeners.readOutputs);
        String output =
                new Cmd(execDir, ls)
                        .execute()
                        .outputUTF8();
        assertEquals(uuid + "\n", output);
    }

    @Test
    public void oneLineCommand() throws Exception {
        Exec exec =
                new Exec("echo Hello world")
                        .beforeStart(Listeners.readOutputs);
        String output =
                new Cmd(generateWorkDirPath(), exec)
                        .execute()
                        .outputUTF8();
        assertEquals("Hello world\n", output);
    }

    @Test
    public void createExecDir() throws Exception {
        Path path = generateWorkDirPath();
        assertFalse(path.toFile().exists());
        new Cmd(path, "echo", "hello world")
                .execute();
        assertTrue(path.toFile().exists());
    }

    @Test
    public void deleteExecDir() throws Exception {
        Path execDir = generateWorkDirPath();
        createDummyFile(execDir, UUID.randomUUID().toString());

        new Cmd(execDir, "echo", "hello world")
                .deleteExecDir(true)
                .execute();
        assertFalse(execDir.toFile().exists());
    }

    @Test
    public void deleteEmptyExecDir() throws Exception {
        Path execDir = generateWorkDirPath();
        new Cmd(execDir, "echo", "hello world")
                .deleteEmptyExecDir(true)
                .execute();
        assertFalse(execDir.toFile().exists());
    }

    @Test
    public void outputFile() throws Exception {
        Path execDir = generateWorkDirPath();
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

    private Path generateWorkDirPath() {
        return Paths.get("./target/", UUID.randomUUID().toString());
    }
}
