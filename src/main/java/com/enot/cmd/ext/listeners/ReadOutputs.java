package com.enot.cmd.ext.listeners;

import com.enot.cmd.core.LambdaListenerAdapter;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * Configure executor to read both error and standard outputs of the process
 */
public class ReadOutputs implements LambdaListenerAdapter.BeforeStart {
    @Override
    public void run(ProcessExecutor executor) {
        executor
                .redirectErrorAlsoTo(Slf4jStream.of(getClass()).asWarn())
                .redirectOutputAlsoTo(Slf4jStream.of(getClass()).asInfo())
                .redirectErrorStream(true)
                .readOutput(true);
    }
}
