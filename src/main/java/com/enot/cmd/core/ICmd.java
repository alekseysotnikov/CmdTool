package com.enot.cmd.core;

import com.enot.cmd.core.listening.BeforeStart;
import com.enot.cmd.core.listening.Listening;

public interface ICmd {
    ICmd configuring(BeforeStart... configuring);

    Listening listening();

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
