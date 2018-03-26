package com.enot.cmd.core.listening;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * See {@link ProcessListener#afterStart(Process, ProcessExecutor)}
 */
public interface AfterStart {
    void run(Process process, ProcessExecutor executor);
}
