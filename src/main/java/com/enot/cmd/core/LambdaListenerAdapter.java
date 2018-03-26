package com.enot.cmd.core;

import com.enot.cmd.core.listening.AfterFinish;
import com.enot.cmd.core.listening.AfterStart;
import com.enot.cmd.core.listening.AfterStop;
import com.enot.cmd.core.listening.BeforeStart;
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

}
