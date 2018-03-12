package com.enot.cmd.listeners;

import com.enot.cmd.core.LambdaListenerAdapter.BeforeStart;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Creates working directory if it does not exists
 */
public final class WorkDir implements BeforeStart{
    private final File dir;

    public WorkDir(String path) {
        this(new File(path));
    }

    public WorkDir(File dir) {
        this.dir = dir;
    }

    @Override
    public void run(ProcessExecutor executor) {
        executor.directory(dir);
        if (!dir.exists()
                && !dir.mkdirs()) {
            throw new UncheckedIOException(
                    new IOException("Work directory " + dir.toPath() + " can not be created"));
        }
    }
}
