package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Queue;

@Service
@Slf4j
public class LeaderElectionService {
    private LeaderElectionController leaderElectionController;
    private ThisNodeInfo thisNodeInfo;
    private Vote vote;

    private int roundsWithoutChange;
    private int leaderUid;
    private boolean hasLeader;

    @Autowired
    public LeaderElectionService(
            @Lazy LeaderElectionController leaderElectionController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.leaderElectionController = leaderElectionController;
        this.thisNodeInfo = thisNodeInfo;

        this.vote = new Vote();
        roundsWithoutChange = 0;
        hasLeader = false;
    }

    public void processNeighborlyAdvice(Queue<LeaderElectionMessage> electionMessages) {
        boolean didVoteChange = false;
        synchronized (electionMessages) {
            for (LeaderElectionMessage nextMessage : electionMessages) {
                int neighborMaxUid = nextMessage.getMaxUidSeen();
                int neighborMaxDistanceSeen = nextMessage.getMaxDistanceSeen();
                if (neighborMaxUid > vote.getMaxUidSeen()) {
                    vote.setMaxUidSeen(neighborMaxUid);
                    vote.setMaxDistanceSeen(neighborMaxDistanceSeen + 1);
                    didVoteChange = true;
                    if (log.isDebugEnabled()) {
                        log.debug("updated vote uid to: {} from message: {} in round: {}", neighborMaxUid, nextMessage, leaderElectionController.getRoundNumber());
                    }
                } else if (neighborMaxUid == vote.getMaxUidSeen() && neighborMaxDistanceSeen > vote.getMaxDistanceSeen()) {
                    if (log.isDebugEnabled()) {
                        log.debug("updated distance to: {} from message: {} in round: {}", neighborMaxDistanceSeen, nextMessage, leaderElectionController.getRoundNumber());
                    }
                    //Note: we don't count this as vote changing
                    vote.setMaxDistanceSeen(neighborMaxDistanceSeen);
                }
            }
        }
        if(didVoteChange) {
            roundsWithoutChange = 0;
        } else {
            roundsWithoutChange++;
        }
        if(log.isTraceEnabled()) {
            log.trace("round without vote change: {}", roundsWithoutChange);
        }
        //increment round in controller before control is handed back
        leaderElectionController.incrementRoundNumber();
        if(roundsWithoutChange >= 3 && thisNodeInfo.getUid() == vote.getMaxUidSeen()) {
            log.debug("--------I am leader");
            vote.setThisNodeLeader(true);
            leaderElectionController.announceSelfLeader();
        } else if(didVoteChange) {
            //only send if we received a message that changed vote
            leaderElectionController.sendLeaderElection();
        }
    }

    public void leaderAnnounce(int leaderUid) {
        //need to check if we have already learned of election result and suppress message if so
        if(!hasLeader) {
            this.leaderUid = leaderUid;
            hasLeader = true;
            leaderElectionController.announceLeader(leaderUid);
        }
    }

    @Bean
    @Qualifier("Node/LeaderElectionService/vote")
    public Vote getVote(){
        return vote;
    }

    public class Vote {
        private int maxUidSeen;
        private int maxDistanceSeen;
        private boolean isThisNodeLeader;

        public Vote(){
            maxUidSeen = thisNodeInfo.getUid();
            maxDistanceSeen = 0;
        }

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
