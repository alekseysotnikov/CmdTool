package com.enot.cmd.core.listening;

import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

/**
 * See {@link ProcessListener#afterFinish(Process, ProcessResult)}
 */
public interface AfterFinish {
    void run(Process process, ProcessResult result);
}
