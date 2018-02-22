package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LeaderElectionService {
    private int maxUidSeen;
    private int maxDistanceSeen;

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    private ThisNodeInfo thisNodeInfo;

    public LeaderElectionService() {
        setMaxUidSeen(thisNodeInfo.getUid());
        setMaxDistanceSeen(0);
    }

    public void leaderElection(LeaderElectionMessage message) {
        //TODO: convert to trace
        log.error("--------processing leader election message");
        //TODO implement
    }

    public int getMaxUidSeen() {
        return maxUidSeen;
    }

    public void setMaxUidSeen(int maxUidSeen) {
        this.maxUidSeen = maxUidSeen;
    }

    public int getMaxDistanceSeen() {
        return maxDistanceSeen;
    }

    public void setMaxDistanceSeen(int maxDistanceSeen) {
        this.maxDistanceSeen = maxDistanceSeen;
    }
}
