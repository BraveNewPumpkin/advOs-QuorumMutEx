package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class SynchGhsController {
    private final SynchGhsService synchGhsService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final SynchGhsService.Vote vote;
    private final GateLock sendingInitialLeaderElectionMessage;
    private final NodeMessageRoundSynchronizer<LeaderElectionMessage> leaderElectionRoundSynchronizer;

    private final Runnable leaderElectionWork;
    private final Runnable leaderDistanceWork;

    @Autowired
    public SynchGhsController(
            SynchGhsService synchGhsService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionConfig/sendingInitialLeaderElectionMessage")
            GateLock sendingInitialLeaderElectionMessage,
            @Qualifier("Node/LeaderElectionConfig/leaderElectionRoundSynchronizer")
            NodeMessageRoundSynchronizer<LeaderElectionMessage> leaderElectionRoundSynchronizer,
            @Qualifier("Node/LeaderElectionConfig/leaderDistanceRoundSynchronizer")
            NodeMessageRoundSynchronizer<LeaderDistanceMessage> leaderDistanceRoundSynchronizer
            ){
        this.synchGhsService = synchGhsService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.vote = synchGhsService.getVote();
        this.sendingInitialLeaderElectionMessage = sendingInitialLeaderElectionMessage;
        this.leaderElectionRoundSynchronizer = leaderElectionRoundSynchronizer;

        leaderElectionWork = () -> {
            synchGhsService.processNeighborlyAdvice(leaderElectionRoundSynchronizer.getMessagesThisRound());
        };
    }

    @MessageMapping("/leaderElection")
    public void leaderElection(LeaderElectionMessage message) {
        if(synchGhsService.hasLeader()) {
            if (log.isDebugEnabled()) {
                log.debug("<---ignoring leader election message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received leader election message {}", message);
            }
            sendingInitialLeaderElectionMessage.enter();
            synchronized (leaderElectionWork) {
                leaderElectionRoundSynchronizer.enqueueAndRunIfReady(
                        message,
                        leaderElectionWork
                );
            }
        }
    }

    public void sendLeaderElection() throws MessagingException {
        LeaderElectionMessage message = new LeaderElectionMessage(
                thisNodeInfo.getUid(),
                leaderElectionRoundSynchronizer.getRoundNumber(),
                vote.getMaxUidSeen(),
                vote.getMaxDistanceSeen()
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending leader election message: {}", message);
        }
        template.convertAndSend("/topic/leaderElection", message);
        log.trace("leader election message sent");
    }

}