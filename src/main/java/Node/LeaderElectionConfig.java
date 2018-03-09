package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeaderElectionConfig {
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public LeaderElectionConfig(
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo
    ) {
        this.thisNodeInfo = thisNodeInfo;
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/electingNewLeader")
    public GateLock electingNewLeader() {
        GateLock gateLock = new GateLock();
        gateLock.close();
        return gateLock;
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/sendingInitialLeaderElectionMessage")
    public GateLock sendingInitialLeaderElectionMessage() {
        GateLock sendingInitialLeaderElectionMessage = new GateLock();
        return sendingInitialLeaderElectionMessage;
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/leaderElectionRoundSynchronizer")
    public NodeMessageRoundSynchronizer<LeaderElectionMessage> leaderElectionRoundSynchronizer(){
        return new NodeMessageRoundSynchronizer<>(thisNodeInfo.getNeighbors().size());
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/leaderDistanceRoundSynchronizer")
    public NodeMessageRoundSynchronizer<LeaderDistanceMessage> leaderDistanceRoundSynchronizer(){
        return new NodeMessageRoundSynchronizer<>(thisNodeInfo.getNeighbors().size());
    }
}
