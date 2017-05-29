package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

public class LambdaListenerAdapter extends ProcessListener {
    private final BeforeStart beforeStart;
    private final AfterStart afterStart;
    private final AfterFinish afterFinish;
    private final AfterStop afterStop;

    public LambdaListenerAdapter(BeforeStart beforeStart, AfterStart afterStart, AfterFinish afterFinish, AfterStop afterStop) {
        this.beforeStart = beforeStart;
        this.afterStart = afterStart;
        this.afterFinish = afterFinish;
        this.afterStop = afterStop;
    }

    public LambdaListenerAdapter(BeforeStart beforeStart) {
        this(beforeStart, (p, e) -> {/*nothing*/}, (p, r) -> {/*nothing*/}, p -> {/*nothing*/});
    }

    public LambdaListenerAdapter(AfterStart afterStart) {
        this(e -> {/*nothing*/}, afterStart, (p, r) -> {/*nothing*/}, p -> {/*nothing*/});
    }

    public LambdaListenerAdapter(AfterFinish afterFinish) {
        this(e -> {/*nothing*/}, (p, e) -> {/*nothing*/}, afterFinish, p -> {/*nothing*/});
    }

    public LambdaListenerAdapter(AfterStop afterStop) {
        this(e -> {/*nothing*/}, (p, e) -> {/*nothing*/}, (p, r) -> {/*nothing*/}, afterStop);
    }

    @Override
    public void beforeStart(ProcessExecutor executor) {
        beforeStart.run(executor);
    }

    @Override
    public void afterStart(Process process, ProcessExecutor executor) {
        afterStart.run(process, executor);
    }

    @Override
    public void afterFinish(Process process, ProcessResult result) {
        afterFinish.run(process, result);
    }

    @Override
    public void afterStop(Process process) {
        afterStop.run(process);
    }

    /**
     * See {@link ProcessListener#beforeStart(ProcessExecutor)}
     */
    public interface BeforeStart {
        void run(ProcessExecutor executor);
    }

    /**
     * See {@link ProcessListener#afterStart(Process, ProcessExecutor)}
     */
    public interface AfterStart {
        void run(Process process, ProcessExecutor executor);
    }

    /**
     * See {@link ProcessListener#afterFinish(Process, ProcessResult)}
     */
    public interface AfterFinish {
        void run(Process process, ProcessResult result);
    }

    /**
     * See {@link ProcessListener#afterStop(Process)}
     */
    public interface AfterStop {
        void run(Process process);
    }
}
