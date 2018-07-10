package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Configuration
@Slf4j
public class ConnectConfig {

    @Bean
    @Qualifier("Node/ConnectConfig/subscriptionDestinations")
    public List<String> getSubscriptionDestinations() {
        return Arrays.asList(
                "/topic/mapMessage",
                "/topic/buildTreeQueryMessage",
                "/topic/buildTreeAckMessage",
                "/topic/buildTreeNackMessage",
                "/topic/markMessage",
                "/topic/stateMessage",
                "/topic/markResponseMessage",
                "/topic/mapResponseMessage"
        );
    }

    @Bean
    @Qualifier("Node/ConnectConfig/connectionTimeoutLatch")
    public CountDownLatch getConnectionTimeoutLatch() {
        return new CountDownLatch(1);
    }

    @Bean
    @Qualifier("Node/ConnectConfig/connectingSynchronizer")
    public Semaphore getConnectingSynchronizer() {
        Semaphore connectingSynchronizer = new Semaphore(1);
        try {
            connectingSynchronizer.acquire();
        }catch (java.lang.InterruptedException e){
            //ignore
        }
        return connectingSynchronizer;
    }

}
