package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Executing {
    ProcessResult executeInShell(String script) throws IOException, TimeoutException, InterruptedException;

    ProcessResult execute(String... command) throws IOException, TimeoutException, InterruptedException;

    ProcessResult executeNoTimeout(String... command) throws IOException, InterruptedException;

    StartedProcess start(String... command) throws IOException;
}
