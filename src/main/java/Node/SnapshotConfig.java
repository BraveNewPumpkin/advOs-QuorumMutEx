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
    @Qualifier("Node/SnapshotConfig/scheduler")
    public ScheduledExecutorService getScheduler() {
       return Executors.newScheduledThreadPool(1);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
    public NodeMessageRoundSynchronizer<MarkMessage> getSnapshotMarkerSynchronizer() {
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        return new NodeMessageRoundSynchronizer<>(numberOfNeighbors);
    }

    @Bean
    @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
    public NodeMessageRoundSynchronizer<StateMessage> getSnapshotStateSynchronizer() {
        int numberOfChildren = treeInfo.getNumChildren();
        return new NodeMessageRoundSynchronizer<>(numberOfChildren);
    }
}
