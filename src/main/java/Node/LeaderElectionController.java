package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
@Slf4j
public class LeaderElectionController {
    private LeaderElectionService leaderElectionService;
    private SimpMessagingTemplate template;
    private ThisNodeInfo thisNodeInfo;
    private LeaderElectionService.Vote vote;

    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LeaderElectionMessage>> roundMessages;
    private int roundNumber;

    @Autowired(required = true)
    public LeaderElectionController(
            LeaderElectionService leaderElectionService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionService/Vote") LeaderElectionService.Vote vote
    ){
        this.leaderElectionService = leaderElectionService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = vote;

        roundNumber = 0;
        roundMessages = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LeaderElectionMessage>>(1);
        roundMessages.put(roundNumber, new ConcurrentLinkedQueue<>());
    }

    @MessageMapping("/leaderElection")
    public void leaderElection(LeaderElectionMessage message) {
        //TODO: change to trace
        log.error("--------received and routed leader election message");
        int numberOfMessagesSoFarThisRound = roundMessages.get(roundNumber).size();
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        if(message.getRoundNumber() == roundNumber && numberOfMessagesSoFarThisRound == numberOfNeighbors){
            leaderElectionService.processNeighborlyAdvice(getMessagesThisRound());
            roundNumber++;
        } else {
            enqueueMessage(message);
        }
    }

    @MessageMapping("/leaderAnnounce")
    public void leaderAnnounce(LeaderAnnounceMessage message) {
        //TODO implement: send leader's UID to all neighbors
        // EXCEPT THE ONE RECEIVED FROM -or- TURN THIS NODE OFF AFTER ANNOUNCING
    }

    private void enqueueMessage(LeaderElectionMessage message) {
        int messageRoundNumber = message.getRoundNumber();
        ConcurrentLinkedQueue<LeaderElectionMessage> roundMessageQueue;
        if(roundMessages.containsKey(messageRoundNumber)){
            roundMessageQueue = roundMessages.get(messageRoundNumber);
        } else {
            roundMessageQueue = new ConcurrentLinkedQueue<>();
            roundMessages.put(messageRoundNumber, roundMessageQueue);
        }
        roundMessageQueue.add(message);
    }

    private Queue<LeaderElectionMessage> getMessagesThisRound() {
        return roundMessages.get(roundNumber);
    }

    public void announceSelfLeader() throws MessagingException {
        announceLeader(thisNodeInfo.getUid());
    }

    private void announceLeader(int leaderUid) throws MessagingException {
        thisNodeInfo.getNeighbors().parallelStream().forEach(neighbor -> {
            //TODO: change to trace
            log.error("--------creating leader announce message");
            LeaderAnnounceMessage message = new LeaderAnnounceMessage(
                    thisNodeInfo.getUid(),
                    neighbor.getUid(),
                    leaderUid
            );
            template.convertAndSend("/topic/leaderAnnounce", message);
            //TODO: change to trace
            log.error("--------after sending leader announce message");
        });
    }

    public void sendLeaderElection() throws MessagingException {
        //method 1 of broadcasting
        thisNodeInfo.getNeighbors().parallelStream().forEach(neighbor -> {
            //TODO: change to trace
            log.error("--------creating leader election message");
            LeaderElectionMessage message = new LeaderElectionMessage(
                    thisNodeInfo.getUid(),
                    neighbor.getUid(),
                    roundNumber,
                    vote.getMaxUidSeen(),
                    vote.getMaxUidSeen()
                    );
            template.convertAndSend("/topic/leaderElection", message);
            //TODO: change to trace
            log.error("--------after sending leader election message");
        });
    }
}