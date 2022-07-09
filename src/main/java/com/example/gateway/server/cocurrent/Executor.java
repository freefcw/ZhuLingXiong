package com.example.gateway.server.cocurrent;

import com.example.gateway.server.logic.Dispatcher;

/**
 * Executor interface.
 */
public interface Executor extends Releasable {

    /**
     * Execute game logic.
     */
    void onExecute(Dispatcher dispatcher);
}
