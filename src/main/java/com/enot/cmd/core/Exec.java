package com.enot.cmd.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.zeroturnaround.exec.ProcessExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.enot.cmd.core.LambdaListenerAdapter.*;

/**
 * Execution process representation
 */
public class Exec {
    private final Iterable<LambdaListenerAdapter> listeners;
    private final String[] command;

    public Exec(String... commandWithArgs) {
        this(ImmutableList.of(), commandWithArgs);
    }

    public Exec(Iterable<LambdaListenerAdapter> listeners, String... command) {
        this.listeners = listeners;
        this.command = command;
    }

    public Exec listeners(LambdaListenerAdapter... listeners) {
        if (listeners == null)
            return this;
        return new Exec(Iterables.concat(this.listeners, ImmutableList.copyOf(listeners)), command);
    }

    public Exec beforeStart(BeforeStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStart(AfterStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStop(AfterFinish... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public Exec afterStop(AfterStop... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Exec(Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), command);
    }

    public ProcessExecutor executor() {
        ProcessExecutor executor = new ProcessExecutor().command(this.command);
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }
        return executor;

    }
}
