package com.enot.cmd.core;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.enot.cmd.core.LambdaListenerAdapter.*;

/**
 * Command line representation with the additional features around a process execution
 */
public class Cmd {
    private static final Logger LOG = LoggerFactory.getLogger(Cmd.class.getName());

    private final ProcessExecutor executor;
    private final boolean deleteExecDir;
    private final String outputFileName;
    private final Iterable<LambdaListenerAdapter> listeners;
    private final boolean script;

    public Cmd(String... command) {
        this(new ProcessExecutor(command));
    }

    public Cmd(ProcessExecutor executor) {
        this(executor, false, "", ImmutableList.of(), false);
    }

    public Cmd(ProcessExecutor executor, boolean deleteExecDir, String outputFileName, Iterable<LambdaListenerAdapter> listeners, boolean script) {
        this.executor = executor;
        this.deleteExecDir = deleteExecDir;
        this.outputFileName = outputFileName;
        this.listeners = listeners;
        this.script = script;
    }

    public Cmd listeners(LambdaListenerAdapter... listeners) {
        return new Cmd(executor, deleteExecDir, outputFileName, Iterables.concat(this.listeners, ImmutableList.copyOf(listeners)), script);
    }

    public Cmd beforeStart(BeforeStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(executor, deleteExecDir, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), script);
    }

    public Cmd afterStart(AfterStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(executor, deleteExecDir, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), script);
    }

    public Cmd afterStop(AfterFinish... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(executor, deleteExecDir, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), script);
    }

    public Cmd afterStop(AfterStop... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(executor, deleteExecDir, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)), script);
    }

    public Cmd deleteExecDir(boolean deleteExecDir) {
        return new Cmd(executor, deleteExecDir, outputFileName, listeners, script);
    }

    public Cmd outputFileName(String outputFileName) {
        return new Cmd(executor, deleteExecDir, outputFileName, listeners, script);
    }

    public Cmd script(boolean script) {
        return new Cmd(executor, deleteExecDir, outputFileName, listeners, script);
    }

    /**
     * Execute command.
     * Before execution: creates execution directory if it does not exists.
     * After execution: if deleteEmptyDir=true, it will delete execution directory.
     *
     * @return {@link ProcessResult}
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public ProcessResult execute() throws IOException, TimeoutException, InterruptedException {
        File dir = executor.getDirectory();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new IOException("Can not create execution dir by path: " + dir.toString());
        }
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }
        ProcessResult result;
        final boolean isSaveOutputToFile = !deleteExecDir && !Strings.isNullOrEmpty(outputFileName);
        try (OutputStream fileOutput =
                     ((isSaveOutputToFile && dir != null) ? Files.newOutputStream(Paths.get(dir.toString(), outputFileName), StandardOpenOption.CREATE) : null)) {
            BeforeStart beforeStart = e -> {
                if (isSaveOutputToFile) {
                    e.redirectOutputAlsoTo(fileOutput);
                }
            };

            AfterStop afterStop = p -> {
                if (this.deleteExecDir && dir != null) {
                    try {
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        LOG.debug(e.getMessage(), e);
                    }
                }
            };

            if (script){
                //OS recognition
                List<String> commands = executor.getCommand();
                commands.addAll(0,Arrays.asList("sh", "-c"));
                executor.command(commands);
            }
            result = executor
                    .addListener(new LambdaListenerAdapter(beforeStart))
                    .addListener(new LambdaListenerAdapter(afterStop))
                    .execute();
        }
        return result;
    }
}
