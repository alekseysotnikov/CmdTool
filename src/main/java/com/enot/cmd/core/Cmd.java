package com.enot.cmd.core;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

    private final boolean cleanUp;
    private final String outputFileName;
    private final Iterable<LambdaListenerAdapter> listeners;

    public Cmd() {
        this(false, "", ImmutableList.of());
    }

    public Cmd(boolean cleanUp, String outputFileName, Iterable<LambdaListenerAdapter> listeners) {
        this.cleanUp = cleanUp;
        this.outputFileName = outputFileName;
        this.listeners = listeners;
    }

    public Cmd listeners(LambdaListenerAdapter... listeners) {
        return new Cmd(cleanUp, outputFileName, Iterables.concat(this.listeners, ImmutableList.copyOf(listeners)));
    }

    public Cmd beforeStart(BeforeStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(cleanUp, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)));
    }

    public Cmd afterStart(AfterStart... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(cleanUp, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)));
    }

    public Cmd afterFinish(AfterFinish... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(cleanUp, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)));
    }

    public Cmd afterStop(AfterStop... lambdas) {
        List<LambdaListenerAdapter> labdasList = Arrays.stream(lambdas).map(LambdaListenerAdapter::new).collect(Collectors.toList());
        return new Cmd(cleanUp, outputFileName, Iterables.unmodifiableIterable(Iterables.concat(this.listeners, (Iterable) labdasList)));
    }

    /**
     * Delete work directory after process stopped, only if directory was specified
     *
     * @param cleanUp
     * @return
     */
    public Cmd cleanUp(boolean cleanUp) {
        return new Cmd(cleanUp, outputFileName, listeners);
    }

    public Cmd outputFileName(String outputFileName) {
        return new Cmd(cleanUp, outputFileName, listeners);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     * <p>
     * Note: Windows OS doesn't supported, use {@link #execute(String...)}} instead
     */
    public ProcessResult executeInShell(String script) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        return execute("sh", "-c", script);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     * <p>
     * Note: Windows OS doesn't supported, use {@link #execute(String...)}} instead
     */
    public ProcessResult executeInShell(ProcessExecutor executor) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        List<String> command = executor.getCommand();
        command.add(0, "sh");
        command.add(1, "-c");
        executor.command(command);
        return execute(executor);
    }

    /**
     * See {@link ProcessExecutor#execute()}
     */
    public ProcessResult execute(String... command) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        return execute(new ProcessExecutor(command));
    }

    /**
     * See {@link ProcessExecutor#execute()}
     */
    public ProcessResult execute(ProcessExecutor executor) throws IOException, TimeoutException, InterruptedException, InvalidExitValueException {
        return prepareExecutor(executor).execute();
    }

    /**
     * See {@link ProcessExecutor#executeNoTimeout()}
     */
    public ProcessResult executeNoTimeout(String... command) throws IOException, InterruptedException, InvalidExitValueException {
        return executeNoTimeout(new ProcessExecutor(command));
    }

    /**
     * See {@link ProcessExecutor#executeNoTimeout()}
     */
    public ProcessResult executeNoTimeout(ProcessExecutor executor) throws IOException, InterruptedException, InvalidExitValueException {
        return prepareExecutor(executor).executeNoTimeout();
    }

    /**
     * See {@link ProcessExecutor#start()}
     */
    public StartedProcess start(String... command) throws IOException {
        return start(new ProcessExecutor(command));
    }

    /**
     * See {@link ProcessExecutor#start()}
     */
    public StartedProcess start(ProcessExecutor executor) throws IOException {
        return prepareExecutor(executor).start();
    }

    private ProcessExecutor prepareExecutor(ProcessExecutor executor) throws IOException {
        File dir = executor.getDirectory();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new IOException("Can not create execution dir by path: " + dir.toString());
        }
        for (LambdaListenerAdapter listener : listeners) {
            executor.addListener(listener);
        }

        BeforeStart beforeStart = e -> {};
        if (!Strings.isNullOrEmpty(outputFileName)) {
            Path outputFile = Paths.get(dir.getPath(), outputFileName);
            OutputStream fileOutputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE);

            beforeStart = e -> {
                e.redirectOutputAlsoTo(fileOutputStream); //output stream will be closed by executor
            };
        }

        AfterStop afterStop = p -> {};
        if (cleanUp && dir != null) {
            afterStop = p -> {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    LOG.debug(e.getMessage(), e);
                }
            };
        }

        return executor
                .addListener(new LambdaListenerAdapter(
                        beforeStart,
                        (p, e) -> {/*nothing*/},
                        (p, r) -> {/*nothing*/},
                        afterStop));
    }

}
