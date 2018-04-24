package io.github.alekseysotnikov.cmd.core;

import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Command line representation with the additional features around a process execution
 */
public final class Cmd implements ICmd {
    private final Iterable<ProcessListenerAdapter> listeners;
    private final Listening.BeforeStart[] configuring;
    private final String interpreter;

    public Cmd() {
        this(new IterableOf<>(), new Listening.BeforeStart[0], "");
    }

    public Cmd(Iterable<ProcessListenerAdapter> listeners, Listening.BeforeStart[] configuring, String interpreter) {
        this.listeners = listeners;
        this.configuring = configuring;
        this.interpreter = interpreter;
    }

    @Override
    public ICmd configuring(Listening.BeforeStart... configuring) {
        return new Cmd(listeners, configuring, interpreter);
    }

    @Override
    public ICmd listening(Listening.BeforeStart... beforeStart) {
        return new Cmd(
                new Joined<>(
                        this.listeners,
                        new Mapped<>(ProcessListenerAdapter::new,
                                new IterableOf<>(beforeStart))),
                configuring,
                interpreter);
    }

    @Override
    public ICmd listening(Listening.AfterStart... afterStart) {
        return new Cmd(
                new Joined<>(
                        this.listeners,
                        new Mapped<>(ProcessListenerAdapter::new,
                                new IterableOf<>(afterStart))),
                configuring,
                interpreter);
    }

    @Override
    public ICmd listening(Listening.AfterFinish... afterFinish) {
        return new Cmd(
                new Joined<>(
                        this.listeners,
                        new Mapped<>(ProcessListenerAdapter::new,
                                new IterableOf<>(afterFinish))),
                configuring,
                interpreter);
    }

    @Override
    public ICmd listening(Listening.AfterStop... afterStop) {
        return new Cmd(
                new Joined<>(
                        this.listeners,
                        new Mapped<>(ProcessListenerAdapter::new,
                                new IterableOf<>(afterStop))),
                configuring,
                interpreter);
    }

    @Override
    public ICmd interpreter(String interpreter) {
        return new Cmd(listeners, configuring, interpreter);
    }

    @Override
    public Command command(String... command) {
        return new BaseCommand(processExecutor(command));
    }

    private ProcessExecutor processExecutor(String... command) {
        ProcessExecutor executor = new ProcessExecutor();

        Map<Boolean, List<Listening.BeforeStart>> configuring = Arrays.stream(this.configuring).collect(
                Collectors.groupingBy(c -> c instanceof Listening.AfterStop));
        List<Listening.BeforeStart> configuringBefore = configuring.getOrDefault(false, Collections.emptyList());
        List<Listening.BeforeStart> configuringAfter = configuring.getOrDefault(true, Collections.emptyList());

        configuringBefore.forEach(c -> c.run(executor));
        listeners.forEach(executor::addListener);
        configuringAfter.forEach(c -> c.run(executor));

        Iterable<String> commands = new IterableOf<>(command);
        if (interpreter != null && !interpreter.trim().isEmpty()) {
            commands = new Joined<>(new IterableOf<>(interpreter), commands);
        }
        return executor.command(commands);
    }

    private static final class BaseCommand implements Command {
        private final ProcessExecutor processExecutor;

        BaseCommand(ProcessExecutor processExecutor) {
            this.processExecutor = processExecutor;
        }

        /**
         * See {@link ProcessExecutor#execute()}
         */
        @Override
        public ProcessResult execute() throws IOException, TimeoutException, InterruptedException {
            return processExecutor.execute();
        }

        /**
         * See {@link ProcessExecutor#executeNoTimeout()}
         */
        @Override
        public ProcessResult executeNoTimeout() throws IOException, InterruptedException {
            return processExecutor.executeNoTimeout();
        }

        /**
         * See {@link ProcessExecutor#start()}
         */
        @Override
        public StartedProcess start() throws IOException {
            return processExecutor.start();
        }

        @Override
        public List<String> commandLine() {
            return processExecutor.getCommand();
        }
    }
}
