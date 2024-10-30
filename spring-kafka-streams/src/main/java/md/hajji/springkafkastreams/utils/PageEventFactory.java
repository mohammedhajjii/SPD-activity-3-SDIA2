package md.hajji.springkafkastreams.utils;

import md.hajji.springkafkastreams.records.PageEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class PageEventFactory {

    // random instance:
    private static final Random random = new Random();
    // user list:
    private static final List<String> USERS =
            List.of("Mohammed", "Hassan", "Mouad", "Souhail", "Taha", "Chaimae", "Meryem");

    // page list:
    private static final List<String> PAGES =
            List.of("costumers", "products", "categories", "orders", "bills");

    /**
     * Create a new pageEvent instance, with appropriate name
     * @param name: page name
     * @return PageEvent instance
     */
    public static PageEvent of(String name){
        // get a random index [0, User list size];
        int userIndex = random.nextInt(USERS.size());
        // create and return a PageEvent instance:
        return new PageEvent(
                name,
                USERS.get(userIndex),
                LocalDateTime.now(),
                random.nextLong(1000, 8000)
        );
    }

    /**
     * generate new page randomly
     * @return PageEvent instance
     */

    public static PageEvent get(){
        int pageIndex = random.nextInt(PAGES.size());
        return of(PAGES.get(pageIndex));
    }

    /**
     * generate a new pageEvent from another
     * @param other: pageEvent to clone
     * @return newPageEvent
     */
    public static PageEvent from(PageEvent other){
        return new PageEvent(
                "/" + other.name(),
                other.username(),
                other.timestamp(),
                other.duration()
        );
    }
}
