package com.enot.cmd.core;

import com.enot.cmd.core.listening.*;
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
    private final CmdListening cmdListening;
    private final BeforeStart[] beforeStart;
    private final String interpreter;

    public Cmd() {
        this(new CmdListening(null, new IterableOf<>()), new BeforeStart[0], "");
    }

    public Cmd(CmdListening cmdListening, BeforeStart[] beforeStart, String interpreter) {
        this.cmdListening = cmdListening;
        this.beforeStart = beforeStart;
        this.interpreter = interpreter;
    }

    @Override
    public Cmd configuring(BeforeStart... configuring) {
        return new Cmd(cmdListening, configuring, interpreter);
    }

    @Override
    public Listening listening() {
        return new CmdListening(this, cmdListening.listeners);
    }

    @Override
    public Cmd interpreter(String interpreter) {
        return new Cmd(cmdListening, beforeStart, interpreter);
    }

    public Command command(String... command) {
        return new BaseCommand(processExecutor(command));
    }

    private ProcessExecutor processExecutor(String... command) {
        ProcessExecutor executor = new ProcessExecutor();

        Map<Boolean, List<BeforeStart>> configuring = Arrays.stream(beforeStart).collect(
                Collectors.groupingBy(c -> c instanceof AfterStop));
        List<BeforeStart> configuringBefore = configuring.getOrDefault(false, Collections.emptyList());
        List<BeforeStart> configuringAfter = configuring.getOrDefault(true, Collections.emptyList());

        configuringBefore.forEach(c -> c.run(executor));
        cmdListening.listeners.forEach(executor::addListener);
        configuringAfter.forEach(c -> c.run(executor));

        Iterable<String> commands = new IterableOf<>(command);
        if (interpreter != null && !interpreter.trim().isEmpty()) {
            commands = new Joined<>(new IterableOf<>(interpreter), commands);
        }
        return executor.command(commands);
    }


    private static final class CmdListening implements Listening {
        private final Cmd owner;
        private final Iterable<LambdaListenerAdapter> listeners;

        public CmdListening(Cmd owner, BeforeStart... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public CmdListening(Cmd owner, AfterStart... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public CmdListening(Cmd owner, AfterFinish... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public CmdListening(Cmd owner, AfterStop... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public CmdListening(Cmd owner, Iterable<LambdaListenerAdapter> listeners) {
            this.owner = owner;
            this.listeners = listeners;
        }

        @Override
        public Listening beforeStart(BeforeStart... lambdas) {
            return new CmdListening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public Listening afterStart(AfterStart... lambdas) {
            return new CmdListening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public Listening afterFinish(AfterFinish... lambdas) {
            return new CmdListening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public Listening afterStop(AfterStop... lambdas) {
            return new CmdListening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public Cmd back() {
            return new Cmd(this, owner.beforeStart, owner.interpreter);
        }
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
