package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
@Slf4j
public class LeaderElectionController {
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LeaderElectionMessage>> roundMessages;
    private int roundNumber;

    @Autowired
    private LeaderElectionService leaderElectionService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    private ThisNodeInfo thisNodeInfo;

    public LeaderElectionController(){
        setRoundNumber(0);
        roundMessages = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<LeaderElectionMessage>>(1);
        roundMessages.put(getRoundNumber(), new ConcurrentLinkedQueue<>());
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
//    @Autowired
//    @Qualifier("Node/WebSocketConnector/sessions")
//    private List<StompSession> sessions;

    @MessageMapping("/leaderElection")
    public void leaderElection(LeaderElectionMessage message) {
        //TODO: change to trace
        log.error("--------received and routed leader election message");
        int numberOfMessagesSoFarThisRound = roundMessages.get(getRoundNumber()).size();
        int numberOfNeighbors = thisNodeInfo.getNeighbors().size();
        if(message.getRoundNumber() == getRoundNumber() && numberOfMessagesSoFarThisRound == numberOfNeighbors){
            //TODO process them
            setRoundNumber(getRoundNumber() + 1);
        } else {
            enqueueMessage(message);
        }
        leaderElectionService.leaderElection(message);
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

    public void sendLeaderElection() throws MessagingException {
        //method 1 of broadcasting
        thisNodeInfo.getNeighbors().parallelStream().forEach(neighbor -> {
            //TODO: change to trace
            log.error("--------creating leader election message");
            LeaderElectionMessage message = new LeaderElectionMessage(
                    thisNodeInfo.getUid(),
                    neighbor.getUid(),
                    getRoundNumber(),
                    leaderElectionService.getMaxUidSeen(),
                    leaderElectionService.getMaxUidSeen()
                    );
            template.convertAndSend("/topic/leaderElection", message);
            //TODO: change to trace
            log.error("--------after sending leader election message");
        });
        //method 2 of broadcasting
//        final LeaderElectionMessage leaderElectionMessage = new LeaderElectionMessage(thisNodeInfo.getUid(), 0);
//        sessions.parallelStream().forEach(session -> {
//            session.send("/topic/leaderElection", leaderElectionMessage);
//        });
    }
}