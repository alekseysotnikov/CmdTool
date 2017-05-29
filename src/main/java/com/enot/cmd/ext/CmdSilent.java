package com.enot.cmd.ext;

import com.enot.cmd.core.Cmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

/**
 * {@link Cmd} wrapper to absorb Exceptions
 */
public class CmdSilent {
    private static final Logger LOG = LoggerFactory.getLogger(CmdSilent.class.getName());

    private final Cmd cmd;

    public CmdSilent(Cmd cmd) {
        this.cmd = cmd;
    }

    public ProcessResult execute() {
        try {
            return cmd.execute();
        } catch (Exception e){
            LOG.debug(e.getMessage(), e);
        }
        return null;
    }
}
