package com.enot.cmd.listeners;

import com.enot.cmd.core.listening.BeforeStart;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.OutputStream;

/**
 * Redirect either output or error stream to another stream, even if the process stopped unexpectedly
 */
public final class RedirectTo implements BeforeStart {
    private final OutputStream outputStream;
    private final boolean fromErrorStream;

    public RedirectTo(OutputStream outputStream) {
        this(outputStream, false);
    }

    public RedirectTo(OutputStream outputStream, boolean fromErrorStream) {
        this.outputStream = outputStream;
        this.fromErrorStream = fromErrorStream;
    }

    @Override
    public void run(ProcessExecutor processExecutor) {
        if (fromErrorStream) {
            processExecutor.redirectErrorAlsoTo(outputStream);
        } else {
            processExecutor.redirectOutputAlsoTo(outputStream);
        }
    }

}
