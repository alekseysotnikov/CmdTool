package com.enot.cmd.core.listening;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * See {@link ProcessListener#beforeStart(ProcessExecutor)}
 */
public interface BeforeStart {
    void run(ProcessExecutor executor);
}
