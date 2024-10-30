package md.hajji.springkafkastreams.services;

import lombok.extern.slf4j.Slf4j;
import md.hajji.springkafkastreams.records.PageEvent;
import md.hajji.springkafkastreams.utils.PageEventFactory;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Slf4j
public class PageEventService {

    @Bean
    public Consumer<PageEvent> pageEventConsumer() {
        return pageEvent -> {
            log.info("received PageEvent: {}", pageEvent);
        };
    }

    @Bean
    public Supplier<PageEvent> pageEventSupplier() {
        return PageEventFactory::get;
    }

    @Bean
    public Function<PageEvent, PageEvent> pageEventFunction() {
        return PageEventFactory::from;
    }


    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> kStreamFunction() {
        return inputStream ->
             inputStream
                     .filter((k, v) -> v.duration() > 100)
                     .map((k, v) -> new KeyValue<>(v.name(), 1L))
                     .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                     .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMillis(7000)))
                     .count(Materialized.as("page-analytics"))
                     .toStream()
                     .map((k, v) -> new KeyValue<>(k.key(), v));

    }


}
