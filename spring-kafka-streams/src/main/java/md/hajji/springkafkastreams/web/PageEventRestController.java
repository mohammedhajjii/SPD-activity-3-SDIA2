package md.hajji.springkafkastreams.web;

import lombok.RequiredArgsConstructor;
import md.hajji.springkafkastreams.utils.PageEventFactory;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
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
        // every second server will send a result:
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> countUntilInstant(Instant.now()));

    }

    private Map<String, Long> countUntilInstant(Instant instant){
        // create initial state that hold results:
        Map<String, Long> pageCountMap = new HashMap<>();
        interactiveQueryService
                // get state store
                .<ReadOnlyWindowStore<String, Long>>getQueryableStore(
                        "page-analytics",
                        QueryableStoreTypes.windowStore())
                // get all records within state store from instant - 5s to instant:
                .fetchAll(instant.minusSeconds(5), instant)
                // for every key value item save key value and value:
                .forEachRemaining(wkv -> pageCountMap.put(wkv.key.key(), wkv.value));
        return pageCountMap;
    }


}
