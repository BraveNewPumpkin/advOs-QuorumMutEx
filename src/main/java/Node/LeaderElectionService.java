package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class LeaderElectionService {
    private final LeaderElectionController leaderElectionController;
    private final ThisNodeInfo thisNodeInfo;
    private final GateLock electingNewLeader;
    private final NodeMessageRoundSynchronizer<LeaderElectionMessage> leaderElectionRoundSynchronizer;
    private final NodeMessageRoundSynchronizer<LeaderDistanceMessage> leaderDistanceRoundSynchronizer;

    private final Vote vote;
    private int roundsWithoutChange;
    private boolean knowsLeader;
    private int MaxDistanceFromLeader;
    private int minDistanceToLeader;

    @Autowired
    public LeaderElectionService(
            @Lazy LeaderElectionController leaderElectionController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionConfig/electingNewLeader") GateLock electingNewLeader,
            @Qualifier("Node/LeaderElectionConfig/leaderElectionRoundSynchronizer")
                    NodeMessageRoundSynchronizer<LeaderElectionMessage> leaderElectionRoundSynchronizer,
            @Qualifier("Node/LeaderElectionConfig/leaderDistanceRoundSynchronizer")
                    NodeMessageRoundSynchronizer<LeaderDistanceMessage> leaderDistanceRoundSynchronizer
    ) {
        this.leaderElectionController = leaderElectionController;
        this.thisNodeInfo = thisNodeInfo;
        this.electingNewLeader = electingNewLeader;
        this.leaderElectionRoundSynchronizer = leaderElectionRoundSynchronizer;
        this.leaderDistanceRoundSynchronizer = leaderDistanceRoundSynchronizer;

        this.vote = new Vote();
        roundsWithoutChange = 0;
        knowsLeader = false;
    }

    public boolean hasLeader() {
        return knowsLeader;
    }

    public void processNeighborlyAdvice(Queue<LeaderElectionMessage> electionMessages) {
        boolean didMaxUidChange = false;
        boolean didDistanceChange = false;
        for (LeaderElectionMessage nextMessage : electionMessages) {
            int neighborMaxUid = nextMessage.getMaxUidSeen();
            int neighborMaxDistanceSeen = nextMessage.getMaxDistanceSeen();
            if (neighborMaxUid > vote.getMaxUidSeen()) {
                vote.setMaxUidSeen(neighborMaxUid);
                vote.setMaxDistanceSeen(neighborMaxDistanceSeen + 1);
                didMaxUidChange = true;
                didDistanceChange = true;
                if (log.isDebugEnabled()) {
                    log.debug("updated vote uid to: {} in round: {}", neighborMaxUid, leaderElectionRoundSynchronizer.getRoundNumber());
                    log.debug("updated distance to: {} in round: {}", neighborMaxDistanceSeen, leaderElectionRoundSynchronizer.getRoundNumber());
                }
            } else if (neighborMaxUid == vote.getMaxUidSeen() && neighborMaxDistanceSeen > vote.getMaxDistanceSeen()) {
                if (log.isDebugEnabled()) {
                    log.debug("updated distance to: {} in round: {}", neighborMaxDistanceSeen, leaderElectionRoundSynchronizer.getRoundNumber());
                }
                //Note: we don't count this as vote changing
                vote.setMaxDistanceSeen(neighborMaxDistanceSeen);
                didDistanceChange = true;
            }
        }
        if(didMaxUidChange || didDistanceChange) {
            roundsWithoutChange = 0;
        } else {
            roundsWithoutChange++;
        }
        if(log.isTraceEnabled()) {
            log.trace("round without vote change: {}", roundsWithoutChange);
        }
        //increment round in controller before control is handed back
        leaderElectionRoundSynchronizer.incrementRoundNumber();
        if(roundsWithoutChange >= 2 && thisNodeInfo.getUid() == vote.getMaxUidSeen()) {
            log.debug("--------I am leader--------");
            vote.setThisNodeLeader(true);
            vote.setLeaderUid(thisNodeInfo.getUid());
            knowsLeader = true;
            MaxDistanceFromLeader = vote.getMaxDistanceSeen();
            minDistanceToLeader = 0;
            thisNodeInfo.setDistanceToRoot(minDistanceToLeader);
            leaderElectionController.sendLeaderAnnounce(thisNodeInfo.getUid(), MaxDistanceFromLeader);
            leaderElectionController.sendLeaderDistance();
        } else {
            leaderElectionController.sendLeaderElection();
        }
    }

    public void processLeaderAnnouncement(int leaderUid, int MaxDistanceFromLeader) {
        //check if we have already learned of election result and suppress message if so
        if(!knowsLeader) {
            vote.setThisNodeLeader(false);
            vote.setLeaderUid(leaderUid);
            knowsLeader = true;
            this.MaxDistanceFromLeader = MaxDistanceFromLeader;
            //set to a number we know will be higher than any number we will receive
            minDistanceToLeader = MaxDistanceFromLeader + 1;
            leaderElectionController.sendLeaderAnnounce(leaderUid, MaxDistanceFromLeader);
            leaderElectionController.sendLeaderDistance();
        }
    }

    public void processLeaderDistances(Queue<LeaderDistanceMessage> distanceMessages) {
        if(leaderDistanceRoundSynchronizer.getRoundNumber() < MaxDistanceFromLeader) {
            if(!vote.isThisNodeLeader()) {
                for (LeaderDistanceMessage message : distanceMessages) {
                    if (message.getDistance() < minDistanceToLeader) {
                        minDistanceToLeader = message.getDistance();
                    }
                }
            }
            leaderDistanceRoundSynchronizer.incrementRoundNumber();
            leaderElectionController.sendLeaderDistance();
        } else {
            thisNodeInfo.setDistanceToRoot(minDistanceToLeader);
            //only move forward with bfs when we know our real distance from root
            log.trace("done establishing distance to leader, releasing semaphore");
            electingNewLeader.open();
        }
    }

    public int getThisDistanceFromRoot() {
        return thisNodeInfo.getDistanceToRoot();
    }

    public int getCurrentMinDistanceToLeader() {
        return minDistanceToLeader + 1;
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

        private int leaderUid;

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

        public int getLeaderUid() {
            return leaderUid;
        }

        public void setLeaderUid(int leaderUid) {
            this.leaderUid = leaderUid;
        }
    }
}
