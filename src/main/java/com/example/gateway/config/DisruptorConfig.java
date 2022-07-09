package com.example.gateway.config;


import com.example.gateway.server.cocurrent.ConcurrentEvent;
import com.example.gateway.server.cocurrent.ConcurrentHandler;
import com.example.gateway.server.logic.Dispatcher;
import com.example.gateway.server.logic.LogicProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DisruptorConfig {
    private static final int DEFAULT_SIZE = 8 * 1024;
    private Disruptor<ConcurrentEvent> disruptor;

    public DisruptorConfig(Dispatcher dispatcher) {
        this.initDisruptor(dispatcher);
    }

    private void initDisruptor(Dispatcher dispatcher) {
        this.disruptor = new Disruptor<>(ConcurrentEvent::new, DEFAULT_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());

        this.disruptor.handleEventsWith(new ConcurrentHandler(dispatcher))
                .then(new ClearingEventHandler<>());

        this.disruptor.setDefaultExceptionHandler(new ExceptionHandler<>() {
            @Override
            public void handleEventException(Throwable throwable, long l, ConcurrentEvent concurrentEvent) {
                log.error("disruptor exception: {}", throwable.getMessage());
            }

            @Override
            public void handleOnStartException(Throwable throwable) {
                log.error("disruptor start exception: {}", throwable.getMessage());
            }

            @Override
            public void handleOnShutdownException(Throwable throwable) {
                log.error("disruptor shutdown exception: {}", throwable.getMessage());
            }
        });
        this.disruptor.start();
    }

    @Bean
    public LogicProcessor createLogicProcessor() {
        return new LogicProcessor(this.disruptor.getRingBuffer());
    }

    public static class ClearingEventHandler<T> implements EventHandler<ConcurrentEvent> {
        public void onEvent(ConcurrentEvent event, long sequence, boolean endOfBatch) {
            // Failing to call clear here will result in the
            // object associated with the event to live until
            // it is overwritten once the ring buffer has wrapped
            // around to the beginning.
            event.clearValues();
        }
    }
}
