package com.enot.cmd;

import com.enot.cmd.core.Cmd;
import com.enot.cmd.ext.CmdSilent;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class ExtTest {
    @Test
    public void failedPath() {
        assertNull(new CmdSilent(new Cmd("unavailableProgram")).execute());

    }
}
