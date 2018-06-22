package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SnapshotConfig {
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public SnapshotConfig(
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo
    ) {
        this.thisNodeInfo = thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/scheduler")
    public ScheduledExecutorService getScheduler() {
       return Executors.newScheduledThreadPool(1);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/snaphshotSynchronizer")
    public NodeMessageRoundSynchronizer<MarkMessage> getSnapshotSynchronizer() {
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        return new NodeMessageRoundSynchronizer<>(numberOfNeighbors);
    }
}
