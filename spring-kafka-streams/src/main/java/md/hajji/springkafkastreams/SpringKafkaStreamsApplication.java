package md.hajji.springkafkastreams;

import md.hajji.springkafkastreams.utils.PageEventFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringKafkaStreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaStreamsApplication.class, args);
    }

    @Bean
    CommandLineRunner start(){
        return args -> {
            Stream.generate(PageEventFactory::get)
                    .limit(10)
                    .forEach(System.out::println);
        };
    }

}
