package com.enot.cmd.core;

/**
 * Shell script representation
 */
public class Script {
    private final String script;

    public Script(String script) {
        this.script = script;
    }

    public Exec toExec() {
        return new Exec("sh", "-c", script);
    }
}
