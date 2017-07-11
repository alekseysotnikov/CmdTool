package com.enot.cmd.core;

public interface ICmd {
    Cmd cleanUp(boolean cleanUp);

    Cmd outputFileName(String outputFileName);

    CmdListening listening();

    Executing executing();
}
