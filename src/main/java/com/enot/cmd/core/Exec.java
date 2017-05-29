package com.enot.cmd.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.enot.cmd.core.LambdaListenerAdapter.*;

/**
 * Execution process representation
 */
public class Exec {
    private final long timeoutSec;
    private final Iterable<LambdaListenerAdapter> listeners;
    private final String[] command;

    public Exec(String... commandWithArgs) {
        this(30, ImmutableList.of(), commandWithArgs);
    }

    public Exec(long timeoutSec, Iterable<LambdaListenerAdapter> listeners, String... command) {
        this.timeoutSec = timeoutSec;
        this.listeners = listeners;
        this.command = command;
    }

    public Exec timeout(long sec) {
        return new Exec(sec, listeners, command);
    }

    public Exec listeners(LambdaListenerAdapter... listeners) {
        if (listeners == null)
            return this;
        return new Exec(timeoutSec, Iterables.concat(this.listeners, ImmutableList.copyOf(listeners)), command);
    }

    public Exec beforeStart(BeforeStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(timeoutSec, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStart(AfterStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(timeoutSec, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStop(AfterFinish... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(timeoutSec, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStop(AfterStop... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(timeoutSec, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public ProcessExecutor executor() {
        ProcessExecutor executor;
        if (command.length == 1) {
            executor = new ProcessExecutor().commandSplit(command[0]);
        } else if (command.length == 2) {
            executor = new ProcessExecutor().commandSplit(String.format("%s %s", command[0], command[1]));
        } else {
            executor = new ProcessExecutor().command(this.command);
        }
        executor = executor
                .redirectErrorAlsoTo(Slf4jStream.of(getClass()).asWarn())
                .redirectOutputAlsoTo(Slf4jStream.of(getClass()).asInfo())
                .redirectErrorStream(true)
                .readOutput(true)
                .timeout(timeoutSec, TimeUnit.SECONDS)
                .destroyOnExit();
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }
        return executor;

    }
}
