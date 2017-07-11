package com.enot.cmd.core;

import org.apache.commons.io.FileUtils;
import org.cactoos.list.ArrayAsIterable;
import org.cactoos.list.ConcatIterable;
import org.cactoos.list.MappedIterable;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeoutException;

import static com.enot.cmd.core.LambdaListenerAdapter.*;

/**
 * Command line representation with the additional features around a process execution
 */
public class Cmd {
    private final boolean cleanUp;
    private final String outputFileName;
    private final Iterable<LambdaListenerAdapter> listeners;

    public Cmd() {
        this(false, "", new ArrayAsIterable<>());
    }

    public Cmd(boolean cleanUp, String outputFileName, Iterable<LambdaListenerAdapter> listeners) {
        this.cleanUp = cleanUp;
        this.outputFileName = outputFileName;
        this.listeners = listeners;
    }

    public Cmd listeners(LambdaListenerAdapter... listeners) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatIterable<>(
                        this.listeners,
                        new ArrayAsIterable<>(listeners)));
    }

    public Cmd configureExecutor(BeforeStart lambda) {
        return beforeStart(lambda);
    }

    public Cmd beforeStart(BeforeStart... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatIterable<>(
                        this.listeners,
                        new MappedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterStart(AfterStart... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatIterable<>(
                        this.listeners,
                        new MappedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterFinish(AfterFinish... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatIterable<>(
                        this.listeners,
                        new MappedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterStop(AfterStop... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatIterable<>(
                        this.listeners,
                        new MappedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    /**
     * Delete work directory after process stopped, only if it has been created during the execution
     *
     * @param cleanUp
     * @return
     */
    public Cmd cleanUp(boolean cleanUp) {
        return new Cmd(cleanUp, outputFileName, listeners);
    }

    public Cmd outputFileName(String outputFileName) {
        return new Cmd(cleanUp, outputFileName, listeners);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     * <p>
     * Note: Windows OS doesn't supported, use {@link #execute(String...)}} instead
     */
    public ProcessResult executeInShell(String script) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        return execute("sh", "-c", script);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     */
    public ProcessResult execute(String... command) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        return executor(command).execute();
    }

    /**
     * See {@link ProcessExecutor#executeNoTimeout()}
     */
    public ProcessResult executeNoTimeout(String... command) throws IOException, InterruptedException, InvalidExitValueException {
        return executor(command).executeNoTimeout();
    }

    /**
     * See {@link ProcessExecutor#start()}
     */
    public StartedProcess start(String... command) throws IOException {
        return executor(command).start();
    }

    private ProcessExecutor executor(final String ...command) throws IOException {
        final ProcessExecutor executor = new ProcessExecutor(command);
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }
        return executor.addListener(cmdListener(cleanUp, outputFileName));
    }

    private ProcessListener cmdListener(boolean cleanUp, String outputFileName) {
        return new ProcessListener() {
            @Override
            public void beforeStart(ProcessExecutor executor) {
                File dir = executor.getDirectory();
                if (dir != null && !dir.exists()) {
                    final boolean workDirCreated = dir.mkdirs();
                    if (!workDirCreated)
                        throw new UncheckedIOException(
                                new IOException(String.format("Work directory %s can not be created", dir.toPath())));
                    if (cleanUp && workDirCreated) {
                        executor.addListener(deleteDirAfterStop(dir));
                    }
                }
                if (outputFileName != null && outputFileName.length() > 0) {
                    executor.redirectOutputAlsoTo(createFileOS(dir)); //output stream will be closed by executor
                }
            }

            private OutputStream createFileOS(File workDir){
                Path outputFile;
                if (workDir != null) {
                    outputFile = Paths.get(workDir.getPath(), outputFileName);
                } else {
                    outputFile = Paths.get(outputFileName);
                }
                try {
                    return Files.newOutputStream(outputFile, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    throw new UncheckedIOException(
                            String.format("Output file %s can not be created", outputFile),
                            e);
                }
            }

            private ProcessListener deleteDirAfterStop(File dir) {
                return new ProcessListener() {
                    @Override
                    public void afterStop(Process process) {
                        try {
                            FileUtils.deleteDirectory(dir);
                        } catch (IOException e) {
                            throw new UncheckedIOException(
                                    String.format("Work directory %s can not be deleted", dir.toPath()),
                                    e);
                        }

                    }
                };
            }
        };
    }
}
