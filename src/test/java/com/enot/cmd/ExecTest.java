package com.enot.cmd;

import com.enot.cmd.core.Exec;
import com.enot.cmd.ext.Listeners;
import com.enot.cmd.ext.listeners.ReadOutputs;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecTest {

    @Test
    public void output() throws IOException, InterruptedException, TimeoutException {
        final String str = "Hello";
        String result =
                new Exec("echo", str)
                        .beforeStart(Listeners.readOutputs)
                        .executor()
                        .execute()
                        .outputUTF8();

        assertEquals(str + "\n", result);
    }

    @Test
    public void beforeStartListener() throws IOException, InterruptedException, TimeoutException {
        ArrayList<String> lines = Lists.newArrayList();
        final String arg = "line1";
        new Exec("echo", arg)
                .beforeStart(e -> e.redirectOutputAlsoTo(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        lines.add(line);
                    }
                }))
                .executor()
                .execute();

        assertEquals(1, lines.size());
        assertEquals(arg, lines.get(0));
    }
}
