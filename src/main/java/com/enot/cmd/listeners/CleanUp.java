package com.enot.cmd.listeners;

import com.enot.cmd.core.LambdaListenerAdapter;
import com.enot.cmd.core.listening.AfterStop;
import com.enot.cmd.core.listening.BeforeStart;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Deletes work directory after process stopped
 */
public final class CleanUp implements BeforeStart, AfterStop {
    private File dir;

    @Override
    public void run(ProcessExecutor processExecutor) {
        dir = processExecutor.getDirectory();
        processExecutor.addListener(new LambdaListenerAdapter((AfterStop) this));
    }

    @Override
    public void run(Process process) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Work directory " + dir.toPath() + " can not be deleted", e);
        }
    }
}
