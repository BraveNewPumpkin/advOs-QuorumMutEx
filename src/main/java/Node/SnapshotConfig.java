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
    private final TreeInfo treeInfo;

    @Autowired
    public SnapshotConfig(
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/BuildTreeConfig/treeInfo")
            TreeInfo treeInfo
    ) {
        this.thisNodeInfo = thisNodeInfo;
        this.treeInfo = treeInfo;
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/preparedForSnapshotSynchronizer")
    public GateLock getPreparedForSnapshotSynchronizer() {
        GateLock preparedForSnapshotSynchronizer = new GateLock();
        preparedForSnapshotSynchronizer.close();
        return preparedForSnapshotSynchronizer;
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/scheduler")
    public ScheduledExecutorService getScheduler() {
       return Executors.newScheduledThreadPool(1);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
    public MessageIntRoundSynchronizer<MarkMessage> getSnapshotMarkerSynchronizer() {
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        return new MessageIntRoundSynchronizer<>(numberOfNeighbors);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
    public MessageIntRoundSynchronizer<StateMessage> getSnapshotStateSynchronizer() {
        return new MessageIntRoundSynchronizer<>(0);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/fifoResponseRoundSynchronizer")
    public MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> getFifoResponseRoundSynchronizer() {
        return new MessageRoundSynchronizer<>(thisNodeInfo.getNeighbors().size());
    }
}
