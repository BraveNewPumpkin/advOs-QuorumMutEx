package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SynchGhsConfig {
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public SynchGhsConfig(
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
    @Qualifier("Node/SynchGhsConfig/sendingInitialMwoeSearchMessage")
    public GateLock sendingInitialMwoeSearchMessage() {
        GateLock sendingInitialMwoeSearchMessage = new GateLock();
        return sendingInitialMwoeSearchMessage;
    }

    @Bean
    @Qualifier("Node/SynchGhsConfig/mwoeSearchGate")
    public GateLock mwoeSearchGate() {
        GateLock mwoeSearchGate = new GateLock();
        return mwoeSearchGate;
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/mwoeSearchResponseRoundSynchronizer")
    public NodeIncrementableRoundSynchronizer mwoeSearchResponseRoundSynchronizer(){
        return new NodeIncrementableRoundSynchronizer(thisNodeInfo.getNeighbors().size());
    }

    @Bean
    @Qualifier("Node/LeaderElectionConfig/mwoeSearchSynchronizer")
    public MwoeSearchSynchronizer<MwoeSearchMessage> mwoeSearchRoundSynchronizer(){
        int roundSize = thisNodeInfo.getNeighbors().size();
        int numRoundsInPhase = thisNodeInfo.getTotalNumberOfNodes() * 3;
        return new MwoeSearchSynchronizer<>(roundSize, numRoundsInPhase);
    }
}
