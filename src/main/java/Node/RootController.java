package Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RootController {
    @Autowired
    private RootService rootService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    @Qualifier("Node/NodeConfigurator/thisNodeInfo")
    private ThisNodeInfo thisNodeInfo;

    @Autowired
    @Qualifier("Node/WebSocketConnector/sessions")
    private List<StompSession> sessions;

    @MessageMapping("/topic/leaderElection")
    @SendTo("/topic/leaderElection")
    public Message leaderElection(LeaderElectionMessage message) throws Exception {
        rootService.leaderElection(message);
        //TODO: check round number vs current round number. if greater push onto queue
        return new LeaderElectionResponse(thisNodeInfo.getUid(), message.getSourceUID());
    }

    //TODO make this a real messsage, connect isn't needed
    public void sendLeaderElection() throws MessagingException {
        System.out.println("connected? " + sessions.get(0).isConnected());
        //method 1 of broadcasting
        thisNodeInfo.getNeighbors().parallelStream().forEach(neighbor -> {
            LeaderElectionMessage message = new LeaderElectionMessage(thisNodeInfo.getUid(), neighbor.getUid());
            template.convertAndSend("/topic/leaderElection", message);
        });
        //method 2 of broadcasting
        /*
        final LeaderElectionMessage leaderElectionMessage = new LeaderElectionMessage(thisNodeInfo.getUid(), 0);

        sessions.get(0).send("/topic/leaderElection", leaderElectionMessage);
        */
    }
}