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

@Controller
@Slf4j
public class LeaderElectionController {
    private final LeaderElectionService leaderElectionService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final LeaderElectionService.Vote vote;
    private final Semaphore electingNewLeader;

    private final List<Queue<LeaderElectionMessage>> roundMessages;
    private int roundNumber;

    @Autowired
    public LeaderElectionController(
            LeaderElectionService leaderElectionService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionConfig/electingNewLeader") Semaphore electingNewLeader
            ){
        this.leaderElectionService = leaderElectionService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = leaderElectionService.getVote();
        this.electingNewLeader = electingNewLeader;

        roundNumber = 0;
        roundMessages = new ArrayList<>(1);
        roundMessages.add(new LinkedList<LeaderElectionMessage>());
    }

    public void incrementRoundNumber(){
        roundNumber++;
    }

    @MessageMapping("/leaderElection")
    public void leaderElection(LeaderElectionMessage message) {
        //TODO: change to trace
        log.error("--------received and routed leader election message");
        enqueueMessage(message);
        //TODO what if message if from future round and queue for this round hasn't been created yet?
        int numberOfMessagesSoFarThisRound = roundMessages.get(roundNumber).size();
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        if(numberOfMessagesSoFarThisRound == numberOfNeighbors){
            leaderElectionService.processNeighborlyAdvice(getMessagesThisRound());
        }
    }

    @MessageMapping("/leaderAnnounce")
    public void leaderAnnounce(LeaderAnnounceMessage message) {
        //TODO: change to trace
        log.error("--------received and routed leader announce message");
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
        //TODO: change to trace
        log.error("--------creating leader announce message");
        LeaderAnnounceMessage message = new LeaderAnnounceMessage(
                thisNodeInfo.getUid(),
                -1, //this is basically "we don't know" because we don't send message out to specific node
                leaderUid
        );
        template.convertAndSend("/topic/leaderAnnounce", message);
        //TODO: convert to trace
        log.error("--------done electing leader");
        electingNewLeader.release();
    }

    public void sendLeaderElection() throws MessagingException {
        //method 1 of broadcasting
        //TODO: change to trace
        log.error("--------creating leader election message");
        LeaderElectionMessage message = new LeaderElectionMessage(
                thisNodeInfo.getUid(),
                -1,
                roundNumber,
                vote.getMaxUidSeen(),
                vote.getMaxDistanceSeen()
                );
        template.convertAndSend("/topic/leaderElection", message);
        //TODO: change to trace
        log.error("--------after sending leader election message");
    }
}