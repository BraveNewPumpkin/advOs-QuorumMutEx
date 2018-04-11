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
    private CountDownLatch connectionTimeoutLatch;
    private List<String> subscriptionsDestinations;

    @Autowired
    public NodeStompSessionHandler(
            SynchGhsController synchGhsController,
            @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
            CountDownLatch connectionTimeoutLatch,
            @Qualifier("Node/NodeConfigurator/subscriptionDestinations")
            List<String> subscriptionsDestinations
    ) {
        this.connectionTimeoutLatch = connectionTimeoutLatch;
        this.synchGhsController = synchGhsController;
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
            case "/topic/mwoeSearch":
                payloadType = MwoeSearchMessage.class;
                break;
            case "/topic/mwoeResponse":
                payloadType = MwoeCandidateMessage.class;
                break;
            case "/topic/mwoeReject":
                payloadType = MwoeRejectMessage.class;
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
            case "/topic/mwoeSearch":
                log.trace("calling SynchGhsController.mwoeSearch");
                MwoeSearchMessage mwoeSearchMessage = (MwoeSearchMessage) message;
                synchGhsController.mwoeSearch(mwoeSearchMessage);
                break;
            case "/topic/mwoeCandidate":
                log.trace("calling SynchGhsController.mwoeCandidate");
                MwoeCandidateMessage mwoeCandidateMessage = (MwoeCandidateMessage) message;
                synchGhsController.mwoeCandidate(mwoeCandidateMessage);
                break;
            case "/topic/mwoeReject":
                log.trace("calling SynchGhsController.mwoeReject");
                MwoeRejectMessage mwoeRejectMessage = (MwoeRejectMessage) message;
                synchGhsController.mwoeReject(mwoeRejectMessage);
                break;
            case "/topic/initiateMerge":
                log.trace("calling SynchGhsController.initiateMerge");
                InitiateMergeMessage initiateMergeMessage = (InitiateMergeMessage) message;
                synchGhsController.initiateMerge(initiateMergeMessage);
                break;
            case "/topic/newLeader":
                log.trace("calling SynchGhsController.newLeader");
                NewLeaderMessage NewLeaderMessage = (NewLeaderMessage) message;
                synchGhsController.newLeader(NewLeaderMessage);
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
