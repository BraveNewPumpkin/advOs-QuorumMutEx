package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;

@Controller
@Slf4j
public class LeaderElectionController {
    private final LeaderElectionService leaderElectionService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final LeaderElectionService.Vote vote;
    private final Semaphore electingNewLeader;
    private final ReadWriteLock sendingInitialLeaderElectionMessage;

    private final List<Queue<LeaderElectionMessage>> roundMessages;
    private int roundNumber;

    @Autowired
    public LeaderElectionController(
            LeaderElectionService leaderElectionService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionConfig/electingNewLeader") Semaphore electingNewLeader,
            @Qualifier("Node/LeaderElectionConfig/sendingInitialLeaderElectionMessage") ReadWriteLock sendingInitialLeaderElectionMessage
            ){
        this.leaderElectionService = leaderElectionService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = leaderElectionService.getVote();
        this.electingNewLeader = electingNewLeader;
        this.sendingInitialLeaderElectionMessage = sendingInitialLeaderElectionMessage;

        roundNumber = 0;
        roundMessages = new ArrayList<>(1);
        roundMessages.add(new LinkedList<LeaderElectionMessage>());
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void incrementRoundNumber(){
        roundNumber++;
    }

    @MessageMapping("/leaderElection")
    public void leaderElection(LeaderElectionMessage message) {
        sendingInitialLeaderElectionMessage.readLock().lock();
        try {
            if (log.isDebugEnabled()) {
                log.debug("--------received leader election message {}", message);
            }

            enqueueMessage(message);
            int numberOfMessagesSoFarThisRound = roundMessages.get(roundNumber).size();
            int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
            if (numberOfMessagesSoFarThisRound == numberOfNeighbors) {
                leaderElectionService.processNeighborlyAdvice(getMessagesThisRound());
            }
        }finally {
            sendingInitialLeaderElectionMessage.readLock().unlock();
        }
    }

    @MessageMapping("/leaderAnnounce")
    public void leaderAnnounce(LeaderAnnounceMessage message) {
        if(log.isDebugEnabled()) {
            log.debug("--------received leader announce message {}", message);
        }
        leaderElectionService.leaderAnnounce(message.getLeaderUid());
    }

    private void enqueueMessage(LeaderElectionMessage message) {
        int messageRoundNumber = message.getRoundNumber();
        int currentRoundIndex = roundMessages.size() - 1;
        if(currentRoundIndex != messageRoundNumber){
            for(int i = currentRoundIndex; i < messageRoundNumber; i++) {
                roundMessages.add(new LinkedList<>());
            }
        }
        roundMessages.get(currentRoundIndex).add(message);
    }

    private Queue<LeaderElectionMessage> getMessagesThisRound() {
        return roundMessages.get(roundNumber);
    }

    public void announceSelfLeader() throws MessagingException {
        announceLeader(thisNodeInfo.getUid());
    }

    public void announceLeader(int leaderUid) throws MessagingException {
        log.trace("creating leader announce message");
        LeaderAnnounceMessage message = new LeaderAnnounceMessage(
                thisNodeInfo.getUid(),
                leaderUid
        );
        template.convertAndSend("/topic/leaderAnnounce", message);
        log.trace("done electing leader, releasing semaphore");
        electingNewLeader.release();
    }

    public void sendLeaderElection() throws MessagingException {
        log.trace("creating leader election message");
        LeaderElectionMessage message = new LeaderElectionMessage(
                thisNodeInfo.getUid(),
                roundNumber,
                vote.getMaxUidSeen(),
                vote.getMaxDistanceSeen()
                );
        template.convertAndSend("/topic/leaderElection", message);
        log.trace("after sending leader election message");
    }
}