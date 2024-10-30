package md.hajji.springkafkastreams.records;

import java.time.LocalDateTime;

public record PageEvent(
        String name,
        String username,
        LocalDateTime timestamp,
        Long duration
) {
}
