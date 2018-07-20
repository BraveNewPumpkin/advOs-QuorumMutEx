package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private QuorumMutExController quorumMutExController;

    @Autowired
    public ConnectConfig(
            QuorumMutExController quorumMutExController
    ) {
        this.quorumMutExController = quorumMutExController;
    }

    @Bean
    @Qualifier("Node/ConnectConfig/messageRouteInfos")
    public List<MessageRouteInfo> getMessageRouteInfos() {
        return Arrays.asList(
                new MessageRouteInfo<>("/topic/requestMessage", RequestMessage.class, quorumMutExController::requestMessage),
                new MessageRouteInfo<>("/topic/releaseMessage", ReleaseMessage.class, quorumMutExController::releaseMessage),
                new MessageRouteInfo<>("/topic/failedMessage", FailedMessage.class, quorumMutExController::failedMessage),
                new MessageRouteInfo<>("/topic/grantMessage", GrantMessage.class, quorumMutExController::grantMessage),
                new MessageRouteInfo<>("/topic/inquireMessage", InquireMessage.class, quorumMutExController::inquireMessage),
                new MessageRouteInfo<>("/topic/yieldMessage", YieldMessage.class, quorumMutExController::yieldMessage)
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
