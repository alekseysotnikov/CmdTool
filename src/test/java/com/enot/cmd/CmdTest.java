package com.enot.cmd;


import com.enot.cmd.core.Cmd;
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

        String output = new Cmd(execDir,"ls", ".")
                .execute()
                .outputUTF8();
        assertEquals(uuid + "\n", output);
    }

    @Test
    public void oneLineCommand() throws Exception {
        String output = new Cmd(generateWorkDirPath(), "echo Hello world")
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
        String fileName = UUID.randomUUID().toString();
        createDummyFile(execDir, fileName);

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
