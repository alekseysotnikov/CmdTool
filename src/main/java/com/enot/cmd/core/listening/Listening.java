package com.enot.cmd.core.listening;

import com.enot.cmd.core.ICmd;

public interface Listening {
    Listening beforeStart(BeforeStart... lambdas);

    Listening afterStart(AfterStart... lambdas);

    Listening afterFinish(AfterFinish... lambdas);

    Listening afterStop(AfterStop... lambdas);

    ICmd back();
}
