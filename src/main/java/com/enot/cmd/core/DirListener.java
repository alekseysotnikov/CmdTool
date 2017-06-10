package com.enot.cmd.core;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by asotnikov on 10/06/2017.
 */
public class DirListener extends ProcessListener {

    @Override
    public void beforeStart(ProcessExecutor executor) {
        File dir = executor.getDirectory();
        if (dir != null && !dir.exists()) {
            final boolean workDirCreated = dir.mkdirs();
            if (!workDirCreated)
                throw new UncheckedIOException(
                        new IOException(String.format("Work directory %s can not be created", dir.toPath())));
            executor.addListener(new ProcessListener() {
                @Override
                public void afterStop(Process process) {
                    if (workDirCreated){

                    }
                }
            });
        }


    }

    @Override
    public void afterStop(Process process) {

    }
}
