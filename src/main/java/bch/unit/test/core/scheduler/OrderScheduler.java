package bch.unit.test.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class OrderScheduler {

//    @Scheduled(fixedRate = 5000)
//    @Scheduled(fixedDelay = 5000)
//    @SchedulerLock(
//            name = "cancelTimeoutOrder",
//            lockAtMostFor = "5m",
//            lockAtLeastFor = "1m"
//    )
    public void cancelTimeoutOrder() {
        log.info("cancelTimeoutOrder");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
