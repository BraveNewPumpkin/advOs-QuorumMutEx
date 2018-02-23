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
import java.util.concurrent.Semaphore;

@Controller
@Slf4j
public class LeaderElectionController {
    private final LeaderElectionService leaderElectionService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final LeaderElectionService.Vote vote;
    private final Semaphore electingNewLeader;

    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LeaderElectionMessage>> roundMessages;
    private int roundNumber;

    @Autowired(required = true)
    public LeaderElectionController(
            LeaderElectionService leaderElectionService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionService/Vote") LeaderElectionService.Vote vote,
            @Qualifier("Node/LeaderElectionConfig/electingNewLeader") Semaphore electingNewLeader
            ){
        this.leaderElectionService = leaderElectionService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = vote;
        this.electingNewLeader = electingNewLeader;

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
        //TODO: change to trace
        log.error("--------received and routed leader announce message");
        leaderElectionService.leaderAnnounce(message.getLeaderUid());
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

    public void announceLeader(int leaderUid) throws MessagingException {
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
        electingNewLeader.release();
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