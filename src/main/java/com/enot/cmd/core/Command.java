package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface Command {
    ProcessResult execute() throws IOException, TimeoutException, InterruptedException;

    ProcessResult executeNoTimeout() throws IOException, InterruptedException;

    StartedProcess start() throws IOException;

    List<String> commandLine();
}
