package com.enot.cmd.core;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import static com.enot.cmd.core.LambdaListenerAdapter.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;

/**
 * Command line representation with the additional features around a process execution
 */
public class Cmd {
    private static final Logger LOG = LoggerFactory.getLogger(Cmd.class.getName());

    private final Path path;
    private final Exec exec;
    private final boolean deleteEmptyExecDir;
    private final boolean deleteExecDir;
    private final String outputFileName;

    public Cmd(Exec exec) {
        this(Paths.get("./"), exec);
    }

    public Cmd(Path path, Exec exec) {
        this(path, exec, false, false, "");
    }

    public Cmd(Path path, Exec exec, boolean deleteEmptyExecDir, boolean deleteExecDir, String outputFileName) {
        this.path = path;
        this.exec = exec;
        this.deleteEmptyExecDir = deleteEmptyExecDir;
        this.deleteExecDir = deleteExecDir;
        this.outputFileName = outputFileName;
    }

    /**
     * Provide execution directory path
     *
     * @param path execution directory path
     * @return {@link Cmd}
     */
    public Cmd path(Path path) {
        return new Cmd(path, exec, deleteEmptyExecDir, deleteExecDir, outputFileName);
    }

    public Cmd deleteEmptyExecDir(boolean deleteEmptyDir) {
        return new Cmd(path, exec, deleteEmptyDir, deleteExecDir, outputFileName);
    }

    public Cmd deleteExecDir(boolean deleteExecDir) {
        return new Cmd(path, exec, deleteEmptyExecDir, deleteExecDir, outputFileName);
    }

    public Cmd outputFileName(String outputFileName) {
        return new Cmd(path, exec, deleteEmptyExecDir, deleteExecDir, outputFileName);
    }

    /**
     * Execute command.
     * Before execution: creates execution directory if it does not exists.
     * After execution: if deleteEmptyExecDir=true, it will delete execution directory.
     *
     * @return {@link ProcessResult}
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public ProcessResult execute() throws IOException, TimeoutException, InterruptedException {
        File dir = path.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Can not create execution dir by path: " + path.toString());
        }
        ProcessResult result;
        final boolean isSaveOutputToFile = !deleteExecDir && !Strings.isNullOrEmpty(outputFileName);
        try (OutputStream fileOutput = (isSaveOutputToFile ? Files.newOutputStream(Paths.get(path.toString(), outputFileName), StandardOpenOption.CREATE) : null)) {
            BeforeStart beforeStart = e -> {
                if (isSaveOutputToFile) {
                    e.redirectOutputAlsoTo(fileOutput);
                }

                e.directory(dir);
            };

            AfterStop afterStop = p -> {
                if (this.deleteExecDir) {
                    try {
                        FileUtils.deleteDirectory(path.toFile());
                    } catch (IOException e) {
                        LOG.debug(e.getMessage(), e);
                    }
                } else if (deleteEmptyExecDir && path.toFile().delete()) {
                    LOG.debug("Execution directory [%s] has not been deleted, because either not empty or by other reasons");
                }
            };

            result = exec
                    .beforeStart(beforeStart)
                    .afterStop(afterStop)
                    .executor()
                    .execute();
        }
        return result;
    }
}
