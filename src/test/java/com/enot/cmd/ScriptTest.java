package com.enot.cmd;

import com.enot.cmd.core.Script;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ScriptTest {
    @Test
    public void runInShell() throws InterruptedException, TimeoutException, IOException {
        Assert.assertEquals("Hello\n",
                new Script("s='Hello'; echo $s;")
                        .toExec()
                        .beforeStart(e -> e.readOutput(true))
                        .executor()
                        .execute()
                        .outputUTF8());
    }
}
