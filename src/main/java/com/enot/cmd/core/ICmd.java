package com.enot.cmd.core;

public interface ICmd {
    ICmd cleanUp(boolean cleanUp);

    ICmd outputFileName(String outputFileName);

    ICmd configuring(LambdaListenerAdapter.BeforeStart configuring);

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
