package com.enot.cmd.core;

import com.enot.cmd.core.LambdaListenerAdapter.BeforeStart;

public interface ICmd {
    ICmd configuring(BeforeStart... configuring);

    CmdListening listening();

    /**
     * Specify command interpreter
     */
    ICmd interpreter(String interpreter);

    /**
     * Create executable command
     * @param command
     * @return
     */
    Command command(String... command);
}
