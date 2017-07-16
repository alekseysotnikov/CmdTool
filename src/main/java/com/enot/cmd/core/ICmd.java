package com.enot.cmd.core;

public interface ICmd {
    Cmd cleanUp(boolean cleanUp);

    Cmd outputFileName(String outputFileName);

    Cmd configuring(LambdaListenerAdapter.BeforeStart configuring);

    CmdListening listening();

    /**
     * Specify command interpreter
     */
    Cmd interpreter(String interpreter);

    /**
     * Create executable command
     * @param command
     * @return
     */
    Command command(String... command);
}
