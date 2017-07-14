package com.enot.cmd.core;

public interface ICmd {
    Cmd cleanUp(boolean cleanUp);

    Cmd outputFileName(String outputFileName);

    Cmd configuring(LambdaListenerAdapter.BeforeStart configuring);

    CmdListening listening();

    /**
     * Create executable shell script
     * <p>
     * Note: Windows OS doesn't supported, use {@link #command(String...)}} instead
     */
    Command script(String script);

    /**
     * Create executable command
     * @param command
     * @return
     */
    Command command(String... command);
}
