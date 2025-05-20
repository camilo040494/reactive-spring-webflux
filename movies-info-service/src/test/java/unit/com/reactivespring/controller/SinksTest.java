package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();


        //when

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux =replaySink.asFlux();
        integerFlux.subscribe((i)->{
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 =replaySink.asFlux();
        integerFlux1.subscribe((i)->{
            System.out.println("Subscriber 2 : " + i);
        });

        replaySink.tryEmitNext(3);

        Flux<Integer> integerFlux2 =replaySink.asFlux();
        integerFlux2.subscribe((i)->{
            System.out.println("Subscriber 3 : " + i);
        });
    }

    @Test
    void sinks_multicast() {
        //given

        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        //when
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux =multicast.asFlux();
        integerFlux.subscribe((i)->{
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 =multicast.asFlux();
        integerFlux1.subscribe((i)->{
            System.out.println("Subscriber 2 : " + i);
        });
        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sinks_unicast() {
        //given

        Sinks.Many<Integer> multicast = Sinks.many().unicast().onBackpressureBuffer();

        //when
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux =multicast.asFlux();
        integerFlux.subscribe((i)->{
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 =multicast.asFlux();
        integerFlux1.subscribe((i)->{
            System.out.println("Subscriber 2 : " + i);
        });
        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
