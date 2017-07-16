package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

final class BaseCommand implements Command {
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
}
