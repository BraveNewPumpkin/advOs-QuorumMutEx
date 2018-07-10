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
    private final QuorumMutExController quorumMutExController;
    private final BuildTreeController buildTreeController;
    private final SnapshotController snapshotController;
    private CountDownLatch connectionTimeoutLatch;
    private List<String> subscriptionsDestinations;

    @Autowired
    public NodeStompSessionHandler(
            QuorumMutExController quorumMutExController,
            BuildTreeController buildTreeController,
            SnapshotController snapshotController,
            @Qualifier("Node/NodeConfigurator/connectionTimeoutLatch")
            CountDownLatch connectionTimeoutLatch,
            @Qualifier("Node/NodeConfigurator/subscriptionDestinations")
            List<String> subscriptionsDestinations
    ) {
        this.quorumMutExController = quorumMutExController;
        this.buildTreeController = buildTreeController;
        this.snapshotController = snapshotController;
        this.connectionTimeoutLatch = connectionTimeoutLatch;
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
            case "/topic/mapMessage":
                payloadType = MapMessage.class;
                break;
            case "/topic/buildTreeQueryMessage":
                payloadType = BuildTreeQueryMessage.class;
                break;
            case "/topic/buildTreeAckMessage":
                payloadType = BuildTreeAckMessage.class;
                break;
            case "/topic/buildTreeNackMessage":
                payloadType = BuildTreeNackMessage.class;
                break;
            case "/topic/markMessage":
                payloadType = MarkMessage.class;
                break;
            case "/topic/stateMessage":
                payloadType = StateMessage.class;
                break;
            case "/topic/mapResponseMessage":
            case "/topic/markResponseMessage":
                payloadType = FifoResponseMessage.class;
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
            case "/topic/mapMessage":
                log.trace("calling mapController.mapMessage");
                MapMessage mapMessage = (MapMessage) message;
                quorumMutExController.mapMessage(mapMessage);
                break;
            case "/topic/buildTreeQueryMessage":
                log.trace("calling buildTreeController.buildTreeQueryMessage");
                BuildTreeQueryMessage buildTreeQueryMessage = (BuildTreeQueryMessage) message;
                buildTreeController.receiveBuildTreeQueryMessage(buildTreeQueryMessage);
                break;
            case "/topic/buildTreeAckMessage":
                log.trace("calling buildTreeController.buildTreeAckMessage");
                BuildTreeAckMessage buildTreeAckMessage = (BuildTreeAckMessage) message;
                buildTreeController.receiveBuildTreeAckMessage(buildTreeAckMessage);
                break;
            case "/topic/buildTreeNackMessage":
                log.trace("calling buildTreeController.buildTreeNackMessage");
                BuildTreeNackMessage buildTreeNackMessage = (BuildTreeNackMessage) message;
                buildTreeController.receiveBuildTreeNackMessage(buildTreeNackMessage);
                break;
            case "/topic/markMessage":
                log.trace("calling snapshotController.receiveMarkMessage");
                MarkMessage markMessage = (MarkMessage) message;
                snapshotController.receiveMarkMessage(markMessage);
                break;
            case "/topic/stateMessage":
                log.trace("calling snapshotController.receiveStateMessage");
                StateMessage stateMessage = (StateMessage) message;
                snapshotController.receiveStateMessage(stateMessage);
                break;
            case "/topic/mapResponseMessage":
                log.trace("calling mapController.receiveFifoResponseMessage");
                FifoResponseMessage fifoResponseMessage = (FifoResponseMessage) message;
                quorumMutExController.receiveFifoResponseMessage(fifoResponseMessage);
                break;
            case "/topic/markResponseMessage":
                log.trace("calling snapshotController.receiveFifoResponseMessage");
                FifoResponseMessage fifoResponseMessage2 = (FifoResponseMessage) message;
                snapshotController.receiveFifoResponseMessage(fifoResponseMessage2);
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
