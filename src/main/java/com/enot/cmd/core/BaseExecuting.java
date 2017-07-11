package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

final class BaseExecuting implements Executing {
    private final ProcessExecutor processExecutor;

    BaseExecuting(ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    /**
     * See {@link ProcessExecutor#execute()}
     * <p>
     * Note: Windows OS doesn't supported, use {@link #execute(String...)}} instead
     */
    @Override
    public ProcessResult executeInShell(String script) throws IOException, TimeoutException, InterruptedException {
        return execute("sh", "-c", script);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     */
    @Override
    public ProcessResult execute(String... command) throws IOException, TimeoutException, InterruptedException {
        return processExecutor.command(command).execute();
    }

    /**
     * See {@link ProcessExecutor#executeNoTimeout()}
     */
    @Override
    public ProcessResult executeNoTimeout(String... command) throws IOException, InterruptedException {
        return processExecutor.command(command).executeNoTimeout();
    }

    /**
     * See {@link ProcessExecutor#start()}
     */
    @Override
    public StartedProcess start(String... command) throws IOException {
        return processExecutor.command(command).start();
    }
}
