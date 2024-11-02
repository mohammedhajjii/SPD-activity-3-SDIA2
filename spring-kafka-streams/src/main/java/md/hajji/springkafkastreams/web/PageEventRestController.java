package md.hajji.springkafkastreams.web;

import lombok.RequiredArgsConstructor;
import md.hajji.springkafkastreams.records.PageEvent;
import md.hajji.springkafkastreams.utils.PageEventFactory;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.internals.AbstractStoreBuilder;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/pages")
@RequiredArgsConstructor
public class PageEventRestController {

    private final StreamBridge streamBridge;
    private final InteractiveQueryService interactiveQueryService;

    @GetMapping(path = "{topic}/{name}")
    public ResponseEntity<?> producePageEvent(
            @PathVariable String topic, @PathVariable String name) {

        var pageEvent = PageEventFactory.of(name);

        if (streamBridge.send(topic, pageEvent)) {
            return  ResponseEntity.ok(pageEvent);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Long>> getPageEvents() {

        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    Map<String, Long> pageCountMap = new HashMap<>();

                    Instant now = Instant.now();

                    interactiveQueryService
                            .<ReadOnlyWindowStore<String, Long>>getQueryableStore(
                            "page-analytics",
                                    QueryableStoreTypes.windowStore())
                            .fetchAll(now.minusSeconds(5), now)
                            .forEachRemaining(windowedKeyValue -> save(windowedKeyValue, pageCountMap));
                    return pageCountMap;
                });


    }

    private void save(KeyValue<Windowed<String>, Long> windowedLongKeyValue, Map<String, Long> pageCountMap){

        if (windowedLongKeyValue.key.key() instanceof String key
                && windowedLongKeyValue.value instanceof Long value) {
            pageCountMap.put(key, value);
        }
    }

}
