package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.PriorityBlockingQueue;

@Configuration
@Slf4j
public class QuorumMutExConfig {

    @Autowired
    public QuorumMutExConfig(
    ) {
    }

    @Bean
    @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
    public QuorumMutExInfo quorumMutExInfo() {
        return new QuorumMutExInfo();
    }

    @Bean
    @Qualifier("Node/QuorumMutExConfig/workQueue")
    public PriorityBlockingQueue<QuorumMutExWork> getWorkQueue() {
        return new PriorityBlockingQueue<>();
    }
}
