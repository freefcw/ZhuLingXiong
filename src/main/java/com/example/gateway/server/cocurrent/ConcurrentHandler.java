package com.example.gateway.server.cocurrent;

import com.example.gateway.server.logic.Dispatcher;
import com.lmax.disruptor.EventHandler;

public class ConcurrentHandler implements EventHandler<ConcurrentEvent> {
    private final Dispatcher dispatcher;

    public ConcurrentHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onEvent(ConcurrentEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            Executor executor = event.getExecutor();
            if (null == executor) {
                return;
            }

            try {
                executor.onExecute(dispatcher);
            } finally {
                executor.release();
            }
        } finally {
            event.clearValues();
        }
    }
}
