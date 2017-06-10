package com.enot.cmd.core;

import org.apache.commons.io.FileUtils;
import org.cactoos.list.ArrayAsIterable;
import org.cactoos.list.ConcatenatedIterable;
import org.cactoos.list.TransformedIterable;
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
import java.util.List;
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
                new ConcatenatedIterable<>(
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
                new ConcatenatedIterable<>(
                        this.listeners,
                        new TransformedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterStart(AfterStart... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatenatedIterable<>(
                        this.listeners,
                        new TransformedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterFinish(AfterFinish... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatenatedIterable<>(
                        this.listeners,
                        new TransformedIterable<>(
                                new ArrayAsIterable<>(lambdas),
                                LambdaListenerAdapter::new)));
    }

    public Cmd afterStop(AfterStop... lambdas) {
        return new Cmd(
                cleanUp,
                outputFileName,
                new ConcatenatedIterable<>(
                        this.listeners,
                        new TransformedIterable<>(
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
        return createExecutor(command).execute();
    }

    /**
     * See {@link ProcessExecutor#executeNoTimeout()}
     */
    public ProcessResult executeNoTimeout(String... command) throws IOException, InterruptedException, InvalidExitValueException {
        return createExecutor(command).executeNoTimeout();
    }

    /**
     * See {@link ProcessExecutor#start()}
     */
    public StartedProcess start(String... command) throws IOException {
        return createExecutor(command).start();
    }

    private ProcessExecutor createExecutor(final String ...command) throws IOException {
        final ProcessExecutor executor = new ProcessExecutor(command);
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }
        executor.addListener(new ProcessListener() {
            @Override
            public void beforeStart(ProcessExecutor executor) {
                File dir = executor.getDirectory();
                if (dir != null && !dir.exists()) {
                    final boolean workDirCreated = dir.mkdirs();
                    if (!workDirCreated)
                        throw new UncheckedIOException(
                                new IOException(String.format("Work directory %s can not be created", dir.toPath())));
                    if (cleanUp && workDirCreated) {
                        executor.addListener(new ProcessListener() {
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
                        });
                    }
                }
                if (outputFileName != null && outputFileName.length() > 0) {
                    Path outputFile;
                    if (dir != null) {
                        outputFile = Paths.get(dir.getPath(), outputFileName);
                    } else {
                        outputFile = Paths.get(outputFileName);
                    }

                    OutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE);
                        executor.redirectOutputAlsoTo(fileOutputStream); //output stream will be closed by executor
                    } catch (IOException e) {
                        throw new UncheckedIOException(
                                String.format("Output file %s can not be created", outputFile),
                                e);
                    }
                }
            }
        });

        return executor;
    }
}
