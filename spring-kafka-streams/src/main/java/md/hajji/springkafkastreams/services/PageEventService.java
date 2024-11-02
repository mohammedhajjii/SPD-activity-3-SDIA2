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

    /**
     * create a transformer that takes KStream<String, PageEvent>
     *      and produce a KStream<String, Long>, spring cloud stream will create and associate those KStream objects
     *      to the input and output topics
     * @return our Transformer
     */

    @Bean
    public Function<KStream<String, PageEvent>, KStream<String, Long>> kStreamFunction() {
        return inputStream ->
             inputStream
                     // keep only pageEvent records when duration is over 100 sc:
                     .filter((k, v) -> v.duration() > 100)
                     // update key and value:
                     .map((k, v) -> new KeyValue<>(v.name(), 1L))
                     // group records by their keys (page name) and specify
                     // serializer and deserializer objects:
                     .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                     // create a time window (Tumbling windows)  of 7 sc
                     // like a micro-batch:
                     .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMillis(7000)))
                     // use aggregation (count) over collected records during time frame
                     // save count state in a state store called 'page-analytics' (used later):
                     .count(Materialized.as("page-analytics"))
                     // convert the KTable result into stream (KStream):
                     .toStream()
                     // extract key
                     .map((k, v) -> new KeyValue<>(k.key(), v));

    }


}
