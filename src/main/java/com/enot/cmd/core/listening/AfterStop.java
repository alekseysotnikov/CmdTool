package com.enot.cmd.core.listening;

import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * See {@link ProcessListener#afterStop(Process)}
 */
public interface AfterStop {
    void run(Process process);
}
