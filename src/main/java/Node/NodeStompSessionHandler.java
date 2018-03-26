package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class NodeStompSessionHandler extends StompSessionHandlerAdapter {
    private SynchGhsController synchGhsController;
    private BfsTreeController bfsTreeController;
    private CountDownLatch connectionTimeoutLatch;
    private List<String> subscriptionsDestinations;

    @Autowired
    public NodeStompSessionHandler(
            SynchGhsController synchGhsController,
            BfsTreeController bfsTreeController,
            @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
            CountDownLatch connectionTimeoutLatch,
            @Qualifier("Node/NodeConfigurator/subscriptionDestinations")
            List<String> subscriptionsDestinations
    ) {
        this.connectionTimeoutLatch = connectionTimeoutLatch;
        this.synchGhsController = synchGhsController;
        this.bfsTreeController = bfsTreeController;
        this.subscriptionsDestinations = subscriptionsDestinations;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        subscriptionsDestinations.parallelStream().forEach((subscriptionDestination) -> {
            session.subscribe(subscriptionDestination, this);
        });
        //we've connected so cancel the timeout
        connectionTimeoutLatch.countDown();

        log.info("New session: {}", session.getSessionId());
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        log.trace("getting payload type");
        Type payloadType = Object.class;
        switch (stompHeaders.getDestination()) {
            case "/topic/leaderElection":
                payloadType = LeaderElectionMessage.class;
                break;
            case "/topic/leaderAnnounce":
                payloadType = LeaderAnnounceMessage.class;
                break;
            case "/topic/leaderDistance":
                payloadType = LeaderDistanceMessage.class;
                break;
            case "/topic/bfsTreeSearch":
                payloadType = BfsTreeSearchMessage.class;
                break;
            case "/topic/bfsTreeAcknowledge":
                payloadType = BfsTreeAcknowledgeMessage.class;
                break;
            case "/topic/bfsTreeReadyToBuild":
                payloadType = BfsTreeReadyToBuildMessage.class;
                break;
            case "/topic/bfsTreeBuild":
                payloadType = BfsTreeBuildMessage.class;
                break;
            default:
                if (log.isErrorEnabled()) {
                    log.error("unknown destination to determine payload type {}", stompHeaders.getDestination());
                }
                break;
        }
        return payloadType;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object message) {
        if(log.isDebugEnabled()) {
            log.debug("handling frame. Destination: {}", stompHeaders.getDestination());
        }
        switch (stompHeaders.getDestination()) {
            case "/topic/leaderElection":
                log.trace("calling SynchGhsController.leaderElection");
                LeaderElectionMessage leaderElectionMessage = (LeaderElectionMessage) message;
                synchGhsController.leaderElection(leaderElectionMessage);
                break;
            case "/topic/leaderAnnounce":
                log.trace("calling SynchGhsController.leaderAnnounce");
                LeaderAnnounceMessage leaderAnnounceMessage = (LeaderAnnounceMessage) message;
                synchGhsController.leaderAnnounce(leaderAnnounceMessage);
                break;
            case "/topic/leaderDistance":
                log.trace("calling SynchGhsController.leaderDistance");
                LeaderDistanceMessage leaderDistanceMessage = (LeaderDistanceMessage) message;
                synchGhsController.leaderDistance(leaderDistanceMessage);
                break;
            case "/topic/bfsTreeSearch":
                log.trace("calling BfsTreeService.bfsTreeSearch");
                BfsTreeSearchMessage bfsTreeSearchMessage = (BfsTreeSearchMessage) message;
                bfsTreeController.bfsTreeSearch(bfsTreeSearchMessage);
                break;
            case "/topic/bfsTreeAcknowledge":
                log.trace("calling BfsTreeService.bfsTreeAcknowledge");
                BfsTreeAcknowledgeMessage bfsTreeAcknowledgeMessage = (BfsTreeAcknowledgeMessage) message;
                bfsTreeController.bfsTreeAcknowledge(bfsTreeAcknowledgeMessage);
                break;
            case "/topic/bfsTreeReadyToBuild":
                log.trace("calling BfsTreeService.bfsTreeReadyToBuild");
                BfsTreeReadyToBuildMessage bfsTreeReadyToBuildMessage = (BfsTreeReadyToBuildMessage) message;
                bfsTreeController.bfsTreeReadyToBuild(bfsTreeReadyToBuildMessage);
                break;
            case "/topic/bfsTreeBuild":
                log.trace("calling BfsTreeService.bfsTreeBuild");
                BfsTreeBuildMessage bfsTreeBuildMessage = (BfsTreeBuildMessage) message;
                bfsTreeController.bfsTreeBuild(bfsTreeBuildMessage);
                break;
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        if(log.isErrorEnabled()) {
            log.error("error handling message: " + exception.getMessage(), exception);
        }
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        if(log.isErrorEnabled()) {
            log.error("error in transport: " + exception.getMessage(), exception);
        }
        //we've failed to connect so cancel the timeout
        connectionTimeoutLatch.countDown();
    }
}
