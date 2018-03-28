package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

public interface Listening {
    /**
     * See {@link ProcessListener#afterFinish(Process, ProcessResult)}
     */
    interface AfterFinish {
        void run(Process process, ProcessResult result);
    }

    /**
     * See {@link ProcessListener#afterStart(Process, ProcessExecutor)}
     */
    interface AfterStart {
        void run(Process process, ProcessExecutor executor);
    }

    /**
     * See {@link ProcessListener#afterStop(Process)}
     */
    interface AfterStop {
        void run(Process process);
    }

    /**
     * See {@link ProcessListener#beforeStart(ProcessExecutor)}
     */
    interface BeforeStart {
        void run(ProcessExecutor executor);
    }
}
