package com.example.gateway.server.logic;

import com.example.gateway.server.cocurrent.ConcurrentEvent;
import com.example.gateway.server.cocurrent.Executor;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicProcessor {
    private final RingBuffer<ConcurrentEvent> ringBuffer;


    public LogicProcessor(RingBuffer<ConcurrentEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void addRequest(final Executor executor) {
        this.ringBuffer.publishEvent((event, sequence, executor1) -> event.setValues(executor1), executor);
    }
}
