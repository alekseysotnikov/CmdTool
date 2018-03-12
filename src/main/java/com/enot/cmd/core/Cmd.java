package com.enot.cmd.core;

import com.enot.cmd.core.LambdaListenerAdapter.AfterFinish;
import com.enot.cmd.core.LambdaListenerAdapter.AfterStart;
import com.enot.cmd.core.LambdaListenerAdapter.AfterStop;
import com.enot.cmd.core.LambdaListenerAdapter.BeforeStart;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.zeroturnaround.exec.ProcessExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command line representation with the additional features around a process execution
 */
public final class Cmd implements ICmd {
    private final Listening listening;
    private final BeforeStart[] beforeStart;
    private final String interpreter;

    public Cmd() {
        this(new Listening(null, new IterableOf<>()), new BeforeStart[0], "");
    }

    public Cmd(Listening listening, BeforeStart[] beforeStart, String interpreter) {
        this.listening = listening;
        this.beforeStart = beforeStart;
        this.interpreter = interpreter;
    }

    @Override
    public Cmd configuring(BeforeStart... configuring) {
        return new Cmd(listening, configuring, interpreter);
    }

    @Override
    public CmdListening listening() {
        return new Listening(this, listening.listeners);
    }

    @Override
    public Cmd interpreter(String interpreter) {
        return new Cmd(listening, beforeStart, interpreter);
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
        listening.listeners.forEach(executor::addListener);
        configuringAfter.forEach(c -> c.run(executor));

        Iterable<String> commands = new IterableOf<>(command);
        if (interpreter != null && !interpreter.trim().isEmpty()) {
            commands = new Joined<>(new IterableOf<>(interpreter), commands);
        }
        return executor.command(commands);
    }


    private static final class Listening implements CmdListening {
        private final Cmd owner;
        private final Iterable<LambdaListenerAdapter> listeners;

        public Listening(Cmd owner, BeforeStart... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public Listening(Cmd owner, AfterStart... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public Listening(Cmd owner, AfterFinish... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public Listening(Cmd owner, AfterStop... lambdas) {
            this(owner, new Mapped<>(LambdaListenerAdapter::new, new IterableOf<>(lambdas)));
        }

        public Listening(Cmd owner, Iterable<LambdaListenerAdapter> listeners) {
            this.owner = owner;
            this.listeners = listeners;
        }

        @Override
        public CmdListening beforeStart(BeforeStart... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterStart(AfterStart... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterFinish(AfterFinish... lambdas) {
            return new Listening(
                    owner,
                    new Joined<>(
                            this.listeners,
                            new Mapped<>(LambdaListenerAdapter::new,
                                    new IterableOf<>(lambdas))));
        }

        @Override
        public CmdListening afterStop(AfterStop... lambdas) {
            return new Listening(
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
}
