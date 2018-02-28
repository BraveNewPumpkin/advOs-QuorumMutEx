package Node;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
public class LeaderElectionConfig {
    @Bean
    @Qualifier("Node/LeaderElectionConfig/electingNewLeader")
    public Semaphore electingNewLeader() {
        return new Semaphore(0);
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/sendingInitialLeaderElectionMessage")
    public ReadWriteLock sendingInitialLeaderElectionMessage() {
        ReadWriteLock sendingInitialLeaderElectionMessage = new ReentrantReadWriteLock();
        return sendingInitialLeaderElectionMessage;
    }

    }
