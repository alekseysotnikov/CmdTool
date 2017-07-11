package com.enot.cmd.core;

import org.apache.commons.io.FileUtils;
import org.cactoos.list.ArrayAsIterable;
import org.cactoos.list.ConcatIterable;
import org.cactoos.list.MappedIterable;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Command line representation with the additional features around a process execution
 */
public final class Cmd implements ICmd {
    private final boolean cleanUp;
    private final String outputFileName;
    private final Listening listening;

    public Cmd() {
        this(false, "", new Listening(null, new ArrayAsIterable<>()));
    }

    public Cmd(LambdaListenerAdapter.BeforeStart configuring) {
        this(false, "", new Listening(null, new ArrayAsIterable<>(new LambdaListenerAdapter(configuring))));
    }

    public Cmd(boolean cleanUp, String outputFileName, Listening listening) {
        this.cleanUp = cleanUp;
        this.outputFileName = outputFileName;
        this.listening = listening;
    }

    /**
     * Delete work directory after process stopped, only if it has been created during the execution
     *
     * @param cleanUp
     * @return
     */
    @Override
    public Cmd cleanUp(boolean cleanUp) {
        return new Cmd(cleanUp, outputFileName, listening);
    }

    @Override
    public Cmd outputFileName(String outputFileName) {
        return new Cmd(cleanUp, outputFileName, listening);
    }

    @Override
    public CmdListening listening() {
        return new Listening(this, listening.listeners);
    }

    @Override
    public Executing executing() {
        return new BaseExecuting(processExecutor());
    }

    private ProcessExecutor processExecutor() {
        final ProcessExecutor executor = new ProcessExecutor();
        for (LambdaListenerAdapter listener : listening.listeners) {
            executor.addListener(listener);
        }
        return executor.addListener(baseListener(cleanUp, outputFileName));
    }

    private ProcessListener baseListener(boolean cleanUp, String outputFileName) {
        return new ProcessListener() {
            @Override
            public void beforeStart(ProcessExecutor executor) {
                File dir = executor.getDirectory();
                if (dir != null && !dir.exists()) {
                    final boolean workDirCreated = dir.mkdirs();
                    if (!workDirCreated)
                        throw new UncheckedIOException(
                                new IOException(String.format("Work directory %s can not be created", dir.toPath())));
                    if (cleanUp) {
                        executor.addListener(deleteDirAfterStop(dir));
                    }
                }
                if (outputFileName != null && outputFileName.length() > 0) {
                    executor.redirectOutputAlsoTo(createFileOS(dir)); //output stream will be closed by processExecutor
                }
            }

            private OutputStream createFileOS(File workDir) {
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

    private static final class Listening implements CmdListening {
        private final Cmd owner;
        private final Iterable<LambdaListenerAdapter> listeners;

        public Listening(Cmd owner, Iterable<LambdaListenerAdapter> listeners) {
            this.owner = owner;
            this.listeners = listeners;
        }

        @Override
        public CmdListening beforeStart(LambdaListenerAdapter.BeforeStart... lambdas) {
            return new Listening(
                    owner,
                    new ConcatIterable<>(
                            this.listeners,
                            new MappedIterable<>(
                                    new ArrayAsIterable<>(lambdas),
                                    LambdaListenerAdapter::new)));
        }

        @Override
        public CmdListening afterStart(LambdaListenerAdapter.AfterStart... lambdas) {
            return new Listening(
                    owner,
                    new ConcatIterable<>(
                            this.listeners,
                            new MappedIterable<>(
                                    new ArrayAsIterable<>(lambdas),
                                    LambdaListenerAdapter::new)));
        }

        @Override
        public CmdListening afterFinish(LambdaListenerAdapter.AfterFinish... lambdas) {
            return new Listening(
                    owner,
                    new ConcatIterable<>(
                            this.listeners,
                            new MappedIterable<>(
                                    new ArrayAsIterable<>(lambdas),
                                    LambdaListenerAdapter::new)));
        }

        @Override
        public CmdListening afterStop(LambdaListenerAdapter.AfterStop... lambdas) {
            return new Listening(
                    owner,
                    new ConcatIterable<>(
                            this.listeners,
                            new MappedIterable<>(
                                    new ArrayAsIterable<>(lambdas),
                                    LambdaListenerAdapter::new)));
        }

        @Override
        public Cmd back() {
            return new Cmd(owner.cleanUp, owner.outputFileName, this);
        }
    }
}
