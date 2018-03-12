package com.enot.cmd.core;

import com.enot.cmd.core.LambdaListenerAdapter.AfterFinish;
import com.enot.cmd.core.LambdaListenerAdapter.AfterStart;
import com.enot.cmd.core.LambdaListenerAdapter.AfterStop;
import com.enot.cmd.core.LambdaListenerAdapter.BeforeStart;

public interface CmdListening {
    CmdListening beforeStart(BeforeStart... lambdas);

    CmdListening afterStart(AfterStart... lambdas);

    CmdListening afterFinish(AfterFinish... lambdas);

    CmdListening afterStop(AfterStop... lambdas);

    Cmd back();
}
