package Node;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class LeaderElectionConfig {
    @Bean
    @Qualifier("Node/LeaderElectionConfig/electingNewLeader")
    public Semaphore electingNewLeader() {
        return new Semaphore(0);
    }
}
