package com.enot.cmd.core;

import org.apache.commons.io.FileUtils;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.zeroturnaround.exec.ProcessExecutor;

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
    private final LambdaListenerAdapter.BeforeStart configuring;
    private final String interpreter;

    public Cmd() {
        this(false,
                "",
                new Listening(null, new IterableOf<>()),
                e -> {
                },
                "");
    }

    public Cmd(boolean cleanUp, String outputFileName, Listening listening, LambdaListenerAdapter.BeforeStart configuring, String interpreter) {
        this.cleanUp = cleanUp;
        this.outputFileName = outputFileName;
        this.listening = listening;
        this.configuring = configuring;
        this.interpreter = interpreter;
    }

    /**
     * Delete work directory after process stopped, only if the directory will be created during the execution
     *
     * @param cleanUp
     * @return
     */
    @Override
    public Cmd cleanUp(boolean cleanUp) {
        return new Cmd(cleanUp, outputFileName, listening, configuring, interpreter);
    }

    @Override
    public Cmd outputFileName(String outputFileName) {
        return new Cmd(cleanUp, outputFileName, listening, configuring, interpreter);
    }

    @Override
    public Cmd configuring(LambdaListenerAdapter.BeforeStart configuring) {
        return new Cmd(cleanUp, outputFileName, listening, configuring, interpreter);
    }

    @Override
    public CmdListening listening() {
        return new Listening(this, listening.listeners);
    }

    @Override
    public Cmd interpreter(String interpreter) {
        return new Cmd(cleanUp, outputFileName, listening, configuring, interpreter);
    }

    public Command command(String... command) {
        return new BaseCommand(processExecutor(command));
    }

    private ProcessExecutor processExecutor(String... command) {
        ProcessExecutor executor = new ProcessExecutor();
        configuring.run(executor);
        for (LambdaListenerAdapter listener : listening.listeners) {
            executor.addListener(listener);
        }

        Iterable<String> commands = new IterableOf<>(command);
        if (interpreter != null && !interpreter.trim().isEmpty()) {
            commands = new Joined<>(new IterableOf<>(interpreter), commands);
        }

        LambdaListenerAdapter beforeStart = new LambdaListenerAdapter((LambdaListenerAdapter.BeforeStart) processExecutor -> {
            File dir = processExecutor.getDirectory();
            if (dir != null && !dir.exists()) {
                final boolean workDirCreated = dir.mkdirs();
                if (!workDirCreated)
                    throw new UncheckedIOException(
                            new IOException(String.format("Work directory %s can not be created", dir.toPath())));
                if (cleanUp) {
                    processExecutor.addListener(new LambdaListenerAdapter((LambdaListenerAdapter.AfterStop) p -> {
                        try {
                            FileUtils.deleteDirectory(dir);
                        } catch (IOException e) {
                            throw new UncheckedIOException(
                                    String.format("Work directory %s can not be deleted", dir.toPath()),
                                    e);
                        }
                    }));
                }
            }
            if (outputFileName != null && outputFileName.length() > 0) {
                OutputStream outputStream = createFileOS(dir);
                processExecutor.redirectOutputAlsoTo(outputStream);
                processExecutor.addListener(new LambdaListenerAdapter((LambdaListenerAdapter.AfterStop) process -> {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
            }
        });
        return executor.command(commands).addListener(beforeStart);
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
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterStart(LambdaListenerAdapter.AfterStart... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterFinish(LambdaListenerAdapter.AfterFinish... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterStop(LambdaListenerAdapter.AfterStop... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public Cmd back() {
            return new Cmd(owner.cleanUp, owner.outputFileName, this, owner.configuring, owner.interpreter);
        }
    }
}
