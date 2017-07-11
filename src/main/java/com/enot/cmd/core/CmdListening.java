package com.enot.cmd.core;

public interface CmdListening {
    CmdListening beforeStart(LambdaListenerAdapter.BeforeStart... lambdas);

    CmdListening afterStart(LambdaListenerAdapter.AfterStart... lambdas);

    CmdListening afterFinish(LambdaListenerAdapter.AfterFinish... lambdas);

    CmdListening afterStop(LambdaListenerAdapter.AfterStop... lambdas);

    Cmd back();
}
