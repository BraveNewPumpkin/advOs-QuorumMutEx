package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Queue;

@Service
@Slf4j
public class LeaderElectionService {
    private LeaderElectionController leaderElectionController;
    private ThisNodeInfo thisNodeInfo;
    private Vote vote;

    private int roundsWithoutChange;

    @Autowired(required = true)
    public LeaderElectionService(
            LeaderElectionController leaderElectionController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionService/Vote") Vote vote
    ) {
        this.leaderElectionController = leaderElectionController;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = vote;
        vote.setMaxUidSeen(thisNodeInfo.getUid());
        vote.setMaxDistanceSeen(0);

        roundsWithoutChange = 0;
    }

    public void processNeighborlyAdvice(Queue<LeaderElectionMessage> advices) {
        boolean didVoteChange = false;
        for(LeaderElectionMessage nextMessage : advices) {
            int neighborMaxUid = nextMessage.getMaxUidSeen();
            int neighborMaxDistanceSeen = nextMessage.getMaxDistanceSeen();
            if(neighborMaxUid > vote.getMaxUidSeen()) {
                vote.setMaxUidSeen(neighborMaxUid);
                vote.setMaxDistanceSeen(neighborMaxDistanceSeen + 1);
                didVoteChange = true;
            } else if(neighborMaxUid == vote.getMaxUidSeen() && neighborMaxDistanceSeen > vote.getMaxDistanceSeen()) {
                vote.setMaxDistanceSeen(neighborMaxDistanceSeen);
                didVoteChange = true;
            }
        }
        if(didVoteChange) {
            roundsWithoutChange = 0;
        } else {
            roundsWithoutChange++;
        }
        if(roundsWithoutChange >= 3 && thisNodeInfo.getUid() == vote.getMaxUidSeen()) {
            leaderElectionController.announceSelfLeader();
        } else {
            leaderElectionController.sendLeaderElection();
        }
    }

    @Component
    @Qualifier("Node/LeaderElectionService/Vote")
    public class Vote {
        private int maxUidSeen;
        private int maxDistanceSeen;
        private boolean isThisNodeLeader;

        //autowire in the outer class component (compiler changes to Vote(LeaderElectionService outer) and then spring
        // injects that component)
        @Autowired
        public Vote(){}

        public int getMaxUidSeen() {
            return maxUidSeen;
        }

        public int getMaxDistanceSeen() {
            return maxDistanceSeen;
        }

        public boolean isThisNodeLeader() {
            return isThisNodeLeader;
        }

        private void setMaxUidSeen(int maxUidSeen) {
            this.maxUidSeen = maxUidSeen;
        }

        private void setMaxDistanceSeen(int maxDistanceSeen) {
            this.maxDistanceSeen = maxDistanceSeen;
        }

        private void setThisNodeLeader(boolean thisNodeLeader) {
            isThisNodeLeader = thisNodeLeader;
        }
    }
}
