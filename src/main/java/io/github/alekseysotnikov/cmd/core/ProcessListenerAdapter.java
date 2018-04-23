package io.github.alekseysotnikov.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

public class ProcessListenerAdapter extends ProcessListener {
    private final Listening.BeforeStart beforeStart;
    private final Listening.AfterStart afterStart;
    private final Listening.AfterFinish afterFinish;
    private final Listening.AfterStop afterStop;

    public ProcessListenerAdapter(Listening.BeforeStart beforeStart, Listening.AfterStart afterStart, Listening.AfterFinish afterFinish, Listening.AfterStop afterStop) {
        this.beforeStart = beforeStart;
        this.afterStart = afterStart;
        this.afterFinish = afterFinish;
        this.afterStop = afterStop;
    }

    public ProcessListenerAdapter(Listening.BeforeStart beforeStart) {
        this(beforeStart, (p, e) -> {/*nothing*/}, (p, r) -> {/*nothing*/}, p -> {/*nothing*/});
    }

    public ProcessListenerAdapter(Listening.AfterStart afterStart) {
        this(e -> {/*nothing*/}, afterStart, (p, r) -> {/*nothing*/}, p -> {/*nothing*/});
    }

    public ProcessListenerAdapter(Listening.AfterFinish afterFinish) {
        this(e -> {/*nothing*/}, (p, e) -> {/*nothing*/}, afterFinish, p -> {/*nothing*/});
    }

    public ProcessListenerAdapter(Listening.AfterStop afterStop) {
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
